package GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GameOverView
{
    private final VBox view;

    public GameOverView(GameNavigation navigation)
    {
        if (navigation == null)
        {
            throw new IllegalArgumentException(
                    "Game navigation cannot be null"
            );
        }

        Label title = new Label(
                "YOU LOST"
        );

        title.setStyle(
                "-fx-font-size: 36px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: white;"
        );

        Label message = new Label(
                "The city is no longer economically sustainable."
        );

        message.setStyle(
                "-fx-font-size: 16px;"
                        + "-fx-text-fill: white;"
        );

        Button restartButton =
                new Button("Play Again");

        Button exitButton =
                new Button("Exit");

        restartButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        navigation.restartGame();
                    }
                }
        );

        exitButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        navigation.returnToHome();
                    }
                }
        );

        HBox buttons = new HBox(
                10,
                restartButton,
                exitButton
        );

        buttons.setAlignment(Pos.CENTER);

        view = new VBox(
                20,
                title,
                message,
                buttons
        );

        view.setAlignment(Pos.CENTER);

        view.setStyle(
                "-fx-background-color: rgba(0,0,0,0.85);"
                        + "-fx-padding: 40px;"
                        + "-fx-background-radius: 12px;"
        );

        view.setVisible(false);
        view.setManaged(false);
    }

    public void show()
    {
        view.setManaged(true);
        view.setVisible(true);
    }

    public void hide()
    {
        view.setVisible(false);
        view.setManaged(false);
    }

    public VBox getView()
    {
        return view;
    }
}