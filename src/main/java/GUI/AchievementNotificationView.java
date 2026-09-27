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
        star.setStyle("-fx-font-size: 17px; -fx-text-fill: #a9782f;");
        StackPane medal = new StackPane(star);
        medal.setMinSize(32, 32);
        medal.setMaxSize(32, 32);
        medal.setStyle("-fx-background-color: #fff1d8; -fx-background-radius: 16;");

        Label eyebrow = new Label("Achievement unlocked");
        eyebrow.setStyle(
                "-fx-text-fill: #9a682e; -fx-font-size: 10px;"
        );
        title = new Label();
        title.setWrapText(true);
        title.setMaxWidth(214);
        title.setStyle(
                "-fx-text-fill: #273442; -fx-font-size: 13px; -fx-font-weight: bold;"
        );

        VBox words = new VBox(2, eyebrow, title);
        words.setAlignment(Pos.CENTER_LEFT);

        view = new HBox(10, medal, words);
        view.setAlignment(Pos.CENTER_LEFT);
        view.setPadding(new Insets(9, 13, 9, 11));
        view.setPrefWidth(285);
        view.setMaxWidth(Region.USE_PREF_SIZE);
        view.setStyle(
                "-fx-background-color: #fcfcfb; -fx-background-radius: 10;"
                        + "-fx-border-color: #e8e2d7; -fx-border-radius: 10;"
        );
        view.setEffect(new DropShadow(9, Color.rgb(20, 30, 45, 0.14)));
        view.setMouseTransparent(true);
        view.setVisible(false);
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
        view.setVisible(true);
        soundManager.playAchievementSound();

        animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(view.opacityProperty(), 0),
                        new KeyValue(view.translateYProperty(), -12)),
                new KeyFrame(Duration.millis(220),
                        new KeyValue(view.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(view.translateYProperty(), 0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(2.25),
                        new KeyValue(view.opacityProperty(), 1),
                        new KeyValue(view.translateYProperty(), 0)),
                new KeyFrame(Duration.seconds(2.50),
                        new KeyValue(view.opacityProperty(), 0),
                        new KeyValue(view.translateYProperty(), -7))
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
