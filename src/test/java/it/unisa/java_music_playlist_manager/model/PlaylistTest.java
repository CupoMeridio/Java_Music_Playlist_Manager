package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlaylistTest {

    private Playlist playlist;
    private Track traccia1;
    private Track traccia2;

    @BeforeEach
    public void setUp() {
        // Inizializzazione pulita prima di ogni singolo test
        playlist = new Playlist("Classici Rock");
        traccia1 = new Track("Whole Lotta Love", "Led Zeppelin", 334, "Rock", 1969);
        traccia2 = new Track("Paranoid", "Black Sabbath", 170, "Metal", 1970);
    }

    // ==========================================
    // 1. TEST VALIDAZIONE TITOLO (EDGE CASES)
    // ==========================================

    @Test
    public void testCostruttoreEsetTitleConSpaziValidi() {
        // Testiamo che una playlist con spazi consentiti venga creata correttamente
        Playlist plConSpazi = new Playlist("  Miei Brani Preferiti  ");
        assertEquals("  Miei Brani Preferiti  ", plConSpazi.getTitle(), 
            "Gli spazi intenzionali all'inizio e alla fine devono essere preservati.");
    }

    @Test
    public void testCostruttoreTitoloNulloLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Playlist(null);
        }, "Il titolo nullo deve lanciare IllegalArgumentException.");
    }

    @Test
    public void testCostruttoreTitoloVuotoLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Playlist("");
        }, "Il titolo vuoto deve lanciare IllegalArgumentException.");
    }

    @Test
    public void testCostruttoreSoloSpaziLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Playlist("     ");
        }, "Un titolo di soli spazi deve essere rifiutato anche se non si usa il trim sul valore finale.");
    }

    // ==========================================
    // 2. TEST COMPOSITE & GESTIONE DUPLICATI
    // ==========================================

    @Test
    public void testAggiuntaElementiEControlloDuplicati() {
        playlist.add(traccia1);
        playlist.add(traccia2);
        
        // Tentiamo di aggiungere nuovamente la traccia1 (stesso ID)
        playlist.add(traccia1);

        // La durata totale non deve considerare il duplicato.
        // Se traccia1 venisse inserita due volte, la durata sarebbe (334*2) + 170 = 838.
        // Con il Set deve essere esattamente 334 + 170 = 504.
        int durataAttesa = traccia1.getDuration() + traccia2.getDuration(); 
        assertEquals(durataAttesa, playlist.getDuration(), 
            "Il set interno deve impedire il calcolo di tracce duplicate.");
    }

    @Test
    public void testRimozioneElemento() {
        playlist.add(traccia1);
        playlist.add(traccia2);
        
        boolean rimosso = playlist.remove(traccia1);
        
        assertTrue(rimosso, "La rimozione di un elemento esistente deve restituire true.");
        assertEquals(traccia2.getDuration(), playlist.getDuration(), 
            "Dopo la rimozione, la durata deve aggiornarsi dinamicamente.");
    }

    // ==========================================
    // 3. TEST CALCOLO DURATA DINAMICA (COMPOSITE)
    // ==========================================

    @Test
    public void testCalcoloDurataPlaylistVuota() {
        Playlist playlistVuota = new Playlist("Vuota");
        assertEquals(0, playlistVuota.getDuration(), 
            "Una playlist senza componenti deve avere durata pari a 0 secondi.");
    }

    @Test
    public void testCalcoloDurataRicorsiva() {
        playlist.add(traccia1); // 334 secondi
        
        // Creiamo una sotto-playlist (rispetta la struttura ad albero del Composite)
        Playlist sottoPlaylist = new Playlist("Sotto Playlist");
        sottoPlaylist.add(traccia2); // 170 secondi
        
        // Aggiungiamo la sotto-playlist alla playlist principale
        playlist.add(sottoPlaylist);
        
        // Il calcolo deve sommare ricorsivamente: 334 + 170 = 504
        int durataTotaleAttesa = traccia1.getDuration() + traccia2.getDuration();
        
        assertEquals(durataTotaleAttesa, playlist.getDuration(), 
            "Il metodo computeDurationPlaylist deve navigare ricorsivamente l'albero del Composite.");
    }
}