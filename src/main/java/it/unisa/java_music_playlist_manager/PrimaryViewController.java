package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import it.unisa.java_music_playlist_manager.model.Observer;

/**
  * Controller per la gestione della vista principale (primaryView.fxml).
  * La struttura dei campi annotati con @FXML e i collegamenti ai metodi di gestione eventi
  * sono configurati automaticamente tramite l'integrazione tra SceneBuilder e NetBeans.
  *
  * Attualmente i metodi di gestione eventi implementano stampe a console come placeholder
  * in attesa dell'integrazione delle classi di logica e dei modelli dati del programma.
  */
public class PrimaryViewController implements Observer {

    private Track currentEditingTrack = null;
    private Playlist currentOpenedPlaylist = null;

    @Override
    public void update() {
        if (songTableView != null) {
            refreshTableData();
            songTableView.refresh();
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshTableData() {
        String currentView = viewTitleLabel.getText();

        if (currentOpenedPlaylist != null) {
            // Se c'è una playlist aperta, mostra i brani della playlist
            ObservableList<Track> trackList = FXCollections.observableArrayList(currentOpenedPlaylist.getTracks());
            ((TableView<Track>) songTableView).setItems(trackList);
        } else if ("Coda di riproduzione".equals(currentView)) {
            // se siamo nella coda, mostra i brani in riproduzione nel playbackmanager
            List<Track> actualQueue = PlaybackManager.getInstance().getCurrentQueue();
            ObservableList<Track> trackList = FXCollections.observableArrayList(actualQueue);
            ((TableView<Track>) songTableView).setItems(trackList);
        } else if ("Musica".equals(currentView)) {
            // Se siamo in Musica, mostra i brani totali della Libreria
            ObservableList<Track> trackList = FXCollections.observableArrayList(Library.getInstance().getTracks());
            ((TableView<Track>) songTableView).setItems(trackList);
        }
    }

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

    // CONTROLLER INIETTATI DA fx:include
    @FXML
    private SidebarController sidebarController;
    @FXML
    private PlayerController playerBarController;

    // CONTROLLI AREA CENTRALE
    @FXML
    private Label viewTitleLabel;
    @FXML
    private Button actionButton;
    @FXML
    private Button shufflePlayButton;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private HBox genreFilterContainer;
    @FXML
    private ComboBox<String> genreComboBox;

    // Tabella Brani (tipi generici generici '?' in attesa del modello Track)
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

    // METODO DI INIZIALIZZAZIONE
    @FXML
    public void initialize() {
        Library.getInstance().attach(this);

        // Collegamento callback del SidebarController
        sidebarController.setOnNavigate(this::handleNavigate);

        // Collegamento callback del PlayerController
        playerBarController.setOnPlayPauseClicked(() -> handlePlayPauseAction());
        playerBarController.setOnPlayerStateChanged(() -> syncTableSelection());

        if (sortComboBox != null) {
            sortComboBox.getItems().addAll("A - Z", "Z - A", "Artista", "Anno", "Durata");
        }
        if (genreComboBox != null) {
            genreComboBox.getItems().addAll("Tutti i generi", "Pop", "Rock", "Jazz", "Classica", "Hip Hop");
        }

        // Configurazione iniziale delle colonne (Vista Brani)
        showSongsColumns();

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Modifica brano");
        editItem.setOnAction(e -> handleEditTrack());

        MenuItem deleteItem = new MenuItem("Elimina brano");
        deleteItem.setOnAction(e -> handleDeleteTrack());

        MenuItem addToPlaylistItem = new MenuItem("Aggiungi a playlist");
        addToPlaylistItem.setOnAction(e -> handleAddTrackToPlaylist());

        MenuItem editPlaylistItem = new MenuItem("Modifica playlist");
        editPlaylistItem.setOnAction(e -> handleEditPlaylist());

        MenuItem deletePlaylistItem = new MenuItem("Elimina playlist");
        deletePlaylistItem.setOnAction(e -> handleDeletePlaylist());

        contextMenu.getItems().addAll(editItem, deleteItem, addToPlaylistItem, editPlaylistItem, deletePlaylistItem);
        songTableView.setContextMenu(contextMenu);

        contextMenu.setOnShowing(e -> {
            Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
            boolean noTrackSelected = !(selectedItem instanceof Track);
            boolean noPlaylistSelected = !(selectedItem instanceof Playlist);

            editItem.setDisable(noTrackSelected);
            deleteItem.setDisable(noTrackSelected);
            addToPlaylistItem.setDisable(noTrackSelected);

            editItem.setVisible(!noTrackSelected);
            deleteItem.setVisible(!noTrackSelected);
            addToPlaylistItem.setVisible(!noTrackSelected);

            editPlaylistItem.setDisable(noPlaylistSelected);
            deletePlaylistItem.setDisable(noPlaylistSelected);
            editPlaylistItem.setVisible(!noPlaylistSelected);
            deletePlaylistItem.setVisible(!noPlaylistSelected);
        });

        songTableView.setOnMouseClicked(event -> {
            Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2
                    && currentOpenedPlaylist == null
                    && "Playlist".equals(viewTitleLabel.getText())
                    && selectedItem instanceof Playlist playlist) {
                openPlaylistDetail(playlist);
            }
        });

        updatePlayerUI();
        System.out.println("Interfaccia grafica inizializzata correttamente.");
    }

    @FXML
    private void handleEditTrack() {
        Object selected = songTableView.getSelectionModel().getSelectedItem();
        if (selected instanceof Track) {
            currentEditingTrack = (Track) selected;
            openAddTrackView();
        }
    }

    @FXML
    private void handleDeleteTrack() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();

        if (!(selectedItem instanceof Track)) {
            return;
        }

        Track selectedTrack = (Track) selectedItem;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare il brano selezionato?");
        confirmAlert.setContentText(
                "Stai per eliminare il brano \"" + selectedTrack.getTitle() + "\" dalla libreria.\n" +
                        "Il brano verrà rimosso anche da tutte le playlist in cui è presente.\n\n" +
                        "Vuoi continuare?"
        );

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

    private void handleEditPlaylist() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();

        if (!(selectedItem instanceof Playlist selectedPlaylist)) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedPlaylist.getTitle());
        dialog.setTitle("Modifica playlist");
        dialog.setHeaderText("Modifica il nome della playlist");
        dialog.setContentText("Nome playlist:");
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            try {
                selectedPlaylist.setTitle(name);
                Library.getInstance().notifyObservers();
                showPlaylistColumns();
                System.out.println("Playlist modificata: " + selectedPlaylist.getTitle());
            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText("Nome playlist non valido");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void handleDeletePlaylist() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();

        if (!(selectedItem instanceof Playlist selectedPlaylist)) {
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare la playlist selezionata?");
        confirmAlert.setContentText(
                "Stai per eliminare la playlist \"" + selectedPlaylist.getTitle() + "\".\n\n" +
                        "I brani resteranno disponibili nella libreria musicale.\n\n" +
                        "Vuoi continuare?"
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean removed = Library.getInstance().removePlaylist(selectedPlaylist);

            if (removed) {
                showPlaylistColumns();
                System.out.println("Playlist eliminata: " + selectedPlaylist.getTitle());
            } else {
                System.out.println("La playlist selezionata non è stata trovata nella libreria.");
            }
        } else {
            System.out.println("Eliminazione playlist annullata dall'utente.");
        }
    }

    // GESTORI EVENTI BARRA LATERALE (chiamati dal SidebarController tramite callback)
    private void handleNavigate(String viewId) {
        if ("Musica".equals(viewId)) {
            handleMusicLibraryAction();
        } else if ("Coda".equals(viewId)) {
            handlePlayQueueAction();
        } else if ("Playlist".equals(viewId)) {
            handlePlaylistAction();
        }
    }

    private void handleMusicLibraryAction() {
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Musica");
        actionButton.setText("Aggiungi brano");
        actionButton.setVisible(true);
        actionButton.setManaged(true);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showSongsColumns();
        System.out.println("Navigazione: Libreria musicale (Vista Brani)");
    }

    private void handlePlayQueueAction() {
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Coda di riproduzione");
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showSongsColumns(); // La coda mostra solitamente i brani
        System.out.println("Navigazione: Coda di riproduzione");
    }

    private void handlePlaylistAction() {
        currentOpenedPlaylist = null;
        viewTitleLabel.setText("Playlist");
        actionButton.setText("Nuova playlist");
        actionButton.setVisible(true);
        actionButton.setManaged(true);
        genreFilterContainer.setVisible(false);
        genreFilterContainer.setManaged(false);
        showPlaylistColumns();
        System.out.println("Navigazione: Riepilogo Playlist (Master View)");
    }

    /**
     * Configura le colonne della tabella per la visualizzazione dei brani.
     */
    @SuppressWarnings("unchecked")
    private void showSongsColumns() {
         songTableView.getColumns().clear();

         TableColumn<Track, String> titleCol = new TableColumn<>("Titolo");
         titleCol.setPrefWidth(200);
         titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

         TableColumn<Track, String> artistCol = new TableColumn<>("Artista");
         artistCol.setPrefWidth(150);
         artistCol.setCellValueFactory(new PropertyValueFactory<>("author"));

         TableColumn<Track, String> albumCol = new TableColumn<>("Album");
         albumCol.setPrefWidth(150);

         TableColumn<Track, Integer> yearCol = new TableColumn<>("Anno");
         yearCol.setPrefWidth(80);
         yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

         TableColumn<Track, String> genreCol = new TableColumn<>("Genere");
         genreCol.setPrefWidth(120);
         genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));

         TableColumn<Track, Integer> durationCol = new TableColumn<>("Durata");
         durationCol.setPrefWidth(80);
         durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

         ((TableView<Track>) songTableView).getColumns().addAll(titleCol, artistCol, albumCol, yearCol, genreCol, durationCol);

         updateTablePlaceholder();
         refreshTableData();
     }

     /**
      * Configura le colonne della tabella per la visualizzazione delle playlist.
      */
     @SuppressWarnings("unchecked")
     private void showPlaylistColumns() {
         songTableView.getColumns().clear();

         TableColumn<Playlist, String> nameCol = new TableColumn<>("Nome Playlist");
         nameCol.setPrefWidth(300);
         nameCol.setCellValueFactory(new PropertyValueFactory<>("title"));

         TableColumn<Playlist, Integer> countCol = new TableColumn<>("Numero Brani");
         countCol.setPrefWidth(150);
         countCol.setCellValueFactory(new PropertyValueFactory<>("trackCount"));

         TableColumn<Playlist, Integer> durationCol = new TableColumn<>("Durata");
         durationCol.setPrefWidth(150);
         durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

         ((TableView<Playlist>) songTableView).getColumns().addAll(nameCol, countCol, durationCol);

         ObservableList<Playlist> playlistList = FXCollections.observableArrayList(Library.getInstance().getPlaylists());
         ((TableView<Playlist>) songTableView).setItems(playlistList);
         updateTablePlaceholder();
     }

    private void openPlaylistDetail(Playlist playlist) {
        currentOpenedPlaylist = playlist;
        viewTitleLabel.setText(playlist.getTitle());
        actionButton.setVisible(false);
        actionButton.setManaged(false);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showSongsColumns();
        System.out.println("Apertura playlist: " + playlist.getTitle());
    }

    private void openCreatePlaylistDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Crea una nuova playlist");
        dialog.setContentText("Nome playlist:");
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            try {
                Playlist playlist = new Playlist(name);
                Library.getInstance().addPlaylist(playlist);
                showPlaylistColumns();
                System.out.println("Playlist creata: " + playlist.getTitle());
            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText("Nome playlist non valido");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void openAddTrackView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/views/addTrackView.fxml"));

            Parent root = loader.load();

            // Recupera il controller creato automaticamente dall'FXMLLoader
            AddTrackController controller = loader.getController();
            controller.setOnTrackSaved(this);
            controller.initForm(currentEditingTrack);

            Stage stage = new Stage();
            stage.setTitle("Aggiungi brano");
            stage.setScene(new Scene(root));

            // Dimensioni minime per evitare che il form diventi troppo piccolo.
            stage.setMinWidth(360);
            stage.setMinHeight(300);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Per ora basta così. Quando Observer sarà completo, la tabella si aggiornerà automaticamente.
            System.out.println("Finestra inserimento brano chiusa.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActionBtnClick() {
        String currentView = viewTitleLabel.getText();
        if ("Musica".equals(currentView)) {
            System.out.println("Azione: Apertura dialogo per aggiungere un nuovo brano alla libreria");
            currentEditingTrack = null;
            openAddTrackView();
        } else if ("Playlist".equals(currentView)) {
            System.out.println("Azione: Apertura dialogo per creare una nuova playlist");
            openCreatePlaylistDialog();
        }
    }

    private void handleAddTrackToPlaylist() {
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();

        if (!(selectedItem instanceof Track selectedTrack)) {
            return;
        }

        List<Playlist> playlists = Library.getInstance().getPlaylists();

        if (playlists.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Nessuna playlist");
            alert.setHeaderText("Non ci sono playlist disponibili");
            alert.setContentText("Crea prima una playlist.");
            alert.showAndWait();
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
            System.out.println(
                    "Brano \"" + selectedTrack.getTitle()
                            + "\" aggiunto alla playlist \""
                            + playlist.getTitle() + "\"."
            );
        });
    }

    // GESTORI EVENTI AREA CENTRALE=
    @FXML
    private void handleShufflePlayAction() {
        System.out.println("Azione: Avvio riproduzione casuale di tutta la libreria");
    }

    // GESTORE PLAY/PAUSE (chiamato dal PlayerController tramite callback)
    private void handlePlayPauseAction() {
        System.out.println("[CONTROLLER] Click sul pulsante Play/Pause.");

        // 1. Recuperiamo l'elemento attualmente selezionato nella tabella
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();

        // 2. Se l'utente ha effettivamente selezionato una canzone
        if (selectedItem instanceof Track) {
            Track selectedTrack = (Track) selectedItem;
            String currentView = viewTitleLabel.getText();

            // Se abbiamo una playlist aperta nel dettaglio, prendiamo i brani da lì
            if (currentOpenedPlaylist != null) {
                System.out.println("[CONTROLLER] Brano selezionato dalla playlist: " + currentOpenedPlaylist.getTitle());
                PlaybackManager.getInstance().selectAndLoadTrack(selectedTrack, currentOpenedPlaylist.getTracks());

            } else if ("Musica".equals(currentView) || "Coda di riproduzione".equals(currentView)) {
                System.out.println("[CONTROLLER] Brano selezionato dalla Libreria.");
                PlaybackManager.getInstance().selectAndLoadTrack(selectedTrack, Library.getInstance().getTracks());
            }
        }

        // 3. Delega l'azione di Play allo stato del PlaybackManager
        PlaybackManager.getInstance().pressPlay();

        // 4. Aggiorna l'interfaccia grafica inferiore
        updatePlayerUI();
    }

    /**
     * Aggiorna l'interfaccia del player e sincronizza la selezione della tabella.
     * Delega l'aggiornamento delle label del player al PlayerController.
     */
    private void updatePlayerUI() {
        playerBarController.updatePlayerUI();
        syncTableSelection();
    }

    /**
     * Sincronizza la selezione della tabella con il brano correntemente
     * in riproduzione nel PlaybackManager.
     */
    private void syncTableSelection() {
        PlaybackManager manager = PlaybackManager.getInstance();
        Track currentTrack = manager.getCurrentTrack();

        if (currentTrack != null) {
            // Sincronizzazione del cursore/selezione della tabella
            if (songTableView != null && !songTableView.getItems().isEmpty()) {
                int indexAttivo = manager.getCurrentIndex();

                if (indexAttivo >= 0 && indexAttivo < songTableView.getItems().size()) {
                    songTableView.getSelectionModel().select(indexAttivo);
                    songTableView.scrollTo(indexAttivo);
                }
            }
        } else {
            if (songTableView != null) {
                songTableView.getSelectionModel().clearSelection();
            }
        }
    }
}
