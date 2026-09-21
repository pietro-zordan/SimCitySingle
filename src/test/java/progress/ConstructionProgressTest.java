package progress;

import model.Construction;
import model.ConstructionType;
import model.NuclearPlant;
import model.Residential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConstructionProgressTest {

    // Verifica che il costruttore conservi tutti i dati ricevuti.
    @Test
    void constructorStoresValues() {
        ConstructionProgress progress = new ConstructionProgress(
                ConstructionType.COMMERCIAL,
                7,
                13,
                21,
                2,
                3,
                1.75,
                90
        );

        assertEquals(ConstructionType.COMMERCIAL, progress.getType());
        assertEquals(7, progress.getRow());
        assertEquals(13, progress.getColumn());
        assertEquals(21, progress.getPopulation());
        assertEquals(2, progress.getPopulationGrowthRate());
        assertEquals(3, progress.getPopulationDecreaseRate());
        assertEquals(
                1.75,
                progress.getEconomyGrowthRate(),
                0.0001
        );
        assertEquals(90, progress.getMoneyProduction());
    }

    // Verifica che lo stato della costruzione venga copiato nel salvataggio.
    @Test
    void fromConstructionCopiesState() {
        Construction construction = mock(Construction.class);

        when(construction.getType())
                .thenReturn(ConstructionType.RESIDENTIAL);
        when(construction.getPopulation()).thenReturn(37);
        when(construction.getPopulationGrowthRate()).thenReturn(4);
        when(construction.getPopulationDecreaseRate()).thenReturn(3);
        when(construction.getEconomyGrowthRate()).thenReturn(0.0);
        when(construction.getMoneyProduction()).thenReturn(25);
        when(construction.isTsunamiInsured()).thenReturn(true);

        ConstructionProgress progress =
                ConstructionProgress.fromConstruction(
                        construction,
                        5,
                        8
                );

        assertEquals(ConstructionType.RESIDENTIAL, progress.getType());
        assertEquals(5, progress.getRow());
        assertEquals(8, progress.getColumn());
        assertEquals(37, progress.getPopulation());
        assertEquals(4, progress.getPopulationGrowthRate());
        assertEquals(3, progress.getPopulationDecreaseRate());
        assertEquals(
                0.0,
                progress.getEconomyGrowthRate(),
                0.0001
        );
        assertEquals(25, progress.getMoneyProduction());
        assertTrue(progress.isTsunamiInsured());
    }

    // Verifica che dal salvataggio venga ricreata la costruzione corretta.
    @Test
    void toConstructionRestoresState() {
        ConstructionProgress progress = new ConstructionProgress(
                ConstructionType.RESIDENTIAL,
                4,
                9,
                37,
                4,
                3,
                0.0,
                25,
                true
        );

        Construction restoredConstruction =
                progress.toConstruction();

        assertInstanceOf(
                Residential.class,
                restoredConstruction
        );
        assertEquals(
                ConstructionType.RESIDENTIAL,
                restoredConstruction.getType()
        );
        assertEquals(37, restoredConstruction.getPopulation());
        assertEquals(
                4,
                restoredConstruction.getPopulationGrowthRate()
        );
        assertEquals(
                3,
                restoredConstruction.getPopulationDecreaseRate()
        );
        assertEquals(25, restoredConstruction.getMoneyProduction());
        assertTrue(restoredConstruction.isTsunamiInsured());
    }

    @Test
    void nuclearFireProtectionSurvivesSaveAndLoad()
    {
        NuclearPlant nuclearPlant = new NuclearPlant();
        nuclearPlant.setFireProtected(true);

        ConstructionProgress progress =
                ConstructionProgress.fromConstruction(
                        nuclearPlant,
                        4,
                        9
                );

        NuclearPlant restoredPlant =
                assertInstanceOf(
                        NuclearPlant.class,
                        progress.toConstruction()
                );

        assertTrue(progress.isFireProtected());
        assertTrue(restoredPlant.isFireProtected());
    }

    // Una costruzione priva del tipo non può essere ricreata.
    @Test
    void nullTypeCannotBeRestored() {
        ConstructionProgress progress = new ConstructionProgress(
                null,
                0,
                0,
                0,
                0,
                0,
                0.0,
                0
        );

        assertThrows(
                IllegalStateException.class,
                progress::toConstruction
        );
    }
}