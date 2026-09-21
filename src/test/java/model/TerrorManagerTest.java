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

    @Test
    void terroristGroupAppearsAfterHappinessDropsBy1000InOneTick()
    {
        Grid grid = new Grid();
        grid.restoreConstruction(new Road(), 10, 10);

        City city = new City(grid, new StandardPolicy());
        TerrorManager terrorManager = new TerrorManager(city, grid);

        city.decreaseGlobalHappiness(1000);

        assertTrue(terrorManager.updateOfOneTick());
        assertEquals(1, terrorManager.getNumOfTG());
    }

    @Test
    void terroristGroupAppearsAfterHappinessDropsBy1000WithinThreeTicks()
    {
        Grid grid = new Grid();
        grid.restoreConstruction(new Road(), 10, 10);

        City city = new City(grid, new StandardPolicy());
        TerrorManager terrorManager = new TerrorManager(city, grid);

        city.decreaseGlobalHappiness(300);
        terrorManager.updateOfOneTick();

        city.decreaseGlobalHappiness(300);
        terrorManager.updateOfOneTick();

        city.decreaseGlobalHappiness(400);

        assertTrue(terrorManager.updateOfOneTick());
        assertEquals(1, terrorManager.getNumOfTG());
    }

    @Test
    void terrorSpawnWorksWithNegativeBudget()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(new Road(), 10, 10);
        grid.restoreConstruction(new CriminalActivity(), 10, 11);

        City city =
                new City(
                        grid,
                        new StandardPolicy(),
                        -100
                );

        TerrorManager terrorManager =
                new TerrorManager(city, grid);

        boolean groupCreated = false;

        for (int i = 0; i < 8; i++) {
            groupCreated = terrorManager.updateOfOneTick();
        }

        assertTrue(groupCreated);
        assertEquals(1, terrorManager.getNumOfTG());
        assertEquals(-100, city.getBudget());
    }
}
