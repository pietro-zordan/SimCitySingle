package model;

import org.junit.jupiter.api.Test;
import policies.StandardPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimeManagerTest
{
    @Test
    void placingCriminalActivityRefreshesCityStatistics()
    {
        Grid grid = new Grid();
        City city = new City(
                grid,
                new StandardPolicy()
        );

        grid.placeConstruction(
                new Road(),
                10,
                10
        );

        CrimeManager crimeManager =
                new CrimeManager(city, grid);

        boolean created =
                crimeManager.placeCriminalActivity(5);

        assertTrue(created);
        assertEquals(
                -300,
                city.getGlobalEconomy()
        );
        assertEquals(
                -400,
                city.getGlobalHappiness()
        );
    }
    @Test
    void criminalActivityDoesNotUseLastBuildableCell()
    {
        Grid grid = new Grid();
        City city = new City(grid, new StandardPolicy());

        grid.restoreConstruction(new Road(), 10, 10);
        grid.restoreConstruction(new Park(), 9, 10);
        grid.restoreConstruction(new Park(), 11, 10);
        grid.restoreConstruction(new Park(), 10, 9);

        CrimeManager crimeManager = new CrimeManager(city, grid);

        assertEquals(1, grid.getBuildableCells().size());
        assertFalse(crimeManager.placeCriminalActivity(5));
        assertTrue(grid.getCell(10, 11).isEmpty());
    }

}
