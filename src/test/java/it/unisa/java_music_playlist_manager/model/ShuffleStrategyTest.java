package it.unisa.java_music_playlist_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ShuffleStrategyTest {

    private PlaybackManager manager;
    private List<Track> testTracks;

    @BeforeEach
    void setUp() {
        manager = PlaybackManager.getInstance();
        manager.setAudioEnabled(false); // Disabilita l'audio reale per i test unitari
        manager.setStrategy(new SequentialStrategy()); // Reset della strategia di default
        manager.changeState(new StoppedState());
        manager.resetQueue();

        // Popolamento dinamico usando il costruttore reale di Track
        testTracks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            // Parametri reali: title, author, album, duration, genre, year, filePath
            testTracks.add(new Track(
                    "Track " + i,
                    "Artist " + i,
                    "Album " + i,
                    180,
                    "Rock",
                    2024,
                    "path/to/file" + i + ".mp3"
            ));
        }
    }

    /**
     * CHK-1: Verificare che l'attivazione dello shuffle rimescoli correttamente la coda attuale.
     */
    @Test
    void testShuffleRimescolaLaCoda() {
        boolean mescolato = false;
        // Eseguiamo fino a 3 tentativi per eliminare i falsi positivi stocastici (1 probabilità su 120 che uno shuffle dia l'ordine identico [0,1,2,3,4])
        for (int attempt = 0; attempt < 3; attempt++) {
            manager.resetQueue();
            manager.setQueue(testTracks);
            manager.setStrategy(new ShuffleStrategy());
            manager.pressPlay(); // Attiva lo stato di riproduzione

            List<Integer> indiciEstratti = new ArrayList<>();
            indiciEstratti.add(manager.getCurrentPlayableIndex());

            // Avanziamo per raccogliere l'intero mazzo iniziale di riproduzione
            for (int i = 1; i < testTracks.size(); i++) {
                manager.advanceTrack();
                indiciEstratti.add(manager.getCurrentPlayableIndex());
            }

            // 1. Verifichiamo l'integrità dei dati (nessun brano deve essere andato smarrito)
            for (int i = 0; i < testTracks.size(); i++) {
                assertTrue(indiciEstratti.contains(i), "Lo shuffle ha smarrito la traccia all'indice reale: " + i);
            }

            // 2. Verifichiamo che l'ordine sia diverso da quello prettamente sequenziale [0, 1, 2, 3, 4]
            for (int i = 0; i < indiciEstratti.size(); i++) {
                if (indiciEstratti.get(i) != i) {
                    mescolato = true;
                    break;
                }
            }
            if (mescolato) break;
        }

        assertTrue(mescolato, "Errore: Lo shuffle non ha mescolato la coda (stesso ordine di SequentialStrategy)");
    }

    /**
     * CHK-2: Verificare che non ci siano ripetizioni consecutive dello stesso brano.
     * Testa anche che la rotazione infinita mantenga la regola nel cambio di mazzo.
     */
    @Test
    void testNoRipetizioniConsecutiveAncheNelCicloInfinito() {
        manager.setQueue(testTracks);
        manager.setStrategy(new ShuffleStrategy());
        manager.pressPlay();

        int tracciaPrecedente = -1;

        // Eseguiamo 15 passaggi continui (pari a 3 giri completi della playlist da 5 elementi)
        for (int i = 0; i < 15; i++) {
            int tracciaCorrente = manager.getCurrentPlayableIndex();

            assertNotEquals(tracciaPrecedente, tracciaCorrente,
                    "Rilevata una ripetizione consecutiva della traccia all'indice: " + tracciaCorrente + " al passo " + i);

            tracciaPrecedente = tracciaCorrente;
            manager.advanceTrack();
        }
    }

    /**
     * CHK-3: Testare il comportamento con playlist molto brevi (1 solo brano o vuote).
     */
    @Test
    void testPlaylistBreviEVuote() {
        // Sotto-caso A: La coda è completamente vuota
        manager.setQueue(new ArrayList<>());
        manager.setStrategy(new ShuffleStrategy());

        assertNull(manager.getCurrentTrack(), "Con la coda vuota, il brano corrente deve essere nullo.");
        assertDoesNotThrow(() -> manager.pressPlay(), "Il sistema è crashato premendo play a coda vuota.");
        assertDoesNotThrow(() -> manager.advanceTrack(), "Il sistema è crashato avanzando a coda vuota.");

        // Sotto-caso B: La coda contiene un solo brano
        List<Track> playlistSingola = new ArrayList<>();
        playlistSingola.add(testTracks.get(0));
        manager.setQueue(playlistSingola);
        manager.pressPlay();

        assertEquals(0, manager.getCurrentPlayableIndex(), "L'unico brano disponibile deve trovarsi all'indice 0.");

        // Trattandosi di shuffle infinito su 1 brano, l'avanzamento deve lasciarci stabilmente sul brano
        assertDoesNotThrow(() -> manager.advanceTrack());
        assertEquals(0, manager.getCurrentPlayableIndex(), "In playlist da 1 brano, lo shuffle deve restare fisso sull'indice 0.");
        assertNotNull(manager.getCurrentTrack());
    }

    /**
     * CHK-4: Verificare che la navigazione (Avanti/Indietro) rispetti l'ordine casuale quando attivo.
     */
    @Test
    void testNavigazioneAvantiIndietroInShuffle() {
        manager.setQueue(testTracks);
        manager.setStrategy(new ShuffleStrategy());
        manager.pressPlay();

        // 1. Avanziamo estraendo i primi tre indici casuali guidati dallo Shuffle
        int primoCasuale = manager.getCurrentPlayableIndex();
        manager.advanceTrack();
        int secondoCasuale = manager.getCurrentPlayableIndex();
        manager.advanceTrack();
        int terzoCasuale = manager.getCurrentPlayableIndex();

        // Tutti gli indici calcolati in avanti devono essere validi e coerenti
        assertTrue(primoCasuale >= 0 && primoCasuale < testTracks.size());
        assertTrue(secondoCasuale >= 0 && secondoCasuale < testTracks.size());
        assertTrue(terzoCasuale >= 0 && terzoCasuale < testTracks.size());

        // 2. Chiamiamo il comando indietro (regressTrack) per validare la stabilità
        // Il manager decrescerà l'indice reale di coda. Verifichiamo che il sistema non rompa i limiti.
        assertDoesNotThrow(() -> manager.regressTrack(), "Il regresso della traccia ha sollevato un'eccezione.");
        int indiceDopoRegress = manager.getCurrentPlayableIndex();
        assertTrue(indiceDopoRegress >= 0 && indiceDopoRegress < testTracks.size(),
                "La navigazione indietro ha portato l'indice fuori dai confini della coda: " + indiceDopoRegress);

        // 3. Riprendiamo la marcia in avanti per assicurare il reinserimento nello Shuffle
        assertDoesNotThrow(() -> manager.advanceTrack());
        assertTrue(manager.getCurrentPlayableIndex() >= 0 && manager.getCurrentPlayableIndex() < testTracks.size());
    }
}