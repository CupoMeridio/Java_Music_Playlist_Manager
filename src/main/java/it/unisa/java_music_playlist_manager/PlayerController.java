package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;

/**
 * Controller per la gestione della barra di riproduzione (PlayerBarView.fxml).
 * Estratto da PrimaryViewController per separare la responsabilità del player
 * dalla gestione della vista principale.
 */
public class PlayerController {

    private Runnable onPlayPauseClicked;
    private Runnable onPlayerStateChanged;

    // CONTROLLI BARRA DI RIPRODUZIONE
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Slider progressSlider;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private ImageView albumCoverImageView;
    @FXML
    private Label currentTrackTitle;
    @FXML
    private Label currentTrackDetails;
    @FXML
    private Button shuffleButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button repeatButton;
    @FXML
    private Button volumeButton;
    @FXML
    private Slider volumeSlider;

    /**
     * Imposta il callback per il click sul pulsante Play/Pause.
     * Il PrimaryViewController fornisce la logica di riproduzione.
     */
    public void setOnPlayPauseClicked(Runnable callback) {
        this.onPlayPauseClicked = callback;
    }

    /**
     * Imposta il callback per notificare il cambio di stato del player
     * (traccia precedente/successiva) al PrimaryViewController.
     */
    public void setOnPlayerStateChanged(Runnable callback) {
        this.onPlayerStateChanged = callback;
    }

    // METODO DI INIZIALIZZAZIONE
    @FXML
    public void initialize() {
        // Inizializzazione tempi a zero
        if (currentTimeLabel != null) {
            currentTimeLabel.setText("00:00:00");
        }
        if (totalTimeLabel != null) {
            totalTimeLabel.setText("00:00:00");
        }

        // Inizializzazione metadati brano a vuoto
        if (currentTrackTitle != null) {
            currentTrackTitle.setText("");
        }
        if (currentTrackDetails != null) {
            currentTrackDetails.setText("");
        }
    }

    // GESTORI EVENTI BARRA DI RIPRODUZIONE (PLAYER)
    @FXML
    private void handleShuffleToggle() {
        System.out.println("Player: Toggle riproduzione casuale");
    }

    @FXML
    private void handlePrevAction() {
        System.out.println("Player: Richiesta traccia precedente.");
        // Delega allo stato corrente tramite il manager
        PlaybackManager.getInstance().pressPrevious();
        // Aggiorna i testi a schermo
        updatePlayerUI();
        if (onPlayerStateChanged != null) {
            onPlayerStateChanged.run();
        }
    }

    @FXML
    private void handlePlayPauseAction() {
        if (onPlayPauseClicked != null) {
            onPlayPauseClicked.run();
        }
    }

    @FXML
    private void handleNextAction() {
        System.out.println("Player: Click sul pulsante Traccia Successiva.");
        // Delega allo stato corrente tramite il manager
        PlaybackManager.getInstance().pressNext();
        // Sincronizza l'interfaccia grafica
        updatePlayerUI();
        if (onPlayerStateChanged != null) {
            onPlayerStateChanged.run();
        }
    }

    @FXML
    private void handleNextPlayableAction() {
        System.out.println("Player: Click sul pulsante Salta Intero Elemento.");
        // Delega allo stato corrente tramite il manager
        PlaybackManager.getInstance().pressNextPlayable();
        // Sincronizza l'interfaccia grafica
        updatePlayerUI();
        if (onPlayerStateChanged != null) {
            onPlayerStateChanged.run();
        }
    }

    @FXML
    private void handleRepeatToggle() {
        System.out.println("Player: Toggle ripetizione (ciclo)");
    }

    @FXML
    private void handleVolumeMuteToggle() {
        System.out.println("Player: Muto / Attiva audio");
    }

    /**
     * Sincronizza le Label della barra di riproduzione inferiore
     * e lo stato del bottone Play/Pause con il brano correntemente nel PlaybackManager.
     */
    public void updatePlayerUI() {
        PlaybackManager manager = PlaybackManager.getInstance();
        Track currentTrack = manager.getCurrentTrack();

        if (currentTrack != null) {
            // Aggiorna i testi del player in basso
            if (currentTrackTitle != null) {
                currentTrackTitle.setText(currentTrack.getTitle());
            }
            if (currentTrackDetails != null) {
                currentTrackDetails.setText(currentTrack.getAuthor());
            }
        } else {
            // Se non c'è nessun brano in riproduzione
            if (currentTrackTitle != null) {
                currentTrackTitle.setText("Nessun brano in riproduzione");
            }
            if (currentTrackDetails != null) {
                currentTrackDetails.setText("");
            }
        }

        // GESTIONE CAMBIO DINAMICO DEL TESTO DEL BOTTONE PLAY/PAUSA

        if (playPauseButton != null) {
            // Recuperiamo il nome della classe dello stato attuale (played/paused/stopped)
            String currentStateName = manager.getCurrentState().getClass().getSimpleName();

            // Se lo stato contiene "Play" o "Playing", significa che la musica si sente.
            // Il bottone deve quindi offrire l'azione di fermarsi -> Mostra "Pausa" o "Stop"
            if (currentStateName.toLowerCase().contains("play")) {
                playPauseButton.setText("||");
            } else {
                // Se siamo in StoppedState o PausedState, il brano è fermo.
                // Il bottone deve offrire l'azione di ripartire -> Mostra "Play"
                playPauseButton.setText("▶️");
            }
        }
    }
}