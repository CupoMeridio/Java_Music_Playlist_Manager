package it.unisa.java_music_playlist_manager.view;

import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

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
        if (table == null) {
            return;
        }
        table.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();
            row.setOnMousePressed(event -> {
                if (row.isEmpty() || row.getItem() == null) {
                    return;
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    tv.getSelectionModel().select(row.getItem());
                }
            });
            bindTrackContextMenu(row, actions);
            return row;
        });
    }

    public static void bindTrackContextMenu(TableRow<Track> row, ContextMenuActions actions) {
        if (row == null || actions == null) {
            return;
        }
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Modifica brano");
        MenuItem deleteItem = new MenuItem("Elimina brano");
        MenuItem addToPlaylistItem = new MenuItem("Aggiungi a playlist");
        MenuItem addTrackToQueueItem = new MenuItem("Aggiungi brano alla coda");
        MenuItem removeFromPlaylistItem = new MenuItem("Rimuovi dalla playlist");

        contextMenu.getItems().addAll(editItem, deleteItem, addToPlaylistItem, addTrackToQueueItem);

        contextMenu.setOnShowing(e -> {
            Track track = row.getItem();
            if (track == null || actions.isQueueView()) {
                e.consume();
                return;
            }

            editItem.setOnAction(ev -> actions.onEditTrack(track));
            deleteItem.setOnAction(ev -> actions.onDeleteTrack(track));
            addToPlaylistItem.setOnAction(ev -> actions.onAddTrackToPlaylist(track));
            addTrackToQueueItem.setOnAction(ev -> actions.onAddTrackToQueue(track));

            contextMenu.getItems().setAll(editItem, deleteItem, addToPlaylistItem, addTrackToQueueItem);

            Playlist currentOpenedPlaylist = actions.getCurrentOpenedPlaylist();
            if (currentOpenedPlaylist != null && currentOpenedPlaylist.isManuallyEditable()) {
                removeFromPlaylistItem.setOnAction(ev -> actions.onRemoveFromPlaylist(track, currentOpenedPlaylist));
                contextMenu.getItems().add(removeFromPlaylistItem);
            }
        });

        row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu));
    }

    public static void setupPlaylistContextMenu(TableView<Playlist> table, ContextMenuActions actions) {
        if (table == null) {
            return;
        }
        table.setRowFactory(tv -> {
            TableRow<Playlist> row = new TableRow<>();
            row.setOnMousePressed(event -> {
                if (row.isEmpty() || row.getItem() == null) {
                    return;
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    tv.getSelectionModel().select(row.getItem());
                }
            });
            bindPlaylistContextMenu(row, actions);
            return row;
        });
    }

    public static void bindPlaylistContextMenu(TableRow<Playlist> row, ContextMenuActions actions) {
        if (row == null || actions == null) {
            return;
        }
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editPlaylistItem = new MenuItem("Modifica playlist");
        MenuItem deletePlaylistItem = new MenuItem("Elimina playlist");
        MenuItem addPlaylistToQueueItem = new MenuItem("Aggiungi playlist alla coda");

        contextMenu.getItems().addAll(editPlaylistItem, deletePlaylistItem, addPlaylistToQueueItem);

        contextMenu.setOnShowing(e -> {
            Playlist playlist = row.getItem();
            if (playlist == null || actions.isQueueView()) {
                e.consume();
                return;
            }

            editPlaylistItem.setOnAction(ev -> actions.onEditPlaylist(playlist));
            deletePlaylistItem.setOnAction(ev -> actions.onDeletePlaylist(playlist));
            addPlaylistToQueueItem.setOnAction(ev -> actions.onAddPlaylistToQueue(playlist));
        });

        row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu));
    }

    /**
     * Verifica che l'evento del mouse sia avvenuto su una TableRow associata
     * all'elemento selezionato, e non su un'area vuota o sull'intestazione della
     * tabella.
     *
     * @param event        L'evento del mouse catturato dalla tabella.
     * @param selectedItem L'elemento selezionato nel SelectionModel della tabella.
     * @return {@code true} se il click è avvenuto sulla riga valida dell'elemento
     *         selezionato.
     */
    public static boolean isClickOnSelectedTableRow(javafx.scene.input.MouseEvent event, Object selectedItem) {
        if (event == null || selectedItem == null) {
            return false;
        }
        javafx.scene.Node node = event.getPickResult() != null ? event.getPickResult().getIntersectedNode() : null;
        while (node != null) {
            if (node instanceof javafx.scene.control.TableRow<?> row) {
                return row.getItem() != null && java.util.Objects.equals(row.getItem(), selectedItem);
            }
            node = node.getParent();
        }
        return false;
    }
}
