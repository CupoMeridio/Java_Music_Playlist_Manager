package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Observer;
import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.TagPredefined;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.command.AddTrackCommand;
import it.unisa.java_music_playlist_manager.model.command.Command;
import it.unisa.java_music_playlist_manager.model.command.UndoManager;
import it.unisa.java_music_playlist_manager.model.command.UpdateTrackCommand;

import java.util.List;
import java.util.TreeSet;
import java.io.File;

// IMPORTAZIONI JAUDIOTAGGER rimosse: la logica è ora in TrackMetadataExtractor

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
import javafx.application.Platform;

// IMPORTAZIONE PER CONTROLSFX
import org.controlsfx.control.CheckComboBox;

/**
 * AddTrackController gestisce la finestra di dialogo per l'inserimento o la
 * modifica di una traccia.
 * Si occupa della validazione dell'input utente e dell'estrazione automatica
 * dei metadati dai file audio.
 * 
 * Ruolo nel progetto:
 * - Fornisce un'interfaccia form per popolare gli oggetti {@link Track}.
 * - Utilizza jaudiotagger per estrarre in modo affidabile i tag ID3 (titolo,
 * artista, album, anno, genere).
 * - Utilizza {@link MediaPlayer} esclusivamente per calcolare la durata del
 * brano.
 * - Integra {@link org.controlsfx.control.CheckComboBox} per permettere
 * l'assegnazione multipla di {@link Tag} ai brani.
 * - Gestisce sia la creazione di nuovi brani che l'aggiornamento di brani
 * esistenti.
 */
public class AddTrackController {

    /**
     * Brano correntemente in fase di editing (null se stiamo aggiungendo un nuovo
     * brano)
     */
    private Track currentEditingTrack = null;

    /**
     * Observer da registrare sul brano appena creato (solitamente il
     * PrimaryViewController)
     */
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

    /**
     * Overlay visualizzato durante l'analisi asincrona del file audio selezionato
     */
    @FXML
    private VBox loadingOverlay;

    /** Percorso del file audio selezionato nel filesystem */
    private String selectedFilePath = null;

    /** Durata in secondi estratta automaticamente dal file audio */
    private int extractedDuration = 0;

    /**
     * Imposta l'observer da collegare ai nuovi brani dopo il salvataggio.
     * 
     * @param observer L'osservatore (tipicamente il controller principale).
     */
    public void setOnTrackSaved(Observer observer) {
        this.trackObserver = observer;
    }

    /**
     * Inizializza il form con i dati di una traccia esistente (modifica) o con
     * campi vuoti (inserimento).
     * Gestisce la sincronizzazione dei componenti UI con lo stato del Modello.
     * 
     * @param editingTrack La traccia da modificare, o null per un nuovo
     *                     inserimento.
     */
    public void initForm(Track editingTrack) {
        this.currentEditingTrack = editingTrack;

        // Configurazione titolo del form
        if (formTitleLabel != null) {
            formTitleLabel.setText(currentEditingTrack != null ? "Modifica brano" : "Aggiungi nuovo brano");
        }

        // Popolamento lista generi predefiniti + custom dalla libreria, in ordine
        // alfabetico
        if (addTrackGenreComboBox != null) {
            addTrackGenreComboBox.setEditable(true);
            TreeSet<String> allGenres = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            allGenres.addAll(List.of(
                    "Alternative", "Ambient", "Anime", "Blues", "Bollywood",
                    "Bossa Nova", "Classica", "Classical", "Country", "Dance",
                    "Disco", "Drum and Bass", "Dubstep", "Electronic", "Folk",
                    "Funk", "Gospel", "Grunge", "Hard Rock", "Heavy Metal",
                    "Hip Hop", "House", "Indie", "Jazz", "K-Pop", "Latin",
                    "Lo-Fi", "Metal", "Metalcore", "Pop", "Pop Rock",
                    "Post-Rock", "Progressive Rock", "Punk", "R&B", "Rap",
                    "Reggae", "Reggaeton", "Rock", "Salsa", "Samba", "Soul",
                    "Synthwave", "Techno", "Trap", "World", "Generico"));
            // Aggiunge i generi custom già presenti in libreria
            Library.getInstance().getTracks().forEach(t -> {
                String g = t.getGenre();
                if (g != null && !g.isBlank())
                    allGenres.add(g);
            });
            if (addTrackGenreComboBox.getItems() == null) {
                addTrackGenreComboBox.setItems(javafx.collections.FXCollections.observableArrayList());
            }
            addTrackGenreComboBox.getItems().setAll(allGenres);
        }

        if (currentEditingTrack != null) {
            // Modalità MODIFICA: popolamento campi con dati esistenti
            addTrackTitleField.setText(currentEditingTrack.getTitle());
            addTrackAuthorField.setText(currentEditingTrack.getAuthor());
            addTrackAlbumField.setText(currentEditingTrack.getAlbum());
            extractedDuration = currentEditingTrack.getDuration();
            addTrackYearField.setText(
                    currentEditingTrack.getYear() == null ? "" : String.valueOf(currentEditingTrack.getYear()));
            addTrackGenreComboBox.setValue(currentEditingTrack.getGenre());
            selectedFilePath = currentEditingTrack.getFilePath();
            filePathLabel
                    .setText(selectedFilePath != null ? new File(selectedFilePath).getName() : "Nessun file associato");

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

        if (addTrackErrorLabel != null)
            addTrackErrorLabel.setText("");

        // I campi rimangono disabilitati finché non viene selezionato un file audio
        // valido
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
        if (addTrackErrorLabel != null)
            addTrackErrorLabel.setText("");
        if (addTrackTagComboBox != null)
            addTrackTagComboBox.getCheckModel().clearChecks();
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
            List<Tag> selectedTags = addTrackTagComboBox != null ? addTrackTagComboBox.getCheckModel().getCheckedItems()
                    : null;

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
                Command updateCmd = new UpdateTrackCommand(currentEditingTrack, title, author, album, genre, year,
                        selectedFilePath, selectedTags);
                UndoManager.getInstance().executeCommand(updateCmd);
            } else {
                // CREAZIONE TRAMITE COMMAND
                Track track = new Track(title, author, album, extractedDuration, genre, year, selectedFilePath);
                if (selectedTags != null)
                    selectedTags.forEach(t -> {
                        if (t != null)
                            track.addTag(t);
                    });
                if (trackObserver != null)
                    track.attach(trackObserver);

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
        File userHome = new File(System.getProperty("user.home"));
        File musicDir = new File(userHome, "Music");
        if (!musicDir.exists() || !musicDir.isDirectory()) {
            musicDir = new File(userHome, "Musica");
        }
        if (musicDir.exists() && musicDir.isDirectory()) {
            fileChooser.setInitialDirectory(musicDir);
        } else {
            fileChooser.setInitialDirectory(userHome);
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Audio", "*.mp3", "*.wav", "*.m4a"));

        File file = fileChooser.showOpenDialog(addTrackTitleField.getScene().getWindow());
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            filePathLabel.setText(file.getName());
            if (addTrackErrorLabel != null)
                addTrackErrorLabel.setText("");
            setFieldsDisable(false);
            setLoading(true);

            // Reset dei campi prima di analizzare il nuovo file,
            // per evitare che i metadati del file precedente rimangano visibili
            addTrackTitleField.setText("");
            addTrackAuthorField.setText("");
            addTrackAlbumField.setText("");
            addTrackYearField.setText("");
            addTrackGenreComboBox.setValue(null);
            extractedDuration = 0;

            analyzeFileMetadata(file);
        }
    }

    /**
     * Analizza il file audio per estrarre metadati e durata.
     * - jaudiotagger: estrae titolo, artista, album, anno, genere in modo sincrono
     * e affidabile.
     * - JavaFX MediaPlayer: utilizzato esclusivamente per calcolare la durata del
     * brano.
     * Entrambe le operazioni vengono eseguite in un thread separato per non
     * bloccare l'UI.
     *
     * @param file Il file da analizzare.
     */
    private void analyzeFileMetadata(File file) {
        // Fallback immediato: usa il nome del file come titolo provvisorio
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf('.');
        addTrackTitleField.setText(lastDot > 0 ? fileName.substring(0, lastDot) : fileName);

        new Thread(() -> {
            it.unisa.java_music_playlist_manager.model.Track extractedTrack = it.unisa.java_music_playlist_manager.services.TrackMetadataExtractor
                    .extractMetadata(file);

            // Cattura variabili final per il lambda del Platform.runLater
            final String fTitle = extractedTrack.getTitle();
            final String fArtist = extractedTrack.getAuthor();
            final String fAlbum = extractedTrack.getAlbum();
            final String fGenre = extractedTrack.getGenre();
            final String fYear = extractedTrack.getYear() != null ? String.valueOf(extractedTrack.getYear()) : null;

            // 2. Aggiorna i campi UI con i tag letti
            Platform.runLater(() -> {
                if (fTitle != null && !fTitle.isEmpty()) {
                    addTrackTitleField.setText(fTitle);
                }
                if (fArtist != null && !fArtist.isEmpty()) {
                    addTrackAuthorField.setText(fArtist);
                }
                if (fAlbum != null && !fAlbum.isEmpty()) {
                    addTrackAlbumField.setText(fAlbum);
                }
                if (fYear != null && !fYear.isEmpty() && fYear.length() >= 4) {
                    addTrackYearField.setText(fYear.substring(0, 4));
                }
                if (fGenre != null && !fGenre.isEmpty()) {
                    // Cerca corrispondenza case-insensitive nella lista esistente
                    String matched = addTrackGenreComboBox.getItems().stream()
                            .filter(g -> g.equalsIgnoreCase(fGenre))
                            .findFirst()
                            .orElse(null);
                    if (matched != null) {
                        addTrackGenreComboBox.getSelectionModel().select(matched);
                    } else {
                        // Genere custom non in lista: aggiorna la lista osservabile in-place mantenendo
                        // l'ordinamento
                        TreeSet<String> sorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                        sorted.addAll(addTrackGenreComboBox.getItems());
                        sorted.add(fGenre);
                        if (addTrackGenreComboBox.getItems() == null) {
                            addTrackGenreComboBox.setItems(javafx.collections.FXCollections.observableArrayList());
                        }
                        addTrackGenreComboBox.getItems().setAll(sorted);
                        addTrackGenreComboBox.getSelectionModel().select(fGenre);
                    }
                }
            });

            // 3. Durata tramite JavaFX MediaPlayer (unico caso d'uso rimasto) e validazione
            // formato
            Platform.runLater(() -> {
                try {
                    Media media = new Media(file.toURI().toString());
                    MediaPlayer tempPlayer = new MediaPlayer(media);
                    tempPlayer.setOnReady(() -> {
                        if (media.getDuration() != null) {
                            extractedDuration = (int) media.getDuration().toSeconds();
                        }
                        setLoading(false);
                        tempPlayer.dispose();
                    });
                    tempPlayer.setOnError(() -> {
                        setLoading(false);
                        invalidateUnsupportedAudioFile(
                                "Formato non supportato (es. WAVE compresso).\nSeleziona un file MP3, M4A o WAV PCM.");
                        tempPlayer.dispose();
                    });
                    startAnalysisTimeout(tempPlayer);
                } catch (Exception e) {
                    setLoading(false);
                    invalidateUnsupportedAudioFile(
                            "Formato non supportato (es. WAVE compresso).\nSeleziona un file MP3, M4A o WAV PCM.");
                }
            });
        }).start();
    }

    /**
     * Resetta il file selezionato e mostra un errore bloccante se il formato audio
     * non è supportato da JavaFX.
     */
    private void invalidateUnsupportedAudioFile(String errorMsg) {
        selectedFilePath = null;
        Platform.runLater(() -> {
            if (filePathLabel != null) {
                filePathLabel.setText("File non supportato");
            }
            if (addTrackErrorLabel != null) {
                addTrackErrorLabel.setText(errorMsg);
            }
            setFieldsDisable(true);
        });
    }

    private void startAnalysisTimeout(MediaPlayer player) {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                if (loadingOverlay.isVisible()) {
                    setLoading(false);
                    Platform.runLater(player::dispose);
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void setFieldsDisable(boolean disable) {
        if (addTrackTitleField != null)
            addTrackTitleField.setDisable(disable);
        if (addTrackAuthorField != null)
            addTrackAuthorField.setDisable(disable);
        if (addTrackAlbumField != null)
            addTrackAlbumField.setDisable(disable);
        if (addTrackGenreComboBox != null)
            addTrackGenreComboBox.setDisable(disable);
        if (addTrackYearField != null)
            addTrackYearField.setDisable(disable);
        if (addTrackTagComboBox != null)
            addTrackTagComboBox.setDisable(disable);
        if (saveTrackButton != null)
            saveTrackButton.setDisable(disable);
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(loading);
                loadingOverlay.setManaged(loading);
            }
            if (saveTrackButton != null)
                saveTrackButton.setDisable(loading);
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