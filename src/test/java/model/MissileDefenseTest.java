package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MissileDefenseTest
{
    @Test
    void absorbsThreeMissilesThenStops()
    {
        MissileDefense defense =
                new MissileDefense();

        assertTrue(defense.purchase());

        assertTrue(defense.absorbMissile());
        assertTrue(defense.absorbMissile());
        assertTrue(defense.absorbMissile());

        assertEquals(
                0,
                defense.getHitsRemaining()
        );

        assertFalse(defense.isActive());
        assertFalse(defense.absorbMissile());
    }

    @Test
    void repairFromZeroReactivatesDefense()
    {
        MissileDefense defense =
                new MissileDefense();

        defense.purchase();
        defense.absorbMissile();
        defense.absorbMissile();
        defense.absorbMissile();

        assertTrue(defense.repairOneHit());
        assertEquals(
                1,
                defense.getHitsRemaining()
        );
        assertTrue(defense.isActive());
    }

    @Test
    void cannotRepairBeyondMaximum()
    {
        MissileDefense defense =
                new MissileDefense();

        defense.purchase();

        assertFalse(defense.repairOneHit());
        assertEquals(
                MissileDefense.MAX_HITS,
                defense.getHitsRemaining()
        );
    }
}
