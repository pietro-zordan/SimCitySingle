package GUI;

import audio.SoundManager;
import controller.Controller;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;
import model.ConstructionType;
import model.FreemasonryChoice;

import java.util.ArrayList;
import java.util.List;

/** Oscura il gioco e mostra il passaggio dell'UFO sopra la diagonale della città. */
public final class UfoEncounterView
{
    private static final int ENCOUNTER_TICK = 510;
    private static final double FLIGHT_MILLIS = 3900;
    private static final double DARKEN_MILLIS = 1750;
    private static final double LIGHTEN_MILLIS = 850;

    private final Controller controller;
    private final GridView gridView;
    private final MapViewport mapViewport;
    private final SoundManager soundManager;
    private final Pane overlay = new Pane();
    private final Canvas canvas = new Canvas();
    private final List<int[]> diagonalBuildings = new ArrayList<>();
    private PauseTransition flightDelay;
    private FadeTransition fadeIn;
    private FadeTransition fadeOut;
    private AnimationTimer flight;
    private boolean played;

    /** Prepara uno strato nero che non modifica l'aspetto normale della griglia. */
    public UfoEncounterView(
            Controller controller,
            GridView gridView,
            MapViewport mapViewport,
            SoundManager soundManager)
    {
        if (controller == null || gridView == null
                || mapViewport == null || soundManager == null)
        {
            throw new IllegalArgumentException(
                    "UFO encounter dependencies cannot be null"
            );
        }

        this.controller = controller;
        this.gridView = gridView;
        this.mapViewport = mapViewport;
        this.soundManager = soundManager;
        canvas.widthProperty().bind(overlay.widthProperty());
        canvas.heightProperty().bind(overlay.heightProperty());
        overlay.getChildren().add(canvas);
        overlay.setMinSize(0, 0);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setVisible(false);
        overlay.setFocusTraversable(true);
    }

    /** Restituisce lo strato da porre sopra l'intera schermata di gioco. */
    public Pane getView()
    {
        return overlay;
    }

    /** Avvia l'incontro una sola volta, soltanto dopo aver accettato la loggia. */
    public void playIfNeeded()
    {
        if (played || controller.getCurrentTick() != ENCOUNTER_TICK
                || controller.getFreemasonryChoice()
                != FreemasonryChoice.ACCEPTED
                || controller.isGameOver())
        {
            return;
        }

        played = true;
        overlay.setOpacity(0);
        overlay.setVisible(true);
        overlay.requestFocus();
        paintDarkness();
        soundManager.playUfoPowerdownSound();

        fadeIn = new FadeTransition(Duration.millis(DARKEN_MILLIS), overlay);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_BOTH);
        fadeIn.play();

        boolean expanded = controller.getNumberOfRows() > 20
                || controller.getNumberOfColumns() > 20;
        if (expanded)
        {
            // La griglia espansa si adatta durante il buio: l'intera
            // diagonale resta in vista quando l'UFO inizia a muoversi.
            mapViewport.animateToFit();
        }

        // Il disco entra solo quando il buio è completo, anche se la
        // griglia allargata sta terminando la propria centratura.
        flightDelay = new PauseTransition(Duration.millis(1950));
        flightDelay.setOnFinished(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                startFlight();
            }
        });
        flightDelay.play();
    }

    /** Individua gli edifici sulla diagonale, escludendo strade e celle vuote. */
    private void findDiagonalBuildings()
    {
        diagonalBuildings.clear();
        int rows = controller.getNumberOfRows();
        int columns = controller.getNumberOfColumns();

        for (int row = 0; row < rows; row++)
        {
            for (int column = 0; column < columns; column++)
            {
                double verticalPosition = (double) row / (rows - 1);
                double horizontalPosition = (double) column / (columns - 1);
                Controller.CellState cell =
                        controller.getCellState(row, column);

                if (Math.abs(verticalPosition - horizontalPosition)
                        <= 0.55 / Math.max(rows, columns)
                        && !cell.empty()
                        && cell.type() != ConstructionType.ROAD
                        && cell.type() != ConstructionType.GRASS)
                {
                    diagonalBuildings.add(new int[]{row, column});
                }
            }
        }
    }

    /** Muove il fascio dal primo all'ultimo angolo in circa quattro secondi. */
    private void startFlight()
    {
        if (!overlay.isVisible())
        {
            return;
        }

        findDiagonalBuildings();
        soundManager.playUfoHowlSound();
        flight = new AnimationTimer()
        {
            private long firstFrame;

            @Override
            public void handle(long now)
            {
                if (firstFrame == 0)
                {
                    firstFrame = now;
                }

                double elapsed = (now - firstFrame) / 1_000_000.0;
                double progress = Math.min(1, elapsed / FLIGHT_MILLIS);
                // Parte e si ferma con dolcezza; al centro attraversa veloce.
                double movement = progress * progress * (3 - 2 * progress);

                Point2D start = cellCenter(0, 0);
                Point2D finish = cellCenter(
                        controller.getNumberOfRows() - 1,
                        controller.getNumberOfColumns() - 1
                );

                double x = start.getX()
                        + (finish.getX() - start.getX()) * movement;
                double y = start.getY()
                        + (finish.getY() - start.getY()) * movement;

                paintFrame(x, y, elapsed);

                if (progress >= 1)
                {
                    flight.stop();
                    flight = null;
                    fadeOut = new FadeTransition(
                            Duration.millis(LIGHTEN_MILLIS), overlay);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setInterpolator(Interpolator.EASE_BOTH);
                    fadeOut.setOnFinished(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent event)
                        {
                            overlay.setVisible(false);
                            overlay.setOpacity(1);
                            fadeOut = null;
                        }
                    });
                    fadeOut.play();
                }
            }
        };
        flight.start();
    }

    /** Converte la posizione di una cella nella superficie dell'animazione. */
    private Point2D cellCenter(int row, int column)
    {
        return canvas.sceneToLocal(
                gridView.getCellCenterInScene(row, column)
        );
    }

    /** Disegna la schermata nera prima che compaia la luce verde. */
    private void paintDarkness()
    {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(Color.BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /** Lascia vedere la città sotto il fascio e disegna il disco luminoso. */
    private void paintFrame(double x, double y, double elapsed)
    {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        graphics.clearRect(0, 0, width, height);
        paintDarkness();

        Point2D first = cellCenter(0, 0);
        Point2D next = cellCenter(0, 1);
        Point2D last = cellCenter(
                controller.getNumberOfRows() - 1,
                controller.getNumberOfColumns() - 1
        );
        double cellSize = Math.max(14,
                Math.abs(next.getX() - first.getX()));
        double radiusX = Math.max(62, cellSize * 3.2);
        double radiusY = Math.max(57, cellSize * 2.7);
        double beamY = y + cellSize * 0.45;

        // Un alone tenue ammorbidisce il confine fra il fascio e il buio.
        graphics.setFill(new RadialGradient(
                0, 0, x, beamY, radiusX * 1.35, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(37, 180, 85, 0.22)),
                new Stop(1, Color.TRANSPARENT)
        ));
        graphics.fillOval(x - radiusX * 1.35,
                beamY - radiusY * 1.35,
                radiusX * 2.7, radiusY * 2.7);

        // Il fascio lascia vedere soltanto le vere celle della città:
        // non scopre i pannelli che circondano la griglia.
        graphics.save();
        graphics.beginPath();
        graphics.rect(first.getX() - cellSize / 2,
                first.getY() - cellSize / 2,
                last.getX() - first.getX() + cellSize,
                last.getY() - first.getY() + cellSize);
        graphics.clip();
        graphics.beginPath();
        graphics.arc(x, beamY, radiusX, radiusY, 0, 360);
        graphics.closePath();
        graphics.clip();
        graphics.clearRect(x - radiusX, beamY - radiusY,
                radiusX * 2, radiusY * 2);
        graphics.setFill(new RadialGradient(
                0, 0, x, beamY, radiusX, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(73, 255, 131, 0.28)),
                new Stop(1, Color.rgb(30, 150, 66, 0.05))
        ));
        graphics.fillOval(x - radiusX, beamY - radiusY,
                radiusX * 2, radiusY * 2);
        graphics.setFill(new RadialGradient(
                0, 0, x, beamY, radiusX, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.72, Color.rgb(0, 0, 0, 0.08)),
                new Stop(1, Color.rgb(0, 0, 0, 0.96))
        ));
        graphics.fillOval(x - radiusX, beamY - radiusY,
                radiusX * 2, radiusY * 2);
        graphics.restore();

        // Gli edifici sulla diagonale ricevono un riflesso verde distinto.
        for (int[] position : diagonalBuildings)
        {
            Point2D building = cellCenter(position[0], position[1]);
            double dx = (building.getX() - x) / radiusX;
            double dy = (building.getY() - beamY) / radiusY;
            if (dx * dx + dy * dy <= 0.85)
            {
                double size = cellSize * 0.78;
                graphics.setFill(Color.rgb(99, 255, 148, 0.17));
                graphics.fillRoundRect(building.getX() - size / 2,
                        building.getY() - size / 2,
                        size, size, 5, 5);
                graphics.setStroke(Color.rgb(145, 255, 172, 0.58));
                graphics.setLineWidth(1.3);
                graphics.strokeRoundRect(building.getX() - size / 2,
                        building.getY() - size / 2,
                        size, size, 5, 5);
            }
        }

        // Il riflesso sale sul disco: cupola, bordo e luci pulsano insieme.
        double pulse = 0.82 + 0.18 * Math.sin(elapsed / 180);
        graphics.setFill(new RadialGradient(
                0, 0, x, y - 16, 64, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(95, 255, 139, 0.35 * pulse)),
                new Stop(1, Color.TRANSPARENT)
        ));
        graphics.fillOval(x - 66, y - 78, 132, 124);

        graphics.setFill(Color.web("#182a29"));
        graphics.fillOval(x - 38, y - 27, 76, 33);
        graphics.setStroke(Color.rgb(124, 255, 163, 0.85 * pulse));
        graphics.setLineWidth(2.5);
        graphics.strokeOval(x - 38, y - 27, 76, 33);

        graphics.setFill(new RadialGradient(
                0, 0, x - 5, y - 26, 24, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#b6ffd0")),
                new Stop(1, Color.web("#305c51"))
        ));
        graphics.fillOval(x - 20, y - 40, 40, 23);
        graphics.setFill(Color.rgb(191, 255, 204, pulse));
        graphics.fillOval(x - 28, y - 12, 7, 5);
        graphics.fillOval(x - 3, y - 8, 7, 5);
        graphics.fillOval(x + 22, y - 12, 7, 5);
    }

    /** Arresta attese e disegno quando si chiude la partita. */
    public void stop()
    {
        soundManager.stopUfoEncounterSounds();
        if (flightDelay != null)
        {
            flightDelay.stop();
            flightDelay = null;
        }
        if (flight != null)
        {
            flight.stop();
            flight = null;
        }
        if (fadeIn != null)
        {
            fadeIn.stop();
            fadeIn = null;
        }
        if (fadeOut != null)
        {
            fadeOut.stop();
            fadeOut = null;
        }
        overlay.setVisible(false);
    }
}
