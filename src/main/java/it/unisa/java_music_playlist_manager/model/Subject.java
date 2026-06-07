package it.unisa.java_music_playlist_manager.model;

/**
 * L'interfaccia Subject definisce il contratto per gli oggetti che possono essere osservati.
 * Permette di gestire la registrazione, la rimozione e la notifica degli osservatori.
 * 
 * In questo progetto, classi come Library, Track e PlaybackManager implementano Subject
 * per avvisare la UI di cambiamenti strutturali o di stato.
 */
public interface Subject {
    /**
     * Registra un osservatore presso questo soggetto.
     * @param observer L'osservatore da aggiungere.
     */
    void attach(Observer observer);
    
    /**
     * Rimuove un osservatore precedentemente registrato.
     * @param observer L'osservatore da rimuovere.
     */
    void detach(Observer observer);
    
    /**
     * Notifica tutti gli osservatori registrati invocando il loro metodo update().
     */
    void notifyObservers();
}
