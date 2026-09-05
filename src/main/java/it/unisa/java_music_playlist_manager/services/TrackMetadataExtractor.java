package it.unisa.java_music_playlist_manager.services;

import it.unisa.java_music_playlist_manager.model.Track;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility per l'estrazione dei metadati (ID3, etc.) dai file audio.
 * Incapsula la dipendenza da jaudiotagger, tenendola separata dalla UI.
 */
public class TrackMetadataExtractor {

    private static final Logger LOGGER = Logger.getLogger(TrackMetadataExtractor.class.getName());

    /**
     * Mappa dei codici numerici ID3v1 nei corrispondenti nomi di genere testuali.
     * jaudiotagger a volte restituisce il codice grezzo "(N)" invece del nome.
     */
    private static final Map<Integer, String> ID3V1_GENRES = Map.ofEntries(
        Map.entry(0,  "Blues"),
        Map.entry(1,  "Classic Rock"),
        Map.entry(2,  "Country"),
        Map.entry(3,  "Dance"),
        Map.entry(4,  "Disco"),
        Map.entry(5,  "Funk"),
        Map.entry(6,  "Grunge"),
        Map.entry(7,  "Hip-Hop"),
        Map.entry(8,  "Jazz"),
        Map.entry(9,  "Metal"),
        Map.entry(10, "New Age"),
        Map.entry(11, "Oldies"),
        Map.entry(12, "Other"),
        Map.entry(13, "Pop"),
        Map.entry(14, "R&B"),
        Map.entry(15, "Rap"),
        Map.entry(16, "Reggae"),
        Map.entry(17, "Rock"),
        Map.entry(18, "Techno"),
        Map.entry(19, "Industrial"),
        Map.entry(20, "Alternative"),
        Map.entry(21, "Ska"),
        Map.entry(22, "Death Metal"),
        Map.entry(23, "Pranks"),
        Map.entry(24, "Soundtrack"),
        Map.entry(25, "Euro-Techno"),
        Map.entry(26, "Ambient"),
        Map.entry(27, "Trip-Hop"),
        Map.entry(28, "Vocal"),
        Map.entry(29, "Jazz+Funk"),
        Map.entry(30, "Fusion"),
        Map.entry(31, "Trance"),
        Map.entry(32, "Classical"),
        Map.entry(33, "Instrumental"),
        Map.entry(34, "Acid"),
        Map.entry(35, "House"),
        Map.entry(36, "Game"),
        Map.entry(37, "Sound Clip"),
        Map.entry(38, "Gospel"),
        Map.entry(39, "Noise"),
        Map.entry(40, "Alt. Rock"),
        Map.entry(41, "Bass"),
        Map.entry(42, "Soul"),
        Map.entry(43, "Punk"),
        Map.entry(44, "Space"),
        Map.entry(45, "Meditative"),
        Map.entry(46, "Instrumental Pop"),
        Map.entry(47, "Instrumental Rock"),
        Map.entry(48, "Ethnic"),
        Map.entry(49, "Gothic"),
        Map.entry(50, "Darkwave"),
        Map.entry(51, "Techno-Industrial"),
        Map.entry(52, "Electronic"),
        Map.entry(53, "Pop-Folk"),
        Map.entry(54, "Eurodance"),
        Map.entry(55, "Dream"),
        Map.entry(56, "Southern Rock"),
        Map.entry(57, "Comedy"),
        Map.entry(58, "Cult"),
        Map.entry(59, "Gangsta Rap"),
        Map.entry(60, "Top 40"),
        Map.entry(61, "Christian Rap"),
        Map.entry(62, "Pop/Funk"),
        Map.entry(63, "Jungle"),
        Map.entry(64, "Native American"),
        Map.entry(65, "Cabaret"),
        Map.entry(66, "New Wave"),
        Map.entry(67, "Psychedelic"),
        Map.entry(68, "Rave"),
        Map.entry(69, "Showtunes"),
        Map.entry(70, "Trailer"),
        Map.entry(71, "Lo-Fi"),
        Map.entry(72, "Tribal"),
        Map.entry(73, "Acid Punk"),
        Map.entry(74, "Acid Jazz"),
        Map.entry(75, "Polka"),
        Map.entry(76, "Retro"),
        Map.entry(77, "Musical"),
        Map.entry(78, "Rock & Roll"),
        Map.entry(79, "Hard Rock")
    );

    /**
     * Normalizza una stringa genere letta da ID3, convertendo il formato
     * numerico "(N)" (standard ID3v1) nel corrispondente nome testuale.
     * Se la stringa è già un nome testuale (es. "Rock", "Pop"), viene restituita
     * invariata. Restituisce {@code null} se la stringa è nulla o vuota.
     *
     * @param raw La stringa grezza letta dal tag ID3
     * @return Il nome del genere testuale, o {@code null} se non disponibile
     */
    static String normalizeGenre(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        // Pattern ID3v1: "(N)" oppure "(N)Nome" — teniamo il nome se presente
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\\((\\d+)\\)(.*)$").matcher(trimmed);
        if (m.matches()) {
            String suffix = m.group(2).trim();
            if (!suffix.isEmpty()) return suffix; // es. "(17)Rock" → "Rock"
            int code = Integer.parseInt(m.group(1));
            return ID3V1_GENRES.get(code);
        }
        // Potrebbe essere solo il numero senza parentesi (raro ma possibile)
        try {
            int code = Integer.parseInt(trimmed);
            return ID3V1_GENRES.getOrDefault(code, trimmed);
        } catch (NumberFormatException ignored) {
            // È già una stringa testuale, restituiscila così
            return trimmed;
        }
    }

    private static String safeGet(Tag tag, FieldKey key) {
        if (tag == null) return null;
        try {
            String value = tag.getFirst(key);
            return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Estrae i metadati da un file audio e costruisce un oggetto Track.
     * Se l'estrazione fallisce per qualsiasi motivo, restituisce un Track di fallback basato sul nome file.
     *
     * @param file Il file audio da analizzare
     * @return L'oggetto Track costruito
     */
    public static Track extractMetadata(File file) {
        String title = file.getName().replaceFirst("[.][^.]+$", "");
        String author = "Artista Sconosciuto";
        String album = "";
        String genre = "Altro";
        Integer year = null;
        int duration = 0;

        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            
            String extractedTitle = safeGet(tag, FieldKey.TITLE);
            if (extractedTitle != null) title = extractedTitle;
            
            String extractedAuthor = safeGet(tag, FieldKey.ARTIST);
            if (extractedAuthor != null) author = extractedAuthor;
            
            String extractedAlbum = safeGet(tag, FieldKey.ALBUM);
            if (extractedAlbum != null) album = extractedAlbum;
            
            String rawGenre = safeGet(tag, FieldKey.GENRE);
            String extractedGenre = normalizeGenre(rawGenre);
            if (extractedGenre != null && !extractedGenre.isBlank()) genre = extractedGenre;
            
            String yearStr = safeGet(tag, FieldKey.YEAR);
            if (yearStr != null && !yearStr.isEmpty()) {
                try { 
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d{4}").matcher(yearStr);
                    if (m.find()) {
                        int parsedYear = Integer.parseInt(m.group());
                        // Evitiamo anni futuri o inverosimili che farebbero crashare il costruttore di Track
                        if (parsedYear <= java.time.LocalDate.now().getYear() && parsedYear >= 1000) {
                            year = parsedYear;
                        }
                    }
                } catch (Exception ignored) {}
            }
            
            if (f.getAudioHeader() != null) {
                duration = f.getAudioHeader().getTrackLength();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Errore generico durante l'estrazione metadati per il file " + file.getName(), e);
        }
        
        try {
            return new Track(title, author, album, duration, genre, year, file.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Parametri rigidi invalidi per il brano " + file.getName() + ". Verrà usato il fallback.", e);
            // Se fallisce (es. titolo vuoto o altro vincolo rigido), forziamo valori sicuramente validi
            if (title == null || title.trim().isEmpty()) title = file.getName();
            return new Track(title, "Artista Sconosciuto", "", duration, "Altro", null, file.getAbsolutePath());
        }
    }
}
