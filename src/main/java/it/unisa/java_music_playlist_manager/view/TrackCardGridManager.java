package it.unisa.java_music_playlist_manager.view;

import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.services.CoverImageService;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableView;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Gestisce la GridView per la visualizzazione a griglia delle schede dei brani (TrackCardView).
 * Si occupa di riciclare le celle, sincronizzare la selezione con la TableView sottostante,
 * gestire i click (play, selezioni) e avviare il caricamento asincrono delle copertine.
 */
public class TrackCardGridManager {

    private final GridView<Track> trackCardGridView;
    private final TableView<Track> trackTableView;
    private final Consumer<Track> onPlayAction;
    private final Runnable onSelectionChanged;

    public TrackCardGridManager(
            GridView<Track> trackCardGridView,
            TableView<Track> trackTableView,
            Consumer<Track> onPlayAction,
            Runnable onSelectionChanged) {
        
        this.trackCardGridView = trackCardGridView;
        this.trackTableView = Objects.requireNonNull(trackTableView, "trackTableView cannot be null");
        this.onPlayAction = onPlayAction;
        this.onSelectionChanged = onSelectionChanged;

        setupTrackCardGridView();
    }

    private void setupTrackCardGridView() {
        if (trackCardGridView == null) return;
        
        final CoverImageService imageService = CoverImageService.getInstance();

        trackCardGridView.setCellFactory(gridView -> new GridCell<Track>() {
            private TrackCardView card;
            private CompletableFuture<javafx.scene.image.Image> pendingLoadTask;

            // Forte reference necessaria affinché il WeakChangeListener non venga
            // garbage-collected mentre la cella è ancora in uso nel pool di ControlsFX.
            private final ChangeListener<Track> selectionListener;

            {
                setStyle("-fx-padding: 0; -fx-background-color: transparent;");

                selectionListener = (obs, oldVal, newVal) -> {
                    if (card != null) {
                        card.setSelected(newVal != null && newVal.equals(getItem()));
                    }
                };
                trackTableView.getSelectionModel().selectedItemProperty().addListener(
                        new WeakChangeListener<>(selectionListener));

                // I click vengono gestiti qui sulla GridCell
                setOnMouseClicked(event -> {
                    if (event.getButton() != javafx.scene.input.MouseButton.PRIMARY)
                        return;
                    Track t = getItem();
                    if (t == null)
                        return;
                    if (event.getClickCount() == 2) {
                        if (onPlayAction != null) onPlayAction.accept(t);
                    } else if (event.getClickCount() == 1) {
                        trackTableView.getSelectionModel().select(t);
                        if (onSelectionChanged != null) onSelectionChanged.run();
                    }
                });

                setOnContextMenuRequested(event -> {
                    Track t = getItem();
                    if (t == null)
                        return;
                    trackTableView.getSelectionModel().select(t);
                    if (onSelectionChanged != null) onSelectionChanged.run();
                    
                    ContextMenu menu = trackTableView.getContextMenu();
                    if (menu != null) {
                        menu.show(trackCardGridView.getScene().getWindow(),
                                event.getScreenX(), event.getScreenY());
                    }
                });
            }

            @Override
            protected void updateItem(Track item, boolean empty) {
                super.updateItem(item, empty);

                if (pendingLoadTask != null) {
                    pendingLoadTask.cancel(true);
                    pendingLoadTask = null;
                }

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    if (card == null) {
                        card = new TrackCardView();
                        if (onPlayAction != null) {
                            card.setOnPlayAction(onPlayAction);
                        }
                    }

                    card.updateData(item, imageService.getCachedCoverOrDefault(item.getFilePath()));

                    Track selectedTrack = trackTableView.getSelectionModel().getSelectedItem();
                    card.setSelected(selectedTrack != null && selectedTrack.equals(item));

                    final Track itemSnapshot = item;
                    pendingLoadTask = imageService.loadCoverAsync(item.getFilePath());
                    pendingLoadTask.thenAcceptAsync(image -> {
                        if (image != null && itemSnapshot.equals(card.getTrack())) {
                            card.updateData(itemSnapshot, image);
                        }
                    }, javafx.application.Platform::runLater);

                    setGraphic(card);
                }
            }
        });
    }

    public void updateTrackCards(ObservableList<Track> tracks) {
        if (trackCardGridView == null) return;
        trackCardGridView.setItems(tracks);
    }
}
