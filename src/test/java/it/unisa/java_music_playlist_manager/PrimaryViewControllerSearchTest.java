package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class PrimaryViewControllerSearchTest {

    private PrimaryViewController controller;
    private Track track1;
    private Track track2;
    private Playlist playlist1;

    // Importa la classe concreta in cima al file se si trova in un altro pacchetto:
// import it.unisa.java_music_playlist_manager.model.TagPredefined;

    @BeforeEach
    public void setUp() {
        controller = new PrimaryViewController();

        // 1. Creiamo i brani usando l'esatto ordine dei parametri del tuo costruttore:
        // parametri: (Titolo, Autore, Album, Durata [int], Genere, Anno [Integer], PercorsoFile)
        track1 = new Track("Bohemian Rhapsody", "Queen", "A Night at the Opera", 355, "Rock", 1975, "path/to/bohemian.mp3");
        track2 = new Track("Bad Guy", "Billie Eilish", "When We All Fall Asleep", 194, "Pop", 2019, "path/to/badguy.mp3");

        // 2. CORRETTO: Usiamo il tuo metodo addTag() esistente invece del vecchio setTags
        track1.addTag(TagPredefined.ROCK);

        // 3. Istanziamo la playlist concreta per il test
        playlist1 = new ManualPlaylist("I miei preferiti anni 70");
    }

    /**
     * Helper per impostare il campo privato 'searchQuery' tramite Reflection
     */
    private void setSearchQuery(String query) throws Exception {
        Field field = PrimaryViewController.class.getDeclaredField("searchQuery");
        field.setAccessible(true);
        field.set(controller, query);
    }

    /**
     * Helper per invandare il metodo privato 'matchesTrackSearch' tramite Reflection
     */
    private boolean invokeMatchesTrackSearch(Track track) throws Exception {
        Method method = PrimaryViewController.class.getDeclaredMethod("matchesTrackSearch", Track.class);
        method.setAccessible(true);
        return (boolean) method.invoke(controller, track);
    }

    /**
     * Helper per invocare il metodo privato 'matchesPlaylistSearch' tramite Reflection
     */
    private boolean invokeMatchesPlaylistSearch(Playlist playlist) throws Exception {
        Method method = PrimaryViewController.class.getDeclaredMethod("matchesPlaylistSearch", Playlist.class);
        method.setAccessible(true);
        return (boolean) method.invoke(controller, playlist);
    }

    @Test
    public void testSearchQueryVuota_DovrebbeIncludereTutto() throws Exception {
        setSearchQuery("");

        assertTrue(invokeMatchesTrackSearch(track1), "Con query vuota la traccia deve essere inclusa");
        assertTrue(invokeMatchesPlaylistSearch(playlist1), "Con query vuota la playlist deve essere inclusa");
    }

    @Test
    public void testSearchQueryNull_DovrebbeIncludereTutto() throws Exception {
        setSearchQuery(null);

        assertTrue(invokeMatchesTrackSearch(track1), "Con query null la traccia deve essere inclusa");
    }

    @Test
    public void testRicercaPerTitolo_CaseInsensitiveESpazi() throws Exception {
        setSearchQuery("  bOhEmIaN  ");

        assertTrue(invokeMatchesTrackSearch(track1), "Dovrebbe trovare 'Bohemian Rhapsody' ignorando spazi e maiuscole");
        assertFalse(invokeMatchesTrackSearch(track2), "Non dovrebbe trovare 'Bad Guy'");
    }

    @Test
    public void testRicercaPerAutore() throws Exception {
        setSearchQuery("Queen");

        assertTrue(invokeMatchesTrackSearch(track1), "Dovrebbe trovare la traccia dei Queen");
        assertFalse(invokeMatchesTrackSearch(track2));
    }

    @Test
    public void testRicercaPerGenere() throws Exception {
        setSearchQuery("Pop");

        assertTrue(invokeMatchesTrackSearch(track2), "Dovrebbe filtrare per genere Pop");
        assertFalse(invokeMatchesTrackSearch(track1));
    }

    @Test
    public void testRicercaPerTag() throws Exception {
        // Il tag ROCK restituisce "Rock & Alternative".
        // Cerchiamo "alternative" in minuscolo per testare la robustezza del filtro.
        setSearchQuery("alternative");

        assertTrue(invokeMatchesTrackSearch(track1), "Dovrebbe trovare la traccia tramite la sotto-stringa del tag 'Rock & Alternative'");
        assertFalse(invokeMatchesTrackSearch(track2), "La traccia senza tag non deve essere trovata");
    }

    @Test
    public void testRicercaPlaylistPerTitolo() throws Exception {
        setSearchQuery("preferiti");
        assertTrue(invokeMatchesPlaylistSearch(playlist1), "Dovrebbe trovare la playlist per titolo");

        setSearchQuery("Rock");
        assertFalse(invokeMatchesPlaylistSearch(playlist1), "Non dovrebbe trovare la playlist");
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