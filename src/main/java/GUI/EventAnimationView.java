// La classe EventAnimationView gestisce le animazioni visive degli eventi speciali di gioco.
// Controlla Tsunami, Hacker Attack, Missile Attack ed esplosioni nucleari, bloccando i comandi quando necessario.

package GUI;

import Events.EventType;
import audio.SoundManager;
import controller.Controller;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.ExplosionInfo;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public final class EventAnimationView
{
    private static final int TSUNAMI_LINE_DELAY = 500;
    private static final int EXPLOSION_RING_DELAY = 360;
    private static final int MISSILE_FLIGHT_DURATION = 1200;
    private static final int MISSILE_IMPACT_DELAY = 220;
    private static final int MISSILE_IMPACT_DURATION = 700;
    private static final int MISSILE_START_MARGIN = 80;
    private static final double MISSILE_SHIELD_INSET = 4.0;
    private static final double MISSILE_SHIELD_ROUNDNESS = 20.0;
    private static final int MISSILE_SHIELD_SEGMENTS = 128;
    private static final int MISSILE_DEFLECTION_DURATION = 650;
    private static final int MISSILE_SHIELD_VISIBLE_DURATION = 1100;

    private final Controller controller;
    private final GridView gridView;
    private final Button nextTurnButton;
    private final SoundManager soundManager;
    private final VBox hackerAttackPanel;
    private final Label hackerBinaryCode;
    private final Group missileNode;
    private final Group missileShieldNode;
    private Path missileShieldOuter;
    private Ellipse missileShieldHighlight;
    private final Random random = new Random();
    private final Queue<ExplosionInfo> pendingExplosions = new ArrayDeque<>();

    private boolean tsunamiAnimationStarted;
    private boolean tsunamiAnimationRunning;
    private boolean economicBoomSoundStarted;
    private boolean energyCrisisSoundStarted;
    private boolean hackerAttackAnimationStarted;
    private boolean missileAnimationStarted;
    private boolean missileAnimationRunning;
    private boolean missileIntercepted;
    private boolean fireSoundPlaying;
    private boolean explosionAnimationRunning;
    private int tsunamiCurrentStep = -1;
    private String tsunamiDirection;
    private int tsunamiAdvancementLength;
    private int missileTargetRow = -1;
    private int missileTargetColumn = -1;
    private Timeline tsunamiTimeline;
    private Timeline hackerCodeTimeline;
    private Timeline missileImpactTimeline;
    private Timeline missileShieldTimeline;
    private Timeline explosionTimeline;
    private PauseTransition nuclearAudioDuckTransition;
    private TranslateTransition missileTransition;
    private ParallelTransition missileDeflectionTransition;

    // Inizializza le dipendenze visive e costruisce i componenti grafici degli eventi.
    public EventAnimationView(
            Controller controller,
            GridView gridView,
            Button nextTurnButton,
            SoundManager soundManager)
    {
        if (controller == null
                || gridView == null
                || nextTurnButton == null
                || soundManager == null)
        {
            throw new IllegalArgumentException(
                    "Animation dependencies cannot be null"
            );
        }

        this.controller = controller;
        this.gridView = gridView;
        this.nextTurnButton = nextTurnButton;
        this.soundManager = soundManager;

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
        hackerAttackPanel.managedProperty()
                .bind(
                        hackerAttackPanel.visibleProperty()
                );
        hackerAttackPanel.setVisible(false);
        hackerAttackPanel.setMouseTransparent(true);

        missileShieldNode = createMissileShieldNode();
        missileShieldNode.setVisible(false);
        missileShieldNode.setManaged(false);
        missileShieldNode.setMouseTransparent(true);

        missileNode = createMissileNode();
        missileNode.setVisible(false);
        // L'animazione del missile usa translateX/translateY anche fuori dalla griglia.
        // Se il nodo resta "managed", StackPane lo considera nel layout e durante
        // il volo può cambiare le dimensioni del contenuto dello ScrollPane,
        // facendo comparire le scrollbar e spostando/riscalando visivamente la mappa.
        missileNode.setManaged(false);
        missileNode.setMouseTransparent(true);
    }

    public void refresh()
    {
        updateTsunamiAnimation();
        updateEconomicBoomSound();
        updateEnergyCrisisSound();
        updateHackerAttackAnimation();
        updateFireSound();
        updateMissileAnimation();
        updateExplosionAnimation();
    }

    private void updateFireSound()
    {
        boolean fireActive =
                controller.getActiveEventType()
                        == EventType.FIRE;

        if (fireActive
                && !fireSoundPlaying)
        {
            soundManager.playFireSound();
            fireSoundPlaying = true;
        }
        else if (!fireActive
                && fireSoundPlaying)
        {
            soundManager.stopFireSound();
            fireSoundPlaying = false;
        }
    }

    private Group createMissileShieldNode()
    {
        missileShieldOuter = new Path();
        missileShieldHighlight = new Ellipse();

        missileShieldOuter.setFill(
                new RadialGradient(
                        -35,
                        0.30,
                        0.34,
                        0.20,
                        1.0,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(
                                0.0,
                                Color.rgb(
                                        255,
                                        255,
                                        225,
                                        0.44
                                )
                        ),
                        new Stop(
                                0.36,
                                Color.rgb(
                                        255,
                                        234,
                                        105,
                                        0.26
                                )
                        ),
                        new Stop(
                                0.72,
                                Color.rgb(
                                        255,
                                        207,
                                        35,
                                        0.14
                                )
                        ),
                        new Stop(
                                1.0,
                                Color.rgb(
                                        255,
                                        188,
                                        0,
                                        0.025
                                )
                        )
                )
        );

        missileShieldOuter.setStroke(
                Color.rgb(
                        255,
                        215,
                        0,
                        0.92
                )
        );
        missileShieldOuter.setStrokeWidth(4);

        missileShieldOuter.setEffect(
                new DropShadow(
                        12,
                        Color.rgb(
                                255,
                                210,
                                25,
                                0.62
                        )
                )
        );

        missileShieldHighlight.setFill(
                new RadialGradient(
                        0,
                        0,
                        0.5,
                        0.5,
                        0.5,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(
                                0.0,
                                Color.rgb(
                                        255,
                                        255,
                                        250,
                                        0.26
                                )
                        ),
                        new Stop(
                                1.0,
                                Color.TRANSPARENT
                        )
                )
        );
        missileShieldHighlight.setStroke(
                Color.TRANSPARENT
        );
        missileShieldHighlight.setRotate(-12);

        updateMissileShieldGeometry();

        Group shield = new Group(
                missileShieldOuter,
                missileShieldHighlight
        );

        double width = gridView.getGridVisualWidth();
        double height = gridView.getGridVisualHeight();
        shield.setClip(
                new Rectangle(
                        -width / 2.0,
                        -height / 2.0,
                        width,
                        height
                )
        );
        return shield;
    }

    /*
     * Una superellisse segue la mappa e arrotonda solo gli angoli.
     * I centri delle celle agli angoli restano protetti; il bordo luminoso
     * occupa quasi soltanto lo spazio della griglia.
     */
    private double[] getMissileShieldRadii()
    {
        double halfWidth =
                gridView.getGridVisualWidth() / 2.0;

        double halfHeight =
                gridView.getGridVisualHeight() / 2.0;

        return new double[] {
                halfWidth - MISSILE_SHIELD_INSET,
                halfHeight - MISSILE_SHIELD_INSET
        };
    }

    private void updateShieldOutline(
            Path outline,
            double radiusX,
            double radiusY,
            double centerY)
    {
        outline.getElements().clear();

        for (int step = 0;
             step < MISSILE_SHIELD_SEGMENTS;
             step++)
        {
            double angle =
                    2.0 * Math.PI * step
                            / MISSILE_SHIELD_SEGMENTS;

            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            double exponent =
                    2.0 / MISSILE_SHIELD_ROUNDNESS;

            double x = radiusX
                    * Math.copySign(
                            Math.pow(Math.abs(cosine), exponent),
                            cosine
                    );
            double y = centerY + radiusY
                    * Math.copySign(
                            Math.pow(Math.abs(sine), exponent),
                            sine
                    );

            if (step == 0)
            {
                outline.getElements().add(new MoveTo(x, y));
            }
            else
            {
                outline.getElements().add(new LineTo(x, y));
            }
        }

        outline.getElements().add(new ClosePath());
    }

    private double shieldEquation(
            double x,
            double y,
            double radiusX,
            double radiusY)
    {
        return Math.pow(
                Math.abs(x / radiusX),
                MISSILE_SHIELD_ROUNDNESS
        ) + Math.pow(
                Math.abs(y / radiusY),
                MISSILE_SHIELD_ROUNDNESS
        );
    }

    private void updateMissileShieldGeometry()
    {
        double[] radii =
                getMissileShieldRadii();

        double radiusX = radii[0];
        double radiusY = radii[1];

        updateShieldOutline(
                missileShieldOuter,
                radiusX,
                radiusY,
                0
        );

        missileShieldHighlight.setRadiusX(
                radiusX * 0.34
        );
        missileShieldHighlight.setRadiusY(
                radiusY * 0.13
        );
        missileShieldHighlight.setCenterX(
                -radiusX * 0.18
        );
        missileShieldHighlight.setCenterY(
                -radiusY * 0.32
        );
    }

    // Aggiorna il ritaglio dello scudo quando la griglia cambia dimensione.
    private void updateMissileShieldClip()
    {
        if (missileShieldNode == null || !(missileShieldNode.getClip() instanceof Rectangle))
        {
            return;
        }

        Rectangle clip = (Rectangle) missileShieldNode.getClip();
        double width = gridView.getGridVisualWidth();
        double height = gridView.getGridVisualHeight();

        clip.setX(-width / 2.0);
        clip.setY(-height / 2.0);
        clip.setWidth(width);
        clip.setHeight(height);
    }

    private Group createMissileNode()
    {
        Rectangle body =
                new Rectangle(
                        -18,
                        -5,
                        32,
                        10
                );
        body.setArcWidth(8);
        body.setArcHeight(8);
        body.setFill(Color.LIGHTGRAY);
        body.setStroke(Color.DARKSLATEGRAY);

        Polygon nose =
                new Polygon(
                        14.0, -5.0,
                        25.0, 0.0,
                        14.0, 5.0
                );
        nose.setFill(Color.DARKRED);

        Polygon upperFin =
                new Polygon(
                        -9.0, -5.0,
                        -17.0, -13.0,
                        0.0, -5.0
                );
        upperFin.setFill(Color.DARKRED);

        Polygon lowerFin =
                new Polygon(
                        -9.0, 5.0,
                        -17.0, 13.0,
                        0.0, 5.0
                );
        lowerFin.setFill(Color.DARKRED);

        Polygon flame =
                new Polygon(
                        -18.0, -3.5,
                        -29.0, 0.0,
                        -18.0, 3.5
                );
        flame.setFill(Color.ORANGE);

        return new Group(
                flame,
                upperFin,
                lowerFin,
                body,
                nose
        );
    }

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

    private void updateEconomicBoomSound()
    {
        boolean economicBoomActive =
                controller.getActiveEventType()
                        == EventType.ECONOMIC_BOOM;

        if (economicBoomActive && !economicBoomSoundStarted)
        {
            economicBoomSoundStarted = true;
            soundManager.playEconomicBoomSound();
        }
        else if (!economicBoomActive)
        {
            economicBoomSoundStarted = false;
        }
    }

    private void updateEnergyCrisisSound()
    {
        boolean energyCrisisActive =
                controller.getActiveEventType()
                        == EventType.ENERGY_CRISIS;

        if (energyCrisisActive && !energyCrisisSoundStarted)
        {
            energyCrisisSoundStarted = true;
            soundManager.playEnergyCrisisSound();
        }
        else if (!energyCrisisActive)
        {
            energyCrisisSoundStarted = false;
        }
    }

    private void startTsunamiAnimation()
    {
        tsunamiAnimationRunning = true;
        nextTurnButton.setDisable(true);
        soundManager.playTsunamiSound();

        tsunamiDirection =
                controller.getActiveTsunamiDirection();

        tsunamiAdvancementLength =
                controller.getActiveTsunamiAdvancementLength();

        tsunamiCurrentStep = -1;

        tsunamiTimeline = new Timeline();

        for (int step = 0;
             step < tsunamiAdvancementLength;
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
                                    tsunamiDirection,
                                    currentStep
                            );

                            tsunamiCurrentStep = currentStep;
                            startWaitingExplosionIfReady();
                        }
                    }
            );

            tsunamiTimeline.getKeyFrames().add(frame);
        }

        KeyFrame passedLastLineFrame = new KeyFrame(
                Duration.millis(
                        (tsunamiAdvancementLength + 1)
                                * TSUNAMI_LINE_DELAY
                ),
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        tsunamiCurrentStep =
                                tsunamiAdvancementLength;
                        startWaitingExplosionIfReady();
                    }
                }
        );

        tsunamiTimeline.getKeyFrames().add(
                passedLastLineFrame
        );

        KeyFrame resetFrame = new KeyFrame(
                Duration.millis(
                        (tsunamiAdvancementLength + 2)
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

    private void clearTsunamiAnimation()
    {
        gridView.clearTsunami();
        tsunamiAnimationRunning = false;
        tsunamiCurrentStep = -1;
        tsunamiDirection = null;
        tsunamiAdvancementLength = 0;

        controller.startTsunamiReconstruction();

        startWaitingExplosionIfReady();

        if (!explosionAnimationRunning
                && !missileAnimationRunning)
        {
            gridView.refresh();
            nextTurnButton.setDisable(false);
        }
    }

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

    private void startHackerAttackAnimation()
    {
        soundManager.playHackerAttackSound();
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

    private void stopHackerCodeAnimation()
    {
        if (hackerCodeTimeline != null)
        {
            hackerCodeTimeline.stop();
            hackerCodeTimeline = null;
        }
    }

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

    private void updateMissileAnimation()
    {
        boolean missileActive =
                controller.getActiveEventType()
                        == EventType.MISSILE_ATTACK;

        if (missileActive
                && !missileAnimationStarted)
        {
            missileAnimationStarted = true;
            startMissileAnimation();
        }
        else if (!missileActive
                && !missileAnimationRunning)
        {
            missileAnimationStarted = false;
        }
    }

    private void startMissileAnimation()
    {
        missileTargetRow =
                controller.getActiveMissileTargetRow();

        missileTargetColumn =
                controller.getActiveMissileTargetColumn();

        if (missileTargetRow < 0
                || missileTargetColumn < 0)
        {
            missileAnimationStarted = false;
            return;
        }

        missileIntercepted =
                controller.isActiveMissileIntercepted();

        missileAnimationRunning = true;
        nextTurnButton.setDisable(true);

        double targetX =
                gridView.getCellCenterOffsetX(
                        missileTargetColumn
                );

        double targetY =
                gridView.getCellCenterOffsetY(
                        missileTargetRow
                );

        double[] startPosition =
                getRandomMissileStartPosition(
                        targetX,
                        targetY
                );

        double startX = startPosition[0];
        double startY = startPosition[1];

        double endX = targetX;
        double endY = targetY;

        if (missileIntercepted)
        {
            double[] interceptionPoint =
                    getShieldIntersection(
                            startX,
                            startY,
                            targetX,
                            targetY
                    );

            endX = interceptionPoint[0];
            endY = interceptionPoint[1];
        }

        double angle =
                Math.toDegrees(
                        Math.atan2(
                                endY - startY,
                                endX - startX
                        )
                );

        missileNode.setOpacity(1.0);
        missileNode.setRotate(angle);
        missileNode.setVisible(true);

        missileTransition =
                new TranslateTransition(
                        Duration.millis(
                                MISSILE_FLIGHT_DURATION
                        ),
                        missileNode
                );

        missileTransition.setFromX(startX);
        missileTransition.setFromY(startY);
        missileTransition.setToX(endX);
        missileTransition.setToY(endY);

        final double missileStartX = startX;
        final double missileStartY = startY;
        final double missileEndX = endX;
        final double missileEndY = endY;

        missileTransition.setOnFinished(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        missileTransition = null;

                        if (missileIntercepted)
                        {
                            startMissileDeflectionAnimation(
                                    missileStartX,
                                    missileStartY,
                                    missileEndX,
                                    missileEndY
                            );
                        }
                        else
                        {
                            missileNode.setVisible(false);
                            startMissileImpactAnimation();
                        }
                    }
                }
        );

        soundManager.playMissileSound();
        missileTransition.play();
    }

    private double[] getRandomMissileStartPosition(
            double targetX,
            double targetY)
    {
        double[] radii =
                getMissileShieldRadii();

        double radiusX = radii[0];
        double radiusY = radii[1];

        int startSide = random.nextInt(6);

        if (startSide == 0)
        {
            return new double[] {
                    -radiusX - MISSILE_START_MARGIN,
                    -radiusY - MISSILE_START_MARGIN
            };
        }

        if (startSide == 1)
        {
            return new double[] {
                    radiusX + MISSILE_START_MARGIN,
                    -radiusY - MISSILE_START_MARGIN
            };
        }

        if (startSide == 2)
        {
            return new double[] {
                    -radiusX - MISSILE_START_MARGIN,
                    targetY
            };
        }

        if (startSide == 3)
        {
            return new double[] {
                    radiusX + MISSILE_START_MARGIN,
                    targetY
            };
        }

        if (startSide == 4)
        {
            return new double[] {
                    -radiusX - MISSILE_START_MARGIN,
                    radiusY + MISSILE_START_MARGIN
            };
        }

        return new double[] {
                radiusX + MISSILE_START_MARGIN,
                radiusY + MISSILE_START_MARGIN
        };
    }

    // Trova il bordo della stessa superellisse disegnata sulla griglia.
    private double[] getShieldIntersection(
            double startX,
            double startY,
            double targetX,
            double targetY)
    {
        double[] radii =
                getMissileShieldRadii();

        double radiusX = radii[0];
        double radiusY = radii[1];

        double dx = targetX - startX;
        double dy = targetY - startY;

        double outside = 0.0;
        double inside = 1.0;

        for (int iteration = 0; iteration < 40; iteration++)
        {
            double middle = (outside + inside) / 2.0;
            double x = startX + dx * middle;
            double y = startY + dy * middle;

            if (shieldEquation(x, y, radiusX, radiusY) > 1.0)
            {
                outside = middle;
            }
            else
            {
                inside = middle;
            }
        }

        return new double[] {
                startX + dx * inside,
                startY + dy * inside
        };
    }

    private void startMissileDeflectionAnimation(
            double startX,
            double startY,
            double impactX,
            double impactY)
    {
        soundManager.playMissileShieldImpactSound();
        showMissileShieldImpact();

        double incomingX =
                impactX - startX;

        double incomingY =
                impactY - startY;

        double[] radii =
                getMissileShieldRadii();

        double radiusX = radii[0];
        double radiusY = radii[1];

        double normalX =
                Math.copySign(
                        Math.pow(
                                Math.abs(impactX / radiusX),
                                MISSILE_SHIELD_ROUNDNESS - 1.0
                        ),
                        impactX
                ) / radiusX;

        double normalY =
                Math.copySign(
                        Math.pow(
                                Math.abs(impactY / radiusY),
                                MISSILE_SHIELD_ROUNDNESS - 1.0
                        ),
                        impactY
                ) / radiusY;

        double normalLength =
                Math.sqrt(
                        normalX * normalX
                                + normalY * normalY
                );

        if (normalLength == 0)
        {
            normalLength = 1;
        }

        normalX /= normalLength;
        normalY /= normalLength;

        double dot =
                incomingX * normalX
                        + incomingY * normalY;

        double reflectedX =
                incomingX
                        - 2.0 * dot * normalX;

        double reflectedY =
                incomingY
                        - 2.0 * dot * normalY;

        double reflectedLength =
                Math.sqrt(
                        reflectedX * reflectedX
                                + reflectedY * reflectedY
                );

        if (reflectedLength == 0)
        {
            reflectedLength = 1;
        }

        reflectedX /= reflectedLength;
        reflectedY /= reflectedLength;

        double bounceDistance =
                Math.max(
                        140,
                        Math.min(
                                radiusX,
                                radiusY
                        ) * 0.35
                );

        double downwardFall =
                Math.max(
                        55,
                        radiusY * 0.10
                );

        double bounceX =
                impactX
                        + reflectedX
                        * bounceDistance;

        double bounceY =
                impactY
                        + reflectedY
                        * bounceDistance
                        + downwardFall;

        missileNode.setRotate(
                Math.toDegrees(
                        Math.atan2(
                                bounceY - impactY,
                                bounceX - impactX
                        )
                )
        );

        TranslateTransition bounce =
                new TranslateTransition(
                        Duration.millis(
                                MISSILE_DEFLECTION_DURATION
                        ),
                        missileNode
                );

        bounce.setFromX(impactX);
        bounce.setFromY(impactY);
        bounce.setToX(bounceX);
        bounce.setToY(bounceY);

        FadeTransition fade =
                new FadeTransition(
                        Duration.millis(
                                MISSILE_DEFLECTION_DURATION
                        ),
                        missileNode
                );

        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        RotateTransition spin =
                new RotateTransition(
                        Duration.millis(
                                MISSILE_DEFLECTION_DURATION
                        ),
                        missileNode
                );

        spin.setByAngle(150);

        missileDeflectionTransition =
                new ParallelTransition(
                        bounce,
                        fade,
                        spin
                );

        missileDeflectionTransition.setOnFinished(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        missileDeflectionTransition = null;
                        missileNode.setVisible(false);
                        missileNode.setOpacity(1.0);
                        finishMissileAnimation();
                    }
                }
        );

        missileDeflectionTransition.play();
    }

    private void showMissileShieldImpact()
    {
        updateMissileShieldGeometry();
        updateMissileShieldClip();

        if (missileShieldTimeline != null)
        {
            missileShieldTimeline.stop();
        }

        missileShieldNode.setVisible(true);
        missileShieldNode.setOpacity(0.0);

        missileShieldTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.ZERO,
                                new KeyValue(
                                        missileShieldNode
                                                .opacityProperty(),
                                        0.0
                                )
                        ),
                        new KeyFrame(
                                Duration.millis(90),
                                new KeyValue(
                                        missileShieldNode
                                                .opacityProperty(),
                                        0.86
                                )
                        ),
                        new KeyFrame(
                                Duration.millis(310),
                                new KeyValue(
                                        missileShieldNode
                                                .opacityProperty(),
                                        0.48
                                )
                        ),
                        new KeyFrame(
                                Duration.millis(
                                        MISSILE_SHIELD_VISIBLE_DURATION
                                ),
                                new KeyValue(
                                        missileShieldNode
                                                .opacityProperty(),
                                        0.0
                                )
                        )
                );

        missileShieldTimeline.setOnFinished(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        missileShieldNode.setVisible(false);
                        missileShieldTimeline = null;
                    }
                }
        );

        missileShieldTimeline.play();
    }

    private void startMissileImpactAnimation()
    {
        soundManager.playMissileGridImpactSound();
        gridView.clearExplosion();

        missileImpactTimeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent event)
                            {
                                gridView.showExplosionRing(
                                        missileTargetRow,
                                        missileTargetColumn,
                                        0,
                                        1
                                );
                            }
                        }
                ),
                new KeyFrame(
                        Duration.millis(
                                MISSILE_IMPACT_DELAY
                        ),
                        new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent event)
                            {
                                gridView.showExplosionRing(
                                        missileTargetRow,
                                        missileTargetColumn,
                                        1,
                                        1
                                );
                            }
                        }
                ),
                new KeyFrame(
                        Duration.millis(
                                MISSILE_IMPACT_DURATION
                        ),
                        new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent event)
                            {
                                finishMissileAnimation();
                            }
                        }
                )
        );

        missileImpactTimeline.play();
    }

    private void finishMissileAnimation()
    {
        gridView.clearExplosion();
        missileImpactTimeline = null;
        missileAnimationRunning = false;
        missileIntercepted = false;
        missileTargetRow = -1;
        missileTargetColumn = -1;

        startWaitingExplosionIfReady();

        if (!explosionAnimationRunning
                && !tsunamiAnimationRunning)
        {
            gridView.refresh();
            nextTurnButton.setDisable(false);
        }
    }

    private void updateExplosionAnimation()
    {
        List<ExplosionInfo> newExplosions =
                controller.consumeExplosions();

        for (ExplosionInfo explosion : newExplosions)
        {
            pendingExplosions.offer(explosion);
        }

        startWaitingExplosionIfReady();
    }

    private void startWaitingExplosionIfReady()
    {
        if (explosionAnimationRunning
                || missileAnimationRunning
                || pendingExplosions.isEmpty())
        {
            return;
        }

        if (!tsunamiAnimationRunning)
        {
            startNextExplosionAnimation();
            return;
        }

        ExplosionInfo readyExplosion = null;

        for (ExplosionInfo explosion
                : pendingExplosions)
        {
            int requiredStep =
                    getExplosionStartStep(explosion);

            if (requiredStep < 0
                    || tsunamiCurrentStep
                    >= requiredStep)
            {
                readyExplosion = explosion;
                break;
            }
        }

        if (readyExplosion == null)
        {
            return;
        }

        pendingExplosions.remove(readyExplosion);
        startExplosionAnimation(readyExplosion);
    }

    private int getExplosionStartStep(
            ExplosionInfo explosion)
    {
        int plantStep;

        if ("UP".equals(tsunamiDirection))
        {
            plantStep = explosion.getRow();
        }
        else if ("DOWN".equals(tsunamiDirection))
        {
            plantStep =
                    controller.getNumberOfRows()
                            - 1
                            - explosion.getRow();
        }
        else if ("LEFT".equals(tsunamiDirection))
        {
            plantStep = explosion.getColumn();
        }
        else if ("RIGHT".equals(tsunamiDirection))
        {
            plantStep =
                    controller.getNumberOfColumns()
                            - 1
                            - explosion.getColumn();
        }
        else
        {
            return -1;
        }

        if (plantStep < 0
                || plantStep >= tsunamiAdvancementLength)
        {
            return -1;
        }

        return plantStep + 1;
    }

    private void startNextExplosionAnimation()
    {
        ExplosionInfo explosion =
                pendingExplosions.poll();

        if (explosion == null)
        {
            return;
        }

        startExplosionAnimation(explosion);
    }

    private void startExplosionAnimation(
            ExplosionInfo explosion)
    {
        explosionAnimationRunning = true;
        nextTurnButton.setDisable(true);
        gridView.clearExplosion();

        if (explosion.isNuclear())
        {
            startNuclearExplosionAudio();
        }

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
                                    currentRadius,
                                    explosion.getRadius()
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

    private void startNuclearExplosionAudio()
    {
        if (nuclearAudioDuckTransition != null)
        {
            nuclearAudioDuckTransition.stop();
        }

        soundManager
                .duckLongEventSoundsForNuclearExplosion();

        soundManager.playNuclearExplosionSound();

        nuclearAudioDuckTransition =
                new PauseTransition(
                        Duration.millis(1800)
                );

        nuclearAudioDuckTransition.setOnFinished(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        soundManager
                                .restoreLongEventSoundsAfterNuclearExplosion();

                        nuclearAudioDuckTransition = null;
                    }
                }
        );

        nuclearAudioDuckTransition.play();
    }

    private void finishExplosionAnimation()
    {
        gridView.clearExplosion();
        explosionAnimationRunning = false;
        explosionTimeline = null;

        if (!pendingExplosions.isEmpty())
        {
            startWaitingExplosionIfReady();
        }

        if (!explosionAnimationRunning)
        {
            gridView.refresh();

            if (!tsunamiAnimationRunning
                    && !missileAnimationRunning)
            {
                nextTurnButton.setDisable(false);
            }
        }
    }

    public boolean isTsunamiAnimationRunning()
    {
        return tsunamiAnimationRunning;
    }

    public boolean isMissileAnimationRunning()
    {
        return missileAnimationRunning;
    }

    public boolean isExplosionAnimationRunning()
    {
        return explosionAnimationRunning;
    }

    public VBox getHackerAttackPanel()
    {
        return hackerAttackPanel;
    }

    public Group getMissileNode()
    {
        return missileNode;
    }

    public Group getMissileShieldNode()
    {
        return missileShieldNode;
    }

    public void stop()
    {
        if (tsunamiTimeline != null)
        {
            tsunamiTimeline.stop();
            tsunamiTimeline = null;
        }

        if (missileTransition != null)
        {
            missileTransition.stop();
            missileTransition = null;
        }

        if (missileImpactTimeline != null)
        {
            missileImpactTimeline.stop();
            missileImpactTimeline = null;
        }

        if (missileShieldTimeline != null)
        {
            missileShieldTimeline.stop();
            missileShieldTimeline = null;
        }

        if (missileDeflectionTransition != null)
        {
            missileDeflectionTransition.stop();
            missileDeflectionTransition = null;
        }

        if (explosionTimeline != null)
        {
            explosionTimeline.stop();
            explosionTimeline = null;
        }

        if (nuclearAudioDuckTransition != null)
        {
            nuclearAudioDuckTransition.stop();
            nuclearAudioDuckTransition = null;
        }

        soundManager
                .restoreLongEventSoundsAfterNuclearExplosion();

        stopHackerCodeAnimation();

        soundManager.stopTsunamiSound();
        soundManager.stopFireSound();
        fireSoundPlaying = false;

        pendingExplosions.clear();
        gridView.clearExplosion();

        tsunamiAnimationRunning = false;
        tsunamiAnimationStarted = false;
        economicBoomSoundStarted = false;
        hackerAttackAnimationStarted = false;
        missileAnimationRunning = false;
        missileAnimationStarted = false;
        missileIntercepted = false;
        explosionAnimationRunning = false;
        tsunamiCurrentStep = -1;
        tsunamiDirection = null;
        tsunamiAdvancementLength = 0;
        missileTargetRow = -1;
        missileTargetColumn = -1;

        missileNode.setVisible(false);
        missileNode.setOpacity(1.0);
        missileNode.setTranslateX(0);
        missileNode.setTranslateY(0);

        missileShieldNode.setVisible(false);
        missileShieldNode.setOpacity(0.0);

        nextTurnButton.setDisable(false);
        hackerAttackPanel.setVisible(false);
    }
}
