package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class AutomaticPlaylistGeneratorTest {

    private Library library;
    private Track trackPop2020;
    private Track trackRock1975;

    public AutomaticPlaylistGeneratorTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
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

        List<Playlist> currentPlaylists = library.getPlaylists();
        for (Playlist p : currentPlaylists) {
            library.removePlaylist(p);
        }
    }

    // TEST CREAZIONE PLAYLIST AUTOMATICA PER GENERE

    @Test
    public void testCreatePlaylistByGenreValida() {
        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.GENRE,
                "Pop"
        );
        Playable playable = generator.createPlaylist("Playlist Pop");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertNotNull(playlist, "La playlist creata non dovrebbe essere nulla");
        assertTrue(playlist instanceof AutomaticPlaylistByGenre,
                "La playlist generata per genere dovrebbe essere una AutomaticPlaylistByGenre");
        assertEquals("Playlist Pop", playlist.getTitle(),
                "La playlist dovrebbe avere il titolo corretto");
        assertEquals(1, playlist.getTrackCount(),
                "La playlist Pop dovrebbe contenere solo i brani Pop");
        assertTrue(playlist.getTracks().contains(trackPop2020),
                "La playlist Pop dovrebbe contenere il brano Pop");
        assertFalse(playlist.getTracks().contains(trackRock1975),
                "La playlist Pop non dovrebbe contenere il brano Rock");
    }

    @Test
    public void testCreatePlaylistByGenreConGenereNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new AutomaticPlaylistGenerator(AutomaticPlaylistGenerator.Criteria.GENRE, null);
        });

        assertEquals("Criterio e valore di filtraggio non possono essere nulli.", exception.getMessage());
    }

    // TEST CREAZIONE PLAYLIST AUTOMATICA PER ANNO

    @Test
    public void testCreatePlaylistByYearValida() {
        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.YEAR,
                2020
        );
        Playable playable = generator.createPlaylist("Playlist 2020");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertNotNull(playlist, "La playlist creata non dovrebbe essere nulla");
        assertTrue(playlist instanceof AutomaticPlaylistByYear,
                "La playlist generata per anno dovrebbe essere una AutomaticPlaylistByYear");
        assertEquals("Playlist 2020", playlist.getTitle(),
                "La playlist dovrebbe avere il titolo corretto");
        assertEquals(1, playlist.getTrackCount(),
                "La playlist 2020 dovrebbe contenere solo i brani del 2020");
        assertTrue(playlist.getTracks().contains(trackPop2020),
                "La playlist 2020 dovrebbe contenere il brano del 2020");
        assertFalse(playlist.getTracks().contains(trackRock1975),
                "La playlist 2020 non dovrebbe contenere il brano del 1975");
    }

    @Test
    public void testPlaylistGenerataPerGenereSiAggiornaDinamicamente() {
        library.addTrack(trackPop2020);

        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.GENRE,
                "Pop"
        );
        Playable playable = generator.createPlaylist("Playlist Pop");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertTrue(playlist.getTracks().contains(trackPop2020),
                "Il brano Pop dovrebbe essere inizialmente presente");

        trackPop2020.setGenre("Rock");

        assertFalse(playlist.getTracks().contains(trackPop2020),
                "Dopo il cambio genere, il brano non dovrebbe più essere nella playlist Pop");
    }

    @Test
    public void testPlaylistGenerataPerAnnoSiAggiornaDinamicamente() {
        library.addTrack(trackPop2020);

        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.YEAR,
                2020
        );
        Playable playable = generator.createPlaylist("Playlist 2020");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertTrue(playlist.getTracks().contains(trackPop2020),
                "Il brano del 2020 dovrebbe essere inizialmente presente");

        trackPop2020.setYear(2023);

        assertFalse(playlist.getTracks().contains(trackPop2020),
                "Dopo il cambio anno, il brano non dovrebbe più essere nella playlist 2020");
    }

    // TEST CREAZIONE PLAYLIST AUTOMATICA PER TAG

    @Test
    public void testCreatePlaylistByTagValida() {
        // Configurazione dei tag sui brani
        trackPop2020.addTag(TagPredefined.PARTY);
        trackRock1975.addTag(TagPredefined.ROCK);

        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.TAG,
                TagPredefined.PARTY
        );
        Playable playable = generator.createPlaylist("Playlist Party");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertNotNull(playlist, "La playlist creata non dovrebbe essere nulla");
        assertTrue(playlist instanceof AutomaticPlaylistByTag,
                "La playlist generata per tag dovrebbe essere una AutomaticPlaylistByTag");
        assertEquals("Playlist Party", playlist.getTitle(),
                "La playlist dovrebbe avere il titolo corretto");
        assertEquals(1, playlist.getTrackCount(),
                "La playlist Party dovrebbe contenere solo i brani con il tag PARTY");
        assertTrue(playlist.getTracks().contains(trackPop2020),
                "La playlist Party dovrebbe contenere il brano Pop");
        assertFalse(playlist.getTracks().contains(trackRock1975),
                "La playlist Party non dovrebbe contenere il brano Rock");
    }

    @Test
    public void testCreatePlaylistByTagConTagNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new AutomaticPlaylistGenerator(AutomaticPlaylistGenerator.Criteria.TAG, null);
        });

        assertEquals("Criterio e valore di filtraggio non possono essere nulli.", exception.getMessage());
    }

    @Test
    public void testPlaylistGenerataPerTagSiAggiornaDinamicamente() {
        trackPop2020.addTag(TagPredefined.PARTY);
        library.addTrack(trackPop2020);

        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.TAG,
                TagPredefined.PARTY
        );
        Playable playable = generator.createPlaylist("Playlist Party Dinamica");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertTrue(playlist.getTracks().contains(trackPop2020),
                "Il brano con il tag PARTY dovrebbe essere inizialmente presente");

        // Rimozione del tag per verificare l'aggiornamento dinamico
        trackPop2020.removeTag(TagPredefined.PARTY);

        assertFalse(playlist.getTracks().contains(trackPop2020),
                "Dopo la rimozione del tag, il brano non dovrebbe più essere nella playlist");
    }

    @Test
    public void testCreatePlaylistByTagNessunRisultato() {
        // Nessun brano possiede il tag LOFI
        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.TAG,
                TagPredefined.LOFI
        );
        Playlist playlist = (Playlist) generator.createPlaylist("Playlist Vuota");

        assertNotNull(playlist, "La playlist creata non dovrebbe essere nulla");
        assertEquals(0, playlist.getTrackCount(),
                "La playlist dovrebbe essere vuota se nessun brano corrisponde al tag");
    }

}
