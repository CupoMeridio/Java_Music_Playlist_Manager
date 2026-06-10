package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * La classe Playlist rappresenta una collezione di elementi riproducibili (Playable).
 * Implementa il pattern Composite, permettendo a una playlist di contenere sia
 * singole tracce (Track) che altre playlist, trattandole in modo uniforme.
 * 
 * Pattern utilizzati:
 * - Composite (Composite): Playlist funge da contenitore che può ospitare altri
 *   oggetti Playable. Implementa i metodi della componente per gestire i figli.
 */
public class ManualPlaylist extends Playlist {
    /** Lista degli elementi contenuti (tracce o altre playlist) */
    private final List<Playable> elements;
    

    /**
     * Costruttore della classe Playlist.
     * 
     * @param title Il nome da assegnare alla playlist.
     */
    public ManualPlaylist(String title) {
        super(title);
        this.elements = new ArrayList<>();
    }

    /**
     * Aggiunge un elemento Playable alla playlist.
     * Gestisce i controlli per evitare ricorsioni infinite o auto-contenimento.
     * 
     * @param element L'elemento (Track o ManualPlaylist) da aggiungere.
     * @throws IllegalArgumentException Se l'elemento è nullo, se è la playlist stessa,
     *                                  o se creerebbe un ciclo di dipendenze.
     */
    @Override
    public void add(Playable element) {
        if (element == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un componente nullo.");
        }
        if (element == this) {
            throw new IllegalArgumentException("Una playlist non può contenere se stessa.");
        }
        // Controllo ricorsivo per evitare che la playlist A venga aggiunta alla playlist B
        // se B è già contenuta in A (direttamente o indirettamente).
        if (element instanceof ManualPlaylist playlist && playlist.containsRecursive(this)) {
            throw new IllegalArgumentException("Impossibile creare una dipendenza ciclica tra playlist.");
        }
        
        elements.add(element);
    }

    /**
     * Rimuove un elemento Playable dalla playlist.
     * 
     * @param element L'elemento da rimuovere.
     */
    @Override
    public void remove(Playable element) {
        if (element == null) {
            return ;
        }
        elements.remove(element);
    }
    
    @Override
    public void removeTrack(Track track){
        if (track == null) {
            return ;
        }
        elements.remove(track);
    }

    /**
     * Verifica se un elemento è contenuto direttamente nella playlist.
     * 
     * @param element L'elemento da cercare.
     * @return true se presente, false altrimenti.
     */
    public boolean contains(Playable element) {
        return elements.contains(element);
    }

    /**
     * Verifica ricorsivamente se un elemento è contenuto nella playlist o nelle sue sottoplaylist.
     * Utilizzato internamente per il controllo dei cicli.
     * 
     * @param target L'elemento da cercare.
     * @return true se trovato in qualsiasi livello della gerarchia, false altrimenti.
     */
    private boolean containsRecursive(Playable target) {
        for (Playable element : elements) {
            if (element.equals(target)) {
                return true;
            }
            if (element instanceof ManualPlaylist playlist && playlist.containsRecursive(target)) {
                return true;
            }
        }
        return false;
    }

    
    /**
     * Implementazione del metodo dell'interfaccia Playable (Pattern Composite).
     * Raccoglie ricorsivamente tutte le tracce presenti nella playlist e nelle sue sottoplaylist.
     * 
     * @return Una lista "appiattita" di tutte le tracce contenute.
     */
    @Override
    public List<Track> getTracks() {
        List<Track> allTracks = new ArrayList<>();
        // Sfrutta la ricorsione del Composite per raccogliere tutte le tracce
        for (Playable element : elements) {
            allTracks.addAll(element.getTracks());
        }
        return allTracks;
    }

    /**
     * Metodo di utilità per aggiungere specificamente una Track.
     * 
     * @param track La traccia da aggiungere.
     */
    public void addTrack(Track track) {
        add(track);
    }

   
}
