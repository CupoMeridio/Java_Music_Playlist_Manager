package it.unisa.java_music_playlist_manager.model;

import it.unisa.java_music_playlist_manager.model.state.PausedState;
import it.unisa.java_music_playlist_manager.model.state.PlayingState;
import it.unisa.java_music_playlist_manager.model.state.StoppedState;
import it.unisa.java_music_playlist_manager.model.strategy.SequentialStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlaybackManagerTest {

    private PlaybackManager manager;
    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    public void setUp() {
        manager = PlaybackManager.getInstance();
        manager.setAudioEnabled(false); // Disabilita audio reale per i test automatici
        manager.changeState(new StoppedState());
        manager.setStrategy(new SequentialStrategy());
        manager.setQueue(new ArrayList<>());

        // Utilizzo di un file reale dalla cartella resources per rendere i test più veritieri
        String baseDir = System.getProperty("user.dir");
        String realPath = new java.io.File(baseDir, "src/test/resources/test_song.mp3").getAbsolutePath();

        track1 = new Track("Vibing Over Venus", "NCS", "Release", 180, "Electronic", 2024, realPath);
        track2 = new Track("Song 2", "Artist 2", "Album 2", 200, "Rock", 2026, realPath);
        track3 = new Track("Song 3", "Artist 3", "Album 3", 220, "Jazz", 2026, realPath);
    }

    @Test
    public void testSkipForwardAndBackwardInsideSingleTracks() {
        manager.setQueue(List.of(track1, track2, track3));

        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
        assertEquals("Vibing Over Venus", manager.getCurrentTrack().getTitle());

        manager.advanceTrack();
        assertEquals(1, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
        assertEquals("Song 2", manager.getCurrentTrack().getTitle());

        manager.regressTrack();
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
        assertEquals("Vibing Over Venus", manager.getCurrentTrack().getTitle());
    }

    @Test
    public void testSkipForwardInsidePlaylistThenNextPlayable() {
        ManualPlaylist playlist = new ManualPlaylist("Playlist A");
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        manager.setQueue(List.of(playlist, track3));

        assertEquals(track1, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());

        manager.advanceTrack();
        assertEquals(track2, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(1, manager.getCurrentTrackIndexInPlayable());

        manager.advancePlayable();
        assertEquals(track3, manager.getCurrentTrack());
        assertEquals(1, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testPreviousPlayableReturnsToFirstTrackOfPreviousQueueElement() {
        ManualPlaylist playlist = new ManualPlaylist("Playlist A");
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        manager.setQueue(List.of(playlist, track3));
        manager.advancePlayable();

        manager.regressPlayable();

        assertEquals(track1, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testPreviousPlayableSkipsEmptyPlaylistBackward() {
        ManualPlaylist emptyPlaylist = new ManualPlaylist("Vuota");

        manager.setQueue(List.of(track1, emptyPlaylist, track2));

        manager.regressPlayable();

        assertEquals(track1, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testAdvanceTrackAtEndOfPlaylistMovesToNextQueueElement() {
        ManualPlaylist playlist = new ManualPlaylist("Playlist A");
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        manager.setQueue(List.of(playlist, track3));

        manager.advanceTrack();
        manager.advanceTrack();

        assertEquals(track3, manager.getCurrentTrack());
        assertEquals(1, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testAdvanceTrackAtEndOfQueueReturnsNullTrack() {
        manager.setQueue(List.of(track1));

        manager.advanceTrack();

        assertEquals(1, manager.getCurrentPlayableIndex());
        assertNull(manager.getCurrentTrack());
    }

    @Test
    public void testRegressTrackAtFirstElementStaysAtStart() {
        manager.setQueue(List.of(track1, track2));

        manager.regressTrack();

        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
        assertEquals(track1, manager.getCurrentTrack());
    }

    @Test
    public void testRegressTrackFromSingleTrackReturnsToLastTrackOfPreviousPlaylist() {
        ManualPlaylist playlist = new ManualPlaylist("Playlist A");
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        manager.setQueue(List.of(playlist, track3));
        manager.advancePlayable();

        manager.regressTrack();

        assertEquals(track2, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(1, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testRegressTrackAfterEndOfQueueReturnsToLastTrack() {
        manager.setQueue(List.of(track1, track2));
        manager.advanceTrack();
        manager.advanceTrack();

        manager.regressTrack();

        assertEquals(track2, manager.getCurrentTrack());
        assertEquals(1, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testRegressTrackSkipsEmptyPlaylistBackward() {
        ManualPlaylist emptyPlaylist = new ManualPlaylist("Vuota");

        manager.setQueue(List.of(track1, emptyPlaylist, track2));
        manager.advanceTrack();

        assertEquals(track2, manager.getCurrentTrack());

        manager.regressTrack();

        assertEquals(track1, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testEmptyPlaylistIsSkippedWhenQueueStarts() {
        ManualPlaylist emptyPlaylist = new ManualPlaylist("Vuota");

        manager.setQueue(List.of(emptyPlaylist, track1));

        assertEquals(track1, manager.getCurrentTrack());
        assertEquals(1, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testPressPlayDaStoppedPassaAPlaying() {
        manager.setQueue(List.of(track1));
        manager.changeState(new StoppedState());

        manager.pressPlay();

        assertTrue(manager.getCurrentState() instanceof PlayingState);
    }

    @Test
    public void testPressPlayDaPlayingPassaAPaused() {
        manager.setQueue(List.of(track1));

        manager.pressPlay();
        manager.pressPlay();

        assertTrue(manager.getCurrentState() instanceof PausedState);
    }

    @Test
    public void testPressPlayDaPausedPassaAPlaying() {
        manager.setQueue(List.of(track1));

        manager.pressPlay();
        manager.pressPlay();
        manager.pressPlay();

        assertTrue(manager.getCurrentState() instanceof PlayingState);
    }

    @Test
    public void testPressStopDaPlayingPassaAStoppedAndResettaIndici() {
        manager.setQueue(List.of(track1, track2));

        manager.pressPlay();
        manager.pressNext();
        manager.pressStop();

        assertTrue(manager.getCurrentState() instanceof StoppedState);
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testComandiRiproduzioneConCodaVuotaNonCausanoCrash() {
        manager.setQueue(new ArrayList<>());
        manager.changeState(new StoppedState());

        assertDoesNotThrow(() -> manager.pressPlay());
        assertDoesNotThrow(() -> manager.pressStop());
        assertDoesNotThrow(() -> manager.pressNext());
        assertDoesNotThrow(() -> manager.pressNextPlayable());
        assertDoesNotThrow(() -> manager.pressPrevious());
        assertDoesNotThrow(() -> manager.pressPreviousPlayable());

        assertTrue(manager.getCurrentState() instanceof StoppedState);
        assertNull(manager.getCurrentTrack());
    }

    @Test
    public void testRemoveFromQueue() {
        manager.setQueue(List.of(track1, track2, track3));
        
        // Rimuovi elemento non corrente dopo quello corrente
        manager.removeFromQueue(2);
        assertEquals(2, manager.getCurrentQueue().size());
        assertEquals(track1, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());

        // Rimuovi elemento corrente
        manager.removeFromQueue(0);
        assertEquals(1, manager.getCurrentQueue().size());
        assertEquals(track2, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());

        // Rimuovi l'unico elemento rimasto
        manager.removeFromQueue(0);
        assertEquals(0, manager.getCurrentQueue().size());
        assertNull(manager.getCurrentTrack());
        assertTrue(manager.getCurrentState() instanceof StoppedState);
    }

    @Test
    public void testComandiSuccessiviOltreFineCodaNonCausanoCrash() {
        manager.setQueue(List.of(track1));
        
        // Iniziamo la riproduzione
        manager.pressPlay(); 
        
        // Saltiamo alla fine della coda usando advanceTrack direttamente (che è quello che causa il crash)
        manager.advanceTrack(); 
        
        // Lo stato deve essere STOPPED (perché la coda è finita) e l'indice 1
        assertEquals(1, manager.getCurrentPlayableIndex());
        assertNull(manager.getCurrentTrack());

        // Proviamo a chiamare advanceTrack ancora (simulando il click su Next in Stopped alla fine)
        assertDoesNotThrow(() -> manager.advanceTrack(), 
            "Chiamare advanceTrack alla fine della coda non deve causare IndexOutOfBoundsException");
        
        assertDoesNotThrow(() -> manager.advancePlayable(), 
            "Chiamare advancePlayable alla fine della coda non deve causare IndexOutOfBoundsException");
        
        // Verifichiamo che gli indici siano rimasti coerenti
        assertEquals(1, manager.getCurrentPlayableIndex());
    }

    @Test
    public void testSelectAndLoadTrackFromLibraryEnqueuesFullLibrary() {
        // Simula il comportamento standard: play dalla libreria carica l'intera lista dei brani visibili
        List<Track> libraryTracks = List.of(track1, track2, track3);
        manager.selectAndLoadTrack(track2, libraryTracks);

        assertEquals(3, manager.getCurrentQueue().size(),
                "La coda deve contenere tutti e 3 i brani della libreria");
        assertEquals(track2, manager.getCurrentTrack(),
                "La traccia corrente deve essere quella selezionata (track2)");
        assertEquals(1, manager.getCurrentPlayableIndex(),
                "L'indice del playable corrente nella coda deve essere 1 (track2)");

        // Avanti alla traccia successiva (track3)
        manager.advanceTrack();
        assertEquals(track3, manager.getCurrentTrack(),
                "Avanzando deve riprodurre la traccia successiva della libreria (track3)");
    }

    @Test
    public void testSelectAndLoadTrackFromPlaylistEnqueuesFullPlaylist() {
        // Simula il comportamento dalla playlist: il contesto è la playlist intera
        ManualPlaylist playlist = new ManualPlaylist("Playlist Test");
        playlist.addTrack(track1);
        playlist.addTrack(track2);
        playlist.addTrack(track3);

        manager.selectAndLoadTrack(track2, List.of(playlist));

        assertEquals(1, manager.getCurrentQueue().size(),
                "La coda deve contenere 1 Playable (la playlist)");
        assertEquals(track2, manager.getCurrentTrack(),
                "La traccia corrente deve essere quella selezionata dentro la playlist");
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(1, manager.getCurrentTrackIndexInPlayable(),
                "L'indice della traccia dentro la playlist deve essere 1 (track2 è la seconda)");
    }


}
