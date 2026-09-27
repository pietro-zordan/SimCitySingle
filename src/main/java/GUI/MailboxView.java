package GUI;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.MailMessage;
import model.Mailbox;

import java.util.List;

/** Compact mailbox card for the right-hand game controls. */
public final class MailboxView
{
    private final Mailbox mailbox;
    private final VBox view;
    private final VBox entries;
    private final Label heading;
    private final ScrollPane scroll;
    private final Button cornerButton;
    private final Pane animationLayer = new Pane();
    private final PauseTransition dockingDelay =
            new PauseTransition(Duration.millis(120));
    private ParallelTransition foldingAnimation;
    private ImageView foldingGhost;
    private String expandedId;
    private boolean docked;

    public MailboxView(Mailbox mailbox)
    {
        if (mailbox == null)
        {
            throw new IllegalArgumentException("Mailbox cannot be null");
        }

        this.mailbox = mailbox;

        heading = new Label();
        heading.setStyle(
                "-fx-text-fill: #334155; -fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
        );

        entries = new VBox(6);

        scroll = new ScrollPane(entries);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMaxHeight(250);
        scroll.setStyle(
                "-fx-background: transparent;"
                        + "-fx-background-color: transparent;"
                        + "-fx-border-color: transparent;"
        );

        view = new VBox(8, heading, scroll);
        view.setPadding(new Insets(10));
        view.setPrefWidth(190);
        view.setMaxWidth(190);
        view.setStyle(
                "-fx-background-color: #f8fafc;"
                        + "-fx-background-radius: 9;"
                        + "-fx-border-color: #dce3eb;"
                        + "-fx-border-radius: 9;"
        );

        cornerButton = createCornerButton();
        dockingDelay.setOnFinished(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if (!docked && expandedId == null
                        && mailbox.hasMessages()
                        && mailbox.getUnreadCount() == 0)
                {
                    foldIntoCorner();
                }
            }
        });

        refresh();
    }

    public VBox getView()
    {
        return view;
    }

    /** Place the corner shortcut above the game without changing its layout. */
    public void attachTo(StackPane gameOverlay)
    {
        animationLayer.setMouseTransparent(true);
        gameOverlay.getChildren().addAll(animationLayer, cornerButton);
        StackPane.setAlignment(cornerButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(cornerButton, new Insets(0, 16, 16, 0));

        // Previously read mail is already folded when a game is loaded.
        docked = mailbox.hasMessages() && mailbox.getUnreadCount() == 0;
        refresh();
    }

    /** Rebuild previews and hide the card entirely when the mailbox is empty. */
    public void refresh()
    {
        List<MailMessage> messages = mailbox.getMessages();

        boolean hasMessages = !messages.isEmpty();
        if (docked && mailbox.getUnreadCount() > 0)
        {
            // New mail brings the card back into the sidebar.
            stopFoldingAnimation();
            docked = false;
        }

        view.setVisible(hasMessages && !docked);
        view.setManaged(hasMessages && !docked);
        cornerButton.setVisible(hasMessages && docked
                && foldingAnimation == null);

        if (!hasMessages)
        {
            expandedId = null;
            return;
        }

        int unreadCount = mailbox.getUnreadCount();
        heading.setText(
                unreadCount > 0
                        ? "MAIL  ·  " + unreadCount + " new"
                        : "MAIL"
        );

        entries.getChildren().clear();

        for (MailMessage message : messages)
        {
            entries.getChildren().add(createEntry(message));
        }

        // Keep the card scrollable when several messages arrive.
        int estimatedHeight = 8 + messages.size() * 68;
        if (expandedId != null)
        {
            estimatedHeight += 140;
        }
        scroll.setPrefViewportHeight(Math.min(230, estimatedHeight));
    }

    private VBox createEntry(final MailMessage message)
    {
        final String id = message.getId();
        boolean expanded = id.equals(expandedId);

        Button preview = new Button(
                (message.isRead() ? "" : "●  ")
                        + message.getSender()
                        + "\n"
                        + message.getSubject()
        );
        preview.setWrapText(true);
        preview.setMaxWidth(Double.MAX_VALUE);
        preview.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: #263446;"
                        + "-fx-font-size: 11px;"
                        + "-fx-alignment: CENTER-LEFT;"
                        + "-fx-padding: 2;"
                        + "-fx-cursor: hand;"
                        + "-fx-focus-color: transparent;"
                        + "-fx-faint-focus-color: transparent;"
        );

        VBox entry = new VBox(6, preview);
        entry.setPadding(new Insets(7));
        entry.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 7;"
                        + "-fx-border-color: "
                        + (expanded ? "#c99748;" : "#e4e8ee;")
                        + "-fx-border-radius: 7;"
        );

        // Only the selected message displays its full text.
        if (expanded)
        {
            Label body = new Label(message.getBody());
            body.setWrapText(true);
            body.setMaxWidth(140);
            body.setStyle(
                    "-fx-text-fill: #475569;"
                            + "-fx-font-size: 11px;"
            );
            entry.getChildren().add(body);
        }

        preview.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                dockingDelay.stop();
                if (id.equals(expandedId))
                {
                    expandedId = null;
                }
                else
                {
                    expandedId = id;
                    mailbox.markAsRead(id);
                }

                refresh();

                // Let the player finish reading before folding the last mail.
                if (expandedId == null && mailbox.getUnreadCount() == 0)
                {
                    dockingDelay.playFromStart();
                }
            }
        });

        return entry;
    }

    private Button createCornerButton()
    {
        Rectangle paper = new Rectangle(36, 40);
        paper.setArcWidth(5);
        paper.setArcHeight(5);
        paper.setFill(Color.web("#f8fafc"));
        paper.setStroke(Color.web("#aebdce"));

        Label envelope = new Label("✉");
        envelope.setStyle("-fx-text-fill: #334155; -fx-font-size: 23px;");

        // A small folded corner makes the mail symbol look like a page tab.
        Polygon fold = new Polygon(0, 0, 11, 0, 11, 11);
        fold.setFill(Color.web("#d9e4f0"));

        StackPane graphic = new StackPane(paper, envelope, fold);
        graphic.setMinSize(42, 44);
        StackPane.setAlignment(fold, Pos.TOP_RIGHT);
        StackPane.setMargin(fold, new Insets(4, 4, 0, 0));

        Button button = new Button();
        button.setGraphic(graphic);
        button.setTooltip(new Tooltip("Open mail"));
        button.setAccessibleText("Open mail");
        button.setStyle(
                "-fx-background-color: transparent; -fx-padding: 0;"
                        + "-fx-cursor: hand; -fx-focus-color: transparent;"
                        + "-fx-faint-focus-color: transparent;"
        );
        button.setVisible(false);
        button.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                docked = false;
                refresh();

                // The reopened card gently settles back into the sidebar.
                view.setOpacity(0);
                FadeTransition reveal =
                        new FadeTransition(Duration.millis(220), view);
                reveal.setToValue(1);
                reveal.play();
            }
        });
        return button;
    }

    /** Move a picture of the entire card to the page tab in the corner. */
    private void foldIntoCorner()
    {
        if (view.getScene() == null)
        {
            docked = true;
            refresh();
            return;
        }

        Bounds from = animationLayer.sceneToLocal(
                view.localToScene(view.getBoundsInLocal()));
        Bounds to = animationLayer.sceneToLocal(
                cornerButton.localToScene(cornerButton.getBoundsInLocal()));

        foldingGhost = new ImageView(
                view.snapshot(new SnapshotParameters(), null));
        foldingGhost.setManaged(false);
        foldingGhost.relocate(from.getMinX(), from.getMinY());
        animationLayer.getChildren().add(foldingGhost);

        docked = true;
        refresh();

        Duration duration = Duration.millis(540);
        TranslateTransition slide =
                new TranslateTransition(duration, foldingGhost);
        slide.setToX(to.getMinX() + to.getWidth() / 2
                - from.getMinX() - from.getWidth() / 2);
        slide.setToY(to.getMinY() + to.getHeight() / 2
                - from.getMinY() - from.getHeight() / 2);
        slide.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition shrink =
                new ScaleTransition(duration, foldingGhost);
        shrink.setToX(0.22);
        shrink.setToY(0.22);

        FadeTransition fade =
                new FadeTransition(duration, foldingGhost);
        fade.setToValue(0.35);

        foldingAnimation = new ParallelTransition(slide, shrink, fade);
        foldingAnimation.setOnFinished(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                animationLayer.getChildren().remove(foldingGhost);
                foldingGhost = null;
                foldingAnimation = null;
                refresh();
            }
        });
        foldingAnimation.play();
    }

    private void stopFoldingAnimation()
    {
        dockingDelay.stop();
        if (foldingAnimation != null)
        {
            foldingAnimation.stop();
            foldingAnimation = null;
        }
        animationLayer.getChildren().remove(foldingGhost);
        foldingGhost = null;
    }

    public void stop()
    {
        stopFoldingAnimation();
    }
}
