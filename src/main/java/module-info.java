module it.unisa.java_music_playlist_manager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens it.unisa.java_music_playlist_manager to javafx.fxml;
    exports it.unisa.java_music_playlist_manager;
}
