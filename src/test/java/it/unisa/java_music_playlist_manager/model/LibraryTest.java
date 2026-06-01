/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class LibraryTest {
    
    private Library library;
    private Track track1;
    private Track track2;
    
    public LibraryTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        // essendo singleton devo otternere istanza
        library = Library.getInstance();
        
        track1 = new Track("Bohemian Rhapsody", "Queen", 354, "Rock", 1975);
        track2 = new Track("Stairway to Heaven", "Led Zeppelin", 482, "Rock", 1971);
    }
    
    @AfterEach
    public void tearDown() {
        // PULIZIA SINGLETON
        // la Library mantiene il suo stato tra un test e l'altro,
        // assicuraro di svuotarla dopo ogni test
        List<Track> currentTracks = library.getTracks();
        for (Track t : currentTracks) {
            library.removeTrack(t);
        }
    }

    // TEST SINGLETON

    @Test
    public void testSingletonInstance() {
        Library instance1 = Library.getInstance();
        Library instance2 = Library.getInstance();
        
        // le due istanza devono essere lo stesso oggetto in memoria
        assertSame(instance1, instance2, "Il metodo getInstance deve restituire sempre la stessa istanza");
    }

    // AGGIUNTA E RECUPERO TRACCE

    @Test
    public void testAddTrackValida() {
        library.addTrack(track1);
        
        List<Track> tracks = library.getTracks();
        assertEquals(1, tracks.size(), "La libreria dovrebbe contenere 1 brano");
        assertTrue(tracks.contains(track1), "La libreria dovrebbe contenere il brano inserito");
    }

    @Test
    public void testAddTrackNulla() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            library.addTrack(null);
        });
        
        assertEquals("Impossibile aggiungere un brano nullo.", exception.getMessage());
    }

    @Test
    public void testGetTracksRitornaCopia() {
        library.addTrack(track1);
        
        // ottengo lista e provo a modificarla
        List<Track> copiaTracks = library.getTracks();
        copiaTracks.add(track2); // aggiungo track 2 solo alla copia
        
        // verifico che lista originale sia intatta
        assertEquals(1, library.getTracks().size(), "Modificare la lista restituita non deve alterare la libreria interna");
        assertFalse(library.getTracks().contains(track2), "La libreria originale non deve contenere il brano aggiunto alla copia");
    }

    // TEST RIMOZIONE TRACCE

    @Test
    public void testRemoveTrackEsistente() {
        library.addTrack(track1);
        library.addTrack(track2);
        
        library.removeTrack(track1);
        
        List<Track> tracks = library.getTracks();
        assertEquals(1, tracks.size(), "La libreria dovrebbe contenere solo 1 brano dopo la rimozione");
        assertFalse(tracks.contains(track1), "Il brano rimosso non dovrebbe più essere presente");
        assertTrue(tracks.contains(track2), "Il brano non rimosso dovrebbe essere ancora presente");
    }

    @Test
    public void testRemoveTrackInesistente() {
        library.addTrack(track1);
        
        // rimuovo brano che non è stato mai aggiunto
        library.removeTrack(track2);
        
        assertEquals(1, library.getTracks().size(), "La dimensione della libreria non deve cambiare se il brano non esiste");
    }

    @Test
    public void testRemoveTrackNulla() {
        library.addTrack(track1);
        
        // return silenzioso se la traccia è nulla
        // verific che non lanci eccezioni e non alteri la lista
        assertDoesNotThrow(() -> library.removeTrack(null), "Rimuovere una traccia nulla non dovrebbe lanciare eccezioni");
        assertEquals(1, library.getTracks().size(), "La libreria non deve subire modifiche");
    }
}