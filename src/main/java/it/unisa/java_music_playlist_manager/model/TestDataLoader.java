package it.unisa.java_music_playlist_manager.model;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe di utilità per caricare brani di test da un file JSON senza utilizzare librerie esterne,
 * in modo da mantenere l'architettura inalterata e non aggiungere dipendenze non necessarie.
 */
public class TestDataLoader {

    public static void loadTestData(String jsonFilePath) {
        try {
            if (!Files.exists(Paths.get(jsonFilePath))) {
                return; // Esce silenziosamente se il file non esiste
            }
            
            String content = Files.readString(Paths.get(jsonFilePath));
            
            // Un parser JSON naif basato su espressioni regolari per mantenere l'architettura pulita.
            // Si assume una struttura piatta: un array di oggetti { "chiave": "valore", "chiave": numero }
            Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(content);
            while (m.find()) {
                String obj = m.group(1);
                String title = extractJsonValue(obj, "title", "Sconosciuto");
                String author = extractJsonValue(obj, "author", "Sconosciuto");
                String album = extractJsonValue(obj, "album", "Sconosciuto");
                int duration = parseIntSafe(extractJsonValue(obj, "duration", "0"));
                String genre = extractJsonValue(obj, "genre", "Generico");
                int year = parseIntSafe(extractJsonValue(obj, "year", "2024"));
                String filePath = extractJsonValue(obj, "filePath", "dummy_path_" + System.currentTimeMillis() + ".mp3");
                
                try {
                    Library.getInstance().addTrack(new Track(title, author, album, duration, genre, year, filePath));
                } catch (IllegalArgumentException e) {
                    System.err.println("Skipped track: " + e.getMessage());
                }
            }
            System.out.println("Brani di test caricati con successo da " + jsonFilePath);
            
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento dei dati di test: " + e.getMessage());
        }
    }

    private static String extractJsonValue(String jsonObject, String key, String defaultValue) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(?:\"([^\"]+)\"|([\\d]+))").matcher(jsonObject);
        if (m.find()) {
            return m.group(1) != null ? m.group(1) : m.group(2);
        }
        return defaultValue;
    }

    private static int parseIntSafe(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
