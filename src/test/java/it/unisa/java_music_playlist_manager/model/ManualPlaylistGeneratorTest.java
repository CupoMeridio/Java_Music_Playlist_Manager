package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ManualPlaylistGeneratorTest {

    private Library library;
    private ManualPlaylistGenerator playlistGenerator;
    private Track trackPop2020;
    private Track trackRock1975;

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

        trackPop2020 = new Track("Blinding Lights", "The Weeknd", "After Hours", 200, "Pop", 2020, "path1.mp3");
        trackRock1975 = new Track("Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock", 1975, "path2.mp3");
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

        List<ManualPlaylist> currentPlaylists = library.getPlaylists();
        for (ManualPlaylist p : currentPlaylists) {
            library.removePlaylist(p);
        }
    }

    // TEST CREAZIONE PLAYLIST VUOTA

    @Test
    public void testCreateEmptyPlaylistValida() {
        ManualPlaylist playlist = playlistGenerator.createEmptyPlaylist("Preferiti");

        assertNotNull(playlist, "La playlist creata non dovrebbe essere nulla");
        assertEquals("Preferiti", playlist.getTitle(), "La playlist dovrebbe avere il titolo indicato");
        assertEquals(0, playlist.getTrackCount(), "La playlist vuota non dovrebbe contenere brani");
    }

    @Test
    public void testCreateEmptyPlaylistConTitoloNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createEmptyPlaylist(null);
        });

        assertEquals("Il nome della playlist non può essere vuoto o nullo.", exception.getMessage());
    }

    @Test
    public void testCreateEmptyPlaylistConTitoloVuoto() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createEmptyPlaylist("   ");
        });

        assertEquals("Il nome della playlist non può essere vuoto o nullo.", exception.getMessage());
    }

    // TEST CREAZIONE PLAYLIST AUTOMATICA PER GENERE

    @Test
    public void testCreatePlaylistByGenreValida() {
        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        Optional<ManualPlaylist> result = playlistGenerator.createPlaylistByGenre("Pop", library.getTracks());

        assertTrue(result.isPresent(), "La playlist automatica Pop dovrebbe essere creata");
        assertTrue(result.get() instanceof AutomaticPlaylist,
                "La playlist generata per genere dovrebbe essere una AutomaticPlaylist");

        assertEquals("Playlist Pop", result.get().getTitle(),
                "La playlist dovrebbe avere il titolo corretto");

        assertEquals(1, result.get().getTrackCount(),
                "La playlist Pop dovrebbe contenere solo i brani Pop");

        assertTrue(result.get().getTracks().contains(trackPop2020),
                "La playlist Pop dovrebbe contenere il brano Pop");

        assertFalse(result.get().getTracks().contains(trackRock1975),
                "La playlist Pop non dovrebbe contenere il brano Rock");
    }

    @Test
    public void testCreatePlaylistByGenreSenzaBraniCompatibili() {
        library.addTrack(trackRock1975);

        Optional<ManualPlaylist> result = playlistGenerator.createPlaylistByGenre("Pop", library.getTracks());

        assertTrue(result.isEmpty(),
                "Se non ci sono brani del genere scelto, il risultato dovrebbe essere Optional.empty");
    }

    @Test
    public void testCreatePlaylistByGenreConGenereNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createPlaylistByGenre(null, library.getTracks());
        });

        assertEquals("Il genere non può essere vuoto.", exception.getMessage());
    }

    @Test
    public void testCreatePlaylistByGenreConGenereVuoto() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createPlaylistByGenre("   ", library.getTracks());
        });

        assertEquals("Il genere non può essere vuoto.", exception.getMessage());
    }

    @Test
    public void testCreatePlaylistByGenreConListaNulla() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createPlaylistByGenre("Pop", null);
        });

        assertEquals("La lista dei brani non può essere nulla.", exception.getMessage());
    }

    // TEST CREAZIONE PLAYLIST AUTOMATICA PER ANNO

    @Test
    public void testCreatePlaylistByYearValida() {
        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        Optional<ManualPlaylist> result = playlistGenerator.createPlaylistByYear(2020, library.getTracks());

        assertTrue(result.isPresent(), "La playlist automatica 2020 dovrebbe essere creata");
        assertTrue(result.get() instanceof AutomaticPlaylist,
                "La playlist generata per anno dovrebbe essere una AutomaticPlaylist");

        assertEquals("Playlist 2020", result.get().getTitle(),
                "La playlist dovrebbe avere il titolo corretto");

        assertEquals(1, result.get().getTrackCount(),
                "La playlist 2020 dovrebbe contenere solo i brani del 2020");

        assertTrue(result.get().getTracks().contains(trackPop2020),
                "La playlist 2020 dovrebbe contenere il brano del 2020");

        assertFalse(result.get().getTracks().contains(trackRock1975),
                "La playlist 2020 non dovrebbe contenere il brano del 1975");
    }

    @Test
    public void testCreatePlaylistByYearSenzaBraniCompatibili() {
        library.addTrack(trackRock1975);

        Optional<ManualPlaylist> result = playlistGenerator.createPlaylistByYear(2020, library.getTracks());

        assertTrue(result.isEmpty(),
                "Se non ci sono brani dell'anno scelto, il risultato dovrebbe essere Optional.empty");
    }

    @Test
    public void testCreatePlaylistByYearConAnnoNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createPlaylistByYear(null, library.getTracks());
        });

        assertEquals("L'anno non può essere vuoto.", exception.getMessage());
    }

    @Test
    public void testCreatePlaylistByYearConListaNulla() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistGenerator.createPlaylistByYear(2020, null);
        });

        assertEquals("La lista dei brani non può essere nulla.", exception.getMessage());
    }

    // TEST COMPORTAMENTO DINAMICO DELLE PLAYLIST GENERATE

    @Test
    public void testPlaylistGenerataPerGenereSiAggiornaDinamicamente() {
        library.addTrack(trackPop2020);

        Optional<ManualPlaylist> result = playlistGenerator.createPlaylistByGenre("Pop", library.getTracks());

        assertTrue(result.isPresent(), "La playlist Pop dovrebbe essere creata");

        ManualPlaylist playlist = result.get();

        assertTrue(playlist.getTracks().contains(trackPop2020),
                "Il brano Pop dovrebbe essere inizialmente presente");

        trackPop2020.setGenre("Rock");

        assertFalse(playlist.getTracks().contains(trackPop2020),
                "Dopo il cambio genere, il brano non dovrebbe più essere nella playlist Pop");
    }

    @Test
    public void testPlaylistGenerataPerAnnoSiAggiornaDinamicamente() {
        library.addTrack(trackPop2020);

        Optional<ManualPlaylist> result = playlistGenerator.createPlaylistByYear(2020, library.getTracks());

        assertTrue(result.isPresent(), "La playlist 2020 dovrebbe essere creata");

        ManualPlaylist playlist = result.get();

        assertTrue(playlist.getTracks().contains(trackPop2020),
                "Il brano del 2020 dovrebbe essere inizialmente presente");

        trackPop2020.setYear(2023);

        assertFalse(playlist.getTracks().contains(trackPop2020),
                "Dopo il cambio anno, il brano non dovrebbe più essere nella playlist 2020");
    }
}