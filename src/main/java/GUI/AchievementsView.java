package GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Achievement;
import model.AchievementManager;

/** Schermata degli achievement già ottenuti, indipendente dal salvataggio della città. */
public final class AchievementsView
{
    private final Scene scene;

    public AchievementsView(AchievementManager manager, final Runnable onBack)
    {
        if (manager == null || onBack == null)
        {
            throw new IllegalArgumentException("Achievements view dependencies cannot be null");
        }

        Button backButton = new Button("←  Menu");
        backButton.setStyle(
                "-fx-background-color: white; -fx-text-fill: #334155;"
                        + "-fx-border-color: #e2e8f0; -fx-border-radius: 9;"
                        + "-fx-background-radius: 9; -fx-padding: 9 15;"
                        + "-fx-cursor: hand; -fx-font-size: 13px;"
        );
        backButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                onBack.run();
            }
        });

        Label heading = new Label("Achievements");
        heading.setStyle(
                "-fx-text-fill: #182438; -fx-font-size: 29px; -fx-font-weight: bold;"
        );

        int unlockedCount = manager.getUnlockedAchievements().size();
        int totalCount = Achievement.values().length;
        Label counter = new Label(unlockedCount + " / " + totalCount + " unlocked");
        counter.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");

        ProgressBar progress = new ProgressBar((double) unlockedCount / totalCount);
        progress.setPrefWidth(660);
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setPrefHeight(7);
        progress.setStyle("-fx-accent: #c99748;");

        VBox header = new VBox(11, backButton, heading, counter, progress);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(26, 44, 22, 44));

        VBox entries = new VBox(12);
        entries.setPadding(new Insets(0, 44, 24, 44));
        entries.setFillWidth(true);

        // L'ordine dell'enum è stabile, a differenza dell'HashSet usato dal manager.
        for (Achievement achievement : Achievement.values())
        {
            if (manager.isUnlocked(achievement))
            {
                entries.getChildren().add(createCard(achievement));
            }
        }

        if (unlockedCount == 0)
        {
            Label emptyTitle = new Label("Your story starts here");
            emptyTitle.setStyle(
                    "-fx-text-fill: #263446; -fx-font-size: 19px; -fx-font-weight: bold;"
            );
            Label emptyHint = new Label("Play a city to unlock your first achievement.");
            emptyHint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            VBox empty = new VBox(8, emptyTitle, emptyHint);
            empty.setAlignment(Pos.CENTER);
            empty.setMinHeight(270);
            entries.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(entries);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background-color: transparent; -fx-background: #f5f7fa;"
                        + "-fx-border-color: transparent; -fx-padding: 0;"
        );

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fa;");
        root.setTop(header);
        root.setCenter(scroll);
        scene = new Scene(root, 800, 600);
    }

    private HBox createCard(Achievement achievement)
    {
        Label star = new Label("★");
        star.setStyle("-fx-text-fill: #a76b21; -fx-font-size: 24px;");
        StackPane medal = new StackPane(star);
        medal.setMinSize(52, 52);
        medal.setMaxSize(52, 52);
        medal.setStyle("-fx-background-color: #fff2d9; -fx-background-radius: 14;");

        Label title = new Label(achievement.getTitle());
        title.setStyle(
                "-fx-text-fill: #1e293b; -fx-font-size: 16px; -fx-font-weight: bold;"
        );
        Label description = new Label(achievement.getDescription().trim());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        VBox details = new VBox(4, title, description);
        details.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

        Label unlocked = new Label("UNLOCKED");
        unlocked.setStyle(
                "-fx-text-fill: #9a6827; -fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        HBox card = new HBox(16, medal, details, unlocked);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setMinHeight(92);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14;"
                        + "-fx-border-color: #e4e8ee; -fx-border-radius: 14;"
        );
        return card;
    }

    public Scene getScene()
    {
        return scene;
    }
}
