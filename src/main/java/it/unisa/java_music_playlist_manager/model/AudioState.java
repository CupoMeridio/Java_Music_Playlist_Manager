package it.unisa.java_music_playlist_manager.model;

/**
 * Stato possibile dell'audio reale riprodotto dall' {@link AudioEngine}.
 * <p>
 * Enum disaccoppiato da qualsiasi libreria UI (es. MediaPlayer.Status).
 * Serve a mantenere il layer Model puro secondo la Dependency Inversion Principle (DIP):
 * il Model non dipende da JavaFX Media.
 */
public enum AudioState {
    /** Nessun file caricato o player rilasciato. */
    STOPPED,
    /** File caricato e in riproduzione. */
    PLAYING,
    /** File caricato ma temporaneamente in pausa. */
    PAUSED,
    /** File appena caricato e decoder pronto (durata totale disponibile). */
    READY,
    /** Risorse liberate / engine non più disponibile. */
    DISPOSED
}
