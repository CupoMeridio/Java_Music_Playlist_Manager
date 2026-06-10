package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.function.Supplier;

/**
 * L'interfaccia base del Creator per il pattern Factory Method.
 * Definisce il metodo astratto per creare una playlist.
 */
public interface PlaylistCreator {
    Playlist createPlaylist(String param, Supplier<List<Track>> trackSource);
}
