package Events;

import model.City;
import model.Grid;
import model.NuclearPlant;
import model.Construction;
import model.ConstructionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireTest {

    private Grid grid;
    private City city;
    private Fire fire;

    private static class TestBuilding extends Construction {
        public TestBuilding() {
            super(10, 10, 100);
        }

        @Override
        public ConstructionType getType() {
            return ConstructionType.RESIDENTIAL;
        }
    }

    @BeforeEach
    void setUp() {
        grid = new Grid();
        city = new City(grid, policies.PolicyFactory.create(policies.PolicyType.STANDARD));
        fire = new Fire(city, grid);
    }

    @Test
    void testCanStartFalseWithoutBurnableCellsEvenWithCorrectProbability() {
        fire.setProbability(12);

        assertFalse(fire.canStart());
    }

    @Test
    void testCanStartTrueWithBurnableCellsAndCorrectProbability() {
        TestBuilding building = new TestBuilding();
        grid.restoreConstruction(building, 5, 5);

        fire.setProbability(12);

        assertTrue(fire.canStart());
    }

    @Test
    void testCanStartFalseWithWrongProbabilityEvenWithBurnableCells() {
        TestBuilding building = new TestBuilding();
        grid.restoreConstruction(building, 5, 5);

        fire.setProbability(0);

        assertFalse(fire.canStart());
    }

    @Test
    void protectedNuclearPlantCannotCatchFire() {
        NuclearPlant nuclearPlant = new NuclearPlant();
        nuclearPlant.setFireProtected(true);
        grid.restoreConstruction(nuclearPlant, 5, 5);

        fire.setProbability(12);

        assertFalse(fire.canStart());

        fire.burn(grid.getCell(5, 5));

        assertSame(nuclearPlant, grid.getCell(5, 5).getConstruction());
    }

    @Test
    void testStartAndFirePropagation() {
        TestBuilding building = new TestBuilding();
        grid.restoreConstruction(building, 5, 5);

        fire.start();

        assertTrue(fire.isCellOnFire(5, 5));
        assertFalse(fire.isFinished(1));

        int initialHappiness = city.getGlobalHappiness();
        fire.updateOfOneTick();

        assertTrue(grid.getCell(5, 5).isEmpty());
        assertEquals(initialHappiness - 5, city.getGlobalHappiness());
    }
}