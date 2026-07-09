package it.unisa.java_music_playlist_manager.model;

/**
 * Rappresenta lo stato di pausa del lettore.
 * In questo stato, la riproduzione è sospesa e i comandi hanno il seguente comportamento:
 * - Play: Riprende la riproduzione dal punto in cui era stata interrotta.
 * - Stop: Interrompe definitivamente l'audio e resetta la coda.
 * - Next/Prev: Cambiano l'indice della traccia corrente nella coda, ma il lettore rimane in pausa.
 */
public class PausedState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        Track currentTrack = context.getCurrentTrack();
        if (currentTrack != null) {
            // Torna allo stato PlayingState e riavvia l'audio reale
            context.changeState(new PlayingState());
            context.triggerRealPlayback();
            context.notifyObservers();
        } else {
            // Se per qualche motivo la traccia non è più valida, torna in STOP
            context.changeState(new StoppedState());
            context.notifyObservers();
        }
    }

    @Override
    public void stop(PlaybackManager context) {
        context.triggerRealStop();
        context.resetQueue();
        context.changeState(new StoppedState());
        context.notifyObservers();
    }

    @Override
    public void next(PlaybackManager context) {
        context.advanceTrack(); // Sposta l'indice in avanti senza avviare l'audio

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            context.notifyObservers();
        } else {
            // Se la coda finisce, il lettore si resetta e passa in STOP
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
            context.notifyObservers();
        } else {
            context.resetQueue();
            context.changeState(new StoppedState());
            context.notifyObservers();
        }
    }

    @Override
    public void previous(PlaybackManager context) {
        context.regressTrack(); // Sposta l'indice all'indietro senza avviare l'audio

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            context.notifyObservers();
        }
    }

    @Override
    public void previousPlayable(PlaybackManager context) {
        context.regressPlayable();

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            context.notifyObservers(); 
        }
    }
}
