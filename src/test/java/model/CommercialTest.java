package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommercialTest {

    private Commercial commercial;
    private PowerPlant powerPlant;

    @BeforeEach
    void setUp() {
        commercial = new Commercial();
        powerPlant = mock(PowerPlant.class);
    }

    @Test
    void initialValuesAreCorrect() {
        assertEquals(0, commercial.getMoneyProduction());
        assertEquals(
                1.0,
                commercial.getEconomyGrowthRate(),
                0.0001
        );

        assertEquals(200, commercial.getPowerConsumption());
        assertEquals(-300, commercial.getPlacementPrice());
        assertEquals(30, commercial.getNumberOfEmployee());
        assertEquals(
                3.0,
                commercial.getMaxEconomyGrowthRate(),
                0.0001
        );

        assertFalse(commercial.isPowered());
        assertEquals(0, commercial.getPollutionImpact());
        assertEquals(0, commercial.getHappinessImpact());
    }

    @Test
    void moneyAndImpactsAreProducedWithPower() {
        connectToActivePowerPlant();

        commercial.updateOfOneTick();

        assertEquals(50, commercial.getMoneyProduction());
        assertEquals(
                1.1,
                commercial.getEconomyGrowthRate(),
                0.0001
        );

        assertEquals(150, commercial.getPollutionImpact());
        assertEquals(150, commercial.getHappinessImpact());
        assertTrue(commercial.isPowered());
    }

    @Test
    void noMoneyOrImpactsAreProducedWithoutPower() {
        commercial.updateOfOneTick();

        assertEquals(0, commercial.getMoneyProduction());
        assertEquals(0, commercial.getPollutionImpact());
        assertEquals(0, commercial.getHappinessImpact());

        assertEquals(
                1.0,
                commercial.getEconomyGrowthRate(),
                0.0001
        );
    }

    @Test
    void growthRateRespectsMaximum() {
        connectToActivePowerPlant();

        for (int tick = 0; tick < 30; tick++) {
            commercial.updateOfOneTick();
        }

        assertEquals(
                3.0,
                commercial.getEconomyGrowthRate(),
                0.0001
        );

        assertEquals(150, commercial.getMoneyProduction());
    }

    @Test
    void economicBoostIncreasesProduction() {
        connectToActivePowerPlant();
        commercial.applyEconomicBoost(1.0);

        commercial.updateOfOneTick();

        assertEquals(100, commercial.getMoneyProduction());

        commercial.removeEconomicBoost();
        commercial.updateOfOneTick();

        assertEquals(55, commercial.getMoneyProduction());
    }

    @Test
    void negativeEconomicBoostThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> commercial.applyEconomicBoost(-1.0)
        );
    }

    @Test
    void typeIsCommercial() {
        assertEquals(
                ConstructionType.COMMERCIAL,
                commercial.getType()
        );
    }

    @Test
    void restoreStateRestoresValues() {
        commercial.restoreState(
                0,
                0,
                0,
                2.4,
                120
        );

        assertEquals(
                2.4,
                commercial.getEconomyGrowthRate(),
                0.0001
        );

        assertEquals(120, commercial.getMoneyProduction());
    }

    private void connectToActivePowerPlant() {
        when(powerPlant.isActive()).thenReturn(true);
        commercial.connectToPowerPlant(powerPlant);
    }
}