package model;

import org.junit.jupiter.api.Test;
import policies.StandardPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerrorManagerTest
{
    @Test
    void terroristAttackReducesPopulation()
    {
        Grid grid = new Grid();

        Residential residential = new Residential();
        residential.restoreState(30, 1, 1, 0, 0);

        grid.restoreConstruction(new Road(), 10, 10);
        grid.restoreConstruction(residential, 10, 11);
        grid.restoreConstruction(new TerroristicGroup(), 10, 9);

        City city = new City(grid, new StandardPolicy());
        TerrorManager terrorManager = new TerrorManager(city, grid);

        terrorManager.updateOfOneTick();

        assertEquals(15, residential.getPopulation());
        assertEquals(15, city.getGlobalPopulation());
    }

    @Test
    void terroristAttackRemovesEmptyResidential()
    {
        Grid grid = new Grid();

        Residential residential = new Residential();

        grid.restoreConstruction(new Road(), 10, 10);
        grid.restoreConstruction(residential, 10, 11);
        grid.restoreConstruction(new TerroristicGroup(), 10, 9);

        City city = new City(grid, new StandardPolicy());
        TerrorManager terrorManager = new TerrorManager(city, grid);

        terrorManager.updateOfOneTick();

        assertTrue(grid.getCell(10, 11).isEmpty());
        assertEquals(0, city.getGlobalPopulation());
    }
}
