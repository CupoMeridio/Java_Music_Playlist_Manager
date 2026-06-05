/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;
import java.util.List;

/**
 *
 * @author Mattia Sanzari
 */

/**
 * Interfaccia che definisce un elemento riproducibile.
 * Permette di trattare singole tracce e intere playlist in modo uniforme.
 */
public interface Playable {
    String getTitle();
    List<Track> getTracks(); 
}