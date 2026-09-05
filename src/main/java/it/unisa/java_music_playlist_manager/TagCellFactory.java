package it.unisa.java_music_playlist_manager;

import it.unisa.java_music_playlist_manager.model.Tag;
import it.unisa.java_music_playlist_manager.model.Track;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.Set;

public class TagCellFactory implements Callback<TableColumn<Track, Set<Tag>>, TableCell<Track, Set<Tag>>> {

    @Override
    public TableCell<Track, Set<Tag>> call(TableColumn<Track, Set<Tag>> param) {
        return new TableCell<>() {
            
            // 1. Dichiariamo i componenti UI fuori dall'updateItem per ottimizzare le prestazioni
            private final HBox container = new HBox(5);
            private final ScrollPane scrollPane = new ScrollPane(container);

            // Blocco di inizializzazione della cella
            {
                // Impostiamo l'allineamento del contenitore
                container.setStyle("-fx-alignment: center-left; -fx-padding: 2 0;");
                
                // Configuriamo lo ScrollPane per scorrere solo in orizzontale
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Niente barra verticale
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Niente barra verticale
                scrollPane.setPannable(true);
                scrollPane.setFitToHeight(true);
                
                // Rimuoviamo i bordi e lo sfondo di default dello ScrollPane per farlo integrare nella cella
                scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-background-insets: 0; -fx-padding: 0;");
            }

            @Override
            protected void updateItem(Set<Tag> tags, boolean empty) {
                super.updateItem(tags, empty);
                
                if (empty || tags == null || tags.isEmpty()) {
                    setGraphic(null);
                } else {
                    // 2. Svuotiamo i vecchi tag invece di creare un nuovo contenitore
                    container.getChildren().clear();
                    
                    tags.forEach(tag -> {
                        if (tag == null) return;
                        
                        Label badge = new Label();
                        try {
                            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon(tag.getIcon());
                            icon.setIconColor(javafx.scene.paint.Color.WHITE);
                            icon.setIconSize(11);
                            badge.setGraphic(icon);
                        } catch (Exception e) {
                            badge.setText(tag.getIcon());
                        }
                        badge.setStyle("-fx-background-color: #4A4A4A; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 7; -fx-font-size: 11px; -fx-font-weight: bold;");
                        
                        Tooltip tooltip = new Tooltip("Tag: " + tag.getName());
                        Tooltip.install(badge, tooltip);
                        
                        container.getChildren().add(badge);
                    });
                    
                    // 3. Impostiamo lo ScrollPane come grafica della cella
                    setGraphic(scrollPane);
                }
            }
        };
    }
}
