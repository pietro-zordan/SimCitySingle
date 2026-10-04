package Events;

import model.*;
import org.junit.jupiter.api.Test;
import policies.StandardPolicy;

import static org.junit.jupiter.api.Assertions.*;

class EarthquakeTest
{
    private Earthquake fixedEarthquake(City city, Grid grid, String direction)
    {
        return new Earthquake(city, grid)
        {
            @Override
            public String getRandomDirection()
            {
                String selected;
                do
                {
                    selected = super.getRandomDirection();
                }
                while (!direction.equals(selected));
                return selected;
            }

            @Override
            public Cell getStartCell(Grid target)
            {
                return target.getCell(2, 3);
            }
        };
    }

    @Test
    void horizontalEarthquakeDestroysOnlyItsRowOnARectangularGrid()
    {
        verifyLine("horizontal", 20, 30, 2);
    }

    @Test
    void verticalEarthquakeDestroysItsWholeColumnOnATallGrid()
    {
        verifyLine("vertical", 30, 20, 3);
    }

    private void verifyLine(String direction, int rows, int columns, int line)
    {
        Grid grid = new Grid(rows, columns);
        City city = new City(grid, new StandardPolicy());
        for (int row = 0; row < rows; row++)
        {
            for (int column = 0; column < columns; column++)
            {
                grid.restoreConstruction(new Park(), row, column);
            }
        }
        // La scossa non deve distruggere le strade neppure nella linea colpita.
        grid.getCell(2, 3).removeConstruction();
        grid.restoreConstruction(new Road(), 2, 3);
        Earthquake earthquake = fixedEarthquake(city, grid, direction);
        earthquake.start();
        assertEquals(EventType.EARTHQUAKE, earthquake.getType());
        assertEquals(direction, earthquake.getDirection());
        assertEquals(line, earthquake.getAffectedLineIndex());
        assertEquals("vertical".equals(direction) ? rows : columns,
                earthquake.getEarthquakeCells(grid).length);
        for (int row = 0; row < rows; row++)
        {
            for (int column = 0; column < columns; column++)
            {
                boolean hit = "vertical".equals(direction) ? column == line : row == line;
                boolean road = row == 2 && column == 3;
                assertEquals(hit && !road, grid.getCell(row, column).isEmpty(),
                        "Unexpected destruction at " + row + "," + column);
            }
        }
    }

    @Test
    void animationCoordinatesAreExposedThroughSimulationAndForcedDestructionStillWorks()
    {
        Grid grid = new Grid(20, 30);
        City city = new City(grid, new StandardPolicy());
        Construction protectedBuilding = new Park()
        {
            @Override
            public boolean canBeRemoved() { return false; }
        };
        grid.restoreConstruction(protectedBuilding, 2, 4);
        Earthquake earthquake = fixedEarthquake(city, grid, "horizontal");
        Simulation simulation = new Simulation(city, grid, 555, 0);
        simulation.startEvent(earthquake);
        assertEquals(EventType.EARTHQUAKE, simulation.getActiveEventType());
        assertEquals("horizontal", simulation.getActiveEarthquakeDirection());
        assertEquals(2, simulation.getActiveEarthquakeLineIndex());
        assertTrue(grid.getCell(2, 4).isEmpty());
    }
}
