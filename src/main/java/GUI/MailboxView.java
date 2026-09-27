package GUI;

import audio.SoundManager;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.MailMessage;
import model.Mailbox;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Mostra la posta nel pannello laterale e apre le lettere in un riquadro centrale. */
public final class MailboxView
{
    private final Mailbox mailbox;
    private final SoundManager soundManager;
    private final Set<String> knownMessageIds = new HashSet<>();
    private final VBox view;
    private final VBox entries;
    private final Label heading;
    private final ScrollPane scroll;
    private final Button cornerButton;
    private final StackPane readerOverlay;
    private final Label readerSubject = new Label();
    private final Label readerSender = new Label();
    private final Label readerTick = new Label();
    private final Label readerBody = new Label();
    private final Pane animationLayer = new Pane();
    private final PauseTransition dockingDelay =
            new PauseTransition(Duration.millis(120));
    private ParallelTransition arrivalAnimation;
    private ParallelTransition foldingAnimation;
    private ImageView foldingGhost;
    private String expandedId;
    private boolean docked;
    private boolean attached;
    private boolean hadMessages;

    /** Prepara le anteprime, il lettore e l'icona della posta richiusa. */
    public MailboxView(Mailbox mailbox, SoundManager soundManager)
    {
        if (mailbox == null || soundManager == null)
        {
            throw new IllegalArgumentException(
                    "Mailbox and sound manager cannot be null"
            );
        }

        this.mailbox = mailbox;
        this.soundManager = soundManager;

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
        readerOverlay = createReaderOverlay();
        // La casella si richiude solo dopo la chiusura dell'ultima lettera letta.
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

    /** Restituisce il piccolo elenco da mettere sopra il pulsante Next Tick. */
    public VBox getView()
    {
        return view;
    }

    /** Aggiunge lettore e icona sopra il gioco senza alterare il pannello. */
    public void attachTo(StackPane gameOverlay)
    {
        animationLayer.setMouseTransparent(true);
        gameOverlay.getChildren().addAll(
                animationLayer, cornerButton, readerOverlay);
        StackPane.setAlignment(cornerButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(cornerButton, new Insets(0, 16, 16, 0));

        // Una partita caricata con tutta la posta già letta mostra solo l'icona.
        docked = mailbox.hasMessages() && mailbox.getUnreadCount() == 0;
        refresh();
        attached = true;
    }

    /** Aggiorna anteprime e contatore; nasconde tutto se non ci sono lettere. */
    public void refresh()
    {
        List<MailMessage> messages = mailbox.getMessages();

        boolean hasMessages = !messages.isEmpty();
        boolean wasDocked = docked;
        boolean animateArrival = attached && hasMessages
                && (!hadMessages
                || (docked && mailbox.getUnreadCount() > 0));
        hadMessages = hasMessages;
        if (docked && mailbox.getUnreadCount() > 0)
        {
            // Una nuova lettera fa ricomparire la casella nel pannello laterale.
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
            readerOverlay.setVisible(false);
            return;
        }

        int unreadCount = mailbox.getUnreadCount();
        heading.setText(
                unreadCount > 0
                        ? "MAIL  ·  " + unreadCount + " new"
                        : "MAIL"
        );

        entries.getChildren().clear();

        boolean newMessageArrived = false;

        // La lettera più recente compare in cima, sempre visibile all'arrivo.
        for (int index = messages.size() - 1; index >= 0; index--)
        {
            MailMessage message = messages.get(index);
            VBox entry = createEntry(message);
            entries.getChildren().add(entry);

            if (attached && !knownMessageIds.contains(message.getId()))
            {
                newMessageArrived = true;
                if (!wasDocked)
                {
                    playNewEntryArrival(entry);
                }
            }
            knownMessageIds.add(message.getId());
        }

        if (newMessageArrived)
        {
            soundManager.playMailNotificationSound();
        }

        // L'elenco resta piccolo e scorre quando arrivano molte lettere.
        int estimatedHeight = 8 + messages.size() * 68;
        scroll.setPrefViewportHeight(Math.min(230, estimatedHeight));

        if (animateArrival)
        {
            playArrival();
        }
    }

    /** Fa scendere dolcemente la casella quando arriva la prima nuova lettera. */
    private void playArrival()
    {
        if (arrivalAnimation != null)
        {
            arrivalAnimation.stop();
        }

        view.setOpacity(0);
        view.setTranslateY(-16);

        FadeTransition fade =
                new FadeTransition(Duration.millis(320), view);
        fade.setToValue(1);

        TranslateTransition slide =
                new TranslateTransition(Duration.millis(320), view);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        arrivalAnimation = new ParallelTransition(fade, slide);
        arrivalAnimation.setOnFinished(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                view.setOpacity(1);
                view.setTranslateY(0);
                arrivalAnimation = null;
            }
        });
        arrivalAnimation.play();
    }

    /** Anima solo la nuova anteprima se la casella era già aperta. */
    private void playNewEntryArrival(VBox entry)
    {
        entry.setOpacity(0);
        entry.setTranslateY(-10);

        FadeTransition fade =
                new FadeTransition(Duration.millis(300), entry);
        fade.setToValue(1);

        TranslateTransition slide =
                new TranslateTransition(Duration.millis(300), entry);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();
    }

    /** Crea un'anteprima cliccabile senza mostrare il testo nella colonna stretta. */
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

        // Il clic segna la lettera come letta e la apre nel riquadro centrale.
        preview.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                dockingDelay.stop();
                expandedId = id;
                mailbox.markAsRead(id);
                readerSubject.setText(message.getSubject());
                readerSender.setText("From " + message.getSender());
                readerTick.setText("Tick " + message.getReceivedTick());
                readerBody.setText(message.getBody());
                refresh();
                readerOverlay.setVisible(true);
            }
        });

        return entry;
    }

    /** Costruisce il lettore centrale con contenuto scorrevole e pulsante Close. */
    private StackPane createReaderOverlay()
    {
        Label eyebrow = new Label("INBOX");
        eyebrow.setStyle(
                "-fx-text-fill: #a46c29; -fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
        );

        Button close = new Button("Close");
        close.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #334155;"
                        + "-fx-background-radius: 8; -fx-padding: 7 13;"
                        + "-fx-cursor: hand;"
        );
        close.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                readerOverlay.setVisible(false);
                expandedId = null;
                refresh();

                if (mailbox.hasMessages()
                        && mailbox.getUnreadCount() == 0)
                {
                    dockingDelay.playFromStart();
                }
            }
        });

        HBox top = new HBox(10, eyebrow, close);
        top.setAlignment(Pos.CENTER_LEFT);
        eyebrow.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(eyebrow, javafx.scene.layout.Priority.ALWAYS);

        readerSubject.setWrapText(true);
        readerSubject.setStyle(
                "-fx-text-fill: #1e293b; -fx-font-size: 19px;"
                        + "-fx-font-weight: bold;"
        );
        readerSender.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        readerTick.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        HBox metadata = new HBox(14, readerSender, readerTick);
        metadata.setAlignment(Pos.CENTER_LEFT);

        readerBody.setWrapText(true);
        readerBody.setMaxWidth(Double.MAX_VALUE);
        readerBody.setStyle(
                "-fx-text-fill: #334155; -fx-font-size: 13px;"
                        + "-fx-line-spacing: 3px;"
        );

        VBox letter = new VBox(12, readerBody);
        letter.setPadding(new Insets(16, 0, 16, 0));
        ScrollPane bodyScroll = new ScrollPane(letter);
        bodyScroll.setFitToWidth(true);
        bodyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        bodyScroll.setStyle(
                "-fx-background: white; -fx-background-color: white;"
                        + "-fx-border-color: #e4e8ee;"
                        + "-fx-border-width: 1 0 0 0;"
        );
        VBox.setVgrow(bodyScroll, javafx.scene.layout.Priority.ALWAYS);

        VBox card = new VBox(12, top, readerSubject, metadata, bodyScroll);
        card.setPadding(new Insets(22, 26, 20, 26));
        card.setMinWidth(320);
        card.setPrefWidth(560);
        card.setMaxWidth(560);
        card.setPrefHeight(400);
        card.setMaxHeight(400);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12;"
                        + "-fx-border-color: #dce3eb; -fx-border-radius: 12;"
                        + "-fx-effect: dropshadow(gaussian,"
                        + " rgba(15,23,42,0.20), 22, 0, 0, 8);"
        );

        StackPane overlay = new StackPane(card);
        overlay.setStyle("-fx-background-color: rgba(15,23,42,0.23);");
        overlay.setVisible(false);
        return overlay;
    }

    /** Disegna un piccolo foglio piegato con il simbolo della posta. */
    private Button createCornerButton()
    {
        Rectangle paper = new Rectangle(36, 40);
        paper.setArcWidth(5);
        paper.setArcHeight(5);
        paper.setFill(Color.web("#f8fafc"));
        paper.setStroke(Color.web("#aebdce"));

        Label envelope = new Label("✉");
        envelope.setStyle("-fx-text-fill: #334155; -fx-font-size: 23px;");

        // L'angolo piegato richiama la linguetta di una pagina.
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

                // Alla riapertura la casella torna dolcemente nel pannello.
                view.setOpacity(0);
                FadeTransition reveal =
                        new FadeTransition(Duration.millis(220), view);
                reveal.setToValue(1);
                reveal.play();
            }
        });
        return button;
    }

    /** Sposta una copia visiva della casella fino all'icona nell'angolo. */
    private void foldIntoCorner()
    {
        if (view.getScene() == null)
        {
            docked = true;
            refresh();
            return;
        }

        // Il cue parte esattamente quando inizia il movimento verso l'angolo.
        soundManager.playMailDockSound();

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

    /** Ferma il movimento e rimuove la copia visiva eventualmente rimasta. */
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

    /** Rilascia le animazioni quando si lascia la schermata di gioco. */
    public void stop()
    {
        if (arrivalAnimation != null)
        {
            arrivalAnimation.stop();
            arrivalAnimation = null;
        }
        stopFoldingAnimation();
        readerOverlay.setVisible(false);
    }
}
