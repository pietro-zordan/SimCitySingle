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

    // La GUI usa la stessa linea scelta dal modello, senza estrarre nuove coordinate.
    public String getDirection() { return direction; }

    public int getAffectedLineIndex() { return affectedLineIndex; }

    public String getRandomDirection()
    {

        int rand = random.nextInt(directions.length);

        direction=directions[rand];

        return direction;
    }

    public Cell getStartCell(Grid grid)
    {
        int randX = random.nextInt(grid.getNumberOfRows());
        int randY = random.nextInt(grid.getNumberOfColumns());

        return grid.getCell(randX, randY);

    }


    public Cell[] getEarthquakeCells(Grid grid)
    {
        Cell startCell= getStartCell(grid);
        boolean vertical = "vertical".equals(direction);
        int length = vertical ? grid.getNumberOfRows() : grid.getNumberOfColumns();
        Cell[] EarthquakeCells = new Cell[length];
        affectedLineIndex = vertical ? startCell.getColumn() : startCell.getRow();

        if("vertical".equals(direction))
        {
            for(int i = 0; i < grid.getNumberOfRows(); i++) {
                if (grid.isInside(i, startCell.getColumn()))
                    EarthquakeCells[i] = grid.getCell(i, startCell.getColumn());
            }
        }

        else {

            for (int j = 0; j <grid.getNumberOfColumns(); j++)
            {
                if (grid.isInside(startCell.getRow(), j))
                    EarthquakeCells[j] = grid.getCell(startCell.getRow(), j);
            }
        }

        return EarthquakeCells;
    }

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

    public void getHappinessImpact()
    {
        city.decreaseGlobalHappiness(HAPPINESS_DECREASE_AMOUNT);
    }

    @Override
    public void updateOfOneTick() {
        getHappinessImpact();
    }
}
