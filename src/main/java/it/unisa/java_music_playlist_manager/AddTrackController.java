package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.Observer;
import it.unisa.java_music_playlist_manager.model.TagPredefined;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// IMPORTAZIONE  PER CONTROLSFX
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
    private TextField addTrackDurationField;

    @FXML
    private TextField addTrackYearField;

    @FXML
    private Label addTrackErrorLabel;

    @FXML
    private ComboBox<String> addTrackGenreComboBox;

    @FXML
    private Label formTitleLabel;
    
    @FXML
    private CheckComboBox<Tag> addTrackTagComboBox;

    /**
     * Imposta il brano da modificare. Se null, il form funziona in modalità inserimento.
     */
    public void setCurrentEditingTrack(Track track) {
        this.currentEditingTrack = track;
    }

    /**
     * Imposta l'observer da collegare ai nuovi brani dopo il salvataggio.
     */
    public void setOnTrackSaved(Observer observer) {
        this.trackObserver = observer;
    }

    /**
     * Inizializza il form con i dati del brano da modificare, oppure
     * con campi vuoti per l'inserimento di un nuovo brano.
     * Deve essere chiamato dopo loader.load().
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

        // Popoliamo la ComboBox del form "Aggiungi brano".
        if (addTrackGenreComboBox != null) {
            addTrackGenreComboBox.getItems().setAll(
                    "Pop", "Rock", "Jazz", "Classica", "Hip Hop", "Rap", "Elettronica"
            );
        }

        if (currentEditingTrack != null) {
            addTrackTitleField.setText(currentEditingTrack.getTags() != null ? currentEditingTrack.getTitle() : "");
            addTrackTitleField.setText(currentEditingTrack.getTitle());
            addTrackAuthorField.setText(currentEditingTrack.getAuthor());
            addTrackDurationField.setText(String.valueOf(currentEditingTrack.getDuration()));
            addTrackYearField.setText(String.valueOf(currentEditingTrack.getYear()));
            addTrackGenreComboBox.setValue(currentEditingTrack.getGenre());

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
            addTrackDurationField.setText("");
            addTrackYearField.setText("");
            addTrackGenreComboBox.setValue(null);

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
            // salvo yearText durationText e come String non come int perchè altrimenti non posso verificare se il field è vuoto
            String title = addTrackTitleField.getText().trim();
            String author = addTrackAuthorField.getText().trim();
            String durationText = addTrackDurationField.getText().trim();
            String genre = addTrackGenreComboBox.getValue();
            String yearText = addTrackYearField.getText().trim();

            List<Tag> selectedTags = addTrackTagComboBox.getCheckModel().getCheckedItems();

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
                
                
                
                currentEditingTrack.removeAllTags();
                if (addTrackTagComboBox != null){ 
                    addTrackTagComboBox.getCheckModel().getCheckedItems().forEach(tag -> {
                        if(tag != null)
                             currentEditingTrack.addTag(tag);
                    });
                }
                
                Library.getInstance().notifyObservers();
                System.out.println("Brano modificato: " + currentEditingTrack.getTitle());
            } else {
                Track track = new Track(title, author, duration, genre, year);
                
                if (selectedTags != null) {
                    selectedTags.forEach(t -> {
                        track.addTag(t);
                    });
                }
                
                Library.getInstance().addTrack(track);
                if (trackObserver != null) {
                    track.attach(trackObserver);
                }
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
    
    /**
     * Metodo di inizializzazione di JavaFX. Viene eseguito automaticamente 
     * al caricamento dell'FXML. Popoliamo la CheckComboBox con i tag predefiniti.
     */
    @FXML
    public void initialize() {
        if (addTrackTagComboBox != null) {
            // Prende tutti i valori definiti nell'enum e li inserisce nella lista visibile  
            addTrackTagComboBox.getItems().addAll(TagPredefined.values());
        }   
    }
}
