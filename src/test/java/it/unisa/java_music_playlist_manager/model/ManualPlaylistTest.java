package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ManualPlaylistTest {

    private ManualPlaylist playlist;
    private Track traccia1;
    private Track traccia2;

    @BeforeEach
    public void setUp() {
        // Inizializzazione pulita prima di ogni singolo test
        playlist = new ManualPlaylist("Classici Rock");
        traccia1 = new Track("Whole Lotta Love", "Led Zeppelin", "Led Zeppelin II", 334, "Rock", 1969, "path1.mp3");
        traccia2 = new Track("Paranoid", "Black Sabbath", "Paranoid", 170, "Metal", 1970, "path2.mp3");
    }

    // ==========================================
    // 1. TEST VALIDAZIONE TITOLO (EDGE CASES)
    // ==========================================

    @Test
    public void testCostruttoreEsetTitleConSpaziValidi() {
        // Testiamo che una playlist con spazi consentiti venga creata correttamente
        ManualPlaylist plConSpazi = new ManualPlaylist("  Miei Brani Preferiti  ");
        assertEquals("  Miei Brani Preferiti  ", plConSpazi.getTitle(), 
            "Gli spazi intenzionali all'inizio e alla fine devono essere preservati.");
    }

    @Test
    public void testCostruttoreTitoloNulloLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ManualPlaylist(null);
        }, "Il titolo nullo deve lanciare IllegalArgumentException.");
    }

    @Test
    public void testCostruttoreTitoloVuotoLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ManualPlaylist("");
        }, "Il titolo vuoto deve lanciare IllegalArgumentException.");
    }

    @Test
    public void testCostruttoreSoloSpaziLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ManualPlaylist("     ");
        }, "Un titolo di soli spazi deve essere rifiutato anche se non si usa il trim sul valore finale.");
    }

    // ==========================================
    // 2. TEST GESTIONE TRACCE & DUPLICATI
    // ==========================================

    @Test
    public void testAggiuntaElementiEControlloDuplicati() {
        playlist.addTrack(traccia1);
        playlist.addTrack(traccia2);
        
        // Tentiamo di aggiungere nuovamente la traccia1 (stesso ID)
        playlist.addTrack(traccia1);

        // La durata totale DEVE considerare il duplicato secondo i nuovi requisiti.
        int durataAttesa = traccia1.getDuration() * 2 + traccia2.getDuration(); 
        assertEquals(durataAttesa, playlist.getDuration(), 
            "La playlist deve permettere l'inserimento di tracce duplicate.");
    }

    @Test
    public void testRimozioneElemento() {
        playlist.addTrack(traccia1);
        playlist.addTrack(traccia2);
        
        boolean rimosso = playlist.removeTrack(traccia1);
        
        assertTrue(rimosso, "La rimozione di un elemento esistente deve restituire true.");
        assertEquals(traccia2.getDuration(), playlist.getDuration(), 
            "Dopo la rimozione, la durata deve aggiornarsi dinamicamente.");
    }

    // ==========================================
    // 3. TEST CALCOLO DURATA DINAMICA
    // ==========================================

    @Test
    public void testCalcoloDurataPlaylistVuota() {
        ManualPlaylist playlistVuota = new ManualPlaylist("Vuota");
        assertEquals(0, playlistVuota.getDuration(), 
            "Una playlist senza brani deve avere durata pari a 0 secondi.");
    }

    @Test
    public void testPlaylistAnnidataRestituisceTutteLeTracce() {
        ManualPlaylist playlistAnnidata = new ManualPlaylist("Annidata");
        playlistAnnidata.addTrack(traccia2);
        playlist.addTrack(traccia1);
        playlist.add(playlistAnnidata);

        assertEquals(2, playlist.getTrackCount(),
                "Il composite deve appiattire anche le playlist annidate.");
        assertEquals(traccia1.getDuration() + traccia2.getDuration(), playlist.getDuration(),
                "La durata deve includere le tracce delle playlist annidate.");
    }

    @Test
    public void testPlaylistNonPuoContenereSeStessa() {
        assertThrows(IllegalArgumentException.class, () -> playlist.add(playlist),
                "Una playlist non deve poter contenere se stessa.");
    }

    @Test
    public void testPlaylistNonPuoCreareCicliIndiretti() {
        ManualPlaylist playlistAnnidata = new ManualPlaylist("Annidata");
        playlist.add(playlistAnnidata);

        assertThrows(IllegalArgumentException.class, () -> playlistAnnidata.add(playlist),
                "Il composite deve rifiutare cicli indiretti tra playlist.");
    }
}
