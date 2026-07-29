package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.AudioState;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller per la gestione della barra di riproduzione (PlayerBarView.fxml).
 * Separato per isolare la responsabilità del player dalla gestione della vista principale.
 *
 * PlayerController gestisce l'interfaccia della barra di riproduzione
 * inferiore. Si occupa di visualizzare i metadati del brano corrente,
 * controllare il volume, gestire la barra di avanzamento e inviare comandi
 * al {@link PlaybackManager}.
 *
 * Ruolo nel progetto:
 * - Implementa {@link Observer} per aggiornare la barra quando il player
 *   cambia traccia.
 * - Sincronizza lo stato visivo (Play/Pause, Slider, Timer) tramite la
 *   <i>facade API</i> di {@link PlaybackManager} (metodi playAudioDirect,
 *   seekAudio, setAudioVolume, getAudioState ...).
 *   In questo modo il controller NON dipende direttamente da
 *   JavaFX MediaPlayer — disaccoppiamento dal layer Model.
 * - Comunica con {@link PrimaryViewController} tramite callback Runnable per
 *   mantenere l'UI coerente.
 */
public class PlayerController implements Observer {

    /** Callback eseguita quando viene cliccato Play/Pause */
    private Runnable onPlayPauseClicked;

    /**
     * Callback eseguita quando cambia lo stato interno del player (es. skip
     * traccia)
     */
    private Runnable onPlayerStateChanged;

    /** Memoria dell'ultimo volume prima del mute */
    private double previousVolume = 50.0;

    /**
     * Metodo del pattern Observer.
     * Chiamato quando il PlaybackManager notifica un cambiamento (es. nuova traccia
     * caricata).
     */
    @Override
    public void update() {
        checkCurrentTrackValidity();
        // I listener per l'audio (ready, time update) sono registrati una sola
        // volta in initialize() sul PlaybackManager tramite facade API — non
        // serve ri-registrarli ad ogni cambiamento di traccia.
        updatePlayerUI();
    }

    // Elementi UI iniettati da FXML
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
    private Button prevPlayableButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button nextPlayableButton;
    @FXML
    private Button repeatButton;
    @FXML
    private Button volumeButton;
    @FXML
    private Slider volumeSlider;

    // FontIcon iniettati da FXML (fx:id)
    /** Icona play/pause: cambia literale a runtime */
    @FXML
    private FontIcon playPauseIcon;
    /** Icona volume: cambia colore quando mutato */
    @FXML
    private FontIcon volumeIcon;

    // Vista copertina
    /** Vista standard: ImageView quadrata */
    private ImageView albumCoverImageView;
    /** Vista vinile: cerchio che ruota */
    private Circle vinylCircle;
    /** Animazione rotazione vinile */
    private RotateTransition vinylRotation;
    /** true = vista vinile attiva, false = vista standard */
    private boolean vinylViewActive = false;

    /** Dimensione fissa del widget copertina */
    private static final double COVER_SIZE = 62.0;
    private static final double COVER_INSET = 5.0;
    private static final double COVER_CONTENT_SIZE = COVER_SIZE - (COVER_INSET * 2.0);

    /**
     * per l'undo:
     * Se l'utente annulla l'inserimento del brano attualmente in riproduzione,
     * il player deve fermarsi per evitare di riprodurre un file rimosso.
     *
     * Gestisce due casi:
     * 1) Il brano è stato rimosso dalla Library (undo di AddTrackCommand)
     * 2) Il brano è stato rimosso dalla playlist in coda (undo di
     * AddElementToPlaylistCommand),
     * per cui getCurrentTrack() torna null ma il MediaPlayer continua a suonare.
     */
    private void checkCurrentTrackValidity() {
        PlaybackManager manager = PlaybackManager.getInstance();
        Track currentTrack = manager.getCurrentTrack();

        boolean needsStop = false;

        if (currentTrack != null && !Library.getInstance().getTracks().contains(currentTrack)) {
            // Caso 1: il brano è stato rimosso dalla libreria
            System.out
                    .println("[PLAYER] Il brano in riproduzione è stato rimosso dalla libreria (Undo). Stop forzato.");
            needsStop = true;
        } else if (currentTrack == null
                && manager.getAudioState() != AudioState.STOPPED
                && manager.getAudioState() != AudioState.DISPOSED) {
            // Caso 2: il brano è stato rimosso dalla playlist in coda (undo di
            // un inserimento): getCurrentTrack() torna null ma l'audio reale
            // sta ancora riproducendo il brano precedente.
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
                if (currentTimeLabel != null)
                    currentTimeLabel.setText("00:00");
                if (totalTimeLabel != null)
                    totalTimeLabel.setText("00:00");
            });
        }
    }

    /**
     * Imposta il callback per il click sul pulsante Play/Pause.
     * 
     * @param callback Azione da eseguire.
     */
    public void setOnPlayPauseClicked(Runnable callback) {
        this.onPlayPauseClicked = callback;
    }

    /**
     * Imposta il callback per notificare cambiamenti di stato (skip, stop).
     * 
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
        if (currentTimeLabel != null)
            currentTimeLabel.setText("00:00");
        if (totalTimeLabel != null)
            totalTimeLabel.setText("00:00");
        if (currentTrackTitle != null)
            currentTrackTitle.setText("");
        if (currentTrackDetails != null)
            currentTrackDetails.setText("");

        // Configurazione volume slider (Default 50%)
        if (volumeSlider != null) {
            volumeSlider.setValue(50);
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double volume = newVal.doubleValue();
                PlaybackManager.getInstance().setAudioVolume(volume / 100.0);

                // Aggiorna dinamicamente l'icona in base al volume impostato dallo slider
                if (volumeIcon != null) {
                    boolean isMuted = volume == 0;
                    volumeIcon.setIconLiteral(isMuted ? "fas-volume-mute" : "fas-volume-up");
                }
                if (volumeButton != null) {
                    volumeButton.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("muted"), volume == 0);
                }
            });
        }

        // Configurazione seeking manuale sulla barra di progresso
        if (progressSlider != null) {
            // Variabile per ricordare se il player stava suonando prima del drag
            final boolean[] wasPlaying = { false };

            progressSlider.setOnMousePressed(e -> {
                PlaybackManager manager = PlaybackManager.getInstance();
                wasPlaying[0] = (manager.getAudioState() == AudioState.PLAYING);
                manager.pauseAudioDirect();
            });
            progressSlider.setOnMouseReleased(e -> {
                PlaybackManager manager = PlaybackManager.getInstance();
                manager.seekAudio(progressSlider.getValue());
                // Riprende la riproduzione solo se era in play prima del drag
                if (wasPlaying[0]) {
                    manager.playAudioDirect();
                }
            });
        }

        if (shuffleButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(shuffleButton);
        if (prevPlayableButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(prevPlayableButton);
        if (prevButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(prevButton);
        if (playPauseButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(playPauseButton);
        if (nextButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(nextButton);
        if (nextPlayableButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(nextPlayableButton);
        if (repeatButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(repeatButton);
        if (volumeButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(volumeButton);
        if (coverContainer != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(coverContainer);

        // Registra i listener audio sul PlaybackManager per l'intero ciclo di vita del controller.
        PlaybackManager manager = PlaybackManager.getInstance();
        manager.setAudioReadyListener(() -> javafx.application.Platform.runLater(() -> {
            if (volumeSlider != null) {
                manager.setAudioVolume(volumeSlider.getValue() / 100.0);
            }
            double totalSec = manager.getAudioTotalDuration();
            if (totalTimeLabel != null)
                totalTimeLabel.setText(formatTime(totalSec));
            if (progressSlider != null)
                progressSlider.setMax(totalSec);
        }));
        manager.setAudioTimeListener(timeSec -> javafx.application.Platform.runLater(() -> {
            if (progressSlider != null && !progressSlider.isValueChanging()) {
                progressSlider.setValue(timeSec);
            }
            if (currentTimeLabel != null)
                currentTimeLabel.setText(formatTime(timeSec));
        }));
    }

    /**
     * Costruisce le due viste copertina (standard e vinile) e le aggiunge al
     * container.
     * Entrambe vengono create una sola volta e poi mostrate/nascoste al click.
     */
    private void buildCoverViews() {
        if (coverContainer == null)
            return;

        // Vista standard: ImageView quadrata con bordo
        albumCoverImageView = new ImageView();
        albumCoverImageView.setFitWidth(COVER_CONTENT_SIZE);
        albumCoverImageView.setFitHeight(COVER_CONTENT_SIZE);
        albumCoverImageView.setPreserveRatio(true);
        albumCoverImageView.setPickOnBounds(true);

        // Vista vinile: cerchio con solchi disegnati via Canvas
        vinylCircle = new Circle(COVER_CONTENT_SIZE / 2);
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
        if (vinylCircle == null)
            return;

        double r = vinylCircle.getRadius();
        double diameter = r * 2;

        // Sfondo: canvas nero su cui disegniamo i solchi
        Canvas canvas = new Canvas(diameter, diameter);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Disco nero di base
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillOval(0, 0, diameter, diameter);

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
        if (albumCoverImageView != null)
            albumCoverImageView.setVisible(true);
        if (vinylCircle != null)
            vinylCircle.setVisible(false);
        if (vinylRotation != null)
            vinylRotation.pause();
        vinylViewActive = false;
    }

    /**
     * Mostra la vista vinile e nasconde la standard. Avvia la rotazione se il
     * player è attivo.
     */
    private void showVinylView() {
        if (albumCoverImageView != null)
            albumCoverImageView.setVisible(false);
        if (vinylCircle != null)
            vinylCircle.setVisible(true);
        vinylViewActive = true;
        syncVinylRotation();
    }

    /** Avvia o ferma la rotazione del vinile in base allo stato del player. */
    private void syncVinylRotation() {
        if (!vinylViewActive || vinylRotation == null)
            return;
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
     * Formatta un tempo in secondi come {@code mm:ss}.
     * <p>
     * Accetta un {@code double} invece di {@code Duration} per rimanere
     * disaccoppiato da qualsiasi libreria UI (DIP).
     *
     * @param totalSeconds tempo in secondi
     * @return stringa formattata {@code mm:ss}
     */
    private String formatTime(double totalSeconds) {
        int seconds = (int) totalSeconds;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    // Gestori eventi UI

    /**
     * Alterna la strategia di riproduzione tra Sequenziale e Shuffle.
     *
     */
    @FXML
    private void handleShuffleToggle() {
        PlaybackManager manager = PlaybackManager.getInstance();

        // Se lo shuffle è già attivo, lo spegniamo ripristinando la strategia
        // sequenziale
        if (manager.getCurrentStrategy() instanceof ShuffleStrategy) {
            manager.setStrategy(new it.unisa.java_music_playlist_manager.model.SequentialStrategy());
            setButtonActiveState(shuffleButton, false);
        } else {
            // Altrimenti attiviamo la strategia shuffle
            manager.setStrategy(new it.unisa.java_music_playlist_manager.model.ShuffleStrategy());
            setButtonActiveState(shuffleButton, true);

            // se attivi lo shuffle si spegne il loop
            if (repeatButton != null) {
                setButtonActiveState(repeatButton, false);
                repeatButton.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("repeat-track"), false);
            }
        }
    }

    /**
     * Utility: rimuove/aggiunge la pseudoclasse 'active' per lo stile CSS.
     */
    private void setButtonActiveState(Button button, boolean active) {
        if (button == null)
            return;
        button.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("active"), active);
    }

    @FXML
    private void handlePrevAction() {
        PlaybackManager.getInstance().pressPrevious();
        updatePlayerUI();
        if (onPlayerStateChanged != null)
            onPlayerStateChanged.run();
    }

    @FXML
    private void handlePreviousPlayableAction() {
        PlaybackManager.getInstance().pressPreviousPlayable();
        updatePlayerUI();
        if (onPlayerStateChanged != null)
            onPlayerStateChanged.run();
    }

    @FXML
    private void handlePlayPauseAction() {
        if (onPlayPauseClicked != null)
            onPlayPauseClicked.run();
    }

    @FXML
    private void handleNextAction() {
        PlaybackManager.getInstance().pressNext();
        updatePlayerUI();
        if (onPlayerStateChanged != null)
            onPlayerStateChanged.run();
    }

    @FXML
    private void handleNextPlayableAction() {
        PlaybackManager.getInstance().pressNextPlayable();
        updatePlayerUI();
        if (onPlayerStateChanged != null)
            onPlayerStateChanged.run();
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
            setButtonActiveState(repeatButton, true);
            // Si potrebbe usare un'altra pseudoclasse (es. :active-track) per il
            // repeat-track
            repeatButton.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("repeat-track"), false);
        } else if (manager.getCurrentStrategy() instanceof it.unisa.java_music_playlist_manager.model.RepeatStrategy) {
            manager.setStrategy(new it.unisa.java_music_playlist_manager.model.RepeatTrackStrategy());
            setButtonActiveState(repeatButton, true);
            repeatButton.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("repeat-track"), true);
        } else {
            manager.setStrategy(new it.unisa.java_music_playlist_manager.model.SequentialStrategy());
            setButtonActiveState(repeatButton, false);
            repeatButton.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("repeat-track"), false);
        }
    }

    @FXML
    private void handleVolumeMuteToggle() {
        if (volumeSlider == null)
            return;

        if (volumeSlider.getValue() > 0) {
            // Mute: salva il volume attuale e porta lo slider a 0
            previousVolume = volumeSlider.getValue();
            volumeSlider.setValue(0);
        } else {
            // Unmute: ripristina il volume precedente (o 50.0 se era 0)
            volumeSlider.setValue(previousVolume > 0 ? previousVolume : 50.0);
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
            if (currentTrackTitle != null)
                currentTrackTitle.setText(currentTrack.getTitle());
            if (currentTrackDetails != null)
                currentTrackDetails.setText(currentTrack.getAuthor());
            updateAlbumCover(currentTrack.getFilePath());
        } else {
            if (currentTrackTitle != null)
                currentTrackTitle.setText("Nessun brano in riproduzione");
            if (currentTrackDetails != null)
                currentTrackDetails.setText("");
            if (albumCoverImageView != null)
                albumCoverImageView.setImage(null);
            drawVinyl(null);
        }

        // Cambio icona dinamico basato sullo stato corrente del Pattern State
        if (playPauseIcon != null) {
            String stateName = manager.getCurrentState().getClass().getSimpleName();
            playPauseIcon.setIconLiteral(stateName.toLowerCase().contains("play") ? "fas-pause" : "fas-play");
        }

        // Sincronizza la rotazione del vinile con lo stato play/pausa
        syncVinylRotation();
    }

    /**
     * Estrae la copertina embedded nel file audio tramite jaudiotagger.
     * Aggiorna sia la vista standard che quella vinile.
     */
    private void updateAlbumCover(String filePath) {
        if (filePath == null)
            return;

        it.unisa.java_music_playlist_manager.ui.CoverImageService.getInstance().loadCoverAsync(filePath)
                .thenAcceptAsync(image -> {
                    Image finalCover = it.unisa.java_music_playlist_manager.ui.CoverImageService.getInstance()
                            .isDefaultCover(image) ? null : image;
                    if (albumCoverImageView != null)
                        albumCoverImageView.setImage(finalCover);
                    drawVinyl(finalCover);
                }, javafx.application.Platform::runLater);
    }
}
