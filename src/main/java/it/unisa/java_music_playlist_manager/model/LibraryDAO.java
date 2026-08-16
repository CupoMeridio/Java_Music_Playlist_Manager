/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;

import java.io.IOException;

/**
 * Porta per l'interfaccia DAO della Library (Persistence / Hexagonal).
 * Consente di salvare e caricare la Library da file locali o remoti
 * indipendentemente dall'implementazione concreta (es. JSON, XML, SQLite...).
 */
public interface LibraryDAO {
    
    public void save( Library lib) throws IOException;
    public Library load() throws IOException;
}
