package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.List;
public class PlaybackManager {
    // Singleton
    private static PlaybackManager instance;

    // Stato corrente del pattern State
    private PlaybackState currentState;

    // Struttura dati della coda
    private final List<Playable> queue = new ArrayList<>();
    private int currentPlayableIndex = 0;
    private int currentTrackIndexInPlayable = 0;

    // Riferimento alla strategia corrente (Pattern Strategy)
    private PlaybackStrategy currentStrategy;

    // Costruttore privato
    private PlaybackManager() {
        this.currentState = new StoppedState(); // Stato iniziale di default
        this.currentStrategy = new SequentialStrategy(); // di default parte con la riproduzione sequenziale
    }

    // Accesso globale al Singleton
    public static synchronized PlaybackManager getInstance() {
        if (instance == null) {
            instance = new PlaybackManager();
        }
        return instance;
    }

    // ---- GESTIONE DELLO STATO (Deleghe) ----
    public void changeState(PlaybackState state) {
        this.currentState = state;
    }

    public void pressPlay() {
        if (queue.isEmpty()) {
            System.out.println("[MANAGER - ERROR] Impossibile avviare il player: nessuna canzone caricata nella coda.");
            return;
        }
        currentState.play(this);
    }

    public void pressStop() {
        if (queue.isEmpty()) return;
        currentState.stop(this);
    }

    public void pressNext() {
        if (queue.isEmpty()) return;
        currentState.next(this);
    }

    public void pressNextPlayable() {
        if (queue.isEmpty()) return;
        currentState.nextPlayable(this);
    }

    public void pressPrevious() {
        if (queue.isEmpty()) return;
        currentState.previous(this);
    }

    public void pressPreviousPlayable() {
        if (queue.isEmpty()) return;
        currentState.previousPlayable(this);
    }

    // ---- METODI PRATICI DI CODA ----
    public void addToQueue(Playable playable) {
        if (playable == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un elemento nullo alla coda.");
        }
        queue.add(playable);
        if (queue.size() == 1) {
            resetQueue();
            skipEmptyPlayablesForward();
        }
    }

    public void setQueue(List<? extends Playable> newItems) {
        if (newItems != null) {
            this.queue.clear();
            this.queue.addAll(newItems);
            this.currentPlayableIndex = 0;
            this.currentTrackIndexInPlayable = 0;
            skipEmptyPlayablesForward();
            System.out.println("[MANAGER] Coda aggiornata con " + newItems.size() + " elementi Playable.");
        }
    }

    public Track getCurrentTrack() {
        if (queue.isEmpty() || currentPlayableIndex >= queue.size() || currentPlayableIndex < 0) {
            return null;
        }
        Playable currentPlayable = queue.get(currentPlayableIndex);
        List<Track> tracks = currentPlayable.getTracks();

        if (currentTrackIndexInPlayable >= tracks.size() || currentTrackIndexInPlayable < 0) {
            return null;
        }
        return tracks.get(currentTrackIndexInPlayable);
    }

    public Playable getCurrentPlayable() {
        if (queue.isEmpty() || currentPlayableIndex >= queue.size() || currentPlayableIndex < 0) {
            return null;
        }
        return queue.get(currentPlayableIndex);
    }

    public int getCurrentPlayableIndex() {
        return this.currentPlayableIndex;
    }

    public int getCurrentTrackIndexInPlayable() {
        return this.currentTrackIndexInPlayable;
    }

    public PlaybackState getCurrentState() {
        return this.currentState;
    }

    public List<Playable> getCurrentQueue() {
        return new ArrayList<>(queue);
    }

    public void advanceTrack() {
        if (queue.isEmpty()) return;

        Playable currentPlayable = queue.get(currentPlayableIndex);
        List<Track> tracks = currentPlayable.getTracks();
        currentTrackIndexInPlayable++;

        // Se abbiamo finito le canzoni di questo Playable, passa al prossimo Playable della coda
        if (currentTrackIndexInPlayable >= tracks.size()) {
            currentPlayableIndex = currentStrategy.getNextIndex(currentPlayableIndex, queue.size());
            currentTrackIndexInPlayable = 0;
            skipEmptyPlayablesForward();
        }
    }

    public void advancePlayable() {
        if (queue.isEmpty()) return;
        currentPlayableIndex = currentStrategy.getNextIndex(currentPlayableIndex, queue.size());
        currentTrackIndexInPlayable = 0;
        skipEmptyPlayablesForward();
    }

    public void regressPlayable() {
        if (queue.isEmpty()) return;

        if (currentPlayableIndex >= queue.size()) {
            currentPlayableIndex = queue.size() - 1;
        } else {
            currentPlayableIndex--;
        }

        skipEmptyPlayablesBackwardToStart();
    }

    public void regressTrack() {
        if (queue.isEmpty()) return;

        if (currentPlayableIndex >= queue.size()) {
            currentPlayableIndex = queue.size() - 1;
            moveToLastTrackOfCurrentPlayableOrPrevious();
            return;
        }

        if (currentTrackIndexInPlayable > 0) {
            currentTrackIndexInPlayable--;
            return;
        }

        currentPlayableIndex--;
        moveToLastTrackOfCurrentPlayableOrPrevious();
    }

    public void resetQueue() {
        this.currentPlayableIndex = 0;
        this.currentTrackIndexInPlayable = 0;
    }

    private void skipEmptyPlayablesForward() {
        while (currentPlayableIndex < queue.size()
                && queue.get(currentPlayableIndex).getTracks().isEmpty()) {
            int nextIndex = currentStrategy.getNextIndex(currentPlayableIndex, queue.size());
            if (nextIndex <= currentPlayableIndex) {
                currentPlayableIndex = queue.size();
                break;
            }
            currentPlayableIndex = nextIndex;
        }
        currentTrackIndexInPlayable = 0;
    }

    private void moveToLastTrackOfCurrentPlayableOrPrevious() {
        while (currentPlayableIndex >= 0) {
            List<Track> tracks = queue.get(currentPlayableIndex).getTracks();
            if (!tracks.isEmpty()) {
                currentTrackIndexInPlayable = tracks.size() - 1;
                return;
            }
            currentPlayableIndex--;
        }

        currentPlayableIndex = 0;
        currentTrackIndexInPlayable = 0;
    }

    private void skipEmptyPlayablesBackwardToStart() {
        while (currentPlayableIndex >= 0) {
            if (!queue.get(currentPlayableIndex).getTracks().isEmpty()) {
                currentTrackIndexInPlayable = 0;
                return;
            }
            currentPlayableIndex--;
        }

        currentPlayableIndex = 0;
        currentTrackIndexInPlayable = 0;
    }

    // Metodo richiamato dal Controller per cambiare strategia a runtime
    public void setStrategy(PlaybackStrategy newStrategy) {
        if (newStrategy != null) {
            this.currentStrategy = newStrategy;
            System.out.println("[MANAGER] Nuova strategia impostata: " + newStrategy.getClass().getSimpleName());
        }
    }

    /**
     * Carica nel player la lista di elementi Playable e imposta gli indici
     * sulla traccia selezionata all'interno del Playable corrispondente.
     */
    public void selectAndLoadTrack(Track selectedTrack, List<? extends Playable> context) {
        if (context != null && !context.isEmpty() && selectedTrack != null) {
            this.queue.clear();
            this.queue.addAll(context);

            // Cerchiamo il Playable che contiene la traccia selezionata
            for (int i = 0; i < queue.size(); i++) {
                Playable p = queue.get(i);
                List<Track> tracks = p.getTracks();
                int trackIdx = tracks.indexOf(selectedTrack);

                if (trackIdx != -1) {
                    this.currentPlayableIndex = i;
                    this.currentTrackIndexInPlayable = trackIdx;
                    System.out.println("[MANAGER] Caricato Playable " + i + " alla traccia " + trackIdx);
                    return;
                }
            }

            // Fallback se non trovato
            this.currentPlayableIndex = 0;
            this.currentTrackIndexInPlayable = 0;
        }
    }

    // ---- METODI PRATICI DI LOGICA AUDIO REALISTICA ----
    public void triggerRealPlayback() {
        Track current = getCurrentTrack();
        if (current != null) {
            System.out.println("🔊 [AUDIO PLAYER] Avvio riproduzione fisica: " + current.getTitle());
        }
    }

    public void triggerRealStop() {
        System.out.println("🔇 [AUDIO PLAYER] Audio interrotto e resettato.");
    }

    public void triggerRealPause() {
        Track current = getCurrentTrack();
        String title = (current != null) ? current.getTitle() : "Nessuna traccia";
        System.out.println("⏸️ [AUDIO PLAYER] Audio in PAUSA su: " + title);
    }
}
