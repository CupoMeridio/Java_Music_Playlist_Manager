package it.unisa.java_music_playlist_manager.model;
import java.util.List;

/**
 * L'interfaccia Playable definisce il contratto per qualsiasi elemento che può essere riprodotto.
 * È la componente base del Pattern Composite utilizzato nel progetto.
 * 
 * Grazie a questa interfaccia, il sistema di riproduzione può trattare allo stesso modo
 * una singola traccia (Track) o un'intera collezione (Playlist), facilitando la gestione della coda.
 */
public interface Playable {
    /**
     * Restituisce il titolo dell'elemento riproducibile.
     * @return Il titolo della traccia o il nome della playlist.
     */
    String getTitle();

    /**
     * Restituisce la lista di tutte le tracce atomiche contenute in questo elemento.
     * Per una Track, restituisce se stessa; per una Playlist, raccoglie ricorsivamente tutte le sue tracce.
     * 
     * @return Una lista di oggetti Track.
     */
    List<Track> getTracks(); 
}