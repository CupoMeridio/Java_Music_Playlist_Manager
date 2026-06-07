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

/**
 * Controller per la gestione della vista di inserimento/modifica brano (addTrackView.fxml).
 * Estratto da PrimaryViewController per separare la responsabilità del form
 * dalla gestione della vista principale.
 */
public class AddTrackController {

    private Track currentEditingTrack = null;
    private Observer trackObserver = null;

    // CONTROLLI PER ADD TRACK
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
    
    @FXML
    private CheckComboBox<Tag> addTrackTagComboBox;

    @FXML
    private Button saveTrackButton;

    @FXML
    private VBox loadingOverlay;

    private String selectedFilePath = null;
    private int extractedDuration = 0;

    /**
     * Imposta il brano da modificare. Se null, il form funziona in modalità inserimento.
     * @param track
     */
    public void setCurrentEditingTrack(Track track) {
        this.currentEditingTrack = track;
    }

    /**
     * Imposta l'observer da collegare ai nuovi brani dopo il salvataggio.
     * @param observer
     */
    public void setOnTrackSaved(Observer observer) {
        this.trackObserver = observer;
    }

    /**
     * Inizializza il form con i dati del brano da modificare, oppure
     * con campi vuoti per l'inserimento di un nuovo brano.
     * Deve essere chiamato dopo loader.load().
     * @param editingTrack
     */
    public void initForm(Track editingTrack) {
        this.currentEditingTrack = editingTrack;

        if (formTitleLabel != null) {
            if (currentEditingTrack != null) {
                formTitleLabel.setText("Modifica brano");
            } else {
                formTitleLabel.setText("Aggiungi nuovo brano");
            }
        }

        // Popoliamo la ComboBox del form "Aggiungi brano" con la lista estesa di generi
        if (addTrackGenreComboBox != null) {
            addTrackGenreComboBox.getItems().setAll(
                "Rock", "Pop", "Jazz", "Classica", "Hip Hop", "R&B", "Metal", 
                "Blues", "Country", "Electronic", "Reggae", "Folk", "Punk", 
                "Soul", "Disco", "Funk", "Techno", "House", "Ambient", "Indie",
                "Alternative", "Lo-Fi", "Trap", "Latin", "Generico"
            );
        }

        if (currentEditingTrack != null) {
            addTrackTitleField.setText(currentEditingTrack.getTitle());
            addTrackAuthorField.setText(currentEditingTrack.getAuthor());
            addTrackAlbumField.setText(currentEditingTrack.getAlbum());
            extractedDuration = currentEditingTrack.getDuration(); 
            if (currentEditingTrack.getYear() == null) {
                addTrackYearField.setText("");
            } else {
                addTrackYearField.setText(String.valueOf(currentEditingTrack.getYear()));
            }
            addTrackGenreComboBox.setValue(currentEditingTrack.getGenre());
            
            // Gestione file audio reale
            selectedFilePath = currentEditingTrack.getFilePath();
            if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
                filePathLabel.setText(new File(selectedFilePath).getName());
            } else {
                filePathLabel.setText("Nessun file associato");
            }

            // --- SINCRONIZZAZIONE DELLA CHECKCOMBOBOX IN MODALITÀ MODIFICA ---
            if (addTrackTagComboBox != null) {
                // Svuota le spunte residue per evitare che rimangano quelle del brano aperto in precedenza
                addTrackTagComboBox.getCheckModel().clearChecks();
                
                // Se il brano ha dei tag, accendi le spunte corrispondenti nella tendina
                if (currentEditingTrack.getTags() != null) {
                    currentEditingTrack.getTags().forEach(tag -> {
                        addTrackTagComboBox.getCheckModel().check(tag);
                    });
                }
            }
        } else {
            addTrackTitleField.setText("");
            addTrackAuthorField.setText("");
            addTrackAlbumField.setText("");
            extractedDuration = 0;
            addTrackYearField.setText("");
            addTrackGenreComboBox.setValue(null);
            selectedFilePath = null;
            filePathLabel.setText("Nessun file selezionato");

            // --- PULIZIA IN CASO DI NUOVO INSERIMENTO ---
            if (addTrackTagComboBox != null) {
                addTrackTagComboBox.getCheckModel().clearChecks();
            }
        }

        // Pulizia eventuale del messaggio di errore ogni volta che si apre il form
        if (addTrackErrorLabel != null) {
            addTrackErrorLabel.setText("");
        }
    }

    // metodo per salvare
    @FXML
    private void handleSaveTrack() {
        try {
            String title = addTrackTitleField.getText().trim();
            String author = addTrackAuthorField.getText().trim();
            String album = addTrackAlbumField.getText().trim();
            String genre = addTrackGenreComboBox.getValue();
            String yearText = addTrackYearField.getText().trim();

            List<Tag> selectedTags = addTrackTagComboBox != null ? addTrackTagComboBox.getCheckModel().getCheckedItems() : null;

            // Titolo e File sono obbligatori
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

            // creo ed aggiorno il brano
            if (currentEditingTrack != null) {
                currentEditingTrack.setTitle(title);
                currentEditingTrack.setAuthor(author);
                currentEditingTrack.setAlbum(album);
                currentEditingTrack.setDuration(extractedDuration);
                currentEditingTrack.setGenre(genre);
                currentEditingTrack.setYear(year);
                currentEditingTrack.setFilePath(selectedFilePath);
                
                // Aggiornamento dei Tag nella modifica
                currentEditingTrack.removeAllTags();
                if (selectedTags != null) {
                    selectedTags.forEach(tag -> {
                        if (tag != null) currentEditingTrack.addTag(tag);
                    });
                }
                
                Library.getInstance().notifyObservers();
                System.out.println("Brano modificato: " + currentEditingTrack.getTitle());
            } else {
                // Costruttore aggiornato con selectedFilePath ed estratto la durata dai metadati
                Track track = new Track(title, author, album, extractedDuration, genre, year, selectedFilePath);
                
                if (selectedTags != null) {
                    selectedTags.forEach(t -> {
                        if (t != null) track.addTag(t);
                    });
                }
                
                Library.getInstance().addTrack(track);
                if (trackObserver != null) {
                    track.attach(trackObserver);
                }
                System.out.println("Brano aggiunto: " + track.getTitle());
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

    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona File Audio");
        
        // Imposta la cartella predefinita "brani di prova" se presente
        File defaultDir = new File(System.getProperty("user.dir"), "brani di prova");
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            fileChooser.setInitialDirectory(defaultDir);
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File Audio", "*.mp3", "*.wav", "*.m4a")
        );

        File file = fileChooser.showOpenDialog(addTrackTitleField.getScene().getWindow());
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            filePathLabel.setText(file.getName());
            
            // Attiviamo l'overlay di caricamento
            setLoading(true);

            // Tentativo di estrazione metadati
            try {
                Media media = new Media(file.toURI().toString());
                MediaPlayer tempPlayer = new MediaPlayer(media);
                
                // 1. Ascolta i cambiamenti nei metadati (vengono caricati in modo asincrono)
                media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                    if (change.wasAdded()) {
                        String key = change.getKey();
                        Object value = change.getValueAdded();
                        
                        Platform.runLater(() -> {
                            switch (key.toLowerCase()) {
                                case "title" -> {
                                    if (addTrackTitleField.getText().isEmpty() || addTrackTitleField.getText().equals(file.getName().substring(0, file.getName().lastIndexOf('.')))) {
                                        addTrackTitleField.setText(value.toString());
                                    }
                                }
                                case "artist" -> {
                                    if (addTrackAuthorField.getText().isEmpty()) {
                                        addTrackAuthorField.setText(value.toString());
                                    }
                                }
                                case "album" -> {
                                    if (addTrackAlbumField.getText().isEmpty()) {
                                        addTrackAlbumField.setText(value.toString());
                                    }
                                }
                                case "year", "date" -> {
                                    if (addTrackYearField.getText().isEmpty()) {
                                        String yearVal = value.toString();
                                        if (yearVal.length() >= 4) {
                                            addTrackYearField.setText(yearVal.substring(0, 4));
                                        }
                                    }
                                }
                                case "genre" -> {
                                    if (addTrackGenreComboBox.getValue() == null) {
                                        String extractedGenre = value.toString();
                                        for (String availableGenre : addTrackGenreComboBox.getItems()) {
                                            if (availableGenre.equalsIgnoreCase(extractedGenre)) {
                                                addTrackGenreComboBox.setValue(availableGenre);
                                                break;
                                            }
                                        }
                                        if (addTrackGenreComboBox.getValue() == null && !extractedGenre.isEmpty()) {
                                            addTrackGenreComboBox.getItems().add(extractedGenre);
                                            addTrackGenreComboBox.setValue(extractedGenre);
                                        }
                                    }
                                }
                            }
                        });
                    }
                });

                // 2. La durata è disponibile tramite il MediaPlayer quando è in stato READY
                tempPlayer.setOnReady(() -> {
                    if (media.getDuration() != null) {
                        extractedDuration = (int) media.getDuration().toSeconds();
                        System.out.println("[METADATA] Durata estratta: " + extractedDuration + " secondi.");
                    }
                    setLoading(false);
                    tempPlayer.dispose(); 
                });

                // Gestione errore nel caricamento del media
                tempPlayer.setOnError(() -> {
                    System.err.println("[METADATA] Errore nel caricamento del file audio.");
                    setLoading(false);
                    tempPlayer.dispose();
                });

                // 3. Fallback titolo (nome file senza estensione)
                String fileName = file.getName();
                int lastDot = fileName.lastIndexOf('.');
                String nameWithoutExt = (lastDot > 0) ? fileName.substring(0, lastDot) : fileName;
                addTrackTitleField.setText(nameWithoutExt);

                // Timeout di sicurezza
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        if (loadingOverlay.isVisible()) {
                            System.out.println("[METADATA] Timeout analisi file, sblocco manuale.");
                            setLoading(false);
                            Platform.runLater(tempPlayer::dispose);
                        }
                    } catch (InterruptedException ignored) {}
                }).start();

            } catch (Exception e) {
                System.out.println("Errore nel caricamento dei metadati: " + e.getMessage());
                extractedDuration = 0;
                setLoading(false);
            }
        }
    }

    /**
     * Mostra o nasconde l'overlay di caricamento e disabilita il pulsante salva.
     */
    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(loading);
                loadingOverlay.setManaged(loading);
            }
            if (saveTrackButton != null) {
                saveTrackButton.setDisable(loading);
            }
        });
    }

    /**
     * Metodo di inizializzazione di JavaFX. Viene eseguito automaticamente 
     * al caricamento dell'FXML. Popoliamo la CheckComboBox con i tag predefiniti.
     */
    @FXML
    public void initialize() {
        if (addTrackTagComboBox != null) {
            addTrackTagComboBox.getItems().addAll(TagPredefined.values());
        }   
    }
}