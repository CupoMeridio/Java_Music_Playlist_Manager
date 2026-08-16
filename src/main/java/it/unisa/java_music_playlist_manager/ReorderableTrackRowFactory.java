package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Library;
import it.unisa.java_music_playlist_manager.model.ManualPlaylist;
import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Factory per le righe della tabella dei brani che implementa il Drag & Drop per il riordinamento.
 */
public class ReorderableTrackRowFactory implements Callback<TableView<Track>, TableRow<Track>> {

    private final BooleanSupplier isReorderMode;
    private final Supplier<Playlist> currentPlaylistProvider;
    private final Runnable onDropCompleted;

    public ReorderableTrackRowFactory(BooleanSupplier isReorderMode, Supplier<Playlist> currentPlaylistProvider, Runnable onDropCompleted) {
        this.isReorderMode = isReorderMode;
        this.currentPlaylistProvider = currentPlaylistProvider;
        this.onDropCompleted = onDropCompleted;
    }

    @Override
    public TableRow<Track> call(TableView<Track> tv) {
        TableRow<Track> row = new TableRow<>();

        row.setOnDragDetected(event -> {
            if (!row.isEmpty() && isReorderMode.getAsBoolean()) {
                Integer index = row.getIndex();
                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                db.setDragView(row.snapshot(null, null));
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(index));
                db.setContent(cc);
                event.consume();
            }
        });

        row.setOnDragOver(event -> {
            if (isReorderMode.getAsBoolean() && event.getDragboard().hasString()) {
                if (row.getIndex() != Integer.parseInt(event.getDragboard().getString())) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            }
        });

        row.setOnDragEntered(event -> {
            if (isReorderMode.getAsBoolean() && event.getDragboard().hasString()) {
                if (row.getIndex() != Integer.parseInt(event.getDragboard().getString())) {
                    row.setStyle("-fx-background-color: #dcdcdc;");
                }
            }
        });

        row.setOnDragExited(event -> {
            if (isReorderMode.getAsBoolean()) {
                row.setStyle("");
            }
        });

        row.setOnDragDropped(event -> {
            if (isReorderMode.getAsBoolean() && event.getDragboard().hasString()) {
                int draggedIndex = Integer.parseInt(event.getDragboard().getString());
                int dropIndex = row.isEmpty()
                        ? tv.getItems().size()
                        : row.getIndex() + (draggedIndex < row.getIndex() ? 1 : 0);

                if (draggedIndex == dropIndex) {
                    event.setDropCompleted(true);
                    event.consume();
                    return;
                }

                Playlist currentPlaylist = currentPlaylistProvider.get();
                if (currentPlaylist instanceof ManualPlaylist) {
                    Track oldTrack = PlaybackManager.getInstance().getCurrentTrack();

                    ((ManualPlaylist) currentPlaylist).moveElement(draggedIndex, dropIndex);
                    Library.getInstance().notifyObservers();

                    Track newTrack = PlaybackManager.getInstance().getCurrentTrack();
                    if (oldTrack != null && newTrack != null && !oldTrack.equals(newTrack)) {
                        PlaybackManager.getInstance().forcePlayCurrent();
                        if (onDropCompleted != null) {
                            onDropCompleted.run();
                        }
                    }
                }
                event.setDropCompleted(true);
                event.consume();
            }
        });

        row.setOnMousePressed(event -> {
            if (row.isEmpty() || row.getItem() == null)
                return;
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                tv.getSelectionModel().select(row.getItem());
            }
        });

        return row;
    }
}
