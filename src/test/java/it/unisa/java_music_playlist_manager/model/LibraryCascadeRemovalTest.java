package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Suite di test unitari automatizzati per verificare l'integrità del modello
Library, la gestione dei contatori globali e le relazioni a cascata (Composite)
con le classi ManualPlaylist e Track.
 */
public class LibraryCascadeRemovalTest {

    private Library library;

    /**
     * Configurazione preliminare eseguita prima di OGNI singolo metodo di test.
     * Trattandosi di un Singleton, svuota esplicitamente il catalogo per garantire
     * l'isolamento e l'indipendenza dei test, prevenendo bug di persistenza in memoria.
     */
    @BeforeEach
    public void setUp() {
        // 1. Otteniamo l'istanza centralizzata del Singleton Library
        library = Library.getInstance();
        
        // CORREZIONE ARCHITETTURALE: Estraiamo una copia difensiva prima di iterare.
        // Invocare removeTrack() o removePlaylist() direttamente dentro un ciclo for-each 
        // sulla collezione interna genererebbe una ConcurrentModificationException.
        List<Track> copieTracce = library.getTracks(); 
        for (Track t : copieTracce) {
            library.removeTrack(t);
        }

        List<ManualPlaylist> copiePlaylist = library.getPlaylists(); 
        for (ManualPlaylist p : copiePlaylist) {
            library.removePlaylist(p);
        }
    }

    // ====================================================================================
    // TEST 1: Inserimento ManualPlaylist e Incremento Contatore Globale (Dati Validi)
    // ====================================================================================
    @Test
    public void testInserimentoEIncrementoConteggioGlobalePlaylist() {
        // Controllo dello stato iniziale: la libreria deve partire da zero elementi
        int conteggioIniziale = library.getPlaylists().size();
        assertEquals(0, conteggioIniziale, "La libreria deve essere inizialmente vuota per questo test.");

        // Creazione della playlist con dati conformi e validi (gli spazi laterali sono accettati dal dominio)
        ManualPlaylist playlistValida = new ManualPlaylist("  I Miei Classici Rock  ");

        // Azione: Registrazione della playlist all'interno del catalogo di sistema
        library.addPlaylist(playlistValida);

        // Verifica: Il contatore globale deve essere aumentato esattamente di 1
        int conteggioFinale = library.getPlaylists().size();
        assertEquals(1, conteggioFinale, "Il conteggio globale delle playlist deve essere aumentato esattamente di 1.");
        
        // Verifica di consistenza: L'oggetto inserito deve essere effettivamente presente
        assertTrue(library.getPlaylists().contains(playlistValida), 
            "La playlist valida deve essere effettivamente presente all'interno della Library.");
    }

    // ====================================================================================
    // TEST 2: Diminuzione del Numero di ManualPlaylist Totali alla Rimozione
    // ====================================================================================
    @Test
    public void testRimozionePlaylistDiminuisceConteggioGlobale() {
        // Creazione e inserimento di due playlist distinte nel sistema
        ManualPlaylist playlist1 = new ManualPlaylist("Playlist Rock");
        ManualPlaylist playlist2 = new ManualPlaylist("Playlist Pop");
        library.addPlaylist(playlist1);
        library.addPlaylist(playlist2);
        
        // Verifica dello stato pre-rimozione
        assertEquals(2, library.getPlaylists().size(), "La libreria deve memorizzare inizialmente 2 playlist.");

        // Azione: Cancellazione di una singola playlist dal catalogo globale
        boolean isRemoved = library.removePlaylist(playlist1);

        // Asserzioni di verifica: l'operazione deve avere successo e aggiornare i dati globali
        assertTrue(isRemoved, "Il metodo removePlaylist deve restituire true confermando l'azione.");
        assertEquals(1, library.getPlaylists().size(), "Il numero totale di playlist deve essere diminuito esattamente a 1.");
        assertFalse(library.getPlaylists().contains(playlist1), "La playlist rimossa non deve più figurare nel sistema.");
        assertTrue(library.getPlaylists().contains(playlist2), "La playlist non coinvolta deve continuare a esistere regolarmente.");
    }

    // ====================================================================================
    // TEST 3: Effetto a Cascata - La Rimozione del Brano Ripulisce le ManualPlaylist (Cascade Removal)
    // ====================================================================================
    @Test
    public void testRimozioneVerificaLibrary() {
        // Preparazione dei dati di test in uno scenario isolato e controllato
        Track tracciaDaRimuovere = new Track("Anarchy in the U.K.", "Sex Pistols", "Never Mind the Bollocks, Here's the Sex Pistols", 212, "Punk", 1976, "path1.mp3");
        ManualPlaylist playlistContenitore = new ManualPlaylist("Punk Rock Playlist");

        // Costruzione dei legami: la traccia fa parte sia del catalogo che della specifica playlist
        library.addTrack(tracciaDaRimuovere);
        playlistContenitore.addTrack(tracciaDaRimuovere); 
        library.addPlaylist(playlistContenitore);

        // Validazione dello stato di partenza (La playlist ha una traccia e accumula 212 secondi di durata)
        assertEquals(212, playlistContenitore.getDuration(), 
            "La playlist deve inizialmente accumulare la durata della traccia inserita.");
        assertTrue(library.getTracks().contains(tracciaDaRimuovere), 
            "La traccia deve essere presente nell'elenco globale della Library.");
        assertTrue(playlistContenitore.contains(tracciaDaRimuovere),
            "La playlist deve contenere internamente la traccia appena aggiunta.");

        // Azione: Eliminazione della traccia dal catalogo principale (es. per motivi di copyright)
        boolean isRemoved = library.removeTrack(tracciaDaRimuovere);

        // Asserzioni di verifica sul corretto comportamento del sistema
        assertTrue(isRemoved, "Il metodo removeTrack deve restituire true confermando l'avvenuta rimozione.");
        
        // Proprietà A: Il brano scompare dal catalogo della Library
        assertFalse(library.getTracks().contains(tracciaDaRimuovere), 
            "La traccia deve essere stata eliminata definitivamente dalla Library.");

        // Proprietà B (Effetto a cascata): La Library ha ripulito autonomamente la ManualPlaylist.
        // Il brano non deve più risultare associato alla playlist e la durata deve ricalcolarsi a zero.
        assertFalse(playlistContenitore.contains(tracciaDaRimuovere),
            "La traccia deve essere stata eliminata a cascata dall'interno della playlist.");
        assertEquals(0, playlistContenitore.getDuration(), 
            "La durata della playlist deve azzerarsi in quanto non contiene più elementi.");
    }

    // ====================================================================================
    // TEST 4: Isolamento dei Cicli di Vita - La Cancellazione della ManualPlaylist Preserva i Brani
    // ====================================================================================
    @Test
    public void testEliminazionePlaylistPreservaBraniInLibreria() {
        // Creazione di due tracce inserite nel catalogo principale
        Track track1 = new Track("Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock", 1975, "path1.mp3");
        Track track2 = new Track("Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", 482, "Rock", 1971, "path2.mp3");
        library.addTrack(track1);
        library.addTrack(track2);

        // Creazione di una playlist che raggruppa queste due tracce
        ManualPlaylist playlist = new ManualPlaylist("Rock Anthems");
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        library.addPlaylist(playlist);

        // Controllo di consistenza dello stato iniziale
        assertEquals(2, library.getTracks().size(), "La libreria deve contenere inizialmente 2 tracce.");
        assertEquals(1, library.getPlaylists().size(), "La libreria deve contenere inizialmente 1 playlist.");

        // Azione: Smantellamento/Cancellazione della playlist dal sistema globale
        library.removePlaylist(playlist);

        // Asserzioni di verifica: La playlist sparisce, ma il ciclo di vita dei brani è indipendente
        assertEquals(0, library.getPlaylists().size(), "La playlist deve risultare rimossa con successo.");
        
        // I brani DEVONO continuare a esistere indisturbati nel catalogo principale
        assertEquals(2, library.getTracks().size(), 
            "I brani non devono essere eliminati dalla Library quando si cancella un semplice contenitore (playlist).");
        assertTrue(library.getTracks().contains(track1), 
            "Il primo brano deve essere ancora disponibile nel catalogo globale.");
        assertTrue(library.getTracks().contains(track2), 
            "Il secondo brano deve essere ancora disponibile nel catalogo globale.");
    }
}