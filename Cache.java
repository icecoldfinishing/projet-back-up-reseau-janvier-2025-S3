package pack;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Cache {
    private static String CACHE_DIRECTORY;
    private static long CACHE_TTL;
    private static final Map<String, byte[]> memoryCache = new HashMap<>();

    static {
        Config configManager = new Config();
        configManager.loadConfiguration("config.conf");
        CACHE_DIRECTORY = configManager.getCacheDirectory();
        CACHE_TTL = configManager.getCacheTTL();

        File cacheDir = new File(CACHE_DIRECTORY);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
    }


    public static synchronized byte[] get(String url) {
        String cacheFileName = getCacheFileName(url);
        if (memoryCache.containsKey(cacheFileName)) {
            System.out.println("Données trouvées en mémoire pour : " + cacheFileName);
            return memoryCache.get(cacheFileName);
        }
        File cacheFile = new File(CACHE_DIRECTORY, cacheFileName);
        if (cacheFile.exists() && (System.currentTimeMillis() - cacheFile.lastModified()) <= CACHE_TTL) {
            try (FileInputStream fis = new FileInputStream(cacheFile);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                byte[] data = bos.toByteArray();
                memoryCache.put(cacheFileName, data);
                return data;
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture du cache: " + e.getMessage());
            }
        }
        return null;
    }


    public static synchronized void put(String url, byte[] data) {
        String cacheFileName = getCacheFileName(url);
        memoryCache.put(cacheFileName, data);

        // Enregistrer les données dans un fichier sur disque
        File cacheFile = new File(CACHE_DIRECTORY, cacheFileName);
        System.out.println("Nom du fichier de cache créé : " + cacheFile.getAbsolutePath());
        try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
            fos.write(data);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le cache: " + e.getMessage());
        }
    }

    public static synchronized void deleteFromCache(String url) {
        String cacheFileName = getCacheFileName(url); 

        if (memoryCache.remove(cacheFileName) != null) {
            System.out.println("Données supprimées du cache en mémoire pour : " + cacheFileName);
        } else {
            System.out.println("Aucune donnée correspondante dans le cache en mémoire pour : " + cacheFileName);
        }

        File cacheFile = new File(CACHE_DIRECTORY, cacheFileName);
        try {
            if (cacheFile.exists() && cacheFile.delete()) {
                System.out.println("Fichier de cache supprimé : " + cacheFileName);
            } else {
                System.err.println("Erreur lors de la suppression du fichier cache : " + cacheFileName);
            }
        } catch (SecurityException e) {
            System.err.println("Permission refusée pour supprimer le fichier de cache : " + cacheFileName);
        }
    }

    public static synchronized void clearCache() {
        memoryCache.clear();
        System.out.println("Cache en mémoire vidé.");
        File cacheDir = new File(CACHE_DIRECTORY);
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null) {
                for (File file : cacheFiles) {
                    if (file.isFile() && !file.delete()) {
                        System.err.println("Erreur lors de la suppression du fichier cache : " + file.getName());
                    }
                }
            }
        }
        System.out.println("Cache sur disque vidé.");
    }

    private static String getCacheFileName(String url) {
        return url.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    public static synchronized void listCache() {
        System.out.println("Contenu du cache en mémoire :");
        if (memoryCache.isEmpty()) {
            System.out.println("  - Aucun élément en mémoire.");
        } else {
            memoryCache.keySet().forEach(key -> System.out.println("  - " + key));
        }

        System.out.println("Contenu du cache sur disque :");
        File cacheDir = new File(CACHE_DIRECTORY);
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null && cacheFiles.length > 0) {
                for (File file : cacheFiles) {
                    System.out.println("  - " + file.getName());
                }
            } else {
                System.out.println("  - Aucun fichier dans le cache sur disque.");
            }
        } else {
            System.err.println("Le répertoire de cache n'existe pas ou n'est pas accessible : " + CACHE_DIRECTORY);
        }
    }

    public static synchronized long getCacheSize() {
        long totalSize = 0;
        for (byte[] data : memoryCache.values()) {
            totalSize += data.length;
        }
        File cacheDir = new File(CACHE_DIRECTORY);
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null) {
                for (File file : cacheFiles) {
                    if (file.isFile()) {
                        totalSize += file.length();
                    }
                }
            }
        }

        return totalSize;
    }

    public static synchronized long getCacheSize(String url) {
        String cacheFileName = getCacheFileName(url);
        long size = 0;
        if (memoryCache.containsKey(cacheFileName)) {
            size += memoryCache.get(cacheFileName).length;
        }
        File cacheFile = new File(CACHE_DIRECTORY, cacheFileName);
        if (cacheFile.exists()) {
            size += cacheFile.length();
        }

        return size;
    }


}
