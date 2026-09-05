package it.unisa.java_music_playlist_manager.model.state;

import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.Track;

/**
 * Rappresenta lo stato del lettore quando la riproduzione è completamente ferma.
 * In questo stato:
 * - Play: Avvia la riproduzione dalla traccia corrente o dall'inizio della coda.
 * - Stop: Non esegue alcuna azione (già fermo).
 * - Next/Prev: Spostano gli indici della coda senza avviare l'audio.
 */
public class StoppedState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        Track current = context.getCurrentTrack();
        if (current != null) {
            // Passaggio allo stato PlayingState e avvio dell'audio reale
            context.changeState(new PlayingState());
            context.triggerRealPlayback();
        } else {
        }
    }

    @Override
    public void stop(PlaybackManager context) {
    }

    @Override
    public void next(PlaybackManager context) {
        context.advanceTrack(); // Aggiorna solo l'indice della coda
    }
    
    @Override
    public void nextPlayable(PlaybackManager context) {
    }

    @Override
    public void previous(PlaybackManager context) {
    }

    @Override
    public void previousPlayable(PlaybackManager context) {
    }

}
