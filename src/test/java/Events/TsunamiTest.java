package Events;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TsunamiTest {

    private Grid grid;
    private City city;
    private Tsunami tsunami;

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
        tsunami = new Tsunami(city, grid);
    }

    @Test
    void testTsunamiStartDestroysBuildingsAndUpdatesStats() {
        TestBuilding building = new TestBuilding();
        grid.restoreConstruction(building, 0, 0);

        tsunami.start();

        assertNotNull(tsunami.getDirection());
        assertEquals(4, tsunami.getAdvancementLength());
        assertEquals(EventType.TSUNAMI, tsunami.getType());
    }

    @Test
    void testUpdateOfOneTickDecreasesHappiness() {
        int initialHappiness = city.getGlobalHappiness();

        tsunami.updateOfOneTick();

        assertEquals(initialHappiness - 50, city.getGlobalHappiness());
    }
}