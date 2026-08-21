package it.unisa.java_music_playlist_manager.ui;

import it.unisa.java_music_playlist_manager.model.Command;
import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.RemoveElementFromPlaylistCommand;
import it.unisa.java_music_playlist_manager.model.RemovePlaylistCommand;
import it.unisa.java_music_playlist_manager.model.RemoveTrackCommand;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.UndoManager;
import it.unisa.java_music_playlist_manager.model.ViewType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import it.unisa.java_music_playlist_manager.ThemeManager;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LibraryActionsHandler implements ContextMenuManager.ContextMenuActions {

    private final PlaylistDialogService dialogService;
    private final Supplier<ViewType> currentViewTypeSupplier;
    private final Supplier<Playlist> currentOpenedPlaylistSupplier;
    private final Runnable refreshQueueViewIfVisibleAction;
    private final Consumer<Track> openEditTrackViewAction;

    public LibraryActionsHandler(
            PlaylistDialogService dialogService,
            Supplier<ViewType> currentViewTypeSupplier,
            Supplier<Playlist> currentOpenedPlaylistSupplier,
            Runnable refreshQueueViewIfVisibleAction,
            Consumer<Track> openEditTrackViewAction) {
        this.dialogService = dialogService;
        this.currentViewTypeSupplier = currentViewTypeSupplier;
        this.currentOpenedPlaylistSupplier = currentOpenedPlaylistSupplier;
        this.refreshQueueViewIfVisibleAction = refreshQueueViewIfVisibleAction;
        this.openEditTrackViewAction = openEditTrackViewAction;
    }

    @Override
    public void onEditTrack(Track track) {
        if (track != null) {
            openEditTrackViewAction.accept(track);
        }
    }

    @Override
    public void onDeleteTrack(Track selectedTrack) {
        if (selectedTrack == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare il brano selezionato?");
        confirmAlert.setContentText("Stai per eliminare \"" + selectedTrack.getTitle()
                + "\" dalla libreria.\nVerrà rimosso anche da tutte le playlist.");

        Optional<ButtonType> result = ThemeManager.getInstance().showThemedDialog(confirmAlert);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Command removeCmd = new RemoveTrackCommand(Library.getInstance(), selectedTrack);
            UndoManager.getInstance().executeCommand(removeCmd);
        }
    }

    @Override
    public void onAddTrackToPlaylist(Track selectedTrack) {
        if (selectedTrack == null) return;
        dialogService.openAddTrackToPlaylistDialog(selectedTrack);
    }

    @Override
    public void onAddTrackToQueue(Track track) {
        if (track != null) {
            PlaybackManager.getInstance().addToQueue(track);
            refreshQueueViewIfVisibleAction.run();
        }
    }

    @Override
    public void onEditPlaylist(Playlist selectedPlaylist) {
        if (selectedPlaylist == null) return;
        dialogService.openEditPlaylistDialog(selectedPlaylist);
    }

    @Override
    public void onDeletePlaylist(Playlist selectedPlaylist) {
        if (selectedPlaylist == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare la playlist selezionata?");
        confirmAlert.setContentText("I brani resteranno disponibili nella libreria musicale.");

        if (ThemeManager.getInstance().showThemedDialog(confirmAlert).filter(r -> r == ButtonType.OK).isPresent()) {
            Command deletePlaylistCmd = new RemovePlaylistCommand(Library.getInstance(), selectedPlaylist);
            UndoManager.getInstance().executeCommand(deletePlaylistCmd);
        }
    }

    @Override
    public void onAddPlaylistToQueue(Playlist playlist) {
        if (playlist != null) {
            PlaybackManager.getInstance().addToQueue(playlist);
            refreshQueueViewIfVisibleAction.run();
        }
    }

    @Override
    public void onRemoveFromPlaylist(Track selectedTrack, Playlist currentOpenedPlaylist) {
        if (selectedTrack != null && currentOpenedPlaylist instanceof ManualPlaylist manualPlaylist) {
            Command removeCmd = new RemoveElementFromPlaylistCommand(manualPlaylist, selectedTrack);
            UndoManager.getInstance().executeCommand(removeCmd);
        }
    }

    @Override
    public boolean isQueueView() {
        return currentViewTypeSupplier.get() == ViewType.QUEUE;
    }

    @Override
    public Playlist getCurrentOpenedPlaylist() {
        return currentOpenedPlaylistSupplier.get();
    }
}
