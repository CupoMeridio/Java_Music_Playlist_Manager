/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;

import java.io.IOException;

/**
 *
 * @author Mattia Sanzari
 */
public interface LibraryDAO {
    
    public void save( Library lib) throws IOException;
    public Library load() throws IOException;
}
