package it.unisa.java_music_playlist_manager.model;

public class PausedState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        Track currentTrack = context.getCurrentTrack();
        if (currentTrack != null) {
            System.out.println("[STATO: PAUSED] -> Riprendo la musica.");
            context.changeState(new PlayingState());
            context.triggerRealPlayback();
        } else {
            context.changeState(new StoppedState());
        }
    }

    @Override
    public void stop(PlaybackManager context) {
        System.out.println("[STATO: PAUSED] -> Stop premuto dalla pausa. Resetto.");
        context.triggerRealStop();
        context.resetQueue();
        context.changeState(new StoppedState());
    }

    @Override
    public void next(PlaybackManager context) {
        System.out.println("[STATO: PAUSED] -> Salto alla prossima traccia (rimanendo in pausa)...");
        context.advanceTrack();

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            System.out.println("[STATO: PAUSED] Coda agganciata su: " + nextTrack.getTitle());
        } else {
            System.out.println("[STATO: PAUSED] Fine coda raggiunta. Spengo.");
            context.resetQueue();
            context.changeState(new StoppedState());
        }
    }

    @Override
    public void nextPlayable(PlaybackManager context) {
        System.out.println("[STATO: PAUSED] -> Salto l'intero blocco (rimanendo in pausa)...");
        context.advancePlayable();

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            System.out.println("[STATO: PAUSED] Coda agganciata sul nuovo blocco: " + nextTrack.getTitle());
        } else {
            System.out.println("[STATO: PAUSED] Fine coda raggiunta. Spengo.");
            context.resetQueue();
            context.changeState(new StoppedState());
        }
    }

    @Override
    public void previous(PlaybackManager context) {
        System.out.println("[STATO: PAUSED] -> Traccia precedente (rimanendo in pausa)...");
        context.regressTrack();

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            System.out.println("[STATO: PAUSED] Coda agganciata su: " + prevTrack.getTitle());
        }
    }
}