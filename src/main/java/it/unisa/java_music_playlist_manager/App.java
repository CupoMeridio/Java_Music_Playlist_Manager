package it.unisa.java_music_playlist_manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Caricamento della vista principale tramite il percorso relativo alle risorse.
        // La configurazione utilizza percorsi assoluti rispetto alla root delle risorse
        // per garantire la compatibilità con SceneBuilder e diversi IDE.
        scene = new Scene(loadFXML("/fxml/primaryView"), 1024, 700);
        stage.setTitle("Java Music Playlist Manager");
        
        // Caricamento delle icone dell'applicazione in diverse dimensioni.
        // Il sistema operativo sceglierà automaticamente la dimensione più adatta.
        loadAppIcons(stage);
        
        stage.setScene(scene);
        stage.show();
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