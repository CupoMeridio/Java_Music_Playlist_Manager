package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.ShuffleStrategy;
import it.unisa.java_music_playlist_manager.model.Observer;
import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.StoppedState;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        checkCurrentTrackValidity();
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
    private StackPane coverContainer;
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

    // --- Vista copertina ---
    /** Vista standard: ImageView quadrata */
    private ImageView albumCoverImageView;
    /** Vista vinile: cerchio che ruota */
    private Circle vinylCircle;
    /** Animazione rotazione vinile */
    private RotateTransition vinylRotation;
    /** true = vista vinile attiva, false = vista standard */
    private boolean vinylViewActive = false;

    /** Dimensione fissa del widget copertina */
    private static final double COVER_SIZE = 50.0;

    /**
     * per l'undo:
     * Se l'utente annulla l'inserimento del brano attualmente in riproduzione,
     * il player deve fermarsi per evitare di riprodurre un file rimosso.
     *
     * Gestisce due casi:
     * 1) Il brano è stato rimosso dalla Library (undo di AddTrackCommand)
     * 2) Il brano è stato rimosso dalla playlist in coda (undo di AddElementToPlaylistCommand),
     *    per cui getCurrentTrack() torna null ma il MediaPlayer continua a suonare.
     */
    private void checkCurrentTrackValidity() {
        PlaybackManager manager = PlaybackManager.getInstance();
        Track currentTrack = manager.getCurrentTrack();

        boolean needsStop = false;

        if (currentTrack != null && !Library.getInstance().getTracks().contains(currentTrack)) {
            // Caso 1: il brano è stato rimosso dalla libreria
            System.out.println("[PLAYER] Il brano in riproduzione è stato rimosso dalla libreria (Undo). Stop forzato.");
            needsStop = true;
        } else if (currentTrack == null && manager.getMediaPlayer() != null
                && manager.getMediaPlayer().getStatus() != MediaPlayer.Status.STOPPED
                && manager.getMediaPlayer().getStatus() != MediaPlayer.Status.DISPOSED) {
            // Caso 2: il brano è stato rimosso dalla playlist in coda (undo di un inserimento),
            // getCurrentTrack() torna null ma il MediaPlayer sta ancora suonando l'audio precedente.
            System.out.println("[PLAYER] Il brano in riproduzione non è più nella coda (Undo). Stop forzato.");
            needsStop = true;
        }

        if (needsStop) {
            // Ferma l'audio reale, resetta lo stato e gli slider
            manager.triggerRealStop();
            manager.changeState(new StoppedState());

            // Reset dello slider e dei timer sulla UI
            javafx.application.Platform.runLater(() -> {
                if (progressSlider != null) {
                    progressSlider.setValue(0);
                    progressSlider.setMax(0);
                }
                if (currentTimeLabel != null) currentTimeLabel.setText("00:00");
                if (totalTimeLabel != null) totalTimeLabel.setText("00:00");
            });
        }
    }




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
        Library.getInstance().attach(this);

        // Costruisce le due viste copertina e mostra quella standard
        buildCoverViews();
        showStandardView();

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
                if (mp != null) mp.pause();
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
     * Costruisce le due viste copertina (standard e vinile) e le aggiunge al container.
     * Entrambe vengono create una sola volta e poi mostrate/nascoste al click.
     */
    private void buildCoverViews() {
        if (coverContainer == null) return;

        // --- Vista standard: ImageView quadrata con bordo ---
        albumCoverImageView = new ImageView();
        albumCoverImageView.setFitWidth(COVER_SIZE);
        albumCoverImageView.setFitHeight(COVER_SIZE);
        albumCoverImageView.setPreserveRatio(true);
        albumCoverImageView.setPickOnBounds(true);
        albumCoverImageView.setStyle("-fx-border-color: #999999; -fx-border-width: 1;");

        // --- Vista vinile: cerchio con solchi disegnati via Canvas ---
        vinylCircle = new Circle(COVER_SIZE / 2);
        drawVinyl(null); // disegna il disco senza copertina inizialmente

        // Animazione rotazione continua (1 giro ogni 3 secondi)
        vinylRotation = new RotateTransition(Duration.seconds(3), vinylCircle);
        vinylRotation.setByAngle(360);
        vinylRotation.setCycleCount(RotateTransition.INDEFINITE);
        vinylRotation.setInterpolator(javafx.animation.Interpolator.LINEAR);

        coverContainer.getChildren().addAll(albumCoverImageView, vinylCircle);
    }

    /**
     * Disegna la texture del disco vinile sul cerchio.
     * La copertina (se presente) viene usata come riempimento circolare centrale.
     * I solchi sono cerchi concentrici grigi su sfondo nero.
     *
     * @param cover immagine copertina, o null per usare solo il nero
     */
    private void drawVinyl(Image cover) {
        if (vinylCircle == null) return;

        double r = COVER_SIZE / 2;

        // Sfondo: canvas nero su cui disegniamo i solchi
        Canvas canvas = new Canvas(COVER_SIZE, COVER_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Disco nero di base
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillOval(0, 0, COVER_SIZE, COVER_SIZE);

        // Solchi concentrici (cerchi grigi a distanze regolari)
        gc.setStroke(Color.web("#3a3a3a"));
        gc.setLineWidth(0.5);
        for (double groove = r * 0.45; groove < r * 0.92; groove += 3.0) {
            gc.strokeOval(r - groove, r - groove, groove * 2, groove * 2);
        }

        // Etichetta centrale: copertina circolare o cerchio colorato
        double labelRadius = r * 0.38;
        if (cover != null) {
            // Ritaglia la copertina in un cerchio centrale
            gc.save();
            gc.beginPath();
            gc.arc(r, r, labelRadius, labelRadius, 0, 360);
            gc.closePath();
            gc.clip();
            gc.drawImage(cover,
                r - labelRadius, r - labelRadius,
                labelRadius * 2, labelRadius * 2);
            gc.restore();
        } else {
            // Etichetta colorata generica
            gc.setFill(Color.web("#cc3333"));
            gc.fillOval(r - labelRadius, r - labelRadius, labelRadius * 2, labelRadius * 2);
        }

        // Buco centrale del vinile
        double holeRadius = r * 0.06;
        gc.setFill(Color.web("#111111"));
        gc.fillOval(r - holeRadius, r - holeRadius, holeRadius * 2, holeRadius * 2);

        // Converti il canvas in immagine e usala come fill del cerchio
        javafx.scene.SnapshotParameters sp = new javafx.scene.SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        Image vinylImage = canvas.snapshot(sp, null);
        vinylCircle.setFill(new ImagePattern(vinylImage));
    }

    /** Mostra la vista standard (ImageView) e nasconde il vinile. */
    private void showStandardView() {
        if (albumCoverImageView != null) albumCoverImageView.setVisible(true);
        if (vinylCircle != null) vinylCircle.setVisible(false);
        if (vinylRotation != null) vinylRotation.pause();
        vinylViewActive = false;
    }

    /** Mostra la vista vinile e nasconde la standard. Avvia la rotazione se il player è attivo. */
    private void showVinylView() {
        if (albumCoverImageView != null) albumCoverImageView.setVisible(false);
        if (vinylCircle != null) vinylCircle.setVisible(true);
        vinylViewActive = true;
        syncVinylRotation();
    }

    /** Avvia o ferma la rotazione del vinile in base allo stato del player. */
    private void syncVinylRotation() {
        if (!vinylViewActive || vinylRotation == null) return;
        String state = PlaybackManager.getInstance().getCurrentState().getClass().getSimpleName().toLowerCase();
        if (state.contains("play")) {
            vinylRotation.play();
        } else {
            vinylRotation.pause();
        }
    }

    /** Gestisce il click sul container copertina: alterna tra le due viste. */
    @FXML
    private void handleCoverClick() {
        if (vinylViewActive) {
            showStandardView();
        } else {
            showVinylView();
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
        MediaPlayer mp = PlaybackManager.getInstance().getMediaPlayer();
        if (mp != null) {
            mp.setMute(!mp.isMute());
            volumeButton.setStyle(mp.isMute() ? "-fx-text-fill: red;" : "-fx-text-fill: black;");
        }
    }

    /**
     * Sincronizza l'interfaccia del player con lo stato attuale del manager.
     * Aggiorna titolo, autore, copertina e l'icona del pulsante Play/Pause.
     */
    public void updatePlayerUI() {
        PlaybackManager manager = PlaybackManager.getInstance();
        Track currentTrack = manager.getCurrentTrack();

        if (currentTrack != null) {
            if (currentTrackTitle != null) currentTrackTitle.setText(currentTrack.getTitle());
            if (currentTrackDetails != null) currentTrackDetails.setText(currentTrack.getAuthor());
            updateAlbumCover(currentTrack.getFilePath());
        } else {
            if (currentTrackTitle != null) currentTrackTitle.setText("Nessun brano in riproduzione");
            if (currentTrackDetails != null) currentTrackDetails.setText("");
            if (albumCoverImageView != null) albumCoverImageView.setImage(null);
            drawVinyl(null);
        }

        // Cambio icona dinamico basato sullo stato corrente del Pattern State
        if (playPauseButton != null) {
            String stateName = manager.getCurrentState().getClass().getSimpleName();
            playPauseButton.setText(stateName.toLowerCase().contains("play") ? "⏸" : "▶");
        }

        // Sincronizza la rotazione del vinile con lo stato play/pausa
        syncVinylRotation();
    }

    /**
     * Estrae la copertina embedded nel file audio tramite jaudiotagger.
     * Aggiorna sia la vista standard che quella vinile.
     */
    private void updateAlbumCover(String filePath) {
        if (filePath == null) return;

        new Thread(() -> {
            Image cover = null;
            try {
                Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
                AudioFile audioFile = AudioFileIO.read(new File(filePath));
                org.jaudiotagger.tag.Tag tag = audioFile.getTag();
                if (tag != null) {
                    Artwork artwork = tag.getFirstArtwork();
                    if (artwork != null) {
                        byte[] data = artwork.getBinaryData();
                        if (data != null && data.length > 0) {
                            cover = new Image(new ByteArrayInputStream(data));
                        }
                    }
                }
            } catch (Exception ignored) {}

            final Image finalCover = cover;
            javafx.application.Platform.runLater(() -> {
                // Aggiorna vista standard
                if (albumCoverImageView != null) albumCoverImageView.setImage(finalCover);
                // Ridisegna il vinile con la nuova copertina
                drawVinyl(finalCover);
            });
        }).start();
    }
}
