package it.unisa.java_music_playlist_manager.model;

public class PausedState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        Track currentTrack = context.getCurrentTrack();
        if (currentTrack != null) {
            System.out.println("[STATO: PAUSED] -> Riprendo la riproduzione di: " + currentTrack.getTitle());
            context.changeState(new PlayingState());
        } else {
            System.out.println("[STATO: PAUSED] Impossibile riprendere: la coda è vuota.");
            context.changeState(new StoppedState());
        }
    }

    @Override
    public void stop(PlaybackManager context) {
        System.out.println("[STATO: PAUSED] -> Riproduzione INTERROTTA.");
        context.resetQueue(); // Riporta l'indice a 0
        context.changeState(new StoppedState());
    }

    @Override
    public void next(PlaybackManager context) {
        System.out.println("[STATO: PAUSED] -> Salto alla prossima traccia...");
        context.advanceQueue(); // Sposta avanti l'indice

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            System.out.println("[STATO: PAUSED] Coda agganciata su: " + nextTrack.getTitle());
            // Restiamo in PausedState con la nuova traccia pronta
        } else {
            System.out.println("[STATO: PAUSED] Coda terminata. Spengo il lettore.");
            context.resetQueue();
            context.changeState(new StoppedState());
        }
    }

    @Override
    public void previous(PlaybackManager context) {
        System.out.println("[STATO: PAUSED] -> Richiesta traccia precedente.");

        // Il manager sposta indietro l'indice in sicurezza
        context.regressQueue();

        // Recuperiamo la traccia
        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            System.out.println("[STATO: PAUSED] Coda agganciata su: " + prevTrack.getTitle());
        }
    }
}