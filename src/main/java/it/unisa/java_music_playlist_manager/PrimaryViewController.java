package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Playable;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.PlaylistGenerator;
import it.unisa.java_music_playlist_manager.model.ManualPlaylistGenerator;
import it.unisa.java_music_playlist_manager.model.AutomaticPlaylistGenerator;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import java.util.Locale;
import it.unisa.java_music_playlist_manager.model.Observer;
import java.io.IOException;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TableRow;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import it.unisa.java_music_playlist_manager.model.Tag;
import java.util.Set;
import java.util.HashSet;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;

/**
 * PrimaryViewController è il coordinatore principale dell'interfaccia utente.
 * Gestisce l'area centrale dell'applicazione, inclusa la visualizzazione delle tracce,
 * delle playlist e della coda di riproduzione.
 * 
 * La struttura dei campi annotati con @FXML e i collegamenti ai metodi di gestione eventi
 * sono configurati automaticamente tramite l'integrazione tra SceneBuilder e NetBeans.
 * 
 * Ruolo nel progetto:
 * - Agisce come Controller principale che integra i sotto-controller (Sidebar e PlayerBar).
 * - Implementa l'interfaccia {@link Observer} per reagire ai cambiamenti nel Modello (Library e PlaybackManager).
- Gestisce la logica di navigazione tra le diverse viste (Musica, Playlist, Coda).
- Coordina le operazioni CRUD su tracce e playlist tramite menu contestuali e dialoghi.
 */
public class PrimaryViewController implements Observer {

    /** Traccia attualmente in fase di modifica (usata per popolare il form di editing) */
    private Track currentEditingTrack = null;
    
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
        if (songTableView != null) {
            refreshTableData();
            songTableView.refresh();
            syncTableSelection();
        }
    }

    /**
     * Aggiorna i dati mostrati nella TableView in base alla vista corrente (Musica, Coda o Playlist aperta).
     */
    @SuppressWarnings("unchecked")
    private void refreshTableData() {
        String currentView = viewTitleLabel.getText();

        if (currentOpenedPlaylist != null) {
            // Mostra i brani della playlist attualmente selezionata
            ObservableList<Track> trackList = filteredTracks(currentOpenedPlaylist.getTracks());
            ((TableView<Track>) songTableView).setItems(trackList);
        } else if ("Coda di riproduzione".equals(currentView)) {
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
        } else if ("Musica".equals(currentView)) {
            // Mostra l'intera libreria musicale
            ObservableList<Track> trackList = filteredTracks(Library.getInstance().getTracks());
            ((TableView<Track>) songTableView).setItems(trackList);
        }
    }

    /**
     * Gestisce il testo segnaposto della tabella quando questa è vuota,
     * fornendo suggerimenti contestuali all'utente.
     */
    private void updateTablePlaceholder() {
        if (songTableView == null) {
            return;
        }

        String currentView = viewTitleLabel != null ? viewTitleLabel.getText() : "";
        String placeholderText;

        if (!searchQuery.isBlank()) {
            placeholderText = "Nessun risultato per \"" + searchQuery.trim() + "\".";
        } else if ("Playlist".equals(currentView)) {
            placeholderText = "Non ci sono playlist. Clicca \"Nuova playlist\" per crearne una.";
        } else if (currentOpenedPlaylist != null) {
            placeholderText = "Questa playlist non contiene brani. Clicca \"Aggiungi brano\" per inserirne uno.";
        } else if ("Coda di riproduzione".equals(currentView)) {
            placeholderText = "Non ci sono brani in coda. Clicca \"Aggiungi brano\" nella Libreria musicale per popolarla.";
        } else {
            placeholderText = "Non ci sono brani. Clicca \"Aggiungi brano\" per inserirne uno.";
        }

        Label placeholderLabel = new Label(placeholderText);
        placeholderLabel.setWrapText(true);
        placeholderLabel.setMaxWidth(420);
        placeholderLabel.setStyle("-fx-alignment: center; -fx-text-alignment: center; -fx-text-fill: #666666; -fx-padding: 16;");
        songTableView.setPlaceholder(placeholderLabel);
        if (queueListView != null) {
            Label queuePlaceholder = new Label(placeholderText);
            queuePlaceholder.setWrapText(true);
            queuePlaceholder.setMaxWidth(420);
            queuePlaceholder.setStyle("-fx-alignment: center; -fx-text-alignment: center; -fx-text-fill: #666666; -fx-padding: 16;");
            queueListView.setPlaceholder(queuePlaceholder);
        }
    }

    // --- Componenti iniettati da FXML ---
    
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
    private ComboBox<String> sortComboBox;
    @FXML
    private HBox genreFilterContainer;
    @FXML
    private ComboBox<String> genreComboBox;

    /** Tabella principale per la visualizzazione di Track o Playlist */
    @FXML
    private TableView<?> songTableView;
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
        // Registrazione come osservatore del Modello
        Library.getInstance().attach(this);
        PlaybackManager.getInstance().attach(this);

        // Collegamento delle callback per la comunicazione tra controller (Mediator-like)
        sidebarController.setOnNavigate(this::handleNavigate);
        sidebarController.setOnSearchQueryChange(this::handleSearchQueryChange);
        // BUG-FIX: il pulsante Play/Pause deve SOLO alternare pausa/ripresa,
        // senza considerare l'elemento selezionato nella lista/tabella.
        playerBarController.setOnPlayPauseClicked(() -> handlePlayPauseAction());
        playerBarController.setOnPlayerStateChanged(() -> syncTableSelection());

        // Popolamento filtri e ordinamenti
        if (sortComboBox != null) {
            sortComboBox.getItems().addAll("A - Z", "Z - A", "Artista", "Anno", "Durata");
        }
        if (genreComboBox != null) {
            genreComboBox.getItems().addAll("Tutti i generi", "Pop", "Rock", "Jazz", "Classica", "Hip Hop");
        }

        // Gestione abilitazione pulsanti in base alla selezione
        songTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isPlaylistView = "Playlist".equals(viewTitleLabel.getText());
            if (isPlaylistView && newVal instanceof Playlist) {
                if (playPlaylistButton != null) {
                    playPlaylistButton.setDisable(false);
                }
            } else {
                if (playPlaylistButton != null) {
                    playPlaylistButton.setDisable(true);
                }
            }
        });

        // Configurazione iniziale delle colonne per la vista "Musica"
        showSongsColumns();
        
        setupQueueListView();

        // Configurazione del menu contestuale dinamico
        setupContextMenu();

        // Gestione del doppio click per navigazione o riproduzione.
        // Il singolo click seleziona solo visivamente; il doppio click avvia la riproduzione.
        songTableView.setOnMouseClicked(event -> {
            Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                if (currentOpenedPlaylist == null
                        && "Playlist".equals(viewTitleLabel.getText())
                        && selectedItem instanceof Playlist playlist) {
                    openPlaylistDetail(playlist);
                } else if (selectedItem instanceof Track track) {
                    // Doppio click su una traccia: avvia la riproduzione di quel brano specifico
                    handleStartTrackPlayback(track);
                }
            }
        });

        updatePlayerUI();
        System.out.println("Interfaccia grafica inizializzata correttamente.");
    }

    private void setupQueueListView() {
        if (queueListView == null) return;
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
                        setText("    " + item.track.getTitle() + " - " + item.track.getAuthor() + " (" + formatDuration(item.track.getDuration()) + ")");
                        setStyle("-fx-font-weight: normal;");
                    } else if (item.parentPlayable instanceof Track) {
                        Track t = (Track) item.parentPlayable;
                        setText(t.getTitle() + " - " + t.getAuthor() + " (" + formatDuration(t.getDuration()) + ")");
                        setStyle("-fx-font-weight: normal;");
                    }
                }
            }
        });

        javafx.scene.control.ContextMenu queueContextMenu = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem removeFromQueueItem = new javafx.scene.control.MenuItem("Rimuovi dalla coda");
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
            if (selected == null) return;
            if (event.getClickCount() == 1 && selected.parentPlayable instanceof Playlist && selected.track == null) {
                Playlist p = (Playlist) selected.parentPlayable;
                if (expandedPlaylists.contains(p)) expandedPlaylists.remove(p);
                else expandedPlaylists.add(p);
                refreshTableData();
            } else if (event.getClickCount() == 2) {
                if (selected.track != null) {
                    int trackIdx = selected.parentPlayable.getTracks().indexOf(selected.track);
                    PlaybackManager.getInstance().setCurrentIndices(selected.queueIndex, trackIdx);
                    // forcePlayCurrent: ferma l'audio corrente e avvia il brano dall'inizio (slider a 0)
                    PlaybackManager.getInstance().forcePlayCurrent();
                    updatePlayerUI();
                } else if (selected.parentPlayable instanceof Playlist && !selected.parentPlayable.getTracks().isEmpty()) {
                    PlaybackManager.getInstance().setCurrentIndices(selected.queueIndex, 0);
                    PlaybackManager.getInstance().forcePlayCurrent();
                    updatePlayerUI();
                }
            }
        });
    }

    /**
     * Configura il menu contestuale della tabella, definendo azioni e visibilità delle voci.
     */
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem editItem = new MenuItem("Modifica brano");
        editItem.setOnAction(e -> handleEditTrack());

        MenuItem deleteItem = new MenuItem("Elimina brano");
        deleteItem.setOnAction(e -> handleDeleteTrack());

        MenuItem addToPlaylistItem = new MenuItem("Aggiungi a playlist");
        addToPlaylistItem.setOnAction(e -> handleAddTrackToPlaylist());

        MenuItem addTrackToQueueItem = new MenuItem("Aggiungi brano alla coda");
        addTrackToQueueItem.setOnAction(e -> handleAddTrackToQueue());

        MenuItem addPlaylistToQueueItem = new MenuItem("Aggiungi playlist alla coda");
        addPlaylistToQueueItem.setOnAction(e -> handleAddPlaylistToQueue());

        MenuItem editPlaylistItem = new MenuItem("Modifica playlist");
        editPlaylistItem.setOnAction(e -> handleEditPlaylist());

        MenuItem deletePlaylistItem = new MenuItem("Elimina playlist");
        deletePlaylistItem.setOnAction(e -> handleDeletePlaylist());

        MenuItem removeFromQueueItem = new MenuItem("Rimuovi dalla coda");
        removeFromQueueItem.setOnAction(e -> handleRemoveFromQueue());

        MenuItem removeFromPlaylistItem = new MenuItem("Rimuovi dalla playlist");
        removeFromPlaylistItem.setOnAction(e -> handleRemoveFromPlaylist());

        contextMenu.getItems().addAll(
                editItem,
                deleteItem,
                addToPlaylistItem,
                addTrackToQueueItem,
                editPlaylistItem,
                deletePlaylistItem,
                addPlaylistToQueueItem,
                removeFromQueueItem,
                removeFromPlaylistItem
        );
        songTableView.setContextMenu(contextMenu);

        // Listener per mostrare/nascondere le voci del menu in base al tipo di elemento selezionato
        contextMenu.setOnShowing(e -> {
            Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
            boolean noTrackSelected = !(selectedItem instanceof Track);
            boolean noPlaylistSelected = !(selectedItem instanceof Playlist);
            boolean isQueueView = "Coda di riproduzione".equals(viewTitleLabel.getText());

            // Logica di visibilità per tracce
            editItem.setVisible(!noTrackSelected && !isQueueView);
            deleteItem.setVisible(!noTrackSelected && !isQueueView);
            addToPlaylistItem.setVisible(!noTrackSelected && !isQueueView);
            addTrackToQueueItem.setVisible(!noTrackSelected && !isQueueView);

            // Logica di visibilità per playlist
            editPlaylistItem.setVisible(!noPlaylistSelected && !isQueueView);
            deletePlaylistItem.setVisible(!noPlaylistSelected && !isQueueView);
            addPlaylistToQueueItem.setVisible(!noPlaylistSelected && !isQueueView);

            removeFromQueueItem.setDisable(selectedItem == null || !isQueueView);
            removeFromQueueItem.setVisible(isQueueView);

            // Logica per la rimozione dalla playlist (solo se siamo in una playlist manuale)
            boolean isPlaylistDetailView = (currentOpenedPlaylist != null);
            boolean isEditable = currentOpenedPlaylist != null && currentOpenedPlaylist.isManuallyEditable();
            removeFromPlaylistItem.setDisable(noTrackSelected || !isPlaylistDetailView || !isEditable);
            removeFromPlaylistItem.setVisible(!noTrackSelected && isPlaylistDetailView && isEditable);
        });
    }

    @FXML
    private void handleEditTrack() {
        Object selected = songTableView.getSelectionModel().getSelectedItem();
        if (selected instanceof Track track) {
            currentEditingTrack = track;
            openAddTrackView();
        }
    }

    /**
     * Gestisce l'eliminazione di una traccia dalla libreria con conferma dell'utente.
     */
    @FXML
    private void handleDeleteTrack() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
        if (!(selectedItem instanceof Track selectedTrack)) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare il brano selezionato?");
        confirmAlert.setContentText("Stai per eliminare \"" + selectedTrack.getTitle() + "\" dalla libreria.\nVerrà rimosso anche da tutte le playlist.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean removed = Library.getInstance().removeTrack(selectedTrack);

            if (removed) {
                System.out.println("Brano eliminato dalla libreria e dalle playlist: " + selectedTrack.getTitle());
            } else {
                System.out.println("Il brano selezionato non è stato trovato nella libreria.");
            }
        } else {
            System.out.println("Eliminazione annullata dall'utente.");
        }
    }

    /**
     * Gestisce la rimozione di un brano dalla playlist corrente.
     */
    @FXML
    private void handleRemoveFromPlaylist() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();

        if (selectedItem instanceof Track selectedTrack && currentOpenedPlaylist != null && currentOpenedPlaylist.isManuallyEditable()) {
            currentOpenedPlaylist.removeTrack(selectedTrack);
            System.out.println("Brano rimosso dalla playlist: " + selectedTrack.getTitle());
            refreshTableData();
            updateTablePlaceholder();
            Library.getInstance().notifyObservers();
        }
    }

    private void handleEditPlaylist() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
        if (!(selectedItem instanceof Playlist selectedPlaylist)) return;

        TextInputDialog dialog = new TextInputDialog(selectedPlaylist.getTitle());
        dialog.setTitle("Modifica playlist");
        dialog.setHeaderText("Modifica il nome della playlist");
        dialog.setContentText("Nome playlist:");
        dialog.setGraphic(null);

        dialog.showAndWait().ifPresent(name -> {
            try {
                selectedPlaylist.setTitle(name);
                Library.getInstance().notifyObservers();
                showPlaylistColumns();
            } catch (IllegalArgumentException e) {
                showErrorAlert("Errore", "Nome non valido", e.getMessage());
            }
        });
    }

    /**
     * Elimina una playlist dalla libreria con conferma.
     */
    private void handleDeletePlaylist() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
        if (!(selectedItem instanceof Playlist selectedPlaylist)) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare la playlist selezionata?");
        confirmAlert.setContentText("I brani resteranno disponibili nella libreria musicale.");

        if (confirmAlert.showAndWait().filter(r -> r == ButtonType.OK).isPresent()) {
            Library.getInstance().removePlaylist(selectedPlaylist);
            showPlaylistColumns();
        }
    }

    /**
     * Rimuove l'elemento selezionato dalla coda di riproduzione.
     */
    private void handleRemoveFromQueue() {
        int selectedIndex = songTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            PlaybackManager.getInstance().removeFromQueue(selectedIndex);
            refreshTableData();
            updatePlayerUI();
            updateTablePlaceholder();
        }
    }

    /**
     * Gestisce gli eventi di navigazione provenienti dalla barra laterale.
     */
    private void handleNavigate(String viewId) {
        if (null != viewId) switch (viewId) {
            case "Home"     -> handleHomeAction();
            case "Musica"   -> handleMusicLibraryAction();
            case "Coda"     -> handlePlayQueueAction();
            case "Playlist" -> handlePlaylistAction();
            default -> { }
        }
    }

    @FXML
    private void handleUndoAction() {
    }

    private void handleSearchQueryChange(String query) {
        searchQuery = query == null ? "" : query;
        refreshTableData();
        updateTablePlaceholder();
    }

    /** Configura la vista per mostrare la schermata Home con le statistiche. */
    private void handleHomeAction() {
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Home");
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        controlsBar.setVisible(false);
        controlsBar.setManaged(false);
        playPlaylistButton.setDisable(true);
        if (reorderButton != null) {
            reorderButton.setVisible(false);
            reorderButton.setManaged(false);
            reorderButton.setSelected(false);
        }
        genreFilterContainer.setVisible(false);
        genreFilterContainer.setManaged(false);

        // Nascondi tabella e coda, mostra pannello Home
        songTableView.setVisible(false);
        songTableView.setManaged(false);
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
        viewTitleLabel.setText("Musica");
        actionButton.setText("Aggiungi brano");
        actionButton.setVisible(true);
        actionButton.setManaged(true);
        controlsBar.setVisible(true);
        controlsBar.setManaged(true);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showSongsColumns();
    }

    /** Configura la vista per mostrare la coda di riproduzione. */
    private void handlePlayQueueAction() {
        hideHomePanel();
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Coda di riproduzione");
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        controlsBar.setVisible(true);
        controlsBar.setManaged(true);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showQueueColumns();
    }

    /** Configura la vista per mostrare l'elenco delle playlist (Master View). */
    private void handlePlaylistAction() {
        hideHomePanel();
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Playlist");
        actionButton.setText("Nuova playlist");
        actionButton.setVisible(true);
        actionButton.setManaged(true);
        controlsBar.setVisible(true);
        controlsBar.setManaged(true);
        genreFilterContainer.setVisible(false);
        genreFilterContainer.setManaged(false);
        showPlaylistColumns();
    }

    /**
     * Nasconde il pannello Home e ripristina la visibilità della TableView principale.
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
        if (songTableView != null && !songTableView.isVisible()) {
            songTableView.setVisible(true);
            songTableView.setManaged(true);
        }
    }

    // --- Metodi per la configurazione dinamica delle colonne della TableView ---

    private void showQueueColumns() {
        songTableView.setVisible(false);
        songTableView.setManaged(false);
        if (queueListView != null) {
            queueListView.setVisible(true);
            queueListView.setManaged(true);
        }
        updateTablePlaceholder();
        refreshTableData();
    }

    @SuppressWarnings("unchecked")
    private void showSongsColumns() {
        if (queueListView != null) {
            queueListView.setVisible(false);
            queueListView.setManaged(false);
        }
        songTableView.setVisible(true);
        songTableView.setManaged(true);
        songTableView.getColumns().clear();

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
        tagCol.setCellFactory(column -> createTagCellFactory());

        ((TableView<Track>) songTableView).getColumns().addAll(titleCol, artistCol, albumCol, yearCol, genreCol, durationCol, tagCol);
        
        ((TableView<Track>) songTableView).setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty() && reorderButton != null && reorderButton.isSelected()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(String.valueOf(index));
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                if (reorderButton != null && reorderButton.isSelected() && event.getDragboard().hasString()) {
                    if (row.getIndex() != Integer.parseInt(event.getDragboard().getString())) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragEntered(event -> {
                if (reorderButton != null && reorderButton.isSelected() && event.getDragboard().hasString()) {
                    if (row.getIndex() != Integer.parseInt(event.getDragboard().getString())) {
                        row.setStyle("-fx-background-color: #dcdcdc;");
                    }
                }
            });

            row.setOnDragExited(event -> {
                if (reorderButton != null && reorderButton.isSelected()) {
                    row.setStyle("");
                }
            });

            row.setOnDragDropped(event -> {
                if (reorderButton != null && reorderButton.isSelected() && event.getDragboard().hasString()) {
                    int draggedIndex = Integer.parseInt(event.getDragboard().getString());
                    int dropIndex = row.isEmpty() ? tv.getItems().size() : row.getIndex();

                    if (currentOpenedPlaylist instanceof ManualPlaylist) {
                        ((ManualPlaylist) currentOpenedPlaylist).moveElement(draggedIndex, dropIndex);
                        Library.getInstance().notifyObservers();
                    }
                    event.setDropCompleted(true);
                    event.consume();
                }
            });

            return row;
        });
        
        updateTablePlaceholder();
        refreshTableData();
    }

    @SuppressWarnings("unchecked")
    private void showPlaylistColumns() {
        if (queueListView != null) {
            queueListView.setVisible(false);
            queueListView.setManaged(false);
        }
        songTableView.setVisible(true);
        songTableView.setManaged(true);
        songTableView.getColumns().clear();
        TableColumn<Playlist, String> nameCol = createColumn("Nome Playlist", 300, p -> p.getTitle());
        TableColumn<Playlist, Integer> countCol = new TableColumn<>("Numero Brani");
        countCol.setPrefWidth(150);
        countCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTrackCount()).asObject());
        TableColumn<Playlist, String> durationCol = createColumn("Durata Totale", 150, p -> formatDuration(p.getDuration()));

        ((TableView<Playlist>) songTableView).getColumns().addAll(nameCol, countCol, durationCol);
        ((TableView<Playlist>) songTableView).setItems(filteredPlaylists(Library.getInstance().getPlaylists()));
        updateTablePlaceholder();
    }

    // --- Metodi di utilità per la creazione di componenti UI ---

    private <S> TableColumn<S, String> createColumn(String title, double width, java.util.function.Function<S, String> mapper) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return col;
    }

    private String formatDuration(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private ObservableList<Track> filteredTracks(List<Track> tracks) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return FXCollections.observableArrayList(tracks);
        }

        return FXCollections.observableArrayList(
                tracks.stream()
                        .filter(this::matchesTrackSearch)
                        .toList()
        );
    }

    private ObservableList<Playlist> filteredPlaylists(List<Playlist> playlists) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return FXCollections.observableArrayList(playlists);
        }

        return FXCollections.observableArrayList(
                playlists.stream()
                        .filter(this::matchesPlaylistSearch)
                        .toList()
        );
    }

    private boolean matchesTrackSearch(Track track) {
        String query = normalizedSearchQuery();
        if (query.isBlank()) {
            return true;
        }

        return containsSearch(track.getTitle(), query)
                || containsSearch(track.getAuthor(), query)
                || containsSearch(track.getAlbum(), query)
                || containsSearch(track.getGenre(), query)
                || containsAnyTag(track, query);
    }

    private boolean matchesPlaylistSearch(Playlist playlist) {
        String query = normalizedSearchQuery();
        return query.isBlank() || containsSearch(playlist.getTitle(), query);
    }

    private boolean containsAnyTag(Track track, String query) {
        if (track.getTags() == null) {
            return false;
        }

        for (Tag tag : track.getTags()) {
            if (tag != null && containsSearch(tag.getName(), query)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSearch(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalizedSearchQuery() {
        return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
    }

  private TableCell<Track, Set<Tag>> createTagCellFactory() {
    return new TableCell<>() {
        
        // 1. Dichiariamo i componenti UI fuori dall'updateItem per ottimizzare le prestazioni
        private final HBox container = new HBox(5);
        private final ScrollPane scrollPane = new ScrollPane(container);

        // Blocco di inizializzazione della cella
        {
            // Impostiamo l'allineamento del contenitore
            container.setStyle("-fx-alignment: center-left; -fx-padding: 2 0;");
            
            // Configuriamo lo ScrollPane per scorrere solo in orizzontale
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Niente barra verticale
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Niente barra verticale
            scrollPane.setPannable(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPannable(true); // Permette di scorrere trascinando con il mouse
            
            // Rimuoviamo i bordi e lo sfondo di default dello ScrollPane per farlo integrare nella cella
            scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-background-insets: 0; -fx-padding: 0;");
            
    }

        @Override
        protected void updateItem(Set<Tag> tags, boolean empty) {
            super.updateItem(tags, empty);
            
            if (empty || tags == null || tags.isEmpty()) {
                setGraphic(null);
            } else {
                // 2. Svuotiamo i vecchi tag invece di creare un nuovo contenitore
                container.getChildren().clear();
                
                tags.forEach(tag -> {
                    if (tag == null) return;
                    
                    Label badge = new Label(tag.getIcon());
                    badge.setStyle("-fx-background-color: #4A4A4A; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 7; -fx-font-size: 11px; -fx-font-weight: bold;");
                    
                    Tooltip tooltip = new Tooltip("Tag: " + tag.getName());
                    Tooltip.install(badge, tooltip);
                    
                    container.getChildren().add(badge);
                });
                
                // 3. Impostiamo lo ScrollPane come grafica della cella
                setGraphic(scrollPane);
            }
        }
    };
}

    /**
     * Naviga all'interno di una playlist specifica per mostrarne il contenuto.
     */
    private void openPlaylistDetail(Playlist playlist) {
        currentOpenedPlaylist = playlist;
        viewTitleLabel.setText(playlist.getTitle());
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
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
    }

    private void openCreatePlaylistDialog() {
        List<String> options = List.of("Playlist vuota", "Playlist automatica");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Playlist vuota", options);
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Scegli il tipo di playlist");
        dialog.setContentText("Tipo:");
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }
        if ("Playlist vuota".equals(result.get())) {
            openCreateEmptyPlaylistDialog();
        } else if ("Playlist automatica".equals(result.get())) {
            openCreateAutomaticPlaylistDialog();
        }
    }

    /**
     * Apre il dialogo per creare una playlist vuota.
     *
     * La playlist vuota viene creata tramite ManualPlaylistGenerator
     * e può essere modificata manualmente dall'utente.
     */
    private void openCreateEmptyPlaylistDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Crea una nuova playlist");
        dialog.setContentText("Nome playlist:");
        dialog.setGraphic(null);

        dialog.showAndWait().ifPresent(name -> {
            try {
                PlaylistGenerator generator = new ManualPlaylistGenerator();
                Playlist playlist = (Playlist) generator.createPlaylist(name);
                Library.getInstance().addPlaylist(playlist);
                showPlaylistColumns();
            } catch (IllegalArgumentException e) {
                showErrorAlert(
                        "Errore",
                        "Nome playlist non valido",
                        e.getMessage()
                );
            }
        });
    }

    /**
     * Apre il dialogo per scegliere il criterio della playlist automatica.
     * I criteri disponibili sono genere musicale e anno di uscita.
     */
    private void openCreateAutomaticPlaylistDialog() {
        List<String> options = List.of("Genere", "Anno", "Tag");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Genere", options);
        dialog.setTitle("Playlist automatica");
        dialog.setHeaderText("Crea una playlist automatica");
        dialog.setContentText("Criterio:");
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        if ("Genere".equals(result.get())) {
            openAutomaticPlaylistByGenreDialog();
        } else if ("Anno".equals(result.get())) {
            openAutomaticPlaylistByYearDialog();
        } else if ("Tag".equals(result.get())) {
            openAutomaticPlaylistByTagDialog();
        }
    }

    /**
     * Apre il dialogo per scegliere il genere musicale
     * su cui basare la playlist automatica.
     */
    private void openAutomaticPlaylistByGenreDialog() {
        List<String> genres = new ArrayList<>();

        for (Track track : Library.getInstance().getTracks()) {
            String genre = track.getGenre();

            if (genre != null && !genre.trim().isEmpty() && !genres.contains(genre)) {
                genres.add(genre);
            }
        }

        genres.sort(String.CASE_INSENSITIVE_ORDER);

        if (genres.isEmpty()) {
            showInfoAlert(
                    "Nessun genere disponibile",
                    "Playlist automatica non creata",
                    "Non ci sono generi disponibili nella libreria."
            );
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(genres.get(0), genres);
        dialog.setTitle("Playlist automatica per genere");
        dialog.setHeaderText("Scegli il genere");
        dialog.setContentText("Genere:");
        dialog.setGraphic(null);

        Optional<String> genreResult = dialog.showAndWait();
        genreResult.ifPresent(genre -> {
            TextInputDialog titleDialog = new TextInputDialog("Playlist per " + genre);
            titleDialog.setTitle("Nome playlist");
            titleDialog.setHeaderText("Inserisci il nome della playlist");
            titleDialog.setContentText("Nome:");
            titleDialog.setGraphic(null);
            
            Optional<String> titleResult = titleDialog.showAndWait();
            titleResult.ifPresent(title -> generateAutomaticPlaylistByGenre(genre, title));
        });
    }

    /**
     * Apre il dialogo per scegliere l'anno di uscita
     * su cui basare la playlist automatica.
     */
    private void openAutomaticPlaylistByYearDialog() {
        List<Integer> years = new ArrayList<>();

        for (Track track : Library.getInstance().getTracks()) {
            Integer year = track.getYear();

            if (year != null && !years.contains(year)) {
                years.add(year);
            }
        }

        years.sort(Integer::compareTo);

        if (years.isEmpty()) {
            showInfoAlert(
                    "Nessun anno disponibile",
                    "Playlist automatica non creata",
                    "Non ci sono anni disponibili nella libreria."
            );
            return;
        }

        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(years.get(0), years);
        dialog.setTitle("Playlist automatica per anno");
        dialog.setHeaderText("Scegli l'anno");
        dialog.setContentText("Anno:");
        dialog.setGraphic(null);

        Optional<Integer> yearResult = dialog.showAndWait();
        yearResult.ifPresent(year -> {
            TextInputDialog titleDialog = new TextInputDialog("Playlist del " + year);
            titleDialog.setTitle("Nome playlist");
            titleDialog.setHeaderText("Inserisci il nome della playlist");
            titleDialog.setContentText("Nome:");
            titleDialog.setGraphic(null);
            
            Optional<String> titleResult = titleDialog.showAndWait();
            titleResult.ifPresent(title -> generateAutomaticPlaylistByYear(year, title));
        });
    }

    /**
     * Genera una playlist automatica filtrata per genere musicale.
     *
     * @param genre Genere scelto dall'utente.
     */
    private void generateAutomaticPlaylistByGenre(String genre, String title) {
        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.GENRE,
                genre
        );
        Playlist playlist = (Playlist) generator.createPlaylist(title);
        saveGeneratedPlaylist(playlist, "Playlist automatica creata per genere: ");
    }

    /**
     * Apre il dialogo per scegliere il tag
     * su cui basare la playlist automatica.
     */
    private void openAutomaticPlaylistByTagDialog() {
        List<Tag> tags = new ArrayList<>();

        // Estrazione dei tag univoci attualmente in uso nella libreria
        for (Track track : Library.getInstance().getTracks()) {
            Set<Tag> trackTags = track.getTags();
            if (trackTags != null) {
                for (Tag tag : trackTags) {
                    if (!tags.contains(tag)) {
                        tags.add(tag);
                    }
                }
            }
        }

        // Ordinamento alfabetico basato sul nome del tag
        tags.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));

        if (tags.isEmpty()) {
            showInfoAlert(
                    "Nessun tag disponibile",
                    "Playlist automatica non creata",
                    "Non ci sono tag associati ai brani nella libreria."
            );
            return;
        }

        // Il ChoiceDialog utilizzerà automaticamente il metodo toString() di TagPredefined
        ChoiceDialog<Tag> dialog = new ChoiceDialog<>(tags.get(0), tags);
        dialog.setTitle("Playlist automatica per tag");
        dialog.setHeaderText("Scegli il tag");
        dialog.setContentText("Tag:");
        dialog.setGraphic(null);

        Optional<Tag> tagResult = dialog.showAndWait();
        tagResult.ifPresent(tag -> {
            TextInputDialog titleDialog = new TextInputDialog("Playlist " + tag.getName());
            titleDialog.setTitle("Nome playlist");
            titleDialog.setHeaderText("Inserisci il nome della playlist");
            titleDialog.setContentText("Nome:");
            titleDialog.setGraphic(null);

            Optional<String> titleResult = titleDialog.showAndWait();
            titleResult.ifPresent(title -> generateAutomaticPlaylistByTag(tag, title));
        });
    }

    /**
     * Genera una playlist automatica filtrata per anno di uscita.
     *
     * @param year Anno scelto dall'utente.
     */
    private void generateAutomaticPlaylistByYear(Integer year, String title) {
        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.YEAR,
                year
        );
        Playlist playlist = (Playlist) generator.createPlaylist(title);
        saveGeneratedPlaylist(playlist, "Playlist automatica creata per anno: ");

    }

    /**
     * Genera una playlist automatica filtrata per tag.
     *
     * @param tag Tag scelto dall'utente.
     * @param title Titolo della playlist.
     */
    private void generateAutomaticPlaylistByTag(Tag tag, String title) {
        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.TAG,
                tag
        );
        Playlist playlist = (Playlist) generator.createPlaylist(title);
        saveGeneratedPlaylist(playlist, "Playlist automatica creata per tag: ");
    }

    /**
     * Salva nella libreria la playlist generata e aggiorna la tabella.
     *
     * @param playlist Playlist da salvare.
     * @param logMessage Messaggio da stampare in console.
     */
    private void saveGeneratedPlaylist(Playlist playlist, String logMessage) {
        Library.getInstance().addPlaylist(playlist);
        showPlaylistColumns();
        System.out.println(logMessage + playlist.getTitle());
    }


    /**
     * Carica e visualizza la finestra modale per l'aggiunta o la modifica di una traccia.
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Gestore del pulsante d'azione principale (contestuale alla vista). */
    @FXML
    private void handleActionBtnClick() {
        String currentView = viewTitleLabel.getText();
        if ("Musica".equals(currentView)) openAddTrackView();
        else if ("Playlist".equals(currentView)) openCreatePlaylistDialog();
    }

    /** Aggiunge la traccia selezionata a una playlist scelta tramite dialogo. */
    private void handleAddTrackToPlaylist() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
        if (!(selectedItem instanceof Track selectedTrack)) return;

        // Le playlist automatiche vengono escluse perchè il loro contenuto
        // viene calcolato dinamicamente in base al criterio scelto
        List<Playlist> playlists = new ArrayList<>();

        for (Playlist playlist : Library.getInstance().getPlaylists()) {
            if (playlist.isManuallyEditable()) {
                playlists.add(playlist);
            }
        }

        if (playlists.isEmpty()) {
            showInfoAlert(
                    "Nessuna playlist",
                    "Non ci sono playlist disponibili",
                    "Crea prima una playlist."
            );
            return;
        }

        ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(playlists.get(0), playlists);
        dialog.setTitle("Aggiungi a playlist");
        dialog.setHeaderText("Scegli la playlist");
        dialog.setContentText("Playlist:");

        Optional<Playlist> result = dialog.showAndWait();
        result.ifPresent(playlist -> {
            if (playlist.isManuallyEditable()) {
                playlist.addTrack(selectedTrack);
                Library.getInstance().notifyObservers();
            }
        });
    }

    /** Aggiunge la traccia selezionata alla coda di riproduzione corrente. */
    private void handleAddTrackToQueue() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof Track track) {
            PlaybackManager.getInstance().addToQueue(track);
            refreshQueueViewIfVisible();
        }
    }

    /** Aggiunge l'intera playlist selezionata alla coda di riproduzione. */
    private void handleAddPlaylistToQueue() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof Playlist playlist) {
            PlaybackManager.getInstance().addToQueue(playlist);
            refreshQueueViewIfVisible();
        }
    }

    private void refreshQueueViewIfVisible() {
        if ("Coda di riproduzione".equals(viewTitleLabel.getText())) {
            refreshTableData();
            updateTablePlaceholder();
        }
    }

    /** Avvia la riproduzione immediata della playlist selezionata. */
    @FXML
    private void handlePlayPlaylistAction() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof Playlist selectedPlaylist) {
            PlaybackManager.getInstance().play(selectedPlaylist, false);
            updatePlayerUI();
        }
    }

    /**
     * Gestisce ESCLUSIVAMENTE il toggle Play/Pausa sul brano attualmente in riproduzione.
     * NON tiene conto dell'elemento selezionato nella tabella o nella lista della coda:
     * la selezione visiva è indipendente dallo stato di riproduzione.
     *
     * Se la coda è vuota e non c'è nessun brano in riproduzione, tenta di caricare
     * il primo brano disponibile dalla vista corrente come comportamento di fallback.
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

        // Delega allo State pattern: PlayingState → pausa, PausedState → riprendi, StoppedState → avvia.
        // L'elemento selezionato nella UI non influisce su questa azione.
        manager.pressPlay();
        updatePlayerUI();
    }

    /**
     * Avvia la riproduzione di una traccia specifica scelta dall'utente (es. doppio click sulla tabella).
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

    /** Aggiorna la barra del player e sincronizza la selezione visiva nella tabella. */
    private void updatePlayerUI() {
        playerBarController.updatePlayerUI();
        syncTableSelection();
    }

    /** Sincronizza l'elemento selezionato nella tabella con la traccia effettivamente in riproduzione. */
    private void syncTableSelection() {
        Track currentTrack = PlaybackManager.getInstance().getCurrentTrack();
        if (currentTrack != null) {
            if ("Coda di riproduzione".equals(viewTitleLabel.getText()) && queueListView != null && !queueListView.getItems().isEmpty()) {
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
            } else if (songTableView != null && !songTableView.getItems().isEmpty()) {
                ObservableList<?> items = songTableView.getItems();
                
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).equals(currentTrack)) {
                        final int index = i;
                        javafx.application.Platform.runLater(() -> {
                            songTableView.getSelectionModel().select(index);
                            songTableView.scrollTo(index);
                        });
                        return;
                    }
                }
            }
        }
    }

    /**
     * Mostra un messaggio informativo all'utente.
     * @param title Titolo della finestra.
     * @param header Intestazione del messaggio.
     * @param content Testo dettagliato del messaggio.
     */
    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Mostra un messaggio di errore all'utente.
     * @param title Titolo della finestra.
     * @param header Intestazione dell'errore.
     * @param content Testo dettagliato dell'errore.
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}