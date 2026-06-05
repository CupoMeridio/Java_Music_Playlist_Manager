package it.unisa.java_music_playlist_manager.model;

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
        manager.changeState(new StoppedState());
        manager.setStrategy(new SequentialStrategy());
        manager.setQueue(new ArrayList<>());

        track1 = new Track("Song 1", "Artist 1", 180, "Pop", 2026);
        track2 = new Track("Song 2", "Artist 2", 200, "Rock", 2026);
        track3 = new Track("Song 3", "Artist 3", 220, "Jazz", 2026);
    }

    @Test
    public void testSkipForwardAndBackwardInsideSingleTracks() {
        manager.setQueue(List.of(track1, track2, track3));

        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
        assertEquals("Song 1", manager.getCurrentTrack().getTitle());

        manager.advanceTrack();
        assertEquals(1, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
        assertEquals("Song 2", manager.getCurrentTrack().getTitle());

        manager.regressTrack();
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
        assertEquals("Song 1", manager.getCurrentTrack().getTitle());
    }

    @Test
    public void testSkipForwardInsidePlaylistThenNextPlayable() {
        Playlist playlist = new Playlist("Playlist A");
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
        Playlist playlist = new Playlist("Playlist A");
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
        Playlist emptyPlaylist = new Playlist("Vuota");

        manager.setQueue(List.of(track1, emptyPlaylist, track2));

        manager.regressPlayable();

        assertEquals(track1, manager.getCurrentTrack());
        assertEquals(0, manager.getCurrentPlayableIndex());
        assertEquals(0, manager.getCurrentTrackIndexInPlayable());
    }

    @Test
    public void testAdvanceTrackAtEndOfPlaylistMovesToNextQueueElement() {
        Playlist playlist = new Playlist("Playlist A");
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
        Playlist playlist = new Playlist("Playlist A");
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
        Playlist emptyPlaylist = new Playlist("Vuota");

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
        Playlist emptyPlaylist = new Playlist("Vuota");

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
}
