package GUI;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import model.FreemasonryChoice;

import java.util.Random;
import java.util.function.Consumer;

// Pergamena a schermo intero mostrata una sola volta quando la città raggiunge il turno 500.
public final class FreemasonryInvitationView
{
    private final StackPane view = new StackPane();
    private final Canvas texture = new Canvas();
    private final Button acceptButton = new Button("Accept");
    private final Button declineButton = new Button("Decline");
    private final Consumer<FreemasonryChoice> choiceHandler;
    private final Runnable closeHandler;
    private Timeline animation;

    public FreemasonryInvitationView(
            Consumer<FreemasonryChoice> choiceHandler,
            Runnable closeHandler)
    {
        if (choiceHandler == null || closeHandler == null)
        {
            throw new IllegalArgumentException(
                    "Invitation callbacks cannot be null"
            );
        }

        this.choiceHandler = choiceHandler;
        this.closeHandler = closeHandler;

        view.setStyle("-fx-background-color: #e4d1a2;");
        view.setVisible(false);
        view.managedProperty().bind(view.visibleProperty());

        texture.setManaged(false);
        texture.setMouseTransparent(true);
        texture.widthProperty().bind(view.widthProperty());
        texture.heightProperty().bind(view.heightProperty());

        ChangeListener<Number> redraw =
                new ChangeListener<Number>()
                {
                    @Override
                    public void changed(
                            ObservableValue<? extends Number> value,
                            Number oldSize,
                            Number newSize)
                    {
                        drawTexture();
                    }
                };

        texture.widthProperty().addListener(redraw);
        texture.heightProperty().addListener(redraw);

        Label title = new Label("A LETTER FOR YOUR CITY");
        title.setStyle(
                "-fx-font-family: Georgia;"
                        + "-fx-font-size: 40px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #50351f;"
        );

        Label message = new Label(
                "Congratulations, player. Your city has reached a good "
                        + "level of progress. More complex dynamics often "
                        + "give rise to even more complex ones, which must "
                        + "be managed wisely. Join us to ensure the best "
                        + "possible development of your city. Things will "
                        + "happen that require human decisions, starting "
                        + "with this one. Your game will be shaped by the "
                        + "choice you are about to make."
        );
        message.setWrapText(true);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setAlignment(Pos.CENTER);
        message.setMaxWidth(950);
        message.setStyle(
                "-fx-font-family: Georgia;"
                        + "-fx-font-size: 25px;"
                        + "-fx-text-fill: #49301d;"
        );

        Label invitation =
                new Label("Accept the invitation to join Freemasonry?");
        invitation.setWrapText(true);
        invitation.setTextAlignment(TextAlignment.CENTER);
        invitation.setStyle(
                "-fx-font-family: Georgia;"
                        + "-fx-font-size: 29px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #56351d;"
        );

        configureButton(acceptButton);
        configureButton(declineButton);

        acceptButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        choose(FreemasonryChoice.ACCEPTED);
                    }
                }
        );

        declineButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        choose(FreemasonryChoice.DECLINED);
                    }
                }
        );

        HBox buttons = new HBox(28, acceptButton, declineButton);
        buttons.setAlignment(Pos.CENTER);

        VBox letter = new VBox(
                31,
                title,
                message,
                invitation,
                buttons,
                createEyeSymbol()
        );
        letter.setAlignment(Pos.CENTER);
        letter.setPadding(new Insets(55, 65, 55, 65));
        letter.setMaxWidth(1100);

        BorderPane rolls = new BorderPane();
        rolls.setMouseTransparent(true);
        rolls.setTop(createRoll());
        rolls.setBottom(createRoll());

        view.getChildren().addAll(texture, letter, rolls);
    }

    private void configureButton(Button button)
    {
        button.setPrefSize(205, 55);
        button.setStyle(
                "-fx-background-color: #ead6a2;"
                        + "-fx-border-color: #785130;"
                        + "-fx-border-width: 2px;"
                        + "-fx-background-radius: 4px;"
                        + "-fx-border-radius: 4px;"
                        + "-fx-text-fill: #412b1b;"
                        + "-fx-font-family: Georgia;"
                        + "-fx-font-size: 21px;"
        );
    }

    private Rectangle createRoll()
    {
        Rectangle roll = new Rectangle(0, 24);
        roll.widthProperty().bind(view.widthProperty());
        roll.setFill(
                new LinearGradient(
                        0, 0, 0, 1,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#58371d")),
                        new Stop(0.35, Color.web("#c09a61")),
                        new Stop(0.68, Color.web("#8c6034")),
                        new Stop(1, Color.web("#4f301a"))
                )
        );
        roll.setEffect(new DropShadow(12, Color.rgb(54, 30, 13, 0.55)));
        return roll;
    }

    private Group createEyeSymbol()
    {
        Polygon triangle = new Polygon(
                0.0, -67.0,
                -83.0, 68.0,
                83.0, 68.0
        );
        triangle.setFill(Color.rgb(208, 178, 121, 0.17));
        triangle.setStroke(Color.web("#654324"));
        triangle.setStrokeWidth(4);

        Path eye = new Path(
                new MoveTo(-36, 9),
                new QuadCurveTo(0, -22, 36, 9),
                new QuadCurveTo(0, 38, -36, 9),
                new ClosePath()
        );
        eye.setFill(Color.web("#f1e2bd"));
        eye.setStroke(Color.web("#654324"));
        eye.setStrokeWidth(3);

        Ellipse iris = new Ellipse(0, 9, 12, 16);
        iris.setFill(Color.web("#967147"));

        Circle pupil = new Circle(0, 9, 6, Color.web("#3d2b20"));
        Circle light = new Circle(-3, 5, 2, Color.web("#fff7df"));

        Group symbol = new Group(triangle, eye, iris, pupil, light);
        symbol.setMouseTransparent(true);
        return symbol;
    }

    private void drawTexture()
    {
        double width = texture.getWidth();
        double height = texture.getHeight();

        if (width <= 0 || height <= 0)
        {
            return;
        }

        GraphicsContext graphics = texture.getGraphicsContext2D();
        graphics.clearRect(0, 0, width, height);
        graphics.setFill(
                new LinearGradient(
                        0, 0, 1, 1,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#d3ba83")),
                        new Stop(0.22, Color.web("#f2e4bd")),
                        new Stop(0.65, Color.web("#e8d6a8")),
                        new Stop(1, Color.web("#b9935b"))
                )
        );
        graphics.fillRect(0, 0, width, height);

        Random random = new Random(500);

        for (int stain = 0; stain < 90; stain++)
        {
            double size = 30 + random.nextDouble() * 170;
            graphics.setFill(Color.rgb(97, 66, 34, 0.014));
            graphics.fillOval(
                    random.nextDouble() * width,
                    random.nextDouble() * height,
                    size,
                    size * 0.6
            );
        }

        for (int grain = 0; grain < 1800; grain++)
        {
            graphics.setFill(Color.rgb(92, 63, 34, 0.06));
            graphics.fillRect(
                    random.nextDouble() * width,
                    random.nextDouble() * height,
                    1 + random.nextDouble() * 2,
                    1
            );
        }

        for (int edge = 0; edge < 28; edge++)
        {
            graphics.setStroke(Color.rgb(91, 57, 29, 0.012));
            graphics.strokeRect(
                    edge,
                    edge,
                    width - edge * 2,
                    height - edge * 2
            );
        }

        graphics.setStroke(Color.rgb(93, 59, 29, 0.48));
        graphics.setLineWidth(2);
        graphics.strokeRect(30, 40, width - 60, height - 80);
    }

    public StackPane getView()
    {
        return view;
    }

    public boolean isShowing()
    {
        return view.isVisible();
    }

    public void show()
    {
        if (view.isVisible())
        {
            return;
        }

        view.setVisible(true);
        view.setScaleY(0.01);
        view.setOpacity(0.7);
        acceptButton.setDisable(true);
        declineButton.setDisable(true);

        animation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(view.scaleYProperty(), 0.01),
                        new KeyValue(view.opacityProperty(), 0.7)
                ),
                new KeyFrame(
                        Duration.millis(1000),
                        new KeyValue(
                                view.scaleYProperty(),
                                1.0,
                                Interpolator.EASE_OUT
                        ),
                        new KeyValue(view.opacityProperty(), 1.0)
                )
        );
        animation.setOnFinished(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        acceptButton.setDisable(false);
                        declineButton.setDisable(false);
                        acceptButton.requestFocus();
                        animation = null;
                    }
                }
        );
        animation.play();
    }

    private void choose(FreemasonryChoice choice)
    {
        acceptButton.setDisable(true);
        declineButton.setDisable(true);
        choiceHandler.accept(choice);

        animation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(view.scaleYProperty(), 1.0),
                        new KeyValue(view.opacityProperty(), 1.0)
                ),
                new KeyFrame(
                        Duration.millis(750),
                        new KeyValue(
                                view.scaleYProperty(),
                                0.01,
                                Interpolator.EASE_IN
                        ),
                        new KeyValue(view.opacityProperty(), 0.7)
                )
        );
        animation.setOnFinished(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        view.setVisible(false);
                        animation = null;
                        closeHandler.run();
                    }
                }
        );
        animation.play();
    }

    public void stop()
    {
        if (animation != null)
        {
            animation.stop();
            animation = null;
        }
    }
}
