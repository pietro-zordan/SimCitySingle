package Events;

import model.City;
import model.Construction;
import model.EconomyBoostable;
import model.Grid;
import model.ConstructionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EconomicBoomTest {

    private Grid grid;
    private City city;
    private EconomicBoom economicBoom;

    private static class TestBoostableBuilding extends Construction implements EconomyBoostable {
        private double boost = 0.0;

        public TestBoostableBuilding() {
            super(10, 10, 100);
        }

        @Override
        public ConstructionType getType() {
            return ConstructionType.COMMERCIAL;
        }

        @Override
        public void applyEconomicBoost(double boost) {
            this.boost = boost;
        }

        @Override
        public void removeEconomicBoost() {
            this.boost = 0.0;
        }

        public double getBoost() {
            return boost;
        }
    }

    @BeforeEach
    void setUp() {
        grid = new Grid();
        city = new City(grid, policies.PolicyFactory.create(policies.PolicyType.STANDARD));
        economicBoom = new EconomicBoom(city, grid);
    }

    @Test
    void testEconomicBoomStartAppliesBoost() {
        TestBoostableBuilding building = new TestBoostableBuilding();
        grid.restoreConstruction(building, 0, 0);

        economicBoom.start();

        assertTrue(economicBoom.isBoosted(building));
        assertTrue(building.getBoost() >= 0.3 && building.getBoost() <= 0.8);
    }

    @Test
    void testEconomicBoomEndRemovesBoost() {
        TestBoostableBuilding building = new TestBoostableBuilding();
        grid.restoreConstruction(building, 0, 0);

        economicBoom.start();
        assertTrue(economicBoom.isBoosted(building));

        economicBoom.end();
        assertEquals(0.0, building.getBoost());
        assertFalse(economicBoom.isBoosted(building));
    }

    @Test
    void testUpdateOfOneTickBoostsNewBuildings() {
        economicBoom.start();

        TestBoostableBuilding lateBuilding = new TestBoostableBuilding();
        grid.restoreConstruction(lateBuilding, 1, 1);

        assertFalse(economicBoom.isBoosted(lateBuilding));

        economicBoom.updateOfOneTick();

        assertTrue(economicBoom.isBoosted(lateBuilding));
        assertTrue(lateBuilding.getBoost() > 0);
    }
}