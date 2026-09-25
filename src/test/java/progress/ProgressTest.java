package progress;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import controller.Controller;
import model.Cell;
import model.City;
import model.ConstructionCompany;
import model.ConstructionType;
import model.CriminalActivity;
import model.Grid;
import model.Grass;
import model.Park;
import model.PoliceStation;
import model.PowerPlant;
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

    // Verifica che una griglia espansa mantenga le proprie dimensioni nel salvataggio.
    @Test
    void expandedGridSizeSurvivesJsonSaveAndLoad()
    {
        Grid grid = new Grid(40, 40);
        grid.restoreConstruction(new Road(), 39, 39);

        City city = new City(grid, new StandardPolicy(), 100000);
        Simulation simulation = new Simulation(city, grid, 400, 396);

        Gson gson = new Gson();
        Progress saved = Progress.fromGame(grid, city, simulation);
        Progress loaded = gson.fromJson(gson.toJson(saved), Progress.class);

        Grid restoredGrid = loaded.restoreGrid();

        assertEquals(40, restoredGrid.getNumberOfRows());
        assertEquals(40, restoredGrid.getNumberOfColumns());
        assertEquals(
                ConstructionType.ROAD,
                restoredGrid.getCell(39, 39).getConstruction().getType()
        );
    }

    // I vecchi JSON senza dimensioni continuano a essere caricati come 20x20.
    @Test
    void oldSaveWithoutGridSizeDefaultsToTwenty()
    {
        Grid grid = new Grid();
        grid.restoreConstruction(new Road(), 19, 19);

        City city = new City(grid, new StandardPolicy(), 100000);
        Simulation simulation = new Simulation(city, grid, 20, 14);

        Gson gson = new Gson();
        JsonObject oldSave = gson.toJsonTree(
                Progress.fromGame(grid, city, simulation)
        ).getAsJsonObject();

        oldSave.remove("gridRows");
        oldSave.remove("gridColumns");

        Grid restoredGrid = gson.fromJson(
                oldSave,
                Progress.class
        ).restoreGrid();

        assertEquals(20, restoredGrid.getNumberOfRows());
        assertEquals(20, restoredGrid.getNumberOfColumns());
        assertEquals(
                ConstructionType.ROAD,
                restoredGrid.getCell(19, 19).getConstruction().getType()
        );
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
    void demolitionAndPoliceCountdownsSurviveJsonSaveAndLoad()
    {
        Grid grid = new Grid();
        grid.restoreConstruction(new PowerPlant(), 5, 5);
        grid.restoreConstruction(new ConstructionCompany(), 5, 6);
        PoliceStation policeStation = new PoliceStation();
        policeStation.registerCriminalActivityRemoval(42);
        grid.restoreConstruction(policeStation, 5, 7);
        grid.restoreConstruction(new Park(), 10, 10);

        CriminalActivity criminalActivity = new CriminalActivity();
        criminalActivity.setCreationTick(42);
        grid.restoreConstruction(criminalActivity, 10, 11);
        grid.rebuildConnectionsAfterLoad();

        City city = new City(grid, new StandardPolicy(), 100000);
        Simulation simulation = new Simulation(city, grid, 43, 0);
        simulation.registerRemoval();
        assertEquals(0, simulation.getAvailableRemovals());

        Gson gson = new Gson();
        Progress saved = Progress.fromGame(grid, city, simulation);
        Progress loaded = gson.fromJson(gson.toJson(saved), Progress.class);
        Controller restored = loaded.restoreController();
        assertEquals(43,
                gson.toJsonTree(restored.createProgress())
                        .getAsJsonObject()
                        .getAsJsonArray("demolitionTicks")
                        .get(0).getAsInt());

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> restored.removeConstructionByPlayer(10, 10)
        );
        assertEquals("No demolitions available", error.getMessage());

        Grid restoredGrid = loaded.restoreGrid();
        PoliceStation restoredPolice = (PoliceStation)
                restoredGrid.getCell(5, 7).getConstruction();
        CriminalActivity restoredCriminalActivity = (CriminalActivity)
                restoredGrid.getCell(10, 11).getConstruction();

        assertFalse(restoredPolice.canRemoveCriminalActivity(56));
        assertTrue(restoredPolice.canRemoveCriminalActivity(57));
        assertFalse(restoredCriminalActivity.canBeRemovedByPolice(44));
        assertTrue(restoredCriminalActivity.canBeRemovedByPolice(45));
    }

    @Test
    void olderSaveKeepsSpentDemolitionQuota()
    {
        Grid grid = new Grid();
        grid.restoreConstruction(new PowerPlant(), 5, 5);
        grid.restoreConstruction(new ConstructionCompany(), 5, 6);
        grid.restoreConstruction(new Park(), 10, 10);
        grid.rebuildConnectionsAfterLoad();

        City city = new City(grid, new StandardPolicy(), 100000);
        Simulation simulation = new Simulation(city, grid, 49, 0);
        simulation.registerRemoval();

        Gson gson = new Gson();
        JsonObject oldSave = gson.toJsonTree(
                Progress.fromGame(grid, city, simulation)
        ).getAsJsonObject();
        oldSave.remove("demolitionTicks");
        oldSave.addProperty("demolitionStatePresent", true);
        oldSave.addProperty("usedRemovals", 1);

        Controller restored = gson.fromJson(
                oldSave, Progress.class
        ).restoreController();

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> restored.removeConstructionByPlayer(10, 10));
        assertEquals("No demolitions available", error.getMessage());
        assertEquals(49,
                gson.toJsonTree(restored.createProgress())
                        .getAsJsonObject()
                        .getAsJsonArray("demolitionTicks")
                        .get(0).getAsInt());
    }

    @Test
    void bankruptcyCountdownSurvivesSaveAndLoad()
    {
        Grid grid = new Grid();
        City city = new City(grid, new StandardPolicy());
        Simulation simulation = new Simulation(city, grid);
        simulation.restoreBankruptcyState(14);

        Controller restored =
                Progress.fromGame(grid, city, simulation)
                        .restoreController();

        assertEquals(14, restored.createProgress().getCriticalTicks());
        assertFalse(restored.isGameOver());
    }

    @Test
    void loadingLostGameKeepsItOver()
    {
        Grid grid = new Grid();
        City city = new City(grid, new StandardPolicy());
        Simulation simulation = new Simulation(city, grid);
        simulation.restoreBankruptcyState(15);

        Controller restored =
                Progress.fromGame(grid, city, simulation)
                        .restoreController();

        assertTrue(restored.isGameOver());
        assertThrows(IllegalStateException.class,
                restored::updateOfOneTick);
        assertEquals(0, restored.getCurrentTick());
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
