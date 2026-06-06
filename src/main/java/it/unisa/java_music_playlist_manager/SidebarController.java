package it.unisa.java_music_playlist_manager;

import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Controller per la gestione della barra laterale (SidebarView.fxml).
 * Estratto da PrimaryViewController per separare la responsabilità della
 * navigazione laterale dalla gestione della vista principale.
 */
public class SidebarController {

    private Consumer<String> onNavigate;

    // CONTROLLI BARRA LATERALE
    @FXML
    private TextField searchField;
    @FXML
    private Button musicLibraryButton;
    @FXML
    private Button playQueueButton;
    @FXML
    private Button playlistButton;

    /**
     * Imposta il callback di navigazione. Il Consumer riceve un identificativo
     * della vista da attivare ("Musica", "Coda", "Playlist").
     * @param callback
     */
    public void setOnNavigate(Consumer<String> callback) {
        this.onNavigate = callback;
    }

    // GESTORI EVENTI BARRA LATERALE
    @FXML
    private void handleMusicLibraryAction() {
        if (onNavigate != null) {
            onNavigate.accept("Musica");
        }
    }

    @FXML
    private void handlePlayQueueAction() {
        if (onNavigate != null) {
            onNavigate.accept("Coda");
        }
    }

    @FXML
    private void handlePlaylistAction() {
        if (onNavigate != null) {
            onNavigate.accept("Playlist");
        }
    }
}
