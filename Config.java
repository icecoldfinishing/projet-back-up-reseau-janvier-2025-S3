package pack;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private Map<String, Map<String, String>> configSections = new HashMap<>();

    public void loadConfiguration(String configFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String currentSection = null;
            Map<String, String> currentProperties = null;

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1).trim();
                    currentProperties = new HashMap<>();
                    configSections.put(currentSection, currentProperties);
                } else if (currentProperties != null) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        currentProperties.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }

            // System.out.println("Fichier de configuration chargé avec succès !");

            configSections.forEach((section, properties) -> {
                // System.out.println("[" + section + "]");
                properties.forEach((key, value) -> {
                    // System.out.println(key + "=" + value);
                });
            });
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier de configuration : " + e.getMessage());
        }
    }

    public String getCacheDirectory() {
        Map<String, String> cacheProperties = configSections.get("cache");
        if (cacheProperties != null) {
            String directory = cacheProperties.get("cache_directory");
            if (directory != null) {
                return directory;
            } else {
                System.err.println(
                        "Erreur : Le répertoire de cache (nomdossier) n'a pas été trouvé dans le fichier de configuration.");
            }
        }
        return "cache";
    }

    public long getCacheTTL() {
        Map<String, String> cacheProperties = configSections.get("cache");
        if (cacheProperties != null) {
            String ttlString = cacheProperties.get("cache_ttl");
            if (ttlString != null) {
                try {
                    return Long.parseLong(ttlString);
                } catch (NumberFormatException e) {
                    System.err.println("Erreur de format pour TTL, utilisation de la valeur par défaut de 60000.");
                }
            } else {
                System.err.println("Erreur : Le TTL n'a pas été trouvé dans le fichier de configuration.");
            }
        }
        return 60000;
    }

    public long getCacheDelay() {
        Map<String, String> cacheProperties = configSections.get("cache");
        if (cacheProperties != null) {
            String ttlString = cacheProperties.get("cache_delay");
            if (ttlString != null) {
                try {
                    return Long.parseLong(ttlString);
                } catch (NumberFormatException e) {
                    System.err.println("Erreur de format pour delay, utilisation de la valeur par défaut de 60000.");
                }
            } else {
                System.err.println("Erreur : Le Delay n'a pas été trouvé dans le fichier de configuration.");
            }
        }
        return 60000;
    }

    public String getProxyIP() {
        Map<String, String> proxyProperties = configSections.get("proxy");
        if (proxyProperties != null) {
            return proxyProperties.getOrDefault("proxy_ip", "127.0.0.1");
        }
        return "127.0.0.1";
    }

    public int getProxyPort() {
        Map<String, String> proxyProperties = configSections.get("proxy");
        if (proxyProperties != null) {
            String portString = proxyProperties.get("proxy_port");
            try {
                return Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format pour le port proxy, utilisation de la valeur par défaut de 8080.");
            }
        }
        return 8080;
    }

    public String getApacheHost() {
        Map<String, String> apacheProperties = configSections.get("apache");
        if (apacheProperties != null) {
            return apacheProperties.getOrDefault("host", "127.0.0.1");
        }
        return "127.0.0.1";
    }

    public int getApachePort() {
        Map<String, String> apacheProperties = configSections.get("apache");
        if (apacheProperties != null) {
            String portString = apacheProperties.get("apache_port");
            try {
                return Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format pour le port apache, utilisation de la valeur par défaut de 80.");
            }
        }
        return 80;
    }

    public String[] getBlockedUrls() {
        Map<String, String> bloqueProperties = configSections.get("bloque");
        if (bloqueProperties != null) {
            return bloqueProperties.values().toArray(new String[0]);
        }
        return new String[0];
    }

    public int getThread() {
        Map<String, String> secondMap = configSections.get("thread");
        if (secondMap != null) {
            String threadString = secondMap.get("max_thread");
            try {
                return Integer.parseInt(threadString);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format pour le max_thread, utilisation de la valeur par défaut de 20.");
            }
        }
        return 20;
    }

    public int getExpImage() {
        Map<String, String> secondMap = configSections.get("expiration");
        if (secondMap != null) {
            String sString = secondMap.get("image");
            try {
                return Integer.parseInt(sString);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format ");
            }
        }
        return 60000;
    }

    public int getExpCss() {
        Map<String, String> expirationProperties = configSections.get("expiration");
        if (expirationProperties != null) {
            String sString = expirationProperties.get("css");
            try {
                return Integer.parseInt(sString);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format pour l'expiration du CSS");
            }
        }
        return 60000; 
    }

    public int getExpPhp() {
        Map<String, String> expirationProperties = configSections.get("expiration");
        if (expirationProperties != null) {
            String sString = expirationProperties.get("php");
            try {
                return Integer.parseInt(sString);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format pour l'expiration du PHP");
            }
        }
        return 60000; 
    }

    public int getExpSuffix() {
        Map<String, String> expirationProperties = configSections.get("expiration");
        if (expirationProperties != null) {
            String sString = expirationProperties.get("suffix");
            try {
                return Integer.parseInt(sString);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de format pour l'expiration du suffixe");
            }
        }
        return 60000; 
    }
}
