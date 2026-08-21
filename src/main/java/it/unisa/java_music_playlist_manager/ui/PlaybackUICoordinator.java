package it.unisa.java_music_playlist_manager.ui;

import it.unisa.java_music_playlist_manager.model.PlaybackManager;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.ViewType;
import it.unisa.java_music_playlist_manager.PlayerController;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.function.Supplier;

public class PlaybackUICoordinator {

    private final Supplier<Playlist> currentOpenedPlaylistSupplier;
    private final Supplier<Playlist> selectedPlaylistSupplier;
    private final Supplier<List<Track>> visibleTracksSupplier;
    private final Supplier<ViewType> currentViewTypeSupplier;
    private final PlayerController playerBarController;
    private final ListView<QueueViewManager.QueueItem> queueListView;
    private final TableView<Track> trackTableView;

    public PlaybackUICoordinator(
            Supplier<Playlist> currentOpenedPlaylistSupplier,
            Supplier<Playlist> selectedPlaylistSupplier,
            Supplier<List<Track>> visibleTracksSupplier,
            Supplier<ViewType> currentViewTypeSupplier,
            PlayerController playerBarController,
            ListView<QueueViewManager.QueueItem> queueListView,
            TableView<Track> trackTableView) {
        this.currentOpenedPlaylistSupplier = currentOpenedPlaylistSupplier;
        this.selectedPlaylistSupplier = selectedPlaylistSupplier;
        this.visibleTracksSupplier = visibleTracksSupplier;
        this.currentViewTypeSupplier = currentViewTypeSupplier;
        this.playerBarController = playerBarController;
        this.queueListView = queueListView;
        this.trackTableView = trackTableView;
    }

    /**
     * Avvia la riproduzione immediata della playlist selezionata o aperta.
     */
    public void handlePlayPlaylistAction() {
        Playlist currentOpened = currentOpenedPlaylistSupplier.get();
        Playlist toPlay = currentOpened != null ? currentOpened : selectedPlaylistSupplier.get();

        if (toPlay != null && !toPlay.getTracks().isEmpty()) {
            PlaybackManager.getInstance().play(toPlay, false);
            updatePlayerUI();
        }
    }

    /**
     * Gestisce ESCLUSIVAMENTE il toggle Play/Pausa sul brano attualmente in riproduzione.
     */
    public void handlePlayPauseAction() {
        PlaybackManager manager = PlaybackManager.getInstance();

        if (manager.getCurrentQueue().isEmpty()) {
            Playlist currentOpened = currentOpenedPlaylistSupplier.get();
            if (currentOpened != null && !currentOpened.getTracks().isEmpty()) {
                manager.selectAndLoadTrack(currentOpened.getTracks().get(0), List.of(currentOpened));
            } else {
                List<Track> visibleTracks = visibleTracksSupplier.get();
                if (!visibleTracks.isEmpty()) {
                    manager.selectAndLoadTrack(visibleTracks.get(0), visibleTracks);
                }
            }
        }

        manager.pressPlay();
        updatePlayerUI();
    }

    /**
     * Avvia la riproduzione di una traccia specifica scelta dall'utente.
     */
    public void handleStartTrackPlayback(Track selectedTrack) {
        PlaybackManager manager = PlaybackManager.getInstance();
        Playlist currentOpened = currentOpenedPlaylistSupplier.get();

        if (currentOpened != null) {
            manager.selectAndLoadTrack(selectedTrack, List.of(currentOpened));
        } else {
            manager.selectAndLoadTrack(selectedTrack, visibleTracksSupplier.get());
        }

        manager.forcePlayCurrent();
        updatePlayerUI();
    }

    /**
     * Aggiorna la barra del player e sincronizza la selezione visiva nella tabella.
     */
    public void updatePlayerUI() {
        if (playerBarController != null) {
            playerBarController.updatePlayerUI();
        }
        syncTableSelection();
    }

    /**
     * Sincronizza l'elemento selezionato nella tabella con la traccia
     * effettivamente in riproduzione.
     */
    public void syncTableSelection() {
        Track currentTrack = PlaybackManager.getInstance().getCurrentTrack();
        if (currentTrack != null) {
            ViewType currentViewType = currentViewTypeSupplier.get();
            
            if (currentViewType == ViewType.QUEUE && queueListView != null && !queueListView.getItems().isEmpty()) {
                ObservableList<QueueViewManager.QueueItem> items = queueListView.getItems();
                int currentQueueIndex = PlaybackManager.getInstance().getCurrentPlayableIndex();
                for (int i = 0; i < items.size(); i++) {
                    QueueViewManager.QueueItem item = items.get(i);
                    if (item.queueIndex == currentQueueIndex && item.track != null && item.track.equals(currentTrack)) {
                        final int index = i;
                        javafx.application.Platform.runLater(() -> {
                            queueListView.getSelectionModel().select(index);
                            queueListView.scrollTo(index);
                        });
                        return;
                    }
                }
            } else if (trackTableView != null && !trackTableView.getItems().isEmpty()) {
                ObservableList<Track> items = trackTableView.getItems();

                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).equals(currentTrack)) {
                        final int index = i;
                        javafx.application.Platform.runLater(() -> {
                            trackTableView.getSelectionModel().select(index);
                            trackTableView.scrollTo(index);
                        });
                        return;
                    }
                }
            }
        }
    }
}
