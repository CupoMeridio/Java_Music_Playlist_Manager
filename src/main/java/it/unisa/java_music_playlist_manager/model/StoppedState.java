package it.unisa.java_music_playlist_manager.model;

public class StoppedState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        Track track = context.getCurrentTrack();
        if (track != null) {
            System.out.println("[STATO: STOPPED] -> Avvio riproduzione: " + track.getTitle());
            // Transizione verso lo stato in riproduzione
            context.changeState(new PlayingState());
        } else {
            System.out.println("[STATO: STOPPED] Impossibile riprodurre: la coda è vuota.");
        }
    }

    @Override
    public void stop(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Il lettore è già in stato di STOP.");
    }

    @Override
    public void next(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Il lettore è fermo, non puoi avanzare alla prossima canzone.");
    }

    @Override
    public void previous(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Il lettore è fermo, non puoi tornare indietro.");
    }


}