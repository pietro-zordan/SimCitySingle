package policies;

import model.Construction;
import model.ConstructionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolicyTest {

    private static class DummyConstruction extends Construction {
        private final ConstructionType type;

        public DummyConstruction(ConstructionType type, int placementPrice) {
            super(0, 0, placementPrice);
            this.type = type;
        }

        @Override
        public ConstructionType getType() {
            return type;
        }


        @Override
        public int getPlacementPrice() {
            return super.getPlacementPrice();
        }
    }

    @Test
    void testStandardPolicy() {
        Policy policy = new StandardPolicy();
        assertEquals("Standard policy", policy.getName());
        assertEquals(PolicyType.STANDARD, policy.getType());

        Construction c = new DummyConstruction(ConstructionType.INDUSTRIAL, -200);
        assertEquals(-200, policy.calculatePlacementCost(c));

        assertEquals(100, policy.modifyPollution(100));
        assertEquals(200, policy.modifyEconomy(200));
    }

    @Test
    void testEnvironmentalPolicy() {
        Policy policy = new EnvironmentalPolicy();
        assertEquals("Environmental policy", policy.getName());
        assertEquals(PolicyType.ENVIRONMENTAL, policy.getType());

        Construction ind = new DummyConstruction(ConstructionType.INDUSTRIAL, -200);
        assertEquals(-300, policy.calculatePlacementCost(ind));

        Construction park = new DummyConstruction(ConstructionType.PARK, -30);
        assertEquals(0, policy.calculatePlacementCost(park));

        Construction res = new DummyConstruction(ConstructionType.RESIDENTIAL, -100);
        assertEquals(-100, policy.calculatePlacementCost(res));

        assertEquals(75, policy.modifyPollution(100));
        assertEquals(75, policy.modifyEconomy(100));
    }

    @Test
    void testIndustrialPolicy() {
        Policy policy = new IndustrialPolicy();
        assertEquals("Industrial policy", policy.getName());
        assertEquals(PolicyType.INDUSTRIAL, policy.getType());

        Construction ind = new DummyConstruction(ConstructionType.INDUSTRIAL, -50);
        assertEquals(0, policy.calculatePlacementCost(ind));

        Construction park = new DummyConstruction(ConstructionType.PARK, -50);
        assertEquals(-100, policy.calculatePlacementCost(park));

        Construction res = new DummyConstruction(ConstructionType.RESIDENTIAL, -100);
        assertEquals(-100, policy.calculatePlacementCost(res));

        assertEquals(125, policy.modifyPollution(100));
        assertEquals(125, policy.modifyEconomy(100));
    }
}