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
 * Verifica il corretto funzionamento del conteggio delle riproduzioni (analytics)
 * focalizzandosi sul comportamento delle Playlist e sulla prevenzione di incrementi duplicati.
 */
public class PlaylistAnalyticsTest {

    private PlaybackManager playbackManager;
    private Library library;
    private Track track1;
    private Track track2;
    private Track track3;
    private ManualPlaylist playlistA;
    private ManualPlaylist playlistB;

    @BeforeEach
    public void setUp() {
        // Recupero delle istanze dei Singleton
        playbackManager = PlaybackManager.getInstance();
        library = Library.getInstance();

        // Configurazione dell'ambiente isolato per i test (audio disabilitato)
        playbackManager.setAudioEnabled(false);
        playbackManager.changeState(new StoppedState());
        playbackManager.setStrategy(new SequentialStrategy());
        playbackManager.setQueue(new ArrayList<>());

        // Svuotamento della libreria per isolare i test
        for (Track t : library.getTracks()) {
            library.removeTrack(t);
        }
        for (Playlist p : library.getPlaylists()) {
            library.removePlaylist(p);
        }

        // Inizializzazione dati di dominio
        track1 = new Track("Track 1", "Artist 1", "Album 1", 180, "Pop", 2026, "path1.mp3");
        track2 = new Track("Track 2", "Artist 2", "Album 2", 200, "Rock", 2026, "path2.mp3");
        track3 = new Track("Track 3", "Artist 3", "Album 3", 220, "Jazz", 2026, "path3.mp3");

        playlistA = new ManualPlaylist("Playlist A");
        playlistA.addTrack(track1);
        playlistA.addTrack(track2);

        playlistB = new ManualPlaylist("Playlist B");
        playlistB.addTrack(track3);

        // Registrazione dei componenti nella libreria
        library.addTrack(track1);
        library.addTrack(track2);
        library.addTrack(track3);
        library.addPlaylist(playlistA);
        library.addPlaylist(playlistB);
    }

    @AfterEach
    public void tearDown() {
        playbackManager.resetQueue();
        playbackManager.changeState(new StoppedState());
    }

    /**
     * Verifica che l'avvio della riproduzione di una playlist (es. tramite tasto Play Playlist)
     * incrementi il contatore della playlist esattamente di 1 unità, registrando 1 sola riproduzione.
     */
    @Test
    public void testAvvioRiproduzionePlaylistIncrementaDiUno() {
        assertEquals(0, playlistA.getPlayCount(), "Il contatore iniziale della playlist deve essere 0.");

        // Avvia la riproduzione dell'intera playlist (svuotando la coda precedente)
        playbackManager.play(playlistA, false);

        assertEquals(1, playlistA.getPlayCount(), "Il contatore della playlist deve essere incrementato esattamente di 1.");
    }

    /**
     * Verifica che il contatore della playlist NON si duplichi e rimanga stabile a 1
     * quando la riproduzione procede internamente alle tracce della stessa playlist.
     */
    @Test
    public void testAvanzamentoTracceStessaPlaylistMantieneContatoreStabile() {
        playbackManager.play(playlistA, false); // Avvia la playlist (carica track1)
        assertEquals(1, playlistA.getPlayCount(), "La playlist viene contata al primo avvio.");

        // Avanza alla seconda canzone della stessa playlist (track2)
        playbackManager.pressNext();

        assertEquals(1, playlistA.getPlayCount(), "Il contatore della playlist NON deve aumentare passando da una traccia all'altra della stessa.");
    }

    /**
     * Verifica che se la riproduzione salta ad un'altra playlist all'interno della coda,
     * il sistema resetti correttamente lo stato e incrementi il contatore della nuova playlist avviata.
     */
    @Test
    public void testSaltoANuovaPlaylistIncrementaNuovoContatore() {
        // Carica in coda una sequenza composta da due playlist distinte
        playbackManager.setQueue(List.of(playlistA, playlistB));

        // Avvia la prima playlist
        playbackManager.forcePlayCurrent();
        assertEquals(1, playlistA.getPlayCount());
        assertEquals(0, playlistB.getPlayCount());

        // Salta direttamente all'elemento Playable successivo (Playlist B)
        playbackManager.pressNextPlayable();

        assertEquals(1, playlistA.getPlayCount(), "Il contatore della prima playlist deve rimanere invariato.");
        assertEquals(1, playlistB.getPlayCount(), "La seconda playlist deve registrare l'avvio incrementando il proprio contatore.");
    }

    /**
     * Verifica che l'azione di interruzione (Stop) azzeri i riferimenti di tracciamento
     * permettendo un nuovo incremento pulito (pari a 1) al successivo avvio.
     */
    @Test
    public void testStopERestartPlaylistRegistraNuovoAscolto() {
        playbackManager.play(playlistA, false);
        assertEquals(1, playlistA.getPlayCount());

        playbackManager.pressStop(); // Interrompe e resetta la coda
        playbackManager.play(playlistA, false); // Riavvia la playlist

        assertEquals(2, playlistA.getPlayCount(), "Il riavvio dopo uno stop deve registrare una nuova riproduzione distinta (totale 2).");
    }
}