/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * Suite di test per l'Adapter di persistenza {@link JsonLibraryDAO}.
 * Verifica roundtrip (save → load) del catalogo, gestione file non
 * esistenti e (nei test avanzati) il polimorfismo Jackson per le
 * {@link AutomaticPlaylistByTag} / ByGenre / ByYear.
 */

public class JsonLibraryDAOTest {

    // Usiamo un file temporaneo per non sovrascrivere i salvataggi reali
    private static final String TEST_FILE_PATH = "test_library.json";
    
    private JsonLibraryDAO dao;
    private Library library;

    @BeforeEach
    public void setUp() {
        // Inizializziamo il DAO puntando al file di test
        dao = new JsonLibraryDAO(TEST_FILE_PATH);
        
        // Recuperiamo il Singleton della libreria
        library = Library.getInstance();
        
        // ATTENZIONE: Essendo un Singleton, dobbiamo pulire la memoria prima di ogni test.
        // Utilizziamo una rimozione iterativa basata sui metodi esistenti.
        for (Track t : library.getTracks()) {
            library.removeTrack(t);
        }
        for (Playlist p : library.getPlaylists()) {
            library.removePlaylist(p);
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Pulizia: cancelliamo il file JSON di test alla fine di ogni metodo
        Path path = Paths.get(TEST_FILE_PATH);
        Files.deleteIfExists(path);
    }

    // ==========================================
    // TEST 1: Comportamento con file non esistente
    // ==========================================
    @Test
    public void testLoadNonExistentFile() throws IOException {
        File file = new File(TEST_FILE_PATH);
        assertFalse(file.exists(), "Il file di test non dovrebbe esistere inizialmente");

        // Il DAO deve gestire l'assenza del file restituendo l'istanza vuota, senza crashare
        Library loadedLib = dao.load();
        
        assertNotNull(loadedLib, "Il metodo load() non deve restituire null");
        assertEquals(0, loadedLib.getTracks().size(), "La libreria deve essere vuota");
    }

    // ==========================================
    // TEST 2: Salvataggio e caricamento libreria vuota
    // ==========================================
    @Test
    public void testSaveAndLoadEmptyLibrary() throws IOException {
        dao.save(library);

        File file = new File(TEST_FILE_PATH);
        assertTrue(file.exists(), "Il file JSON deve essere stato creato");

        Library loadedLib = dao.load();
        
        assertNotNull(loadedLib);
        assertEquals(0, loadedLib.getTracks().size(), "La libreria ricaricata deve essere vuota");
    }

    // ==========================================
    // TEST 3: Salvataggio dati, verifica e integrità
    // ==========================================
    @Test
    public void testSaveAndLoadPopulatedLibrary() throws IOException {
        // 1. Preparazione dei dati utilizzando il costruttore completo di Track
        Track t1 = new Track(
            "Bohemian Rhapsody", 
            "Queen", 
            "A Night at the Opera", 
            354, 
            "Rock", 
            1975, 
            "C:/music/bohemian.mp3"
        );
        
        Track t2 = new Track(
            "Stairway to Heaven", 
            "Led Zeppelin", 
            "Led Zeppelin IV", 
            482, 
            "Rock", 
            1971, 
            "C:/music/stairway.mp3"
        );

        library.addTrack(t1);
        library.addTrack(t2);

        // 2. Salvataggio su disco
        dao.save(library);
        
        File file = new File(TEST_FILE_PATH);
        assertTrue(file.length() > 0, "Il file JSON non deve essere vuoto");

        // 3. Svuotiamo la memoria RAM per simulare la chiusura e riapertura dell'app
        for (Track t : library.getTracks()) {
            library.removeTrack(t);
        }

        // 4. Ricarichiamo i dati dal file JSON
        Library loadedLib = dao.load();

        // 5. Verifiche di integrità
        assertEquals(2, loadedLib.getTracks().size(), "Devono esserci esattamente 2 tracce caricate");
        
        // Recuperiamo la prima traccia (l'ordine nelle liste standard viene preservato dal JSON)
        Track loadedTrack = loadedLib.getTracks().get(0);
        
        assertEquals("Bohemian Rhapsody", loadedTrack.getTitle(), "Il titolo deve essere stato ripristinato");
        assertEquals("Queen", loadedTrack.getAuthor(), "L'autore deve essere stato ripristinato");
        assertEquals(1975, loadedTrack.getYear(), "L'anno deve essere stato ripristinato");
        assertEquals("C:/music/bohemian.mp3", loadedTrack.getFilePath(), "Il path deve essere stato ripristinato");
    }

    // ==========================================
    // TEST 4: Roundtrip polimorfismo Jackson — AutomaticPlaylistByTag
    // ==========================================
    @Test
    public void testSaveAndLoadAutomaticPlaylistByTagJacksonPolimorfismo() throws IOException {
        // 1. Setup dati
        Track t1 = new Track("Blinding Lights", "The Weeknd", "After Hours",
                200, "Pop", 2020, "C:/bl.mp3");
        Track t2 = new Track("Levitating", "Dua Lipa", "Future Nostalgia",
                203, "Pop", 2020, "C:/lv.mp3");
        t1.addTag(TagPredefined.PARTY);
        t2.addTag(TagPredefined.PARTY);

        library.addTrack(t1);
        library.addTrack(t2);

        // 2. Crea AutomaticPlaylist (tipo concreto!)
        AutomaticPlaylistByTag partyPlaylist = new AutomaticPlaylistByTag(
                "Party Hits", TagPredefined.PARTY);
        library.addPlaylist(partyPlaylist);

        // Verifica istantanea (prima della persistenza)
        assertEquals(2, partyPlaylist.getTrackCount(), "Prima del save: 2 brani col tag PARTY");

        // 3. Salvataggio JSON
        dao.save(library);
        File f = new File(TEST_FILE_PATH);
        assertTrue(f.exists() && f.length() > 0, "File JSON creato con AutomaticPlaylistByTag");

        // 4. Simula riavvio app: svuota la library (Singleton)
        for (Track t : library.getTracks()) library.removeTrack(t);
        for (Playlist p : library.getPlaylists()) library.removePlaylist(p);
        assertEquals(0, library.getPlaylists().size());

        // 5. Ricarica da JSON
        Library reloaded = dao.load();

        // 6. Verifica critica: il polimorfismo Jackson deve aver ricreato
        //    la classe CONCRETA (AutomaticPlaylistByTag), NON una superclasse.
        assertEquals(1, reloaded.getPlaylists().size());
        Playlist loadedPlaylist = reloaded.getPlaylists().get(0);

        assertTrue(loadedPlaylist instanceof AutomaticPlaylistByTag,
                "Jackson @JsonSubTypes deve deserializzare come AutomaticPlaylistByTag concreto, non generico!");
        AutomaticPlaylistByTag reloadedParty = (AutomaticPlaylistByTag) loadedPlaylist;

        assertEquals("Party Hits", reloadedParty.getTitle(), "Titolo preservato");
        assertEquals(TagPredefined.PARTY, reloadedParty.getFilterTag(), "filterTag preservato");

        // Dopo il reload, la Library riempita, la playlist dinamica deve
        // ri-trovare i suoi 2 brani (filtraggio lazy al volo)
        reloadedParty.setLibrary(reloaded);  // opzionale: testiamo anche la DI
        assertEquals(2, reloadedParty.getTrackCount(),
                "Dopo load, la playlist dinamica deve filtrare e trovare 2 brani col tag PARTY");
    }

    // ==========================================
    // TEST 5: Roundtrip AutomaticPlaylistByGenre
    // ==========================================
    @Test
    public void testSaveAndLoadAutomaticPlaylistByGenreRoundtrip() throws IOException {
        Track tRock = new Track("T", "A", "Al", 100, "Rock", 1990, "r.mp3");
        library.addTrack(tRock);

        AutomaticPlaylistByGenre genrePlaylist = new AutomaticPlaylistByGenre("Solo Rock", "Rock");
        library.addPlaylist(genrePlaylist);
        assertEquals(1, genrePlaylist.getTrackCount());

        dao.save(library);

        for (Track t : library.getTracks()) library.removeTrack(t);
        for (Playlist p : library.getPlaylists()) library.removePlaylist(p);

        Library reloaded = dao.load();
        assertEquals(1, reloaded.getPlaylists().size());

        Playlist loaded = reloaded.getPlaylists().get(0);
        assertTrue(loaded instanceof AutomaticPlaylistByGenre,
                "Polimorfismo Jackson errato: atteso AutomaticPlaylistByGenre");

        AutomaticPlaylistByGenre gp = (AutomaticPlaylistByGenre) loaded;
        assertEquals("Rock", gp.getGenreFilter());
        gp.setLibrary(reloaded);
        assertEquals(1, gp.getTrackCount(), "Filtro genere Rock deve funzionare anche dopo reload");
    }
}