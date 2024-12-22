package pack;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;


public class Client implements Runnable {
    private final Socket clientSocket; 
    private static String[] blockedUrls;
    private static String urlConf;

    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    static {
        Config configManager = new Config();
        configManager.loadConfiguration("config.conf");
        blockedUrls = configManager.getBlockedUrls();
        urlConf="http://"+configManager.getProxyIP()+":"+configManager.getApachePort();
    }

    @Override
    public void run() {
        try (
            InputStream clientInput = clientSocket.getInputStream();
            OutputStream clientOutput = clientSocket.getOutputStream(); 
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput, StandardCharsets.UTF_8)); 
            BufferedOutputStream bos = new BufferedOutputStream(clientOutput)
        ) {
            String requestLine = reader.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                System.err.println("Requête non valide reçue : " + requestLine);
                bos.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                return;
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                System.err.println("Requête mal formée.");
                bos.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                return;
            }

            String urlString = requestParts[1];

            for (String blockedUrl : blockedUrls) {
                if (urlString.contains(blockedUrl)) {
                    System.err.println("Accès bloqué pour l'URL : " + urlString);
                    bos.write("HTTP/1.1 403 Forbidden\r\n\r\nAccès interdit\r\n".getBytes(StandardCharsets.UTF_8));
                    bos.flush();
                    return;
                }
            }
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                //urlString = "http://localhost" + urlString;
                urlString = urlConf+urlString;
            }

            URI uri = new URI(urlString);
            String host = uri.getHost();
            int port = 80; 

            byte[] cachedData = Cache.get(urlString);
            if (cachedData != null) {
                System.out.println("Cache hit pour l'URL : " + urlString);
                bos.write(cachedData);  
                bos.flush();
                return;
            }

            //raha toa ka tsisy visible ao anaty cache de trycatch any am serveur distant
            try (
                Socket serverSocket = new Socket(host, port);
                OutputStream serverOutput = serverSocket.getOutputStream();
                InputStream serverInput = serverSocket.getInputStream(); 
            ) {
                String requestHeaders = requestLine + "\r\n" +
                                        "Host: " + host + "\r\n" +
                                        "Connection: close\r\n\r\n";
                serverOutput.write(requestHeaders.getBytes(StandardCharsets.UTF_8));
                serverOutput.flush();

                ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192]; 
                int bytesRead;
                while ((bytesRead = serverInput.read(buffer)) != -1) {
                    responseBuffer.write(buffer, 0, bytesRead);  // Stockena mapiasa  tampon tableauByte,indexDepart,NbOctet
                }
                byte[] serverResponse = responseBuffer.toByteArray();
                Cache.put(urlString, serverResponse);  

                // mamerina mandefa valiny serveur ho any client
                bos.write(serverResponse);
                bos.flush();
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Erreur lors du traitement de la requête : " + e.getMessage());
            e.printStackTrace();
            try {
                clientSocket.getOutputStream().write("HTTP/1.1 500 Internal Server Error\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignored) {}  
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}
