package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.Observer;
import it.unisa.java_music_playlist_manager.model.TagPredefined;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import javafx.collections.MapChangeListener;
import javafx.application.Platform;

// IMPORTAZIONE PER CONTROLSFX
import org.controlsfx.control.CheckComboBox;

import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.Command;
import it.unisa.java_music_playlist_manager.model.UndoManager;
import it.unisa.java_music_playlist_manager.model.UpdateTrackCommand;
import it.unisa.java_music_playlist_manager.model.AddTrackCommand;




/**
 * Controller per la gestione della vista di inserimento/modifica brano (addTrackView.fxml).
 * Estratto da PrimaryViewController per separare la responsabilità del form
 * dalla gestione della vista principale.
 */
/**
 * AddTrackController gestisce la finestra di dialogo per l'inserimento o la modifica di una traccia.
 * Si occupa della validazione dell'input utente e dell'estrazione automatica dei metadati dai file audio.
 * 
 * Ruolo nel progetto:
 * - Fornisce un'interfaccia form per popolare gli oggetti {@link Track}.
 * - Utilizza {@link MediaPlayer} in modalità "silente" per estrarre durata e metadati (ID3) all'atto della selezione del file.
 * - Integra {@link org.controlsfx.control.CheckComboBox} per permettere l'assegnazione multipla di {@link Tag} ai brani.
 * - Gestisce sia la creazione di nuovi brani che l'aggiornamento di brani esistenti.
 */
public class AddTrackController {

    /** Brano correntemente in fase di editing (null se stiamo aggiungendo un nuovo brano) */
    private Track currentEditingTrack = null;
    
    /** Observer da registrare sul brano appena creato (solitamente il PrimaryViewController) */
    private Observer trackObserver = null;

    @FXML
    private TextField addTrackTitleField;
    @FXML
    private TextField addTrackAuthorField;
    @FXML
    private TextField addTrackAlbumField;
    @FXML
    private TextField addTrackYearField;
    @FXML
    private Label addTrackErrorLabel;
    @FXML
    private Label filePathLabel;
    @FXML
    private ComboBox<String> addTrackGenreComboBox;
    @FXML
    private Label formTitleLabel;
    
    /** Componente della libreria ControlsFX per la selezione multipla dei tag */
    @FXML
    private CheckComboBox<Tag> addTrackTagComboBox;

    @FXML
    private Button saveTrackButton;

    /** Overlay visualizzato durante l'analisi asincrona del file audio selezionato */
    @FXML
    private VBox loadingOverlay;

    /** Percorso del file audio selezionato nel filesystem */
    private String selectedFilePath = null;
    
    /** Durata in secondi estratta automaticamente dal file audio */
    private int extractedDuration = 0;

    /**
     * Imposta l'observer da collegare ai nuovi brani dopo il salvataggio.
     * @param observer L'osservatore (tipicamente il controller principale).
     */
    public void setOnTrackSaved(Observer observer) {
        this.trackObserver = observer;
    }

    /**
     * Inizializza il form con i dati di una traccia esistente (modifica) o con campi vuoti (inserimento).
     * Gestisce la sincronizzazione dei componenti UI con lo stato del Modello.
     * 
     * @param editingTrack La traccia da modificare, o null per un nuovo inserimento.
     */
    public void initForm(Track editingTrack) {
        this.currentEditingTrack = editingTrack;

        // Configurazione titolo del form
        if (formTitleLabel != null) {
            formTitleLabel.setText(currentEditingTrack != null ? "Modifica brano" : "Aggiungi nuovo brano");
        }

        // Popolamento lista generi predefiniti
        if (addTrackGenreComboBox != null) {
            addTrackGenreComboBox.getItems().setAll(
                "Rock", "Pop", "Jazz", "Classica", "Hip Hop", "R&B", "Metal", 
                "Blues", "Country", "Electronic", "Reggae", "Folk", "Punk", 
                "Soul", "Disco", "Funk", "Techno", "House", "Ambient", "Indie",
                "Alternative", "Lo-Fi", "Trap", "Latin", "Generico"
            );
        }

        if (currentEditingTrack != null) {
            // Modalità MODIFICA: popolamento campi con dati esistenti
            addTrackTitleField.setText(currentEditingTrack.getTitle());
            addTrackAuthorField.setText(currentEditingTrack.getAuthor());
            addTrackAlbumField.setText(currentEditingTrack.getAlbum());
            extractedDuration = currentEditingTrack.getDuration(); 
            addTrackYearField.setText(currentEditingTrack.getYear() == null ? "" : String.valueOf(currentEditingTrack.getYear()));
            addTrackGenreComboBox.setValue(currentEditingTrack.getGenre());
            selectedFilePath = currentEditingTrack.getFilePath();
            filePathLabel.setText(selectedFilePath != null ? new File(selectedFilePath).getName() : "Nessun file associato");

            // Sincronizzazione spunte dei Tag
            if (addTrackTagComboBox != null) {
                addTrackTagComboBox.getCheckModel().clearChecks();
                if (currentEditingTrack.getTags() != null) {
                    currentEditingTrack.getTags().forEach(tag -> addTrackTagComboBox.getCheckModel().check(tag));
                }
            }
        } else {
            // Modalità INSERIMENTO: reset dei campi
            resetFields();
        }

        if (addTrackErrorLabel != null) addTrackErrorLabel.setText("");

        // I campi rimangono disabilitati finché non viene selezionato un file audio valido
        setFieldsDisable(selectedFilePath == null || selectedFilePath.isEmpty());
    }

    private void resetFields() {
        addTrackTitleField.setText("");
        addTrackAuthorField.setText("");
        addTrackAlbumField.setText("");
        extractedDuration = 0;
        addTrackYearField.setText("");
        addTrackGenreComboBox.setValue(null);
        selectedFilePath = null;
        filePathLabel.setText("Nessun file selezionato");
        if (addTrackTagComboBox != null) addTrackTagComboBox.getCheckModel().clearChecks();
    }

    /**
     * Gestisce il salvataggio dei dati del form.
     * Crea una nuova {@link Track} o aggiorna quella esistente, validando l'input.
     */
    @FXML
    private void handleSaveTrack() {
        try {
            String title = addTrackTitleField.getText().trim();
            String author = addTrackAuthorField.getText().trim();
            String album = addTrackAlbumField.getText().trim();
            String genre = addTrackGenreComboBox.getValue();
            String yearText = addTrackYearField.getText().trim();
            List<Tag> selectedTags = addTrackTagComboBox != null ? addTrackTagComboBox.getCheckModel().getCheckedItems() : null;

            if (title.isEmpty()) {
                addTrackErrorLabel.setText("Il titolo è obbligatorio.");
                return;
            }
            if (selectedFilePath == null) {
                addTrackErrorLabel.setText("Devi selezionare un file audio.");
                return;
            }

            Integer year = null;
            if (!yearText.isEmpty()) {
                try {
                    year = Integer.parseInt(yearText);
                } catch (NumberFormatException e) {
                    addTrackErrorLabel.setText("L'anno deve essere un numero valido.");
                    return;
                }
            }

            if (currentEditingTrack != null) {
                // MODIFICA TRAMITE COMMAND
                Command updateCmd = new UpdateTrackCommand(currentEditingTrack, title, author, album, genre, year, selectedFilePath, selectedTags);
                UndoManager.getInstance().executeCommand(updateCmd);
            } else {
                // CREAZIONE TRAMITE COMMAND
                Track track = new Track(title, author, album, extractedDuration, genre, year, selectedFilePath);
                if (selectedTags != null) selectedTags.forEach(t -> { if (t != null) track.addTag(t); });
                if (trackObserver != null) track.attach(trackObserver);

                Command addCmd = new AddTrackCommand(Library.getInstance(), track);
                UndoManager.getInstance().executeCommand(addCmd);
            }

            ((Stage) addTrackTitleField.getScene().getWindow()).close();

        } catch (IllegalArgumentException e) {
            addTrackErrorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleCancelAddTrack() {
        ((Stage) addTrackTitleField.getScene().getWindow()).close();
    }

    /**
     * Apre il selettore file e avvia l'analisi asincrona dei metadati.
     * Utilizza un'istanza temporanea di MediaPlayer per estrarre la durata.
     */
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona File Audio");
        
        // Shortcut alla cartella brani di prova se esiste
        File defaultDir = new File(System.getProperty("user.dir"), "brani di prova");
        if (defaultDir.exists() && defaultDir.isDirectory()) fileChooser.setInitialDirectory(defaultDir);

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Audio", "*.mp3", "*.wav", "*.m4a"));

        File file = fileChooser.showOpenDialog(addTrackTitleField.getScene().getWindow());
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            filePathLabel.setText(file.getName());
            setFieldsDisable(false);
            setLoading(true);

            analyzeFileMetadata(file);
        }
    }

    /**
     * Analizza il file audio per estrarre metadati ID3 e durata.
     * @param file Il file da analizzare.
     */
    private void analyzeFileMetadata(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(media);
            
            // Estrazione metadati asincrona (Titolo, Artista, Album, Anno, Genere)
            media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                if (change.wasAdded()) {
                    String key = change.getKey();
                    Object value = change.getValueAdded();
                    Platform.runLater(() -> fillExtractedMetadata(key, value, file.getName()));
                }
            });

            // Estrazione durata quando il player è "READY"
            tempPlayer.setOnReady(() -> {
                if (media.getDuration() != null) {
                    extractedDuration = (int) media.getDuration().toSeconds();
                }
                setLoading(false);
                tempPlayer.dispose(); 
            });

            tempPlayer.setOnError(() -> {
                setLoading(false);
                tempPlayer.dispose();
            });

            // Fallback titolo immediato basato sul nome file
            String fileName = file.getName();
            int lastDot = fileName.lastIndexOf('.');
            addTrackTitleField.setText(lastDot > 0 ? fileName.substring(0, lastDot) : fileName);

            // Timeout di sicurezza per evitare blocchi dell'interfaccia se il file è corrotto
            startAnalysisTimeout(tempPlayer);

        } catch (Exception e) {
            setLoading(false);
        }
    }

    private void fillExtractedMetadata(String key, Object value, String fileName) {
        switch (key.toLowerCase()) {
            case "title" -> {
                if (addTrackTitleField.getText().isEmpty() || addTrackTitleField.getText().contains(fileName.split("\\.")[0]))
                    addTrackTitleField.setText(value.toString());
            }
            case "artist" -> { if (addTrackAuthorField.getText().isEmpty()) addTrackAuthorField.setText(value.toString()); }
            case "album" -> { if (addTrackAlbumField.getText().isEmpty()) addTrackAlbumField.setText(value.toString()); }
            case "year", "date" -> {
                if (addTrackYearField.getText().isEmpty()) {
                    String yearVal = value.toString();
                    if (yearVal.length() >= 4) addTrackYearField.setText(yearVal.substring(0, 4));
                }
            }
            case "genre" -> {
                if (addTrackGenreComboBox.getValue() == null) {
                    String extGenre = value.toString();
                    addTrackGenreComboBox.getItems().stream()
                        .filter(g -> g.equalsIgnoreCase(extGenre))
                        .findFirst()
                        .ifPresent(addTrackGenreComboBox::setValue);
                }
            }
        }
    }

    private void startAnalysisTimeout(MediaPlayer player) {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                if (loadingOverlay.isVisible()) {
                    setLoading(false);
                    Platform.runLater(player::dispose);
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void setFieldsDisable(boolean disable) {
        if (addTrackTitleField != null) addTrackTitleField.setDisable(disable);
        if (addTrackAuthorField != null) addTrackAuthorField.setDisable(disable);
        if (addTrackAlbumField != null) addTrackAlbumField.setDisable(disable);
        if (addTrackGenreComboBox != null) addTrackGenreComboBox.setDisable(disable);
        if (addTrackYearField != null) addTrackYearField.setDisable(disable);
        if (addTrackTagComboBox != null) addTrackTagComboBox.setDisable(disable);
        if (saveTrackButton != null) saveTrackButton.setDisable(disable);
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(loading);
                loadingOverlay.setManaged(loading);
            }
            if (saveTrackButton != null) saveTrackButton.setDisable(loading);
        });
    }

    /**
     * Inizializzazione JavaFX: popolamento dei tag predefiniti nella CheckComboBox.
     */
    @FXML
    public void initialize() {
        if (addTrackTagComboBox != null) {
            addTrackTagComboBox.getItems().addAll(TagPredefined.values());
        }   
    }
}