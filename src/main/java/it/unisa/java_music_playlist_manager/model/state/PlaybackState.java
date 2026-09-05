package it.unisa.java_music_playlist_manager.model.state;

import it.unisa.java_music_playlist_manager.model.PlaybackManager;

/**
 * L'interfaccia PlaybackState definisce il contratto per i vari stati di riproduzione.
 * È la componente fondamentale del Pattern State utilizzato nel PlaybackManager.
 * 
 * Ogni stato concreto implementerà la logica specifica per le azioni di riproduzione
 * (play, stop, next, prev) in base alla situazione attuale del lettore.
 */
public interface PlaybackState {
    /**
     * Gestisce l'azione di riproduzione/pausa.
     * @param context Il manager di riproduzione che detiene lo stato.
     */
    void play(PlaybackManager context);

    /**
     * Gestisce l'azione di arresto.
     * @param context Il manager di riproduzione che detiene lo stato.
     */
    void stop(PlaybackManager context);

    /**
     * Gestisce il passaggio alla traccia successiva.
     * @param context Il manager di riproduzione che detiene lo stato.
     */
    void next(PlaybackManager context);

    /**
     * Gestisce il salto all'elemento riproducibile successivo nella coda.
     * @param context Il manager di riproduzione che detiene lo stato.
     */
    void nextPlayable(PlaybackManager context);

    /**
     * Gestisce il ritorno alla traccia precedente.
     * @param context Il manager di riproduzione che detiene lo stato.
     */
    void previous(PlaybackManager context);

    /**
     * Gestisce il ritorno all'elemento riproducibile precedente nella coda.
     * @param context Il manager di riproduzione che detiene lo stato.
     */
    void previousPlayable(PlaybackManager context);
}

