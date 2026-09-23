package GUI;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import model.FreemasonryChoice;

// Occhio solare destro: sopracciglio, linea cosmetica e segni del falco wedjat.
public final class RaEyeView
{
    private static final Color INK = Color.web("#382516");
    private static final Color GOLD = Color.web("#c79b55");
    private final StackPane view = new StackPane();

    public RaEyeView()
    {
        SVGPath eyebrow = filledPath(
                "M 17 31 C 54 7 114 4 173 26 "
                        + "C 139 19 72 15 17 36 Z",
                INK
        );
        SVGPath browHighlight = line(
                "M 29 27 C 75 10 127 13 160 24",
                GOLD,
                2
        );

        SVGPath eyeOutline = filledPath(
                "M 23 54 C 57 23 118 21 160 51 "
                        + "C 127 73 75 82 23 54 Z",
                INK
        );
        SVGPath eyeWhite = filledPath(
                "M 37 54 C 69 35 120 32 149 50 "
                        + "C 121 66 76 73 37 54 Z",
                Color.web("#f0dfb9")
        );

        Ellipse iris = new Ellipse(95, 52, 13, 17);
        iris.setFill(Color.web("#3d7180"));
        iris.setStroke(GOLD);
        iris.setStrokeWidth(2);

        Circle pupil = new Circle(95, 52, 6, INK);
        Circle glint = new Circle(92, 47, 2, Color.web("#fff6d8"));

        SVGPath upperLidAndWing = filledPath(
                "M 23 54 C 58 23 116 20 160 51 "
                        + "L 185 40 L 172 56 L 158 55 "
                        + "C 114 31 64 30 23 54 Z",
                INK
        );
        SVGPath lowerLid = line(
                "M 29 56 C 73 80 126 71 158 53",
                GOLD,
                2
        );

        SVGPath verticalMark = filledPath(
                "M 96 69 L 105 69 L 107 108 "
                        + "L 102 118 L 97 107 Z",
                INK
        );
        SVGPath verticalHighlight = line(
                "M 101 78 L 102 106",
                GOLD,
                2
        );

        SVGPath cheekAndSpiral = line(
                "M 54 65 C 58 79 53 91 41 100 "
                        + "C 33 106 24 103 24 96 "
                        + "C 24 91 30 88 34 92",
                INK,
                7
        );

        Group symbol = new Group(
                eyebrow,
                browHighlight,
                eyeOutline,
                eyeWhite,
                iris,
                pupil,
                glint,
                upperLidAndWing,
                lowerLid,
                verticalMark,
                verticalHighlight,
                cheekAndSpiral
        );
        // Il lato destro è il simbolo solare associato a Ra.
        symbol.setScaleX(-0.75);
        symbol.setScaleY(0.75);
        symbol.setEffect(new DropShadow(7, Color.rgb(57, 35, 15, 0.4)));

        view.getChildren().add(symbol);
        view.setAlignment(Pos.CENTER);
        view.setPrefHeight(95);
        view.setMaxHeight(95);
        view.setMouseTransparent(true);
        view.setVisible(false);
        view.managedProperty().bind(view.visibleProperty());
    }

    private SVGPath filledPath(String data, Color color)
    {
        SVGPath path = new SVGPath();
        path.setContent(data);
        path.setFill(color);
        return path;
    }

    private SVGPath line(String data, Color color, double width)
    {
        SVGPath path = new SVGPath();
        path.setContent(data);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(color);
        path.setStrokeWidth(width);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return path;
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
