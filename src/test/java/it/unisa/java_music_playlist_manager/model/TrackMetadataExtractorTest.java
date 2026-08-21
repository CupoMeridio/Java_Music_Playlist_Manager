package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrackMetadataExtractorTest {

    @Test
    void testNormalizeGenre() {
        // Casi standard testuali
        assertEquals("Rock", TrackMetadataExtractor.normalizeGenre("Rock"));
        assertEquals("Pop", TrackMetadataExtractor.normalizeGenre("Pop"));
        
        // Formato ID3v1 numerico esatto
        assertEquals("Hard Rock", TrackMetadataExtractor.normalizeGenre("(79)"));
        assertEquals("Pop", TrackMetadataExtractor.normalizeGenre("(13)"));
        
        // Formato ID3v1 numerico con nome in coda
        assertEquals("Rock", TrackMetadataExtractor.normalizeGenre("(17)Rock"));
        assertEquals("Pop", TrackMetadataExtractor.normalizeGenre("(13)Pop"));
        
        // Formato numerico ma codice inesistente/fuori range
        assertNull(TrackMetadataExtractor.normalizeGenre("(200)"));
        assertEquals("Sconosciuto", TrackMetadataExtractor.normalizeGenre("(999)Sconosciuto"));
        
        // Formato senza parentesi ma numerico puro (caso limite descritto nei commenti)
        assertEquals("Rock", TrackMetadataExtractor.normalizeGenre("17"));
        
        // Input nulli o vuoti
        assertNull(TrackMetadataExtractor.normalizeGenre(""));
        assertNull(TrackMetadataExtractor.normalizeGenre("   "));
        assertNull(TrackMetadataExtractor.normalizeGenre(null));
    }
}
