package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Playable;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.PlaylistGenerator;
import it.unisa.java_music_playlist_manager.model.AutomaticPlaylist;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import it.unisa.java_music_playlist_manager.model.Observer;
import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import it.unisa.java_music_playlist_manager.model.Tag;
import java.util.Set;

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
 * - Gestisce la logica di navigazione tra le diverse viste (Musica, Playlist, Coda).
 * - Coordina le operazioni CRUD su tracce e playlist tramite menu contestuali e dialoghi.
 */
public class PrimaryViewController implements Observer {

    /** Traccia attualmente in fase di modifica (usata per popolare il form di editing) */
    private Track currentEditingTrack = null;
    
    /** Playlist attualmente aperta nella vista dettaglio */
    private Playlist currentOpenedPlaylist = null;
    private final PlaylistGenerator playlistGenerator = new PlaylistGenerator();

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
            ObservableList<Track> trackList = FXCollections.observableArrayList(currentOpenedPlaylist.getTracks());
            ((TableView<Track>) songTableView).setItems(trackList);
        } else if ("Coda di riproduzione".equals(currentView)) {
            // Mostra la coda "appiattita": estrae tutte le tracce da ogni elemento Playable in coda
            List<Track> flattenedQueue = new ArrayList<>();
            for (Playable p : PlaybackManager.getInstance().getCurrentQueue()) {
                flattenedQueue.addAll(p.getTracks());
            }
            ObservableList<Track> trackList = FXCollections.observableArrayList(flattenedQueue);
            ((TableView<Track>) songTableView).setItems(trackList);
        } else if ("Musica".equals(currentView)) {
            // Mostra l'intera libreria musicale
            ObservableList<Track> trackList = FXCollections.observableArrayList(Library.getInstance().getTracks());
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

        if ("Playlist".equals(currentView)) {
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
    }

    // --- Componenti iniettati da FXML ---
    
    /** Controller della barra laterale (iniettato tramite fx:include) */
    @FXML
    private SidebarController sidebarController;
    
    /** Controller della barra del player (iniettato tramite fx:include) */
    @FXML
    private PlayerController playerBarController;

    @FXML
    private Label viewTitleLabel;
    @FXML
    private Button actionButton;
    @FXML
    private Button shufflePlayButton;
    @FXML
    private Button playPlaylistButton;
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

        // Configurazione del menu contestuale dinamico
        setupContextMenu();

        // Gestione del doppio click per navigazione o riproduzione
        songTableView.setOnMouseClicked(event -> {
            Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                if (currentOpenedPlaylist == null
                        && "Playlist".equals(viewTitleLabel.getText())
                        && selectedItem instanceof Playlist playlist) {
                    openPlaylistDetail(playlist);
                } else if (selectedItem instanceof Track) {
                    handlePlayPauseAction();
                }
            }
        });

        updatePlayerUI();
        System.out.println("Interfaccia grafica inizializzata correttamente.");
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
            boolean isAutomaticPlaylist = currentOpenedPlaylist instanceof AutomaticPlaylist;
            removeFromPlaylistItem.setDisable(noTrackSelected || !isPlaylistDetailView || isAutomaticPlaylist);
            removeFromPlaylistItem.setVisible(!noTrackSelected && isPlaylistDetailView && !isAutomaticPlaylist);
        });

        songTableView.setOnMouseClicked(event -> {
            Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                if (currentOpenedPlaylist == null
                        && "Playlist".equals(viewTitleLabel.getText())
                        && selectedItem instanceof Playlist playlist) {
                    openPlaylistDetail(playlist);
                } else if (selectedItem instanceof Track) {
                    handlePlayPauseAction();
                }
            }
        });

        updatePlayerUI();
        System.out.println("Interfaccia grafica inizializzata correttamente.");
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

        if (selectedItem instanceof Track selectedTrack && currentOpenedPlaylist != null) {
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
            case "Musica" -> handleMusicLibraryAction();
            case "Coda" -> handlePlayQueueAction();
            case "Playlist" -> handlePlaylistAction();
            default -> { }
        }
    }

    /** Configura la vista per mostrare la libreria musicale completa. */
    private void handleMusicLibraryAction() {
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Musica");
        actionButton.setText("Aggiungi brano");
        actionButton.setVisible(true);
        actionButton.setManaged(true);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showSongsColumns();
    }

    /** Configura la vista per mostrare la coda di riproduzione. */
    private void handlePlayQueueAction() {
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Coda di riproduzione");
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showQueueColumns();
    }

    /** Configura la vista per mostrare l'elenco delle playlist (Master View). */
    private void handlePlaylistAction() {
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Playlist");
        actionButton.setText("Nuova playlist");
        actionButton.setVisible(true);
        actionButton.setManaged(true);
        genreFilterContainer.setVisible(false);
        genreFilterContainer.setManaged(false);
        showPlaylistColumns();
    }

    // --- Metodi per la configurazione dinamica delle colonne della TableView ---

    @SuppressWarnings("unchecked")
    private void showQueueColumns() {
        songTableView.getColumns().clear();
        TableColumn<Track, String> titleCol = createColumn("Titolo", 250, t -> t.getTitle());
        TableColumn<Track, String> artistCol = createColumn("Artista", 150, t -> t.getAuthor());
        TableColumn<Track, String> durationCol = createColumn("Durata", 100, t -> formatDuration(t.getDuration()));

        ((TableView<Track>) songTableView).getColumns().addAll(titleCol, artistCol, durationCol);
        updateTablePlaceholder();
        refreshTableData();
    }

    @SuppressWarnings("unchecked")
    private void showSongsColumns() {
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
        
        updateTablePlaceholder();
        refreshTableData();
    }

    @SuppressWarnings("unchecked")
    private void showPlaylistColumns() {
        songTableView.getColumns().clear();
        TableColumn<Playlist, String> nameCol = createColumn("Nome Playlist", 300, p -> p.getTitle());
        TableColumn<Playlist, Integer> countCol = new TableColumn<>("Numero Brani");
        countCol.setPrefWidth(150);
        countCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTrackCount()).asObject());
        TableColumn<Playlist, String> durationCol = createColumn("Durata Totale", 150, p -> formatDuration(p.getDuration()));

        ((TableView<Playlist>) songTableView).getColumns().addAll(nameCol, countCol, durationCol);
        ((TableView<Playlist>) songTableView).setItems(FXCollections.observableArrayList(Library.getInstance().getPlaylists()));
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

    private javafx.scene.control.TableCell<Track, Set<Tag>> createTagCellFactory() {
        return new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Set<Tag> tags, boolean empty) {
                super.updateItem(tags, empty);
                if (empty || tags == null || tags.isEmpty()) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.FlowPane flowPane = new javafx.scene.layout.FlowPane(5, 4);
                    tags.forEach(tag -> {
                        if (tag == null) return;
                        Label badge = new Label(tag.getIcon());
                        badge.setStyle("-fx-background-color: #4A4A4A; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 7; -fx-font-size: 11px; -fx-font-weight: bold;");
                        
                        // Aggiunge un tooltip per mostrare il nome del tag al passaggio del mouse
                        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Tag: " + tag.getName());
                        javafx.scene.control.Tooltip.install(badge, tooltip);
                        
                        flowPane.getChildren().add(badge);
                    });
                    setGraphic(flowPane);
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
     * La playlist vuota viene creata tramite PlaylistGenerator
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
                Playlist playlist = playlistGenerator.createEmptyPlaylist(name);
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
        List<String> options = List.of("Genere", "Anno");

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

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(this::generateAutomaticPlaylistByGenre);
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

        Optional<Integer> result = dialog.showAndWait();

        result.ifPresent(this::generateAutomaticPlaylistByYear);
    }

    /**
     * Genera una playlist automatica filtrata per genere musicale.
     *
     * @param genre Genere scelto dall'utente.
     */
    private void generateAutomaticPlaylistByGenre(String genre) {
        Optional<Playlist> result = playlistGenerator.createPlaylistByGenre(
                genre,
                Library.getInstance().getTracks()
        );

        if (result.isPresent()) {
            saveGeneratedPlaylist(
                    result.get(),
                    "Playlist automatica creata per genere: "
            );
        } else {
            showInfoAlert(
                    "Nessun brano trovato",
                    "Playlist automatica non creata",
                    "Non ci sono brani con il genere selezionato."
            );
        }
    }

    /**
     * Genera una playlist automatica filtrata per anno di uscita.
     *
     * @param year Anno scelto dall'utente.
     */
    private void generateAutomaticPlaylistByYear(Integer year) {
        Optional<Playlist> result = playlistGenerator.createPlaylistByYear(
                year,
                Library.getInstance().getTracks()
        );

        if (result.isPresent()) {
            saveGeneratedPlaylist(
                    result.get(),
                    "Playlist automatica creata per anno: "
            );
        } else {
            showInfoAlert(
                    "Nessun brano trovato",
                    "Playlist automatica non creata",
                    "Non ci sono brani per l'anno selezionato."
            );
        }
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
            if (!(playlist instanceof AutomaticPlaylist)) {
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
            playlist.addTrack(selectedTrack);
            Library.getInstance().notifyObservers();
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

    @FXML
    private void handleShufflePlayAction() {
        // Logica per lo shuffle (da implementare nel PlaybackManager)
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
     * Gestisce la logica di avvio riproduzione quando viene cliccato Play o fatto doppio click.
     * Si occupa di caricare il contesto corretto (tutta la libreria o la playlist corrente).
     */
    private void handlePlayPauseAction() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
        PlaybackManager manager = PlaybackManager.getInstance();

        // Se non c'è selezione, carica il primo brano della vista corrente
        if (selectedItem == null && manager.getCurrentQueue().isEmpty()) {
            if (currentOpenedPlaylist != null && !currentOpenedPlaylist.getTracks().isEmpty()) {
                manager.selectAndLoadTrack(currentOpenedPlaylist.getTracks().get(0), List.of(currentOpenedPlaylist));
            } else if (!Library.getInstance().getTracks().isEmpty()) {
                manager.selectAndLoadTrack(Library.getInstance().getTracks().get(0), Library.getInstance().getTracks());
            }
        }

        // Se c'è una selezione, imposta il contesto di riproduzione
        if (selectedItem instanceof Track selectedTrack) {
            if (currentOpenedPlaylist != null) manager.selectAndLoadTrack(selectedTrack, List.of(currentOpenedPlaylist));
            else if ("Coda di riproduzione".equals(viewTitleLabel.getText())) manager.selectAndLoadTrack(selectedTrack, manager.getCurrentQueue());
            else manager.selectAndLoadTrack(selectedTrack, Library.getInstance().getTracks());
        }

        manager.pressPlay();
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
        if (currentTrack != null && songTableView != null && !songTableView.getItems().isEmpty()) {
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