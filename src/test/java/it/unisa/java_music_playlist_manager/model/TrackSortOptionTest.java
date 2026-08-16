package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrackSortOptionTest {

    private Track track1;
    private Track track2;
    private Track track3;
    private Track trackNoYear;
    private List<Track> tracks;

    @BeforeEach
    public void setUp() {
        track1 = new Track("Hotel California", "Eagles", "Hotel California", 390, "Rock", 1976, "path1.mp3");
        track2 = new Track("Billie Jean", "Michael Jackson", "Thriller", 294, "Pop", 1982, "path2.mp3");
        track3 = new Track("A Hard Day's Night", "The Beatles", "A Hard Day's Night", 154, "Rock", 1964, "path3.mp3");
        trackNoYear = new Track("Unknown Song", "Unknown Artist", "Unknown Album", 200, "Rock", null, "path4.mp3");

        tracks = List.of(track1, track2, track3, trackNoYear);
    }

    @Test
    public void testInsertionOrder() {
        List<Track> sorted = TrackSortOption.INSERTION_ORDER.sort(tracks);
        assertEquals(tracks, sorted);
        assertEquals("Data di inserimento", TrackSortOption.INSERTION_ORDER.getDisplayName());
    }

    @Test
    public void testTitleAscending() {
        List<Track> sorted = TrackSortOption.TITLE_ASC.sort(tracks);
        assertEquals(track3, sorted.get(0)); // "A Hard Day's Night"
        assertEquals(track2, sorted.get(1)); // "Billie Jean"
        assertEquals(track1, sorted.get(2)); // "Hotel California"
        assertEquals(trackNoYear, sorted.get(3)); // "Unknown Song"
    }

    @Test
    public void testTitleDescending() {
        List<Track> sorted = TrackSortOption.TITLE_DESC.sort(tracks);
        assertEquals(trackNoYear, sorted.get(0)); // "Unknown Song"
        assertEquals(track1, sorted.get(1)); // "Hotel California"
        assertEquals(track2, sorted.get(2)); // "Billie Jean"
        assertEquals(track3, sorted.get(3)); // "A Hard Day's Night"
    }

    @Test
    public void testArtistAscending() {
        List<Track> sorted = TrackSortOption.ARTIST_ASC.sort(tracks);
        assertEquals(track1, sorted.get(0)); // "Eagles"
        assertEquals(track2, sorted.get(1)); // "Michael Jackson"
        assertEquals(track3, sorted.get(2)); // "The Beatles"
        assertEquals(trackNoYear, sorted.get(3)); // "Unknown Artist"
    }

    @Test
    public void testArtistDescending() {
        List<Track> sorted = TrackSortOption.ARTIST_DESC.sort(tracks);
        assertEquals(trackNoYear, sorted.get(0)); // "Unknown Artist"
        assertEquals(track3, sorted.get(1)); // "The Beatles"
        assertEquals(track2, sorted.get(2)); // "Michael Jackson"
        assertEquals(track1, sorted.get(3)); // "Eagles"
    }

    @Test
    public void testAlbumAscending() {
        List<Track> sorted = TrackSortOption.ALBUM_ASC.sort(tracks);
        assertEquals(track3, sorted.get(0)); // "A Hard Day's Night"
        assertEquals(track1, sorted.get(1)); // "Hotel California"
        assertEquals(track2, sorted.get(2)); // "Thriller"
        assertEquals(trackNoYear, sorted.get(3)); // "Unknown Album"
    }

    @Test
    public void testYearDescending_NullsLast() {
        List<Track> sorted = TrackSortOption.YEAR_DESC.sort(tracks);
        assertEquals(track2, sorted.get(0)); // 1982
        assertEquals(track1, sorted.get(1)); // 1976
        assertEquals(track3, sorted.get(2)); // 1964
        assertEquals(trackNoYear, sorted.get(3)); // null year
    }

    @Test
    public void testYearAscending_NullsLast() {
        List<Track> sorted = TrackSortOption.YEAR_ASC.sort(tracks);
        assertEquals(track3, sorted.get(0)); // 1964
        assertEquals(track1, sorted.get(1)); // 1976
        assertEquals(track2, sorted.get(2)); // 1982
        assertEquals(trackNoYear, sorted.get(3)); // null year
    }

    @Test
    public void testDurationAscending() {
        List<Track> sorted = TrackSortOption.DURATION_ASC.sort(tracks);
        assertEquals(track3, sorted.get(0)); // 154s
        assertEquals(trackNoYear, sorted.get(1)); // 200s
        assertEquals(track2, sorted.get(2)); // 294s
        assertEquals(track1, sorted.get(3)); // 390s
    }

    @Test
    public void testDurationDescending() {
        List<Track> sorted = TrackSortOption.DURATION_DESC.sort(tracks);
        assertEquals(track1, sorted.get(0)); // 390s
        assertEquals(track2, sorted.get(1)); // 294s
        assertEquals(trackNoYear, sorted.get(2)); // 200s
        assertEquals(track3, sorted.get(3)); // 154s
    }

    @Test
    public void testSortNullList_ReturnsEmptyList() {
        List<Track> sorted = TrackSortOption.TITLE_ASC.sort(null);
        assertNotNull(sorted);
        assertTrue(sorted.isEmpty());
    }
}
