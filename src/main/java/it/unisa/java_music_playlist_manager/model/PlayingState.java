package it.unisa.java_music_playlist_manager.model;

/**
 * Rappresenta lo stato di riproduzione attiva del lettore.
 * In questo stato, la musica sta scorrendo e i comandi hanno il seguente comportamento:
 * - Play: Mette in pausa la riproduzione.
 * - Stop: Interrompe l'audio e resetta la posizione nella coda.
 * - Next/Prev: Cambiano traccia e avviano immediatamente il nuovo brano.
 */
public class PlayingState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        // Passaggio allo stato PausedState e sospensione dell'audio reale
        context.changeState(new PausedState());
        context.triggerRealPause();
        context.notifyObservers();
    }

    @Override
    public void stop(PlaybackManager context) {
        // Interruzione totale dell'audio, reset degli indici e passaggio allo stato StoppedState
        context.triggerRealStop();
        context.resetQueue();
        context.changeState(new StoppedState());
        context.notifyObservers(); 
    }

    @Override
    public void next(PlaybackManager context) {
        context.advanceTrack();

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            context.triggerRealPlayback(); // Avvio immediato della nuova traccia
        } else {
            // Se la coda è terminata, il lettore si ferma
            context.triggerRealStop();
            context.resetQueue();
            context.changeState(new StoppedState());
            context.notifyObservers();
        }
    }

    @Override
    public void nextPlayable(PlaybackManager context) {
        context.advancePlayable();

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            context.triggerRealPlayback();
        } else {
            context.triggerRealStop();
            context.resetQueue();
            context.changeState(new StoppedState());
            context.notifyObservers();
        }
    }

    @Override
    public void previous(PlaybackManager context) {
        context.regressTrack();

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            context.triggerRealPlayback();
        }
    }

    @Override
    public void previousPlayable(PlaybackManager context) {
        context.regressPlayable();

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            context.triggerRealPlayback();
        }
    }
}
