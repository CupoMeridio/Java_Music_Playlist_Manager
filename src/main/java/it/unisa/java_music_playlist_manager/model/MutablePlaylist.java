package it.unisa.java_music_playlist_manager.model;

public interface MutablePlaylist extends Playable {
    void add(Playable element);
    void remove(Playable element);
    void removeTrack(Track track);
    void addTrack(Track track);
    void moveElement(int fromIndex, int toIndex);
}
