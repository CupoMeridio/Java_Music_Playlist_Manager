package it.unisa.java_music_playlist_manager;

import javafx.scene.Scene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ThemeManager gestisce il tema visivo dell'applicazione.
 * 
 * Responsabilità:
 * - Mantiene il catalogo dei temi disponibili.
 * - Applica i CSS del tema selezionato alla scena principale.
 * - Persiste la scelta dell'utente in un file .properties per il riavvio.
 */
public class ThemeManager {

    private static ThemeManager instance;

    /** Cartella di salvataggio preferenze (stessa della libreria) */
    private static final String PREFS_PATH = "salvataggio/theme.properties";
    private static final String PREFS_KEY  = "activeTheme";

    /** Tema di default */
    private static final String DEFAULT_THEME = "Neo-Pop Brutal";

    /**
     * Catalogo temi: nome → array di percorsi CSS (nell'ordine di caricamento).
     * Ogni tema ha i propri 5 file CSS nella sua sottocartella.
     */
    private final Map<String, String[]> themes = new LinkedHashMap<>();

    /** Tema attualmente attivo */
    private String activeTheme = DEFAULT_THEME;

    /** Scena principale, impostata da App all'avvio */
    private Scene scene;

    private ThemeManager() {
        registerThemes();
        loadPreference();
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    /** Registra tutti i temi disponibili. */
    private void registerThemes() {
        themes.put("Neo-Pop Brutal", new String[]{
            "/styles/themes/neo-pop-brutal/primaryview.css",
            "/styles/themes/neo-pop-brutal/player.css",
            "/styles/themes/neo-pop-brutal/sidebar.css",
            "/styles/themes/neo-pop-brutal/home.css",
            "/styles/themes/neo-pop-brutal/addtrack.css"
        });
        themes.put("Phantom Thief", new String[]{
            "/styles/themes/phantom-thief/primaryview.css",
            "/styles/themes/phantom-thief/player.css",
            "/styles/themes/phantom-thief/sidebar.css",
            "/styles/themes/phantom-thief/home.css",
            "/styles/themes/phantom-thief/addtrack.css"
        });
        themes.put("Braun Retro-HiFi", new String[]{
            "/styles/themes/braun-retro-hifi/primaryview.css",
            "/styles/themes/braun-retro-hifi/player.css",
            "/styles/themes/braun-retro-hifi/sidebar.css",
            "/styles/themes/braun-retro-hifi/home.css",
            "/styles/themes/braun-retro-hifi/addtrack.css"
        });
        themes.put("Playful Bento Box", new String[]{
            "/styles/themes/playful-bento-box/primaryview.css",
            "/styles/themes/playful-bento-box/player.css",
            "/styles/themes/playful-bento-box/sidebar.css",
            "/styles/themes/playful-bento-box/home.css",
            "/styles/themes/playful-bento-box/addtrack.css"
        });
        themes.put("Swiss Bauhaus", new String[]{
            "/styles/themes/swiss-bauhaus/primaryview.css",
            "/styles/themes/swiss-bauhaus/player.css",
            "/styles/themes/swiss-bauhaus/sidebar.css",
            "/styles/themes/swiss-bauhaus/home.css",
            "/styles/themes/swiss-bauhaus/addtrack.css"
        });
        themes.put("Terminal ASCII (Light Mode)", new String[]{
            "/styles/themes/terminal-ascii-light/primaryview.css",
            "/styles/themes/terminal-ascii-light/player.css",
            "/styles/themes/terminal-ascii-light/sidebar.css",
            "/styles/themes/terminal-ascii-light/home.css",
            "/styles/themes/terminal-ascii-light/addtrack.css"
        });
        themes.put("Pastel Kawaii", new String[]{
            "/styles/themes/pastel-kawaii/primaryview.css",
            "/styles/themes/pastel-kawaii/player.css",
            "/styles/themes/pastel-kawaii/sidebar.css",
            "/styles/themes/pastel-kawaii/home.css",
            "/styles/themes/pastel-kawaii/addtrack.css"
        });
        themes.put("Vaporwave Sunset", new String[]{
            "/styles/themes/vaporwave-sunset/primaryview.css",
            "/styles/themes/vaporwave-sunset/player.css",
            "/styles/themes/vaporwave-sunset/sidebar.css",
            "/styles/themes/vaporwave-sunset/home.css",
            "/styles/themes/vaporwave-sunset/addtrack.css"
        });
    }

    /** Restituisce i nomi di tutti i temi disponibili. */
    public java.util.Set<String> getThemeNames() {
        return themes.keySet();
    }

    /** Restituisce il nome del tema attivo. */
    public String getActiveTheme() {
        return activeTheme;
    }

    /** Registra la scena principale. Va chiamato una sola volta da App. */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Applica il tema selezionato alla scena, sostituendo tutti i CSS precedenti.
     * Salva la scelta nelle preferenze.
     *
     * @param themeName nome del tema (deve esistere nel catalogo)
     */
    public void applyTheme(String themeName) {
        if (!themes.containsKey(themeName)) return;

        activeTheme = themeName;
        applyActiveThemeToScene(scene);
        savePreference();
    }

    /** Applica il tema attivo corrente alla scena principale. */
    public void applyActiveTheme() {
        applyActiveThemeToScene(scene);
    }

    /** Applica il tema attivo corrente a una scena secondaria, ad esempio una finestra modale. */
    public void applyActiveThemeToScene(Scene targetScene) {
        if (targetScene == null) return;

        targetScene.getStylesheets().clear();
        for (String cssPath : themes.get(activeTheme)) {
            var resource = getClass().getResource(cssPath);
            if (resource != null) {
                targetScene.getStylesheets().add(resource.toExternalForm());
            } else {
                System.err.println("[THEME] CSS non trovato: " + cssPath);
            }
        }
    }

    /** Legge la preferenza salvata. */
    private void loadPreference() {
        File file = new File(PREFS_PATH);
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            String saved = props.getProperty(PREFS_KEY);
            if (saved != null && themes.containsKey(saved)) {
                activeTheme = saved;
            }
        } catch (IOException ignored) {}
    }

    /** Salva la preferenza su disco. */
    private void savePreference() {
        try {
            new File("salvataggio").mkdirs();
            Properties props = new Properties();
            props.setProperty(PREFS_KEY, activeTheme);
            try (FileOutputStream fos = new FileOutputStream(PREFS_PATH)) {
                props.store(fos, "Java Music Playlist Manager - Theme Preference");
            }
        } catch (IOException ignored) {}
    }
}
