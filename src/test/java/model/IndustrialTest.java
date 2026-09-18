package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndustrialTest {

    private Industrial industrial;
    private PowerPlant powerPlant;

    @BeforeEach
    void setUp() {
        industrial = new Industrial();
        powerPlant = mock(PowerPlant.class);
    }

    @Test
    void initialValuesAreCorrect() {
        assertEquals(0, industrial.getMoneyProduction());
        assertEquals(
                1.0,
                industrial.getEconomyGrowthRate(),
                0.0001
        );

        assertEquals(1000, industrial.getPowerConsumption());
        assertEquals(-600, industrial.getPlacementPrice());
        assertEquals(100, industrial.getNumberOfEmployee());
        assertEquals(
                5.0,
                industrial.getMaxEconomyGrowthRate(),
                0.0001
        );

        assertFalse(industrial.isPowered());
        assertEquals(0, industrial.getPollutionImpact());
        assertEquals(0, industrial.getHappinessImpact());
    }

    @Test
    void moneyAndImpactsAreProducedWithPower() {
        connectToActivePowerPlant();

        industrial.updateOfOneTick();

        assertEquals(150, industrial.getMoneyProduction());
        assertEquals(
                1.1,
                industrial.getEconomyGrowthRate(),
                0.0001
        );

        assertEquals(600, industrial.getPollutionImpact());
        assertEquals(-200, industrial.getHappinessImpact());
        assertTrue(industrial.isPowered());
    }

    @Test
    void noMoneyOrImpactsAreProducedWithoutPower() {
        industrial.updateOfOneTick();

        assertEquals(0, industrial.getMoneyProduction());
        assertEquals(0, industrial.getPollutionImpact());
        assertEquals(0, industrial.getHappinessImpact());

        assertEquals(
                1.0,
                industrial.getEconomyGrowthRate(),
                0.0001
        );
    }

    @Test
    void growthRateRespectsMaximum() {
        connectToActivePowerPlant();

        for (int tick = 0; tick < 50; tick++) {
            industrial.updateOfOneTick();
        }

        assertEquals(
                5.0,
                industrial.getEconomyGrowthRate(),
                0.0001
        );

        assertEquals(750, industrial.getMoneyProduction());
    }

    @Test
    void typeIsIndustrial() {
        assertEquals(
                ConstructionType.INDUSTRIAL,
                industrial.getType()
        );
    }

    @Test
    void restoreStateRestoresValues() {
        industrial.restoreState(
                0,
                0,
                0,
                4.2,
                800
        );

        assertEquals(
                4.2,
                industrial.getEconomyGrowthRate(),
                0.0001
        );

        assertEquals(800, industrial.getMoneyProduction());
    }

    private void connectToActivePowerPlant() {
        when(powerPlant.isActive()).thenReturn(true);
        industrial.connectToPowerPlant(powerPlant);
    }
}