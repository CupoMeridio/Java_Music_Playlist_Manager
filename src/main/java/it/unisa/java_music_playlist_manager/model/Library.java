
package it.unisa.java_music_playlist_manager.model;



import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta la libreria musicale principale dell'applicazione (Pattern Singleton).
 * Mantiene il catalogo globale dei brani e delle playlist, notificando gli osservatori
 * ad ogni modifica della collezione.
 */
public class Library implements Subject {
    
    private static Library instance;

    // Jackson deve riscrivere questi campi tramite reflection durante la
    // deserializzazione JSON roundtrip (JsonLibraryDAOTest). Sono NON-finali
    // per evitare i warning "Final field X has been mutated reflectively" e
    // la futura incompatibilità con JDK dove la mutazione di campi finali
    // verrà bloccata per default. L'immutabilità della referenza è comunque
    // garantita dalla visibilità privata e dall'assenza di setter; il
    // costruttore privato le inizializza sempre a ArrayList vuoti.
    private List<Track> tracks;

    private List<Playlist> playlists;

    @JsonIgnore
    private final List<Observer> observers;

    private Library() {
        this.tracks = new ArrayList<>();
        this.playlists = new ArrayList<>();
        this.observers = new ArrayList<>();
    }
    
    /**
     * Restituisce l'istanza singleton della libreria musicale.
     *
     * @return L'istanza condivisa di {@link Library}.
     */
    public static synchronized Library getInstance() {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un brano nullo.");
        }

        for (Track existingTrack : this.tracks) {
            if (existingTrack.getFilePath().equals(track.getFilePath())) {
                throw new IllegalArgumentException("Il brano '" + track.getTitle() + "' è già presente nella libreria.");
            }
        }

        this.tracks.add(track);
        notifyObservers();
    }
    
    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }


    public boolean removeTrack(Track track) {
        if (track == null) {
            return false;
        }
        boolean isRemoved = this.tracks.remove(track);

        if (isRemoved) {

            for (Playlist playlist : this.playlists) {
                if (playlist instanceof ManualPlaylist manualPlaylist) {
                    manualPlaylist.removeTrack(track);
                }
            }

            notifyObservers();
        }
        return isRemoved;
    }

    public void addPlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Impossibile aggiungere una playlist nulla.");
        }

        this.playlists.add(playlist);
        notifyObservers();
    }

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

    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }

    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
