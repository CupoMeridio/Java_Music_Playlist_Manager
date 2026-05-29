package it.unisa.java_music_playlist_manager;

import java.io.IOException;
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

/**
  * Controller per la gestione della vista principale (primaryView.fxml).
  * La struttura dei campi annotati con @FXML e i collegamenti ai metodi di gestione eventi
  * sono configurati automaticamente tramite l'integrazione tra SceneBuilder e NetBeans.
  * 
  * Attualmente i metodi di gestione eventi implementano stampe a console come placeholder
  * in attesa dell'integrazione delle classi di logica e dei modelli dati del programma.
  */
public class PrimaryViewController {

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

    // METODO DI INIZIALIZZAZIONE
    @FXML
    public void initialize() {
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
        
        System.out.println("Interfaccia grafica inizializzata correttamente.");
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
    private void showSongsColumns() {
         songTableView.getColumns().clear();
         
         TableColumn titleCol = new TableColumn("Titolo");
         titleCol.setPrefWidth(200);
         
         TableColumn artistCol = new TableColumn("Artista");
         artistCol.setPrefWidth(150);
         
         TableColumn albumCol = new TableColumn("Album");
         albumCol.setPrefWidth(150);
         
         TableColumn yearCol = new TableColumn("Anno");
         yearCol.setPrefWidth(80);
         
         TableColumn genreCol = new TableColumn("Genere");
         genreCol.setPrefWidth(120);
         
         TableColumn durationCol = new TableColumn("Durata");
         durationCol.setPrefWidth(80);
         
         songTableView.getColumns().addAll(titleCol, artistCol, albumCol, yearCol, genreCol, durationCol);
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

    @FXML
    private void handleActionBtnClick() {
        String currentView = viewTitleLabel.getText();
        if ("Musica".equals(currentView)) {
            System.out.println("Azione: Apertura dialogo per aggiungere un nuovo brano alla libreria");
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
        System.out.println("Player: Canzone precedente");
    }

    @FXML
    private void handlePlayPauseAction() {
        System.out.println("Player: Play / Pausa");
    }

    @FXML
    private void handleNextAction() {
        System.out.println("Player: Canzone successiva");
    }

    @FXML
    private void handleRepeatToggle() {
        System.out.println("Player: Toggle ripetizione (ciclo)");
    }

    @FXML
    private void handleVolumeMuteToggle() {
        System.out.println("Player: Muto / Attiva audio");
    }

}
