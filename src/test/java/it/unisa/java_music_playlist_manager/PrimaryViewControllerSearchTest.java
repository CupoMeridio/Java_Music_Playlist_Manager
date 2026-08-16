package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.TagPredefined;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;

import it.unisa.java_music_playlist_manager.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrimaryViewControllerSearchTest {

    private PrimaryViewController controller;
    private Track track1;
    private Track track2;
    private Playlist playlist1;
    private List<Track> tracks;
    private List<Playlist> playlists;

    @BeforeEach
    public void setUp() {
        controller = new PrimaryViewController();

        track1 = new Track("Bohemian Rhapsody", "Queen", "A Night at the Opera", 355, "Rock", 1975, "path/to/bohemian.mp3");
        track2 = new Track("Bad Guy", "Billie Eilish", "When We All Fall Asleep", 194, "Pop", 2019, "path/to/badguy.mp3");

        track1.addTag(TagPredefined.ROCK);

        playlist1 = new ManualPlaylist("I miei preferiti anni 70");
        
        tracks = List.of(track1, track2);
        playlists = List.of(playlist1);
    }

    @Test
    public void testSearchQueryVuota_DovrebbeIncludereTutto() {
        List<Track> resultTracks = LibrarySearchService.filterTracks(tracks, "");
        List<Playlist> resultPlaylists = LibrarySearchService.filterPlaylists(playlists, "");

        assertTrue(resultTracks.contains(track1), "Con query vuota la traccia deve essere inclusa");
        assertTrue(resultPlaylists.contains(playlist1), "Con query vuota la playlist deve essere inclusa");
    }

    @Test
    public void testSearchQueryNull_DovrebbeIncludereTutto() {
        List<Track> resultTracks = LibrarySearchService.filterTracks(tracks, null);
        assertTrue(resultTracks.contains(track1), "Con query null la traccia deve essere inclusa");
    }

    @Test
    public void testRicercaPerTitolo_CaseInsensitiveESpazi() {
        List<Track> resultTracks = LibrarySearchService.filterTracks(tracks, "  bOhEmIaN  ");

        assertTrue(resultTracks.contains(track1), "Dovrebbe trovare 'Bohemian Rhapsody' ignorando spazi e maiuscole");
        assertFalse(resultTracks.contains(track2), "Non dovrebbe trovare 'Bad Guy'");
    }

    @Test
    public void testRicercaPerAutore() {
        List<Track> resultTracks = LibrarySearchService.filterTracks(tracks, "Queen");

        assertTrue(resultTracks.contains(track1), "Dovrebbe trovare la traccia dei Queen");
        assertFalse(resultTracks.contains(track2));
    }

    @Test
    public void testRicercaPerGenere() {
        List<Track> resultTracks = LibrarySearchService.filterTracks(tracks, "Pop");

        assertTrue(resultTracks.contains(track2), "Dovrebbe filtrare per genere Pop");
        assertFalse(resultTracks.contains(track1));
    }

    @Test
    public void testRicercaPerTag() {
        List<Track> resultTracks = LibrarySearchService.filterTracks(tracks, "ener");

        assertTrue(resultTracks.contains(track1), "Dovrebbe trovare la traccia tramite la sotto-stringa del tag 'Energici'");
        assertFalse(resultTracks.contains(track2), "La traccia senza tag non deve essere trovata");
    }

    @Test
    public void testRicercaPlaylistPerTitolo() {
        List<Playlist> resultPlaylists = LibrarySearchService.filterPlaylists(playlists, "preferiti");
        assertTrue(resultPlaylists.contains(playlist1), "Dovrebbe trovare la playlist per titolo");

        List<Playlist> resultPlaylists2 = LibrarySearchService.filterPlaylists(playlists, "Rock");
        assertFalse(resultPlaylists2.contains(playlist1), "Non dovrebbe trovare la playlist");
    }

    @Test
    public void testFormatDuration() throws Exception {
        Method method = PrimaryViewController.class.getDeclaredMethod("formatDuration", int.class);
        method.setAccessible(true);

        String result1 = (String) method.invoke(controller, 355);
        String result2 = (String) method.invoke(controller, 65);

        assertEquals("05:55", result1);
        assertEquals("01:05", result2);
    }
}