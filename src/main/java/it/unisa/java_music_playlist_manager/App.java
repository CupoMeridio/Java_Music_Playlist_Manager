package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.LibraryDAO;
import it.unisa.java_music_playlist_manager.model.JsonLibraryDAO;
import it.unisa.java_music_playlist_manager.model.UndoManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static LibraryDAO saveDAO;

    private static final String CARTELLA_FILE_PATH = "salvataggio/";
    private static final String SAVE_FILE_PATH = CARTELLA_FILE_PATH + "library.json";

    @Override
    public void init() {
        saveDAO = new JsonLibraryDAO(SAVE_FILE_PATH);
    }

    @Override
    public void start(Stage stage) throws IOException {

        // Carico il salvataggio vecchio
        try {
            saveDAO.load();
        } catch (Exception e) {
            // Alert
            gestisciFileCorrotto(e);
        }
        // Caricamento della vista principale tramite il percorso relativo alle risorse.
        // La configurazione utilizza percorsi assoluti rispetto alla root delle risorse
        // per garantire la compatibilità con SceneBuilder e diversi IDE.
        scene = new Scene(loadFXML("/fxml/primaryView"), 1024, 700);
        stage.setTitle("Java Music Playlist Manager");
        ThemeManager.getInstance().setScene(scene);
        ThemeManager.getInstance().applyActiveTheme();

        // Caricamento delle icone dell'applicazione in diverse dimensioni.
        // Il sistema operativo sceglierà automaticamente la dimensione più adatta.
        loadAppIcons(stage);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        try {
            // Svuota la cronologia dell'undo prima di chiudere il programma per liberare
            // memoria
            UndoManager.getInstance().clearHistory();
            saveDAO.save(Library.getInstance());
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Impossibile salvare la libreria precedente.\nDettagli: " + ex.getMessage(),
                    ButtonType.OK);
            ThemeManager.getInstance().applyActiveThemeToScene(alert.getDialogPane().getScene());
            alert.showAndWait();
        }
    }

    /**
     * Tenta di caricare le icone dell'applicazione dalla cartella resources/images.
     * 
     * @param stage Lo stage principale dell'applicazione.
     */
    private void loadAppIcons(Stage stage) {
        String[] iconNames = { "app_icon_16.png", "app_icon_32.png", "app_icon_64.png", "app_icon_128.png" };
        for (String name : iconNames) {
            try {
                var resource = getClass().getResourceAsStream("/images/" + name);
                if (resource != null) {
                    stage.getIcons().add(new Image(resource));
                }
            } catch (Exception e) {
                // Se un'icona specifica manca, continuiamo con le altre
            }
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        // L'utilizzo di App.class.getResource garantisce il corretto recupero dei file
        // FXML
        // all'interno del classpath del progetto Maven.
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    private void gestisciFileCorrotto(Exception e) {
        // Puoi usare una costante di classe per "library.json" se vuoi fare un lavoro
        // ancora più pulito
        java.io.File corruptedFile = new java.io.File(SAVE_FILE_PATH);

        if (corruptedFile.exists()) {
            String backupName = CARTELLA_FILE_PATH + "library_corrupted_" + System.currentTimeMillis() + ".json";
            java.io.File backupFile = new java.io.File(backupName);

            // Rinomina il file (sposta i dati rotti al sicuro)
            corruptedFile.renameTo(backupFile);
        }
        UndoManager.getInstance().clearHistory();
        // Mostra l'avviso all'utente
        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Il file di salvataggio precedente risulta danneggiato o illeggibile.\n" +
                        "Per sicurezza, è stata creata una copia di backup del file corrotto.\n" +
                        "L'applicazione verrà avviata con una nuova libreria vuota.\n\n" +
                        "Dettagli errore: " + e.getMessage(),
                ButtonType.OK);
        ThemeManager.getInstance().applyActiveThemeToScene(alert.getDialogPane().getScene());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }

}