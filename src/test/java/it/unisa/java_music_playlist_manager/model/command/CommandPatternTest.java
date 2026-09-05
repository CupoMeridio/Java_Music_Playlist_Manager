package it.unisa.java_music_playlist_manager.model.command;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.TagPredefined;
import it.unisa.java_music_playlist_manager.model.Track;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test unitari per il <b>Pattern Command</b> implementato nel model.
 * <p>
 * Ogni Command viene verificato per:
 * <ul>
 *   <li><b>Correttezza di {@code execute()}</b> — lo stato risulta cambiato come atteso</li>
 *   <li><b>Correttezza di {@code undo()}</b> — lo stato torna <i>esattamente</i>
 *       al valore precedente (nessuna perdita di metadati)</li>
 *   <li>Idempotenza: execute() → undo() → execute() → undo() sempre coerente</li>
 * </ul>
 * <p>
 * Copertura: {@link AddTrackCommand}, {@link RemoveTrackCommand},
 * {@link RenamePlaylistCommand}, {@link UpdateTrackCommand}.
 */
public class CommandPatternTest {

    private Library library;

    @BeforeEach
    public void setUp() {
        library = Library.getInstance();
        // Pulizia Singleton per test indipendenti
        for (Track t : library.getTracks()) library.removeTrack(t);
        for (Playlist p : library.getPlaylists()) library.removePlaylist(p);
    }

    @AfterEach
    public void tearDown() {
        // Pulizia finale
        for (Track t : library.getTracks()) library.removeTrack(t);
        for (Playlist p : library.getPlaylists()) library.removePlaylist(p);
    }

    // =============================================================
    // 1. AddTrackCommand
    // =============================================================

    @Test
    public void testAddTrackCommandExecuteEUndo() {
        Track track = new Track("Hotel California", "Eagles",
                "Hotel California", 391, "Rock", 1976, "hotel.mp3");

        AddTrackCommand cmd = new AddTrackCommand(library, track);

        assertEquals(0, library.getTracks().size());

        cmd.execute();
        assertEquals(1, library.getTracks().size(), "execute() deve aggiungere la traccia");
        assertTrue(library.getTracks().contains(track));

        cmd.undo();
        assertEquals(0, library.getTracks().size(), "undo() deve rimuovere la traccia");
        assertFalse(library.getTracks().contains(track));
    }

    // =============================================================
    // 2. RemoveTrackCommand
    // =============================================================

    @Test
    public void testRemoveTrackCommandExecuteEUndo() {
        Track track = new Track("Africa", "Toto", "Toto IV",
                295, "Pop", 1982, "africa.mp3");
        library.addTrack(track);

        RemoveTrackCommand cmd = new RemoveTrackCommand(library, track);

        cmd.execute();
        assertEquals(0, library.getTracks().size(), "execute() rimuove la traccia");

        cmd.undo();
        assertEquals(1, library.getTracks().size(), "undo() ripristina la traccia");
        assertTrue(library.getTracks().contains(track));
    }

    // =============================================================
    // 3. RenamePlaylistCommand — controllo integrità oldName
    // =============================================================

    @Test
    public void testRenamePlaylistCommandExecuteUndo() {
        ManualPlaylist playlist = new ManualPlaylist("Rock Antenni 70");
        library.addPlaylist(playlist);

        RenamePlaylistCommand cmd = new RenamePlaylistCommand(playlist, "Rock Anni '70");

        cmd.execute();
        assertEquals("Rock Anni '70", playlist.getTitle(),
                "execute() deve applicare il nuovo titolo");

        cmd.undo();
        assertEquals("Rock Antenni 70", playlist.getTitle(),
                "undo() deve ripristinare ESATTAMENTE il titolo originale");
    }

    @Test
    public void testRenamePlaylistCommandUndoConStessoValoreIniziale() {
        // Caso sottile: se oldName è calcolato nel costruttore,
        // un rename sullo stesso valore → undo deve comunque ripristinare
        ManualPlaylist playlist = new ManualPlaylist("Invariato");
        library.addPlaylist(playlist);

        RenamePlaylistCommand cmd = new RenamePlaylistCommand(playlist, "Invariato");
        cmd.execute();
        cmd.undo();
        assertEquals("Invariato", playlist.getTitle());
    }

    // =============================================================
    // 4. UpdateTrackCommand — VERIFICA DI TUTTI I CAMPI (più critico)
    // =============================================================

    @Test
    public void testUpdateTrackCommandTuttiCampiUndoCorretto() {
        // STATO INIZIALE (vecchi valori):
        Track traccia = new Track("Old Title", "Old Author", "Old Album",
                100, "Old Genre", 2000, "old_path.mp3");
        traccia.addTag(TagPredefined.ROCK);
        traccia.addTag(TagPredefined.PARTY);
        library.addTrack(traccia);

        Set<Tag> tagsIniziali = Set.copyOf(traccia.getTags());
        assertEquals(2, tagsIniziali.size());

        // NUOVI VALORI:
        List<Tag> nuoviTag = List.of(TagPredefined.LOFI, TagPredefined.CHILL);
        UpdateTrackCommand cmd = new UpdateTrackCommand(
                traccia,
                "New Title",          // title
                "New Author",          // author
                "New Album",           // album
                "New Genre",           // genre
                2025,                  // year
                "new_path.mp3",        // filePath
                nuoviTag
        );

        // --- EXECUTE ---
        cmd.execute();

        assertEquals("New Title", traccia.getTitle());
        assertEquals("New Author", traccia.getAuthor());
        assertEquals("New Album", traccia.getAlbum());
        assertEquals("New Genre", traccia.getGenre());
        assertEquals(2025, traccia.getYear());
        assertEquals("new_path.mp3", traccia.getFilePath());
        assertTrue(traccia.getTags().containsAll(nuoviTag), "Nuovi tag applicati");
        assertEquals(2, traccia.getTags().size());

        // --- UNDO ---
        cmd.undo();

        // Verifica TUTTI i campi ripristinati allo stato iniziale
        assertEquals("Old Title", traccia.getTitle(), "UNDO: Title non ripristinato");
        assertEquals("Old Author", traccia.getAuthor(), "UNDO: Author non ripristinato");
        assertEquals("Old Album", traccia.getAlbum(), "UNDO: Album non ripristinato");
        assertEquals("Old Genre", traccia.getGenre(), "UNDO: Genre non ripristinato");
        assertEquals(2000, traccia.getYear(), "UNDO: Year non ripristinato");
        assertEquals("old_path.mp3", traccia.getFilePath(), "UNDO: FilePath non ripristinato");
        assertEquals(tagsIniziali, traccia.getTags(),
                "UNDO: I tag devono tornare ESATTAMENTE al set iniziale (ROCK + PARTY)");
    }

    @Test
    public void testUpdateTrackCommandConCampiNull() {
        Track traccia = new Track("Titolo", "Autore", "Album",
                100, "Genere", 2000, "path.mp3");
        traccia.setYear(null);  // anno nullo permesso
        traccia.addTag(TagPredefined.ROCK);
        library.addTrack(traccia);

        Integer annoIniziale = traccia.getYear(); // null
        Set<Tag> tagIniziali = Set.copyOf(traccia.getTags());

        UpdateTrackCommand cmd = new UpdateTrackCommand(
                traccia,
                "Nuovo",
                "Nuovo A",
                "Nuovo Al",
                "Nuovo G",
                1990,
                "new.mp3",
                List.of(TagPredefined.LOFI)
        );

        cmd.execute();
        assertEquals(1990, traccia.getYear());
        assertTrue(traccia.getTags().contains(TagPredefined.LOFI));

        cmd.undo();
        assertSame(annoIniziale, traccia.getYear(),
                "UNDO: anno null deve tornare null (no boxing tricks)");
        assertEquals(tagIniziali, traccia.getTags());
    }

    // =============================================================
    // 5. Sequenza composta: add → rename → update → 3 undo → ripristino
    // =============================================================

    @Test
    public void testSequenzaMultiplaComandiEUndoLifo() {
        Track t = new Track("Time", "Pink Floyd", "The Dark Side of the Moon",
                420, "Prog", 1973, "time.mp3");
        ManualPlaylist p = new ManualPlaylist("Playlist");
        library.addPlaylist(p);

        AddTrackCommand add = new AddTrackCommand(library, t);
        RenamePlaylistCommand rename = new RenamePlaylistCommand(p, "Floyd");
        UpdateTrackCommand update = new UpdateTrackCommand(
                t, "Time (Remix)", "Floyd", "TDSOTM",
                "Progressive Rock", 1973, "remix.mp3", List.of(TagPredefined.ROCK));

        // Sequenza execute
        add.execute();
        rename.execute();
        update.execute();

        assertEquals(1, library.getTracks().size());
        assertEquals("Floyd", p.getTitle());
        assertEquals("Time (Remix)", t.getTitle());

        // Undo in ordine LIFO → prima l'ultimo comando
        update.undo();
        rename.undo();
        add.undo();

        assertEquals(0, library.getTracks().size(), "AddTrack undoato");
        assertEquals("Playlist", p.getTitle(), "Rename undoato");
        assertEquals("Time", t.getTitle(), "Update undoato");
    }
}
