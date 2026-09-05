
module it.unisa.java_music_playlist_manager {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.media;
    requires java.base;
    requires java.logging;

    requires org.controlsfx.controls;

    // Ikonli: icone FontAwesome 5 per JavaFX
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    // Libreria per la lettura affidabile dei tag ID3 (MP3, M4A, FLAC, ecc.)
    requires jaudiotagger;
    
    opens it.unisa.java_music_playlist_manager to javafx.fxml;
    opens it.unisa.java_music_playlist_manager.controllers to javafx.fxml;
    opens it.unisa.java_music_playlist_manager.view to javafx.fxml;
    opens it.unisa.java_music_playlist_manager.model to javafx.base, com.fasterxml.jackson.databind, org.junit.platform.commons;
    exports it.unisa.java_music_playlist_manager;
    exports it.unisa.java_music_playlist_manager.controllers;
    exports it.unisa.java_music_playlist_manager.view;
    exports it.unisa.java_music_playlist_manager.services;
    exports it.unisa.java_music_playlist_manager.model;
    exports it.unisa.java_music_playlist_manager.model.command;
    exports it.unisa.java_music_playlist_manager.model.generator;
    exports it.unisa.java_music_playlist_manager.model.state;
    exports it.unisa.java_music_playlist_manager.model.strategy;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    
}
