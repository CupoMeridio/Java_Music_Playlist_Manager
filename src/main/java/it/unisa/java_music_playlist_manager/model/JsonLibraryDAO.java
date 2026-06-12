/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unisa.java_music_playlist_manager.model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
/**
 *
 * @author Mattia Sanzari
 */
public class JsonLibraryDAO implements LibraryDAO{

    private String path;
    private ObjectMapper mapper ;


    public JsonLibraryDAO(String path) {
        this.path = path;
        mapper = new ObjectMapper();
        // Questo serve a Jackson per le date come LocalDate
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                // rendo leggibile agli umani
       mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
    
    
    
    @Override
    public void save(Library lib) throws IOException {
        File path_file = new File(path);
       // Crea le cartelle genitore se non esistono
        File parent = path_file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
       mapper.writeValue(path_file, lib);
    }

    @Override
    public Library load() throws IOException {
        Library lib = Library.getInstance();
        File file = new File(path);
        if(!file.exists()) return lib;
        mapper.readerForUpdating(lib).readValue(file);
        return lib;
    }
    
}
