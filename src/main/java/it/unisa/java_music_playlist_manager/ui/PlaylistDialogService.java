package it.unisa.java_music_playlist_manager.ui;

import it.unisa.java_music_playlist_manager.ThemeManager;
import it.unisa.java_music_playlist_manager.model.AddElementToPlaylistCommand;
import it.unisa.java_music_playlist_manager.model.AddPlaylistCommand;
import it.unisa.java_music_playlist_manager.model.AutomaticPlaylistGenerator;
import it.unisa.java_music_playlist_manager.model.Command;
import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import it.unisa.java_music_playlist_manager.model.ManualPlaylistGenerator;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.PlaylistGenerator;
import it.unisa.java_music_playlist_manager.model.RenamePlaylistCommand;
import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.UndoManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PlaylistDialogService {

    private final Runnable onPlaylistChanged;

    public PlaylistDialogService(Runnable onPlaylistChanged) {
        this.onPlaylistChanged = onPlaylistChanged;
    }

    public void openCreatePlaylistDialog() {
        List<String> options = List.of("Playlist vuota", "Playlist automatica");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Playlist vuota", options);
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Scegli il tipo di playlist");
        dialog.setContentText("Tipo:");
        dialog.setGraphic(null);

        Optional<String> result = showThemedDialog(dialog);

        if (result.isEmpty()) {
            return;
        }
        if ("Playlist vuota".equals(result.get())) {
            openCreateEmptyPlaylistDialog();
        } else if ("Playlist automatica".equals(result.get())) {
            openCreateAutomaticPlaylistDialog();
        }
    }

    public void openEditPlaylistDialog(Playlist playlist) {
        if (playlist == null) return;

        TextInputDialog dialog = new TextInputDialog(playlist.getTitle());
        dialog.setTitle("Modifica playlist");
        dialog.setHeaderText("Modifica il nome della playlist");
        dialog.setContentText("Nome playlist:");
        dialog.setGraphic(null);

        showThemedDialog(dialog).ifPresent(name -> {
            try {
                Command renameCmd = new RenamePlaylistCommand(playlist, name);
                UndoManager.getInstance().executeCommand(renameCmd);
                if (onPlaylistChanged != null) onPlaylistChanged.run();
            } catch (IllegalArgumentException e) {
                showErrorAlert("Errore", "Nome non valido", e.getMessage());
            }
        });
    }

    public void openAddTrackToPlaylistDialog(Track selectedTrack) {
        if (selectedTrack == null) return;

        List<Playlist> playlists = new ArrayList<>();
        for (Playlist playlist : Library.getInstance().getPlaylists()) {
            if (playlist.isManuallyEditable()) {
                playlists.add(playlist);
            }
        }

        if (playlists.isEmpty()) {
            showInfoAlert("Nessuna playlist", "Non ci sono playlist disponibili", "Crea prima una playlist.");
            return;
        }

        ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(playlists.get(0), playlists);
        dialog.setTitle("Aggiungi a playlist");
        dialog.setHeaderText("Scegli la playlist");
        dialog.setContentText("Playlist:");

        Optional<Playlist> result = showThemedDialog(dialog);
        result.ifPresent(playlist -> {
            if (playlist instanceof ManualPlaylist manualPlaylist) {
                Command addTrackCmd = new AddElementToPlaylistCommand(manualPlaylist, selectedTrack);
                UndoManager.getInstance().executeCommand(addTrackCmd);
                // Non serve aggiornare immediatamente le colonne delle playlist qui
            }
        });
    }

    private void openCreateEmptyPlaylistDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Crea una nuova playlist");
        dialog.setContentText("Nome playlist:");
        dialog.setGraphic(null);

        showThemedDialog(dialog).ifPresent(name -> {
            try {
                PlaylistGenerator generator = new ManualPlaylistGenerator();
                Playlist playlist = (Playlist) generator.createPlaylist(name);

                Command addPlaylistCmd = new AddPlaylistCommand(Library.getInstance(), playlist);
                UndoManager.getInstance().executeCommand(addPlaylistCmd);

                if (onPlaylistChanged != null) onPlaylistChanged.run();
            } catch (IllegalArgumentException e) {
                showErrorAlert("Errore", "Nome playlist non valido", e.getMessage());
            }
        });
    }

    private void openCreateAutomaticPlaylistDialog() {
        List<String> options = List.of("Genere", "Anno", "Tag");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Genere", options);
        dialog.setTitle("Playlist automatica");
        dialog.setHeaderText("Crea una playlist automatica");
        dialog.setContentText("Criterio:");
        dialog.setGraphic(null);

        Optional<String> result = showThemedDialog(dialog);

        if (result.isEmpty()) {
            return;
        }

        if ("Genere".equals(result.get())) {
            openAutomaticPlaylistByGenreDialog();
        } else if ("Anno".equals(result.get())) {
            openAutomaticPlaylistByYearDialog();
        } else if ("Tag".equals(result.get())) {
            openAutomaticPlaylistByTagDialog();
        }
    }

    private void openAutomaticPlaylistByGenreDialog() {
        List<String> genres = new ArrayList<>();

        for (Track track : Library.getInstance().getTracks()) {
            String genre = track.getGenre();

            if (genre != null && !genre.trim().isEmpty() && !genres.contains(genre)) {
                genres.add(genre);
            }
        }

        genres.sort(String.CASE_INSENSITIVE_ORDER);

        if (genres.isEmpty()) {
            showInfoAlert(
                    "Nessun genere disponibile",
                    "Playlist automatica non creata",
                    "Non ci sono generi disponibili nella libreria."
            );
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(genres.get(0), genres);
        dialog.setTitle("Playlist automatica per genere");
        dialog.setHeaderText("Scegli il genere");
        dialog.setContentText("Genere:");
        dialog.setGraphic(null);

        Optional<String> genreResult = showThemedDialog(dialog);
        genreResult.ifPresent(genre -> {
            TextInputDialog titleDialog = new TextInputDialog("Playlist per " + genre);
            titleDialog.setTitle("Nome playlist");
            titleDialog.setHeaderText("Inserisci il nome della playlist");
            titleDialog.setContentText("Nome:");
            titleDialog.setGraphic(null);

            showThemedDialog(titleDialog).ifPresent(title -> generateAutomaticPlaylistByGenre(genre, title));
        });
    }

    private void openAutomaticPlaylistByYearDialog() {
        List<Integer> years = new ArrayList<>();

        for (Track track : Library.getInstance().getTracks()) {
            Integer year = track.getYear();

            if (year != null && !years.contains(year)) {
                years.add(year);
            }
        }

        years.sort(Integer::compareTo);

        if (years.isEmpty()) {
            showInfoAlert(
                    "Nessun anno disponibile",
                    "Playlist automatica non creata",
                    "Non ci sono anni disponibili nella libreria."
            );
            return;
        }

        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(years.get(0), years);
        dialog.setTitle("Playlist automatica per anno");
        dialog.setHeaderText("Scegli l'anno");
        dialog.setContentText("Anno:");
        dialog.setGraphic(null);

        Optional<Integer> yearResult = showThemedDialog(dialog);
        yearResult.ifPresent(year -> {
            TextInputDialog titleDialog = new TextInputDialog("Playlist del " + year);
            titleDialog.setTitle("Nome playlist");
            titleDialog.setHeaderText("Inserisci il nome della playlist");
            titleDialog.setContentText("Nome:");
            titleDialog.setGraphic(null);

            showThemedDialog(titleDialog).ifPresent(title -> generateAutomaticPlaylistByYear(year, title));
        });
    }

    private void generateAutomaticPlaylistByGenre(String genre, String title) {
        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.GENRE,
                genre
        );
        Playlist playlist = (Playlist) generator.createPlaylist(title);
        saveGeneratedPlaylist(playlist, "Playlist automatica creata per genere: ");
    }

    private void openAutomaticPlaylistByTagDialog() {
        List<Tag> tags = new ArrayList<>();

        for (Track track : Library.getInstance().getTracks()) {
            Set<Tag> trackTags = track.getTags();
            if (trackTags != null) {
                for (Tag tag : trackTags) {
                    if (!tags.contains(tag)) {
                        tags.add(tag);
                    }
                }
            }
        }

        tags.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));

        if (tags.isEmpty()) {
            showInfoAlert(
                    "Nessun tag disponibile",
                    "Playlist automatica non creata",
                    "Non ci sono tag associati ai brani nella libreria."
            );
            return;
        }

        ChoiceDialog<Tag> dialog = new ChoiceDialog<>(tags.get(0), tags);
        dialog.setTitle("Playlist automatica per tag");
        dialog.setHeaderText("Scegli il tag");
        dialog.setContentText("Tag:");
        dialog.setGraphic(null);

        Optional<Tag> tagResult = showThemedDialog(dialog);
        tagResult.ifPresent(tag -> {
            TextInputDialog titleDialog = new TextInputDialog("Playlist " + tag.getName());
            titleDialog.setTitle("Nome playlist");
            titleDialog.setHeaderText("Inserisci il nome della playlist");
            titleDialog.setContentText("Nome:");
            titleDialog.setGraphic(null);

            showThemedDialog(titleDialog).ifPresent(title -> generateAutomaticPlaylistByTag(tag, title));
        });
    }

    private void generateAutomaticPlaylistByYear(Integer year, String title) {
        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.YEAR,
                year
        );
        Playlist playlist = (Playlist) generator.createPlaylist(title);
        saveGeneratedPlaylist(playlist, "Playlist automatica creata per anno: ");
    }

    private void generateAutomaticPlaylistByTag(Tag tag, String title) {
        PlaylistGenerator generator = new AutomaticPlaylistGenerator(
                AutomaticPlaylistGenerator.Criteria.TAG,
                tag
        );
        Playlist playlist = (Playlist) generator.createPlaylist(title);
        saveGeneratedPlaylist(playlist, "Playlist automatica creata per tag: ");
    }

    private void saveGeneratedPlaylist(Playlist playlist, String logMessage) {
        Command addPlaylistCmd = new AddPlaylistCommand(Library.getInstance(), playlist);
        UndoManager.getInstance().executeCommand(addPlaylistCmd);

        if (onPlaylistChanged != null) onPlaylistChanged.run();
        System.out.println(logMessage + playlist.getTitle());
    }

    private <T> Optional<T> showThemedDialog(Dialog<T> dialog) {
        dialog.setOnShown(event -> ThemeManager.getInstance().applyActiveThemeToScene(dialog.getDialogPane().getScene()));
        return dialog.showAndWait();
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        showThemedDialog(alert);
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        showThemedDialog(alert);
    }
}
