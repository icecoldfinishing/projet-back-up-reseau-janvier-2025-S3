package pack;

import java.util.Scanner;

public class Command implements Runnable {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("~ ");
            String command = scanner.nextLine().trim();
            String[] commandParts = command.split(" ", 2);

            switch (commandParts[0].toLowerCase()) {
                case "bye":
                    System.out.println("Arrêt du serveur");
                    System.exit(0);
                    break;
                case "clear":
                    Cache.clearCache();
                    System.out.println("Le cache a été vidé");
                    break;
                case "ls":
                    Cache.listCache();
                    break;
                case "rm":
                    if (commandParts.length < 2) {
                        System.out.println("Veuillez spécifier l'URL à supprimer du cache.");
                    } else {
                        String urlToDelete = commandParts[1];
                        Cache.deleteFromCache(urlToDelete);
                    }
                    break;
                case "size":
                    if (commandParts.length < 2) {
                        long cacheSize = Cache.getCacheSize();
                        System.out.println("Taille actuelle du cache : " + cacheSize + " octets");
                    } else {
                        String urlToCheck = commandParts[1];
                        long urlSize = Cache.getCacheSize(urlToCheck);
                        System.out.println("Taille du cache pour l'URL " + urlToCheck + " : " + urlSize + " octets");
                    }
                    break;
                default:
                    System.out.println("Commande inconnue");
                    break;
            }
        }
    }
}
