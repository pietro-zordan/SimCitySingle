package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PowerPlantTest
{
    private PowerPlant powerPlant;

    @BeforeEach
    void setUp()
    {
        powerPlant = new PowerPlant();
    }

    @Test
    void initialValuesAreCorrect()
    {
        assertTrue(powerPlant.isActive());
        assertTrue(powerPlant.isPowered());

        assertEquals(
                6000,
                powerPlant.getPowerGenerated()
        );

        assertEquals(
                6000,
                powerPlant.getPowerCapacity()
        );

        assertEquals(
                200,
                powerPlant.getPollutionImpact()
        );

        assertEquals(
                -800,
                powerPlant.getPlacementPrice()
        );

        assertEquals(
                0,
                powerPlant.getPowerConsumption()
        );

        assertFalse(
                powerPlant.requiresPower()
        );

        assertEquals(
                ConstructionType.POWER_PLANT,
                powerPlant.getType()
        );
    }

    @Test
    void suspensionStopsProduction()
    {
        powerPlant.suspend();

        assertFalse(
                powerPlant.isActive()
        );

        assertFalse(
                powerPlant.isPowered()
        );

        assertEquals(
                0,
                powerPlant.getPowerGenerated()
        );

        assertEquals(
                0,
                powerPlant.getPollutionImpact()
        );
    }

    @Test
    void resumeRestoresProduction()
    {
        powerPlant.suspend();
        powerPlant.resume();

        assertTrue(
                powerPlant.isActive()
        );

        assertTrue(
                powerPlant.isPowered()
        );

        assertEquals(
                6000,
                powerPlant.getPowerGenerated()
        );
    }
}
