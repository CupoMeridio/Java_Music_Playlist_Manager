package it.unisa.java_music_playlist_manager.model;

public interface Command {
    void execute();
    void undo();
}