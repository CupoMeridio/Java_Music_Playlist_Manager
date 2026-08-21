package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.*;
import it.unisa.java_music_playlist_manager.ui.ContextMenuManager;
import it.unisa.java_music_playlist_manager.utils.TimeFormatUtils;

import java.util.Comparator;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
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
     * Configura le colonne delle tabelle, le interazioni utente e carica i dati iniziali.
     */
    @FXML
    public void initialize() {
        setupColumns();
        setupTableInteractions();
        Library.getInstance().attach(this);
        PlaybackManager.getInstance().attach(this);
        refreshStats();
    }
    /**
     * Configura le cell-value-factory e le cell-factory delle colonne delle tre tabelle.
     */
    private void setupColumns() {
        // Tabella brani
        topTrackRankColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(getTableRow().getIndex() + 1));
                }
            }
        });
        topTrackTitleColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTitle()));
        topTrackArtistColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getAuthor()));
        topTrackCountColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getPlayCount()).asObject());

        // Tabella playlist
        topPlaylistRankColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(getTableRow().getIndex() + 1));
                }
            }
        });
        topPlaylistNameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTitle()));
        topPlaylistCountColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getTrackCount()).asObject());
        topPlaylistDurationColumn.setCellValueFactory(
                data -> new SimpleStringProperty(TimeFormatUtils.formatDuration(data.getValue().getDuration())));

        // Tabella playlist più riprodotte
        topPlayedPlaylistRankColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(getTableRow().getIndex() + 1));
                }
            }
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
        topTracksTable.refresh();
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
        topPlaylistsTable.refresh();
        setTablePlaceholder(topPlaylistsTable, "Non ci sono playlist. Creane una dalla sezione Playlist.");

        // Top 10 playlist per riproduzioni (decrescente)
        ObservableList<Playlist> topPlayedPlaylists = FXCollections.observableArrayList(
                allPlaylists.stream()
                        .sorted(Comparator.comparingInt(Playlist::getPlayCount).reversed())
                        .limit(10)
                        .toList()
        );
        topPlayedPlaylistsTable.setItems(topPlayedPlaylists);
        topPlayedPlaylistsTable.refresh();
        setTablePlaceholder(topPlayedPlaylistsTable, "Non ci sono playlist. Creane una dalla sezione Playlist.");
    }

    /**
     * Metodo del pattern Observer: aggiorna le statistiche quando la Library notifica un cambiamento.
     */
    @Override
    public void update() {
        javafx.application.Platform.runLater(() -> {
            refreshStats();
        });
    }

    /**
     * Configura il doppio click e i menu contestuali per tutte le tabelle della Home.
     */
    private void setupTableInteractions() {
        setupTrackTableInteractions();
        setupPlaylistTableInteractions(topPlaylistsTable);
        setupPlaylistTableInteractions(topPlayedPlaylistsTable);
    }

    /**
     * Configura il doppio click e il menu contestuale per la tabella dei brani più ascoltati.
     */
    private void setupTrackTableInteractions() {
        if (topTracksTable == null) {
            return;
        }

        // Avvio riproduzione al doppio click
        topTracksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                Track selectedTrack = topTracksTable.getSelectionModel().getSelectedItem();
                if (ContextMenuManager.isClickOnSelectedTableRow(event, selectedTrack)) {
                    playTrack(selectedTrack);
                }
            }
        });

        // Menu contestuale (Tasto destro) legato alle righe
        topTracksTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Track> row = new javafx.scene.control.TableRow<>();
            row.setOnMousePressed(event -> {
                if (row.isEmpty() || row.getItem() == null) {
                    return;
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    tv.getSelectionModel().select(row.getItem());
                }
            });

            ContextMenu trackContextMenu = new ContextMenu();
            MenuItem playItem = new MenuItem("Riproduci");
            MenuItem addToQueueItem = new MenuItem("Aggiungi brano alla coda");
            trackContextMenu.getItems().addAll(playItem, addToQueueItem);

            trackContextMenu.setOnShowing(e -> {
                Track selectedTrack = row.getItem();
                if (selectedTrack == null) {
                    e.consume();
                    return;
                }
                playItem.setOnAction(ev -> playTrack(selectedTrack));
                addToQueueItem.setOnAction(ev -> PlaybackManager.getInstance().addToQueue(selectedTrack));
            });

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(trackContextMenu));
            return row;
        });
    }

    /**
     * Configura il doppio click e il menu contestuale per una tabella di playlist.
     */
    private void setupPlaylistTableInteractions(TableView<Playlist> table) {
        if (table == null) {
            return;
        }

        // Avvio riproduzione al doppio click
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                Playlist selectedPlaylist = table.getSelectionModel().getSelectedItem();
                if (ContextMenuManager.isClickOnSelectedTableRow(event, selectedPlaylist)) {
                    playPlaylist(selectedPlaylist);
                }
            }
        });

        // Menu contestuale (Tasto destro) legato alle righe
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Playlist> row = new javafx.scene.control.TableRow<>();
            row.setOnMousePressed(event -> {
                if (row.isEmpty() || row.getItem() == null) {
                    return;
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    tv.getSelectionModel().select(row.getItem());
                }
            });

            ContextMenu playlistContextMenu = new ContextMenu();
            MenuItem playItem = new MenuItem("Riproduci playlist");
            MenuItem addToQueueItem = new MenuItem("Aggiungi playlist alla coda");
            playlistContextMenu.getItems().addAll(playItem, addToQueueItem);

            playlistContextMenu.setOnShowing(e -> {
                Playlist selectedPlaylist = row.getItem();
                if (selectedPlaylist == null) {
                    e.consume();
                    return;
                }
                playItem.setOnAction(ev -> playPlaylist(selectedPlaylist));
                addToQueueItem.setOnAction(ev -> PlaybackManager.getInstance().addToQueue(selectedPlaylist));
            });

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(playlistContextMenu));
            return row;
        });
    }

    /**
     * Avvia la riproduzione del brano selezionato, usando come contesto i brani mostrati in classifica.
     */
    private void playTrack(Track track) {
        if (track != null) {
            PlaybackManager.getInstance().selectAndLoadTrack(track, topTracksTable.getItems());
            PlaybackManager.getInstance().forcePlayCurrent();
        }
    }

    /**
     * Avvia la riproduzione della playlist selezionata se non vuota.
     */
    private void playPlaylist(Playlist playlist) {
        if (playlist != null && !playlist.getTracks().isEmpty()) {
            PlaybackManager.getInstance().play(playlist, false);
        }
    }

    // Utilità

    private <T> void setTablePlaceholder(TableView<T> table, String message) {
        Label placeholder = new Label(message);
        placeholder.setStyle("-fx-padding: 20;");
        placeholder.setWrapText(true);
        table.setPlaceholder(placeholder);
    }
}
