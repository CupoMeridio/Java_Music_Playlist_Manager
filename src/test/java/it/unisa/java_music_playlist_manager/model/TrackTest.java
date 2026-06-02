/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class TrackTest {
    
    private Track track;
    
    public TrackTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        
    }
    
    @AfterAll
    public static void tearDownClass() {

    }
    
    @BeforeEach
    public void setUp() {
        // eseguito prima di ogni singolo test. 
        track = new Track("Epitaph", "King Crimson", 527, "Progressive Rock", 1969);
    }
    
    @AfterEach
    public void tearDown() {
        // eseguito dopo ogni singolo test
        track = null;
    }

    // TEST COMPORTAMENTO CORRETTO

    @Test
    public void testCreazioneValidaEGetters() {
        assertEquals("Epitaph", track.getTitle());
        assertEquals("King Crimson", track.getAuthor());
        assertEquals(527, track.getDuration());
        assertEquals("Progressive Rock", track.getGenre());
        assertEquals(1969, track.getYear());
    }

    @Test
    public void testTrimDelleStringhe() {
        // rimuove spazi vuoti?
        Track tracciaSpaziata = new Track("   Starless   ", "  King Crimson  ", 742, "  Prog  ", 1974);
        
        assertEquals("Starless", tracciaSpaziata.getTitle());
        assertEquals("King Crimson", tracciaSpaziata.getAuthor());
        assertEquals("Prog", tracciaSpaziata.getGenre());
    }

    // TEST ECCEZIONI

    @Test
    public void testSetTitleInvalido() {
        assertThrows(IllegalArgumentException.class, () -> track.setTitle(null), "Dovrebbe lanciare eccezione per titolo nullo");
        assertThrows(IllegalArgumentException.class, () -> track.setTitle(""), "Dovrebbe lanciare eccezione per titolo vuoto");
        assertThrows(IllegalArgumentException.class, () -> track.setTitle("   "), "Dovrebbe lanciare eccezione per titolo composto solo da spazi");
    }

    @Test
    public void testSetAuthorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> track.setAuthor(null));
        assertThrows(IllegalArgumentException.class, () -> track.setAuthor(""));
        assertThrows(IllegalArgumentException.class, () -> track.setAuthor("   "));
    }

    @Test
    public void testSetDurationInvalida() {
        assertThrows(IllegalArgumentException.class, () -> track.setDuration(0), "La durata 0 non è ammessa");
        assertThrows(IllegalArgumentException.class, () -> track.setDuration(-150), "La durata negativa non è ammessa");
    }

    @Test
    public void testSetGenreInvalido() {
        assertThrows(IllegalArgumentException.class, () -> track.setGenre(null));
        assertThrows(IllegalArgumentException.class, () -> track.setGenre(""));
        assertThrows(IllegalArgumentException.class, () -> track.setGenre("   "));
    }

    @Test
    public void testSetYearInvalido() {
        assertThrows(IllegalArgumentException.class, () -> track.setYear(-5), "L'anno negativo non è ammesso");
        assertThrows(IllegalArgumentException.class, () -> track.setYear(2027), "L'anno nel futuro (oltre il 2026) non è ammesso");
    }
    
    @Test
    public void testCostruttoreConParametriInvalidi() {
        // il costruttore blocca l'inserimento propagando l'eccezione dei setter
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Track("Titolo", "Autore", -10, "Genere", 2020);
        });
        
        assertEquals("La durata deve essere maggiore di zero secondi.", exception.getMessage());
    }
    
        // TEST PATTERN OBSERVER

    @Test
    public void testObserverNotificationOnEdit() {
        // observer finto per test
        class TestObserver implements Observer {
            boolean isUpdated = false;
            
            @Override
            public void update() {
                isUpdated = true;
            }
        }
        
        TestObserver observer = new TestObserver();
        track.attach(observer);
        
        // modifico titolo: deve esser notificato l'observer
        track.setTitle("Nuovo Titolo Modificato");
        assertTrue(observer.isUpdated, "L'observer deve essere notificato dopo la modifica effettiva del titolo");
        
        // resetto flag
        observer.isUpdated = false;
        track.setYear(2025);
        assertTrue(observer.isUpdated, "L'observer deve essere notificato dopo la modifica effettiva dell'anno");
    }

    @Test
    public void testObserverNotNotifiedWhenValueIsSame() {
        class TestObserver implements Observer {
            boolean isUpdated = false;
            
            @Override
            public void update() {
                isUpdated = true;
            }
        }
        
        TestObserver observer = new TestObserver();
        track.attach(observer);
        
        // stesso identico titolo con cui la traccia è stata inizializzata nel setUp() ("Epitaph")
        track.setTitle("Epitaph");
        
        // observer NON dovrebbe essere notificato perché il valore non è realmente cambiato
        assertFalse(observer.isUpdated, "L'observer non deve essere notificato se il nuovo valore è uguale al precedente");
        
        // verifico anche con gli spazi vuoti, dato che il setter applica il trim()
        track.setTitle("   Epitaph   ");
        assertFalse(observer.isUpdated, "L'observer non deve essere notificato nemmeno se si inseriscono spazi ignorati dal trim");
    }

    @Test
    public void testObserverDetach() {
        class TestObserver implements Observer {
            boolean isUpdated = false;
            
            @Override
            public void update() {
                isUpdated = true;
            }
        }
        
        TestObserver observer = new TestObserver();
        track.attach(observer);
        
        // scollego
        track.detach(observer);
        
        track.setAuthor("Nuovo Autore");
        
        // verifico che non viene notificato essendo scollegato
        assertFalse(observer.isUpdated, "L'observer non deve essere notificato se è stato scollegato (detach)");
    }


    @Test
    public void testTrackInfoVisualization() {
        // 1. Verifica iniziale (Dati inseriti nel setUp)
        assertEquals("Epitaph", track.getTitle(), "Il titolo restituito non è corretto");
        assertEquals("King Crimson", track.getAuthor(), "L'artista restituito non è corretto");
        assertEquals(527, track.getDuration(), "La durata restituita non è corretta");
        assertEquals("Progressive Rock", track.getGenre(), "Il genere restituito non è corretto");
        assertEquals(1969, track.getYear(), "L'anno restituito non è corretto");

        // 2. Verifica dopo la modifica (i setter devono aggiornare i metadati restituiti dai getter)
        track.setTitle("21st Century Schizoid Man");
        track.setAuthor("Crimson");
        track.setDuration(443);
        track.setGenre("Heavy Prog");
        track.setYear(1969);

        assertEquals("21st Century Schizoid Man", track.getTitle());
        assertEquals("Crimson", track.getAuthor());
        assertEquals(443, track.getDuration());
        assertEquals("Heavy Prog", track.getGenre());
        assertEquals(1969, track.getYear());
    }

}