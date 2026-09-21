package progress;

import controller.Controller;
import model.Cell;
import model.City;
import model.ConstructionType;
import model.Grid;
import model.Grass;
import model.ReconstructionEntry;
import model.Residential;
import model.Road;
import model.Simulation;
import org.junit.jupiter.api.Test;
import policies.EnvironmentalPolicy;
import policies.Policy;
import policies.PolicyType;
import policies.StandardPolicy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgressTest {

    // Verifica che il costruttore conservi lo stato generale della partita.
    @Test
    void constructorStoresValues() {
        List<ConstructionProgress> constructions =
                createSavedConstructions();

        Progress progress = new Progress(
                20,
                14,
                1800,
                PolicyType.ENVIRONMENTAL,
                constructions
        );

        assertEquals(20, progress.getCurrentTick());
        assertEquals(14, progress.getLastPolicyChangeTick());
        assertEquals(1800, progress.getBudget());
        assertEquals(
                PolicyType.ENVIRONMENTAL,
                progress.getPolicyType()
        );
        assertEquals(
                constructions,
                progress.getConstructions()
        );
    }

    // Verifica che fromGame raccolga i dati correnti della partita.
    @Test
    void fromGameCopiesCurrentState() {
        Grid grid = mock(Grid.class);
        City city = mock(City.class);
        Simulation simulation = mock(Simulation.class);
        Policy policy = mock(Policy.class);
        Cell occupiedCell = mock(Cell.class);
        Cell emptyCell = mock(Cell.class);

        Residential residential = new Residential();
        residential.restoreState(
                37,
                4,
                3,
                0.0,
                0
        );

        // Simula una piccola griglia contenente una sola costruzione.
        when(grid.getNumberOfRows()).thenReturn(1);
        when(grid.getNumberOfColumns()).thenReturn(2);
        when(grid.getCell(0, 0)).thenReturn(occupiedCell);
        when(grid.getCell(0, 1)).thenReturn(emptyCell);
        when(occupiedCell.getConstruction()).thenReturn(residential);
        when(emptyCell.getConstruction()).thenReturn(null);

        // Imposta i dati che devono essere inseriti nel salvataggio.
        when(city.getBudget()).thenReturn(1800);
        when(city.getCurrentPolicy()).thenReturn(policy);
        when(policy.getType())
                .thenReturn(PolicyType.ENVIRONMENTAL);
        when(simulation.getCurrentTick()).thenReturn(20);
        when(simulation.getLastPolicyChangeTick()).thenReturn(14);

        Progress progress =
                Progress.fromGame(
                        grid,
                        city,
                        simulation
                );

        assertEquals(20, progress.getCurrentTick());
        assertEquals(14, progress.getLastPolicyChangeTick());
        assertEquals(1800, progress.getBudget());
        assertEquals(
                PolicyType.ENVIRONMENTAL,
                progress.getPolicyType()
        );
        assertEquals(1, progress.getConstructions().size());

        ConstructionProgress savedConstruction =
                progress.getConstructions().get(0);

        assertEquals(
                ConstructionType.RESIDENTIAL,
                savedConstruction.getType()
        );
        assertEquals(0, savedConstruction.getRow());
        assertEquals(0, savedConstruction.getColumn());
        assertEquals(37, savedConstruction.getPopulation());
    }

    // Verifica il ripristino delle costruzioni e delle connessioni stradali.
    @Test
    void restoreGridRestoresConstructions() {
        Progress progress = new Progress(
                20,
                14,
                1800,
                PolicyType.STANDARD,
                createSavedConstructions()
        );

        Grid restoredGrid = progress.restoreGrid();

        assertEquals(
                ConstructionType.ROAD,
                restoredGrid
                        .getCell(5, 5)
                        .getConstruction()
                        .getType()
        );

        Residential residential = assertInstanceOf(
                Residential.class,
                restoredGrid
                        .getCell(5, 6)
                        .getConstruction()
        );

        assertEquals(37, residential.getPopulation());
        assertEquals(
                4,
                residential.getPopulationGrowthRate()
        );
        assertEquals(
                3,
                residential.getPopulationDecreaseRate()
        );
        assertTrue(residential.isRoadConnected());
    }

    // Verifica che venga ricreata la policy salvata.
    @Test
    void restorePolicyRestoresCorrectType() {
        Progress progress = new Progress(
                20,
                14,
                1800,
                PolicyType.ENVIRONMENTAL,
                List.of()
        );

        Policy restoredPolicy =
                progress.restorePolicy();

        assertInstanceOf(
                EnvironmentalPolicy.class,
                restoredPolicy
        );
        assertEquals(
                PolicyType.ENVIRONMENTAL,
                restoredPolicy.getType()
        );
    }

    // Verifica che il controller ripristinato contenga lo stato salvato.
    @Test
    void restoreControllerRestoresGameState() {
        Progress progress = new Progress(
                20,
                14,
                1800,
                PolicyType.ENVIRONMENTAL,
                createSavedConstructions()
        );

        Controller controller =
                progress.restoreController();

        assertEquals(1800, controller.getBudget());
        assertEquals(20, controller.getCurrentTick());
        assertEquals(6, controller.getTicksToPolicyChange());
        assertEquals(
                "Environmental policy",
                controller.getCurrentPolicyName()
        );
        assertEquals(
                ConstructionType.ROAD,
                controller.getCellState(5, 5).type()
        );
        assertEquals(
                ConstructionType.RESIDENTIAL,
                controller.getCellState(5, 6).type()
        );
    }

    @Test
    void pendingTsunamiReconstructionSurvivesSaveAndLoad()
    {
        Grid grid =
                new Grid();

        grid.restoreConstruction(
                new Road(),
                10,
                10
        );

        City city =
                new City(
                        grid,
                        new StandardPolicy()
                );

        Simulation simulation =
                new Simulation(
                        city,
                        grid,
                        20,
                        0,
                        true
                );

        Residential damagedResidential =
                new Residential();

        damagedResidential.restoreState(
                37,
                4,
                3,
                0.0,
                0
        );

        damagedResidential.setTsunamiInsured(
                true
        );

        simulation.restoreTsunamiReconstruction(
                List.of(
                        new ReconstructionEntry(
                                damagedResidential,
                                0,
                                5
                        )
                ),
                "UP"
        );

        Progress progress =
                Progress.fromGame(
                        grid,
                        city,
                        simulation
                );

        assertEquals(
                1,
                progress
                        .getPendingTsunamiReconstructions()
                        .size()
        );

        assertEquals(
                "UP",
                progress
                        .getTsunamiReconstructionDirection()
        );

        Controller restoredController =
                progress.restoreController();

        assertTrue(
                restoredController
                        .getCellState(0, 5)
                        .empty()
        );

        restoredController.updateOfOneTick();

        assertEquals(
                ConstructionType.RESIDENTIAL,
                restoredController
                        .getCellState(0, 5)
                        .type()
        );
    }

    @Test
    void oldSavedGrassIsIgnoredWhenLoading()
    {
        Progress progress = new Progress(
                10,
                0,
                2500,
                PolicyType.STANDARD,
                List.of(
                        new ConstructionProgress(
                                ConstructionType.GRASS,
                                4,
                                4,
                                0,
                                0,
                                0,
                                0.0,
                                0
                        )
                )
        );

        Grid restoredGrid = progress.restoreGrid();

        assertTrue(restoredGrid.getCell(4, 4).isEmpty());
    }

    @Test
    void grassIsNotSavedAnymore()
    {
        Grid grid = new Grid();
        grid.restoreConstruction(new Grass(), 4, 4);

        City city = new City(grid, new StandardPolicy());
        Simulation simulation = new Simulation(city, grid);

        Progress progress = Progress.fromGame(grid, city, simulation);

        assertTrue(progress.getConstructions().isEmpty());
    }

    // Un salvataggio senza lista di costruzioni non può ripristinare la griglia.
    @Test
    void nullConstructionListCannotBeRestored() {
        Progress progress = new Progress(
                0,
                0,
                2500,
                PolicyType.STANDARD,
                null
        );

        assertThrows(
                IllegalStateException.class,
                progress::restoreGrid
        );
    }

    // Crea una strada e una residenza adiacenti riutilizzate nei test.
    private List<ConstructionProgress> createSavedConstructions() {
        return List.of(
                new ConstructionProgress(
                        ConstructionType.ROAD,
                        5,
                        5,
                        0,
                        0,
                        0,
                        0.0,
                        0
                ),
                new ConstructionProgress(
                        ConstructionType.RESIDENTIAL,
                        5,
                        6,
                        37,
                        4,
                        3,
                        0.0,
                        0
                )
        );
    }
}