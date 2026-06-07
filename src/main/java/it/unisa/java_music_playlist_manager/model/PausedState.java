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
            System.out.println("[STATO: PAUSED] -> Riprendo la musica.");
            // Torna allo stato PlayingState e riavvia l'audio reale
            context.changeState(new PlayingState());
            context.triggerRealPlayback();
        } else {
            // Se per qualche motivo la traccia non è più valida, torna in STOP
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
        context.advanceTrack(); // Sposta l'indice in avanti senza avviare l'audio

        Track nextTrack = context.getCurrentTrack();
        if (nextTrack != null) {
            System.out.println("[STATO: PAUSED] Coda agganciata su: " + nextTrack.getTitle());
        } else {
            // Se la coda finisce, il lettore si resetta e passa in STOP
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
        context.regressTrack(); // Sposta l'indice all'indietro senza avviare l'audio

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            System.out.println("[STATO: PAUSED] Coda agganciata su: " + prevTrack.getTitle());
        }
    }

    @Override
    public void previousPlayable(PlaybackManager context) {
        System.out.println("[STATO: PAUSED] -> Salto al blocco precedente (rimanendo in pausa)...");
        context.regressPlayable();

        Track prevTrack = context.getCurrentTrack();
        if (prevTrack != null) {
            System.out.println("[STATO: PAUSED] Coda agganciata sul blocco precedente: " + prevTrack.getTitle());
        }
    }
}
