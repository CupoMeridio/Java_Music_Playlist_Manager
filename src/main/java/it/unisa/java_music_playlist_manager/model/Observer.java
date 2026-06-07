
package it.unisa.java_music_playlist_manager.model;


/**
 * L'interfaccia Observer definisce il contratto per gli oggetti che desiderano
 * essere notificati dei cambiamenti in un oggetto Subject.
 * 
 * Fa parte dell'implementazione del Pattern Observer personalizzato del progetto,
 * utilizzato per mantenere sincronizzata l'interfaccia utente (UI) con il Modello.
 */
public interface Observer {
    /**
     * Metodo invocato dal Subject per notificare un cambiamento di stato.
     */
    void update();
}
