
package it.unisa.java_music_playlist_manager.model;

public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers();
}
