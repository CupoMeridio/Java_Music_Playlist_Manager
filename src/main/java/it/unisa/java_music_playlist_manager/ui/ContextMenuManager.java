package it.unisa.java_music_playlist_manager.ui;

import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

public class ContextMenuManager {

    public interface ContextMenuActions {
        void onEditTrack(Track track);
        void onDeleteTrack(Track track);
        void onAddTrackToPlaylist(Track track);
        void onAddTrackToQueue(Track track);
        void onEditPlaylist(Playlist playlist);
        void onDeletePlaylist(Playlist playlist);
        void onAddPlaylistToQueue(Playlist playlist);
        void onRemoveFromPlaylist(Track track, Playlist playlist);
        boolean isQueueView();
        Playlist getCurrentOpenedPlaylist();
    }

    public static void setupTrackContextMenu(TableView<Track> table, ContextMenuActions actions) {
        ContextMenu contextMenu = new ContextMenu();
        table.setContextMenu(contextMenu);

        contextMenu.setOnShowing(e -> {
            contextMenu.getItems().clear();
            Track selectedTrack = table.getSelectionModel().getSelectedItem();
            if (selectedTrack == null || actions.isQueueView()) {
                e.consume();
                return;
            }

            MenuItem editItem = new MenuItem("Modifica brano");
            editItem.setOnAction(ev -> actions.onEditTrack(selectedTrack));

            MenuItem deleteItem = new MenuItem("Elimina brano");
            deleteItem.setOnAction(ev -> actions.onDeleteTrack(selectedTrack));

            MenuItem addToPlaylistItem = new MenuItem("Aggiungi a playlist");
            addToPlaylistItem.setOnAction(ev -> actions.onAddTrackToPlaylist(selectedTrack));

            MenuItem addTrackToQueueItem = new MenuItem("Aggiungi brano alla coda");
            addTrackToQueueItem.setOnAction(ev -> actions.onAddTrackToQueue(selectedTrack));

            contextMenu.getItems().addAll(editItem, deleteItem, addToPlaylistItem, addTrackToQueueItem);

            Playlist currentOpenedPlaylist = actions.getCurrentOpenedPlaylist();
            if (currentOpenedPlaylist != null && currentOpenedPlaylist.isManuallyEditable()) {
                MenuItem removeFromPlaylistItem = new MenuItem("Rimuovi dalla playlist");
                removeFromPlaylistItem.setOnAction(ev -> actions.onRemoveFromPlaylist(selectedTrack, currentOpenedPlaylist));
                contextMenu.getItems().add(removeFromPlaylistItem);
            }
        });
    }

    public static void setupPlaylistContextMenu(TableView<Playlist> table, ContextMenuActions actions) {
        ContextMenu contextMenu = new ContextMenu();
        table.setContextMenu(contextMenu);

        contextMenu.setOnShowing(e -> {
            contextMenu.getItems().clear();
            Playlist selectedPlaylist = table.getSelectionModel().getSelectedItem();
            if (selectedPlaylist == null || actions.isQueueView()) {
                e.consume();
                return;
            }

            MenuItem editPlaylistItem = new MenuItem("Modifica playlist");
            editPlaylistItem.setOnAction(ev -> actions.onEditPlaylist(selectedPlaylist));

            MenuItem deletePlaylistItem = new MenuItem("Elimina playlist");
            deletePlaylistItem.setOnAction(ev -> actions.onDeletePlaylist(selectedPlaylist));

            MenuItem addPlaylistToQueueItem = new MenuItem("Aggiungi playlist alla coda");
            addPlaylistToQueueItem.setOnAction(ev -> actions.onAddPlaylistToQueue(selectedPlaylist));

            contextMenu.getItems().addAll(editPlaylistItem, deletePlaylistItem, addPlaylistToQueueItem);
        });
    }
}
