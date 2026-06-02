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

    public PlaybackState getCurrentState() {
        return this.currentState;
    }

    public List<Track> getCurrentQueue() {
        if (this.currentQueue == null) {
            return new ArrayList<>(); // Ritorna una lista vuota anziché null per evitare crash nella UI
        }
        return this.currentQueue;
    }

    /**
     * Carica nel player la lista di brani corrente (Libreria o Playlist)
     * e imposta l'indice esattamente sulla canzone selezionata dall'utente.
     */
    public void selectAndLoadTrack(Track selectedTrack, List<Track> tracksContext) {
        if (tracksContext != null && !tracksContext.isEmpty() && selectedTrack != null) {
            this.currentQueue = tracksContext;

            // 1. Tentativo standard tramite uguaglianza di riferimento/ID
            this.currentIndex = tracksContext.indexOf(selectedTrack);

            // 2. Fallback di sicurezza: se restituisce -1, cerchiamo per contenuto reale (Titolo e Autore)
            if (this.currentIndex == -1) {
                for (int i = 0; i < tracksContext.size(); i++) {
                    Track t = tracksContext.get(i);
                    if (t.getTitle().equalsIgnoreCase(selectedTrack.getTitle()) &&
                            t.getAuthor().equalsIgnoreCase(selectedTrack.getAuthor())) {
                        this.currentIndex = i;
                        break; // Trovato!
                    }
                }
            }

            // 3. Fallback se non lo trova
            if (this.currentIndex == -1) {
                this.currentIndex = 0;
            }

            System.out.println("[MANAGER] Coda caricata con successo (" + tracksContext.size() + " brani). " +
                    "Brano attivo: " + this.currentQueue.get(this.currentIndex).getTitle() + " [Indice: " + this.currentIndex + "]");
        } else {
            System.out.println("[MANAGER - ERROR] Tentato caricamento di un contesto vuoto o nullo.");
        }
    }



    // --- INTERFACCIA PER I BOTTONI (Metodi delegati allo Stato Corrente) ---

    public void pressPlay() {
        // CONTROLLO DI SICUREZZA: Verifichiamo solo se la coda contiene elementi
        if (currentQueue == null || currentQueue.isEmpty()) {
            System.out.println("[MANAGER - ERROR] Impossibile avviare il player: nessuna canzone caricata nella coda.");
            return;
        }

        // Se l'indice è fuori dai giochi per errore, resettalo al primo brano della coda caricata
        if (currentIndex < 0 || currentIndex >= currentQueue.size()) {
            System.out.println("[MANAGER - WARNING] Indice " + currentIndex + " non valido per la coda attuale. Reset a 0.");
            currentIndex = 0;
        }

        // Se siamo qui, la coda è sicuramente valida e non vuota!
        currentState.play(this);
    }
    public void pressStop() {
        // Sicurezza: se la coda è già vuota, non c'è nulla da interrompere
        if (currentQueue == null || currentQueue.isEmpty()) {
            System.out.println("[MANAGER] Coda vuota, nulla da interrompere.");
            return;
        }
        currentState.stop(this);
    }

    public void pressNext() {
        if (currentQueue == null || currentQueue.isEmpty()) {
            System.out.println("[MANAGER] Coda vuota, impossibile andare al brano successivo.");
            return;
        }
        currentState.next(this);
    }

    public void pressPrevious() {
        if (currentQueue == null || currentQueue.isEmpty()) {
            System.out.println("[MANAGER] Coda vuota, impossibile andare al brano precedente.");
            return;
        }
        currentState.previous(this);
    }


}