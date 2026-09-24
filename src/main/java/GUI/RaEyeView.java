package GUI;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import model.FreemasonryChoice;

public final class RaEyeView
{
    private final StackPane view = new StackPane();

    public RaEyeView()
    {
        Image eye = new Image(Objects.requireNonNull(
                RaEyeView.class.getResource("/images/eye-of-ra.png")
        ).toExternalForm());

        ImageView symbol = new ImageView(eye);
        symbol.setFitWidth(96);
        symbol.setPreserveRatio(true);
        symbol.setSmooth(true);
        symbol.setEffect(new DropShadow(5, Color.rgb(57, 35, 15, 0.35)));

        view.getChildren().add(symbol);
        view.setAlignment(Pos.CENTER);
        view.setPrefHeight(78);
        view.setMaxHeight(78);
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
