package it.unisa.java_music_playlist_manager.model;

/**
 * Rappresenta lo stato del lettore quando la riproduzione è completamente ferma.
 * In questo stato:
 * - Play: Avvia la riproduzione dalla traccia corrente o dall'inizio della coda.
 * - Stop: Non esegue alcuna azione (già fermo).
 * - Next/Prev: Spostano gli indici della coda senza avviare l'audio.
 */
public class StoppedState implements PlaybackState {

    @Override
    public void play(PlaybackManager context) {
        Track current = context.getCurrentTrack();
        if (current != null) {
            System.out.println("[STATO: STOPPED] -> Avvio riproduzione iniziale.");
            // Passaggio allo stato PlayingState e avvio dell'audio reale
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
        context.advanceTrack(); // Aggiorna solo l'indice della coda
    }
    
    @Override
    public void nextPlayable(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Azione ignorata: impossibile saltare il blocco mentre il lettore è spento.");
    }

    @Override
    public void previous(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Azione ignorata: impossibile fare 'Previous' mentre il lettore è spento.");
    }

    @Override
    public void previousPlayable(PlaybackManager context) {
        System.out.println("[STATO: STOPPED] Azione ignorata: impossibile saltare al blocco precedente mentre il lettore è spento.");
    }

}
