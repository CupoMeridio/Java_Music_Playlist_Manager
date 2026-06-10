package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.ShuffleStrategy;
import it.unisa.java_music_playlist_manager.model.Observer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Controller per la gestione della barra di riproduzione (PlayerBarView.fxml).
 * Estratto da PrimaryViewController per separare la responsabilità del player
 * dalla gestione della vista principale.
 */
/**
 * PlayerController gestisce l'interfaccia della barra di riproduzione inferiore.
 * Si occupa di visualizzare i metadati del brano corrente, controllare il volume,
 * gestire la barra di avanzamento e inviare comandi al {@link PlaybackManager}.
 * 
 * Ruolo nel progetto:
 * - Implementa {@link Observer} per aggiornare la barra quando il player cambia traccia.
 * - Sincronizza lo stato visivo (Play/Pause, Slider, Timer) con il {@link MediaPlayer} di JavaFX.
 * - Comunica con {@link PrimaryViewController} tramite callback Runnable per mantenere l'UI coerente.
 */
public class PlayerController implements Observer {

    /** Callback eseguita quando viene cliccato Play/Pause */
    private Runnable onPlayPauseClicked;
    
    /** Callback eseguita quando cambia lo stato interno del player (es. skip traccia) */
    private Runnable onPlayerStateChanged;

    /**
     * Metodo del pattern Observer.
     * Chiamato quando il PlaybackManager notifica un cambiamento (es. nuova traccia caricata).
     */
    @Override
    public void update() {
        // Riconfigura i listener sul nuovo oggetto MediaPlayer creato nel manager
        setupMediaPlayerListeners();
        // Aggiorna i testi e lo stato dei pulsanti
        updatePlayerUI();
    }

    // --- Elementi UI iniettati da FXML ---
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
     * @param callback Azione da eseguire.
     */
    public void setOnPlayPauseClicked(Runnable callback) {
        this.onPlayPauseClicked = callback;
    }

    /**
     * Imposta il callback per notificare cambiamenti di stato (skip, stop).
     * @param callback Azione da eseguire.
     */
    public void setOnPlayerStateChanged(Runnable callback) {
        this.onPlayerStateChanged = callback;
    }

    /**
     * Inizializzazione del controller.
     * Configura i listener sugli slider di volume e progresso.
     */
    @FXML
    public void initialize() {
        PlaybackManager.getInstance().attach(this);

        // Reset testi iniziali
        if (currentTimeLabel != null) currentTimeLabel.setText("00:00");
        if (totalTimeLabel != null) totalTimeLabel.setText("00:00");
        if (currentTrackTitle != null) currentTrackTitle.setText("");
        if (currentTrackDetails != null) currentTrackDetails.setText("");

        // Configurazione volume slider (Default 50%)
        if (volumeSlider != null) {
            volumeSlider.setValue(50);
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                MediaPlayer mp = PlaybackManager.getInstance().getMediaPlayer();
                if (mp != null) mp.setVolume(newVal.doubleValue() / 100.0);
            });
        }

        // Configurazione seeking manuale sulla barra di progresso
        if (progressSlider != null) {
            progressSlider.setOnMousePressed(e -> {
                MediaPlayer mp = PlaybackManager.getInstance().getMediaPlayer();
                if (mp != null) mp.pause(); // Pausa temporanea durante il trascinamento
            });
            progressSlider.setOnMouseReleased(e -> {
                MediaPlayer mp = PlaybackManager.getInstance().getMediaPlayer();
                if (mp != null) {
                    mp.seek(Duration.seconds(progressSlider.getValue()));
                    mp.play();
                }
            });
        }
    }

    /**
     * Configura i listener sull'istanza corrente di MediaPlayer.
     * Gestisce l'aggiornamento automatico dei timer e della barra di progresso durante la riproduzione.
     */
    private void setupMediaPlayerListeners() {
        MediaPlayer mp = PlaybackManager.getInstance().getMediaPlayer();
        if (mp == null) return;

        // Sincronizza il volume del nuovo player con lo slider corrente
        if (volumeSlider != null) mp.setVolume(volumeSlider.getValue() / 100.0);

        // Quando il file audio è caricato e pronto
        mp.setOnReady(() -> {
            Duration totalDuration = mp.getTotalDuration();
            if (totalTimeLabel != null) totalTimeLabel.setText(formatTime(totalDuration));
            if (progressSlider != null) progressSlider.setMax(totalDuration.toSeconds());
        });

        // Aggiornamento continuo dei timer (ogni volta che il tempo di riproduzione avanza)
        mp.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (progressSlider != null && !progressSlider.isValueChanging()) {
                progressSlider.setValue(newTime.toSeconds());
            }
            if (currentTimeLabel != null) currentTimeLabel.setText(formatTime(newTime));
        });
    }

    private String formatTime(Duration duration) {
        int seconds = (int) duration.toSeconds();
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    // --- Gestori eventi UI ---

    /**
     * Alterna la strategia di riproduzione tra Sequenziale e Shuffle.
     *
     */
    @FXML
    private void handleShuffleToggle() {
        PlaybackManager manager = PlaybackManager.getInstance();

        // Se lo shuffle è già attivo, lo spegniamo ripristinando la strategia sequenziale
        if (manager.getCurrentStrategy() instanceof ShuffleStrategy) {
            manager.setStrategy(new it.unisa.java_music_playlist_manager.model.SequentialStrategy());
            shuffleButton.setStyle("-fx-text-fill: black;"); // Torna allo stato normale (non selezionato)
        } else {
            // Se non era attivo, attiviamo lo Shuffle
            manager.setStrategy(new ShuffleStrategy());
            shuffleButton.setStyle("-fx-text-fill: #1DB954;"); // selezionato

            //se attivi lo shuffle si spegne il loop
            if (repeatButton != null) {
                repeatButton.setStyle("-fx-text-fill: black;");
            }
        }
    }
    @FXML
    private void handlePrevAction() {
        PlaybackManager.getInstance().pressPrevious();
        updatePlayerUI();
        if (onPlayerStateChanged != null) onPlayerStateChanged.run();
    }

    @FXML
    private void handlePreviousPlayableAction() {
        PlaybackManager.getInstance().pressPreviousPlayable();
        updatePlayerUI();
        if (onPlayerStateChanged != null) onPlayerStateChanged.run();
    }

    @FXML
    private void handlePlayPauseAction() {
        if (onPlayPauseClicked != null) onPlayPauseClicked.run();
    }

    @FXML
    private void handleNextAction() {
        PlaybackManager.getInstance().pressNext();
        updatePlayerUI();
        if (onPlayerStateChanged != null) onPlayerStateChanged.run();
    }

    @FXML
    private void handleNextPlayableAction() {
        PlaybackManager.getInstance().pressNextPlayable();
        updatePlayerUI();
        if (onPlayerStateChanged != null) onPlayerStateChanged.run();
    }

    /**
     * Alterna la strategia di riproduzione tra Sequenziale e Ripetizione.
     * Utilizza il Pattern Strategy del Modello.
     */
    @FXML
    private void handleRepeatToggle() {
        PlaybackManager manager = PlaybackManager.getInstance();
        if (manager.getCurrentStrategy() instanceof it.unisa.java_music_playlist_manager.model.SequentialStrategy) {
            manager.setStrategy(new it.unisa.java_music_playlist_manager.model.RepeatStrategy());
            repeatButton.setStyle("-fx-text-fill: #1DB954;"); // Verde (Attivo - Ripeti Coda)
        } else if (manager.getCurrentStrategy() instanceof it.unisa.java_music_playlist_manager.model.RepeatStrategy) {
            manager.setStrategy(new it.unisa.java_music_playlist_manager.model.RepeatTrackStrategy());
            repeatButton.setStyle("-fx-text-fill: #FF8C00;"); // Arancione (Attivo - Ripeti Traccia)
        } else {
            manager.setStrategy(new it.unisa.java_music_playlist_manager.model.SequentialStrategy());
            repeatButton.setStyle("-fx-text-fill: black;"); // Default (Disattivo)
        }
    }

    @FXML
    private void handleVolumeMuteToggle() {
        // Implementazione mute
    }

    /**
     * Sincronizza l'interfaccia del player con lo stato attuale del manager.
     * Aggiorna titolo, autore e l'icona del pulsante Play/Pause.
     */
    public void updatePlayerUI() {
        PlaybackManager manager = PlaybackManager.getInstance();
        Track currentTrack = manager.getCurrentTrack();

        if (currentTrack != null) {
            if (currentTrackTitle != null) currentTrackTitle.setText(currentTrack.getTitle());
            if (currentTrackDetails != null) currentTrackDetails.setText(currentTrack.getAuthor());
        } else {
            if (currentTrackTitle != null) currentTrackTitle.setText("Nessun brano in riproduzione");
            if (currentTrackDetails != null) currentTrackDetails.setText("");
        }

        // Cambio icona dinamico basato sullo stato corrente del Pattern State
        if (playPauseButton != null) {
            String stateName = manager.getCurrentState().getClass().getSimpleName();
            // Se siamo in uno stato di riproduzione (PlayingState), mostriamo l'icona Pausa
            playPauseButton.setText(stateName.toLowerCase().contains("play") ? "||" : "▶️");
        }
    }
}
