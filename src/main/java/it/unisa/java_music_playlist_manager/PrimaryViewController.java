package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.ui.ContextMenuManager;
import it.unisa.java_music_playlist_manager.ui.ContextMenuManager.ContextMenuActions;

import it.unisa.java_music_playlist_manager.ui.PlaylistDialogService;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.LibrarySearchService;
import it.unisa.java_music_playlist_manager.model.Playable;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.Command;
import it.unisa.java_music_playlist_manager.model.RemoveTrackCommand;
import it.unisa.java_music_playlist_manager.model.RemovePlaylistCommand;
import it.unisa.java_music_playlist_manager.model.RemoveElementFromPlaylistCommand;

import it.unisa.java_music_playlist_manager.model.UndoManager;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import it.unisa.java_music_playlist_manager.model.Observer;
import java.io.IOException;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.ViewType;
import java.util.Set;
import java.util.HashSet;
import javafx.scene.Node;

/**
 * PrimaryViewController è il coordinatore principale dell'interfaccia utente.
 * Gestisce l'area centrale dell'applicazione, inclusa la visualizzazione delle
 * tracce,
 * delle playlist e della coda di riproduzione.
 * 
 * La struttura dei campi annotati con @FXML e i collegamenti ai metodi di
 * gestione eventi
 * sono configurati automaticamente tramite l'integrazione tra SceneBuilder e
 * NetBeans.
 * 
 * Ruolo nel progetto:
 * - Agisce come Controller principale che integra i sotto-controller (Sidebar e
 * PlayerBar).
 * - Implementa l'interfaccia {@link Observer} per reagire ai cambiamenti nel
 * Modello (Library e PlaybackManager).
 * - Gestisce la logica di navigazione tra le diverse viste (Musica, Playlist,
 * Coda).
 * - Coordina le operazioni CRUD su tracce e playlist tramite menu contestuali e
 * dialoghi.
 */
public class PrimaryViewController implements Observer, ContextMenuActions {

    /**
     * Traccia attualmente in fase di modifica (usata per popolare il form di
     * editing)
     */
    private Track currentEditingTrack = null;

    private final PlaylistDialogService dialogService = new PlaylistDialogService(this::showPlaylistColumns);

    /** Playlist attualmente aperta nella vista dettaglio */
    private Playlist currentOpenedPlaylist = null;

    private String searchQuery = "";

    @FXML
    private ListView<QueueItem> queueListView;

    private final Set<Playlist> expandedPlaylists = new HashSet<>();

    private static class QueueItem {
        final Playable parentPlayable;
        final Track track;
        final int queueIndex;

        QueueItem(Playable parentPlayable, Track track, int queueIndex) {
            this.parentPlayable = parentPlayable;
            this.track = track;
            this.queueIndex = queueIndex;
        }
    }

    /**
     * Metodo del pattern Observer.
     * Chiamato quando la Library o il PlaybackManager notificano un cambiamento.
     * Forza il refresh della tabella e la sincronizzazione della selezione.
     */
    @Override
    public void update() {
        javafx.application.Platform.runLater(() -> {
            if (currentViewType == ViewType.PLAYLISTS) {
                showPlaylistColumns();
                if (playlistTableView != null)
                    playlistTableView.refresh();
            } else {
                refreshTableData();
                if (trackTableView != null)
                    trackTableView.refresh();
            }
            syncTableSelection();
            updatePlayPlaylistButtonState();

            if (undoButton != null) {
                undoButton.setDisable(!UndoManager.getInstance().canUndo());
            }
        });
    }

    private boolean canPlayPlaylistButton() {
        if (currentOpenedPlaylist != null) {
            return !currentOpenedPlaylist.getTracks().isEmpty();
        }

        if (playlistTableView != null && playlistTableView.getSelectionModel().getSelectedItem() != null) {
            return !playlistTableView.getSelectionModel().getSelectedItem().getTracks().isEmpty();
        }

        return false;
    }

    private void updatePlayPlaylistButtonState() {
        if (playPlaylistButton != null) {
            playPlaylistButton.setDisable(!canPlayPlaylistButton());
        }
    }

    /**
     * Aggiorna i dati mostrati nella TableView in base alla vista corrente (Musica,
     * Coda o Playlist aperta).
     */
    private void refreshTableData() {
        TableViewSortState sortState = captureSortState();

        if (currentOpenedPlaylist != null) {
            ObservableList<Track> trackList = javafx.collections.FXCollections.observableArrayList(
                    LibrarySearchService.filterTracks(currentOpenedPlaylist.getTracks(), searchQuery));
            if (trackTableView != null)
                trackTableView.setItems(trackList);
        } else if (currentViewType == ViewType.QUEUE) {
            List<QueueItem> items = new ArrayList<>();
            List<Playable> queue = PlaybackManager.getInstance().getCurrentQueue();
            for (int i = 0; i < queue.size(); i++) {
                Playable p = queue.get(i);
                if (p instanceof Playlist playlist) {
                    items.add(new QueueItem(p, null, i));
                    if (expandedPlaylists.contains(playlist)) {
                        for (Track t : playlist.getTracks()) {
                            items.add(new QueueItem(p, t, i));
                        }
                    }
                } else if (p instanceof Track track) {
                    items.add(new QueueItem(p, track, i));
                }
            }
            if (queueListView != null) {
                queueListView.setItems(FXCollections.observableArrayList(items));
            }
        } else if (currentViewType == ViewType.MUSIC) {
            ObservableList<Track> trackList = javafx.collections.FXCollections.observableArrayList(
                    LibrarySearchService.filterTracks(Library.getInstance().getTracks(), searchQuery));
            if (trackTableView != null)
                trackTableView.setItems(trackList);
        }

        restoreSortState(sortState);
    }

    private TableViewSortState captureSortState() {
        if (currentViewType == ViewType.PLAYLISTS && playlistTableView != null) {
            return new TableViewSortState(new ArrayList<>(playlistTableView.getSortOrder()));
        } else if (trackTableView != null) {
            return new TableViewSortState(new ArrayList<>(trackTableView.getSortOrder()));
        }
        return new TableViewSortState(List.of());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void restoreSortState(TableViewSortState sortState) {
        if (currentViewType == ViewType.PLAYLISTS && playlistTableView != null) {
            playlistTableView.getSortOrder().setAll((List) sortState.sortColumns);
        } else if (trackTableView != null) {
            trackTableView.getSortOrder().setAll((List) sortState.sortColumns);
        }
    }

    private record TableViewSortState(List<TableColumn<?, ?>> sortColumns) {
    }

    /**
     * Gestisce il testo segnaposto della tabella quando questa è vuota,
     * fornendo suggerimenti contestuali all'utente.
     */
    private Label createPlaceholderLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(420);
        label.setStyle("-fx-alignment: center; -fx-text-alignment: center; -fx-text-fill: #666666; -fx-padding: 16;");
        return label;
    }

    private void updateTablePlaceholder() {
        String placeholderText;

        if (!searchQuery.isBlank()) {
            placeholderText = "Nessun risultato per \"" + searchQuery.trim() + "\".";
        } else if (currentViewType == ViewType.PLAYLISTS) {
            placeholderText = "Non ci sono playlist. Clicca \"Nuova playlist\" per crearne una.";
        } else if (currentOpenedPlaylist != null) {
            placeholderText = "Questa playlist non contiene brani. Clicca \"Aggiungi brano\" per inserirne uno.";
        } else if (currentViewType == ViewType.QUEUE) {
            placeholderText = "Non ci sono brani in coda. Clicca \"Aggiungi brano\" nella Libreria musicale per popolarla.";
        } else {
            placeholderText = "Non ci sono brani. Clicca \"Aggiungi brano\" per inserirne uno.";
        }

        if (trackTableView != null)
            trackTableView.setPlaceholder(createPlaceholderLabel(placeholderText));
        if (playlistTableView != null)
            playlistTableView.setPlaceholder(createPlaceholderLabel(placeholderText));
        if (queueListView != null)
            queueListView.setPlaceholder(createPlaceholderLabel(placeholderText));
    }

    private void showOnlyTrackTable() {
        if (trackTableView != null) {
            trackTableView.setVisible(true);
            trackTableView.setManaged(true);
        }
        if (playlistTableView != null) {
            playlistTableView.setVisible(false);
            playlistTableView.setManaged(false);
        }
    }

    private void showOnlyPlaylistTable() {
        if (playlistTableView != null) {
            playlistTableView.setVisible(true);
            playlistTableView.setManaged(true);
        }
        if (trackTableView != null) {
            trackTableView.setVisible(false);
            trackTableView.setManaged(false);
        }
    }

    // Componenti iniettati da FXML

    /** Controller della barra laterale (iniettato tramite fx:include) */
    @FXML
    private SidebarController sidebarController;

    /** Controller della barra del player (iniettato tramite fx:include) */
    @FXML
    private PlayerController playerBarController;

    /** Nodo radice della vista Home (iniettato tramite fx:include) */
    @FXML
    private Node homeView;

    /** Controller della vista Home (iniettato tramite fx:include) */
    @FXML
    private HomeController homeViewController;

    @FXML
    private Label viewTitleLabel;
    @FXML
    private Button actionButton;
    @FXML
    private Button undoButton;
    @FXML
    private HBox controlsBar;
    @FXML
    private Button playPlaylistButton;
    @FXML
    private ToggleButton reorderButton;

    /** Tabella principale per la visualizzazione di Track o Playlist */
    @FXML
    private TableView<Track> trackTableView;
    @FXML
    private TableView<Playlist> playlistTableView;

    private ViewType currentViewType = ViewType.HOME;
    @FXML
    private TableColumn<?, ?> titleColumn;
    @FXML
    private TableColumn<?, ?> artistColumn;
    @FXML
    private TableColumn<?, ?> albumColumn;
    @FXML
    private TableColumn<?, ?> yearColumn;
    @FXML
    private TableColumn<?, ?> genreColumn;
    @FXML
    private TableColumn<?, ?> durationColumn;
    @FXML
    private TableColumn<?, ?> tagColumn;

    /**
     * Metodo di inizializzazione chiamato automaticamente da JavaFX.
     * Configura i listener, i menu contestuali e le callback tra controller.
     */
    @FXML
    public void initialize() {
        Library.getInstance().attach(this);
        PlaybackManager.getInstance().attach(this);

        sidebarController.setOnNavigate(this::handleNavigate);
        sidebarController.setOnSearchQueryChange(this::handleSearchQueryChange);
        playerBarController.setOnPlayPauseClicked(this::handlePlayPauseAction);
        playerBarController.setOnPlayerStateChanged(this::syncTableSelection);

        if (trackTableView != null)
            trackTableView.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> updatePlayPlaylistButtonState());
        if (playlistTableView != null)
            playlistTableView.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> updatePlayPlaylistButtonState());

        if (reorderButton != null) {
            reorderButton.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (trackTableView != null) {
                    if (selected) {
                        if (!trackTableView.getStyleClass().contains("reorder-mode")) {
                            trackTableView.getStyleClass().add("reorder-mode");
                        }
                    } else {
                        trackTableView.getStyleClass().remove("reorder-mode");
                    }
                }
            });
        }

        setupQueueListView();
        ContextMenuManager.setupTrackContextMenu(trackTableView, this);
        ContextMenuManager.setupPlaylistContextMenu(playlistTableView, this);

        if (trackTableView != null)
            trackTableView.setOnMouseClicked(event -> {
                if (event.getClickCount() != 2 || event.getButton() != javafx.scene.input.MouseButton.PRIMARY) {
                    return;
                }

                Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
                if (!isClickOnSelectedTableRow(event, selectedTrack)) {
                    return;
                }

                if (selectedTrack != null) {
                    handleStartTrackPlayback(selectedTrack);
                }
            });

        if (playlistTableView != null)
            playlistTableView.setOnMouseClicked(event -> {
                if (event.getClickCount() != 2 || event.getButton() != javafx.scene.input.MouseButton.PRIMARY) {
                    return;
                }

                Playlist playlist = playlistTableView.getSelectionModel().getSelectedItem();
                if (!isClickOnSelectedTableRow(event, playlist)) {
                    return;
                }

                if (currentOpenedPlaylist == null && currentViewType == ViewType.PLAYLISTS && playlist != null) {
                    openPlaylistDetail(playlist);
                }
            });

        if (undoButton != null) {
            undoButton.setDisable(!UndoManager.getInstance().canUndo());
        }
        // Inizializza la vista iniziale come "Musica"
        handleMusicLibraryAction();
        updatePlayerUI();

        if (actionButton != null) it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(actionButton);
        if (undoButton != null) it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(undoButton);
        if (playPlaylistButton != null) it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(playPlaylistButton);
        if (reorderButton != null) it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(reorderButton);
    }

    private boolean isClickOnSelectedTableRow(MouseEvent event, Object selectedItem) {
        Node node = event.getPickResult().getIntersectedNode();
        while (node != null) {
            if (node instanceof TableRow<?> row) {
                return row.getItem() != null && row.getItem() == selectedItem;
            }
            node = node.getParent();
        }
        return false;
    }

    private void setupQueueListView() {
        if (queueListView == null)
            return;
        queueListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(QueueItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.parentPlayable instanceof Playlist && item.track == null) {
                        Playlist p = (Playlist) item.parentPlayable;
                        boolean expanded = expandedPlaylists.contains(p);
                        String indicator = expanded ? "[-]" : "[+]";
                        setText(indicator + " " + p.getTitle() + " (" + p.getTrackCount() + " brani)");
                        setStyle("-fx-font-weight: bold;");
                    } else if (item.parentPlayable instanceof Playlist && item.track != null) {
                        setText("    " + item.track.getTitle() + " - " + item.track.getAuthor() + " ("
                                + formatDuration(item.track.getDuration()) + ")");
                        setStyle("-fx-font-weight: normal;");
                    } else if (item.parentPlayable instanceof Track) {
                        Track t = (Track) item.parentPlayable;
                        setText(t.getTitle() + " - " + t.getAuthor() + " (" + formatDuration(t.getDuration()) + ")");
                        setStyle("-fx-font-weight: normal;");
                    }
                }
            }
        });

        ContextMenu queueContextMenu = new ContextMenu();
        MenuItem removeFromQueueItem = new MenuItem("Rimuovi dalla coda");
        removeFromQueueItem.setOnAction(e -> {
            QueueItem selected = queueListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                PlaybackManager.getInstance().removeFromQueue(selected.queueIndex);
                refreshTableData();
                updatePlayerUI();
                updateTablePlaceholder();
            }
        });
        queueContextMenu.getItems().add(removeFromQueueItem);
        queueListView.setContextMenu(queueContextMenu);

        queueListView.setOnMouseClicked(event -> {
            QueueItem selected = queueListView.getSelectionModel().getSelectedItem();
            if (selected == null)
                return;
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && event.getClickCount() == 1
                    && selected.parentPlayable instanceof Playlist && selected.track == null) {
                Playlist p = (Playlist) selected.parentPlayable;
                if (expandedPlaylists.contains(p))
                    expandedPlaylists.remove(p);
                else
                    expandedPlaylists.add(p);
                refreshTableData();
            } else if (event.getClickCount() == 2) {
                if (selected.track != null) {
                    int trackIdx = selected.parentPlayable.getTracks().indexOf(selected.track);
                    PlaybackManager.getInstance().setCurrentIndices(selected.queueIndex, trackIdx);
                    // forcePlayCurrent: ferma l'audio corrente e avvia il brano dall'inizio (slider
                    // a 0)
                    PlaybackManager.getInstance().forcePlayCurrent();
                    updatePlayerUI();
                } else if (selected.parentPlayable instanceof Playlist
                        && !selected.parentPlayable.getTracks().isEmpty()) {
                    PlaybackManager.getInstance().setCurrentIndices(selected.queueIndex, 0);
                    PlaybackManager.getInstance().forcePlayCurrent();
                    updatePlayerUI();
                }
            }
        });
    }

    @Override
    public void onEditTrack(Track track) {
        if (track != null) {
            currentEditingTrack = track;
            openAddTrackView();
        }
    }

    /**
     * Gestisce l'eliminazione di una traccia dalla libreria con conferma
     * dell'utente.
     */
    @Override
    public void onDeleteTrack(Track selectedTrack) {
        if (selectedTrack == null)
            return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare il brano selezionato?");
        confirmAlert.setContentText("Stai per eliminare \"" + selectedTrack.getTitle()
                + "\" dalla libreria.\nVerrà rimosso anche da tutte le playlist.");

        ThemeManager.getInstance().applyActiveThemeToScene(confirmAlert.getDialogPane().getScene());
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Command removeCmd = new RemoveTrackCommand(Library.getInstance(), selectedTrack);
            UndoManager.getInstance().executeCommand(removeCmd);
        }
    }

    /**
     * Utilizza la classe RemoveElementFromPlaylistCommand passandogli la plylist
     * corrente e il brano selezionato
     */
    @Override
    public void onRemoveFromPlaylist(Track selectedTrack, Playlist currentOpenedPlaylist) {
        if (selectedTrack != null && currentOpenedPlaylist instanceof ManualPlaylist manualPlaylist) {
            Command removeCmd = new RemoveElementFromPlaylistCommand(manualPlaylist, selectedTrack);
            UndoManager.getInstance().executeCommand(removeCmd);
        }
    }

    @Override
    public void onEditPlaylist(Playlist selectedPlaylist) {
        if (selectedPlaylist == null)
            return;
        dialogService.openEditPlaylistDialog(selectedPlaylist);
    }

    /**
     * Elimina una playlist dalla libreria con conferma.
     */
    @Override
    public void onDeletePlaylist(Playlist selectedPlaylist) {
        if (selectedPlaylist == null)
            return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare la playlist selezionata?");
        confirmAlert.setContentText("I brani resteranno disponibili nella libreria musicale.");

        ThemeManager.getInstance().applyActiveThemeToScene(confirmAlert.getDialogPane().getScene());
        if (confirmAlert.showAndWait().filter(r -> r == ButtonType.OK).isPresent()) {
            Command deletePlaylistCmd = new RemovePlaylistCommand(Library.getInstance(), selectedPlaylist);
            UndoManager.getInstance().executeCommand(deletePlaylistCmd);
            showPlaylistColumns();
        }
    }

    /**
     * Gestisce gli eventi di navigazione provenienti dalla barra laterale.
     */
    private void handleNavigate(String viewId) {
        if (null != viewId)
            switch (viewId) {
                case "Home" -> handleHomeAction();
                case "Musica" -> handleMusicLibraryAction();
                case "Coda" -> handlePlayQueueAction();
                case "Playlist" -> handlePlaylistAction();
                default -> {
                }
            }
    }

    @FXML
    private void handleUndoAction() {
        if (UndoManager.getInstance().canUndo()) {
            UndoManager.getInstance().undo();
        }
    }

    private void handleSearchQueryChange(String query) {
        searchQuery = query == null ? "" : query;
        if (currentViewType == ViewType.PLAYLISTS) {
            showPlaylistColumns();
        } else {
            refreshTableData();
        }
        updateTablePlaceholder();
    }

    /** Configura la vista per mostrare la schermata Home con le statistiche. */
    private void handleHomeAction() {
        currentOpenedPlaylist = null;
        currentViewType = ViewType.HOME;
        viewTitleLabel.setText("Home");
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        controlsBar.setVisible(false);
        controlsBar.setManaged(false);
        updatePlayPlaylistButtonState();
        if (reorderButton != null) {
            reorderButton.setVisible(false);
            reorderButton.setManaged(false);
            reorderButton.setSelected(false);
        }

        // Nascondi tabella e coda, mostra pannello Home
        if (trackTableView != null) {
            trackTableView.setVisible(false);
            trackTableView.setManaged(false);
        }
        if (playlistTableView != null) {
            playlistTableView.setVisible(false);
            playlistTableView.setManaged(false);
        }
        if (queueListView != null) {
            queueListView.setVisible(false);
            queueListView.setManaged(false);
        }
        if (homeView != null) {
            homeView.setVisible(true);
            homeView.setManaged(true);
        }
        if (homeViewController != null) {
            homeViewController.refreshStats();
        }
    }

    /** Configura la vista per mostrare la libreria musicale completa. */
    private void handleMusicLibraryAction() {
        hideHomePanel();
        currentOpenedPlaylist = null;
        currentViewType = ViewType.MUSIC;
        viewTitleLabel.setText("Musica");
        actionButton.setText("Aggiungi brano");
        actionButton.setVisible(true);
        actionButton.setManaged(true);
        controlsBar.setVisible(true);
        controlsBar.setManaged(true);
        showSongsColumns();
    }

    /** Configura la vista per mostrare la coda di riproduzione. */
    private void handlePlayQueueAction() {
        hideHomePanel();
        currentOpenedPlaylist = null;
        currentViewType = ViewType.QUEUE;
        viewTitleLabel.setText("Coda di riproduzione");
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        controlsBar.setVisible(true);
        controlsBar.setManaged(true);
        showQueueColumns();
    }

    /** Configura la vista per mostrare l'elenco delle playlist (Master View). */
    private void handlePlaylistAction() {
        hideHomePanel();
        currentOpenedPlaylist = null;
        currentViewType = ViewType.PLAYLISTS;
        viewTitleLabel.setText("Playlist");
        actionButton.setText("Nuova playlist");
        actionButton.setVisible(true);
        actionButton.setManaged(true);
        controlsBar.setVisible(true);
        controlsBar.setManaged(true);
        showPlaylistColumns();
        updatePlayPlaylistButtonState();
    }

    /**
     * Nasconde il pannello Home e ripristina la visibilità della TableView
     * principale.
     * Da chiamare all'inizio di ogni metodo di navigazione che non sia la Home.
     */
    private void hideHomePanel() {
        if (reorderButton != null) {
            reorderButton.setVisible(false);
            reorderButton.setManaged(false);
            reorderButton.setSelected(false);
        }
        if (homeView != null) {
            homeView.setVisible(false);
            homeView.setManaged(false);
        }
        // Ripristina la visibilità della tabella principale se non era già visibile
        // Gestito separatamente da showOnlyTrackTable e showOnlyPlaylistTable
    }

    // Metodi per la configurazione dinamica delle colonne della TableView

    private void showQueueColumns() {
        if (trackTableView != null) {
            trackTableView.setVisible(false);
            trackTableView.setManaged(false);
        }
        if (playlistTableView != null) {
            playlistTableView.setVisible(false);
            playlistTableView.setManaged(false);
        }
        if (queueListView != null) {
            queueListView.setVisible(true);
            queueListView.setManaged(true);
        }
        updateTablePlaceholder();
        refreshTableData();
        updatePlayPlaylistButtonState();
    }

    @SuppressWarnings("unchecked")
    private void showSongsColumns() {
        if (queueListView != null) {
            queueListView.setVisible(false);
            queueListView.setManaged(false);
        }
        showOnlyTrackTable();
        if (trackTableView == null)
            return;
        trackTableView.getColumns().clear();

        TableColumn<Track, String> titleCol = new TableColumn<>("Titolo");
        titleCol.setPrefWidth(200);
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Track, String> artistCol = new TableColumn<>("Artista");
        artistCol.setPrefWidth(150);
        artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));

        TableColumn<Track, String> albumCol = new TableColumn<>("Album");
        albumCol.setPrefWidth(150);
        albumCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlbum()));

        TableColumn<Track, String> yearCol = new TableColumn<>("Anno");
        yearCol.setPrefWidth(80);
        yearCol.setCellValueFactory(data -> {
            Integer year = data.getValue().getYear();

            if (year == null) {
                return new SimpleStringProperty("");
            }

            return new SimpleStringProperty(String.valueOf(year));
        });

        TableColumn<Track, String> genreCol = new TableColumn<>("Genere");
        genreCol.setPrefWidth(120);
        genreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre()));

        TableColumn<Track, String> durationCol = new TableColumn<>("Durata");
        durationCol.setPrefWidth(80);
        durationCol.setCellValueFactory(data -> {
            int seconds = data.getValue().getDuration();
            return new SimpleStringProperty(String.format("%02d:%02d", seconds / 60, seconds % 60));
        });

        // Risoluzione conflitto: Reintroduzione colonna e cellFactory custom per i Tag
        TableColumn<Track, Set<Tag>> tagCol = new TableColumn<>("Tag");
        tagCol.setPrefWidth(180);
        tagCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTags()));
        tagCol.setCellFactory(new TagCellFactory());

        trackTableView.getColumns().addAll(titleCol, artistCol, albumCol, yearCol, genreCol, durationCol, tagCol);

        trackTableView.setRowFactory(new ReorderableTrackRowFactory(
                () -> reorderButton != null && reorderButton.isSelected(),
                () -> currentOpenedPlaylist,
                this::updatePlayerUI));

        updateTablePlaceholder();
        refreshTableData();
        updatePlayPlaylistButtonState();
    }

    @SuppressWarnings("unchecked")
    private void showPlaylistColumns() {
        if (queueListView != null) {
            queueListView.setVisible(false);
            queueListView.setManaged(false);
        }
        showOnlyPlaylistTable();
        if (playlistTableView == null)
            return;
        playlistTableView.getColumns().clear();
        TableColumn<Playlist, String> nameCol = createColumn("Nome Playlist", 300, p -> p.getTitle());
        TableColumn<Playlist, Integer> countCol = new TableColumn<>("Numero Brani");
        countCol.setPrefWidth(150);
        countCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTrackCount()).asObject());
        TableColumn<Playlist, String> durationCol = createColumn("Durata Totale", 150,
                p -> formatDuration(p.getDuration()));

        playlistTableView.getColumns().addAll(nameCol, countCol, durationCol);
        playlistTableView.setItems(javafx.collections.FXCollections.observableArrayList(
                LibrarySearchService.filterPlaylists(Library.getInstance().getPlaylists(), searchQuery)));
        updateTablePlaceholder();
        updatePlayPlaylistButtonState();
    }

    // Metodi di utilità per la creazione di componenti UI

    private <S> TableColumn<S, String> createColumn(String title, double width,
            java.util.function.Function<S, String> mapper) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return col;
    }

    private String formatDuration(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    /**
     * Naviga all'interno di una playlist specifica per mostrarne il contenuto.
     */
    private void openPlaylistDetail(Playlist playlist) {
        currentOpenedPlaylist = playlist;
        currentViewType = ViewType.PLAYLIST_DETAIL;
        viewTitleLabel.setText(playlist.getTitle());
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        if (playlist.isManuallyEditable() && reorderButton != null) {
            reorderButton.setVisible(true);
            reorderButton.setManaged(true);
            reorderButton.setSelected(false);
        } else if (reorderButton != null) {
            reorderButton.setVisible(false);
            reorderButton.setManaged(false);
            reorderButton.setSelected(false);
        }
        showSongsColumns();
        updatePlayPlaylistButtonState();
    }

    /**
     * Carica e visualizza la finestra modale per l'aggiunta o la modifica di una
     * traccia.
     */
    private void openAddTrackView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/views/addTrackView.fxml"));
            Parent root = loader.load();
            AddTrackController controller = loader.getController();
            controller.setOnTrackSaved(this);
            controller.initForm(currentEditingTrack);

            Stage stage = new Stage();
            stage.setTitle(currentEditingTrack == null ? "Aggiungi brano" : "Modifica brano");
            stage.setScene(new Scene(root));
            ThemeManager.getInstance().applyActiveThemeToScene(stage.getScene());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Resetta lo stato in modo da non precompilare form futuri erroneamente
            currentEditingTrack = null;
        } catch (IOException e) {
        }
    }

    /** Gestore del pulsante d'azione principale (contestuale alla vista). */
    @FXML
    private void handleActionBtnClick() {
        if (currentViewType == ViewType.MUSIC) {
            currentEditingTrack = null;
            openAddTrackView();
        } else if (currentViewType == ViewType.PLAYLISTS) {
            dialogService.openCreatePlaylistDialog();
        }
    }

    /** Aggiunge la traccia selezionata a una playlist scelta tramite dialogo. */
    @Override
    public void onAddTrackToPlaylist(Track selectedTrack) {
        if (selectedTrack == null)
            return;
        dialogService.openAddTrackToPlaylistDialog(selectedTrack);
    }

    /** Aggiunge la traccia selezionata alla coda di riproduzione corrente. */
    @Override
    public void onAddTrackToQueue(Track track) {
        if (track != null) {
            PlaybackManager.getInstance().addToQueue(track);
            refreshQueueViewIfVisible();
        }
    }

    /** Aggiunge l'intera playlist selezionata alla coda di riproduzione. */
    @Override
    public void onAddPlaylistToQueue(Playlist playlist) {
        if (playlist != null) {
            PlaybackManager.getInstance().addToQueue(playlist);
            refreshQueueViewIfVisible();
        }
    }

    private void refreshQueueViewIfVisible() {
        if (currentViewType == ViewType.QUEUE) {
            refreshTableData();
            updateTablePlaceholder();
        }
    }

    @Override
    public boolean isQueueView() {
        return currentViewType == ViewType.QUEUE;
    }

    @Override
    public Playlist getCurrentOpenedPlaylist() {
        return currentOpenedPlaylist;
    }

    /** Avvia la riproduzione immediata della playlist selezionata. */
    @FXML
    private void handlePlayPlaylistAction() {
        Playable playlistToPlay = currentOpenedPlaylist != null
                ? currentOpenedPlaylist
                : playlistTableView != null ? playlistTableView.getSelectionModel().getSelectedItem() : null;

        if (playlistToPlay != null && !playlistToPlay.getTracks().isEmpty()) {
            PlaybackManager.getInstance().play(playlistToPlay, false);
            updatePlayerUI();
        }
    }

    /**
     * Gestisce ESCLUSIVAMENTE il toggle Play/Pausa sul brano attualmente in
     * riproduzione.
     * NON tiene conto dell'elemento selezionato nella tabella o nella lista della
     * coda:
     * la selezione visiva è indipendente dallo stato di riproduzione.
     *
     * Se la coda è vuota e non c'è nessun brano in riproduzione, tenta di caricare
     * il primo brano disponibile dalla vista corrente come comportamento di
     * fallback.
     */
    private void handlePlayPauseAction() {
        PlaybackManager manager = PlaybackManager.getInstance();

        // Fallback: se la coda è completamente vuota, carica il primo brano disponibile
        // dalla vista corrente per consentire l'avvio iniziale della riproduzione.
        if (manager.getCurrentQueue().isEmpty()) {
            if (currentOpenedPlaylist != null && !currentOpenedPlaylist.getTracks().isEmpty()) {
                manager.selectAndLoadTrack(currentOpenedPlaylist.getTracks().get(0), List.of(currentOpenedPlaylist));
            } else if (!Library.getInstance().getTracks().isEmpty()) {
                Track firstTrack = Library.getInstance().getTracks().get(0);
                manager.selectAndLoadTrack(firstTrack, List.of(firstTrack));
            }
        }

        // Delega allo State pattern: PlayingState → pausa, PausedState → riprendi,
        // StoppedState → avvia.
        // L'elemento selezionato nella UI non influisce su questa azione.
        manager.pressPlay();
        updatePlayerUI();
    }

    /**
     * Avvia la riproduzione di una traccia specifica scelta dall'utente (es. doppio
     * click sulla tabella).
     * A differenza di {@link #handlePlayPauseAction()}, questo metodo imposta
     * il contesto di riproduzione basandosi sulla traccia selezionata.
     *
     * @param selectedTrack La traccia da riprodurre immediatamente.
     */
    private void handleStartTrackPlayback(Track selectedTrack) {
        PlaybackManager manager = PlaybackManager.getInstance();

        if (currentOpenedPlaylist != null) {
            // Da una playlist aperta: il contesto è l'intera playlist
            manager.selectAndLoadTrack(selectedTrack, List.of(currentOpenedPlaylist));
        } else {
            // Dalla libreria: avvia solo il brano selezionato
            manager.selectAndLoadTrack(selectedTrack, List.of(selectedTrack));
        }

        manager.forcePlayCurrent();
        updatePlayerUI();
    }

    /**
     * Aggiorna la barra del player e sincronizza la selezione visiva nella tabella.
     */
    private void updatePlayerUI() {
        playerBarController.updatePlayerUI();
        syncTableSelection();
    }

    /**
     * Sincronizza l'elemento selezionato nella tabella con la traccia
     * effettivamente in riproduzione.
     */
    private void syncTableSelection() {
        Track currentTrack = PlaybackManager.getInstance().getCurrentTrack();
        if (currentTrack != null) {
            if (currentViewType == ViewType.QUEUE && queueListView != null && !queueListView.getItems().isEmpty()) {
                ObservableList<QueueItem> items = queueListView.getItems();
                int currentQueueIndex = PlaybackManager.getInstance().getCurrentPlayableIndex();
                for (int i = 0; i < items.size(); i++) {
                    QueueItem item = items.get(i);
                    if (item.queueIndex == currentQueueIndex && item.track != null && item.track.equals(currentTrack)) {
                        final int index = i;
                        javafx.application.Platform.runLater(() -> {
                            queueListView.getSelectionModel().select(index);
                            queueListView.scrollTo(index);
                        });
                        return;
                    }
                }
            } else if (trackTableView != null && !trackTableView.getItems().isEmpty()) {
                ObservableList<Track> items = trackTableView.getItems();

                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).equals(currentTrack)) {
                        final int index = i;
                        javafx.application.Platform.runLater(() -> {
                            trackTableView.getSelectionModel().select(index);
                            trackTableView.scrollTo(index);
                        });
                        return;
                    }
                }
            }
        }
    }

}