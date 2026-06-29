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
        
        MenuItem editItem = new MenuItem("Modifica brano");
        editItem.setOnAction(e -> actions.onEditTrack(table.getSelectionModel().getSelectedItem()));

        MenuItem deleteItem = new MenuItem("Elimina brano");
        deleteItem.setOnAction(e -> actions.onDeleteTrack(table.getSelectionModel().getSelectedItem()));

        MenuItem addToPlaylistItem = new MenuItem("Aggiungi a playlist");
        addToPlaylistItem.setOnAction(e -> actions.onAddTrackToPlaylist(table.getSelectionModel().getSelectedItem()));

        MenuItem addTrackToQueueItem = new MenuItem("Aggiungi brano alla coda");
        addTrackToQueueItem.setOnAction(e -> actions.onAddTrackToQueue(table.getSelectionModel().getSelectedItem()));

        MenuItem removeFromPlaylistItem = new MenuItem("Rimuovi dalla playlist");
        removeFromPlaylistItem.setOnAction(e -> actions.onRemoveFromPlaylist(
            table.getSelectionModel().getSelectedItem(),
            actions.getCurrentOpenedPlaylist()
        ));

        contextMenu.getItems().addAll(
                editItem,
                deleteItem,
                addToPlaylistItem,
                addTrackToQueueItem,
                removeFromPlaylistItem
        );
        table.setContextMenu(contextMenu);

        contextMenu.setOnShowing(e -> {
            Track selectedTrack = table.getSelectionModel().getSelectedItem();
            boolean noTrackSelected = selectedTrack == null;
            boolean isQueueView = actions.isQueueView();

            editItem.setVisible(!noTrackSelected && !isQueueView);
            deleteItem.setVisible(!noTrackSelected && !isQueueView);
            addToPlaylistItem.setVisible(!noTrackSelected && !isQueueView);
            addTrackToQueueItem.setVisible(!noTrackSelected && !isQueueView);

            Playlist currentOpenedPlaylist = actions.getCurrentOpenedPlaylist();
            boolean isPlaylistDetailView = currentOpenedPlaylist != null;
            boolean isEditable = currentOpenedPlaylist != null && currentOpenedPlaylist.isManuallyEditable();
            
            removeFromPlaylistItem.setDisable(noTrackSelected || !isPlaylistDetailView || !isEditable);
            removeFromPlaylistItem.setVisible(!noTrackSelected && isPlaylistDetailView && isEditable);
        });
    }

    public static void setupPlaylistContextMenu(TableView<Playlist> table, ContextMenuActions actions) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem editPlaylistItem = new MenuItem("Modifica playlist");
        editPlaylistItem.setOnAction(e -> actions.onEditPlaylist(table.getSelectionModel().getSelectedItem()));

        MenuItem deletePlaylistItem = new MenuItem("Elimina playlist");
        deletePlaylistItem.setOnAction(e -> actions.onDeletePlaylist(table.getSelectionModel().getSelectedItem()));

        MenuItem addPlaylistToQueueItem = new MenuItem("Aggiungi playlist alla coda");
        addPlaylistToQueueItem.setOnAction(e -> actions.onAddPlaylistToQueue(table.getSelectionModel().getSelectedItem()));

        contextMenu.getItems().addAll(
                editPlaylistItem,
                deletePlaylistItem,
                addPlaylistToQueueItem
        );
        table.setContextMenu(contextMenu);

        contextMenu.setOnShowing(e -> {
            Playlist selectedPlaylist = table.getSelectionModel().getSelectedItem();
            boolean noPlaylistSelected = selectedPlaylist == null;
            boolean isQueueView = actions.isQueueView();

            editPlaylistItem.setVisible(!noPlaylistSelected && !isQueueView);
            deletePlaylistItem.setVisible(!noPlaylistSelected && !isQueueView);
            addPlaylistToQueueItem.setVisible(!noPlaylistSelected && !isQueueView);
        });
    }
}
