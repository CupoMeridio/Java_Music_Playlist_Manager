
module it.unisa.java_music_playlist_manager {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.media;
    requires java.base;

    
    requires org.controlsfx.controls;
    
    opens it.unisa.java_music_playlist_manager to javafx.fxml;
    opens it.unisa.java_music_playlist_manager.model to javafx.base, com.fasterxml.jackson.databind;
    exports it.unisa.java_music_playlist_manager;
    exports it.unisa.java_music_playlist_manager.model;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    
}
