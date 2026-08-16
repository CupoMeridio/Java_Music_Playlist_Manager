package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per i Concrete Creators del pattern Factory Method (GoF Puro):
 * {@link GenrePlaylistGenerator}, {@link YearPlaylistGenerator}, {@link TagPlaylistGenerator}.
 */
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
        // la Library mantiene il suo stato tra un test e l'altro, quindi svuoto tracce e playlist
        List<Track> currentTracks = library.getTracks();
        for (Track t : currentTracks) {
            library.removeTrack(t);
        }

        List<Playlist> currentPlaylists = library.getPlaylists();
        for (Playlist p : currentPlaylists) {
            library.removePlaylist(p);
        }
    }

    // TEST CREAZIONE PLAYLIST AUTOMATICA PER GENERE (GenrePlaylistGenerator)

    @Test
    public void testCreatePlaylistByGenreValida() {
        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        PlaylistGenerator generator = new GenrePlaylistGenerator("Pop");
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
        assertThrows(IllegalArgumentException.class, () -> {
            new GenrePlaylistGenerator(null);
        });
    }

    // TEST CREAZIONE PLAYLIST AUTOMATICA PER ANNO (YearPlaylistGenerator)

    @Test
    public void testCreatePlaylistByYearValida() {
        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        PlaylistGenerator generator = new YearPlaylistGenerator(2020);
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

        PlaylistGenerator generator = new GenrePlaylistGenerator("Pop");
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

        PlaylistGenerator generator = new YearPlaylistGenerator(2020);
        Playable playable = generator.createPlaylist("Playlist 2020");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertTrue(playlist.getTracks().contains(trackPop2020),
                "Il brano del 2020 dovrebbe essere inizialmente presente");

        trackPop2020.setYear(2023);

        assertFalse(playlist.getTracks().contains(trackPop2020),
                "Dopo il cambio anno, il brano non dovrebbe più essere nella playlist 2020");
    }

    // TEST CREAZIONE PLAYLIST AUTOMATICA PER TAG (TagPlaylistGenerator)

    @Test
    public void testCreatePlaylistByTagValida() {
        trackPop2020.addTag(TagPredefined.PARTY);
        trackRock1975.addTag(TagPredefined.ROCK);

        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        PlaylistGenerator generator = new TagPlaylistGenerator(TagPredefined.PARTY);
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
        assertThrows(IllegalArgumentException.class, () -> {
            new TagPlaylistGenerator(null);
        });
    }

    @Test
    public void testPlaylistGenerataPerTagSiAggiornaDinamicamente() {
        trackPop2020.addTag(TagPredefined.PARTY);
        library.addTrack(trackPop2020);

        PlaylistGenerator generator = new TagPlaylistGenerator(TagPredefined.PARTY);
        Playable playable = generator.createPlaylist("Playlist Party Dinamica");
        assertTrue(playable instanceof Playlist, "Il risultato dovrebbe essere una Playlist");
        Playlist playlist = (Playlist) playable;

        assertTrue(playlist.getTracks().contains(trackPop2020),
                "Il brano con il tag PARTY dovrebbe essere inizialmente presente");

        trackPop2020.removeTag(TagPredefined.PARTY);

        assertFalse(playlist.getTracks().contains(trackPop2020),
                "Dopo la rimozione del tag, il brano non dovrebbe più essere nella playlist");
    }

    @Test
    public void testCreatePlaylistByTagNessunRisultato() {
        library.addTrack(trackPop2020);
        library.addTrack(trackRock1975);

        PlaylistGenerator generator = new TagPlaylistGenerator(TagPredefined.LOFI);
        Playlist playlist = (Playlist) generator.createPlaylist("Playlist Vuota");

        assertNotNull(playlist, "La playlist creata non dovrebbe essere nulla");
        assertEquals(0, playlist.getTrackCount(),
                "La playlist dovrebbe essere vuota se nessun brano corrisponde al tag");
    }

    @Test
    public void testSetLibraryDependencyInjectionOverrideDefaultFallback() {
        Library singleton = Library.getInstance();
        for (Track t : singleton.getTracks()) singleton.removeTrack(t);
        for (Playlist p : singleton.getPlaylists()) singleton.removePlaylist(p);

        Track tracciaTagParty = new Track("S", "A", "Al", 60, "Pop", 2024, "p.mp3");
        tracciaTagParty.addTag(TagPredefined.PARTY);
        Track tracciaTagRock = new Track("R", "A", "Al", 80, "Rock", 2020, "r.mp3");
        tracciaTagRock.addTag(TagPredefined.ROCK);
        singleton.addTrack(tracciaTagParty);
        singleton.addTrack(tracciaTagRock);

        PlaylistGenerator gen = new TagPlaylistGenerator(TagPredefined.PARTY);
        AutomaticPlaylistByTag partyList = (AutomaticPlaylistByTag) gen.createPlaylist("Party DI Test");

        assertEquals(1, partyList.getTrackCount(),
                "Prima DI → fallback al Singleton che ha 1 brano PARTY");

        singleton.removeTrack(tracciaTagParty);
        partyList.setLibrary(singleton);

        assertEquals(0, partyList.getTrackCount(),
                "Dopo setLibrary() con library SENZA PARTY brani → 0 risultati.");

        partyList.setLibrary(null);
        assertEquals(0, partyList.getTrackCount(),
                "setLibrary(null) torna al comportamento di default");
    }

    @Test
    public void testSetLibraryFunzionaPerTuttiTipiAutomaticPlaylist() {
        Library lib = Library.getInstance();
        for (Track t : lib.getTracks()) lib.removeTrack(t);

        Track rock2020 = new Track("R1", "A", "Al", 60, "Rock", 2020, "1.mp3");
        Track pop2018 = new Track("P1", "A", "Al", 60, "Pop", 2018, "2.mp3");
        lib.addTrack(rock2020);
        lib.addTrack(pop2018);
        rock2020.addTag(TagPredefined.ROCK);

        AutomaticPlaylistByGenre genrePl = new AutomaticPlaylistByGenre("Rock", "Rock");
        AutomaticPlaylistByYear  yearPl  = new AutomaticPlaylistByYear("2020", 2020);
        AutomaticPlaylistByTag   tagPl   = new AutomaticPlaylistByTag("Tag", TagPredefined.ROCK);

        assertEquals(1, genrePl.getTrackCount(), "default: 1 Rock");
        assertEquals(1, yearPl.getTrackCount(),  "default: 1 Year 2020");
        assertEquals(1, tagPl.getTrackCount(),   "default: 1 Tag ROCK");

        for (Track t : lib.getTracks()) lib.removeTrack(t);
        genrePl.setLibrary(lib);
        yearPl.setLibrary(lib);
        tagPl.setLibrary(lib);

        assertEquals(0, genrePl.getTrackCount(), "DI genre: library vuota");
        assertEquals(0, yearPl.getTrackCount(),  "DI year: library vuota");
        assertEquals(0, tagPl.getTrackCount(),   "DI tag: library vuota");
    }

}
