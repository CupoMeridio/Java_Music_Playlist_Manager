package it.unisa.java_music_playlist_manager.ui;

import it.unisa.java_music_playlist_manager.model.Track;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Componente UI personalizzato per visualizzare un brano musicale come una "Scheda" (Card).
 * Estende VBox e incapsula la logica grafica usando il pattern fx:root.
 * Questo componente è una "Dumb View" in ottica MVC: non accede ai Service esterni
 * per i dati (es. CoverImageService), ma aspetta che gli vengano passati.
 */
public class TrackCardView extends VBox {

    private Track track;

    @FXML
    private ImageView coverImageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label artistLabel;

    /**
     * Costruttore "dumb view": carica l'FXML senza registrare handler di interazione.
     * Gli eventi mouse sono gestiti esternamente dalla GridCell che ospita questa view.
     */
    public TrackCardView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/components/TrackCardView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException("Errore critico durante il caricamento di TrackCardView.fxml", exception);
        }
    }

    /**
     * Aggiorna i dati mostrati dalla View, senza ricreare il nodo.
     * @param track Il brano da visualizzare
     * @param cover L'immagine di copertina passata dal Controller
     */
    public void updateData(Track track, Image cover) {
        this.track = track;
        if (titleLabel != null && !track.getTitle().equals(titleLabel.getText())) {
            titleLabel.setText(track.getTitle());
        }
        if (artistLabel != null && !track.getAuthor().equals(artistLabel.getText())) {
            artistLabel.setText(track.getAuthor());
        }
        if (coverImageView != null && coverImageView.getImage() != cover) {
            coverImageView.setImage(cover);
        }
    }

    public Track getTrack() {
        return track;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            if (!this.getStyleClass().contains("track-card-selected")) {
                this.getStyleClass().add("track-card-selected");
            }
        } else {
            this.getStyleClass().remove("track-card-selected");
        }
    }
}
