package it.unisa.java_music_playlist_manager.view;

import it.unisa.java_music_playlist_manager.TagCellFactory;
import it.unisa.java_music_playlist_manager.model.Playlist;
import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.Track;
import it.unisa.java_music_playlist_manager.utils.TimeFormatUtils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
/**
 * Factory per la creazione e configurazione delle colonne delle tabelle.
 * Centralizza la definizione grafica e il binding dei dati.
 */
public class TableColumnFactory {

    public static List<TableColumn<Track, ?>> createTrackColumns(Consumer<Track> onPlayAction) {
        TableColumn<Track, Void> playCol = new TableColumn<>("");
        playCol.setMinWidth(40);
        playCol.setPrefWidth(44);
        playCol.setMaxWidth(48);
        playCol.setResizable(false);
        playCol.setSortable(false);
        playCol.setCellFactory(col -> new TableCell<>() {
            private final Button playBtn = new Button();
            {
                playBtn.getStyleClass().add("table-play-button");
                playBtn.setMinSize(22, 22);
                playBtn.setPrefSize(22, 22);
                playBtn.setMaxSize(22, 22);
                FontIcon playIcon = new FontIcon("fas-play");
                playIcon.getStyleClass().add("table-play-icon");
                playIcon.setMouseTransparent(true);
                playBtn.setGraphic(playIcon);
                playBtn.setOnAction(event -> {
                    event.consume();
                    Track track = getTableRow() != null ? getTableRow().getItem() : null;
                    if (track != null && onPlayAction != null) {
                        onPlayAction.accept(track);
                    }
                });
                playBtn.setOnMouseClicked(javafx.scene.input.MouseEvent::consume);
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(playBtn);
                }
            }
        });

        TableColumn<Track, String> titleCol = new TableColumn<>("Titolo");
        titleCol.setMinWidth(160);
        titleCol.setPrefWidth(220);
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Track, String> artistCol = new TableColumn<>("Artista");
        artistCol.setMinWidth(120);
        artistCol.setPrefWidth(160);
        artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));

        TableColumn<Track, String> albumCol = new TableColumn<>("Album");
        albumCol.setMinWidth(120);
        albumCol.setPrefWidth(160);
        albumCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlbum()));

        TableColumn<Track, String> yearCol = new TableColumn<>("Anno");
        yearCol.setMinWidth(60);
        yearCol.setPrefWidth(70);
        yearCol.setMaxWidth(80);
        yearCol.setCellValueFactory(data -> {
            Integer year = data.getValue().getYear();
            if (year == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(String.valueOf(year));
        });

        TableColumn<Track, String> genreCol = new TableColumn<>("Genere");
        genreCol.setMinWidth(90);
        genreCol.setPrefWidth(110);
        genreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre()));

        TableColumn<Track, String> durationCol = new TableColumn<>("Durata");
        durationCol.setMinWidth(65);
        durationCol.setPrefWidth(75);
        durationCol.setMaxWidth(85);
        durationCol.setCellValueFactory(cellData -> {
            int seconds = cellData.getValue().getDuration();
            return new SimpleStringProperty(TimeFormatUtils.formatDuration(seconds));
        });

        TableColumn<Track, Set<Tag>> tagCol = new TableColumn<>("Tag");
        tagCol.setMinWidth(120);
        tagCol.setPrefWidth(160);
        tagCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTags()));
        tagCol.setCellFactory(new TagCellFactory());

        return Arrays.asList(playCol, titleCol, artistCol, albumCol, yearCol, genreCol, durationCol, tagCol);
    }

    public static List<TableColumn<Playlist, ?>> createPlaylistColumns() {
        TableColumn<Playlist, String> nameCol = createColumn("Nome Playlist", 350, Playlist::getTitle);
        nameCol.setMinWidth(200);

        TableColumn<Playlist, Integer> countCol = new TableColumn<>("Numero Brani");
        countCol.setMinWidth(90);
        countCol.setPrefWidth(110);
        countCol.setMaxWidth(130);
        countCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTrackCount()).asObject());

        TableColumn<Playlist, String> durationCol = new TableColumn<>("Durata Totale");
        durationCol.setMinWidth(85);
        durationCol.setMaxWidth(120);
        durationCol.setPrefWidth(100);
        durationCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                TimeFormatUtils.formatDuration(cellData.getValue().getDuration())));

        return Arrays.asList(nameCol, countCol, durationCol);
    }

    public static <S> TableColumn<S, String> createColumn(String title, double width, Function<S, String> mapper) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return col;
    }
}
