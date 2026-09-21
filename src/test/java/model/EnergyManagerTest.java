package model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnergyManagerTest
{
    @Test
    void nearbyConsumerIsConnectedAfterRebuild()
    {
        Grid grid =
                new Grid();

        PowerPlant powerPlant =
                new PowerPlant();

        Residential residential =
                new Residential();

        grid.restoreConstruction(
                powerPlant,
                10,
                10
        );

        grid.restoreConstruction(
                residential,
                10,
                11
        );

        grid.rebuildConnectionsAfterLoad();

        assertTrue(
                residential.isPowerPlantConnected()
        );

        assertSame(
                powerPlant,
                residential.getConnectedPowerPlant()
        );
    }

    @Test
    void consumerOutsideAreaIsNotConnected()
    {
        Grid grid =
                new Grid();

        PowerPlant powerPlant =
                new PowerPlant();

        Residential residential =
                new Residential();

        grid.restoreConstruction(
                powerPlant,
                10,
                10
        );

        grid.restoreConstruction(
                residential,
                14,
                10
        );

        grid.rebuildConnectionsAfterLoad();

        assertFalse(
                residential.isPowerPlantConnected()
        );
    }

    @Test
    void powerPlantCapacityIsRespected()
    {
        Grid grid =
                new Grid();

        PowerPlant powerPlant =
                new PowerPlant();

        grid.restoreConstruction(
                powerPlant,
                10,
                10
        );

        List<Industrial> industrials =
                createIndustrials(
                        grid,
                        7
                );

        grid.rebuildConnectionsAfterLoad();

        int connectedConstructions = 0;

        for (Industrial industrial
                : industrials)
        {
            if (industrial
                    .isPowerPlantConnected())
            {
                connectedConstructions++;
            }
        }

        assertEquals(
                6,
                connectedConstructions
        );

        assertEquals(
                6000,
                grid.getPowerPlantUsedPower(
                        powerPlant
                )
        );

        assertEquals(
                7000,
                grid.getTotalEnergyDemand()
        );
    }

    @Test
    void maxEnergyServedIsAvailableFromGrid()
    {
        Grid grid =
                new Grid();

        assertEquals(
                24000,
                grid.getMaxEnergyServed()
        );
    }

    private List<Industrial> createIndustrials(
            Grid grid,
            int number)
    {
        List<Industrial> industrials =
                new ArrayList<>();

        for (int row = 7;
             row <= 13
                     && industrials.size()
                     < number;
             row++)
        {
            for (int column = 7;
                 column <= 13
                         && industrials.size()
                         < number;
                 column++)
            {
                if (grid.getCell(
                        row,
                        column
                ).isEmpty())
                {
                    Industrial industrial =
                            new Industrial();

                    grid.restoreConstruction(
                            industrial,
                            row,
                            column
                    );

                    industrials.add(
                            industrial
                    );
                }
            }
        }

        return industrials;
    }
}
