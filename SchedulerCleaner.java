package pack;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerCleaner {
    private static String CACHE_DIRECTORY;
    private static long CACHE_DELAY;
    private static long CACHE_TTL;
    private static long IMAGE_TTL;
    private static long JS_CSS_TTL;
    private static long PHP_TTL;
    private static long SUFFIX_TTL;

    static {
        Config configManager = new Config();
        configManager.loadConfiguration("config.conf");
        CACHE_DIRECTORY = configManager.getCacheDirectory();
        CACHE_DELAY = configManager.getCacheDelay();
        CACHE_TTL = configManager.getCacheTTL();
        IMAGE_TTL = configManager.getExpImage();
        JS_CSS_TTL = configManager.getExpCss();
        PHP_TTL = configManager.getExpPhp();
        SUFFIX_TTL = configManager.getExpSuffix();
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

        scheduler.scheduleAtFixedRate(() -> {
            // System.out.println("Nettoyage du cache...");
            Cache.clearCache();
        }, CACHE_TTL, CACHE_TTL, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            // System.out.println("Nettoyage des fichiers images...");
            cleanFilesWithExtensions(".*\\.(jpg|jpeg|svg|png|gif|bmp|tiff|webp)$", IMAGE_TTL);
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            cleanFilesWithExtensions(".*\\.(js|css)$", JS_CSS_TTL);
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            // System.out.println("Nettoyage des fichiers PHP...");
            cleanFilesWithExtensions(".*\\.php$", PHP_TTL);
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            // System.out.println("Nettoyage des fichiers se terminant par '_'");
            cleanFilesWithSuffix("_", SUFFIX_TTL);
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void cleanFilesWithExtensions(String regex, long ttl) {
        File cacheDir = new File(CACHE_DIRECTORY);
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null) {
                for (File file : cacheFiles) {
                    if (file.getName().matches(regex)) {
                        long fileAge = System.currentTimeMillis() - file.lastModified();
                        if (fileAge > ttl) {
                            Cache.deleteFromCache(file.getName());
                            System.out.println("Fichier supprimé : " + file.getName());
                        }
                    }
                }
            }
        }
    }

    private void cleanFilesWithSuffix(String suffix, long ttl) {
        File cacheDir = new File(CACHE_DIRECTORY);
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null) {
                for (File file : cacheFiles) {
                    if (file.getName().endsWith(suffix)) {
                        long fileAge = System.currentTimeMillis() - file.lastModified();
                        if (fileAge > ttl) {
                            Cache.deleteFromCache(file.getName());
                            System.out.println("Fichier supprimé : " + file.getName());
                        }
                    }
                }
            }
        }
    }
}
