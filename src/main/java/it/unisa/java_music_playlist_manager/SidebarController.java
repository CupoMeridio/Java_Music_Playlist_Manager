package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.view.ViewType;

import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

/**
 * SidebarController gestisce l'interfaccia della barra di navigazione laterale.
 * Permette all'utente di passare tra le diverse sezioni dell'applicazione e
 * di selezionare il tema visivo tramite il pulsante 🎨.
 */
public class SidebarController {

    private Consumer<ViewType> onNavigate;
    private Consumer<String> onSearchQueryChange;

    @FXML private TextField searchField;
    @FXML private Button homeButton;
    @FXML private Button musicLibraryButton;
    @FXML private Button playQueueButton;
    @FXML private Button playlistButton;
    @FXML private Button themeButton;

    /** Menu contestuale con l'elenco dei temi disponibili */
    private ContextMenu themeMenu;

    public void setOnNavigate(Consumer<ViewType> callback) {
        this.onNavigate = callback;
    }

    public void setOnSearchQueryChange(Consumer<String> callback) {
        this.onSearchQueryChange = callback;
    }

    @FXML
    private void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (onSearchQueryChange != null) onSearchQueryChange.accept(newValue);
            });
        }
        buildThemeMenu();
    }

    /**
     * Costruisce il ContextMenu con tutti i temi registrati nel ThemeManager.
     * Il tema attivo viene marcato con ✓.
     */
    private void buildThemeMenu() {
        themeMenu = new ContextMenu();
        themeMenu.getStyleClass().add("theme-menu");

        for (String name : ThemeManager.getInstance().getThemeNames()) {
            MenuItem item = new MenuItem(name);
            item.setOnAction(e -> {
                ThemeManager.getInstance().applyTheme(name);
                refreshThemeMenu(); // aggiorna il segno di spunta
            });
            themeMenu.getItems().add(item);
        }
        refreshThemeMenu();
    }

    /** Aggiorna i segni di spunta nel menu in base al tema attivo corrente. */
    private void refreshThemeMenu() {
        String active = ThemeManager.getInstance().getActiveTheme();
        for (MenuItem item : themeMenu.getItems()) {
            String label = item.getText().replace("✓  ", "");
            item.setText(active.equals(label) ? "✓  " + label : label);
        }
    }

    // Gestori eventi UI

    @FXML
    private void handleHomeAction() {
        if (onNavigate != null) onNavigate.accept(ViewType.HOME);
    }

    @FXML
    private void handleMusicLibraryAction() {
        if (onNavigate != null) onNavigate.accept(ViewType.MUSIC);
    }

    @FXML
    private void handlePlayQueueAction() {
        if (onNavigate != null) onNavigate.accept(ViewType.QUEUE);
    }

    @FXML
    private void handlePlaylistAction() {
        if (onNavigate != null) onNavigate.accept(ViewType.PLAYLISTS);
    }

    /** Mostra il menu dei temi sotto il pulsante 🎨. */
    @FXML
    private void handleThemeAction() {
        if (themeButton != null && themeMenu != null) {
            themeMenu.show(themeButton, javafx.geometry.Side.RIGHT, 0, 0);
        }
    }
}
