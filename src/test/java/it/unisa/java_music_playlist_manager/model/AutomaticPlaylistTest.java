package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AutomaticPlaylistTest {

    private Library library;
    private Track trackPop;
    private Track trackRock;
    private Track track2020;
    private Track track2023;
    private AutomaticGenerator generatorGenre;
    private AutomaticGenerator generatorYear;

    @BeforeEach
    public void setUp() {
        library = Library.getInstance();

        trackPop = new Track("Blinding Lights", "The Weeknd", "After Hours", 200, "Pop", 2020, "path1.mp3");
        trackRock = new Track("Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock", 1975, "path2.mp3");
        track2020 = new Track("Song 2020", "Artist", "Album", 180, "Pop", 2020, "path3.mp3");
        track2023 = new Track("Song 2023", "Artist", "Album", 190, "Pop", 2023, "path4.mp3");

        generatorGenre = new AutomaticGenerator(AutomaticGenerator.Type.GENRE);
        generatorYear = new AutomaticGenerator(AutomaticGenerator.Type.YEAR);
    }

    @AfterEach
    public void tearDown() {
        List<Track> currentTracks = library.getTracks();
        for (Track t : currentTracks) {
            library.removeTrack(t);
        }

        List<Playlist> currentPlaylists = library.getPlaylists();
        for (Playlist p : currentPlaylists) {
            library.removePlaylist(p);
        }
    }

    @Test
    public void testPlaylistAutomaticaPerGenereContieneSoloBraniDelGenereScelto() {
        library.addTrack(trackPop);
        library.addTrack(trackRock);

        Playlist playlist = generatorGenre.createPlaylist("Pop", library::getTracks);

        assertEquals(1, playlist.getTrackCount(), "La playlist automatica Pop dovrebbe contenere solo 1 brano");
        assertTrue(playlist.getTracks().contains(trackPop), "La playlist automatica Pop dovrebbe contenere il brano Pop");
        assertFalse(playlist.getTracks().contains(trackRock), "La playlist automatica Pop non dovrebbe contenere il brano Rock");
    }

    @Test
    public void testPlaylistAutomaticaPerGenereSiAggiornaDopoCambioGenere() {
        library.addTrack(trackPop);

        Playlist playlist = generatorGenre.createPlaylist("Pop", library::getTracks);

        assertTrue(playlist.getTracks().contains(trackPop), "Il brano inizialmente Pop dovrebbe essere presente nella playlist");

        trackPop.setGenre("Rock");

        assertFalse(playlist.getTracks().contains(trackPop), "Dopo il cambio genere, il brano non dovrebbe più essere nella playlist Pop");
        assertEquals(0, playlist.getTrackCount(), "La playlist Pop dovrebbe risultare vuota dopo il cambio genere");
    }

    @Test
    public void testCreazionePlaylistAutomaticaConGenereNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generatorGenre.createPlaylist(null, library::getTracks);
        });
        assertEquals("Il genere non può essere vuoto.", exception.getMessage());
    }

    @Test
    public void testPlaylistAutomaticaPerAnnoContieneSoloBraniDellAnnoScelto() {
        library.addTrack(track2020);
        library.addTrack(track2023);

        Playlist playlist = generatorYear.createPlaylist("2020", library::getTracks);

        assertEquals(1, playlist.getTrackCount(), "La playlist automatica 2020 dovrebbe contenere solo 1 brano");
        assertTrue(playlist.getTracks().contains(track2020), "La playlist automatica 2020 dovrebbe contenere il brano del 2020");
        assertFalse(playlist.getTracks().contains(track2023), "La playlist automatica 2020 non dovrebbe contenere il brano del 2023");
    }

    @Test
    public void testCreazionePlaylistAutomaticaConAnnoVuoto() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generatorYear.createPlaylist("", library::getTracks);
        });
        assertEquals("L'anno non può essere vuoto.", exception.getMessage());
    }
}