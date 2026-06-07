package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class AutomaticPlaylistTest {

    private Library library;
    private Track trackPop;
    private Track trackRock;
    private Track track2020;
    private Track track2023;

    public AutomaticPlaylistTest() {
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

        trackPop = new Track("Blinding Lights", "The Weeknd", "After Hours", 200, "Pop", 2020, "path1.mp3");
        trackRock = new Track("Bohemian Rhapsody", "Queen", "A Night at the Opera", 354, "Rock", 1975, "path2.mp3");

        track2020 = new Track("Song 2020", "Artist", "Album", 180, "Pop", 2020, "path3.mp3");
        track2023 = new Track("Song 2023", "Artist", "Album", 190, "Pop", 2023, "path4.mp3");
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

    // TEST PLAYLIST AUTOMATICA PER GENERE

    @Test
    public void testPlaylistAutomaticaPerGenereContieneSoloBraniDelGenereScelto() {
        library.addTrack(trackPop);
        library.addTrack(trackRock);

        AutomaticPlaylist playlist = AutomaticPlaylist.byGenre("Pop");

        assertEquals(1, playlist.getTrackCount(),
                "La playlist automatica Pop dovrebbe contenere solo 1 brano");

        assertTrue(playlist.getTracks().contains(trackPop),
                "La playlist automatica Pop dovrebbe contenere il brano Pop");

        assertFalse(playlist.getTracks().contains(trackRock),
                "La playlist automatica Pop non dovrebbe contenere il brano Rock");
    }

    @Test
    public void testPlaylistAutomaticaPerGenereSiAggiornaDopoCambioGenere() {
        library.addTrack(trackPop);

        AutomaticPlaylist playlist = AutomaticPlaylist.byGenre("Pop");

        assertTrue(playlist.getTracks().contains(trackPop),
                "Il brano inizialmente Pop dovrebbe essere presente nella playlist");

        trackPop.setGenre("Rock");

        assertFalse(playlist.getTracks().contains(trackPop),
                "Dopo il cambio genere, il brano non dovrebbe più essere nella playlist Pop");

        assertEquals(0, playlist.getTrackCount(),
                "La playlist Pop dovrebbe risultare vuota dopo il cambio genere");
    }

    @Test
    public void testPlaylistAutomaticaPerGenereAccettaMaiuscoleMinuscole() {
        library.addTrack(trackPop);

        AutomaticPlaylist playlist = AutomaticPlaylist.byGenre("pop");

        assertTrue(playlist.getTracks().contains(trackPop),
                "Il confronto del genere dovrebbe ignorare maiuscole e minuscole");
    }

    @Test
    public void testCreazionePlaylistAutomaticaConGenereNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            AutomaticPlaylist.byGenre(null);
        });

        assertEquals("Il genere non può essere vuoto.", exception.getMessage());
    }

    @Test
    public void testCreazionePlaylistAutomaticaConGenereVuoto() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            AutomaticPlaylist.byGenre("   ");
        });

        assertEquals("Il genere non può essere vuoto.", exception.getMessage());
    }

    // TEST PLAYLIST AUTOMATICA PER ANNO

    @Test
    public void testPlaylistAutomaticaPerAnnoContieneSoloBraniDellAnnoScelto() {
        library.addTrack(track2020);
        library.addTrack(track2023);

        AutomaticPlaylist playlist = AutomaticPlaylist.byYear(2020);

        assertEquals(1, playlist.getTrackCount(),
                "La playlist automatica 2020 dovrebbe contenere solo 1 brano");

        assertTrue(playlist.getTracks().contains(track2020),
                "La playlist automatica 2020 dovrebbe contenere il brano del 2020");

        assertFalse(playlist.getTracks().contains(track2023),
                "La playlist automatica 2020 non dovrebbe contenere il brano del 2023");
    }

    @Test
    public void testPlaylistAutomaticaPerAnnoSiAggiornaDopoCambioAnno() {
        library.addTrack(track2020);

        AutomaticPlaylist playlist = AutomaticPlaylist.byYear(2020);

        assertTrue(playlist.getTracks().contains(track2020),
                "Il brano inizialmente del 2020 dovrebbe essere presente nella playlist");

        track2020.setYear(2023);

        assertFalse(playlist.getTracks().contains(track2020),
                "Dopo il cambio anno, il brano non dovrebbe più essere nella playlist 2020");

        assertEquals(0, playlist.getTrackCount(),
                "La playlist 2020 dovrebbe risultare vuota dopo il cambio anno");
    }

    @Test
    public void testCreazionePlaylistAutomaticaConAnnoNullo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            AutomaticPlaylist.byYear(null);
        });

        assertEquals("L'anno non può essere vuoto.", exception.getMessage());
    }

    // TEST BLOCCO MODIFICHE MANUALI

    @Test
    public void testAddTrackNonConsentitoSuPlaylistAutomatica() {
        AutomaticPlaylist playlist = AutomaticPlaylist.byGenre("Pop");

        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            playlist.addTrack(trackPop);
        });

        assertEquals("Non puoi aggiungere manualmente brani a una playlist automatica.",
                exception.getMessage());
    }

    @Test
    public void testAddPlayableNonConsentitoSuPlaylistAutomatica() {
        AutomaticPlaylist playlist = AutomaticPlaylist.byGenre("Pop");

        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            playlist.add(trackPop);
        });

        assertEquals("Non puoi modificare manualmente una playlist automatica.",
                exception.getMessage());
    }

    @Test
    public void testRemoveTrackSuPlaylistAutomaticaRitornaFalse() {
        AutomaticPlaylist playlist = AutomaticPlaylist.byGenre("Pop");

        boolean result = playlist.removeTrack(trackPop);

        assertFalse(result,
                "La rimozione manuale da una playlist automatica non deve essere consentita");
    }

    @Test
    public void testRemovePlayableNonLanciaEccezioni() {
        AutomaticPlaylist playlist = AutomaticPlaylist.byGenre("Pop");

        assertDoesNotThrow(() -> playlist.remove(trackPop),
                "La rimozione manuale di un Playable non dovrebbe lanciare eccezioni");
    }
}