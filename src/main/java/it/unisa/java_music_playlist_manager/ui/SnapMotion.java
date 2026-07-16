package it.unisa.java_music_playlist_manager.ui;

import it.unisa.java_music_playlist_manager.ThemeManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * JavaFX CSS non ha una proprietà "transition": i pseudo-stati (:hover,
 * :focused, :pressed) cambiano di scatto, senza interpolazione. Per avere
 * il movimento vero — quello "snap" di Persona 5, non un semplice fade —
 * bisogna animare i parametri via codice, con una Timeline molto corta
 * e un interpolatore che overshoot-a leggermente (l'effetto "elastico").
 */
public final class SnapMotion {

    private static final Duration DURATION = Duration.millis(110);
    // interpolatore "snap": parte veloce, arriva con un piccolo overshoot
    private static final Interpolator SNAP = Interpolator.SPLINE(0.25, 1.0, 0.35, 1.0);

    private SnapMotion() {}

    public static void attach(Region node) {
        // dx maggiore di dy = ombra "storta", più lunga in orizzontale
        attach(node, 5, 2, 10, 1, -2, -2);
    }

    /**
     * @param baseDx / baseDy   offset dell'ombra a riposo
     * @param hoverDx / hoverDy offset dell'ombra in hover/focus (l'ombra "si allunga")
     * @param liftX / liftY     quanto il nodo si sposta (di solito verso alto-sinistra)
     */
    public static void attach(Region node,
                               double baseDx, double baseDy,
                               double hoverDx, double hoverDy,
                               double liftX, double liftY) {

        DropShadow shadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 0, 1, baseDx, baseDy);
        
        // Applica l'ombra di default solo se il tema è Phantom Thief, 
        // altrimenti rimuovi l'effetto
        Runnable updateEffect = () -> {
            if ("Phantom Thief".equals(ThemeManager.getInstance().getActiveTheme())) {
                node.setEffect(shadow);
            } else {
                node.setEffect(null);
                node.setTranslateX(0);
                node.setTranslateY(0);
                node.setScaleX(1.0);
                node.setScaleY(1.0);
            }
        };
        
        // Applica subito l'effetto
        updateEffect.run();
        
        // Sarebbe ideale aggiornare l'effetto al cambio del tema, ma ThemeManager 
        // attualmente non offre listener. Lo aggiorniamo quantomeno sugli eventi mouse.

        Timeline toHover = new Timeline(new KeyFrame(DURATION,
                new KeyValue(shadow.offsetXProperty(), hoverDx, SNAP),
                new KeyValue(shadow.offsetYProperty(), hoverDy, SNAP),
                new KeyValue(node.scaleXProperty(), 1.04, SNAP),
                new KeyValue(node.scaleYProperty(), 1.04, SNAP),
                new KeyValue(node.translateXProperty(), liftX, SNAP),
                new KeyValue(node.translateYProperty(), liftY, SNAP)
        ));

        Timeline toRest = new Timeline(new KeyFrame(DURATION,
                new KeyValue(shadow.offsetXProperty(), baseDx, SNAP),
                new KeyValue(shadow.offsetYProperty(), baseDy, SNAP),
                new KeyValue(node.scaleXProperty(), 1.0, SNAP),
                new KeyValue(node.scaleYProperty(), 1.0, SNAP),
                new KeyValue(node.translateXProperty(), 0, SNAP),
                new KeyValue(node.translateYProperty(), 0, SNAP)
        ));

        node.setOnMouseEntered(e -> { 
            updateEffect.run();
            if (!"Phantom Thief".equals(ThemeManager.getInstance().getActiveTheme())) return;
            toRest.stop(); 
            toHover.playFromStart(); 
        });
        
        node.setOnMouseExited(e -> {
            if (!"Phantom Thief".equals(ThemeManager.getInstance().getActiveTheme())) return;
            if (!node.isFocused()) { toHover.stop(); toRest.playFromStart(); }
        });
        
        node.focusedProperty().addListener((obs, was, isNow) -> {
            if (!"Phantom Thief".equals(ThemeManager.getInstance().getActiveTheme())) return;
            if (isNow) { toRest.stop(); toHover.playFromStart(); }
            else if (!node.isHover()) { toHover.stop(); toRest.playFromStart(); }
        });
    }
}
