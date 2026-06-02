/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;

/**
 *
 * @author Mattia Sanzari
 */
public interface Playable {
    public void play(); //operation
    public void add(Playable component);
    public boolean remove (Playable component);
    public String getTitle();
    public int getDuration(); // in secondi per track e in numero di track per Playlist  
}
