package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResidentialTest {

    private Residential residential;

    @BeforeEach
    void setUp() {
        residential = new Residential();
    }

    @Test
    void initialValuesAreCorrect() {
        assertEquals(10, residential.getPopulation());
        assertEquals(1, residential.getPopulationGrowthRate());
        assertEquals(1, residential.getPopulationDecreaseRate());
        assertEquals(100, residential.getPowerConsumption());
        assertEquals(-200, residential.getPlacementPrice());
        assertEquals(0, residential.getMoneyProduction());

        assertTrue(residential.requiresPower());
        assertFalse(residential.isPowerPlantConnected());
        assertFalse(residential.hasNoPopulation());
        assertFalse(residential.mustBeRemoved());
    }

    @Test
    void populationDecreasesWithoutPower() {
        residential.updateOfOneTick();

        assertEquals(9, residential.getPopulation());
        assertEquals(2, residential.getPopulationDecreaseRate());
        assertEquals(1, residential.getPopulationGrowthRate());
    }

    @Test
    void populationIncreasesWithPower() {
        PowerPlant powerPlant = mock(PowerPlant.class);
        when(powerPlant.isActive()).thenReturn(true);

        residential.connectToPowerPlant(powerPlant);
        residential.updateOfOneTick();

        assertEquals(11, residential.getPopulation());
        assertEquals(2, residential.getPopulationGrowthRate());
        assertEquals(1, residential.getPopulationDecreaseRate());

        assertTrue(residential.isPowerPlantConnected());
        assertSame(
                powerPlant,
                residential.getConnectedPowerPlant()
        );
    }

    @Test
    void populationDecreasesWithInactivePowerPlant() {
        PowerPlant powerPlant = mock(PowerPlant.class);
        when(powerPlant.isActive()).thenReturn(false);

        residential.connectToPowerPlant(powerPlant);
        residential.updateOfOneTick();

        assertEquals(9, residential.getPopulation());
        assertTrue(residential.isPowerPlantConnected());
        assertFalse(residential.isPowered());
    }

    @Test
    void populationRespectsMaximumValues() {
        PowerPlant powerPlant = mock(PowerPlant.class);
        when(powerPlant.isActive()).thenReturn(true);

        residential.connectToPowerPlant(powerPlant);

        for (int tick = 0; tick < 20; tick++) {
            residential.updateOfOneTick();
        }

        assertEquals(50, residential.getPopulation());
        assertEquals(5, residential.getPopulationGrowthRate());
    }

    @Test
    void zeroPopulationRequiresRemoval() {
        for (int tick = 0; tick < 20; tick++) {
            residential.updateOfOneTick();
        }

        assertEquals(0, residential.getPopulation());
        assertEquals(5, residential.getPopulationDecreaseRate());

        assertTrue(residential.hasNoPopulation());
        assertTrue(residential.mustBeRemoved());
    }

    @Test
    void typeIsResidential() {
        assertEquals(
                ConstructionType.RESIDENTIAL,
                residential.getType()
        );
    }

    @Test
    void restoreStateRestoresValues() {
        residential.restoreState(
                37,
                4,
                3,
                0,
                0
        );

        assertEquals(37, residential.getPopulation());
        assertEquals(4, residential.getPopulationGrowthRate());
        assertEquals(3, residential.getPopulationDecreaseRate());
    }
}