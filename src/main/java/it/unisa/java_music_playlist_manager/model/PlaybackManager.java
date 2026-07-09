package it.unisa.java_music_playlist_manager.model;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * La classe PlaybackManager è il cuore del sistema di riproduzione audio.
 * Coordina la logica di riproduzione, la gestione della coda e l'integrazione con il player audio reale (MediaPlayer).
 * 
 * Pattern utilizzati:
 * - Singleton: Garantisce un unico punto di controllo per la riproduzione in tutta l'applicazione.
 * - State: Delega la logica dei comandi (play, stop, next, prev) a classi di stato specifiche 
 *   (PlayingState, PausedState, StoppedState) per gestire comportamenti diversi in base al contesto.
 * - Strategy: Utilizza diverse strategie di riproduzione (SequentialStrategy, RepeatStrategy)
 *   per determinare l'ordine di avanzamento nella coda.
 * - Observer (Subject): Notifica gli osservatori (UI) quando cambia la traccia corrente o lo stato del player.
 * - Composite: Gestisce una coda di elementi {@link Playable}, che possono essere singole tracce o intere playlist.
 */
public class PlaybackManager implements Subject {
    
    /** Istanza unica del manager (Pattern Singleton) */
    private static PlaybackManager instance;

    /** Lista di osservatori per aggiornamenti in tempo reale (Pattern Observer) */
    private final List<Observer> observers = new ArrayList<>();

    /** Stato corrente della riproduzione (Pattern State) */
    private PlaybackState currentState;

    /** Riferimento al player audio di JavaFX */
    private MediaPlayer mediaPlayer;
    
    /** Percorso dell'ultimo file riprodotto, usato per ottimizzare i caricamenti */
    private String lastPlayedFilePath;
    
    /** Flag per disabilitare l'audio reale (utile per i test unitari) */
    private boolean audioEnabled = true;

    /** Flag che indica se la prossima chiamata a triggerRealPlayback è una ripresa dalla pausa */
    private boolean resumingFromPause = false;

    /** Coda di riproduzione contenente elementi Playable (Pattern Composite) */
    private final List<Playable> queue = new ArrayList<>();
    
    /** Playlist attualmente in riproduzione il cui contatore è già stato incrementato */
    private Playlist currentPlaylistCounted;
    
    /** Indice dell'elemento Playable attualmente selezionato nella coda */
    private int currentPlayableIndex = 0;
    
    /** Indice della traccia corrente all'interno del Playable selezionato (se è una Playlist) */
    private int currentTrackIndexInPlayable = 0;

    /** Strategia di navigazione della coda (Pattern Strategy) */
    private PlaybackStrategy currentStrategy;

    /**
     * Costruttore privato. Inizializza il manager nello stato fermo e con strategia sequenziale.
     */
    private PlaybackManager() {
        this.currentState = new StoppedState();
        this.currentStrategy = new SequentialStrategy();
    }

    /**
     * Punto di accesso al Singleton.
     * 
     * @return L'unica istanza di PlaybackManager.
     */
    public static synchronized PlaybackManager getInstance() {
        if (instance == null) {
            instance = new PlaybackManager();
        }
        return instance;
    }

    // ---- GESTIONE DELLO STATO (Pattern State - Deleghe) ----

    /**
     * Cambia lo stato corrente del manager.
     * 
     * @param state Il nuovo stato da assumere.
     */
    public void changeState(PlaybackState state) {
        this.currentState = state;
    }

    /**
     * Esegue l'azione "Play" delegandola allo stato corrente.
     */
    public void pressPlay() {
        if (queue.isEmpty()) {
            return;
        }
        currentState.play(this);
    }

    /**
     * Esegue l'azione "Stop" delegandola allo stato corrente.
     */
    public void pressStop() {
        if (queue.isEmpty()) return;
        currentState.stop(this);
    }

    /**
     * Salta alla traccia successiva delegando allo stato corrente.
     */
    public void pressNext() {
        if (queue.isEmpty()) return;
        currentState.next(this);
    }

    /**
     * Salta all'intero elemento Playable successivo delegando allo stato corrente.
     */
    public void pressNextPlayable() {
        if (queue.isEmpty()) return;
        currentState.nextPlayable(this);
    }

    /**
     * Torna alla traccia precedente delegando allo stato corrente.
     */
    public void pressPrevious() {
        if (queue.isEmpty()) return;
        currentState.previous(this);
    }

    /**
     * Torna all'elemento Playable precedente delegando allo stato corrente.
     */
    public void pressPreviousPlayable() {
        if (queue.isEmpty()) return;
        currentState.previousPlayable(this);
    }

    // ---- GESTIONE DELLA CODA (Pattern Composite) ----

    /**
     * Aggiunge un elemento alla fine della coda.
     * 
     * @param playable L'elemento da aggiungere.
     */
    public void addToQueue(Playable playable) {
        if (playable == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un elemento nullo alla coda.");
        }
        queue.add(playable);
        // Se è il primo elemento, resetta gli indici per puntare all'inizio
        if (queue.size() == 1) {
            resetQueue();
            skipEmptyPlayablesForward();
        }
    }

    /**
     * Rimuove un elemento dalla coda in base all'indice.
     * Gestisce il ricalcolo degli indici se viene rimosso l'elemento in riproduzione.
     * 
     * @param index L'indice dell'elemento da rimuovere.
     */
    public void removeFromQueue(int index) {
        if (index < 0 || index >= queue.size()) return;

        boolean removingCurrent = (index == currentPlayableIndex);

        queue.remove(index);

        if (queue.isEmpty()) {
            triggerRealStop();
            resetQueue();
            changeState(new StoppedState());
        } else if (removingCurrent) {
            // Se rimuoviamo ciò che stiamo ascoltando, passiamo all'elemento successivo disponibile
            currentTrackIndexInPlayable = 0;
            currentPlaylistCounted = null;
            
            if (currentPlayableIndex >= queue.size()) {
                currentPlayableIndex = 0;
            }
            
            skipEmptyPlayablesForward();

            // Aggiorna la riproduzione se eravamo attivi
            if (currentState instanceof PlayingState) {
                if (getCurrentTrack() != null) {
                    triggerRealPlayback();
                } else {
                    triggerRealStop();
                    changeState(new StoppedState());
                }
            }
        } else if (index < currentPlayableIndex) {
            // Se rimuoviamo un elemento precedente, scaliamo l'indice per mantenere il puntamento corretto
            currentPlayableIndex--;
        }
        
    }

    /**
     * Sostituisce l'intera coda con una nuova lista di elementi.
     * 
     * @param newItems La nuova lista di elementi Playable.
     */
    public void setQueue(List<? extends Playable> newItems) {
        if (newItems != null) {
            this.queue.clear();
            this.queue.addAll(newItems);
            this.currentPlayableIndex = 0;
            this.currentTrackIndexInPlayable = 0;
            this.currentPlaylistCounted = null;
            skipEmptyPlayablesForward();
        }
    }

    /**
     * Restituisce la traccia {@link Track} attualmente selezionata per la riproduzione.
     * 
     * @return La traccia corrente, o null se la coda è vuota o l'indice non è valido.
     */
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

    /**
     * Restituisce l'elemento {@link Playable} (Playlist o Track) correntemente selezionato nella coda.
     */
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

    // ---- LOGICA DI NAVIGAZIONE INTERNA ----

    /**
     * Avanza alla traccia successiva. Se il Playable corrente è terminato, 
     * passa al Playable successivo secondo la strategia impostata.
     */
    public void advanceTrack() {
        if (queue.isEmpty() || currentPlayableIndex >= queue.size() || currentPlayableIndex < 0) return;

        Playable currentPlayable = queue.get(currentPlayableIndex);
        List<Track> tracks = currentPlayable.getTracks();
        currentTrackIndexInPlayable = currentStrategy.getNextTrackIndex(currentTrackIndexInPlayable, tracks.size());

        // Se abbiamo esaurito le tracce nel Playable corrente, chiediamo alla strategia l'indice del prossimo Playable
        if (currentTrackIndexInPlayable >= tracks.size()) {
            currentPlayableIndex = currentStrategy.getNextIndex(currentPlayableIndex, queue.size());
            currentTrackIndexInPlayable = 0;
            skipEmptyPlayablesForward();
        }
    }

    /**
     * Avanza direttamente al prossimo elemento Playable della coda.
     */
    public void advancePlayable() {
        if (queue.isEmpty() || currentPlayableIndex >= queue.size() || currentPlayableIndex < 0) return;
        currentPlayableIndex = currentStrategy.getNextIndex(currentPlayableIndex, queue.size());
        currentTrackIndexInPlayable = 0;
        currentPlaylistCounted = null;
        skipEmptyPlayablesForward();
    }

    /**
     * Torna all'elemento Playable precedente.
     */
    public void regressPlayable() {
        if (queue.isEmpty()) return;

        if (currentPlayableIndex >= queue.size()) {
            currentPlayableIndex = queue.size() - 1;
        } else {
            currentPlayableIndex--;
        }

        skipEmptyPlayablesBackwardToStart();
    }

    /**
     * Torna alla traccia precedente. Se siamo all'inizio di un Playable, 
     * torna all'ultima traccia del Playable precedente.
     */
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

    /**
     * Resetta gli indici della coda alla posizione iniziale.
     */
    public void resetQueue() {
        this.currentPlayableIndex = 0;
        this.currentTrackIndexInPlayable = 0;
        this.currentPlaylistCounted = null;
        this.resumingFromPause = false;
        lastPlayedFilePath = null;
    }

    /**
     * Salta eventuali elementi Playable vuoti (es. playlist senza tracce) andando in avanti.
     */
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

    /**
     * Posiziona l'indice sull'ultima traccia dell'elemento corrente o cerca all'indietro.
     */
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

    /**
     * Salta elementi vuoti tornando all'indietro fino a trovarne uno valido o arrivare all'inizio.
     */
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

    /**
     * Imposta una nuova strategia di riproduzione (Pattern Strategy).
     * 
     * @param newStrategy La strategia (es. Sequential, Repeat).
     */
    public void setStrategy(PlaybackStrategy newStrategy) {
        if (newStrategy != null) {
            this.currentStrategy = newStrategy;
        }
    }

    public PlaybackStrategy getCurrentStrategy() {
        return this.currentStrategy;
    }

    /**
     * Avvia immediatamente la riproduzione di un singolo elemento Playable (svuotando la coda precedente).
     * 
     * @param playable L'elemento da riprodurre.
     * @param shuffle (Non ancora implementato completamente nella logica di coda).
     */
    public void play(Playable playable, boolean shuffle) {
        if (playable != null) {
            this.queue.clear();
            this.queue.add(playable);
            this.currentPlayableIndex = 0;
            this.currentTrackIndexInPlayable = 0;
            this.currentPlaylistCounted = null;
            skipEmptyPlayablesForward();
            pressPlay();
        }
    }

    /**
     * Carica un contesto di riproduzione e seleziona una traccia specifica al suo interno.
     * 
     * @param selectedTrack La traccia da cui iniziare.
     * @param context La lista di elementi Playable che comporranno la coda.
     */
    public void selectAndLoadTrack(Track selectedTrack, List<? extends Playable> context) {
        if (context != null && !context.isEmpty() && selectedTrack != null) {
            this.queue.clear();
            this.queue.addAll(context);
            this.currentPlayableIndex = 0;
            this.currentTrackIndexInPlayable = 0;
            this.currentPlaylistCounted = null;

            // Cerchiamo il Playable che contiene la traccia selezionata per impostare gli indici corretti
            for (int i = 0; i < queue.size(); i++) {
                Playable p = queue.get(i);
                List<Track> tracks = p.getTracks();
                int trackIdx = tracks.indexOf(selectedTrack);

                if (trackIdx != -1) {
                    this.currentPlayableIndex = i;
                    this.currentTrackIndexInPlayable = trackIdx;
                    return;
                }
            }

            this.currentPlayableIndex = 0;
            this.currentTrackIndexInPlayable = 0;
        }
    }

    /**
     * Imposta direttamente gli indici di riproduzione corrente.
     * Usato dall'interfaccia utente per saltare a un punto specifico della coda.
     */
    public void setCurrentIndices(int playableIndex, int trackIndex) {
        if (playableIndex >= 0 && playableIndex < queue.size()) {
            this.currentPlayableIndex = playableIndex;
            List<Track> tracks = queue.get(playableIndex).getTracks();
            if (trackIndex >= 0 && trackIndex < tracks.size()) {
                this.currentTrackIndexInPlayable = trackIndex;
            } else {
                this.currentTrackIndexInPlayable = 0;
            }
        }
    }

    /**
     * Forza l'avvio della riproduzione dal brano attualmente indicizzato nella coda.
     * A differenza di {@link #pressPlay()}, ignora lo stato corrente e avvia sempre
     * una nuova riproduzione dall'inizio del brano, resettando lo slider del tempo a zero.
     *
     * Usato quando l'utente seleziona esplicitamente un brano diverso (es. doppio click sulla coda).
     * Chiamare {@link #setCurrentIndices(int, int)} prima di questo metodo per posizionare la coda.
     */
    public void forcePlayCurrent() {
        if (queue.isEmpty()) return;
        // Ferma l'audio corrente, forza lo stato su Playing e avvia la nuova traccia dall'inizio.
        triggerRealStop();
        // Forza il ricaricamento del MediaPlayer per garantire il reset completo (slider a 0 e avvio pulito)
        lastPlayedFilePath = null;
        changeState(new PlayingState());
        triggerRealPlayback();
    }

    private void countCurrentPlaylistIfPresent() {
        Playable currentPlayable = getCurrentPlayable();
        if (currentPlayable instanceof Playlist playlist
                && currentPlaylistCounted != playlist) {
            playlist.incrementPlayCount();
            currentPlaylistCounted = playlist;
            
            // Sincronizza con l'istanza canonica nella Library (se è una copia creata da Jackson)
            for (Playlist p : Library.getInstance().getPlaylists()) {
                if (p.equals(playlist) && p != playlist) {
                    p.incrementPlayCount();
                    break;
                }
            }
        }
    }

    private void incrementTrackPlayCount(Track current) {
        current.incrementPlayCount();
        // Sincronizza con l'istanza canonica nella Library
        for (Track t : Library.getInstance().getTracks()) {
            if (t.equals(current) && t != current) {
                t.incrementPlayCount();
                break;
            }
        }
    }

    // ---- LOGICA DI INTERAZIONE CON IL MEDIA PLAYER REALE ----

    /**
     * Gestisce l'avvio o la ripresa effettiva dell'audio tramite MediaPlayer.
     * Si occupa di caricare il file, gestire i loop e le riprese dalla pausa.
     */
    public void triggerRealPlayback() {
        Track current = getCurrentTrack();
        if (current == null || current.getFilePath() == null) {
            return;
        }

        if (!audioEnabled) {
            // --- GESTIONE ANALYTICS (modalità test, senza audio reale)
            if (!resumingFromPause) {
                countCurrentPlaylistIfPresent();
                incrementTrackPlayCount(current);
            }
            resumingFromPause = false;
            notifyObservers();
            return;
        }

        String filePath = current.getFilePath();

        // Caso 1: ripresa dalla pausa sullo stesso file → NON incrementare il contatore
        if (resumingFromPause && mediaPlayer != null && filePath.equals(lastPlayedFilePath)) {
            resumingFromPause = false;
            mediaPlayer.play();
            return;
        }

        // Caso 2: nuovo ascolto (nuovo file, loop, skip, onEndOfMedia) → incrementa il contatore
        resumingFromPause = false;
        countCurrentPlaylistIfPresent();
        incrementTrackPlayCount(current);

        // Fermiamo il player precedente (se esiste) prima di crearne uno nuovo.
        // Salva il riferimento al vecchio player per il dispose differito:
        // il dispose dall'interno di un callback onEndOfMedia dello stesso MediaPlayer
        // può causare problemi in JavaFX, quindi lo differiamo.
        MediaPlayer oldPlayer = mediaPlayer;
        mediaPlayer = null;
        if (oldPlayer != null) {
            oldPlayer.stop();
            javafx.application.Platform.runLater(oldPlayer::dispose);
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                notifyObservers();
                pressNext(); // Salta alla prossima se il file manca
                return;
            }
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            lastPlayedFilePath = filePath;

            // Al termine della canzone, avanziamo automaticamente (delega allo stato)
            mediaPlayer.setOnEndOfMedia(() -> {
                pressNext();
            });

            mediaPlayer.play();
        } catch (Exception e) {
            pressNext();
        }
        // Notifica SEMPRE gli osservatori dopo l'incremento del contatore,
        // indipendentemente dal successo della creazione del MediaPlayer.
        notifyObservers();
    }

    /**
     * Ferma fisicamente l'audio.
     */
    public void triggerRealStop() {
        if (!audioEnabled) return;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * Mette fisicamente l'audio in pausa.
     */
    public void triggerRealPause() {
        resumingFromPause = true;
        if (!audioEnabled) return;
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    // ---- METODI DI SUPPORTO E OBSERVER ----

    public void setAudioEnabled(boolean enabled) {
        this.audioEnabled = enabled;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
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
        for (Observer o : observers) {
            o.update();
        }
    }
}
