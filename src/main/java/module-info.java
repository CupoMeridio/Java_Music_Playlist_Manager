
module it.unisa.java_music_playlist_manager {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.media;
    requires java.base;

    
    requires org.controlsfx.controls;
    
    opens it.unisa.java_music_playlist_manager to javafx.fxml;
    opens it.unisa.java_music_playlist_manager.model to javafx.base;
    exports it.unisa.java_music_playlist_manager;
    exports it.unisa.java_music_playlist_manager.model;
    
}
