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
    private final Label description;
    private Timeline animation;

    public AchievementNotificationView(SoundManager soundManager)
    {
        if (soundManager == null)
        {
            throw new IllegalArgumentException("Sound manager cannot be null");
        }
        this.soundManager = soundManager;

        Label star = new Label("★");
        star.setStyle("-fx-font-size: 23px; -fx-text-fill: #f5d48b;");
        StackPane medal = new StackPane(star);
        medal.setMinSize(48, 48);
        medal.setMaxSize(48, 48);
        medal.setStyle("-fx-background-color: #705321; -fx-background-radius: 13;");

        Label eyebrow = new Label("ACHIEVEMENT UNLOCKED");
        eyebrow.setStyle(
                "-fx-text-fill: #e8c57e; -fx-font-size: 10px; -fx-font-weight: bold;"
        );
        title = new Label();
        title.setWrapText(true);
        title.setMaxWidth(245);
        title.setStyle(
                "-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;"
        );
        description = new Label();
        description.setWrapText(true);
        description.setMaxWidth(245);
        description.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        VBox words = new VBox(4, eyebrow, title, description);
        words.setAlignment(Pos.CENTER_LEFT);

        view = new HBox(15, medal, words);
        view.setAlignment(Pos.CENTER_LEFT);
        view.setPadding(new Insets(17, 20, 17, 18));
        view.setPrefWidth(360);
        view.setMaxWidth(Region.USE_PREF_SIZE);
        view.setStyle(
                "-fx-background-color: #1b2735; -fx-background-radius: 15;"
                        + "-fx-border-color: #ba9556; -fx-border-radius: 15;"
        );
        view.setEffect(new DropShadow(18, Color.rgb(20, 30, 45, 0.27)));
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
        description.setText(achievement.getDescription().trim());
        view.setVisible(true);
        soundManager.playAchievementSound();

        animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(view.opacityProperty(), 0),
                        new KeyValue(view.translateXProperty(), 52),
                        new KeyValue(view.scaleXProperty(), 0.96),
                        new KeyValue(view.scaleYProperty(), 0.96)),
                new KeyFrame(Duration.millis(350),
                        new KeyValue(view.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(view.translateXProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(view.scaleXProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(view.scaleYProperty(), 1, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(3.2),
                        new KeyValue(view.opacityProperty(), 1),
                        new KeyValue(view.translateXProperty(), 0)),
                new KeyFrame(Duration.seconds(3.55),
                        new KeyValue(view.opacityProperty(), 0),
                        new KeyValue(view.translateXProperty(), 30))
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
