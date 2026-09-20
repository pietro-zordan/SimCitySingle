package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import policies.Policy;
import policies.PolicyType;

import static org.junit.jupiter.api.Assertions.*;

class CityTest {

    private Grid grid;
    private Policy defaultPolicy;
    private City city;

    private static class DummyPolicy implements Policy {
        private final int cost;
        private final int economyBonus;

        public DummyPolicy(int cost, int economyBonus) {
            this.cost = cost;
            this.economyBonus = economyBonus;
        }

        @Override
        public int modifyPollution(int pollution) {
            return pollution;
        }

        @Override
        public int modifyEconomy(int economy) {
            return economy + economyBonus;
        }

        @Override
        public int calculatePlacementCost(Construction construction) {
            return cost; // Restituisce il costo configurato (es. -500)
        }

        @Override
        public PolicyType getType() {
            return null;
        }

        @Override
        public String getName() {
            return "";
        }
    }

    @BeforeEach
    void setUp() {
        grid = new Grid();
        defaultPolicy = new DummyPolicy(-500, 0);
        city = new City(grid, defaultPolicy, 2500);
    }

    @Test
    void testConstructorsAndInitialBudget() {

        City customCity = new City(grid, defaultPolicy, 3000);
        assertEquals(3000, customCity.getBudget());
        assertEquals(defaultPolicy, customCity.getCurrentPolicy());

        City defaultCity = new City(grid, defaultPolicy);
        assertEquals(2500, defaultCity.getBudget());

        assertThrows(IllegalArgumentException.class, () -> new City(null, defaultPolicy, 1000));
        assertThrows(IllegalArgumentException.class, () -> new City(grid, null, 1000));
        City indebtedCity = new City(grid, defaultPolicy, -10);
        assertEquals(-10, indebtedCity.getBudget());
    }

    @Test
    void testUpdateBudgetPositiveAndNegative() {

        city.updateBudget(500);
        assertEquals(3000, city.getBudget());

        city.updateBudget(-1000);
        assertEquals(2000, city.getBudget());
    }

    @Test
    void testUpdateBudgetInsufficientThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> city.updateBudget(-3000));

        assertEquals(2500, city.getBudget());
    }

    @Test
    void testDecreaseGlobalHappiness() {
        assertEquals(0, city.getGlobalHappiness());
        city.decreaseGlobalHappiness(15);
        assertEquals(-15, city.getGlobalHappiness());
    }

    @Test
    void testSetPolicyValidAndNull() {
        assertEquals(defaultPolicy, city.getCurrentPolicy());

        Policy newPolicy = new DummyPolicy(-200, 50);
        city.setPolicy(newPolicy);
        assertEquals(newPolicy, city.getCurrentPolicy());

        city.setPolicy(null);
        assertEquals(newPolicy, city.getCurrentPolicy());
    }

    @Test
    void testUpdateOfOneTickAddsEconomyOnce() {
        int initialBudget = city.getBudget();
        int initialEconomy = city.getGlobalEconomy();

        city.updateOfOneTick();

        assertEquals(initialBudget + initialEconomy, city.getBudget());
    }

    @Test
    void unemploymentNeverBecomesNegative()
    {
        Grid employmentGrid =
                new Grid();

        employmentGrid.restoreConstruction(
                new Residential(),
                5,
                5
        );

        employmentGrid.restoreConstruction(
                new Industrial(),
                5,
                6
        );

        City employmentCity =
                new City(
                        employmentGrid,
                        defaultPolicy,
                        2500
                );

        assertEquals(
                0,
                employmentCity
                        .getGlobalUnemployed()
        );
    }

    @Test
    void testPlaceConstructionNullOrInsufficientBudget()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> city.placeConstruction(
                        null,
                        0,
                        0
                )
        );

        City poorCity =
                new City(
                        grid,
                        new DummyPolicy(-500, 0),
                        10
                );

        Construction residential =
                new Residential();

        assertThrows(
                IllegalStateException.class,
                () -> poorCity.placeConstruction(
                        residential,
                        0,
                        0
                )
        );

        assertEquals(
                10,
                poorCity.getBudget()
        );
    }
}