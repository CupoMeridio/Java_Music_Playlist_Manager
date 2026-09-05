package it.unisa.java_music_playlist_manager.services;

import it.unisa.java_music_playlist_manager.model.AudioEngine;
import it.unisa.java_music_playlist_manager.model.AudioState;

import java.io.File;
import java.util.function.DoubleConsumer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Adapter (implementazione concreta di {@link AudioEngine} basato sul modulo JavaFX Media.
 * Vive nel package {@code ui} perché può dipendere da {@code javafx.media}
 * — il layer Model invece non lo fa (Dependency Inversion Principle rispettato).
 * <p>
 * L'adapter traduce:
 * <ul>
 *   <li>tempo: {@code Duration} ↔ {@code double seconds}</li>
 *   <li>stato: {@link MediaPlayer.Status} ↔ {@link AudioState}</li>
 *   <li>property listeners: {@code currentTimeProperty().addListener(...)} ↔ {@link DoubleConsumer}</li>
 *   <li>onEndOfMedia / onReady: ↔ callback {@link Runnable}</li>
 * </ul>
 */
public class JavaFXAudioEngine implements AudioEngine {

    private MediaPlayer player;
    private DoubleConsumer timeUpdateCallback;
    private double volume = 0.5;
    private Timeline fadeTimeline;

    private static AudioState mapStatus(MediaPlayer.Status s) {
        if (s == null) return AudioState.STOPPED;
        return switch (s) {
            case PLAYING -> AudioState.PLAYING;
            case PAUSED -> AudioState.PAUSED;
            // STALLED: sta buffering/attendendo dati; HALTED: errore non recuperabile (ma still "loaded")
            case READY, STALLED, HALTED -> AudioState.READY;
            case STOPPED -> AudioState.STOPPED;
            case DISPOSED, UNKNOWN -> AudioState.DISPOSED;
        };
    }

    private void fadeInVolume() {
        if (player == null) return;
        if (fadeTimeline != null) {
            fadeTimeline.stop();
        }
        double targetVolume = this.volume;
        if (targetVolume <= 0) {
            player.setVolume(0.0);
            return;
        }
        player.setVolume(0.0);
        fadeTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(player.volumeProperty(), 0.0)),
            new KeyFrame(Duration.millis(40), new KeyValue(player.volumeProperty(), targetVolume))
        );
        fadeTimeline.play();
    }

    @Override
    public void load(String filePath,
                     Runnable onReady,
                     DoubleConsumer onTimeUpdate,
                     Runnable onEndOfMedia) {
        // (1) dispose del player precedente (se c'era), per rilasciare handle nativi.
        //     differiamo il dispose sul JavaFX Application Thread per evitare
        //     problemi se siamo dentro a un callback dello stesso player (onEndOfMedia).
        if (fadeTimeline != null) {
            fadeTimeline.stop();
            fadeTimeline = null;
        }
        MediaPlayer oldPlayer = this.player;
        this.player = null;
        this.timeUpdateCallback = onTimeUpdate;
        if (oldPlayer != null) {
            try {
                oldPlayer.setVolume(0.0);
                oldPlayer.stop();
            } catch (Exception ignore) { /* gia' fermo */ }
            Platform.runLater(oldPlayer::dispose);
        }

        if (filePath == null) {
            this.timeUpdateCallback = null;
            return;
        }

        File f = new File(filePath);
        if (!f.exists()) {
            this.timeUpdateCallback = null;
            return;
        }

        try {
            Media media = new Media(f.toURI().toString());
            MediaPlayer mp = new MediaPlayer(media);
            // Imposta subito volume a 0 per evitare il pop legato al valore 1.0 di default in JavaFX
            mp.setVolume(0.0);

            mp.setOnReady(() -> {
                if (onReady != null) {
                    onReady.run();
                }
            });

            mp.currentTimeProperty().addListener((obs, oldD, newD) -> {
                if (timeUpdateCallback != null && newD != null) {
                    timeUpdateCallback.accept(newD.toSeconds());
                }
            });

            mp.setOnEndOfMedia(() -> {
                if (onEndOfMedia != null) {
                    onEndOfMedia.run();
                }
            });

            // Aggiungi fallback su error: salta il brano se non caricabile durante la riproduzione
            mp.setOnError(() -> {
                if (onEndOfMedia != null) {
                    onEndOfMedia.run();
                }
            });

            this.player = mp;
        } catch (Exception e) {
            System.err.println("Impossibile caricare il file audio " + filePath + ": " + e.getMessage());
            this.player = null;
        }
    }

    @Override
    public void play() {
        if (player != null) {
            if (fadeTimeline != null) {
                fadeTimeline.stop();
            }
            player.play();
            fadeInVolume();
        }
    }

    @Override
    public void pause() {
        if (player != null) {
            if (fadeTimeline != null) {
                fadeTimeline.stop();
            }
            player.setVolume(0.0);
            player.pause();
        }
    }

    @Override
    public void stop() {
        if (player != null) {
            if (fadeTimeline != null) {
                fadeTimeline.stop();
            }
            player.setVolume(0.0);
            player.stop();
        }
    }

    @Override
    public void dispose() {
        if (fadeTimeline != null) {
            fadeTimeline.stop();
            fadeTimeline = null;
        }
        if (player != null) {
            MediaPlayer p = player;
            player = null;
            timeUpdateCallback = null;
            try { p.setVolume(0.0); p.stop(); } catch (Exception ignore) {}
            Platform.runLater(p::dispose);
        }
    }

    @Override
    public void seek(double seconds) {
        if (player != null) {
            player.seek(Duration.seconds(seconds));
        }
    }

    @Override
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        if (player != null) {
            if (fadeTimeline != null) {
                fadeTimeline.stop();
            }
            player.setVolume(this.volume);
        }
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public double getCurrentTimeSeconds() {
        if (player == null || player.getCurrentTime() == null) return 0.0;
        return player.getCurrentTime().toSeconds();
    }

    @Override
    public double getTotalDurationSeconds() {
        if (player == null || player.getTotalDuration() == null) return 0.0;
        return player.getTotalDuration().toSeconds();
    }

    @Override
    public AudioState getState() {
        if (player == null) return AudioState.STOPPED;
        return mapStatus(player.getStatus());
    }
}
