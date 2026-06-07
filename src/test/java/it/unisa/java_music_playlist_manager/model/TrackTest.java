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
        String baseDir = System.getProperty("user.dir");
        String realPath = new java.io.File(baseDir, "brani di prova/Vibing Over Venus.mp3").getAbsolutePath();
        track = new Track("Epitaph", "King Crimson", "In the Court of the Crimson King", 527, "Progressive Rock", 1969, realPath);
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
        assertEquals("In the Court of the Crimson King", track.getAlbum());
        assertEquals(527, track.getDuration());
        assertEquals("Progressive Rock", track.getGenre());
        assertEquals(1969, track.getYear());
        assertTrue(track.getFilePath().contains("Vibing Over Venus.mp3"));
    }

    @Test
    public void testTrimDelleStringhe() {
        // rimuove spazi vuoti?
        Track tracciaSpaziata = new Track("   Starless   ", "  King Crimson  ", "  Red  ", 742, "  Prog  ", 1974, "path.mp3");
        
        assertEquals("Starless", tracciaSpaziata.getTitle());
        assertEquals("King Crimson", tracciaSpaziata.getAuthor());
        assertEquals("Red", tracciaSpaziata.getAlbum());
        assertEquals("Prog", tracciaSpaziata.getGenre());
    }

    // TEST ECCEZIONI

    @Test
    public void testSetAlbumInvalido() {
        track.setAlbum(null);
        assertEquals("Sconosciuto", track.getAlbum());
        track.setAlbum("");
        assertEquals("Sconosciuto", track.getAlbum());
    }

    @Test
    public void testSetTitleInvalido() {
        assertThrows(IllegalArgumentException.class, () -> track.setTitle(null));
        assertThrows(IllegalArgumentException.class, () -> track.setTitle(""));
    }

    @Test
    public void testSetAuthorInvalido() {
        track.setAuthor(null);
        assertEquals("Sconosciuto", track.getAuthor());
        track.setAuthor("");
        assertEquals("Sconosciuto", track.getAuthor());
    }

    @Test
    public void testSetDurationInvalida() {
        track.setDuration(-10);
        assertEquals(0, track.getDuration(), "La durata negativa deve essere impostata a 0");
    }

    @Test
    public void testSetGenreInvalido() {
        track.setGenre(null);
        assertEquals("Generico", track.getGenre());
        track.setGenre("");
        assertEquals("Generico", track.getGenre());
    }

    @Test
    public void testSetYearInvalido() {
        int currentYear = java.time.LocalDate.now().getYear();
        
        // Verifica anno nel futuro: lancia eccezione
        assertThrows(IllegalArgumentException.class, () -> track.setYear(currentYear + 1), "L'anno futuro deve lanciare IllegalArgumentException");
        
        // Verifica anno null: permesso e impostato a null
        track.setYear(null);
        assertNull(track.getYear(), "L'anno nullo deve essere permesso e impostato a null");

        // Verifica anno negativo: portato a null
        track.setYear(-5);
        assertNull(track.getYear(), "L'anno negativo deve essere portato a null");
    }
    
    @Test
    public void testCostruttoreConParametriInvalidi() {
        // il costruttore blocca l'inserimento solo per i campi critici (titolo e percorso file)
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("", "Autore", "Album", 120, "Genere", 2020, "path.mp3");
        }, "Il titolo vuoto deve lanciare eccezione");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Track("Titolo", "Autore", "Album", 120, "Genere", 2020, "");
        }, "Il percorso file vuoto deve lanciare eccezione");
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