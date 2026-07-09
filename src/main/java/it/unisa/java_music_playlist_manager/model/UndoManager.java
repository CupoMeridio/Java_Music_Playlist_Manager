package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
    private static UndoManager instance;
    private final Deque<Command> history = new ArrayDeque<>();

    private UndoManager() {}

    public static synchronized UndoManager getInstance() {
        if (instance == null) {
            instance = new UndoManager();
        }
        return instance;
    }

    /**
     * Esegue un comando e lo salva nella cronologia per l'undo.
     */
    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
    }

    /**
     * Annulla l'ultima operazione effettuata (Undo).
     */
    public void undo() {
        if (!history.isEmpty()) {
            Command lastCommand = history.pop();
            lastCommand.undo();
        } else {
        }
    }

    /**
     * Svuota la cronologia (es. al cambio di libreria o chiusura).
     */
    public void clearHistory() {
        history.clear();
    }

    /**
     *  Verifica se ci sono operazioni memorizzate nella cronologia.
     * Serve al controller per attivare/disattivare il pulsante Undo nella UI.
     */
    public boolean canUndo() {
        return !history.isEmpty();
    }

}