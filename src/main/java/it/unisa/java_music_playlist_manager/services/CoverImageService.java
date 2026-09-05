package it.unisa.java_music_playlist_manager.services;




import javafx.scene.image.Image;
import javafx.concurrent.Task;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servizio singleton per la gestione delle copertine dei brani musicali.
 * Gestisce la cache delle immagini estratte e dei percorsi privi di artwork.
 */
public class CoverImageService {

    private static CoverImageService instance;

    // Cache thread-safe per le copertine valide trovate nei file (Key: filePath)
    private final Map<String, Image> imageCache;

    // Insieme thread-safe per i file verificati che NON contengono un'artwork
    private final Set<String> noArtworkPaths;

    // Executor limitato a 4 thread per la lettura I/O dai file
    private final ExecutorService executorService;

    // Immagine di fallback da usare quando il file non contiene un'artwork
    private Image defaultCover;

    private CoverImageService() {
        imageCache = Collections.synchronizedMap(new LinkedHashMap<String, Image>(200, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
                return size() > 200;
            }
        });
        noArtworkPaths = Collections.synchronizedSet(new HashSet<>());

        executorService = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        try {
            defaultCover = new Image(getClass().getResourceAsStream("/images/app_icon_128.png"));
        } catch (Exception e) {
            defaultCover = null;
        }
    }

    public static synchronized CoverImageService getInstance() {
        if (instance == null) {
            instance = new CoverImageService();
        }
        return instance;
    }

    public Image getDefaultCover() {
        return defaultCover;
    }

    /**
     * Verifica se un'immagine corrisponde alla copertina di default.
     * È più sicuro del confronto diretto per riferimento (==) qualora
     * l'implementazione del fallback cambiasse in futuro.
     */
    public boolean isDefaultCover(Image img) {
        return this.defaultCover != null && this.defaultCover == img;
    }

    /**
     * Restituisce sincronicamente l'immagine dalla memoria se già caricata,
     * altrimenti restituisce la copertina di default senza bloccare il thread.
     */
    public Image getCachedCoverOrDefault(String filePath) {
        if (filePath != null && imageCache.containsKey(filePath)) {
            return imageCache.get(filePath);
        }
        return defaultCover;
    }

    /**
     * Carica la copertina in maniera asincrona.
     * Restituisce sempre un CompletableFuture<Image> (mai null).
     */
    public CompletableFuture<Image> loadCoverAsync(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return CompletableFuture.completedFuture(defaultCover);
        }

        // 1. Se l'immagine reale è già in cache
        if (imageCache.containsKey(filePath)) {
            return CompletableFuture.completedFuture(imageCache.get(filePath));
        }

        // 2. Se il file è già stato scansionato ed è privo di artwork
        if (noArtworkPaths.contains(filePath)) {
            return CompletableFuture.completedFuture(defaultCover);
        }

        // 3. Altrimenti, sottometti il task di lettura I/O al ThreadPool
        CompletableFuture<Image> future = new CompletableFuture<>();

        Task<Image> loadTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                try {
                    File audioFile = new File(filePath);
                    if (!audioFile.exists()) {
                        return null;
                    }
                    Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
                    AudioFile f = AudioFileIO.read(audioFile);
                    Tag tag = f.getTag();
                    if (tag == null)
                        return null;

                    Artwork artwork = tag.getFirstArtwork();
                    if (artwork == null)
                        return null;

                    byte[] imageData = artwork.getBinaryData();
                    if (imageData == null || imageData.length == 0)
                        return null;

                    return new Image(new ByteArrayInputStream(imageData));
                } catch (Exception e) {
                    return null;
                }
            }
        };

        loadTask.setOnSucceeded(e -> {
            Image img = loadTask.getValue();
            if (img != null) {
                imageCache.put(filePath, img);
                future.complete(img);
            } else {
                noArtworkPaths.add(filePath);
                future.complete(defaultCover);
            }
        });

        loadTask.setOnFailed(e -> {
            noArtworkPaths.add(filePath);
            future.complete(defaultCover);
        });

        future.whenComplete((result, ex) -> {
            if (future.isCancelled()) {
                loadTask.cancel();
            }
        });

        executorService.submit(loadTask);

        return future;
    }
}
