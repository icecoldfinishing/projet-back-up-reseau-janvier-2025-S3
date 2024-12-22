package pack;
//javac -d . *.java && java Main

public class Main {
    public static void main(String[] args) {
        Config config = new Config();
        config.loadConfiguration("config.conf");
        String proxyIP = config.getProxyIP();
        int proxyPort = config.getProxyPort();

        Proxy server = new Proxy(proxyIP, proxyPort);
        server.start();
    }
}
