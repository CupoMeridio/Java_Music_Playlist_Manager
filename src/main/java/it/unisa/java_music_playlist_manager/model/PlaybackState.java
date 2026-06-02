package it.unisa.java_music_playlist_manager.model;

public interface PlaybackState {
    void play(PlaybackManager context);
    void stop(PlaybackManager context);
    void next(PlaybackManager context);
    void previous(PlaybackManager context);
}

