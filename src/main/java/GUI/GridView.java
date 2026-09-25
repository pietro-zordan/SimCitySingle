package GUI;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import Events.EventType;
import controller.Controller;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import model.ConstructionType;

// La classe GridView gestisce la rappresentazione grafica della griglia di gioco 2D.
// Si occupa di renderizzare le singole celle, mostrare icone di stato (mancanza energia, boost, incendi, tsunami) e gestire l'interazione da mouse.
public final class GridView
{
    private static final int CELL_SIZE = 30; // pixel
    private static final double CRISIS_OVERLAY_OPACITY = 0.16;
    private static final Paint MILITARY_CAMOUFLAGE =
            createMilitaryCamouflagePattern();
    private static final Image LODGE_EYE = new Image(
            Objects.requireNonNull(
                    GridView.class.getResource("/images/lodge-eye.png")
            ).toExternalForm()
    );

    private final Controller controller;
    private final Consumer<String> errorHandler;
    private final Runnable demolitionSound;
    private final Runnable placementSound;
    private final Runnable expansionSound;
    private Runnable expansionViewAction;
    private final GridPane view;
    private final Label infoLabel;
    private StackPane[][] cells;
    private Rectangle[][] graphicCells;
    private Rectangle[][] roadPreviewOverlays;
    private Rectangle[][] crisisOverlays;
    private Label[][] crisisSparkIcons;
    private Label[][] noPowerIcons;
    private Label[][] boostIcons;
    private Label[][] tsunamiIcons;
    private Rectangle[][] tsunamiOverlays;
    private Rectangle[][] explosionOverlays;
    private Label[][] grassIcons;
    private StackPane[][] lodgeIcons;
    private Tooltip[][] grassTooltips;
    private Tooltip[][] lodgeTooltips;
    private Tooltip[][] powerPlantTooltips;
    private Controller.CellState[][] renderedStates;
    private boolean hasRenderedFrame;
    private boolean lastRenderedCrisisState;
    private final List<Label> crisisEligibleSparks = new ArrayList<>();
    private final List<Label> crisisVisibleSparks = new ArrayList<>();
    private final Random crisisRandom = new Random();
    private Timeline crisisSparkTimeline;
    private boolean energyCrisisActive;

    private ConstructionType selectedType =
            ConstructionType.COMMERCIAL;
    private boolean demolitionActive = false;
    private boolean roadDragActive;
    private int roadStartRow;
    private int roadStartColumn;
    private int roadEndRow;
    private int roadEndColumn;

    // Inizializza la griglia di gioco, allocando le matrici di componenti grafici per le celle e impostando il layout del GridPane.
    public GridView(
            Controller controller,
            Consumer<String> errorHandler,
            Runnable demolitionSound,
            Runnable placementSound,
            Runnable expansionSound)
    {
        if (controller == null || errorHandler == null
                || demolitionSound == null || placementSound == null
                || expansionSound == null)
        {
            throw new IllegalArgumentException(
                    "Grid view dependencies cannot be null"
            );
        }

        this.controller = controller;
        this.errorHandler = errorHandler;
        this.demolitionSound = demolitionSound;
        this.placementSound = placementSound;
        this.expansionSound = expansionSound;

        // ---------- GRIGLIA CENTRATA ----------
        view = new GridPane();
        view.setHgap(1);
        view.setVgap(1);
        view.setPadding(new Insets(10));
        view.setAlignment(Pos.CENTER);

        infoLabel = new Label(
                "Clicca su una casella per colorarla"
        );
        infoLabel.setPadding(new Insets(5));

        rebuildCells();
    }

    // Ricrea la rappresentazione grafica quando cambia la dimensione della griglia.
    private void rebuildCells()
    {
        roadDragActive = false;
        int rows = controller.getNumberOfRows();
        int columns = controller.getNumberOfColumns();

        stop();
        crisisEligibleSparks.clear();
        view.getChildren().clear();

        cells = new StackPane[rows][columns];
        graphicCells = new Rectangle[rows][columns];
        roadPreviewOverlays = new Rectangle[rows][columns];
        crisisOverlays = new Rectangle[rows][columns];
        crisisSparkIcons = new Label[rows][columns];
        noPowerIcons = new Label[rows][columns];
        boostIcons = new Label[rows][columns];
        tsunamiIcons = new Label[rows][columns];
        tsunamiOverlays = new Rectangle[rows][columns];
        explosionOverlays = new Rectangle[rows][columns];
        grassIcons = new Label[rows][columns];
        lodgeIcons = new StackPane[rows][columns];
        grassTooltips = new Tooltip[rows][columns];
        lodgeTooltips = new Tooltip[rows][columns];
        powerPlantTooltips = new Tooltip[rows][columns];
        renderedStates = new Controller.CellState[rows][columns];
        hasRenderedFrame = false;

        createCells();
    }

    // Mantiene sincronizzate le dimensioni della GUI con quelle del modello.
    public void ensureGridSize()
    {
        int rows = controller.getNumberOfRows();
        int columns = controller.getNumberOfColumns();

        if (cells == null || cells.length != rows || cells[0].length != columns)
        {
            rebuildCells();
        }
    }

    // Instanzia e sovrappone le componenti grafiche per ciascuna cella della griglia agganciando i relativi listener per i click del mouse.
    private void createCells()
    {
        for (int row = 0;
             row < controller.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < controller.getNumberOfColumns();
                 column++)
            {
                Rectangle graphicCell =
                        new Rectangle(CELL_SIZE, CELL_SIZE);
                graphicCell.setFill(Color.WHITESMOKE);
                graphicCell.setStroke(Color.LIGHTGRAY);

                Rectangle crisisOverlay =
                        new Rectangle(CELL_SIZE - 4, CELL_SIZE - 4);
                crisisOverlay.setFill(Color.web("#171326"));
                crisisOverlay.setVisible(false);
                crisisOverlay.setMouseTransparent(true);

                Label crisisSpark = new Label("✦");
                crisisSpark.setStyle(
                        "-fx-text-fill: #f0d7ff;"
                                + "-fx-font-size: 13px;"
                                + "-fx-font-weight: bold;"
                );
                crisisSpark.setVisible(false);
                crisisSpark.setMouseTransparent(true);
                StackPane.setAlignment(crisisSpark, Pos.TOP_RIGHT);
                crisisSpark.setTranslateX(-2);
                crisisSpark.setTranslateY(1);

                Label noPowerIcon = new Label("⚡");
                noPowerIcon.setStyle(
                        "-fx-text-fill: red;"
                                + "-fx-font-size: 16px;"
                );
                noPowerIcon.setVisible(false);
                noPowerIcon.setMouseTransparent(true);

                Label boostIcon = new Label("⬆");
                boostIcon.setStyle(
                        "-fx-text-fill: #32CD32;"
                                + "-fx-font-weight: bold;"
                                + "-fx-font-size: 22px;"
                );
                boostIcon.setVisible(false);
                boostIcon.setMouseTransparent(true);

                Rectangle tsunamiOverlay =
                        new Rectangle(
                                CELL_SIZE,
                                CELL_SIZE
                        );
                tsunamiOverlay.setFill(
                        Color.LIGHTBLUE
                );
                tsunamiOverlay.setVisible(false);
                tsunamiOverlay.setMouseTransparent(true);

                Rectangle explosionOverlay =
                        new Rectangle(
                                CELL_SIZE,
                                CELL_SIZE
                        );
                explosionOverlay.setFill(Color.ORANGERED);
                explosionOverlay.setOpacity(0.75);
                explosionOverlay.setVisible(false);
                explosionOverlay.setMouseTransparent(true);

                Label tsunamiIcon = new Label("∿");
                tsunamiIcon.setStyle(
                        "-fx-text-fill: white;"
                                + "-fx-font-size: 22px;"
                                + "-fx-font-weight: bold;"
                );
                tsunamiIcon.setVisible(false);
                tsunamiIcon.setMouseTransparent(true);

                Label grassIcon = new Label("❧");
                grassIcon.setStyle(
                        "-fx-text-fill: #2F5D31;"
                                + "-fx-font-size: 21px;"
                                + "-fx-font-weight: bold;"
                );
                grassIcon.setVisible(false);
                grassIcon.setMouseTransparent(true);

                StackPane lodgeIcon = createLodgeIcon();
                lodgeIcon.setVisible(false);

                Tooltip grassTooltip = new Tooltip("Grass");
                grassTooltip.setShowDelay(
                        Duration.millis(100)
                );
                grassTooltip.setHideDelay(
                        Duration.ZERO
                );

                Tooltip lodgeTooltip = new Tooltip("Masonic Lodge");
                lodgeTooltip.setShowDelay(Duration.millis(100));
                lodgeTooltip.setHideDelay(Duration.ZERO);

                Tooltip powerPlantTooltip = new Tooltip();
                powerPlantTooltip.setShowDelay(
                        Duration.millis(100)
                );
                powerPlantTooltip.setHideDelay(
                        Duration.ZERO
                );

                StackPane cell = new StackPane(
                        graphicCell,
                        crisisOverlay,
                        noPowerIcon,
                        boostIcon,
                        grassIcon,
                        lodgeIcon,
                        crisisSpark,
                        tsunamiOverlay,
                        tsunamiIcon,
                        explosionOverlay
                );

                Rectangle roadPreview =
                        new Rectangle(CELL_SIZE, CELL_SIZE);
                roadPreview.setFill(Color.rgb(60, 185, 230, 0.42));
                roadPreview.setVisible(false);
                roadPreview.setMouseTransparent(true);
                cell.getChildren().add(roadPreview);

                final int selectedRow = row;
                final int selectedColumn = column;

                cell.setOnMouseClicked(
                        new EventHandler<MouseEvent>()
                        {
                            @Override
                            public void handle(MouseEvent event)
                            {
                                if (selectedType != ConstructionType.ROAD
                                        || demolitionActive)
                                {
                                    handleCellClick(
                                            selectedRow,
                                            selectedColumn
                                    );
                                }
                            }
                        }
                );

                cell.setOnMousePressed(
                        new EventHandler<MouseEvent>()
                        {
                            @Override
                            public void handle(MouseEvent event)
                            {
                                if (selectedType == ConstructionType.ROAD
                                        && !demolitionActive
                                        && event.getButton()
                                        == MouseButton.PRIMARY)
                                {
                                    startRoadDrag(selectedRow, selectedColumn);
                                    event.consume();
                                }
                            }
                        }
                );

                cell.setOnMouseDragged(
                        new EventHandler<MouseEvent>()
                        {
                            @Override
                            public void handle(MouseEvent event)
                            {
                                if (roadDragActive)
                                {
                                    updateRoadDrag(event);
                                    event.consume();
                                }
                            }
                        }
                );

                cell.setOnMouseReleased(
                        new EventHandler<MouseEvent>()
                        {
                            @Override
                            public void handle(MouseEvent event)
                            {
                                if (roadDragActive
                                        && event.getButton()
                                        == MouseButton.PRIMARY)
                                {
                                    updateRoadDrag(event);
                                    finishRoadDrag();
                                    event.consume();
                                }
                            }
                        }
                );

                cells[row][column] = cell;
                graphicCells[row][column] = graphicCell;
                roadPreviewOverlays[row][column] = roadPreview;
                crisisOverlays[row][column] = crisisOverlay;
                crisisSparkIcons[row][column] = crisisSpark;
                noPowerIcons[row][column] = noPowerIcon;
                boostIcons[row][column] = boostIcon;
                tsunamiIcons[row][column] = tsunamiIcon;
                tsunamiOverlays[row][column] =
                        tsunamiOverlay;
                explosionOverlays[row][column] =
                        explosionOverlay;
                grassIcons[row][column] = grassIcon;
                lodgeIcons[row][column] = lodgeIcon;
                grassTooltips[row][column] = grassTooltip;
                lodgeTooltips[row][column] = lodgeTooltip;
                powerPlantTooltips[row][column] =
                        powerPlantTooltip;

                view.add(cell, column, row);
            }
        }
    }

    private void startRoadDrag(int row, int column)
    {
        roadDragActive = true;
        roadStartRow = row;
        roadStartColumn = column;
        roadEndRow = row;
        roadEndColumn = column;
        showRoadPreview(true);
    }

    // Converte direttamente la posizione del mouse nella cella corrispondente:
    // il costo resta costante anche su griglie 30x30 o 40x40.
    private void updateRoadDrag(MouseEvent event)
    {
        Point2D localPoint = view.sceneToLocal(event.getSceneX(), event.getSceneY());
        Bounds firstCell = cells[0][0].getBoundsInParent();
        double relativeX = localPoint.getX() - firstCell.getMinX();
        double relativeY = localPoint.getY() - firstCell.getMinY();
        double stepX = CELL_SIZE + view.getHgap();
        double stepY = CELL_SIZE + view.getVgap();

        if (relativeX < 0 || relativeY < 0)
        {
            return;
        }

        int column = (int) Math.floor(relativeX / stepX);
        int row = (int) Math.floor(relativeY / stepY);

        if (row < 0 || row >= cells.length || column < 0 || column >= cells[0].length)
        {
            return;
        }

        // Se il cursore è nello spazio di 1 px tra due celle, manteniamo l'ultima cella valida.
        if (relativeX - column * stepX > CELL_SIZE || relativeY - row * stepY > CELL_SIZE)
        {
            return;
        }

        int endRow = row;
        int endColumn = column;
        if (Math.abs(column - roadStartColumn) >= Math.abs(row - roadStartRow))
        {
            endRow = roadStartRow;
        }
        else
        {
            endColumn = roadStartColumn;
        }

        if (endRow != roadEndRow || endColumn != roadEndColumn)
        {
            showRoadPreview(false);
            roadEndRow = endRow;
            roadEndColumn = endColumn;
            showRoadPreview(true);
        }
    }

    private void showRoadPreview(boolean visible)
    {
        int rowStep = Integer.compare(roadEndRow, roadStartRow);
        int columnStep = Integer.compare(roadEndColumn, roadStartColumn);
        int length = Math.max(
                Math.abs(roadEndRow - roadStartRow),
                Math.abs(roadEndColumn - roadStartColumn));

        for (int i = 0; i <= length; i++)
        {
            roadPreviewOverlays[roadStartRow + i * rowStep]
                    [roadStartColumn + i * columnStep]
                    .setVisible(visible);
        }
    }

    private void finishRoadDrag()
    {
        roadDragActive = false;
        showRoadPreview(false);

        if (roadStartRow == roadEndRow
                && roadStartColumn == roadEndColumn)
        {
            handleCellClick(roadStartRow, roadStartColumn);
            return;
        }

        int rowStep = Integer.compare(roadEndRow, roadStartRow);
        int columnStep = Integer.compare(roadEndColumn, roadStartColumn);
        int length = Math.max(
                Math.abs(roadEndRow - roadStartRow),
                Math.abs(roadEndColumn - roadStartColumn));
        int previousRows = controller.getNumberOfRows();
        int previousColumns = controller.getNumberOfColumns();
        int placed = 0;
        boolean canContinue = true;

        for (int i = 0; i <= length && canContinue; i++)
        {
            int row = roadStartRow + i * rowStep;
            int column = roadStartColumn + i * columnStep;
            Controller.CellState state =
                    controller.getCellState(row, column);

            if (state.empty()
                    || state.type() != ConstructionType.ROAD)
            {
                try
                {
                    controller.placeConstruction(
                            ConstructionType.ROAD, row, column);
                    placed++;
                }
                catch (IllegalStateException
                       | IllegalArgumentException exception)
                {
                    errorHandler.accept(exception.getMessage());
                    canContinue = false;
                }
            }
        }

        if (placed > 0)
        {
            if (controller.getNumberOfRows() != previousRows
                    || controller.getNumberOfColumns() != previousColumns)
            {
                ensureGridSize();
                if (expansionViewAction != null)
                {
                    expansionViewAction.run();
                }
                expansionSound.run();
            }
            else
            {
                placementSound.run();
            }
            infoLabel.setText("Roads built: " + placed);
        }
    }

    private static StackPane createLodgeIcon()
    {
        Polygon triangle = new Polygon(
                15.0, 2.0,
                27.0, 26.0,
                3.0, 26.0
        );
        triangle.setFill(Color.TRANSPARENT);
        triangle.setStroke(Color.web("#483414"));
        triangle.setStrokeWidth(2);

        ImageView eye = new ImageView(LODGE_EYE);
        eye.setFitWidth(13);
        eye.setPreserveRatio(true);
        eye.setSmooth(true);
        eye.setTranslateY(4);

        StackPane icon = new StackPane(triangle, eye);
        icon.setPrefSize(CELL_SIZE, CELL_SIZE);
        icon.setMouseTransparent(true);
        return icon;
    }

    // Gestisce il click su una cella provando a posizionare l'edificio selezionato o mostrando le informazioni di riga e colonna.
    private void handleCellClick(int row, int column)
    {
        Controller.CellState state =
                controller.getCellState(row, column);

        // Se è stata attivata la demolizione
        if (demolitionActive)
        {
            if (state.empty())
            {
                errorHandler.accept(
                        "There is no construction to demolish"
                );
                return;
            }

            try
            {
                controller.removeConstructionByPlayer(
                        row,
                        column
                );
                demolitionSound.run();

                // La modalità resta attiva per permettere
                // demolizioni consecutive con un solo clic sul pulsante.
                if (state.type() == ConstructionType.TERRORISTIC_GROUP)
                {
                    errorHandler.accept(
                            "Terrorist group eliminated!"
                    );
                }
                else if (state.type() == ConstructionType.CRIMINAL_ACTIVITY)
                {
                    errorHandler.accept(
                            "Criminal activity eliminated!"
                    );
                }
                else
                {
                    infoLabel.setText(
                            "Construction demolished at row "
                                    + row
                                    + ", column "
                                    + column
                    );
                }
            }
            catch (IllegalStateException
                   | IllegalArgumentException exception)
            {
                errorHandler.accept(
                        exception.getMessage()
                );
            }

            return;
        }

        // Modalità normale: prova a piazzare una costruzione
        if (state.empty())
        {
            try
            {
                int previousRows = controller.getNumberOfRows();
                int previousColumns = controller.getNumberOfColumns();

                controller.placeConstruction(selectedType, row, column);

                boolean gridExpanded =
                        controller.getNumberOfRows() != previousRows
                                || controller.getNumberOfColumns() != previousColumns;

                if (gridExpanded)
                {
                    ensureGridSize();

                    if (expansionViewAction != null)
                    {
                        expansionViewAction.run();
                    }

                    expansionSound.run();
                    infoLabel.setText(
                            "Grid expanded to " + controller.getNumberOfRows()
                                    + "x" + controller.getNumberOfColumns()
                    );
                }
                else
                {
                    placementSound.run();
                    infoLabel.setText(
                            "Creato/a un/a " + selectedType
                                    + " (riga " + row
                                    + ", colonna " + column + ")"
                    );
                }
            }
            catch (IllegalStateException
                   | IllegalArgumentException exception)
            {
                errorHandler.accept(
                        exception.getMessage()
                );
            }
        }
        else
        {
            infoLabel.setText(
                    "Ultima casella cliccata: riga "
                            + row
                            + ", colonna "
                            + column
            );
        }
    }



    // Controlla tutte le celle, ma ridisegna soltanto quelle il cui stato visivo è cambiato.
    public void refresh()
    {
        ensureGridSize();
        boolean crisisNowActive = controller.getActiveEventType() == EventType.ENERGY_CRISIS;
        boolean crisisModeChanged = !hasRenderedFrame || crisisNowActive != lastRenderedCrisisState;

        clearCrisisSparks();
        crisisEligibleSparks.clear();

        for (int row = 0; row < controller.getNumberOfRows(); row++)
        {
            for (int column = 0; column < controller.getNumberOfColumns(); column++)
            {
                Controller.CellState state = controller.getCellState(row, column);

                if (crisisNowActive && isCrisisAffected(row, column, state))
                {
                        }

                if (crisisModeChanged || !Objects.equals(renderedStates[row][column], state))
                {
                    renderCell(row, column, state, crisisNowActive);
                }
                else if (!state.empty() && state.type() == ConstructionType.POWER_PLANT)
                {
                    updatePowerPlantTooltip(row, column);
                }
            }
        }

        lastRenderedCrisisState = crisisNowActive;
        hasRenderedFrame = true;

        if (crisisNowActive && !energyCrisisActive)
        {
            energyCrisisActive = true;
            startCrisisSparks();
        }
        else if (!crisisNowActive && energyCrisisActive)
        {
            stop();
        }
    }

    private void startCrisisSparks()
    {
        crisisSparkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1.6),
                        event -> showCrisisSparks()),
                new KeyFrame(Duration.seconds(1.75),
                        event -> clearCrisisSparks())
        );
        crisisSparkTimeline.setCycleCount(Timeline.INDEFINITE);
        crisisSparkTimeline.play();
    }

    private void showCrisisSparks()
    {
        clearCrisisSparks();
        int count = crisisEligibleSparks.size();
        if (count == 0)
        {
            return;
        }

        int first = crisisRandom.nextInt(count);
        Label spark = crisisEligibleSparks.get(first);
        spark.setVisible(true);
        crisisVisibleSparks.add(spark);

        if (count > 6)
        {
            int second = (first + 1
                    + crisisRandom.nextInt(count - 1)) % count;
            Label secondSpark = crisisEligibleSparks.get(second);
            secondSpark.setVisible(true);
            crisisVisibleSparks.add(secondSpark);
        }
    }

    private void clearCrisisSparks()
    {
        for (Label spark : crisisVisibleSparks)
        {
            spark.setVisible(false);
        }
        crisisVisibleSparks.clear();
    }

    public void stop()
    {
        energyCrisisActive = false;
        if (crisisSparkTimeline != null)
        {
            crisisSparkTimeline.stop();
            crisisSparkTimeline = null;
        }
        clearCrisisSparks();
    }

    // Applica i colori di sfondo, l'evidenziazione di crisi energetica e visibilita delle icone (incendi, alimentazione, boost) per una specifica cella.
    private void renderCell(
            int row,
            int column,
            Controller.CellState state,
            boolean energyCrisisActive)
    {
        Rectangle graphicCell = graphicCells[row][column];

        if (state.onFire())
        {
            graphicCell.setFill(Color.RED);
        }
        else if (state.empty())
        {
            graphicCell.setFill(Color.WHITESMOKE);
        }
        else
        {
            graphicCell.setFill(
                    getConstructionColor(state.type())
            );
        }

        if (!state.empty())
        {
            noPowerIcons[row][column].setVisible(
                    !state.powered()
            );
        }
        else
        {
            // Cella vuota: nessuna icona da mostrare
            noPowerIcons[row][column].setVisible(false);
        }

        boostIcons[row][column].setVisible(
                !state.empty() && state.boosted()
        );

        boolean grassPresent =
                !state.empty()
                        && state.type()
                        == ConstructionType.GRASS;

        grassIcons[row][column].setVisible(
                grassPresent
        );

        boolean lodgePresent =
                !state.empty()
                        && state.type() == ConstructionType.MASONIC_LODGE;
        lodgeIcons[row][column].setVisible(lodgePresent);

        Tooltip.uninstall(
                cells[row][column],
                lodgeTooltips[row][column]
        );
        if (lodgePresent)
        {
            Tooltip.install(
                    cells[row][column],
                    lodgeTooltips[row][column]
            );
        }

        Tooltip.uninstall(
                cells[row][column],
                grassTooltips[row][column]
        );

        if (grassPresent)
        {
            Tooltip.install(
                    cells[row][column],
                    grassTooltips[row][column]
            );
        }

        boolean powerPlantPresent =
                !state.empty()
                        && state.type()
                        == ConstructionType.POWER_PLANT;

        Tooltip.uninstall(
                cells[row][column],
                powerPlantTooltips[row][column]
        );

        if (powerPlantPresent)
        {
            updatePowerPlantTooltip(row, column);
            Tooltip.install(cells[row][column], powerPlantTooltips[row][column]);
        }

        // --- EFFETTO GRAFICO ENERGY CRISIS ---

        boolean crisisAffected = energyCrisisActive && isCrisisAffected(row, column, state);
        crisisOverlays[row][column].setVisible(crisisAffected);

        if (crisisAffected)
        {
            crisisEligibleSparks.add(crisisSparkIcons[row][column]);
            crisisOverlays[row][column].setOpacity(
                    CRISIS_OVERLAY_OPACITY
            );
            graphicCell.setStroke(Color.web("#a76cdd"));
            graphicCell.setEffect(null);
            graphicCell.setStrokeWidth(2.2);
        }
        else if (grassPresent)
        {
            graphicCell.setEffect(null);
            graphicCell.setStroke(
                    Color.web("#6F8F55")
            );
            graphicCell.setStrokeWidth(1.5);
        }
        else
        {
            graphicCell.setEffect(null);
            // Altrimenti ripristiniamo il bordo grigio standard
            graphicCell.setStroke(Color.LIGHTGRAY);
            graphicCell.setStrokeWidth(1.0);
        }

        renderedStates[row][column] = state;
    }

    private boolean isCrisisAffected(int row, int column, Controller.CellState state)
    {
        return !state.empty() && state.powered() && controller.cellConsumesPower(row, column);
    }

    private void updatePowerPlantTooltip(int row, int column)
    {
        powerPlantTooltips[row][column].setText(
                "Energy: " + controller.getPowerPlantEnergyConsumed(row, column)
                        + " / " + controller.getPowerPlantEnergyCapacity(row, column)
        );
    }

    // Imposta il tipo di costruzione corrente da piazzare al momento del click sulla griglia.
    public void setSelectedType(ConstructionType selectedType)
    {
        if (selectedType == null)
        {
            throw new IllegalArgumentException(
                    "Selected construction type cannot be null"
            );
        }

        if (roadDragActive)
        {
            showRoadPreview(false);
            roadDragActive = false;
        }

        this.selectedType = selectedType;

        // Se seleziono una costruzione esco dalla modalità demolizione
        demolitionActive = false;
    }

    // Mostra l'effetto Tsunami su un'intera riga della griglia.
    public void showTsunamiRow(int row)
    {
        ensureGridSize();
        for (int column = 0;
             column < controller.getNumberOfColumns();
             column++)
        {
            showTsunamiCell(row, column);
        }
    }

    // Mostra l'effetto Tsunami su un'intera colonna della griglia.
    public void showTsunamiColumn(int column)
    {
        ensureGridSize();
        for (int row = 0;
             row < controller.getNumberOfRows();
             row++)
        {
            showTsunamiCell(row, column);
        }
    }

    /*
     * Mostra lo tsunami con un overlay sopra l'intera cella.
     * In questo modo anche l'erba viene coperta correttamente
     * dall'animazione senza modificarne lo stato o il colore reale.
     */
    private void showTsunamiCell(int row, int column)
    {
        tsunamiOverlays[row][column].setVisible(
                true
        );

        tsunamiIcons[row][column].setVisible(
                true
        );
    }

    // Nasconde l'icona dello Tsunami da tutte le celle della griglia al termine dell'evento.
    public void clearTsunami()
    {
        ensureGridSize();
        for (int row = 0;
             row < controller.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < controller.getNumberOfColumns();
                 column++)
            {
                tsunamiOverlays[row][column].setVisible(
                        false
                );

                tsunamiIcons[row][column].setVisible(
                        false
                );
            }
        }
    }

    // Mostra un anello dell'esplosione e aggiorna le celle solo quando l'onda le raggiunge.
    public void showExplosionRing(
            int centerRow,
            int centerColumn,
            int radius,
            int maxRadius)
    {
        ensureGridSize();
        double progress = 0.0;

        if (maxRadius > 0)
        {
            progress = (double) radius / maxRadius;
        }

        Color explosionColor =
                Color.YELLOW.interpolate(
                        Color.DARKRED,
                        progress
                );

        double opacity =
                0.9 - (0.4 * progress);

        boolean energyCrisisActive =
                controller.getActiveEventType()
                        == EventType.ENERGY_CRISIS;

        for (int row = 0; row < controller.getNumberOfRows(); row++)
        {
            for (int column = 0; column < controller.getNumberOfColumns(); column++)
            {
                int rowDistance = Math.abs(row - centerRow);
                int columnDistance = Math.abs(column - centerColumn);

                if (Math.max(rowDistance, columnDistance) == radius)
                {
                    renderCell(
                            row,
                            column,
                            controller.getCellState(row, column),
                            energyCrisisActive
                    );

                    Rectangle overlay =
                            explosionOverlays[row][column];

                    overlay.setFill(explosionColor);
                    overlay.setOpacity(opacity);
                    overlay.setVisible(true);
                }
            }
        }
    }

    // Nasconde l'effetto grafico dell'esplosione da tutta la griglia.
    public void clearExplosion()
    {
        ensureGridSize();
        for (int row = 0; row < controller.getNumberOfRows(); row++)
        {
            for (int column = 0; column < controller.getNumberOfColumns(); column++)
            {
                explosionOverlays[row][column].setVisible(false);
            }
        }
    }

    public void activateDemolition()
    {
        if (roadDragActive)
        {
            showRoadPreview(false);
            roadDragActive = false;
        }
        demolitionActive = true;
    }

    public void cancelDemolition()
    {
        demolitionActive = false;
    }

    // Mappa ed ottiene il riempimento grafico associato a ciascun tipo di edificio sulla griglia.
    public static Paint getConstructionColor(
            ConstructionType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException(
                    "model.Construction type cannot be null"
            );
        }

        return switch (type)
        {
            case INDUSTRIAL -> Color.DARKGRAY;
            case RESIDENTIAL -> Color.LIGHTGREEN;
            case PARK -> Color.FORESTGREEN;
            case ROAD -> Color.DIMGRAY;
            case POWER_PLANT -> Color.ORANGE;
            case COMMERCIAL -> Color.CORNFLOWERBLUE;
            case BANK -> Color.GOLD;
            case CONSTRUCTION_COMPANY -> Color.web("#7B5E3B");
            case CRIMINAL_ACTIVITY -> Color.BLACK;
            case TERRORISTIC_GROUP -> Color.DARKRED;
            case POLICE_STATION -> Color.DARKBLUE;
            case GRASS -> Color.web("#9DBB7A");
            case NUCLEAR_PLANT -> Color.PURPLE;
            case WASTE_TREATMENT_PLANT -> Color.web("#A0522D");
            case MILITARY_BASE -> MILITARY_CAMOUFLAGE;
            case MASONIC_LODGE -> Color.YELLOW;
        };
    }

    // Genera una texture mimetica originale usando verdi, marroni e beige.
    // In questo modo la base militare ha l'aspetto camouflage senza usare immagini o scritte esterne.
    // Usa una piccola immagine camouflage incorporata direttamente nel gioco.
    // In questo modo non servono connessioni Internet e la texture resta nitida anche a 30x30.
    private static Paint createMilitaryCamouflagePattern()
    {
        byte[] imageBytes =
                Base64.getDecoder().decode(
                        "iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAIAAAC0Ujn1AAABYklEQVR42rWWv0vDQBTHX54ZVLAUDiOB0nNUCAQiOhWCa3Fxdu7k3+Pf4d/gLAQKHVwDgZJCIQQXwcGh8Wjv57u0fdySI/ncl+/73iPBy+wR/ivnDAw1L2qwVsNCaQcpXABIsys7erj+NaI/yrXlyx6qd55VuqqFXuirhW4X+jpoKrVVB1Bt6jA6vyR6ogqnWiklxKQ050xkAYlatlnOjFPRQsWG6OQKW1AkQSwnnSgF1YTZ6UQuAKAW1I8uvRDSr4CzFsVyB92wUNVIzMDb+8KREOlS0O/I63PiDl/DQrG8rLDQTzA4XS2/xYrii+vhuZfFD7fR59fKfWWkVuyjHeFApdKD9O5mz3aZ0kJCA8CEX1qGtTaFmGRxksW9fZgXdVm1T/fjI3qt0oN8mnbRrn983egGTnSm3Q+1x/LRgPLvYeEax1NZtXw0sE+Ssmrtp3boydg9j5wsDVrL3Qj3xW3XH7eZjsFsvkrrAAAAAElFTkSuQmCC"
                );

        Image image =
                new Image(
                        new ByteArrayInputStream(
                                imageBytes
                        )
                );

        return new ImagePattern(
                image,
                0,
                0,
                CELL_SIZE,
                CELL_SIZE,
                false
        );
    }

    public double getCellCenterOffsetX(int column)
    {
        if (column < 0
                || column >= controller.getNumberOfColumns())
        {
            throw new IllegalArgumentException(
                    "Column outside the grid"
            );
        }

        double cellCenterFromLeft =
                view.getPadding().getLeft()
                        + column * (CELL_SIZE + view.getHgap())
                        + CELL_SIZE / 2.0;

        return cellCenterFromLeft
                - getGridVisualWidth() / 2.0;
    }

    public double getCellCenterOffsetY(int row)
    {
        if (row < 0
                || row >= controller.getNumberOfRows())
        {
            throw new IllegalArgumentException(
                    "Row outside the grid"
            );
        }

        double cellCenterFromTop =
                view.getPadding().getTop()
                        + row * (CELL_SIZE + view.getVgap())
                        + CELL_SIZE / 2.0;

        return cellCenterFromTop
                - getGridVisualHeight() / 2.0;
    }

    public double getGridVisualWidth()
    {
        return view.getPadding().getLeft()
                + view.getPadding().getRight()
                + controller.getNumberOfColumns() * CELL_SIZE
                + (controller.getNumberOfColumns() - 1)
                * view.getHgap();
    }

    public double getGridVisualHeight()
    {
        return view.getPadding().getTop()
                + view.getPadding().getBottom()
                + controller.getNumberOfRows() * CELL_SIZE
                + (controller.getNumberOfRows() - 1)
                * view.getVgap();
    }

    public void setExpansionViewAction(Runnable expansionViewAction)
    {
        this.expansionViewAction = expansionViewAction;
    }

    // Restituisce il contenitore GridPane che rappresenta la griglia visiva.
    public GridPane getView()
    {
        return view;
    }

    // Restituisce l'etichetta usata per mostrare le informazioni di stato o le notifiche sulle celle.
    public Label getInfoLabel()
    {
        return infoLabel;
    }
}
