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
                crimeManager.placeCriminalActivity(20);

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
    void criminalActivityCannotAppearBeforeTick20()
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

        assertFalse(
                crimeManager.placeCriminalActivity(19)
        );

        assertEquals(
                0,
                countCriminalActivities(grid)
        );
    }

    @Test
    void suddenHappinessDropDoesNotCreateCriminalActivity()
    {
        Grid grid = new Grid();
        City city = new City(grid, new StandardPolicy());

        grid.placeConstruction(new Road(), 10, 10);

        CrimeManager crimeManager = new CrimeManager(city, grid);

        city.decreaseGlobalHappiness(1200);

        assertFalse(crimeManager.tryToCreateCriminalActivity(20));
        assertEquals(0, countCriminalActivities(grid));
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
        assertFalse(crimeManager.placeCriminalActivity(20));
        assertTrue(grid.getCell(10, 11).isEmpty());
    }


    @Test
    void policeRemovesTerroristicGroupOnlyAfterFiveTicks()
    {
        Grid grid = createGridWithPoweredPoliceStation();

        TerroristicGroup terroristicGroup = new TerroristicGroup();
        grid.restoreConstruction(terroristicGroup, 10, 10);

        City city = new City(grid, new StandardPolicy());
        CrimeManager crimeManager = new CrimeManager(city, grid);

        for (int i = 0; i < 4; i++)
        {
            terroristicGroup.updateOfOneTick();
        }

        assertEquals(0, crimeManager.tryToDestroyCriminalActivities(4));
        assertFalse(grid.getCell(10, 10).isEmpty());

        terroristicGroup.updateOfOneTick();

        assertEquals(1, crimeManager.tryToDestroyCriminalActivities(5));
        assertEquals(0, crimeManager.getRemovedCriminalActivities());
        assertEquals(1, crimeManager.getRemovedTerroristicGroups());
        assertTrue(grid.getCell(10, 10).isEmpty());
    }

    @Test
    void onePoliceStationRemovesOnlyOneThreat()
    {
        Grid grid =
                createGridWithPoweredPoliceStation();

        CriminalActivity criminalActivity =
                new CriminalActivity();

        criminalActivity.setCreationTick(0);

        grid.restoreConstruction(
                criminalActivity,
                10,
                10
        );
        TerroristicGroup terroristicGroup =
                new TerroristicGroup();
        terroristicGroup.setLifetimeTicks(5);
        grid.restoreConstruction(
                terroristicGroup,
                10,
                11
        );

        City city =
                new City(
                        grid,
                        new StandardPolicy()
                );

        CrimeManager crimeManager =
                new CrimeManager(city, grid);

        assertEquals(
                1,
                crimeManager
                        .tryToDestroyCriminalActivities(15)
        );

        // Nello stesso tick la stazione è in cooldown:
        // non può rimuovere anche l'altra minaccia.
        assertEquals(
                0,
                crimeManager
                        .tryToDestroyCriminalActivities(15)
        );
        assertEquals(
                1,
                countThreats(grid)
        );
    }

    @Test
    void policeWaitsFifteenTicksBetweenRemovals()
    {
        Grid grid = createGridWithPoweredPoliceStation();

        CriminalActivity first = new CriminalActivity();
        first.setCreationTick(50);
        CriminalActivity second = new CriminalActivity();
        second.setCreationTick(50);
        grid.restoreConstruction(first, 10, 10);
        grid.restoreConstruction(second, 10, 11);

        CrimeManager crimeManager = new CrimeManager(
                new City(grid, new StandardPolicy()), grid
        );

        assertEquals(1, crimeManager.tryToDestroyCriminalActivities(60));
        assertEquals(0, crimeManager.tryToDestroyCriminalActivities(74));
        assertEquals(1, countThreats(grid));
        assertEquals(1, crimeManager.tryToDestroyCriminalActivities(75));
        assertEquals(0, countThreats(grid));
    }

    private Grid createGridWithPoweredPoliceStation()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(
                new PowerPlant(),
                5,
                5
        );
        grid.restoreConstruction(
                new PoliceStation(),
                5,
                6
        );

        grid.rebuildConnectionsAfterLoad();

        return grid;
    }

    private int countCriminalActivities(
            Grid grid)
    {
        int count = 0;

        for (Construction construction
                : grid.getConstructions())
        {
            if (construction
                    instanceof CriminalActivity)
            {
                count++;
            }
        }

        return count;
    }

    private int countThreats(Grid grid)
    {
        int count = 0;

        for (Construction construction
                : grid.getConstructions())
        {
            if (construction
                    instanceof CriminalActivity
                    || construction
                    instanceof TerroristicGroup)
            {
                count++;
            }
        }

        return count;
    }
}
