package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.Observer;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * HomeController gestisce la schermata principale dell'applicazione.
 * Mostra le statistiche di riproduzione: brani più ascoltati, playlist più popolari
 * e playlist più riprodotte.
 *
 * Si registra come Observer della Library per aggiornarsi automaticamente
 * a ogni modifica del catalogo.
 */
public class HomeController implements Observer {

    @FXML private Label greetingLabel;

    // Statistiche rapide
    @FXML private Label totalTracksLabel;
    @FXML private Label totalPlaylistsLabel;
    @FXML private Label totalPlaysLabel;

    // Tabella brani più riprodotti
    @FXML private TableView<Track> topTracksTable;
    @FXML private TableColumn<Track, Integer> topTrackRankColumn;
    @FXML private TableColumn<Track, String>  topTrackTitleColumn;
    @FXML private TableColumn<Track, String>  topTrackArtistColumn;
    @FXML private TableColumn<Track, Integer> topTrackCountColumn;

    // Tabella playlist più popolari
    @FXML private TableView<Playlist> topPlaylistsTable;
    @FXML private TableColumn<Playlist, Integer> topPlaylistRankColumn;
    @FXML private TableColumn<Playlist, String>  topPlaylistNameColumn;
    @FXML private TableColumn<Playlist, Integer> topPlaylistCountColumn;
    @FXML private TableColumn<Playlist, String>  topPlaylistDurationColumn;

    // Tabella playlist più riprodotte
    @FXML private TableView<Playlist> topPlayedPlaylistsTable;
    @FXML private TableColumn<Playlist, Integer> topPlayedPlaylistRankColumn;
    @FXML private TableColumn<Playlist, String>  topPlayedPlaylistNameColumn;
    @FXML private TableColumn<Playlist, Integer> topPlayedPlaylistPlayCountColumn;
    @FXML private TableColumn<Playlist, Integer> topPlayedPlaylistTrackCountColumn;

    /**
     * Inizializzazione chiamata automaticamente da JavaFX.
     * Configura le colonne delle tabelle e carica i dati iniziali.
     */
    @FXML
    public void initialize() {
        setupColumns();
        Library.getInstance().attach(this);
        refreshStats();
    }

    /**
     * Configura le cell-value-factory delle colonne delle tre tabelle.
     */
    private void setupColumns() {
        // -- Tabella brani --
        topTrackRankColumn.setCellValueFactory(data -> {
            int idx = topTracksTable.getItems().indexOf(data.getValue()) + 1;
            return new SimpleIntegerProperty(idx).asObject();
        });
        topTrackTitleColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTitle()));
        topTrackArtistColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getAuthor()));
        topTrackCountColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getPlayCount()).asObject());

        // -- Tabella playlist --
        topPlaylistRankColumn.setCellValueFactory(data -> {
            int idx = topPlaylistsTable.getItems().indexOf(data.getValue()) + 1;
            return new SimpleIntegerProperty(idx).asObject();
        });
        topPlaylistNameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTitle()));
        topPlaylistCountColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getTrackCount()).asObject());
        topPlaylistDurationColumn.setCellValueFactory(
                data -> new SimpleStringProperty(formatDuration(data.getValue().getDuration())));

        // -- Tabella playlist più riprodotte --
        topPlayedPlaylistRankColumn.setCellValueFactory(data -> {
            int idx = topPlayedPlaylistsTable.getItems().indexOf(data.getValue()) + 1;
            return new SimpleIntegerProperty(idx).asObject();
        });
        topPlayedPlaylistNameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTitle()));
        topPlayedPlaylistPlayCountColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getPlayCount()).asObject());
        topPlayedPlaylistTrackCountColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getTrackCount()).asObject());
    }

    /**
     * Aggiorna tutte le statistiche lette dalla Library e dal PlaybackManager.
     * Viene chiamato all'inizializzazione e ogni volta che la Library cambia stato.
     */
    public void refreshStats() {
        Library lib = Library.getInstance();
        List<Track>    allTracks    = lib.getTracks();
        List<Playlist> allPlaylists = lib.getPlaylists();

        // Calcola il totale delle riproduzioni
        int totalPlays = allTracks.stream()
                .mapToInt(Track::getPlayCount)
                .sum();

        // Aggiorna le card
        totalTracksLabel.setText(String.valueOf(allTracks.size()));
        totalPlaylistsLabel.setText(String.valueOf(allPlaylists.size()));
        totalPlaysLabel.setText(String.valueOf(totalPlays));

        // Top 10 brani per playCount (decrescente)
        ObservableList<Track> topTracks = FXCollections.observableArrayList(
                allTracks.stream()
                        .sorted(Comparator.comparingInt(Track::getPlayCount).reversed())
                        .limit(10)
                        .toList()
        );
        topTracksTable.setItems(topTracks);
        setTablePlaceholder(topTracksTable,
                allTracks.isEmpty()
                        ? "Non ci sono brani in libreria."
                        : "Nessun brano ancora riprodotto. Inizia ad ascoltare!");

        // Top 10 playlist per numero di brani (decrescente)
        ObservableList<Playlist> topPlaylists = FXCollections.observableArrayList(
                allPlaylists.stream()
                        .sorted(Comparator.comparingInt(Playlist::getTrackCount).reversed())
                        .limit(10)
                        .toList()
        );
        topPlaylistsTable.setItems(topPlaylists);
        setTablePlaceholder(topPlaylistsTable, "Non ci sono playlist. Creane una dalla sezione Playlist.");

        // Top 10 playlist per riproduzioni (decrescente)
        ObservableList<Playlist> topPlayedPlaylists = FXCollections.observableArrayList(
                allPlaylists.stream()
                        .sorted(Comparator.comparingInt(Playlist::getPlayCount).reversed())
                        .limit(10)
                        .toList()
        );
        topPlayedPlaylistsTable.setItems(topPlayedPlaylists);
        setTablePlaceholder(topPlayedPlaylistsTable, "Non ci sono playlist. Creane una dalla sezione Playlist.");
    }

    /**
     * Metodo del pattern Observer: aggiorna le statistiche quando la Library notifica un cambiamento.
     */
    @Override
    public void update() {
        refreshStats();
    }

    // --- Utilità ---

    private String formatDuration(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private <T> void setTablePlaceholder(TableView<T> table, String message) {
        Label placeholder = new Label(message);
        placeholder.setStyle("-fx-padding: 20;");
        placeholder.setWrapText(true);
        table.setPlaceholder(placeholder);
    }
}
