package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NuclearPlantTest {

    @Test
    void removingNuclearPlantDestroysBuildingsInNineByNineArea()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(new NuclearPlant(), 10, 10);
        grid.restoreConstruction(new Park(), 6, 6);
        grid.restoreConstruction(new Park(), 14, 14);
        grid.restoreConstruction(new Park(), 15, 15);
        grid.restoreConstruction(new Road(), 10, 11);

        grid.removeConstruction(10, 10);

        assertTrue(grid.getCell(10, 10).isEmpty());
        assertTrue(grid.getCell(6, 6).isEmpty());
        assertTrue(grid.getCell(14, 14).isEmpty());
        assertFalse(grid.getCell(15, 15).isEmpty());
        assertFalse(grid.getCell(10, 11).isEmpty());
    }

    @Test
    void explosionWorksNearGridBorder()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(new NuclearPlant(), 1, 1);
        grid.restoreConstruction(new Park(), 0, 0);
        grid.restoreConstruction(new Park(), 5, 5);
        grid.restoreConstruction(new Park(), 6, 6);

        grid.removeConstruction(1, 1);

        assertTrue(grid.getCell(0, 0).isEmpty());
        assertTrue(grid.getCell(5, 5).isEmpty());
        assertFalse(grid.getCell(6, 6).isEmpty());
    }

    @Test
    void explosionInfoCanBeConsumedOnlyOnce()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(new NuclearPlant(), 10, 10);
        grid.removeConstruction(10, 10);

        assertFalse(grid.consumeExplosions().isEmpty());
        assertTrue(grid.consumeExplosions().isEmpty());
    }

    @Test
    void nuclearExplosionTriggersChainReaction()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(new NuclearPlant(), 10, 10);
        grid.restoreConstruction(new NuclearPlant(), 14, 10);
        grid.restoreConstruction(new Park(), 18, 10);

        grid.removeConstruction(10, 10);

        assertTrue(grid.getCell(10, 10).isEmpty());
        assertTrue(grid.getCell(14, 10).isEmpty());
        assertTrue(grid.getCell(18, 10).isEmpty());
    }
}
