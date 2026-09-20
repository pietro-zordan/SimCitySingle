package GUI;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.ConstructionType;

// La classe GridView gestisce la rappresentazione grafica della griglia di gioco 2D.
// Si occupa di renderizzare le singole celle, mostrare icone di stato (mancanza energia, boost, incendi, tsunami) e gestire l'interazione da mouse.
public final class GridView
{
    private static final int CELL_SIZE = 30; // pixel

    private final Controller controller;
    private final Consumer<String> errorHandler;
    private final GridPane view;
    private final Label infoLabel;
    private final StackPane[][] cells;
    private final Rectangle[][] graphicCells;
    private final Label[][] noPowerIcons;
    private final Label[][] boostIcons;
    private final Label[][] tsunamiIcons;
    private final Tooltip[][] grassTooltips;

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
        grassTooltips = new Tooltip[rows][columns];

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

                Label tsunamiIcon = new Label("∿");
                tsunamiIcon.setStyle(
                        "-fx-text-fill: white;"
                                + "-fx-font-size: 22px;"
                                + "-fx-font-weight: bold;"
                );
                tsunamiIcon.setVisible(false);
                tsunamiIcon.setMouseTransparent(true);

                Tooltip grassTooltip = new Tooltip("Grass");
                grassTooltip.setShowDelay(
                        Duration.millis(100)
                );
                grassTooltip.setHideDelay(
                        Duration.ZERO
                );

                StackPane cell = new StackPane(
                        graphicCell,
                        noPowerIcon,
                        boostIcon,
                        tsunamiIcon
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
                grassTooltips[row][column] = grassTooltip;

                view.add(cell, column, row);
            }
        }
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

                demolitionActive = false;

                infoLabel.setText(
                        "Construction demolished at row "
                                + row
                                + ", column "
                                + column
                );
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

        Tooltip.uninstall(
                cells[row][column],
                grassTooltips[row][column]
        );

        if (!state.empty()
                && state.type() == ConstructionType.GRASS)
        {
            Tooltip.install(
                    cells[row][column],
                    grassTooltips[row][column]
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

    // Evidenzia visivamente una singola cella colpita dallo Tsunami cambiandone il colore e attivando l'icona dell'onda.
    private void showTsunamiCell(int row, int column)
    {
        graphicCells[row][column].setFill(Color.LIGHTBLUE);
        tsunamiIcons[row][column].setVisible(true);
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
                tsunamiIcons[row][column].setVisible(false);
            }
        }
    }

    public void activateDemolition()
    {
        demolitionActive = true;
    }

    // Mappa ed ottiene il colore identificativo associato a ciascun tipo di edificio sulla griglia.
    public static Color getConstructionColor(
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
            case CONSTRUCTION_COMPANY -> Color.BROWN;
            case CRIMINAL_ACTIVITY -> Color.BLACK;
            case POLICE_STATION -> Color.DARKBLUE;
            case GRASS -> Color.PALEGREEN;
        };
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