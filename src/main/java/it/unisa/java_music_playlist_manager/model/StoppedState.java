package it.unisa.java_music_playlist_manager.model;

public class StoppedState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        Track current = context.getCurrentTrack();
        if (current != null) {
            System.out.println("[STATO: STOPPED] -> Avvio riproduzione iniziale.");
            context.changeState(new PlayingState());
            context.triggerRealPlayback();
        } else {
            System.out.println("[STATO: STOPPED] Coda vuota, impossibile fare Play.");
        }
    }

    @Override
    public void stop(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Già stoppato.");
    }

    @Override
    public void next(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] -> Sposto indice avanti (in stop)...");
        context.advanceTrack();
    }
    @Override
    // Bloccato: in STOP non si salta la playlist
    public void nextPlayable(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Azione ignorata: impossibile saltare il blocco mentre il lettore è spento.");
        // Non chiamiamo context.advancePlayable(), non facciamo nulla.
    }

    @Override
    // Bloccato: in STOP non si torna indietro
    public void previous(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Azione ignorata: impossibile fare 'Previous' mentre il lettore è spento.");
        // Non chiamiamo context.regressTrack(), non facciamo nulla.
    }

    @Override
    public void previousPlayable(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Azione ignorata: impossibile saltare al blocco precedente mentre il lettore è spento.");
    }

}
