package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manager per l'history di operazioni reversibili (Command Pattern).
 * <p>
 * La dimensione massima dello stack &egrave; limitata a {@link #MAX_HISTORY_SIZE}
 * per evitare consumi di memoria illimitati in caso di librerie grandi
 * (es. migliaia di operazioni di editing di metadati).
 * Le entry pi&ugrave; vecchie della capacit&agrave; vengono scartate automaticamente
 * in ordine FIFO quando si aggiunge un nuovo comando.
 */
public class UndoManager {
    /**
     * Dimensione massima della cronologia undo. Oltre questo limite le entry
     * piu' vecchie vengono dimenticate.
     */
    public static final int MAX_HISTORY_SIZE = 100;

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
     * Se la cronologia supera {@link #MAX_HISTORY_SIZE}, la entry piu' vecchia
     * viene scartata per evitare memory leak.
     */
    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }
    }

    /**
     * Annulla l'ultima operazione effettuata (Undo).
     * Se la cronologia e' vuota non fa nulla.
     */
    public void undo() {
        if (!history.isEmpty()) {
            Command lastCommand = history.pop();
            lastCommand.undo();
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