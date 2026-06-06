package it.unisa.java_music_playlist_manager.model;
import java.util.List;

/**
 * Interfaccia che definisce un elemento riproducibile.
 * Permette di trattare singole tracce e intere playlist in modo uniforme.
 */
public interface Playable {
    String getTitle();
    List<Track> getTracks(); 
}