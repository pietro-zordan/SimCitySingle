package model;

import org.junit.jupiter.api.Test;
import policies.StandardPolicy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceManagerTest
{
    @Test
    void newBuildingRequiresManualInsuranceExtension()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(
                new Bank(),
                10,
                10
        );

        Residential firstResidential =
                new Residential();

        grid.restoreConstruction(
                firstResidential,
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

        // 80 euro meno il 5% dato dalla banca.
        assertEquals(
                76,
                insuranceManager
                        .getTsunamiInsuranceCost()
        );

        assertTrue(
                insuranceManager
                        .buyTsunamiInsurance()
        );

        assertTrue(
                firstResidential
                        .isTsunamiInsured()
        );

        // Appena applicata non c'è nulla di nuovo da assicurare.
        assertFalse(
                insuranceManager
                        .canBuyTsunamiInsurance()
        );

        Residential newResidential =
                new Residential();

        grid.restoreConstruction(
                newResidential,
                1,
                10
        );

        // La nuova costruzione NON viene coperta automaticamente.
        assertFalse(
                newResidential
                        .isTsunamiInsured()
        );

        // L'estensione diventa subito disponibile: nessun cooldown.
        assertTrue(
                insuranceManager
                        .canBuyTsunamiInsurance()
        );

        assertEquals(
                1,
                insuranceManager
                        .getTsunamiInsuranceBuildingCount()
        );

        assertEquals(
                76,
                insuranceManager
                        .getTsunamiInsuranceCost()
        );

        assertTrue(
                insuranceManager
                        .buyTsunamiInsurance()
        );

        assertTrue(
                newResidential
                        .isTsunamiInsured()
        );

        assertFalse(
                insuranceManager
                        .canBuyTsunamiInsurance()
        );
    }

    @Test
    void insuredDamageIsRebuiltAndAreaStaysBlocked()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(
                new Bank(),
                10,
                10
        );

        Residential residential =
                new Residential();

        grid.restoreConstruction(
                residential,
                1,
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

        grid.removeConstruction(
                1,
                10
        );

        insuranceManager.registerTsunamiDamage(
                List.of(
                        new ReconstructionEntry(
                                residential,
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

        assertFalse(
                insuranceManager
                        .wasReconstructionRestoredThisTick()
        );

        insuranceManager
                .updateReconstruction();

        assertTrue(
                insuranceManager
                        .wasReconstructionRestoredThisTick()
        );

        assertTrue(
                insuranceManager
                        .wasReconstructionCompletedThisTick()
        );

        assertSame(
                residential,
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

    @Test
    void uninsuredNewBuildingIsNotRebuilt()
    {
        Grid grid = new Grid();

        grid.restoreConstruction(
                new Bank(),
                10,
                10
        );

        Residential insuredResidential =
                new Residential();

        grid.restoreConstruction(
                insuredResidential,
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

        Residential uninsuredResidential =
                new Residential();

        grid.restoreConstruction(
                uninsuredResidential,
                1,
                10
        );

        grid.removeConstruction(
                1,
                10
        );

        insuranceManager.registerTsunamiDamage(
                List.of(
                        new ReconstructionEntry(
                                uninsuredResidential,
                                1,
                                10
                        )
                ),
                "UP"
        );

        insuranceManager
                .startTsunamiReconstruction();

        insuranceManager
                .updateReconstruction();

        assertNull(
                grid.getCell(
                        1,
                        10
                ).getConstruction()
        );
    }
    @Test
    void nuclearFireProtectionUsesBankDiscountAndProtectsPlants()
    {
        Grid grid = new Grid();

        for (int i = 0; i < 7; i++)
        {
            grid.restoreConstruction(new Bank(), 5, i);
        }

        NuclearPlant firstPlant = new NuclearPlant();
        NuclearPlant secondPlant = new NuclearPlant();

        grid.restoreConstruction(firstPlant, 10, 10);
        grid.restoreConstruction(secondPlant, 10, 11);

        City city = new City(grid, new StandardPolicy(), 5000);
        InsuranceManager insuranceManager = new InsuranceManager(city, grid);

        assertEquals(2, insuranceManager.getNuclearPlantsNeedingFireProtectionCount());
        assertEquals(30, insuranceManager.getBankDiscountPercentage());
        assertEquals(2800, insuranceManager.getNuclearFireProtectionCost());

        assertTrue(insuranceManager.buyNuclearFireProtection());
        assertTrue(firstPlant.isFireProtected());
        assertTrue(secondPlant.isFireProtected());
        assertEquals(2200, city.getBudget());
        assertFalse(insuranceManager.canBuyNuclearFireProtection());
    }

}
