package Events;

import model.Cell;
import model.City;
import model.Grid;

import java.util.Random;

public class Earthquake extends Event{

    private Grid grid;
    private String[] directions = {"horizontal", "vertical"};
    private String direction;
    private int affectedLineIndex = -1;

    Random random = new Random();

    public static final int HAPPINESS_DECREASE_AMOUNT = 700;

    public Earthquake(City city, Grid grid)
    {
        super(3, city,500 );
        this.grid=grid;

        launchNumber = 1; //25;
    }

    @Override
    public EventType getType() {
        return EventType.EARTHQUAKE;
    }

    // Indica se il terremoto colpisce una riga o una colonna.
    public String getDirection()
    {
        return direction;
    }

    // Restituisce l'indice della riga o colonna colpita, usato per disegnare la scossa.
    public int getAffectedLineIndex()
    {
        return affectedLineIndex;
    }

    // Estrae e memorizza l'orientamento della scossa.
    public String getRandomDirection()
    {

        int rand = random.nextInt(directions.length);

        direction=directions[rand];

        return direction;
    }

    // Estrae una cella che determina quale riga o colonna verrà colpita.
    public Cell getStartCell(Grid grid)
    {
        int row = random.nextInt(grid.getNumberOfRows());
        int column = random.nextInt(grid.getNumberOfColumns());

        return grid.getCell(row, column);

    }


    // Raccoglie le celle lungo la riga o colonna scelta e ne memorizza l'indice.
    public Cell[] getEarthquakeCells(Grid grid)
    {
        Cell[] affectedCells;
        Cell startCell = getStartCell(grid);

        if ("vertical".equals(direction))
        {
            affectedCells = new Cell[grid.getNumberOfRows()];
            affectedLineIndex = startCell.getColumn();

            for (int row = 0; row < grid.getNumberOfRows(); row++)
            {
                if (grid.isInside(row, startCell.getColumn()))
                {
                    affectedCells[row] = grid.getCell(row, startCell.getColumn());
                }
            }
        }
        else
        {
            affectedCells = new Cell[grid.getNumberOfColumns()];
            affectedLineIndex = startCell.getRow();

            for (int column = 0; column < grid.getNumberOfColumns(); column++)
            {
                if (grid.isInside(startCell.getRow(), column))
                {
                    affectedCells[column] = grid.getCell(startCell.getRow(), column);
                }
            }
        }

        return affectedCells;
    }

    // Distrugge le costruzioni lungo la linea colpita, lasciando alla griglia gli eventuali effetti a catena.
    public void getDestruction()
    {
        Cell[] earthquakeCells = getEarthquakeCells(grid);

        for (Cell cell: earthquakeCells)
        {
            if (cell != null)
            {
                grid.destroyArea(cell.getRow(), cell.getColumn(), 0);
            }
        }
    }

    @Override
    public void start() {
        getRandomDirection();
        getDestruction();
        city.refreshStatistics();
    }

    // Applica alla città la perdita di felicità dovuta al terremoto.
    public void getHappinessImpact()
    {
        city.decreaseGlobalHappiness(HAPPINESS_DECREASE_AMOUNT);
    }

    @Override
    public void updateOfOneTick() {
        getHappinessImpact();
    }
}
