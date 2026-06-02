package it.unisa.java_music_playlist_manager.model;

public class StoppedState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        // Il PlaybackManager ha già verificato che la coda contiene musica.
        Track track = context.getCurrentTrack();

        System.out.println("[STATO: STOPPED] -> Avvio riproduzione: " + track.getTitle());
        context.changeState(new PlayingState());
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