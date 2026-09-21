// La classe EventAnimationView gestisce le animazioni visive degli eventi speciali di gioco.
// Controlla Tsunami, Hacker Attack ed esplosioni nucleari, bloccando i comandi quando necessario.

package GUI;

import Events.EventType;
import controller.Controller;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.ExplosionInfo;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public final class EventAnimationView
{
    private static final int TSUNAMI_LINE_DELAY = 500;
    private static final int EXPLOSION_RING_DELAY = 120;

    private final Controller controller;
    private final GridView gridView;
    private final Button nextTurnButton;
    private final VBox hackerAttackPanel;
    private final Label hackerBinaryCode;
    private final Queue<ExplosionInfo> pendingExplosions = new ArrayDeque<>();

    private boolean tsunamiAnimationStarted;
    private boolean tsunamiAnimationRunning;
    private boolean hackerAttackAnimationStarted;
    private boolean explosionAnimationRunning;
    private Timeline tsunamiTimeline;
    private Timeline hackerCodeTimeline;
    private Timeline explosionTimeline;

    // Inizializza le dipendenze visive e costruisce il pannello grafico dell'attacco hacker.
    public EventAnimationView(
            Controller controller,
            GridView gridView,
            Button nextTurnButton)
    {
        if (controller == null
                || gridView == null
                || nextTurnButton == null)
        {
            throw new IllegalArgumentException(
                    "Animation dependencies cannot be null"
            );
        }

        this.controller = controller;
        this.gridView = gridView;
        this.nextTurnButton = nextTurnButton;

        Rectangle monitorScreen = new Rectangle(70, 42);
        monitorScreen.setArcWidth(8);
        monitorScreen.setArcHeight(8);
        monitorScreen.setFill(Color.web("#263238"));
        monitorScreen.setStroke(Color.BLACK);
        monitorScreen.setStrokeWidth(2);

        Label terminalSymbol = new Label(">_");
        terminalSymbol.setStyle(
                "-fx-text-fill: #00e676;"
                        + "-fx-font-family: monospace;"
                        + "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
        );

        StackPane monitor =
                new StackPane(monitorScreen, terminalSymbol);

        Rectangle monitorStand =
                new Rectangle(8, 8, Color.web("#263238"));

        Rectangle monitorBase =
                new Rectangle(30, 4, Color.web("#263238"));

        VBox hackerComputerIcon = new VBox(
                0,
                monitor,
                monitorStand,
                monitorBase
        );
        hackerComputerIcon.setAlignment(Pos.CENTER);

        Label hackerAttackText = new Label("Hacker Attack");
        hackerAttackText.setStyle(
                "-fx-text-fill: red;"
                        + "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
        );

        hackerBinaryCode = new Label("01001101 10110100");
        hackerBinaryCode.setStyle(
                "-fx-text-fill: #008a36;"
                        + "-fx-font-family: monospace;"
                        + "-fx-font-size: 12px;"
        );

        hackerAttackPanel = new VBox(
                4,
                hackerComputerIcon,
                hackerAttackText,
                hackerBinaryCode
        );
        hackerAttackPanel.setAlignment(Pos.CENTER);
        hackerAttackPanel.setStyle(
                "-fx-background-color: #e9eef5;"
                        + "-fx-border-color: red;"
                        + "-fx-border-width: 2px;"
                        + "-fx-padding: 12px;"
                        + "-fx-background-radius: 6px;"
                        + "-fx-border-radius: 6px;"
        );
        hackerAttackPanel.setMaxSize(
                Region.USE_PREF_SIZE,
                Region.USE_PREF_SIZE
        );
        hackerAttackPanel.setVisible(false);
        hackerAttackPanel.setMouseTransparent(true);
    }

    // Sincronizza tutte le animazioni con lo stato corrente del gioco.
    public void refresh()
    {
        updateTsunamiAnimation();
        updateHackerAttackAnimation();
        updateExplosionAnimation();
    }

    // Verifica se lo Tsunami è attivo e ne avvia o resetta l'animazione.
    private void updateTsunamiAnimation()
    {
        boolean tsunamiActive =
                controller.getActiveEventType()
                        == EventType.TSUNAMI;

        if (tsunamiActive && !tsunamiAnimationStarted)
        {
            tsunamiAnimationStarted = true;
            startTsunamiAnimation();
        }
        else if (!tsunamiActive)
        {
            tsunamiAnimationStarted = false;
        }
    }

    // Crea e avvia la Timeline per l'avanzamento sequenziale dello Tsunami.
    private void startTsunamiAnimation()
    {
        tsunamiAnimationRunning = true;
        nextTurnButton.setDisable(true);

        final String direction =
                controller.getActiveTsunamiDirection();

        int advancementLength =
                controller.getActiveTsunamiAdvancementLength();

        tsunamiTimeline = new Timeline();

        for (int step = 0;
             step < advancementLength;
             step++)
        {
            final int currentStep = step;

            KeyFrame frame = new KeyFrame(
                    Duration.millis(
                            (step + 1) * TSUNAMI_LINE_DELAY
                    ),
                    new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent event)
                        {
                            showTsunamiLine(
                                    direction,
                                    currentStep
                            );
                        }
                    }
            );

            tsunamiTimeline.getKeyFrames().add(frame);
        }

        KeyFrame resetFrame = new KeyFrame(
                Duration.millis(
                        (advancementLength + 2)
                                * TSUNAMI_LINE_DELAY
                ),
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        clearTsunamiAnimation();
                    }
                }
        );

        tsunamiTimeline.getKeyFrames().add(resetFrame);
        tsunamiTimeline.play();
    }

    // Evidenzia sulla griglia la riga o colonna raggiunta dallo Tsunami.
    private void showTsunamiLine(
            String direction,
            int step)
    {
        if ("UP".equals(direction))
        {
            gridView.showTsunamiRow(step);
        }
        else if ("DOWN".equals(direction))
        {
            int row =
                    controller.getNumberOfRows() - 1 - step;
            gridView.showTsunamiRow(row);
        }
        else if ("LEFT".equals(direction))
        {
            gridView.showTsunamiColumn(step);
        }
        else if ("RIGHT".equals(direction))
        {
            int column =
                    controller.getNumberOfColumns() - 1 - step;
            gridView.showTsunamiColumn(column);
        }
    }

    // Ripristina la griglia al termine dello Tsunami.
    private void clearTsunamiAnimation()
    {
        gridView.clearTsunami();
        tsunamiAnimationRunning = false;

        controller.startTsunamiReconstruction();

        gridView.refresh();

        if (!explosionAnimationRunning)
        {
            nextTurnButton.setDisable(false);
        }
    }

    // Gestisce la visibilità dell'animazione dell'attacco hacker.
    private void updateHackerAttackAnimation()
    {
        boolean hackerAttackActive =
                controller.getActiveEventType()
                        == EventType.HACKER_ATTACK;

        if (hackerAttackActive
                && !hackerAttackAnimationStarted)
        {
            hackerAttackAnimationStarted = true;
            startHackerAttackAnimation();
        }
        else if (!hackerAttackActive)
        {
            stopHackerCodeAnimation();
            hackerAttackAnimationStarted = false;
            hackerAttackPanel.setVisible(false);
            hackerAttackPanel.setOpacity(1.0);
        }
    }

    // Mostra il pannello dell'attacco hacker con un effetto glitch.
    private void startHackerAttackAnimation()
    {
        hackerAttackPanel.setVisible(true);
        hackerAttackPanel.setOpacity(0.0);

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                0.0
                        )
                ),
                new KeyFrame(
                        Duration.millis(120),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                1.0
                        )
                ),
                new KeyFrame(
                        Duration.millis(240),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                0.2
                        )
                ),
                new KeyFrame(
                        Duration.millis(360),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                1.0
                        )
                ),
                new KeyFrame(
                        Duration.millis(500),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                0.4
                        )
                ),
                new KeyFrame(
                        Duration.millis(650),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                1.0
                        )
                )
        );

        timeline.play();
        startHackerCodeAnimation();
    }

    // Avvia il codice binario animato dell'attacco hacker.
    private void startHackerCodeAnimation()
    {
        hackerCodeTimeline = new Timeline(
                new KeyFrame(
                        Duration.millis(180),
                        new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent event)
                            {
                                hackerBinaryCode.setText(
                                        generateBinaryCode()
                                );
                            }
                        }
                )
        );

        hackerCodeTimeline.setCycleCount(Timeline.INDEFINITE);
        hackerCodeTimeline.play();
    }

    // Ferma la Timeline del codice binario.
    private void stopHackerCodeAnimation()
    {
        if (hackerCodeTimeline != null)
        {
            hackerCodeTimeline.stop();
            hackerCodeTimeline = null;
        }
    }

    // Genera una stringa casuale di 16 cifre binarie.
    private String generateBinaryCode()
    {
        StringBuilder code = new StringBuilder();

        for (int index = 0; index < 16; index++)
        {
            if (index == 8)
            {
                code.append(" ");
            }

            if (Math.random() < 0.5)
            {
                code.append("0");
            }
            else
            {
                code.append("1");
            }
        }

        return code.toString();
    }

    // Legge le nuove esplosioni dal Controller e le mette in coda per l'animazione.
    private void updateExplosionAnimation()
    {
        List<ExplosionInfo> newExplosions =
                controller.consumeExplosions();

        for (ExplosionInfo explosion : newExplosions)
        {
            pendingExplosions.offer(explosion);
        }

        if (!explosionAnimationRunning
                && !pendingExplosions.isEmpty())
        {
            startNextExplosionAnimation();
        }
    }

    // Anima una singola esplosione espandendo progressivamente gli anelli dal centro verso l'esterno.
    private void startNextExplosionAnimation()
    {
        ExplosionInfo explosion =
                pendingExplosions.poll();

        if (explosion == null)
        {
            return;
        }

        explosionAnimationRunning = true;
        nextTurnButton.setDisable(true);
        gridView.clearExplosion();

        explosionTimeline = new Timeline();

        for (int radius = 0;
             radius <= explosion.getRadius();
             radius++)
        {
            final int currentRadius = radius;

            KeyFrame frame = new KeyFrame(
                    Duration.millis(
                            radius * EXPLOSION_RING_DELAY
                    ),
                    new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent event)
                        {
                            gridView.showExplosionRing(
                                    explosion.getRow(),
                                    explosion.getColumn(),
                                    currentRadius
                            );
                        }
                    }
            );

            explosionTimeline
                    .getKeyFrames()
                    .add(frame);
        }

        KeyFrame resetFrame = new KeyFrame(
                Duration.millis(
                        (explosion.getRadius() + 2)
                                * EXPLOSION_RING_DELAY
                ),
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        finishExplosionAnimation();
                    }
                }
        );

        explosionTimeline
                .getKeyFrames()
                .add(resetFrame);

        explosionTimeline.play();
    }

    // Pulisce l'esplosione appena conclusa e avvia quella successiva, se presente.
    private void finishExplosionAnimation()
    {
        gridView.clearExplosion();
        explosionAnimationRunning = false;
        explosionTimeline = null;

        if (!pendingExplosions.isEmpty())
        {
            startNextExplosionAnimation();
        }
        else if (!tsunamiAnimationRunning)
        {
            nextTurnButton.setDisable(false);
        }
    }

    // Restituisce true se l'animazione dello Tsunami è in corso.
    public boolean isTsunamiAnimationRunning()
    {
        return tsunamiAnimationRunning;
    }

    // Restituisce il pannello dell'attacco hacker.
    public VBox getHackerAttackPanel()
    {
        return hackerAttackPanel;
    }

    // Interrompe tutte le animazioni e ripristina lo stato grafico.
    public void stop()
    {
        if (tsunamiTimeline != null)
        {
            tsunamiTimeline.stop();
            tsunamiTimeline = null;
        }

        if (explosionTimeline != null)
        {
            explosionTimeline.stop();
            explosionTimeline = null;
        }

        stopHackerCodeAnimation();

        pendingExplosions.clear();
        gridView.clearExplosion();

        tsunamiAnimationRunning = false;
        tsunamiAnimationStarted = false;
        hackerAttackAnimationStarted = false;
        explosionAnimationRunning = false;

        nextTurnButton.setDisable(false);
        hackerAttackPanel.setVisible(false);
    }
}
