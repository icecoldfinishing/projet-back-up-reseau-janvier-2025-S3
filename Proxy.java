package pack;
import java.util.concurrent.ThreadPoolExecutor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Proxy {
    private int port;
    private String ipAddress;
    private static ExecutorService threadPool;

    public Proxy(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    static {
        Config configManager = new Config();
        configManager.loadConfiguration("config.conf");
        int nbThreads = configManager.getThread();
        threadPool = Executors.newFixedThreadPool(nbThreads);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ipAddress))) {
            System.out.println("Serveur proxy démarré. IP: " + ipAddress + ", Port: " + port);

            new Thread(new Command()).start();
            new SchedulerCleaner().start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connecté: " + clientSocket.getInetAddress());

                if (((ThreadPoolExecutor) threadPool).getActiveCount() < 10) { 
                    threadPool.execute(new Client(clientSocket));
                } else {
                    System.out.println("Nombre maximum de threads atteint. Connexion refusée pour : "
                            + clientSocket.getInetAddress());
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'initialisation du serveur : " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
