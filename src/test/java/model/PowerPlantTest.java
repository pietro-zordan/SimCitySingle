package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PowerPlantTest {

    private Grid grid;
    private PowerPlant powerPlant;

    @BeforeEach
    void setUp() {
        grid = new Grid();
        powerPlant = new PowerPlant();

        grid.restoreConstruction(
                powerPlant,
                10,
                10
        );
    }

    @Test
    void initialValuesAreCorrect() {
        assertTrue(powerPlant.isActive());
        assertTrue(powerPlant.isPowered());

        assertEquals(10000, powerPlant.getPowerGenerated());
        assertEquals(200, powerPlant.getPollutionImpact());
        assertEquals(-800, powerPlant.getPlacementPrice());
        assertEquals(0, powerPlant.getPowerConsumption());

        assertFalse(powerPlant.requiresPower());

        assertEquals(
                ConstructionType.POWER_PLANT,
                powerPlant.getType()
        );
    }

    @Test
    void nearbyPowerConsumersAreConnected() {
        Residential residential = new Residential();
        Park park = new Park();

        grid.restoreConstruction(
                residential,
                10,
                11
        );

        grid.restoreConstruction(
                park,
                10,
                12
        );

        powerPlant.serveConstruction();

        assertTrue(residential.isPowerPlantConnected());
        assertSame(
                powerPlant,
                residential.getConnectedPowerPlant()
        );

        assertFalse(park.isPowerPlantConnected());
    }

    @Test
    void constructionsOutsideAreaAreNotConnected() {
        Residential nearbyResidential = new Residential();
        Residential distantResidential = new Residential();

        grid.restoreConstruction(
                nearbyResidential,
                13,
                13
        );

        grid.restoreConstruction(
                distantResidential,
                14,
                10
        );

        powerPlant.serveConstruction();

        assertTrue(
                nearbyResidential.isPowerPlantConnected()
        );

        assertFalse(
                distantResidential.isPowerPlantConnected()
        );
    }

    @Test
    void suspensionStopsProductionAndConnections() {
        Residential residential = new Residential();

        grid.restoreConstruction(
                residential,
                10,
                11
        );

        powerPlant.suspend();
        powerPlant.updateOfOneTick();

        assertFalse(powerPlant.isActive());
        assertFalse(powerPlant.isPowered());
        assertEquals(0, powerPlant.getPowerGenerated());
        assertEquals(0, powerPlant.getPollutionImpact());
        assertFalse(residential.isPowerPlantConnected());

        powerPlant.resume();
        powerPlant.updateOfOneTick();

        assertTrue(powerPlant.isActive());
        assertEquals(10000, powerPlant.getPowerGenerated());
        assertTrue(residential.isPowerPlantConnected());
    }

    @Test
    void removeConstructionDisconnectsBuilding() {
        Residential residential = new Residential();

        grid.restoreConstruction(
                residential,
                10,
                11
        );

        powerPlant.serveConstruction();

        assertTrue(residential.isPowerPlantConnected());

        powerPlant.removeConstruction(residential);

        assertFalse(residential.isPowerPlantConnected());
        assertNull(residential.getConnectedPowerPlant());
    }

    @Test
    void disconnectAllDisconnectsEveryBuilding() {
        Residential residential = new Residential();
        Commercial commercial = new Commercial();

        grid.restoreConstruction(
                residential,
                10,
                11
        );

        grid.restoreConstruction(
                commercial,
                11,
                10
        );

        powerPlant.serveConstruction();

        assertTrue(residential.isPowerPlantConnected());
        assertTrue(commercial.isPowerPlantConnected());

        powerPlant.disconnectAll();

        assertFalse(residential.isPowerPlantConnected());
        assertFalse(commercial.isPowerPlantConnected());
    }

    @Test
    void generatedPowerLimitsConnections() {
        List<Industrial> industrials =
                createIndustrials(11);

        powerPlant.serveConstruction();

        int connectedConstructions = 0;
        int usedPower = 0;

        for (Industrial industrial : industrials) {
            if (industrial.isPowerPlantConnected()) {
                connectedConstructions++;
                usedPower += industrial.getPowerConsumption();
            }
        }

        assertEquals(10, connectedConstructions);
        assertEquals(10000, usedPower);
    }

    @Test
    void lowerConsumptionCandidateReconnectsFirst() {
        Residential residential = new Residential();

        grid.restoreConstruction(
                residential,
                7,
                7
        );

        List<Industrial> industrials =
                createIndustrials(11);

        powerPlant.serveConstruction();

        Industrial disconnectedIndustrial = null;
        Industrial connectedIndustrial = null;

        for (Industrial industrial : industrials) {
            if (industrial.isPowerPlantConnected()) {
                connectedIndustrial = industrial;
            } else {
                disconnectedIndustrial = industrial;
            }
        }

        assertFalse(residential.isPowerPlantConnected());
        assertNotNull(disconnectedIndustrial);
        assertNotNull(connectedIndustrial);

        powerPlant.removeConstruction(
                connectedIndustrial
        );

        powerPlant.reconnect();

        assertTrue(residential.isPowerPlantConnected());
        assertFalse(
                disconnectedIndustrial.isPowerPlantConnected()
        );
    }

    private List<Industrial> createIndustrials(int number) {
        List<Industrial> industrials =
                new ArrayList<>();

        for (int row = 7;
             row <= 13 && industrials.size() < number;
             row++) {

            for (int column = 7;
                 column <= 13 && industrials.size() < number;
                 column++) {

                if (grid.getCell(row, column).isEmpty()) {
                    Industrial industrial =
                            new Industrial();

                    grid.restoreConstruction(
                            industrial,
                            row,
                            column
                    );

                    industrials.add(industrial);
                }
            }
        }

        return industrials;
    }
}