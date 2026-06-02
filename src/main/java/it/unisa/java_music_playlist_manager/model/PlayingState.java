package it.unisa.java_music_playlist_manager.model;

public class PlayingState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        System.out.println("[PLAYING] -> Musica già in corso. Interpreto il click come PAUSA.");
        // Logica per stoppare momentaneamente l'audio...
        context.changeState(new PausedState()); // Transizione automatica a Pausa!
    }


    @Override
    public void stop(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Riproduzione INTERROTTA.");
        context.resetQueue(); // Riporta l'indice a 0
        context.changeState(new StoppedState());
    }

    @Override
    public void next(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Salto alla prossima traccia...");
        context.advanceQueue(); // Sposta avanti l'indice

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            System.out.println("[STATO: PLAYING] Ora riproduco: " + nextTrack.getTitle());
            // Restiamo in PlayingState ma l'audio è cambiato
        } else {
            System.out.println("[STATO: PLAYING] Coda terminata. Spengo il lettore.");
            context.resetQueue();
            context.changeState(new StoppedState());
        }
    }

    @Override
    public void previous(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Richiesta traccia precedente.");

        // Il manager sposta indietro l'indice
        context.regressQueue();

        // Recuperiamo la traccia (sarà la precedente o la prima se eravamo già all'inizio)
        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            System.out.println("[STATO: PLAYING] Ora riproduco: " + prevTrack.getTitle());
        }
    }

}


