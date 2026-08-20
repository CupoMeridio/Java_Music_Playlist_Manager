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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.GridView;
import org.controlsfx.control.GridCell;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import it.unisa.java_music_playlist_manager.model.Observer;
import java.io.IOException;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.TrackSortOption;
import it.unisa.java_music_playlist_manager.model.ViewType;
import it.unisa.java_music_playlist_manager.model.AddMultipleTracksCommand;
import java.util.Set;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.geometry.Side;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
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

    private final PlaylistDialogService dialogService = new PlaylistDialogService(this::refreshTableData);

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
            refreshTableData();
            if (playlistTableView != null)
                playlistTableView.refresh();
            if (trackTableView != null)
                trackTableView.refresh();
            if (queueListView != null)
                queueListView.refresh();
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

        TrackSortOption sortOption = (sortComboBox != null && sortComboBox.getValue() != null)
                ? sortComboBox.getValue()
                : TrackSortOption.INSERTION_ORDER;

        if (currentOpenedPlaylist != null) {
            List<Track> filtered = LibrarySearchService.filterTracks(currentOpenedPlaylist.getTracks(), searchQuery);
            List<Track> sorted = sortOption.sort(filtered);
            ObservableList<Track> trackList = javafx.collections.FXCollections.observableArrayList(sorted);
            if (trackTableView != null)
                trackTableView.setItems(trackList);
            if (isCardView)
                updateTrackCards(trackList);
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
        } else if (currentViewType == ViewType.PLAYLISTS) {
            if (playlistTableView != null) {
                playlistTableView.setItems(javafx.collections.FXCollections.observableArrayList(
                        LibrarySearchService.filterPlaylists(Library.getInstance().getPlaylists(), searchQuery)));
            }
        } else if (currentViewType == ViewType.MUSIC) {
            List<Track> filtered = LibrarySearchService.filterTracks(Library.getInstance().getTracks(), searchQuery);
            List<Track> sorted = sortOption.sort(filtered);
            ObservableList<Track> trackList = javafx.collections.FXCollections.observableArrayList(sorted);
            if (trackTableView != null)
                trackTableView.setItems(trackList);
            if (isCardView)
                updateTrackCards(trackList);
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
        if (playlistTableView != null) {
            playlistTableView.setVisible(false);
            playlistTableView.setManaged(false);
        }
        boolean isTrackSection = currentViewType == ViewType.MUSIC
                || currentViewType == ViewType.PLAYLIST_DETAIL;
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

    private ViewType currentViewType = ViewType.HOME;

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

        setupQueueListView();
        setupTrackCardGridView();


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

        if (actionButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(actionButton);
        if (undoButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(undoButton);
        if (playPlaylistButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(playPlaylistButton);
        if (reorderButton != null)
            it.unisa.java_music_playlist_manager.ui.SnapMotion.attach(reorderButton);
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

    private void setupQueueListView() {
        if (queueListView == null)
            return;
        queueListView.setCellFactory(lv -> new ListCell<>() {
            private final FontIcon playingIcon = new FontIcon("fas-volume-up");
            {
                playingIcon.getStyleClass().add("queue-playing-icon");
                playingIcon.setIconSize(12);
            }

            @Override
            protected void updateItem(QueueItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().remove("queue-cell-playing");
                    getStyleClass().remove("queue-cell-playlist-header");
                } else {
                    Track currentTrack = PlaybackManager.getInstance().getCurrentTrack();
                    int currentQueueIndex = PlaybackManager.getInstance().getCurrentPlayableIndex();
                    boolean isCurrentQueuePlayable = (item.queueIndex == currentQueueIndex);

                    boolean isPlaying = false;
                    if (item.track != null) {
                        isPlaying = isCurrentQueuePlayable && item.track.equals(currentTrack);
                    } else if (item.parentPlayable instanceof Playlist) {
                        isPlaying = isCurrentQueuePlayable;
                    }

                    if (isPlaying) {
                        if (!getStyleClass().contains("queue-cell-playing")) {
                            getStyleClass().add("queue-cell-playing");
                        }
                        setGraphic(playingIcon);
                    } else {
                        getStyleClass().remove("queue-cell-playing");
                        setGraphic(null);
                    }

                    if (item.parentPlayable instanceof Playlist && item.track == null) {
                        Playlist p = (Playlist) item.parentPlayable;
                        boolean expanded = expandedPlaylists.contains(p);
                        String indicator = expanded ? "[-]" : "[+]";
                        setText(indicator + " " + p.getTitle() + " (" + p.getTrackCount() + " brani)");
                        if (!getStyleClass().contains("queue-cell-playlist-header")) {
                            getStyleClass().add("queue-cell-playlist-header");
                        }
                    } else if (item.parentPlayable instanceof Playlist && item.track != null) {
                        getStyleClass().remove("queue-cell-playlist-header");
                        setText("    " + item.track.getTitle() + " - " + item.track.getAuthor() + " ("
                                + formatDuration(item.track.getDuration()) + ")");
                    } else if (item.parentPlayable instanceof Track) {
                        getStyleClass().remove("queue-cell-playlist-header");
                        Track t = (Track) item.parentPlayable;
                        setText(t.getTitle() + " - " + t.getAuthor() + " (" + formatDuration(t.getDuration()) + ")");
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

        Optional<ButtonType> result = ThemeManager.getInstance().showThemedDialog(confirmAlert);
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

        if (ThemeManager.getInstance().showThemedDialog(confirmAlert).filter(r -> r == ButtonType.OK).isPresent()) {
            Command deletePlaylistCmd = new RemovePlaylistCommand(Library.getInstance(), selectedPlaylist);
            UndoManager.getInstance().executeCommand(deletePlaylistCmd);
            refreshTableData();
        }
    }

    /**
     * Gestisce gli eventi di navigazione provenienti dalla barra laterale.
     */
    private void handleNavigate(ViewType viewType) {
        if (viewType != null) {
            switch (viewType) {
                case HOME -> handleHomeAction();
                case MUSIC -> handleMusicLibraryAction();
                case QUEUE -> handlePlayQueueAction();
                case PLAYLISTS -> handlePlaylistAction();
                default -> {
                }
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
        refreshTableData();
        updateTablePlaceholder();
    }

    /**
     * Aggiorna la visibilità di tutti i pulsanti contestuali in base alla sezione
     * corrente. È l'unica fonte di verità per la visibilità dei controlli della
     * barra superiore: chiamare una sola volta dopo aver impostato currentViewType
     * in ogni metodo di navigazione.
     *
     * Regole:
     * - undoButton: visibile solo dove esistono operazioni reversibili
     * (Libreria, Playlist, Dettaglio Playlist).
     * - playPlaylistButton: visibile solo nell'elenco Playlist, l'unico contesto
     * in cui selezionare una playlist e avviarla senza entrarci è significativo.
     */
    private void updateContextualUI() {
        boolean isEditableSection = currentViewType == ViewType.MUSIC
                || currentViewType == ViewType.PLAYLISTS
                || currentViewType == ViewType.PLAYLIST_DETAIL;
        boolean isPlaylistListSection = currentViewType == ViewType.PLAYLISTS;
        boolean isTrackSection = currentViewType == ViewType.MUSIC
                || currentViewType == ViewType.PLAYLIST_DETAIL;

        if (undoButton != null) {
            undoButton.setVisible(isEditableSection);
            undoButton.setManaged(isEditableSection);
        }
        if (playPlaylistButton != null) {
            playPlaylistButton.setVisible(isPlaylistListSection);
            playPlaylistButton.setManaged(isPlaylistListSection);
        }
        if (sortComboBox != null) {
            sortComboBox.setVisible(isTrackSection);
            sortComboBox.setManaged(isTrackSection);
        }
        if (viewToggleButton != null) {
            viewToggleButton.setVisible(isTrackSection);
            viewToggleButton.setManaged(isTrackSection);
        }
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
        updateContextualUI();
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
        if (trackCardGridView != null) {
            trackCardGridView.setVisible(false);
            trackCardGridView.setManaged(false);
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
        updateContextualUI();
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
        updateContextualUI();
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
        updateContextualUI();
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

        TableColumn<Track, Void> playCol = new TableColumn<>("");
        playCol.setMinWidth(40);
        playCol.setPrefWidth(44);
        playCol.setMaxWidth(48);
        playCol.setResizable(false);
        playCol.setSortable(false);
        playCol.setCellFactory(col -> new TableCell<>() {
            private final Button playBtn = new Button();
            {
                playBtn.getStyleClass().add("table-play-button");
                playBtn.setMinSize(22, 22);
                playBtn.setPrefSize(22, 22);
                playBtn.setMaxSize(22, 22);
                FontIcon playIcon = new FontIcon("fas-play");
                playIcon.getStyleClass().add("table-play-icon");
                playIcon.setMouseTransparent(true);
                playBtn.setGraphic(playIcon);
                playBtn.setOnAction(event -> {
                    event.consume();
                    Track track = getTableRow() != null ? getTableRow().getItem() : null;
                    if (track != null) {
                        handleStartTrackPlayback(track);
                    }
                });
                playBtn.setOnMouseClicked(javafx.scene.input.MouseEvent::consume);
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(playBtn);
                }
            }
        });

        TableColumn<Track, String> titleCol = new TableColumn<>("Titolo");
        titleCol.setMinWidth(160);
        titleCol.setPrefWidth(220);
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Track, String> artistCol = new TableColumn<>("Artista");
        artistCol.setMinWidth(120);
        artistCol.setPrefWidth(160);
        artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));

        TableColumn<Track, String> albumCol = new TableColumn<>("Album");
        albumCol.setMinWidth(120);
        albumCol.setPrefWidth(160);
        albumCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlbum()));

        TableColumn<Track, String> yearCol = new TableColumn<>("Anno");
        yearCol.setMinWidth(60);
        yearCol.setPrefWidth(70);
        yearCol.setMaxWidth(80);
        yearCol.setCellValueFactory(data -> {
            Integer year = data.getValue().getYear();

            if (year == null) {
                return new SimpleStringProperty("");
            }

            return new SimpleStringProperty(String.valueOf(year));
        });

        TableColumn<Track, String> genreCol = new TableColumn<>("Genere");
        genreCol.setMinWidth(90);
        genreCol.setPrefWidth(110);
        genreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre()));

        TableColumn<Track, String> durationCol = new TableColumn<>("Durata");
        durationCol.setMinWidth(65);
        durationCol.setPrefWidth(75);
        durationCol.setMaxWidth(85);
        durationCol.setCellValueFactory(data -> {
            int seconds = data.getValue().getDuration();
            return new SimpleStringProperty(String.format("%02d:%02d", seconds / 60, seconds % 60));
        });

        // Configurazione della colonna e della cellFactory custom per i Tag
        TableColumn<Track, Set<Tag>> tagCol = new TableColumn<>("Tag");
        tagCol.setMinWidth(120);
        tagCol.setPrefWidth(160);
        tagCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTags()));
        tagCol.setCellFactory(new TagCellFactory());

        trackTableView.getColumns().addAll(playCol, titleCol, artistCol, albumCol, yearCol, genreCol, durationCol, tagCol);

        trackTableView.setRowFactory(new ReorderableTrackRowFactory(
                () -> reorderButton != null && reorderButton.isSelected(),
                () -> currentOpenedPlaylist,
                this::updatePlayerUI,
                this));

        afterViewSwitch();
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
        TableColumn<Playlist, String> nameCol = createColumn("Nome Playlist", 350, p -> p.getTitle());
        nameCol.setMinWidth(200);

        TableColumn<Playlist, Integer> countCol = new TableColumn<>("Numero Brani");
        countCol.setMinWidth(90);
        countCol.setPrefWidth(110);
        countCol.setMaxWidth(130);
        countCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTrackCount()).asObject());

        TableColumn<Playlist, String> durationCol = createColumn("Durata Totale", 100,
                p -> formatDuration(p.getDuration()));
        durationCol.setMinWidth(85);
        durationCol.setMaxWidth(120);

        playlistTableView.getColumns().addAll(nameCol, countCol, durationCol);
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
        updateContextualUI();
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
        if (currentViewType == ViewType.MUSIC) {
            currentEditingTrack = null;
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem singleItem = new MenuItem("Aggiungi singolo brano (Modifica metadati)");
            singleItem.setOnAction(e -> openAddTrackView());
            
            MenuItem multiItem = new MenuItem("Aggiungi più file...");
            multiItem.setOnAction(e -> handleAddFiles());
            
            MenuItem folderItem = new MenuItem("Aggiungi intera cartella...");
            folderItem.setOnAction(e -> handleAddFolder());
            
            contextMenu.getItems().addAll(singleItem, multiItem, folderItem);
            contextMenu.show(actionButton, Side.BOTTOM, 0, 0);
        } else if (currentViewType == ViewType.PLAYLISTS) {
            dialogService.openCreatePlaylistDialog();
        }
    }

    private void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona File Audio");
        File userHome = new File(System.getProperty("user.home"));
        File musicDir = new File(userHome, "Music");
        if (!musicDir.exists() || !musicDir.isDirectory()) musicDir = new File(userHome, "Musica");
        fileChooser.setInitialDirectory((musicDir.exists() && musicDir.isDirectory()) ? musicDir : userHome);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Audio", "*.mp3", "*.wav", "*.m4a"));
        
        List<File> files = fileChooser.showOpenMultipleDialog(actionButton.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            processMultipleFilesAsync(files);
        }
    }

    private void handleAddFolder() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Seleziona Cartella Musicale");
        File userHome = new File(System.getProperty("user.home"));
        File musicDir = new File(userHome, "Music");
        if (!musicDir.exists() || !musicDir.isDirectory()) musicDir = new File(userHome, "Musica");
        dirChooser.setInitialDirectory((musicDir.exists() && musicDir.isDirectory()) ? musicDir : userHome);
        
        File dir = dirChooser.showDialog(actionButton.getScene().getWindow());
        if (dir != null) {
            try (Stream<Path> paths = Files.walk(dir.toPath())) {
                List<File> files = paths.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(f -> {
                        String name = f.getName().toLowerCase();
                        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a");
                    })
                    .collect(Collectors.toList());
                if (!files.isEmpty()) {
                    processMultipleFilesAsync(files);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMultipleFilesAsync(List<File> files) {
        Dialog<Void> progressDialog = new Dialog<>();
        progressDialog.setTitle("Importazione Brani");
        progressDialog.setHeaderText("Analisi di " + files.size() + " brani in corso...");
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        Label progressLabel = new Label("Preparazione...");
        
        VBox content = new VBox(10, progressLabel, progressBar);
        progressDialog.getDialogPane().setContent(content);
        progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL); // Required to show dialog, though we will close it programmatically
        progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(true); // Disable cancel for simplicity
        
        Task<List<Track>> task = new Task<>() {
            @Override
            protected List<Track> call() {
                List<Track> importedTracks = new ArrayList<>();
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    updateMessage("Elaborazione: " + file.getName() + " (" + (i+1) + "/" + files.size() + ")");
                    updateProgress(i, files.size());
                    Track t = extractMetadata(file);
                    if (t != null) {
                        importedTracks.add(t);
                    }
                }
                updateProgress(files.size(), files.size());
                return importedTracks;
            }
        };

        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            Platform.runLater(() -> progressLabel.setText(newMsg));
        });
        
        task.progressProperty().addListener((obs, oldProg, newProg) -> {
            Platform.runLater(() -> progressBar.setProgress(newProg.doubleValue()));
        });

        task.setOnSucceeded(e -> {
            List<Track> importedTracks = task.getValue();
            if (!importedTracks.isEmpty()) {
                Command addMulti = new AddMultipleTracksCommand(Library.getInstance(), importedTracks);
                UndoManager.getInstance().executeCommand(addMulti);
            }
            progressDialog.setResult(null);
            progressDialog.close();
        });

        task.setOnFailed(e -> {
            progressDialog.setResult(null);
            progressDialog.close();
        });

        new Thread(task).start();
        progressDialog.showAndWait();
    }

    private Track extractMetadata(File file) {
        try {
            AudioFile f = AudioFileIO.read(file);
            org.jaudiotagger.tag.Tag tag = f.getTag();
            String title = (tag != null && tag.getFirst(FieldKey.TITLE) != null && !tag.getFirst(FieldKey.TITLE).isEmpty()) 
                    ? tag.getFirst(FieldKey.TITLE) : file.getName().replaceFirst("[.][^.]+$", "");
            String author = (tag != null && tag.getFirst(FieldKey.ARTIST) != null && !tag.getFirst(FieldKey.ARTIST).isEmpty()) 
                    ? tag.getFirst(FieldKey.ARTIST) : "Artista Sconosciuto";
            String album = (tag != null && tag.getFirst(FieldKey.ALBUM) != null) ? tag.getFirst(FieldKey.ALBUM) : "";
            String genre = (tag != null && tag.getFirst(FieldKey.GENRE) != null) ? tag.getFirst(FieldKey.GENRE) : "Altro";
            String yearStr = (tag != null && tag.getFirst(FieldKey.YEAR) != null) ? tag.getFirst(FieldKey.YEAR) : "";
            Integer year = null;
            if (!yearStr.isEmpty()) {
                try { year = Integer.parseInt(yearStr.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
            }
            int duration = f.getAudioHeader().getTrackLength(); // seconds
            return new Track(title, author, album, duration, genre, year, file.getAbsolutePath());
        } catch (Exception e) {
            // Fallback base
            return new Track(file.getName().replaceFirst("[.][^.]+$", ""), "Artista Sconosciuto", "", 0, "Altro", null, file.getAbsolutePath());
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

    private List<Track> getVisibleLibraryTracks() {
        if (trackTableView != null && !trackTableView.getItems().isEmpty()) {
            return new ArrayList<>(trackTableView.getItems());
        }
        return new ArrayList<>(Library.getInstance().getTracks());
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
            } else {
                List<Track> visibleTracks = getVisibleLibraryTracks();
                if (!visibleTracks.isEmpty()) {
                    manager.selectAndLoadTrack(visibleTracks.get(0), visibleTracks);
                }
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
            // Dalla libreria: carica l'intera lista dei brani visibili come contesto
            manager.selectAndLoadTrack(selectedTrack, getVisibleLibraryTracks());
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

    private void setupTrackCardGridView() {
        if (trackCardGridView == null)
            return;

        // Recupero il singleton UNA volta sola qui nella factory,
        // non ad ogni singola chiamata updateItem().
        final it.unisa.java_music_playlist_manager.ui.CoverImageService imageService = it.unisa.java_music_playlist_manager.ui.CoverImageService
                .getInstance();

        trackCardGridView.setCellFactory(gridView -> new GridCell<Track>() {
            private it.unisa.java_music_playlist_manager.ui.TrackCardView card;
            private java.util.concurrent.CompletableFuture<javafx.scene.image.Image> pendingLoadTask;

            // Forte reference necessaria affinché il WeakChangeListener non venga
            // garbage-collected mentre la cella è ancora in uso nel pool di ControlsFX.
            private final javafx.beans.value.ChangeListener<Track> selectionListener;

            {
                setStyle("-fx-padding: 0; -fx-background-color: transparent;");

                selectionListener = (obs, oldVal, newVal) -> {
                    if (card != null) {
                        card.setSelected(newVal != null && newVal.equals(getItem()));
                    }
                };
                trackTableView.getSelectionModel().selectedItemProperty().addListener(
                        new javafx.beans.value.WeakChangeListener<>(selectionListener));

                // I click vengono gestiti qui sulla GridCell, non nella TrackCardView,
                // perché ControlsFX garantisce la consegna degli eventi mouse alle celle.
                setOnMouseClicked(event -> {
                    if (event.getButton() != javafx.scene.input.MouseButton.PRIMARY)
                        return;
                    Track t = getItem();
                    if (t == null)
                        return;
                    if (event.getClickCount() == 2) {
                        handleStartTrackPlayback(t);
                    } else if (event.getClickCount() == 1) {
                        trackTableView.getSelectionModel().select(t);
                        updatePlayPlaylistButtonState();
                    }
                });

                setOnContextMenuRequested(event -> {
                    Track t = getItem();
                    if (t == null)
                        return;
                    trackTableView.getSelectionModel().select(t);
                    updatePlayPlaylistButtonState();
                    ContextMenu menu = trackTableView.getContextMenu();
                    if (menu != null) {
                        menu.show(trackCardGridView.getScene().getWindow(),
                                event.getScreenX(), event.getScreenY());
                    }
                });
            }

            @Override
            protected void updateItem(Track item, boolean empty) {
                super.updateItem(item, empty);

                // Se la cella viene svuotata o riciclata, cancella l'eventuale task I/O in
                // corso
                if (pendingLoadTask != null) {
                    pendingLoadTask.cancel(true);
                    pendingLoadTask = null;
                }

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Lazy init: la TrackCardView è ora una pura dumb view,
                    // senza handlers — li gestisce la GridCell sovrastante.
                    if (card == null) {
                        card = new it.unisa.java_music_playlist_manager.ui.TrackCardView();
                        card.setOnPlayAction(PrimaryViewController.this::handleStartTrackPlayback);
                    }

                    card.updateData(item, imageService.getCachedCoverOrDefault(item.getFilePath()));

                    Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
                    card.setSelected(selectedTrack != null && selectedTrack.equals(item));

                    // Snapshot dell'item al momento della sottomissione del task asincrono:
                    // quando il task torna, verifichiamo che la cella mostri ancora la
                    // stessa traccia (equals basato su ID) prima di aggiornare la cover.
                    final Track itemSnapshot = item;
                    pendingLoadTask = imageService.loadCoverAsync(item.getFilePath());
                    pendingLoadTask.thenAcceptAsync(image -> {
                        if (image != null && itemSnapshot.equals(card.getTrack())) {
                            card.updateData(itemSnapshot, image);
                        }
                    }, javafx.application.Platform::runLater);

                    setGraphic(card);
                }
            }
        });
    }

    @FXML
    private void handleViewToggleAction() {
        isCardView = viewToggleButton.isSelected();
        showOnlyTrackTable();
        refreshTableData();
    }

    private void updateTrackCards(ObservableList<Track> tracks) {
        if (trackCardGridView == null)
            return;
        trackCardGridView.setItems(tracks);
    }
}
