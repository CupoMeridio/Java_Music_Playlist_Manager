package it.unisa.java_music_playlist_manager.model;

import it.unisa.java_music_playlist_manager.model.state.StoppedState;
import it.unisa.java_music_playlist_manager.model.strategy.SequentialStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test automatizzati per l'adempimento del Task 4.2.3.
 * Verifica il corretto funzionamento del conteggio delle riproduzioni
 * (analytics)
 * su singoli brani e playlist, e la corretta propagazione degli eventi verso
 * gli osservatori.
 */
public class TrackAnalyticsTest {

    private PlaybackManager playbackManager;
    private Library library;
    private Track track1;
    private Track track2;
    private ManualPlaylist manualPlaylist;

    @BeforeEach
    public void setUp() {
        // Recupero delle istanze dei Singleton
        playbackManager = PlaybackManager.getInstance();
        library = Library.getInstance();

        // Configurazione dell'ambiente isolato per i test
        playbackManager.setAudioEnabled(false); // Impedisce l'avvio del MediaPlayer reale
        playbackManager.changeState(new StoppedState());
        playbackManager.setStrategy(new SequentialStrategy());
        playbackManager.setQueue(new ArrayList<>());

        // Pulizia preliminare del catalogo della Library
        for (Track t : library.getTracks()) {
            library.removeTrack(t);
        }
        for (Playlist p : library.getPlaylists()) {
            library.removePlaylist(p);
        }

        // Inizializzazione dei dati fittizi conformi al dominio
        track1 = new Track("Epitaph", "King Crimson", "In the Court of the Crimson King", 527, "Progressive Rock", 1969,
                "path1.mp3");
        track2 = new Track("Starless", "King Crimson", "Red", 742, "Progressive Rock", 1974, "path2.mp3");

        manualPlaylist = new ManualPlaylist("Test Analytics Playlist");
        manualPlaylist.addTrack(track1);

        // Registrazione nel catalogo globale per consentire le letture statistiche
        library.addTrack(track1);
        library.addTrack(track2);
        library.addPlaylist(manualPlaylist);
    }

    @AfterEach
    public void tearDown() {
        playbackManager.resetQueue();
        playbackManager.changeState(new StoppedState());
    }

    /**
     * Verifica che il contatore delle riproduzioni di un singolo brano aumenti
     * esattamente di 1 non appena viene avviata la sua esecuzione.
     */
    @Test
    public void testIncrementoPlayCountSingoloBrano() {
        assertEquals(0, track1.getPlayCount(), "Il contatore iniziale del brano deve essere 0.");

        // Caricamento del brano nel contesto ed esecuzione immediata
        playbackManager.selectAndLoadTrack(track1, List.of(track1));
        playbackManager.forcePlayCurrent();

        assertEquals(1, track1.getPlayCount(),
                "Il contatore del brano deve essere incrementato a 1 dopo la riproduzione.");
    }

    /**
     * Verifica che la riproduzione di un brano all'interno di una playlist
     * incrementi
     * simultaneamente sia il contatore del brano che quello della playlist
     * contenitrice.
     */
    @Test
    public void testIncrementoPlayCountInContestoPlaylist() {
        assertEquals(0, track1.getPlayCount(), "Il contatore del brano deve essere inizialmente 0.");
        assertEquals(0, manualPlaylist.getPlayCount(), "Il contatore della playlist deve essere inizialmente 0.");

        // Caricamento del contesto della playlist ed avvio del brano contenuto
        playbackManager.selectAndLoadTrack(track1, List.of(manualPlaylist));
        playbackManager.forcePlayCurrent();

        assertEquals(1, track1.getPlayCount(), "Il contatore del brano deve essere incrementato.");
        assertEquals(1, manualPlaylist.getPlayCount(), "Il contatore della playlist deve essere incrementato.");
    }

    /**
     * Garantisce che la stessa playlist non subisca incrementi multipli del proprio
     * contatore
     * se l'utente va avanti con le tracce rimanendo sempre all'interno dello stesso
     * blocco riproducibile.
     */
    @Test
    public void testContatorePlaylistNonIncrementatoConsecutivamente() {
        manualPlaylist.addTrack(track2); // La playlist ora contiene track1 e track2

        playbackManager.selectAndLoadTrack(track1, List.of(manualPlaylist));
        playbackManager.forcePlayCurrent(); // Avvia track1

        assertEquals(1, manualPlaylist.getPlayCount(), "La playlist deve essere contata al primo avvio.");

        playbackManager.advanceTrack(); // Sposta gli indici su track2
        playbackManager.triggerRealPlayback(); // <--- AGGIUNGERE QUESTA RIGA PER AVVIARE IL SECONDO BRANO

        assertEquals(1, track2.getPlayCount(), "Il secondo brano deve registrare l'ascolto.");
        assertEquals(1, manualPlaylist.getPlayCount(), "La playlist NON deve incrementare nuovamente il contatore.");
    }

    /**
     * Verifica l'integrità del Pattern Observer: l'incremento delle metriche di
     * riproduzione
     * deve propagare immediatamente una notifica verso gli osservatori registrati.
     */
    @Test
    public void testPropagazioneNotificaObserverSuIncrementoAnalytics() {
        // Definizione di un mock di test per verificare la ricezione dell'evento
        class AnalyticsObserverMock implements Observer {
            boolean notificationReceived = false;

            @Override
            public void update() {
                notificationReceived = true;
            }
        }

        AnalyticsObserverMock mockObserver = new AnalyticsObserverMock();
        playbackManager.attach(mockObserver);

        playbackManager.selectAndLoadTrack(track1, List.of(track1));
        playbackManager.forcePlayCurrent();

        assertTrue(mockObserver.notificationReceived,
                "Il PlaybackManager deve notificare i propri osservatori (es. HomeController) all'avvio e incremento del brano.");
    }
}