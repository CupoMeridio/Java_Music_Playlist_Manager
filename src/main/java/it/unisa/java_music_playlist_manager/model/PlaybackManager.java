package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.List;

public class PlaybackManager {
    // Singleton
    private static PlaybackManager instance;

    // Stato corrente del pattern State
    private PlaybackState currentState;

    // Struttura dati della coda
    private List<Track> currentQueue;
    private int currentIndex;

    // Riferimento alla strategia corrente (Pattern Strategy)
    private PlaybackStrategy currentStrategy;

    // Costruttore privato
    private PlaybackManager() {
        this.currentState = new StoppedState(); // Stato iniziale di default
        this.currentQueue = new ArrayList<>();
        this.currentIndex = 0;
        this.currentStrategy = new SequentialStrategy(); // di default parte con la riproduzione sequenziale
    }

    // Accesso globale al Singleton
    public static synchronized PlaybackManager getInstance() {
        if (instance == null) {
            instance = new PlaybackManager();
        }
        return instance;
    }

    // Metodo richiamato dal Controller per caricare le canzoni da riprodurre
    public void setQueue(List<Track> newTracks) {
        if (newTracks != null) {
            this.currentQueue = newTracks;
            this.currentIndex = 0;
            System.out.println("[MANAGER] Coda aggiornata con " + newTracks.size() + " brani.");
        }
    }

    // Metodo richiamato dal Controller per cambiare strategia a runtime
    public void setStrategy(PlaybackStrategy newStrategy) {
        if (newStrategy != null) {
            this.currentStrategy = newStrategy;
            System.out.println("[MANAGER] Nuova strategia impostata: " + newStrategy.getClass().getSimpleName());
        }
    }

    // Metodo richiamato dagli Stati concreti per cambiare lo stato dell'app
    public void changeState(PlaybackState newState) {
        this.currentState = newState;
    }

    // Prossima canzone definita dalla strategia corrente
    public void advanceQueue() {
        if (currentQueue != null && !currentQueue.isEmpty()) {
            // Delega il calcolo del prossimo indice alla strategia attiva
            this.currentIndex = currentStrategy.getNextIndex(this.currentIndex, this.currentQueue.size());
            System.out.println("[MANAGER] Avanzamento coda. Nuovo indice calcolato: " + this.currentIndex);
        }
    }

    // Canzone precedente
    public void regressQueue() {
        if (currentQueue != null && !currentQueue.isEmpty()) {
            // Se l'indice è andato oltre la fine della coda ( SequentialStrategy ha restituito queueSize)
            // premendo indietro ritorniamo all'ultimo brano valido
            if (this.currentIndex >= currentQueue.size()) {
                this.currentIndex = currentQueue.size() - 1;
            } else if (this.currentIndex > 0) {
                this.currentIndex--;
            } else {
                // Altrimenti lo forziamo a rimanere a 0 (Ricomincia la canzone da capo)
                this.currentIndex = 0;
                System.out.println("[MANAGER] Sei già al primo brano. L'indice resta a 0.");
            }
        }
    }

    public void resetQueue() {
        this.currentIndex = 0;
    }

    public Track getCurrentTrack() {
        if (currentQueue == null || currentIndex >= currentQueue.size() || currentIndex < 0) {
            return null;
        }
        return currentQueue.get(currentIndex);
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    /**
     * Carica nel player la lista di brani corrente (Libreria o Playlist)
     * e imposta l'indice esattamente sulla canzone selezionata dall'utente.
     */
    public void selectAndLoadTrack(Track selectedTrack, List<Track> tracksContext) {
        if (tracksContext != null && selectedTrack != null) {
            this.currentQueue = tracksContext;
            // Trova la posizione del brano selezionato all'interno del contesto attuale
            this.currentIndex = tracksContext.indexOf(selectedTrack);

            // Fallback di sicurezza se per qualche motivo non dovesse trovarlo
            if (this.currentIndex == -1) {
                this.currentIndex = 0;
            }

            System.out.println("[MANAGER] Coda impostata con " + tracksContext.size() + " brani. " +
                    "Brano attivo: " + selectedTrack.getTitle() + " (Indice: " + this.currentIndex + ")");
        }
    }



    // --- INTERFACCIA PER I BOTTONI (Metodi delegati allo Stato Corrente) ---

    public void pressPlay() {
        currentState.play(this);
    }
    public void pressStop() {
        currentState.stop(this);
    }

    public void pressNext() {
        currentState.next(this);
    }

    public void pressPrevious() {
        currentState.previous(this);
    }


}