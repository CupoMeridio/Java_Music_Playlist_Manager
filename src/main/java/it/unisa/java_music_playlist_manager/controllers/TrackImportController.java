package it.unisa.java_music_playlist_manager.controllers;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.command.AddMultipleTracksCommand;
import it.unisa.java_music_playlist_manager.model.command.Command;
import it.unisa.java_music_playlist_manager.model.command.UndoManager;
import it.unisa.java_music_playlist_manager.services.TrackMetadataExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Controller dedicato alla logica di importazione massiva di file e cartelle musicali.
 * Gestisce l'apertura dei file chooser, l'orchestrazione asincrona del parsing
 * (tramite TrackMetadataExtractor) e la visualizzazione del progresso tramite interfaccia JavaFX.
 */
public class TrackImportController {

    private final Window ownerWindow;

    public TrackImportController(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    public void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona File Audio");
        File userHome = new File(System.getProperty("user.home"));
        File musicDir = new File(userHome, "Music");
        if (!musicDir.exists() || !musicDir.isDirectory()) musicDir = new File(userHome, "Musica");
        fileChooser.setInitialDirectory((musicDir.exists() && musicDir.isDirectory()) ? musicDir : userHome);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File Audio", "*.mp3", "*.wav", "*.m4a"));
        
        List<File> files = fileChooser.showOpenMultipleDialog(ownerWindow);
        if (files != null && !files.isEmpty()) {
            processMultipleFilesAsync(files);
        }
    }

    public void handleAddFolder() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Seleziona Cartella Musicale");
        File userHome = new File(System.getProperty("user.home"));
        File musicDir = new File(userHome, "Music");
        if (!musicDir.exists() || !musicDir.isDirectory()) musicDir = new File(userHome, "Musica");
        dirChooser.setInitialDirectory((musicDir.exists() && musicDir.isDirectory()) ? musicDir : userHome);
        
        File dir = dirChooser.showDialog(ownerWindow);
        if (dir != null) {
            try (Stream<Path> paths = Files.walk(dir.toPath())) {
                List<File> files = paths.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(f -> {
                        String name = f.getName().toLowerCase();
                        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a");
                    })
                    .collect(Collectors.toList());
                if (!files.isEmpty()) {
                    processMultipleFilesAsync(files);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore di Lettura");
                    alert.setHeaderText("Impossibile leggere la cartella selezionata");
                    alert.setContentText("Verifica di avere i permessi necessari o che il percorso sia valido.\n" + e.getMessage());
                    alert.showAndWait();
                });
            }
        }
    }

    private void processMultipleFilesAsync(List<File> files) {
        Dialog<Void> progressDialog = new Dialog<>();
        progressDialog.initOwner(ownerWindow);
        progressDialog.setTitle("Importazione Brani");
        progressDialog.setHeaderText("Analisi di " + files.size() + " brani in corso...");
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        Label progressLabel = new Label("Preparazione...");
        
        VBox content = new VBox(10, progressLabel, progressBar);
        progressDialog.getDialogPane().setContent(content);
        progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL); // Required to show dialog
        progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(true); // Disable cancel for simplicity
        
        Task<List<Track>> task = new Task<>() {
            @Override
            protected List<Track> call() {
                List<Track> importedTracks = new ArrayList<>();
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    updateMessage("Elaborazione: " + file.getName() + " (" + (i+1) + "/" + files.size() + ")");
                    updateProgress(i, files.size());
                    Track t = TrackMetadataExtractor.extractMetadata(file);
                    if (t != null) {
                        importedTracks.add(t);
                    }
                }
                updateProgress(files.size(), files.size());
                return importedTracks;
            }
        };

        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            Platform.runLater(() -> progressLabel.setText(newMsg));
        });
        
        task.progressProperty().addListener((obs, oldProg, newProg) -> {
            Platform.runLater(() -> progressBar.setProgress(newProg.doubleValue()));
        });

        task.setOnSucceeded(e -> {
            List<Track> importedTracks = task.getValue();
            if (!importedTracks.isEmpty()) {
                Command addMulti = new AddMultipleTracksCommand(Library.getInstance(), importedTracks);
                UndoManager.getInstance().executeCommand(addMulti);
            }
            progressDialog.setResult(null);
            progressDialog.close();
        });

        task.setOnFailed(e -> {
            progressDialog.setResult(null);
            progressDialog.close();
        });

        new Thread(task).start();
        progressDialog.showAndWait();
    }
}
