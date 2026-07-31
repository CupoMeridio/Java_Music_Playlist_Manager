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
        System.out.println("[STATO: PLAYING] -> Click su Play. Metto in PAUSA.");
        // Passaggio allo stato PausedState e sospensione dell'audio reale
        context.changeState(new PausedState());
        context.triggerRealPause();
        context.notifyObservers();
    }

    @Override
    public void stop(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Click su STOP. Interrompo la musica.");
        // Interruzione totale dell'audio, reset degli indici e passaggio allo stato StoppedState
        context.triggerRealStop();
        context.resetQueue();
        context.changeState(new StoppedState());
        context.notifyObservers(); 
    }

    @Override
    public void next(PlaybackManager context) {
        System.out.println("[STATO: PLAYING] -> Salto alla prossima traccia...");
        context.advanceTrack();

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            System.out.println("[STATO: PLAYING] Ora riproduco: " + nextTrack.getTitle());
            context.triggerRealPlayback(); // Avvio immediato della nuova traccia
        } else {
            // Se la coda è terminata, il lettore si ferma
            System.out.println("[STATO: PLAYING] Coda terminata. Spengo il lettore.");
            context.triggerRealStop();
            context.resetQueue();
            context.changeState(new StoppedState());
            context.notifyObservers();
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
            context.notifyObservers();
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
