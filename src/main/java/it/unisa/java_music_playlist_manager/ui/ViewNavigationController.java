package it.unisa.java_music_playlist_manager.ui;

import it.unisa.java_music_playlist_manager.HomeController;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.model.TrackSortOption;
import it.unisa.java_music_playlist_manager.model.ViewType;
import javafx.scene.control.*;
import org.controlsfx.control.GridView;
import javafx.scene.layout.HBox;
import javafx.scene.Node;

public class ViewNavigationController {

    private ViewType currentViewType = ViewType.MUSIC;
    private Playlist currentOpenedPlaylist = null;

    private final Label viewTitleLabel;
    private final Button actionButton;
    private final HBox controlsBar;
    private final Button undoButton;
    private final Button playPlaylistButton;
    private final ComboBox<TrackSortOption> sortComboBox;
    private final ToggleButton viewToggleButton;
    private final ToggleButton reorderButton;
    private final TableView<Track> trackTableView;
    private final GridView<Track> trackCardGridView;
    private final TableView<Playlist> playlistTableView;
    private final ListView<QueueViewManager.QueueItem> queueListView;
    private final Node homeView;
    private final HomeController homeViewController;

    private final Runnable showSongsColumns;
    private final Runnable showQueueColumns;
    private final Runnable showPlaylistColumns;
    private final Runnable updatePlayPlaylistButtonState;

    public ViewNavigationController(
            Label viewTitleLabel, Button actionButton, HBox controlsBar,
            Button undoButton, Button playPlaylistButton, ComboBox<TrackSortOption> sortComboBox,
            ToggleButton viewToggleButton, ToggleButton reorderButton,
            TableView<Track> trackTableView, GridView<Track> trackCardGridView,
            TableView<Playlist> playlistTableView, ListView<QueueViewManager.QueueItem> queueListView,
            Node homeView, HomeController homeViewController,
            Runnable showSongsColumns, Runnable showQueueColumns,
            Runnable showPlaylistColumns, Runnable updatePlayPlaylistButtonState) {
        
        this.viewTitleLabel = viewTitleLabel;
        this.actionButton = actionButton;
        this.controlsBar = controlsBar;
        this.undoButton = undoButton;
        this.playPlaylistButton = playPlaylistButton;
        this.sortComboBox = sortComboBox;
        this.viewToggleButton = viewToggleButton;
        this.reorderButton = reorderButton;
        this.trackTableView = trackTableView;
        this.trackCardGridView = trackCardGridView;
        this.playlistTableView = playlistTableView;
        this.queueListView = queueListView;
        this.homeView = homeView;
        this.homeViewController = homeViewController;

        this.showSongsColumns = showSongsColumns;
        this.showQueueColumns = showQueueColumns;
        this.showPlaylistColumns = showPlaylistColumns;
        this.updatePlayPlaylistButtonState = updatePlayPlaylistButtonState;
    }

    public ViewType getCurrentViewType() {
        return currentViewType;
    }

    public Playlist getCurrentOpenedPlaylist() {
        return currentOpenedPlaylist;
    }

    public void handleNavigate(ViewType viewType) {
        if (viewType != null) {
            switch (viewType) {
                case HOME -> handleHomeAction();
                case MUSIC -> handleMusicLibraryAction();
                case QUEUE -> handlePlayQueueAction();
                case PLAYLISTS -> handlePlaylistAction();
                default -> {
                }
            }
        }
    }

    public void handleHomeAction() {
        currentOpenedPlaylist = null;
        currentViewType = ViewType.HOME;
        if (viewTitleLabel != null) viewTitleLabel.setText("Home");
        if (actionButton != null) {
            actionButton.setVisible(false);
            actionButton.setManaged(false);
        }
        if (controlsBar != null) {
            controlsBar.setVisible(false);
            controlsBar.setManaged(false);
        }
        updateContextualUI();
        if (updatePlayPlaylistButtonState != null) updatePlayPlaylistButtonState.run();
        
        if (reorderButton != null) {
            reorderButton.setVisible(false);
            reorderButton.setManaged(false);
            reorderButton.setSelected(false);
        }

        if (trackTableView != null) {
            trackTableView.setVisible(false);
            trackTableView.setManaged(false);
        }
        if (trackCardGridView != null) {
            trackCardGridView.setVisible(false);
            trackCardGridView.setManaged(false);
        }
        if (playlistTableView != null) {
            playlistTableView.setVisible(false);
            playlistTableView.setManaged(false);
        }
        if (queueListView != null) {
            queueListView.setVisible(false);
            queueListView.setManaged(false);
        }
        if (homeView != null) {
            homeView.setVisible(true);
            homeView.setManaged(true);
        }
        if (homeViewController != null) {
            homeViewController.refreshStats();
        }
    }

    public void handleMusicLibraryAction() {
        hideHomePanel();
        currentOpenedPlaylist = null;
        currentViewType = ViewType.MUSIC;
        if (viewTitleLabel != null) viewTitleLabel.setText("Musica");
        if (actionButton != null) {
            actionButton.setText("Aggiungi brano");
            actionButton.setVisible(true);
            actionButton.setManaged(true);
        }
        if (controlsBar != null) {
            controlsBar.setVisible(true);
            controlsBar.setManaged(true);
        }
        updateContextualUI();
        if (showSongsColumns != null) showSongsColumns.run();
    }

    public void handlePlayQueueAction() {
        hideHomePanel();
        currentOpenedPlaylist = null;
        currentViewType = ViewType.QUEUE;
        if (viewTitleLabel != null) viewTitleLabel.setText("Coda di riproduzione");
        if (actionButton != null) {
            actionButton.setVisible(false);
            actionButton.setManaged(false);
        }
        if (controlsBar != null) {
            controlsBar.setVisible(true);
            controlsBar.setManaged(true);
        }
        updateContextualUI();
        if (showQueueColumns != null) showQueueColumns.run();
    }

    public void handlePlaylistAction() {
        hideHomePanel();
        currentOpenedPlaylist = null;
        currentViewType = ViewType.PLAYLISTS;
        if (viewTitleLabel != null) viewTitleLabel.setText("Playlist");
        if (actionButton != null) {
            actionButton.setText("Nuova playlist");
            actionButton.setVisible(true);
            actionButton.setManaged(true);
        }
        if (controlsBar != null) {
            controlsBar.setVisible(true);
            controlsBar.setManaged(true);
        }
        updateContextualUI();
        if (showPlaylistColumns != null) showPlaylistColumns.run();
        if (updatePlayPlaylistButtonState != null) updatePlayPlaylistButtonState.run();
    }

    public void openPlaylistDetail(Playlist playlist) {
        currentOpenedPlaylist = playlist;
        currentViewType = ViewType.PLAYLIST_DETAIL;
        if (viewTitleLabel != null) viewTitleLabel.setText(playlist.getTitle());
        if (actionButton != null) {
            actionButton.setVisible(false);
            actionButton.setManaged(false);
        }
        updateContextualUI();
        if (playlist.isManuallyEditable() && reorderButton != null) {
            reorderButton.setVisible(true);
            reorderButton.setManaged(true);
            reorderButton.setSelected(false);
        } else if (reorderButton != null) {
            reorderButton.setVisible(false);
            reorderButton.setManaged(false);
            reorderButton.setSelected(false);
        }
        if (showSongsColumns != null) showSongsColumns.run();
    }

    private void updateContextualUI() {
        boolean isEditableSection = currentViewType == ViewType.MUSIC
                || currentViewType == ViewType.PLAYLISTS
                || currentViewType == ViewType.PLAYLIST_DETAIL;
        boolean isPlaylistListSection = currentViewType == ViewType.PLAYLISTS;
        boolean isTrackSection = currentViewType == ViewType.MUSIC
                || currentViewType == ViewType.PLAYLIST_DETAIL;

        if (undoButton != null) {
            undoButton.setVisible(isEditableSection);
            undoButton.setManaged(isEditableSection);
        }
        if (playPlaylistButton != null) {
            playPlaylistButton.setVisible(isPlaylistListSection);
            playPlaylistButton.setManaged(isPlaylistListSection);
        }
        if (sortComboBox != null) {
            sortComboBox.setVisible(isTrackSection);
            sortComboBox.setManaged(isTrackSection);
        }
        if (viewToggleButton != null) {
            viewToggleButton.setVisible(isTrackSection);
            viewToggleButton.setManaged(isTrackSection);
        }
    }

    private void hideHomePanel() {
        if (reorderButton != null) {
            reorderButton.setVisible(false);
            reorderButton.setManaged(false);
            reorderButton.setSelected(false);
        }
        if (homeView != null) {
            homeView.setVisible(false);
            homeView.setManaged(false);
        }
    }
}
