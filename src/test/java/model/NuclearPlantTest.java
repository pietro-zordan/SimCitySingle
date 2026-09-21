package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NuclearPlantTest {

    @Test
    void removingNuclearPlantDestroysBuildingsInNineByNineArea() {
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
    void nuclearExplosionCanTriggerAnotherNuclearPlant() {
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
