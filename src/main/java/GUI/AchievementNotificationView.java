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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
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
    private final Rectangle reveal;
    private Timeline animation;

    public AchievementNotificationView(SoundManager soundManager)
    {
        if (soundManager == null)
        {
            throw new IllegalArgumentException("Sound manager cannot be null");
        }
        this.soundManager = soundManager;

        Label star = new Label("✦");
        star.setStyle("-fx-font-size: 17px; -fx-text-fill: #d8b979;");

        Label eyebrow = new Label("ACHIEVEMENT UNLOCKED");
        eyebrow.setStyle(
                "-fx-text-fill: #d8b979; -fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
        );
        title = new Label();
        title.setWrapText(true);
        title.setMaxWidth(206);
        title.setStyle(
                "-fx-text-fill: #f8fafc; -fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        VBox words = new VBox(2, eyebrow, title);
        words.setAlignment(Pos.CENTER_LEFT);

        view = new HBox(10, star, words);
        view.setAlignment(Pos.CENTER_LEFT);
        view.setPadding(new Insets(7, 12, 8, 12));
        view.setMinSize(252, 58);
        view.setPrefSize(252, 58);
        view.setMaxSize(252, 58);
        view.setStyle(
                "-fx-background-color: #273142; -fx-background-radius: 0 0 10 10;"
                        + "-fx-border-color: #c8a76a; -fx-border-width: 0 0 2 0;"
        );

        // Il ritaglio resta ancorato in alto: aprendo mostra il pannello
        // verso il basso; chiudendo ne ritira il bordo inferiore verso l'alto.
        reveal = new Rectangle(252, 0);
        reveal.widthProperty().bind(view.widthProperty());
        view.setClip(reveal);
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
        reveal.setHeight(0);
        view.setVisible(true);
        soundManager.playAchievementSound();

        animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(reveal.heightProperty(), 0)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(reveal.heightProperty(), 58, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(2.45),
                        new KeyValue(reveal.heightProperty(), 58)),
                new KeyFrame(Duration.seconds(2.80),
                        new KeyValue(reveal.heightProperty(), 0, Interpolator.EASE_IN))
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
