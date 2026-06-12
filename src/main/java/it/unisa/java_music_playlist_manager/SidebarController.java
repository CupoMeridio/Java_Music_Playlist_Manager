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
/**
 * SidebarController gestisce l'interfaccia della barra di navigazione laterale.
 * Permette all'utente di passare tra le diverse sezioni dell'applicazione:
 * Libreria Musicale, Coda di riproduzione e Playlist.
 * 
 * Ruolo nel progetto:
 * - Agisce come emettitore di eventi di navigazione.
 * - Comunica con il {@link PrimaryViewController} tramite un {@link Consumer} di stringhe,
 *   disaccoppiando la struttura della barra laterale dalla logica di visualizzazione centrale.
 */
public class SidebarController {

    /** Callback per la notifica degli eventi di navigazione al controller principale */
    private Consumer<String> onNavigate;
    private Consumer<String> onSearchQueryChange;

    @FXML
    private TextField searchField;
    @FXML
    private Button homeButton;
    @FXML
    private Button musicLibraryButton;
    @FXML
    private Button playQueueButton;
    @FXML
    private Button playlistButton;

    /**
     * Imposta il callback di navigazione.
     * @param callback Un Consumer che accetta l'identificativo della vista ("Musica", "Coda", "Playlist").
     */
    public void setOnNavigate(Consumer<String> callback) {
        this.onNavigate = callback;
    }

    public void setOnSearchQueryChange(Consumer<String> callback) {
        this.onSearchQueryChange = callback;
    }

    @FXML
    private void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (onSearchQueryChange != null) {
                    onSearchQueryChange.accept(newValue);
                }
            });
        }
    }

    // --- Gestori eventi UI ---

    /** Notifica la richiesta di visualizzazione della Home (statistiche). */
    @FXML
    private void handleHomeAction() {
        if (onNavigate != null) {
            onNavigate.accept("Home");
        }
    }

    /** Notifica la richiesta di visualizzazione della Libreria Musicale. */
    @FXML
    private void handleMusicLibraryAction() {
        if (onNavigate != null) {
            onNavigate.accept("Musica");
        }
    }

    /** Notifica la richiesta di visualizzazione della Coda di Riproduzione. */
    @FXML
    private void handlePlayQueueAction() {
        if (onNavigate != null) {
            onNavigate.accept("Coda");
        }
    }

    /** Notifica la richiesta di visualizzazione dell'elenco delle Playlist. */
    @FXML
    private void handlePlaylistAction() {
        if (onNavigate != null) {
            onNavigate.accept("Playlist");
        }
    }
}
