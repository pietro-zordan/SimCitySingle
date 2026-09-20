package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmploymentPlacementTest
{
    @Test
    void commercialRequiresThirtyUnemployedCitizens()
    {
        Commercial commercial = new Commercial();

        assertFalse(commercial.isPlacementAllowed(0, 29));
        assertTrue(commercial.isPlacementAllowed(0, 30));
    }

    @Test
    void industrialCannotBePlacedWhenPollutionLimitIsReached()
    {
        Industrial industrial = new Industrial();

        assertTrue(industrial.isPlacementAllowed(1999, 100));
        assertFalse(industrial.isPlacementAllowed(2000, 100));
    }

    @Test
    void industrialRequiresOneHundredUnemployedCitizens()
    {
        Industrial industrial = new Industrial();

        assertFalse(industrial.isPlacementAllowed(0, 99));
        assertTrue(industrial.isPlacementAllowed(0, 100));
    }
}
