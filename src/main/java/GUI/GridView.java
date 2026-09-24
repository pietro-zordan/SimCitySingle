package GUI;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Consumer;

import Events.EventType;
import controller.Controller;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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
    private static final Paint MILITARY_CAMOUFLAGE =
            createMilitaryCamouflagePattern();
    private static final Image LODGE_EYE = new Image(
            Objects.requireNonNull(
                    GridView.class.getResource("/images/lodge-eye.png")
            ).toExternalForm()
    );

    private final Controller controller;
    private final Consumer<String> errorHandler;
    private final GridPane view;
    private final Label infoLabel;
    private final StackPane[][] cells;
    private final Rectangle[][] graphicCells;
    private final Label[][] noPowerIcons;
    private final Label[][] boostIcons;
    private final Label[][] tsunamiIcons;
    private final Rectangle[][] tsunamiOverlays;
    private final Rectangle[][] explosionOverlays;
    private final Label[][] grassIcons;
    private final StackPane[][] lodgeIcons;
    private final Tooltip[][] grassTooltips;
    private final Tooltip[][] lodgeTooltips;
    private final Tooltip[][] powerPlantTooltips;

    private ConstructionType selectedType =
            ConstructionType.COMMERCIAL;
    private boolean demolitionActive = false;

    // Inizializza la griglia di gioco, allocando le matrici di componenti grafici per le celle e impostando il layout del GridPane.
    public GridView(
            Controller controller,
            Consumer<String> errorHandler)
    {
        if (controller == null || errorHandler == null)
        {
            throw new IllegalArgumentException(
                    "controller.Controller and error handler cannot be null"
            );
        }

        this.controller = controller;
        this.errorHandler = errorHandler;

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

        int rows = controller.getNumberOfRows();
        int columns = controller.getNumberOfColumns();

        cells = new StackPane[rows][columns];
        graphicCells = new Rectangle[rows][columns];
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

        // Popolamento celle griglia
        createCells();
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
                        noPowerIcon,
                        boostIcon,
                        grassIcon,
                        lodgeIcon,
                        tsunamiOverlay,
                        tsunamiIcon,
                        explosionOverlay
                );

                final int selectedRow = row;
                final int selectedColumn = column;

                cell.setOnMouseClicked(
                        new EventHandler<MouseEvent>()
                        {
                            @Override
                            public void handle(MouseEvent event)
                            {
                                handleCellClick(
                                        selectedRow,
                                        selectedColumn
                                );
                            }
                        }
                );

                cells[row][column] = cell;
                graphicCells[row][column] = graphicCell;
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
                controller.placeConstruction(
                        selectedType,
                        row,
                        column
                );

                infoLabel.setText(
                        "Creato/a un/a "
                                + selectedType
                                + " (riga "
                                + row
                                + ", colonna "
                                + column
                                + ")"
                );
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



    // Scorrendo l'intera griglia, invoca il rendering grafico di ciascuna cella sincronizzandola con lo stato attuale del modello.
    public void refresh()
    {
        boolean energyCrisisActive =
                controller.getActiveEventType()
                        == EventType.ENERGY_CRISIS;

        for (int row = 0;
             row < controller.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < controller.getNumberOfColumns();
                 column++)
            {
                renderCell(
                        row,
                        column,
                        controller.getCellState(row, column),
                        energyCrisisActive
                );
            }
        }
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
            powerPlantTooltips[row][column].setText(
                    "Energy: "
                            + controller.getPowerPlantEnergyConsumed(
                                    row,
                                    column
                            )
                            + " / "
                            + controller.getPowerPlantEnergyCapacity(
                                    row,
                                    column
                            )
            );

            Tooltip.install(
                    cells[row][column],
                    powerPlantTooltips[row][column]
            );
        }

        // --- EFFETTO GRAFICO ENERGY CRISIS ---

        // Controlliamo che l'edificio sia uno di quelli che consuma energia
        boolean consumesPower =
                state.type() == ConstructionType.INDUSTRIAL
                        || state.type()
                        == ConstructionType.RESIDENTIAL
                        || state.type()
                        == ConstructionType.COMMERCIAL
                        || state.type()
                        == ConstructionType.BANK
                        || state.type()
                        == ConstructionType.CONSTRUCTION_COMPANY;

        // Applichiamo il bordo solo se c'è la crisi, la cella è alimentata, e l'edificio consuma energia
        if (energyCrisisActive
                && !state.empty()
                && state.powered()
                && consumesPower)
        {
            graphicCell.setStroke(Color.PURPLE); // Cambiato in viola per non confondersi
            graphicCell.setStrokeWidth(2.5);
        }
        else if (grassPresent)
        {
            graphicCell.setStroke(
                    Color.web("#6F8F55")
            );
            graphicCell.setStrokeWidth(1.5);
        }
        else
        {
            // Altrimenti ripristiniamo il bordo grigio standard
            graphicCell.setStroke(Color.LIGHTGRAY);
            graphicCell.setStrokeWidth(1.0);
        }
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

        this.selectedType = selectedType;

        // Se seleziono una costruzione esco dalla modalità demolizione
        demolitionActive = false;
    }

    // Mostra l'effetto Tsunami su un'intera riga della griglia.
    public void showTsunamiRow(int row)
    {
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
