package it.unisa.java_music_playlist_manager.model.command;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test per {@link UndoManager} (Cronologia Undo Command Pattern).
 * <p>
 * Copre:
 * <ul>
 *   <li>Cronologia vuota — undo() non crasha</li>
 *   <li>canUndo() true/false in base allo stato</li>
 *   <li>execute() esegue veramente il comando (chiamata execute())</li>
 *   <li>undo() annulla l'ultimo comando (chiamata undo()), ordine LIFO</li>
 *   <li>clearHistory() svuota la cronologia</li>
 *   <li><b>Boundary test</b>: oltre {@link UndoManager#MAX_HISTORY_SIZE} le
 *       entry pi&ugrave; vecchie vengono scartate (FIFO) — memory leak prevention</li>
 *   <li>Sequenze complesse: 3 comandi → 2 undo → stato iniziale ripristinato</li>
 * </ul>
 */
public class UndoManagerTest {

    private UndoManager undoManager;
    private Library library;

    @BeforeEach
    public void setUp() {
        undoManager = UndoManager.getInstance();
        undoManager.clearHistory();

        library = Library.getInstance();
        // Pulizia singleton per isolamento
        for (Track t : library.getTracks()) library.removeTrack(t);
        for (Playlist p : library.getPlaylists()) library.removePlaylist(p);
    }

    @AfterEach
    public void tearDown() {
        undoManager.clearHistory();
    }

    // =============================================================
    // 1. STATO INIZIALE / CRONOLOGIA VUOTA
    // =============================================================

    @Test
    public void testCronologiaVuotaUndoNonCrasha() {
        assertFalse(undoManager.canUndo(), "Cronologia vuota → canUndo() deve essere false");
        assertDoesNotThrow(() -> undoManager.undo(), "undo() a cronologia vuota non deve lanciare eccezioni");
    }

    @Test
    public void testClearHistorySvuotaLaCronologia() {
        Track t = new Track("T1", "A1", "Al1", 100, "Rock", 2000, "p1.mp3");
        undoManager.executeCommand(new AddTrackCommand(library, t));
        assertTrue(undoManager.canUndo());

        undoManager.clearHistory();

        assertFalse(undoManager.canUndo(), "Dopo clearHistory() non deve essere possibile annullare");
    }

    // =============================================================
    // 2. EXECUTE() + UNDO() —  comportamento base
    // =============================================================

    @Test
    public void testExecuteAggiungeTracciaEUndoLaRimuove() {
        Track traccia = new Track("Sultans of Swing", "Dire Straits",
                "Dire Straits", 351, "Rock", 1978, "sultans.mp3");

        assertEquals(0, library.getTracks().size(), "Stato iniziale: library vuota");

        undoManager.executeCommand(new AddTrackCommand(library, traccia));
        assertEquals(1, library.getTracks().size(), "Dopo execute() la traccia deve essere in library");
        assertTrue(undoManager.canUndo(), "Dopo execute() si deve poter annullare");

        undoManager.undo();
        assertEquals(0, library.getTracks().size(), "Dopo undo() la traccia deve essere rimossa");
        assertFalse(undoManager.canUndo(), "Dopo undo() a 0 comandi, canUndo() torna false");
    }

    @Test
    public void testUndoOrdineLIFO() {
        Track t1 = new Track("T1", "A1", "Al1", 100, "Rock", 2000, "p1.mp3");
        Track t2 = new Track("T2", "A2", "Al2", 120, "Pop", 2010, "p2.mp3");
        ManualPlaylist playlist = new ManualPlaylist("Test Rename");
        library.addPlaylist(playlist);

        // Sequenza comandi: aggiungi T1 → rinomina playlist → aggiungi T2
        undoManager.executeCommand(new AddTrackCommand(library, t1));
        undoManager.executeCommand(new RenamePlaylistCommand(playlist, "Nuovo Nome"));
        undoManager.executeCommand(new AddTrackCommand(library, t2));

        assertEquals(2, library.getTracks().size());
        assertEquals("Nuovo Nome", playlist.getTitle());

        // 1° Undo → annulla T2
        undoManager.undo();
        assertEquals(1, library.getTracks().size());
        assertFalse(library.getTracks().contains(t2), "T2 deve essere sparita (1° undo)");

        // 2° Undo → annulla rename
        undoManager.undo();
        assertEquals("Test Rename", playlist.getTitle(), "Nome playlist deve essere ripristinato (2° undo)");

        // 3° Undo → annulla T1
        undoManager.undo();
        assertEquals(0, library.getTracks().size(), "T1 deve essere sparita (3° undo)");
        assertFalse(undoManager.canUndo(), "Fine cronologia");
    }

    @Test
    public void testRenamePlaylistExecuteUndo() {
        ManualPlaylist playlist = new ManualPlaylist("Nome Iniziale");

        RenamePlaylistCommand cmd = new RenamePlaylistCommand(playlist, "Nome Finale");
        undoManager.executeCommand(cmd);

        assertEquals("Nome Finale", playlist.getTitle());

        undoManager.undo();
        assertEquals("Nome Iniziale", playlist.getTitle(), "undo() deve ripristinare il nome originale");
    }

    // =============================================================
    // 3. BOUNDARY TEST — MAX_HISTORY_SIZE = 100
    // =============================================================

    @Test
    public void testBoundedHistoryScartaLeVecchie() {
        final int OVER_LIMIT = UndoManager.MAX_HISTORY_SIZE + 10; // 110 comandi

        // Eseguiamo 110 comandi (10 oltre il limite)
        for (int i = 0; i < OVER_LIMIT; i++) {
            Track t = new Track("T" + i, "A", "Al", 60, "Rock", 2000, "p" + i + ".mp3");
            undoManager.executeCommand(new AddTrackCommand(library, t));
        }

        // 1) Stato intermedio: la cronologia NON può superare MAX_HISTORY_SIZE
        //    Eseguiamo 100 undo contigui: dopo 100 undo canUndo() deve essere false
        //    (perché le 10 entry più vecchie sono state scartate!)
        for (int i = 0; i < UndoManager.MAX_HISTORY_SIZE; i++) {
            assertTrue(undoManager.canUndo(), "Mancano ancora undo all'indice " + i);
            undoManager.undo();
        }
        assertFalse(undoManager.canUndo(),
                "Oltre MAX_HISTORY_SIZE le entry vecchie DEVONO essere scartate: 100 undo e cronologia vuota");

        // 2) Verifica: le prime 10 tracce (T0..T9) sono ANCORA presenti perché
        //    il loro comando è stato scartato dalla cronologia → undo() non può più rimuoverle
        long tracceRimanenti = library.getTracks().size();
        assertEquals(10, tracceRimanenti,
                "Le 10 entry scartate dalla history → T0..T9 non sono annullabili: devono restare in Library");
    }

    @Test
    public void testBoundedHistoryEsattamenteAlLimite() {
        // Caso esatto: MAX_HISTORY_SIZE comandi → tutti undoabili, nessuno scartato
        for (int i = 0; i < UndoManager.MAX_HISTORY_SIZE; i++) {
            Track t = new Track("T" + i, "A", "Al", 60, "Rock", 2000, "p" + i + ".mp3");
            undoManager.executeCommand(new AddTrackCommand(library, t));
        }

        // Undo di TUTTI i 100 comandi
        for (int i = 0; i < UndoManager.MAX_HISTORY_SIZE; i++) {
            assertTrue(undoManager.canUndo(), "Al passo " + i + " canUndo deve essere true");
            undoManager.undo();
        }
        assertFalse(undoManager.canUndo());
        assertEquals(0, library.getTracks().size(),
                "Con esattamente MAX_HISTORY_SIZE comandi, TUTTI devono essere annullabili");
    }
}
