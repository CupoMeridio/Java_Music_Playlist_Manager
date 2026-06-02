package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class LibraryCascadeRemovalTest {

    private Library library;

    @BeforeEach
    public void setUp() {
        // 1. Otteniamo l'istanza del Singleton
        library = Library.getInstance();
        
        // CORREZIONE CRITICA: Estraiamo una COPIA delle liste prima di ciclare.
        // Fare library.removeTrack(t) dentro un ciclo for-each sulla lista originale 
        // causerebbe una ConcurrentModificationException.
        List<Track> copieTracce = library.getTracks(); // getTracks() ritorna già una copia (new ArrayList)
        for (Track t : copieTracce) {
            library.removeTrack(t);
        }

        List<Playlist> copiePlaylist = library.getPlaylists(); // getPlaylists() ritorna già una copia
        for (Playlist p : copiePlaylist) {
            library.removePlaylist(p);
        }
    }

    // ====================================================================================
    //  Verifica effettivo inserimento e aumento del conteggio globale
    // ====================================================================================
    @Test
    public void testInserimentoEIncrementoConteggioGlobalePlaylist() {
        // 1. Controllo dello stato iniziale: la libreria deve essere vuota (conteggio = 0) dopo il setUp
        int conteggioIniziale = library.getPlaylists().size();
        assertEquals(0, conteggioIniziale, "La libreria dovrebbe essere inizialmente vuota per questo test.");

        // 2. Creazione della playlist con dati conformi e validi (gli spazi intermedi/laterali sono gestiti)
        Playlist playlistValida = new Playlist("  I Miei Classici Rock  ");

        // 3. Eseguiamo l'azione: il sistema inserisce la nuova playlist nella libreria generale
        library.addPlaylist(playlistValida);

        // 4. Verifica dell'aumento del conteggio globale delle playlist nel sistema
        int conteggioFinale = library.getPlaylists().size();
        assertEquals(1, conteggioFinale, "Il conteggio globale delle playlist deve essere aumentato esattamente di 1.");
        
        // 5. Verifica dell'effettivo inserimento dell'oggetto corretto
        assertTrue(library.getPlaylists().contains(playlistValida), 
            "La playlist valida deve essere effettivamente presente all'interno della Library.");
    }

    // ====================================================================================
    // Verifica rimozione a cascata (Cascade Removal)
    // ====================================================================================
    @Test
    public void testRimozioneVerificaLibrary() {
        // 2. Prepariamo i dati di test isolati e puliti
        Track tracciaDaRimuovere = new Track("Anarchy in the U.K.", "Sex Pistols", 212, "Punk", 1976);
        Playlist playlistContenitore = new Playlist("Punk Rock Playlist");

        // 3. Costruiamo le relazioni nel sistema
        library.addTrack(tracciaDaRimuovere);
        playlistContenitore.add(tracciaDaRimuovere); 
        library.addPlaylist(playlistContenitore);

        // Verifichiamo la consistenza dello stato iniziale del test
        assertEquals(212, playlistContenitore.getDuration(), 
            "La playlist deve inizialmente accumulare la durata della traccia inserita.");
        assertTrue(library.getTracks().contains(tracciaDaRimuovere), 
            "La traccia deve essere presente nell'elenco globale della Library.");
        assertTrue(playlistContenitore.contains(tracciaDaRimuovere),
            "La playlist deve contenere internamente la traccia appena aggiunta.");

        // 4. Eseguiamo l'azione: Rimozione dal catalogo globale
        boolean isRemoved = library.removeTrack(tracciaDaRimuovere);

        // 5. ASSERZIONI DI VERIFICA
        assertTrue(isRemoved, "Il metodo removeTrack deve restituire true confermando l'avvenuta rimozione.");
        
        // Verifica Proprietà A: Sparita dalla libreria globale
        assertFalse(library.getTracks().contains(tracciaDaRimuovere), 
            "La traccia deve essere stata eliminata definitivamente dalla Library.");

        // Verifica Proprietà B (Effetto a cascata): Rimossa automaticamente dalla playlist.
        // Usiamo sia il metodo contains() della tua playlist che il ricalcolo della durata a 0.
        assertFalse(playlistContenitore.contains(tracciaDaRimuovere),
            "La traccia deve essere stata eliminata a cascata dall'interno della playlist.");
        
        assertEquals(0, playlistContenitore.getDuration(), 
            "La durata della playlist deve azzerarsi in quanto non contiene più elementi.");
    }
}