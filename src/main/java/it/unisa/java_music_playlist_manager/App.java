package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.LibraryDAO;
import it.unisa.java_music_playlist_manager.model.JsonLibraryDAO;
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
    
    
    @Override
    public void init(){
         saveDAO = new JsonLibraryDAO("salvataggio\\ library.json");
    }
    
    @Override
    public void start(Stage stage) throws IOException {
        // Caricamento della vista principale tramite il percorso relativo alle risorse.
        // La configurazione utilizza percorsi assoluti rispetto alla root delle risorse
        // per garantire la compatibilità con SceneBuilder e diversi IDE.
        scene = new Scene(loadFXML("/fxml/primaryView"), 1024, 700);
        stage.setTitle("Java Music Playlist Manager");
        
        //Carico il salvataggio vecchio
        try{
            saveDAO.load();
        }catch(Exception e){
            // aggiungere alla scena un popUp che scrive l
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Impossibile caricare la libreria precedente.\nDettagli: " + e.getMessage(), 
                ButtonType.OK);
            alert.showAndWait();
        }
        
        // Caricamento delle icone dell'applicazione in diverse dimensioni.
        // Il sistema operativo sceglierà automaticamente la dimensione più adatta.
        loadAppIcons(stage);
            
        
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void stop(){
        try {
            saveDAO.save(Library.getInstance());
        } catch (IOException ex) {
             Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Impossibile salvare la libreria precedente.\nDettagli: " + ex.getMessage(), 
                ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * Tenta di caricare le icone dell'applicazione dalla cartella resources/images.
     * @param stage Lo stage principale dell'applicazione.
     */
    private void loadAppIcons(Stage stage) {
        String[] iconNames = {"app_icon_16.png", "app_icon_32.png", "app_icon_64.png", "app_icon_128.png"};
        for (String name : iconNames) {
            try {
                var resource = getClass().getResourceAsStream("/images/" + name);
                if (resource != null) {
                    stage.getIcons().add(new Image(resource));
                }
            } catch (Exception e) {
                // Se un'icona specifica manca, continuiamo con le altre
                System.err.println("Impossibile caricare l'icona: " + name);
            }
        }
    }


    private static Parent loadFXML(String fxml) throws IOException {
        // L'utilizzo di App.class.getResource garantisce il corretto recupero dei file FXML
        // all'interno del classpath del progetto Maven.
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}