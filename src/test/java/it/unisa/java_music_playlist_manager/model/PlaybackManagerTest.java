package it.unisa.java_music_playlist_manager.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PlaybackManagerTest {

    private PlaybackManager manager;
    private List<Track> testTracks;

    @BeforeEach
    public void setUp() {
        // Recuperiamo l'istanza del Singleton
        manager = PlaybackManager.getInstance();

        // Resettiamo lo stato e la strategia per partire da una situazione pulita ed evitare interferenze
        manager.changeState(new StoppedState());
        manager.setStrategy(new SequentialStrategy());
        manager.resetQueue();

        // Creiamo una lista di test con 3 brani
        testTracks = new ArrayList<>();
        testTracks.add(new Track("Song 1", "Artist 1", 180, "Pop", 2026));
        testTracks.add(new Track("Song 2", "Artist 2", 200, "Rock", 2026));
        testTracks.add(new Track("Song 3", "Artist 3", 220, "Jazz", 2026));

        // Carichiamo la coda nel manager
        manager.setQueue(testTracks);
    }

    /**
     *  I comandi di skip spostano la riproduzione in avanti e all'indietro
     * rispettando l'ordine degli elementi.
     */
    @Test
    public void testSkipForwardAndBackwardInOrder() {
        // All'inizio l'indice deve essere 0
        assertEquals(0, manager.getCurrentIndex(), "Il lettore deve partire dal primo brano (indice 0).");

        // Simuliamo il comando di skip in avanti (pressNext)
        manager.advanceQueue(); // Avanza l'indice tramite la strategia
        assertEquals(1, manager.getCurrentIndex(), "Dopo uno skip in avanti, l'indice deve essere 1.");
        assertEquals("Song 2", manager.getCurrentTrack().getTitle());

        // Simuliamo il comando di skip all'indietro (pressPrevious)
        manager.regressQueue();
        assertEquals(0, manager.getCurrentIndex(), "Dopo uno skip all'indietro, l'indice deve tornare a 0.");
        assertEquals("Song 1", manager.getCurrentTrack().getTitle());
    }

    /**
     *  Validare il comportamento in corrispondenza del PRIMO elemento della lista.
     * Premendo 'indietro' dal primo brano, l'indice deve restare bloccato a 0.
     */
    @Test
    public void testRegressQueueAtFirstElement() {
        // Siamo già sul primo brano (indice 0). Proviamo a tornare indietro.
        manager.regressQueue();

        // L'indice deve rimanere bloccato a 0 (grazie al controllo di sicurezza che hai scritto nel manager!)
        assertEquals(0, manager.getCurrentIndex(), "Premendo indietro dal primo brano, l'indice deve rimanere 0.");
        assertNotNull(manager.getCurrentTrack(), "Il brano corrente non deve essere null.");
    }

    /**
     * Validare il comportamento in corrispondenza dell'ULTIMO elemento della lista.
     * Premendo 'avanti' dall'ultimo brano, il sistema deve segnalare la fine della coda.
     */
    @Test
    public void testAdvanceQueueAtLastElement() {
        // Ci portiamo manualmente sull'ultimo brano (indice 2)
        manager.advanceQueue(); // va a 1
        manager.advanceQueue(); // va a 2
        assertEquals(2, manager.getCurrentIndex(), "Verifica di essere sull'ultimo brano valido.");

        // Forziamo l'ultimo skip in avanti oltre la fine della lista
        manager.advanceQueue();

        // La SequentialStrategy manderà l'indice a 3 (pari a testTracks.size())
        assertEquals(testTracks.size(), manager.getCurrentIndex(),
                "Oltre l'ultimo brano, l'indice deve diventare pari alla dimensione della coda.");

        // Di conseguenza, getCurrentTrack() deve restituire null per far capire al sistema di fermarsi
        assertNull(manager.getCurrentTrack(),
                "Oltre l'ultimo brano, il brano corrente deve essere null per consentire l'arresto.");
    }
}