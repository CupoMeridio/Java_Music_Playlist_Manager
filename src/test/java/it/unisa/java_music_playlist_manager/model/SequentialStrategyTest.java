package it.unisa.java_music_playlist_manager.model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SequentialStrategyTest {

    private PlaybackStrategy strategy;
    private int queueSize;

    @BeforeEach
    public void setUp() {
        // Inizializziamo la strategia prima di ogni test
        strategy = new SequentialStrategy();
        // Simuliamo una coda con 3 brani (indici validi: 0, 1, 2)
        queueSize = 3;
    }

    /**
     * Verificare che la strategia sequenziale restituisca
     * i brani nell'esatto ordine di inserimento della lista.
     */
    @Test
    public void testGetNextIndexInSequence() {
        // Partendo dall'indice 0 (primo brano), il prossimo deve essere 1
        int nextFromFirst = strategy.getNextIndex(0, queueSize);
        assertEquals(1, nextFromFirst, "Partendo dall'indice 0, la strategia deve restituire 1");

        // Partendo dall'indice 1 (secondo brano), il prossimo deve essere 2
        int nextFromSecond = strategy.getNextIndex(1, queueSize);
        assertEquals(2, nextFromSecond, "Partendo dall'indice 1, la strategia deve restituire 2");
    }

    /**
     * Verificare che, una volta raggiunta la fine della playlist,
     * la strategia segnali correttamente la conclusione dell'elenco (restituendo queueSize).
     */
    @Test
    public void testGetNextIndexAtEndOfQueue() {
        // Ci posizioniamo sull'ultimo brano disponibile (indice 2 su una coda di 3 elementi)
        int lastIndex = queueSize - 1;

        // Richiediamo il prossimo indice quando siamo già alla fine
        int resultIndex = strategy.getNextIndex(lastIndex, queueSize);

        // La strategia deve restituire il valore esatto di queueSize (ovvero 3)
        assertEquals(queueSize, resultIndex,
                "Raggiunta la fine della lista, la strategia deve restituire una dimensione pari a queueSize per segnalare l'arresto.");
    }


// casi limite

/**
* Verificare il comportamento con una coda completamente vuota.
 */

@Test
public void testGetNextIndexWithEmptyQueue() {
    int emptyQueueSize = 0;

    // Se la coda è vuota, l'indice corrente ipotetico è 0
    int resultIndex = strategy.getNextIndex(0, emptyQueueSize);

    // Deve restituire immediatamente 0 (ovvero emptyQueueSize) per bloccare il player
    assertEquals(emptyQueueSize, resultIndex,
            "Con coda vuota, la strategia deve restituire 0 per bloccare subito la riproduzione.");
}

/**
 *  Verificare il comportamento con un solo brano in lista.
 */
@Test
public void testGetNextIndexWithSingleTrackQueue() {
    int singleTrackQueueSize = 1;

    // Siamo sul primo e unico brano (indice 0)
    int resultIndex = strategy.getNextIndex(0, singleTrackQueueSize);

    // Premendo next deve restituire 1 (queueSize) segnalando la fine
    assertEquals(singleTrackQueueSize, resultIndex,
            "Con un solo brano in coda, l'avanzamento deve portare immediatamente alla segnalazione di fine lista.");
}

/**
 *  Test di robustezza contro indici di partenza non validi (fuori limite).
 */
@Test
public void testGetNextIndexWithCurrentIndexOutOfBounds() {
    // Supponiamo che l'indice corrente sia rimasto a 5, ma la coda ora ha solo 3 elementi
    int invalidCurrentIndex = 5;

    int resultIndex = strategy.getNextIndex(invalidCurrentIndex, queueSize);

    // L'algoritmo (currentIndex < queueSize - 1) valuterà 5 < 2 (falso) e restituirà giustamente queueSize (3)
    assertEquals(queueSize, resultIndex,
            "Se l'indice di partenza è già fuori dai limiti superiori, la strategia deve restituire queueSize.");
}

}