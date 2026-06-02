package it.unisa.java_music_playlist_manager;

import java.io.IOException;
import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

    @Override
    public void update() {
        if (songTableView != null) {
            refreshTableData();
            songTableView.refresh();
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshTableData() {
        if ("Musica".equals(viewTitleLabel.getText()) || "Coda di riproduzione".equals(viewTitleLabel.getText())) {
            ObservableList<Track> trackList = FXCollections.observableArrayList(Library.getInstance().getTracks());
            ((TableView<Track>) songTableView).setItems(trackList);
        }
    }

    // CONTROLLI BARRA LATERALE
    @FXML
    private TextField searchField;
    @FXML
    private Button musicLibraryButton;
    @FXML
    private Button playQueueButton;
    @FXML
    private Button playlistButton;

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

    // CONTROLLI BARRA DI RIPRODUZIONE
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Slider progressSlider;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private ImageView albumCoverImageView;
    @FXML
    private Label currentTrackTitle;
    @FXML
    private Label currentTrackDetails;
    @FXML
    private Button shuffleButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button repeatButton;
    @FXML
    private Button volumeButton;
    @FXML
    private Slider volumeSlider;

    // CONTROLLI PER ADD TRACK
    @FXML
    private TextField addTrackTitleField;

    @FXML
    private TextField addTrackAuthorField;

    @FXML
    private TextField addTrackDurationField;

    @FXML
    private TextField addTrackYearField;

    @FXML
    private Label addTrackErrorLabel;

    @FXML
    private ComboBox<String> addTrackGenreComboBox;

    @FXML
    private Label formTitleLabel;

    // METODO DI INIZIALIZZAZIONE
    @FXML
    public void initialize() {
        Library.getInstance().attach(this);

        if (sortComboBox != null) {
            sortComboBox.getItems().addAll("A - Z", "Z - A", "Artista", "Anno", "Durata");
        }
        if (genreComboBox != null) {
            genreComboBox.getItems().addAll("Tutti i generi", "Pop", "Rock", "Jazz", "Classica", "Hip Hop");
        }

        // Inizializzazione tempi a zero
        if (currentTimeLabel != null) {
            currentTimeLabel.setText("00:00:00");
        }
        if (totalTimeLabel != null) {
            totalTimeLabel.setText("00:00:00");
        }

        // Inizializzazione metadati brano a vuoto
        if (currentTrackTitle != null) {
            currentTrackTitle.setText("");
        }
        if (currentTrackDetails != null) {
            currentTrackDetails.setText("");
        }

        // Configurazione iniziale delle colonne (Vista Brani)
        showSongsColumns();

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Modifica brano");
        editItem.setOnAction(e -> handleEditTrack());

        MenuItem deleteItem = new MenuItem("Elimina brano");
        deleteItem.setOnAction(e -> handleDeleteTrack());

        contextMenu.getItems().addAll(editItem, deleteItem);
        songTableView.setContextMenu(contextMenu);

        contextMenu.setOnShowing(e -> {
            Object selectedItem = songTableView.getSelectionModel().getSelectedItem();
            boolean noTrackSelected = !(selectedItem instanceof Track);

            editItem.setDisable(noTrackSelected);
            deleteItem.setDisable(noTrackSelected);
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

    // GESTORI EVENTI BARRA LATERALE
    @FXML
    private void handleMusicLibraryAction() {
        viewTitleLabel.setText("Musica");
        actionButton.setText("Aggiungi brano");
        actionButton.setVisible(true);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showSongsColumns();
        System.out.println("Navigazione: Libreria musicale (Vista Brani)");
    }

    @FXML
    private void handlePlayQueueAction() {
        viewTitleLabel.setText("Coda di riproduzione");
        actionButton.setVisible(false);
        genreFilterContainer.setVisible(true);
        genreFilterContainer.setManaged(true);
        showSongsColumns(); // La coda mostra solitamente i brani
        System.out.println("Navigazione: Coda di riproduzione");
    }

    @FXML
    private void handlePlaylistAction() {
        viewTitleLabel.setText("Playlist");
        actionButton.setText("Nuova playlist");
        actionButton.setVisible(true);
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

         refreshTableData();
     }

     /**
      * Configura le colonne della tabella per la visualizzazione delle playlist.
      */
     private void showPlaylistColumns() {
         songTableView.getColumns().clear();

         TableColumn nameCol = new TableColumn("Nome Playlist");
         nameCol.setPrefWidth(300);

         TableColumn countCol = new TableColumn("Numero Brani");
         countCol.setPrefWidth(150);

         TableColumn dateCol = new TableColumn("Data Creazione");
         dateCol.setPrefWidth(200);

         songTableView.getColumns().addAll(nameCol, countCol, dateCol);
     }

    private void openAddTrackView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/views/addTrackView.fxml"));

            // Usa lo stesso controller già esistente
            loader.setController(this);

            Parent root = loader.load();

            if (formTitleLabel != null) {
                if (currentEditingTrack != null) {
                    formTitleLabel.setText("Modifica brano");
                } else {
                    formTitleLabel.setText("Aggiungi nuovo brano");
                }
            }

            // Popoliamo la ComboBox del form "Aggiungi brano".
            // Questa operazione va fatta dopo loader.load(), perché solo dopo il caricamento
            // dell'FXML il campo addTrackGenreComboBox viene collegato al nodo grafico.
            if (addTrackGenreComboBox != null) {
                addTrackGenreComboBox.getItems().setAll(
                        "Pop",
                        "Rock",
                        "Jazz",
                        "Classica",
                        "Hip Hop",
                        "Rap",
                        "Elettronica"
                );
            }

            if (currentEditingTrack != null) {
                addTrackTitleField.setText(currentEditingTrack.getTitle());
                addTrackAuthorField.setText(currentEditingTrack.getAuthor());
                addTrackDurationField.setText(String.valueOf(currentEditingTrack.getDuration()));
                addTrackYearField.setText(String.valueOf(currentEditingTrack.getYear()));
                addTrackGenreComboBox.setValue(currentEditingTrack.getGenre());
            } else {
                addTrackTitleField.setText("");
                addTrackAuthorField.setText("");
                addTrackDurationField.setText("");
                addTrackYearField.setText("");
                addTrackGenreComboBox.setValue(null);
            }

            // Pulizia eventuale del messaggio di errore ogni volta che si apre il form
            if (addTrackErrorLabel != null) {
                addTrackErrorLabel.setText("");
            }

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
        }
    }

    // GESTORI EVENTI AREA CENTRALE=
    @FXML
    private void handleShufflePlayAction() {
        System.out.println("Azione: Avvio riproduzione casuale di tutta la libreria");
    }

    // GESTORI EVENTI BARRA DI RIPRODUZIONE (PLAYER)
    @FXML
    private void handleShuffleToggle() {
        System.out.println("Player: Toggle riproduzione casuale");
    }

    @FXML
    private void handlePrevAction() {
        System.out.println("Player: Richiesta traccia precedente.");
        // Delega allo stato corrente tramite il manager
        PlaybackManager.getInstance().pressPrevious();
        // Aggiorna i testi a schermo
        updatePlayerUI();
    }

    @FXML
    private void handlePlayPauseAction() {
        System.out.println("[CONTROLLER] Click sul pulsante Play/Pause.");

        // 1. Recuperiamo l'elemento attualmente selezionato nella tabella
        Object selectedItem = songTableView.getSelectionModel().getSelectedItem();

        // 2. Se l'utente ha effettivamente selezionato una canzone
        if (selectedItem instanceof Track) {
            Track selectedTrack = (Track) selectedItem;
            String currentView = viewTitleLabel.getText();

            if ("Musica".equals(currentView) || "Coda di riproduzione".equals(currentView)) {
                System.out.println("[CONTROLLER] Brano selezionato dalla Libreria.");
                // Carica tutti i brani della libreria e imposta l'indice su quello selezionato
                PlaybackManager.getInstance().selectAndLoadTrack(selectedTrack, Library.getInstance().getTracks());

            } else if ("Playlist".equals(currentView)) {
                System.out.println("[CONTROLLER] Brano selezionato da una Playlist.");
                // TODO: Quando sarà pronto  Playlist, qua prendiamo i brani della playlist corrente:
                // List<Track> playlistTracks = currentSelectedPlaylist.getTracks();
                // PlaybackManager.getInstance().selectAndLoadTrack(selectedTrack, playlistTracks);

                // da cancellare dopo: visto che non c'è playlist per ora prenndo sempre da library
                PlaybackManager.getInstance().selectAndLoadTrack(selectedTrack, Library.getInstance().getTracks());
            }
        }

        // 3. Delega l'azione di Play allo stato del PlaybackManager (Stopped, Playing o Paused)
        PlaybackManager.getInstance().pressPlay();

        // 4. Aggiorna l'interfaccia grafica inferiore
        updatePlayerUI();
    }

    @FXML
    private void handleNextAction() {
        System.out.println("Player: Click sul pulsante Traccia Successiva.");
        // Delega allo stato corrente tramite il manager
        PlaybackManager.getInstance().pressNext();
        // Sincronizza l'interfaccia grafica
        updatePlayerUI();
    }
    @FXML
    private void handleRepeatToggle() {
        System.out.println("Player: Toggle ripetizione (ciclo)");
    }

    @FXML
    private void handleVolumeMuteToggle() {
        System.out.println("Player: Muto / Attiva audio");
    }

    // metodo per salvare
    @FXML
    private void handleSaveTrack() {
        try {
            // salvo yearText durationText e come String non come int perchè altrimenti non posso verificare se il field è vuoto
            String title = addTrackTitleField.getText().trim();
            String author = addTrackAuthorField.getText().trim();
            String durationText = addTrackDurationField.getText().trim();
            String genre = addTrackGenreComboBox.getValue();
            String yearText = addTrackYearField.getText().trim();


            // campi vuoti
            if (title.isEmpty() || author.isEmpty() || durationText.isEmpty()
                    || genre == null || yearText.isEmpty()) {
                addTrackErrorLabel.setText("Compila tutti i campi obbligatori.");
                return;
            }


            int duration;
            int year;

            try {
                duration = Integer.parseInt(durationText);
            } catch (NumberFormatException e) {
                addTrackErrorLabel.setText("La durata deve essere un numero valido.");
                return;
            }

            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException e) {
                addTrackErrorLabel.setText("L'anno deve essere un numero valido.");
                return;
            }

            // creo ed aggiorno il brano
            if (currentEditingTrack != null) {
                currentEditingTrack.setTitle(title);
                currentEditingTrack.setAuthor(author);
                currentEditingTrack.setDuration(duration);
                currentEditingTrack.setGenre(genre);
                currentEditingTrack.setYear(year);
                System.out.println("Brano modificato: " + currentEditingTrack.getTitle());
            } else {
                Track track = new Track(title, author, duration, genre, year);
                track.attach(this);

                // Aggiunta alla Library.
                // La Library notificherà automaticamente gli Observer.
                Library.getInstance().addTrack(track);
                System.out.println("Brano aggiunto: " + track.getTitle());
                System.out.println("Numero brani in libreria: " + Library.getInstance().getTracks().size());
            }

            // chiudo finestra
            Stage stage = (Stage) addTrackTitleField.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            addTrackErrorLabel.setText(e.getMessage());
        }
    }

    // Metodo per annullare l'aggiunta di una track dal form di addTrack
    @FXML
    private void handleCancelAddTrack() {
        Stage stage = (Stage) addTrackTitleField.getScene().getWindow();
        stage.close();
    }

    // GESTIONE STATO

    /**
     * Sincronizza le Label della barra di riproduzione inferiore
     * con il brano correntemente selezionato nel PlaybackManager.
     */
    private void updatePlayerUI() {
        PlaybackManager manager = PlaybackManager.getInstance();
        Track currentTrack = manager.getCurrentTrack();

        if (currentTrack != null) {
            // 1. Aggiorna i testi del player in basso
            if (currentTrackTitle != null) {
                currentTrackTitle.setText(currentTrack.getTitle());
            }
            if (currentTrackDetails != null) {
                currentTrackDetails.setText(currentTrack.getAuthor());
            }

            // =================================================================
            // AGGIUNTA: Sincronizzazione del cursore/selezione della tabella
            // =================================================================
            if (songTableView != null && !songTableView.getItems().isEmpty()) {
                int indexAttivo = manager.getCurrentIndex();

                // Controlliamo di essere nei limiti della tabella per sicurezza
                if (indexAttivo >= 0 && indexAttivo < songTableView.getItems().size()) {
                    // Seleziona la riga corrispondente all'indice del manager
                    songTableView.getSelectionModel().select(indexAttivo);

                    // Muove visibilmente lo scroll della tabella per non perdere di vista il brano
                    songTableView.scrollTo(indexAttivo);
                }
            }

        } else {
            // Se non c'è nessun brano in riproduzione (coda vuota o finita)
            if (currentTrackTitle != null) {
                currentTrackTitle.setText("Nessun brano in riproduzione");
            }
            if (currentTrackDetails != null) {
                currentTrackDetails.setText("");
            }

            // Opzionale: pulisce la selezione dalla tabella se tutto è fermo
            if (songTableView != null) {
                songTableView.getSelectionModel().clearSelection();
            }
        }
    }
}




