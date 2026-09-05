package it.unisa.java_music_playlist_manager.model.command;

/**
 * Interfaccia comune per l'implementazione del pattern Command.
 * Permette di incapsulare una richiesta come oggetto, supportando le operazioni di Undo/Redo.
 */
public interface Command {
    /** Esegue il comando modificando lo stato dell'applicazione. */
    void execute();

    /** Annulla le modifiche apportate dall'esecuzione del comando. */
    void undo();
}