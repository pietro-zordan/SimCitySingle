package GUI;

import audio.SoundManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import model.Achievement;

import java.util.ArrayDeque;
import java.util.Queue;

/** Una notifica alla volta, per non perdere gli sblocchi avvenuti nello stesso tick. */
public final class AchievementNotificationView
{
    private final SoundManager soundManager;
    private final Queue<Achievement> pending = new ArrayDeque<>();
    private final HBox view;
    private final Label title;
    private Timeline animation;

    public AchievementNotificationView(SoundManager soundManager)
    {
        if (soundManager == null)
        {
            throw new IllegalArgumentException("Sound manager cannot be null");
        }
        this.soundManager = soundManager;

        Label star = new Label("★");
        star.setStyle("-fx-font-size: 15px; -fx-text-fill: #a9782f;");
        StackPane medal = new StackPane(star);
        medal.setMinSize(26, 26);
        medal.setMaxSize(26, 26);
        medal.setStyle("-fx-background-color: #f9ebcf; -fx-background-radius: 13;");

        Label eyebrow = new Label("Achievement unlocked");
        eyebrow.setStyle(
                "-fx-text-fill: #9a682e; -fx-font-size: 9px;"
        );
        title = new Label();
        title.setWrapText(true);
        title.setMaxWidth(185);
        title.setStyle(
                "-fx-text-fill: #273442; -fx-font-size: 12px; -fx-font-weight: bold;"
        );

        VBox words = new VBox(2, eyebrow, title);
        words.setAlignment(Pos.CENTER_LEFT);

        view = new HBox(8, medal, words);
        view.setAlignment(Pos.CENTER_LEFT);
        view.setPadding(new Insets(7, 12, 8, 10));
        view.setPrefWidth(255);
        view.setMaxWidth(Region.USE_PREF_SIZE);
        view.setStyle(
                "-fx-background-color: #fffaf1; -fx-background-radius: 0 0 10 10;"
                        + "-fx-border-color: #d5b175; -fx-border-width: 0 0 2 0;"
        );
        view.setEffect(new DropShadow(7, Color.rgb(20, 30, 45, 0.13)));
        view.setMouseTransparent(true);
        view.setVisible(false);
        view.setTranslateY(-100);
    }

    public HBox getView()
    {
        return view;
    }

    public void show(Achievement achievement)
    {
        if (achievement == null)
        {
            return;
        }

        pending.add(achievement);
        if (animation == null)
        {
            playNext();
        }
    }

    private void playNext()
    {
        Achievement achievement = pending.poll();
        if (achievement == null)
        {
            animation = null;
            view.setVisible(false);
            return;
        }

        title.setText(achievement.getTitle());
        view.setTranslateY(-100);
        view.setVisible(true);
        soundManager.playAchievementSound();

        animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(view.translateYProperty(), -100)),
                new KeyFrame(Duration.millis(320),
                        new KeyValue(view.translateYProperty(), 0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(2.35),
                        new KeyValue(view.translateYProperty(), 0)),
                new KeyFrame(Duration.seconds(2.68),
                        new KeyValue(view.translateYProperty(), -100, Interpolator.EASE_IN))
        );
        animation.setOnFinished(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                playNext();
            }
        });
        animation.play();
    }

    public void stop()
    {
        pending.clear();
        if (animation != null)
        {
            animation.stop();
            animation = null;
        }
        view.setVisible(false);
    }
}
