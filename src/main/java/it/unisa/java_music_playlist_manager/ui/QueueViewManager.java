package it.unisa.java_music_playlist_manager.ui;

import it.unisa.java_music_playlist_manager.utils.TimeFormatUtils;

import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Playable;
import it.unisa.java_music_playlist_manager.model.Track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Gestisce interamente la ListView della coda di riproduzione.
 * Incapsula la logica delle celle grafiche, espansione playlist, rimozione dalla coda e doppio click.
 */
public class QueueViewManager {

    private final ListView<QueueItem> queueListView;
    private final Set<Playlist> expandedPlaylists = new HashSet<>();
    private final Runnable onDataChangedCallback;

    public static class QueueItem {
        public final Playable parentPlayable;
        public final Track track;
        public final int queueIndex;

        public QueueItem(Playable parentPlayable, Track track, int queueIndex) {
            this.parentPlayable = parentPlayable;
            this.track = track;
            this.queueIndex = queueIndex;
        }
    }

    public QueueViewManager(ListView<QueueItem> queueListView, Runnable onDataChangedCallback) {
        this.queueListView = queueListView;
        this.onDataChangedCallback = onDataChangedCallback;
        setupQueueListView();
    }

    private void setupQueueListView() {
        if (queueListView == null) return;
        
        queueListView.setCellFactory(lv -> new ListCell<>() {
            private final FontIcon playingIcon = new FontIcon("fas-volume-up");
            {
                playingIcon.getStyleClass().add("queue-playing-icon");
                playingIcon.setIconSize(12);
            }

            @Override
            protected void updateItem(QueueItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().remove("queue-cell-playing");
                    getStyleClass().remove("queue-cell-playlist-header");
                } else {
                    Track currentTrack = PlaybackManager.getInstance().getCurrentTrack();
                    int currentQueueIndex = PlaybackManager.getInstance().getCurrentPlayableIndex();
                    boolean isCurrentQueuePlayable = (item.queueIndex == currentQueueIndex);

                    boolean isPlaying = false;
                    if (item.track != null) {
                        isPlaying = isCurrentQueuePlayable && item.track.equals(currentTrack);
                    } else if (item.parentPlayable instanceof Playlist) {
                        isPlaying = isCurrentQueuePlayable;
                    }

                    if (isPlaying) {
                        if (!getStyleClass().contains("queue-cell-playing")) {
                            getStyleClass().add("queue-cell-playing");
                        }
                        setGraphic(playingIcon);
                    } else {
                        getStyleClass().remove("queue-cell-playing");
                        setGraphic(null);
                    }

                    if (item.parentPlayable instanceof Playlist && item.track == null) {
                        Playlist p = (Playlist) item.parentPlayable;
                        boolean expanded = expandedPlaylists.contains(p);
                        String indicator = expanded ? "[-]" : "[+]";
                        setText(indicator + " " + p.getTitle() + " (" + p.getTrackCount() + " brani)");
                        if (!getStyleClass().contains("queue-cell-playlist-header")) {
                            getStyleClass().add("queue-cell-playlist-header");
                        }
                    } else if (item.parentPlayable instanceof Playlist && item.track != null) {
                        getStyleClass().remove("queue-cell-playlist-header");
                        setText("    " + item.track.getTitle() + " - " + item.track.getAuthor() + " ("
                                + TimeFormatUtils.formatDuration(item.track.getDuration()) + ")");
                    } else if (item.parentPlayable instanceof Track) {
                        getStyleClass().remove("queue-cell-playlist-header");
                        Track t = (Track) item.parentPlayable;
                        setText(t.getTitle() + " - " + t.getAuthor() + " (" + TimeFormatUtils.formatDuration(t.getDuration()) + ")");
                    }
                }
            }
        });

        ContextMenu queueContextMenu = new ContextMenu();
        MenuItem removeFromQueueItem = new MenuItem("Rimuovi dalla coda");
        removeFromQueueItem.setOnAction(e -> {
            QueueItem selected = queueListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                PlaybackManager.getInstance().removeFromQueue(selected.queueIndex);
                refreshQueue(); // Aggiorna visivamente la coda subito
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            }
        });
        queueContextMenu.getItems().add(removeFromQueueItem);
        queueListView.setContextMenu(queueContextMenu);

        queueListView.setOnMouseClicked(event -> {
            QueueItem selected = queueListView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && event.getClickCount() == 1
                    && selected.parentPlayable instanceof Playlist && selected.track == null) {
                Playlist p = (Playlist) selected.parentPlayable;
                if (expandedPlaylists.contains(p)) {
                    expandedPlaylists.remove(p);
                } else {
                    expandedPlaylists.add(p);
                }
                refreshQueue();
            } else if (event.getClickCount() == 2) {
                if (selected.track != null) {
                    int trackIdx = selected.parentPlayable.getTracks().indexOf(selected.track);
                    PlaybackManager.getInstance().setCurrentIndices(selected.queueIndex, trackIdx);
                    PlaybackManager.getInstance().forcePlayCurrent();
                    refreshQueue(); // Aggiorna per mostrare il badge in riproduzione
                    if (onDataChangedCallback != null) onDataChangedCallback.run();
                } else if (selected.parentPlayable instanceof Playlist && !selected.parentPlayable.getTracks().isEmpty()) {
                    PlaybackManager.getInstance().setCurrentIndices(selected.queueIndex, 0);
                    PlaybackManager.getInstance().forcePlayCurrent();
                    refreshQueue();
                    if (onDataChangedCallback != null) onDataChangedCallback.run();
                }
            }
        });
    }

    public void refreshQueue() {
        if (queueListView == null) return;
        
        List<QueueItem> items = new ArrayList<>();
        List<Playable> queue = PlaybackManager.getInstance().getCurrentQueue();
        for (int i = 0; i < queue.size(); i++) {
            Playable p = queue.get(i);
            if (p instanceof Playlist playlist) {
                items.add(new QueueItem(p, null, i));
                if (expandedPlaylists.contains(playlist)) {
                    for (Track t : playlist.getTracks()) {
                        items.add(new QueueItem(p, t, i));
                    }
                }
            } else if (p instanceof Track track) {
                items.add(new QueueItem(p, track, i));
            }
        }
        queueListView.setItems(FXCollections.observableArrayList(items));
    }
}
