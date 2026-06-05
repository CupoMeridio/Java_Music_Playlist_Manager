module it.unisa.java_music_playlist_manager {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens it.unisa.java_music_playlist_manager to javafx.fxml;
    opens it.unisa.java_music_playlist_manager.model to javafx.base;
    exports it.unisa.java_music_playlist_manager;
    exports it.unisa.java_music_playlist_manager.model;
}
