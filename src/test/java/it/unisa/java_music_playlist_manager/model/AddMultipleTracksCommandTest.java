package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddMultipleTracksCommandTest {

    private Library library;
    private Track track1;
    private Track track2;
    private AddMultipleTracksCommand command;

    @BeforeEach
    public void setUp() {
        library = Library.getInstance();
        for (Track t : library.getTracks()) {
            library.removeTrack(t);
        }
        
        track1 = new Track("Title1", "Author1", "Album1", 100, "Genre1", 2024, "path1_" + System.currentTimeMillis() + ".mp3");
        track2 = new Track("Title2", "Author2", "Album2", 200, "Genre2", 2025, "path2_" + System.currentTimeMillis() + ".mp3");
        
        List<Track> tracks = Arrays.asList(track1, track2);
        command = new AddMultipleTracksCommand(library, tracks);
    }

    @Test
    public void testExecute() {
        command.execute();
        
        assertEquals(2, library.getTracks().size());
        assertTrue(library.getTracks().contains(track1));
        assertTrue(library.getTracks().contains(track2));
    }

    @Test
    public void testUndo() {
        command.execute();
        command.undo();
        
        assertEquals(0, library.getTracks().size());
    }
}
