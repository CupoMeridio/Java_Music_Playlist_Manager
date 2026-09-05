package it.unisa.java_music_playlist_manager.controllers;

import it.unisa.java_music_playlist_manager.AddTrackController;
import it.unisa.java_music_playlist_manager.HomeController;
import it.unisa.java_music_playlist_manager.PlayerController;
import it.unisa.java_music_playlist_manager.ReorderableTrackRowFactory;
import it.unisa.java_music_playlist_manager.SidebarController;
import it.unisa.java_music_playlist_manager.ThemeManager;
import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Observer;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.TrackSortOption;
import it.unisa.java_music_playlist_manager.model.command.UndoManager;
import it.unisa.java_music_playlist_manager.services.LibrarySearchService;
import it.unisa.java_music_playlist_manager.view.ContextMenuManager;
import it.unisa.java_music_playlist_manager.view.PlaylistDialogService;
import it.unisa.java_music_playlist_manager.view.TableSortStateMemento;
import it.unisa.java_music_playlist_manager.view.ViewType;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.GridView;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.io.IOException;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.geometry.Side;
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
public class PrimaryViewController implements Observer {

    /**
     * Traccia attualmente in fase di modifica (usata per popolare il form di
     * editing)
     */
    private Track currentEditingTrack = null;

    private final PlaylistDialogService dialogService = new PlaylistDialogService(this::refreshTableData);

    private String searchQuery = "";

    @FXML
    private ListView<it.unisa.java_music_playlist_manager.view.QueueViewManager.QueueItem> queueListView;

    public ListView<it.unisa.java_music_playlist_manager.view.QueueViewManager.QueueItem> getQueueListView() {
        return queueListView;
    }

    private it.unisa.java_music_playlist_manager.view.QueueViewManager queueViewManager;
    private it.unisa.java_music_playlist_manager.view.TrackCardGridManager trackCardGridManager;

    private it.unisa.java_music_playlist_manager.controllers.PlaybackUICoordinator playbackCoordinator;
    private it.unisa.java_music_playlist_manager.controllers.LibraryActionsHandler libraryActionsHandler;
    private it.unisa.java_music_playlist_manager.controllers.ViewNavigationController viewNavigationController;

    /**
     * Metodo del pattern Observer.
     * Chiamato quando la Library o il PlaybackManager notificano un cambiamento.
     * Forza il refresh della tabella e la sincronizzazione della selezione.
     */
    @Override
    public void update() {
        javafx.application.Platform.runLater(() -> {
            refreshTableData();
            if (playlistTableView != null)
                playlistTableView.refresh();
            if (trackTableView != null)
                trackTableView.refresh();
            if (queueListView != null)
                queueListView.refresh();
            if (playbackCoordinator != null)
                playbackCoordinator.syncTableSelection();
            updatePlayPlaylistButtonState();

            if (undoButton != null) {
                undoButton.setDisable(!UndoManager.getInstance().canUndo());
            }
        });
    }

    private void updatePlayPlaylistButtonState() {
        if (playPlaylistButton == null)
            return;

        boolean disable = true;
        if (viewNavigationController != null
                && viewNavigationController.getCurrentViewType() == ViewType.PLAYLIST_DETAIL) {
            if (viewNavigationController.getCurrentOpenedPlaylist() != null) {
                disable = viewNavigationController.getCurrentOpenedPlaylist().getTracks().isEmpty();
            }
        } else if (playlistTableView != null) {
            Playlist selected = playlistTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                disable = selected.getTracks().isEmpty();
            }
        }
        playPlaylistButton.setDisable(disable);
    }

    /**
     * Aggiorna i dati mostrati nella TableView in base alla vista corrente (Musica,
     * Coda o Playlist aperta).
     */
    private void refreshTableData() {
        TableSortStateMemento sortState = captureSortState();

        TrackSortOption sortOption = (sortComboBox != null && sortComboBox.getValue() != null)
                ? sortComboBox.getValue()
                : TrackSortOption.INSERTION_ORDER;

        if (viewNavigationController != null && viewNavigationController.getCurrentOpenedPlaylist() != null) {
            List<Track> filtered = LibrarySearchService
                    .filterTracks(viewNavigationController.getCurrentOpenedPlaylist().getTracks(), searchQuery);
            List<Track> sorted = sortOption.sort(filtered);
            ObservableList<Track> trackList = javafx.collections.FXCollections.observableArrayList(sorted);
            if (trackTableView != null)
                trackTableView.setItems(trackList);
            if (isCardView && trackCardGridManager != null)
                trackCardGridManager.updateTrackCards(trackList);
        } else if (viewNavigationController != null
                && viewNavigationController.getCurrentViewType() == ViewType.QUEUE) {
            if (queueViewManager != null) {
                queueViewManager.refreshQueue();
            }
        } else if (viewNavigationController != null
                && viewNavigationController.getCurrentViewType() == ViewType.PLAYLISTS) {
            if (playlistTableView != null) {
                playlistTableView.setItems(javafx.collections.FXCollections.observableArrayList(
                        LibrarySearchService.filterPlaylists(Library.getInstance().getPlaylists(), searchQuery)));
            }
        } else if (viewNavigationController != null
                && viewNavigationController.getCurrentViewType() == ViewType.MUSIC) {
            List<Track> filtered = LibrarySearchService.filterTracks(Library.getInstance().getTracks(), searchQuery);
            List<Track> sorted = sortOption.sort(filtered);
            ObservableList<Track> trackList = javafx.collections.FXCollections.observableArrayList(sorted);
            if (trackTableView != null)
                trackTableView.setItems(trackList);
            if (isCardView && trackCardGridManager != null)
                trackCardGridManager.updateTrackCards(trackList);
        }

        restoreSortState(sortState);
    }

    private TableSortStateMemento captureSortState() {
        if (viewNavigationController != null && viewNavigationController.getCurrentViewType() == ViewType.PLAYLISTS
                && playlistTableView != null) {
            return new TableSortStateMemento(playlistTableView);
        } else if (trackTableView != null) {
            return new TableSortStateMemento(trackTableView);
        }
        return new TableSortStateMemento(null);
    }

    private void restoreSortState(TableSortStateMemento sortState) {
        if (viewNavigationController != null && viewNavigationController.getCurrentViewType() == ViewType.PLAYLISTS
                && playlistTableView != null) {
            sortState.restore(playlistTableView);
        } else if (trackTableView != null) {
            sortState.restore(trackTableView);
        }
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
        } else if (viewNavigationController != null
                && viewNavigationController.getCurrentViewType() == ViewType.PLAYLISTS) {
            placeholderText = "Non ci sono playlist. Clicca \"Nuova playlist\" per crearne una.";
        } else if (viewNavigationController != null && viewNavigationController.getCurrentOpenedPlaylist() != null) {
            placeholderText = "Questa playlist non contiene brani. Clicca \"Aggiungi brano\" per inserirne uno.";
        } else if (viewNavigationController != null
                && viewNavigationController.getCurrentViewType() == ViewType.QUEUE) {
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
        if (playlistTableView != null) {
            playlistTableView.setVisible(false);
            playlistTableView.setManaged(false);
        }
        boolean isTrackSection = viewNavigationController != null &&
                (viewNavigationController.getCurrentViewType() == ViewType.MUSIC
                        || viewNavigationController.getCurrentViewType() == ViewType.PLAYLIST_DETAIL);
        if (viewToggleButton != null) {
            viewToggleButton.setVisible(isTrackSection);
            viewToggleButton.setManaged(isTrackSection);
        }
        if (isCardView) {
            if (trackTableView != null) {
                trackTableView.setVisible(false);
                trackTableView.setManaged(false);
            }
            if (trackCardGridView != null) {
                trackCardGridView.setVisible(true);
                trackCardGridView.setManaged(true);
            }
        } else {
            if (trackCardGridView != null) {
                trackCardGridView.setVisible(false);
                trackCardGridView.setManaged(false);
            }
            if (trackTableView != null) {
                trackTableView.setVisible(true);
                trackTableView.setManaged(true);
            }
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
        if (trackCardGridView != null) {
            trackCardGridView.setVisible(false);
            trackCardGridView.setManaged(false);
        }
        if (viewToggleButton != null) {
            viewToggleButton.setVisible(false);
            viewToggleButton.setManaged(false);
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
    @FXML
    private ComboBox<TrackSortOption> sortComboBox;
    @FXML
    private ToggleButton viewToggleButton;
    @FXML
    private GridView<Track> trackCardGridView;
    private boolean isCardView = false;

    /** Tabella principale per la visualizzazione di Track o Playlist */
    @FXML
    private TableView<Track> trackTableView;
    @FXML
    private TableView<Playlist> playlistTableView;

    /**
     * Metodo di inizializzazione chiamato automaticamente da JavaFX.
     * Configura i listener, i menu contestuali e le callback tra controller.
     */
    @FXML
    public void initialize() {
        Library.getInstance().attach(this);
        PlaybackManager.getInstance().attach(this);

        viewNavigationController = new it.unisa.java_music_playlist_manager.controllers.ViewNavigationController(
                viewTitleLabel, actionButton, controlsBar,
                undoButton, playPlaylistButton, sortComboBox,
                viewToggleButton, reorderButton,
                trackTableView, trackCardGridView,
                playlistTableView, queueListView,
                homeView, homeViewController,
                this::showSongsColumns, this::showQueueColumns,
                this::showPlaylistColumns, this::updatePlayPlaylistButtonState);

        playbackCoordinator = new it.unisa.java_music_playlist_manager.controllers.PlaybackUICoordinator(
                () -> viewNavigationController.getCurrentOpenedPlaylist(),
                () -> playlistTableView != null ? playlistTableView.getSelectionModel().getSelectedItem() : null,
                this::getVisibleLibraryTracks,
                () -> viewNavigationController.getCurrentViewType(),
                playerBarController,
                queueListView,
                trackTableView);

        libraryActionsHandler = new it.unisa.java_music_playlist_manager.controllers.LibraryActionsHandler(
                dialogService,
                () -> viewNavigationController.getCurrentViewType(),
                () -> viewNavigationController.getCurrentOpenedPlaylist(),
                this::refreshQueueViewIfVisible,
                track -> {
                    if (track != null) {
                        currentEditingTrack = track;
                        openAddTrackView();
                    }
                });

        sidebarController.setOnNavigate(viewNavigationController::handleNavigate);
        sidebarController.setOnSearchQueryChange(this::handleSearchQueryChange);
        playerBarController.setOnPlayPauseClicked(playbackCoordinator::handlePlayPauseAction);
        playerBarController.setOnPlayerStateChanged(playbackCoordinator::syncTableSelection);

        if (trackTableView != null)
            trackTableView.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> {
                        updatePlayPlaylistButtonState();
                    });
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

        // Inizializzazione QueueViewManager
        queueViewManager = new it.unisa.java_music_playlist_manager.view.QueueViewManager(queueListView, () -> {
            playbackCoordinator.updatePlayerUI();
            updateTablePlaceholder();
            // Aggiorna visivamente anche le altre tabelle qualora vi sia un incrocio
            if (trackTableView != null)
                trackTableView.refresh();
        });
        trackCardGridManager = new it.unisa.java_music_playlist_manager.view.TrackCardGridManager(
                trackCardGridView,
                trackTableView,
                playbackCoordinator::handleStartTrackPlayback,
                this::updatePlayPlaylistButtonState);

        ContextMenuManager.setupPlaylistContextMenu(playlistTableView, libraryActionsHandler);

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
                    playbackCoordinator.handleStartTrackPlayback(selectedTrack);
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

                if (viewNavigationController.getCurrentOpenedPlaylist() == null
                        && viewNavigationController.getCurrentViewType() == ViewType.PLAYLISTS && playlist != null) {
                    viewNavigationController.openPlaylistDetail(playlist);
                }
            });

        if (undoButton != null) {
            undoButton.setDisable(!UndoManager.getInstance().canUndo());
        }
        // Inizializza la vista iniziale come "Musica"
        viewNavigationController.handleMusicLibraryAction();
        playbackCoordinator.updatePlayerUI();

        if (actionButton != null)
            it.unisa.java_music_playlist_manager.view.SnapMotion.attach(actionButton);
        if (undoButton != null)
            it.unisa.java_music_playlist_manager.view.SnapMotion.attach(undoButton);
        if (playPlaylistButton != null)
            it.unisa.java_music_playlist_manager.view.SnapMotion.attach(playPlaylistButton);
        if (reorderButton != null)
            it.unisa.java_music_playlist_manager.view.SnapMotion.attach(reorderButton);
        if (sortComboBox != null) {
            sortComboBox.setFocusTraversable(false);
            sortComboBox.getItems().setAll(TrackSortOption.values());
            sortComboBox.setValue(TrackSortOption.INSERTION_ORDER);
            sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (trackTableView != null) {
                    trackTableView.getSortOrder().clear();
                }
                refreshTableData();
                javafx.application.Platform.runLater(() -> {
                    if (controlsBar != null) {
                        controlsBar.requestFocus();
                    }
                });
            });

            sortComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
                if (!isNowShowing && controlsBar != null) {
                    javafx.application.Platform.runLater(controlsBar::requestFocus);
                }
            });
        }

        // Registra Ctrl+Z come shortcut globale sulla scena non appena essa è
        // disponibile
        if (undoButton != null) {
            undoButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.getAccelerators().put(
                            new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                            this::handleUndoAction);
                }
            });
        }
    }

    private boolean isClickOnSelectedTableRow(MouseEvent event, Object selectedItem) {
        return ContextMenuManager.isClickOnSelectedTableRow(event, selectedItem);
    }

    private void refreshQueueViewIfVisible() {
        if (viewNavigationController != null && viewNavigationController.getCurrentViewType() == ViewType.QUEUE) {
            refreshTableData();
            updateTablePlaceholder();
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
        refreshTableData();
        updateTablePlaceholder();
    }

    // Metodi per la configurazione dinamica delle colonne della TableView

    private void showQueueColumns() {
        if (trackTableView != null) {
            trackTableView.setVisible(false);
            trackTableView.setManaged(false);
        }
        if (trackCardGridView != null) {
            trackCardGridView.setVisible(false);
            trackCardGridView.setManaged(false);
        }
        if (playlistTableView != null) {
            playlistTableView.setVisible(false);
            playlistTableView.setManaged(false);
        }
        if (queueListView != null) {
            queueListView.setVisible(true);
            queueListView.setManaged(true);
        }
        if (viewToggleButton != null) {
            viewToggleButton.setVisible(false);
            viewToggleButton.setManaged(false);
        }
        afterViewSwitch();
    }

    private void showSongsColumns() {
        if (queueListView != null) {
            queueListView.setVisible(false);
            queueListView.setManaged(false);
        }
        showOnlyTrackTable();
        if (trackTableView == null)
            return;
        trackTableView.getColumns().clear();

        List<TableColumn<Track, ?>> columns = it.unisa.java_music_playlist_manager.view.TableColumnFactory
                .createTrackColumns(playbackCoordinator::handleStartTrackPlayback);
        trackTableView.getColumns().addAll(columns);

        trackTableView.setRowFactory(new ReorderableTrackRowFactory(
                () -> reorderButton != null && reorderButton.isSelected(),
                () -> viewNavigationController.getCurrentOpenedPlaylist(),
                playbackCoordinator::updatePlayerUI,
                libraryActionsHandler));

        afterViewSwitch();
    }

    private void showPlaylistColumns() {
        if (queueListView != null) {
            queueListView.setVisible(false);
            queueListView.setManaged(false);
        }
        showOnlyPlaylistTable();
        if (playlistTableView == null)
            return;
        playlistTableView.getColumns().clear();

        List<TableColumn<Playlist, ?>> columns = it.unisa.java_music_playlist_manager.view.TableColumnFactory
                .createPlaylistColumns();
        playlistTableView.getColumns().addAll(columns);
        afterViewSwitch();
    }

    /**
     * Aggiorna lo stato dei componenti grafici dopo la riconfigurazione
     * o il cambio delle colonne della tabella (placeholder, refresh dati, stato
     * bottoni).
     */
    private void afterViewSwitch() {
        updateTablePlaceholder();
        refreshTableData();
        updatePlayPlaylistButtonState();
    }

    // Metodi di utilità rimossi poiché spostati nella TableColumnFactory

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
            // Vincoli di sicurezza: corrispondono ai prefWidth/prefHeight dichiarati in
            // addTrackView.fxml
            stage.setMinWidth(400);
            stage.setMinHeight(500);
            stage.showAndWait();

            // Resetta lo stato in modo da non precompilare form futuri erroneamente
            currentEditingTrack = null;
        } catch (IOException e) {
        }
    }

    /** Gestore del pulsante d'azione principale (contestuale alla vista). */
    @FXML
    private void handleActionBtnClick() {
        if (viewNavigationController != null && viewNavigationController.getCurrentViewType() == ViewType.MUSIC) {
            currentEditingTrack = null;
            ContextMenu contextMenu = new ContextMenu();

            MenuItem singleItem = new MenuItem("Aggiungi singolo brano (Modifica metadati)");
            singleItem.setOnAction(e -> openAddTrackView());

            MenuItem multiItem = new MenuItem("Aggiungi più file...");
            multiItem.setOnAction(e -> new it.unisa.java_music_playlist_manager.controllers.TrackImportController(
                    actionButton.getScene().getWindow()).handleAddFiles());

            MenuItem folderItem = new MenuItem("Aggiungi intera cartella...");
            folderItem.setOnAction(e -> new it.unisa.java_music_playlist_manager.controllers.TrackImportController(
                    actionButton.getScene().getWindow()).handleAddFolder());

            contextMenu.getItems().addAll(singleItem, multiItem, folderItem);
            contextMenu.show(actionButton, Side.BOTTOM, 0, 0);
        } else if (viewNavigationController != null
                && viewNavigationController.getCurrentViewType() == ViewType.PLAYLISTS) {
            dialogService.openCreatePlaylistDialog();
        }
    }

    /** Avvia la riproduzione immediata della playlist selezionata. */
    @FXML
    private void handlePlayPlaylistAction() {
        playbackCoordinator.handlePlayPlaylistAction();
    }

    private List<Track> getVisibleLibraryTracks() {
        if (trackTableView != null && !trackTableView.getItems().isEmpty()) {
            return new ArrayList<>(trackTableView.getItems());
        }
        return new ArrayList<>(Library.getInstance().getTracks());
    }

    @FXML
    private void handleViewToggleAction() {
        isCardView = viewToggleButton.isSelected();
        showOnlyTrackTable();
        refreshTableData();
    }

}
