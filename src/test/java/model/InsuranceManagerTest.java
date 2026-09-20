package model;

import org.junit.jupiter.api.Test;
import policies.StandardPolicy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceManagerTest
{
    @Test
    void newRiskBuildingIsCoveredAndAreaStaysBlocked()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(
                new Bank(),
                10,
                10
        );

        Residential initialResidential =
                new Residential();

        grid.restoreConstruction(
                initialResidential,
                0,
                10
        );

        City city =
                new City(
                        grid,
                        new StandardPolicy()
                );

        InsuranceManager insuranceManager =
                new InsuranceManager(
                        city,
                        grid
                );

        assertTrue(
                insuranceManager
                        .buyTsunamiInsurance()
        );

        Residential newResidential =
                new Residential();

        grid.restoreConstruction(
                newResidential,
                1,
                10
        );

        int extraCost =
                insuranceManager
                        .getAdditionalCoverageCost(
                                ConstructionType.RESIDENTIAL,
                                1,
                                10
                        );

        insuranceManager.coverNewConstruction(
                newResidential,
                1,
                10,
                extraCost
        );

        grid.removeConstruction(
                1,
                10
        );

        insuranceManager.registerTsunamiDamage(
                List.of(
                        new ReconstructionEntry(
                                newResidential,
                                1,
                                10
                        )
                ),
                "UP"
        );

        assertThrows(
                IllegalStateException.class,
                () -> grid.placeConstruction(
                        new Road(),
                        2,
                        5
                )
        );

        insuranceManager
                .startTsunamiReconstruction();

        insuranceManager
                .updateReconstruction();

        assertSame(
                newResidential,
                grid.getCell(
                        1,
                        10
                ).getConstruction()
        );

        assertDoesNotThrow(
                () -> grid.placeConstruction(
                        new Road(),
                        2,
                        5
                )
        );
    }
}
