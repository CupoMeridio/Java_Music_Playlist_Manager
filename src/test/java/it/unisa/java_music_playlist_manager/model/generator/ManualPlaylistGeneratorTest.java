package it.unisa.java_music_playlist_manager.model.generator;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import it.unisa.java_music_playlist_manager.model.Playable;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ManualPlaylistGeneratorTest {

    private Library library;
    private ManualPlaylistGenerator playlistGenerator;

    public ManualPlaylistGeneratorTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        // creo il generator da testare
        playlistGenerator = new ManualPlaylistGenerator();

        // essendo Library singleton, ottengo sempre la stessa istanza
        library = Library.getInstance();
    }

    @AfterEach
    public void tearDown() {
        // PULIZIA SINGLETON
        // la Library mantiene il suo stato tra un test e l'altro,
        // quindi svuoto tracce e playlist dopo ogni test
        List<Track> currentTracks = library.getTracks();
        for (Track t : currentTracks) {
            library.removeTrack(t);
        }

        List<Playlist> currentPlaylists = library.getPlaylists();
        for (Playlist p : currentPlaylists) {
            library.removePlaylist(p);
        }
    }

    // TEST CREAZIONE PLAYLIST VUOTA

    @Test
    public void testCreatePlaylistValida() {
        Playable playable = playlistGenerator.createPlaylist("Preferiti");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertNotNull(playlist, "La playlist creata non dovrebbe essere nulla");
        assertTrue(playlist instanceof ManualPlaylist, "La playlist generata dovrebbe essere una ManualPlaylist");
        assertEquals("Preferiti", playlist.getTitle(), "La playlist dovrebbe avere il titolo corretto");
        assertEquals(0, playlist.getTrackCount(), "La playlist vuota non dovrebbe contenere brani");
    }

    @Test
    public void testCreatePlaylistConTitoloNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createPlaylist(null);
        });

        assertEquals("Il titolo della playlist non può essere vuoto.", exception.getMessage());
    }

    @Test
    public void testCreatePlaylistConTitoloVuoto() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createPlaylist("   ");
        });

        assertEquals("Il titolo della playlist non può essere vuoto.", exception.getMessage());
    }
}
