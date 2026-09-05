package it.unisa.java_music_playlist_manager.model.generator;

import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import it.unisa.java_music_playlist_manager.model.Playable;

/**
 * Classe responsabile della creazione delle playlist.
 *
 * Permette di creare sia playlist vuote, modificabili manualmente
 * dall'utente, sia playlist automatiche basate sui metadati dei brani.
 *
 * Le playlist automatiche possono essere generate per genere musicale
 * oppure per anno di uscita.
 */
/**
 * Creatore concreto per playlist manuali vuote, che l'utente
 * potrà successivamente far modificare inserendo o rimuovendo brani.
 */
public class ManualPlaylistGenerator extends PlaylistGenerator {

    @Override
    public Playable createPlaylist(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della playlist non può essere vuoto.");
        }
        return new ManualPlaylist(title);
    }
}