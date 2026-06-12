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
 *
 * @author Mattia Sanzari
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
}