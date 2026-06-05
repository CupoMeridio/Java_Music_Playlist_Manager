package it.unisa.java_music_playlist_manager.model;

public class PlayingState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Click su Play. Metto in PAUSA.");
        context.changeState(new PausedState());
        context.triggerRealPause();
    }

    @Override
    public void stop(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Click su STOP. Interrompo la musica.");
        context.triggerRealStop();
        context.resetQueue();
        context.changeState(new StoppedState());
    }

    @Override
    public void next(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Salto alla prossima traccia...");
        context.advanceTrack();

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            System.out.println("[STATO: PLAYING] Ora riproduco: " + nextTrack.getTitle());
            context.triggerRealPlayback();
        } else {
            System.out.println("[STATO: PLAYING] Coda terminata. Spengo il lettore.");
            context.triggerRealStop();
            context.resetQueue();
            context.changeState(new StoppedState());
        }
    }

    @Override
    public void nextPlayable(PlaybackManager context) {
        Playable currentPlayable = context.getCurrentPlayable();
        String currentTitle = (currentPlayable != null) ? currentPlayable.getTitle() : "Sconosciuto";
        System.out.println("[STATO: PLAYING] -> SKIP dell'intero elemento: " + currentTitle);

        context.advancePlayable();

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            System.out.println("[STATO: PLAYING] Caricato nuovo blocco. Ora riproduco: " + nextTrack.getTitle());
            context.triggerRealPlayback();
        } else {
            System.out.println("[STATO: PLAYING] Nessun altro elemento in coda. Spengo il lettore.");
            context.triggerRealStop();
            context.resetQueue();
            context.changeState(new StoppedState());
        }
    }

    @Override
    public void previous(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Torno alla traccia precedente.");
        context.regressTrack();

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            System.out.println("[STATO: PLAYING] Ora riproduco: " + prevTrack.getTitle());
            context.triggerRealPlayback();
        }
    }

    @Override
    public void previousPlayable(PlaybackManager context) {
        Playable currentPlayable = context.getCurrentPlayable();
        String currentTitle = (currentPlayable != null) ? currentPlayable.getTitle() : "Sconosciuto";
        System.out.println("[STATO: PLAYING] -> SKIP all'elemento precedente da: " + currentTitle);

        context.regressPlayable();

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            System.out.println("[STATO: PLAYING] Caricato blocco precedente. Ora riproduco: " + prevTrack.getTitle());
            context.triggerRealPlayback();
        }
    }
}
