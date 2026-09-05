package it.unisa.java_music_playlist_manager.model.strategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleStrategy implements PlaybackStrategy {

    private final List<Integer> shuffledIndices = new ArrayList<>();
    private int currentShufflePointer = 0;
    private int cachedQueueSize = 0;

    /**
     * Calcola l'indice del prossimo elemento.
     * * @param currentIndex L'indice dell'elemento attualmente in riproduzione (nel Manager).
     * @param queueSize    La dimensione totale della coda.
     * @return L'indice del prossimo elemento casuale, o queueSize se la coda è terminata.
     */
    @Override
    public int getNextIndex(int currentIndex, int queueSize) {
        if (queueSize <= 0) return queueSize;

        // Se la dimensione della coda è cambiata dall'ultima volta, rigeneriamo
        if (queueSize != cachedQueueSize || shuffledIndices.isEmpty()) {
            initializeShuffle(queueSize, currentIndex);
        }

        // Avanziamo il puntatore interno del mazzo mescolato
        currentShufflePointer++;

        if (currentShufflePointer >= shuffledIndices.size()) {

            // Rigeneriamo il mazzo passando l'indice corrente affinché non venga ripetuto subito
            initializeShuffle(queueSize, currentIndex);

            // Spostiamo il puntatore a 1, poiché l'indice 0 conterrà la traccia attuale (appena finita)
            // e noi vogliamo passare direttamente alla prima traccia del NUOVO ordine casuale
            currentShufflePointer = 1;

            // Caso limite: se c'è solo 1 traccia nella playlist, rimaniamo su quella
            if (shuffledIndices.size() <= 1) {
                currentShufflePointer = 0;
            }
        }
        // Restituisce il valore reale mappato a quella posizione casuale
        int nextRealIndex = shuffledIndices.get(currentShufflePointer);

        return nextRealIndex;
    }

    /**
     * Inizializza il mazzo di indici mescolati.
     * Garantisce che l'elemento attualmente in riproduzione finisca in prima posizione,
     * in modo che il "Next" non lo ripeta subito.
     */
    private void initializeShuffle(int queueSize, int currentIndex) {
        this.cachedQueueSize = queueSize;
        shuffledIndices.clear();

        // 1. Popoliamo la lista con tutti gli indici della coda reale
        for (int i = 0; i < queueSize; i++) {
            shuffledIndices.add(i);
        }

        // 2. Rimuoviamo temporaneamente l'indice corrente per non mescolarlo
        //    (Evita che la canzone attuale rimanga incastrata a metà mazzo)
        if (currentIndex >= 0 && currentIndex < queueSize) {
            shuffledIndices.remove(Integer.valueOf(currentIndex));
        }

        // 3. Mescoliamo il resto degli indici
        Collections.shuffle(shuffledIndices);

        // 4. Reinseriamo l'indice corrente all'inizio del mazzo (posizione 0)
        if (currentIndex >= 0 && currentIndex < queueSize) {
            shuffledIndices.add(0, currentIndex);
        }

        // 5. Il puntatore parte da 0 (che corrisponde all'elemento corrente)
        this.currentShufflePointer = 0;
    }
}