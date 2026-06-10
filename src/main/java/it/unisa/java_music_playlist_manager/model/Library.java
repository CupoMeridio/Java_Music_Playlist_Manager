package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.List;


/**
 * La classe Library funge da archivio centrale per l'intera applicazione.
 * Gestisce l'elenco globale di tutte le tracce musicali e delle playlist create dall'utente.
 * 
 * Pattern utilizzati:
 * - Singleton: Garantisce che esista un'unica istanza della libreria in tutta l'applicazione,
 *   fornendo un punto di accesso globale ai dati musicali.
 * - Observer (Subject): Implementa l'interfaccia Subject per notificare eventuali osservatori
 *   (come le classi della UI) quando avvengono cambiamenti nella libreria (aggiunta/rimozione tracce o playlist).
 */
public class Library implements Subject{
    
    /** Istanza unica della classe (Pattern Singleton) */
    private static Library instance;

    /** Lista di tutte le tracce caricate nella libreria */
    private final List<Track> tracks;
    
    /** Lista di tutte le playlist create dall'utente */
    private final List<Playlist> playlists;
    
    /** Lista degli osservatori registrati per ricevere notifiche sui cambiamenti (Pattern Observer) */
    private final List<Observer> observers;

    /**
     * Costruttore privato per impedire l'istanziazione esterna (Pattern Singleton).
     * Inizializza le liste interne per tracce, playlist e osservatori.
     */
    private Library() {
        this.tracks = new ArrayList<>();
        this.playlists = new ArrayList<>();
        this.observers = new ArrayList<>();
    }
    
    /**
     * Fornisce l'accesso all'unica istanza della classe Library.
     * Utilizza la sincronizzazione per garantire la thread-safety durante la creazione dell'istanza.
     * 
     * @return L'istanza singleton della libreria.
     */
    public static synchronized Library getInstance() {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    /**
     * Aggiunge una nuova traccia alla libreria.
     * Esegue un controllo di integrità (non nullo) e verifica che non esistano duplicati
     * basandosi sul percorso del file.
     * 
     * @param track La traccia da aggiungere.
     * @throws IllegalArgumentException Se la traccia è nulla o se il file è già presente in libreria.
     */
    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un brano nullo.");
        }

        // Verifica duplicati basandosi sul percorso del file per evitare ridondanze nell'archivio
        for (Track existingTrack : this.tracks) {
            if (existingTrack.getFilePath().equals(track.getFilePath())) {
                throw new IllegalArgumentException("Il brano '" + track.getTitle() + "' è già presente nella libreria.");
            }
        }

        this.tracks.add(track);
        notifyObservers(); // Notifica la UI o altri componenti del cambiamento
    }
    
    /**
     * Restituisce una copia della lista delle tracce.
     * Viene restituita una copia per preservare l'incapsulamento ed evitare modifiche esterne
     * non controllate alla lista originale.
     * 
     * @return Una nuova lista contenente tutte le tracce della libreria.
     */
    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }


    /**
     * Rimuove una traccia dalla libreria e da tutte le playlist che la contengono.
     * Implementa una rimozione a cascata per mantenere la coerenza dei dati.
     * 
     * @param track La traccia da rimuovere.
     * @return true se la traccia è stata trovata e rimossa, false altrimenti.
     */
    public boolean removeTrack(Track track) {    // la rendo booleana per eventuali controlli
        if (track == null) {
            return false;
        }
        boolean isRemoved = this.tracks.remove(track);

        // Se la traccia è stata effettivamente rimossa dalla libreria centrale,
        // dobbiamo rimuoverla anche da ogni playlist per evitare riferimenti a tracce inesistenti.
        if (isRemoved) {

            for (Playlist playlist : this.playlists) {
                if (playlist instanceof ManualPlaylist manualPlaylist) {
                    manualPlaylist.removeTrack(track);
                }
            }

            notifyObservers(); // Notifica il cambiamento dopo la pulizia a cascata
        }
        return isRemoved;
    }

    /**
     * Aggiunge una nuova playlist alla libreria.
     * 
     * @param playlist La playlist da aggiungere.
     * @throws IllegalArgumentException Se la playlist è nulla.
     */
    public void addPlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Impossibile aggiungere una playlist nulla.");
        }

        this.playlists.add(playlist);
        notifyObservers();
    }

    /**
     * Rimuove una playlist dalla libreria.
     * Non rimuove le tracce contenute nella playlist dalla libreria globale.
     * 
     * @param playlist La playlist da rimuovere.
     * @return true se la playlist è stata trovata e rimossa, false altrimenti.
     */
    public boolean removePlaylist(Playlist playlist) {
        if (playlist == null) {
            return false;
        }

        boolean isRemoved = this.playlists.remove(playlist);

        if (isRemoved) {
            notifyObservers();
        }

        return isRemoved;
    }

    /**
     * Restituisce una copia della lista delle playlist.
     * 
     * @return Una nuova lista contenente tutte le playlist della libreria.
     */
    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }

    // --- Metodi dell'interfaccia Subject (Pattern Observer) ---

    /**
     * Registra un nuovo osservatore.
     * 
     * @param observer L'osservatore da aggiungere.
     */
    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     * 
     * @param observer L'osservatore da rimuovere.
     */
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notifica tutti gli osservatori registrati invocando il loro metodo update().
     * Viene chiamato ogni volta che la struttura della libreria subisce una modifica significativa.
     */
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
