package it.unisa.java_music_playlist_manager.dao;

import it.unisa.java_music_playlist_manager.model.Library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
/**
 * Implementazione concreta di {@link LibraryDAO} che persiste la Library
 * tramite Jackson su file JSON (Adapter di persistenza).
 */
public class JsonLibraryDAO implements LibraryDAO{

    private String path;
    private ObjectMapper mapper ;


    public JsonLibraryDAO(String path) {
        this.path = path;
        mapper = new ObjectMapper();
        // Registra il modulo Jackson per la gestione dei tipi di data Java Time (es. LocalDate)
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        // Abilita la formattazione formattata (indent) dell'output JSON per la leggibilità
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
