package it.unisa.java_music_playlist_manager;

/**
 * Entry point alternativo per l'avvio dell'applicazione.
 * <p>
 * Non estendendo direttamente {@link javafx.application.Application}, consente di
 * avviare correttamente il file JAR eseguibile (Fat JAR / Shaded JAR) senza incorrere
 * nel blocco di runtime JavaFX "JavaFX runtime components are missing".
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
