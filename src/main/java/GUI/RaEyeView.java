package GUI;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import model.FreemasonryChoice;

// Simbolo egizio mostrato sopra la griglia soltanto dopo aver accettato l'invito.
public final class RaEyeView
{
    private static final Color INK = Color.web("#50341c");
    private final StackPane view = new StackPane();

    public RaEyeView()
    {
        Path eyebrow = new Path(
                new MoveTo(12, 23),
                new QuadCurveTo(67, -14, 132, 19)
        );
        eyebrow.setFill(Color.TRANSPARENT);
        eyebrow.setStroke(INK);
        eyebrow.setStrokeWidth(4);

        Path eye = new Path(
                new MoveTo(15, 34),
                new QuadCurveTo(71, -1, 126, 32),
                new QuadCurveTo(72, 71, 15, 34),
                new ClosePath()
        );
        eye.setFill(Color.web("#e6ca8b"));
        eye.setStroke(INK);
        eye.setStrokeWidth(4);

        Ellipse iris = new Ellipse(72, 32, 14, 18);
        iris.setFill(Color.web("#91612d"));
        iris.setStroke(INK);
        iris.setStrokeWidth(2);

        Circle pupil = new Circle(72, 32, 7, INK);
        Circle light = new Circle(68, 27, 2, Color.web("#fff2cc"));

        Path wing = new Path(
                new MoveTo(124, 32),
                new QuadCurveTo(139, 30, 149, 21)
        );
        wing.setFill(Color.TRANSPARENT);
        wing.setStroke(INK);
        wing.setStrokeWidth(4);

        Path marking = new Path(
                new MoveTo(72, 50),
                new QuadCurveTo(72, 65, 54, 78),
                new MoveTo(39, 47),
                new QuadCurveTo(31, 64, 18, 62),
                new QuadCurveTo(32, 72, 48, 62)
        );
        marking.setFill(Color.TRANSPARENT);
        marking.setStroke(INK);
        marking.setStrokeWidth(3);

        Group symbol = new Group(
                eyebrow, eye, iris, pupil, light, wing, marking
        );
        symbol.setEffect(new DropShadow(7, Color.rgb(57, 35, 15, 0.35)));

        view.getChildren().add(symbol);
        view.setAlignment(Pos.CENTER);
        view.setPrefHeight(85);
        view.setMaxHeight(85);
        view.setMouseTransparent(true);
        view.setVisible(false);
        view.managedProperty().bind(view.visibleProperty());
    }

    public StackPane getView()
    {
        return view;
    }

    public void refresh(FreemasonryChoice choice)
    {
        view.setVisible(choice == FreemasonryChoice.ACCEPTED);
    }
}
