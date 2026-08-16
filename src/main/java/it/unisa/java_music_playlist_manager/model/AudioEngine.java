package it.unisa.java_music_playlist_manager.model;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/**
 * Porta (Port in stile Ports &amp; Adapters (Hexagonal Architecture) per l'audio.
 * <p>
 * Il dominio ({@code model} layer dipende da questa interfaccia — MAI da una libreria concreta
 * (es. JavaFX MediaPlayer).
 * L'implementazione concreta (es. {@code JavaFXAudioEngine}) vive nel layer {@code ui}.
 *
 * <h3>Convenzioni tempo</h3>
 * Tutti i valori temporali (seek, currentTime, totalDuration) sono in <b>secondi</b>
 * come {@code double} &mdash; cos&igrave; l'interfaccia &egrave; framework-agnostica.
 *
 * <h3>Listeners</h3>
 * I listener sono callback semplici ({@link Runnable}, {@link Consumer}, {@link DoubleConsumer})
 * invece di JavaFX {@code Observable} &mdash; per mantenere l'interfaccia disaccoppiata.
 */
public interface AudioEngine {

    /**
     * Carica un file audio e (opzionalmente) registra i listener per gli eventi di lifecycle.
     * Implementazioni gi&agrave; caricati devono chiamare:
     * <ul>
     *   <li>{@code onReady} quando la durata totale e tutti i metadati sono noti</li>
     *   <li>{@code onTimeUpdate} ogni volta che avanza la posizione corrente</li>
     *   <li>{@code onEndOfMedia} quando il brano arriva alla fine naturalmente</li>
     * </ul>
     *
     * @param filePath percorso assoluto del file audio
     * @param onReady chiamato quando la durata totale e disponibile
     * @param onTimeUpdate chiamato con currentTime seconds
     * @param onEndOfMedia chiamato quando il brano finisce
     */
    void load(String filePath,
              Runnable onReady,
              DoubleConsumer onTimeUpdate,
              Runnable onEndOfMedia);

    /** Avvia la riproduzione (se c'e' un brano caricato. */
    void play();

    /** Mette in pausa la riproduzione. */
    void pause();

    /** Ferma la riproduzione e resetta la posizione all'inizio. */
    void stop();

    /** Rilascia TUTTE le risorse native e imposta lo stato a DISPOSED. */
    void dispose();

    /**
     * Salta alla posizione indicata in secondi.
     * @param seconds posizione in secondi
     */
    void seek(double seconds);

    /** @param volume valore compreso tra 0.0 (muto) e 1.0 (massimo). */
    void setVolume(double volume);

    /** @return volume corrente (0..1). */
    double getVolume();

    /** @return istante corrente in secondi (0 se non caricato). */
    double getCurrentTimeSeconds();

    /** @return durata totale del brano caricato in secondi (0 se non pronto). */
    double getTotalDurationSeconds();

    /** @return stato corrente dell'engine, vedi {@link AudioState}. */
    AudioState getState();
}
