package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EconomicBuildingTest {

    // Sottoclasse concreta di supporto per testare EconomicBuilding
    private static class TestEconomicBuilding extends EconomicBuilding {
        private boolean powered = true;

        public TestEconomicBuilding() {
            super(10, 100);
        }

        public void setPowered(boolean powered) {
            this.powered = powered;
        }

        @Override
        public boolean isPowered() {
            return powered;
        }

        @Override
        public ConstructionType getType() {
            return ConstructionType.COMMERCIAL;
        }

        @Override
        public double getMaxEconomyGrowthRate() {
            return 1.5;
        }

        @Override
        protected double getBaseMoneyProduction() {
            return 100.0;
        }

        @Override
        protected int getMaxMoneyProduction() {
            return 150;
        }
    }

    private TestEconomicBuilding building;

    @BeforeEach
    void setUp() {
        building = new TestEconomicBuilding();
    }

    @Test
    void testInitialValues() {
        assertEquals(1.0, building.getEconomyGrowthRate());
        assertEquals(0, building.getMoneyProduction());
    }

    @Test
    void testProgressiveGrowthTickByTickUntilCap() {
        building.updateOfOneTick();
        assertEquals(100, building.getMoneyProduction());
        assertEquals(1.1, building.getEconomyGrowthRate(), 0.001);

        building.updateOfOneTick();
        assertEquals(110, building.getMoneyProduction());
        assertEquals(1.2, building.getEconomyGrowthRate(), 0.001);

        building.updateOfOneTick();
        assertEquals(120, building.getMoneyProduction());
        assertEquals(1.3, building.getEconomyGrowthRate(), 0.001);

        building.updateOfOneTick();
        assertEquals(130, building.getMoneyProduction());
        assertEquals(1.4, building.getEconomyGrowthRate(), 0.001);

        building.updateOfOneTick();
        assertEquals(140, building.getMoneyProduction());
        assertEquals(1.5, building.getEconomyGrowthRate(), 0.001);

        building.updateOfOneTick();
        assertEquals(150, building.getMoneyProduction());
        assertEquals(1.5, building.getEconomyGrowthRate(), 0.001);
    }

    @Test
    void testNoProductionWhenNotPowered() {
        building.updateOfOneTick();
        assertTrue(building.getMoneyProduction() > 0);

        building.setPowered(false);
        building.updateOfOneTick();

        assertEquals(0, building.getMoneyProduction());
    }

    @Test
    void testEconomicBoostApplyAndRemove() {
        building.applyEconomicBoost(0.3);

        building.updateOfOneTick();
        assertEquals(130, building.getMoneyProduction());

        building.removeEconomicBoost();
        building.updateOfOneTick();

        assertEquals(110, building.getMoneyProduction());
    }

    @Test
    void testInvalidEconomicBoostThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> building.applyEconomicBoost(-0.5));
    }
}