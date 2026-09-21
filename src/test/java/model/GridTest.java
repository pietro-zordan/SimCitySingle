package model;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GridTest {

    private Grid grid;

    //Creazione di oggetti finti per testare la classe
    @Mock
    private Road road;

    @Mock
    private Road anotherRoad;

    @Mock
    private PowerPlant powerPlant;

    @Mock
    private Construction building; // costruzione generica non-model.Road, non-model.PowerPlant

    @BeforeEach
    void setUp() {
        grid = new Grid();
        lenient().when(road.getType()).thenReturn(ConstructionType.ROAD);
        lenient().when(anotherRoad.getType()).thenReturn(ConstructionType.ROAD);
    }

    // Controllo che il metodo isInside ritorni valori corretti
    //prendendo a campione celle fuori e dentro la griglia

    @Test
    void isInsideReturnsTrueForCellsInsideTheGrid() {
        assertTrue(grid.isInside(0, 0));
        assertTrue(grid.isInside(19, 19));
        assertTrue(grid.isInside(10, 5));
    }

    @Test
    void isInsideReturnsFalseForCellsOutsideTheGrid() {
        assertFalse(grid.isInside(-1, 0));
        assertFalse(grid.isInside(0, -1));
        assertFalse(grid.isInside(20, 0));
        assertFalse(grid.isInside(0, 20));
    }

    // ---------- getCell ----------

    @Test
    void getCellReturnsCellWithCorrectCoordinates() {
        // prende una cella si assicura che colonna e riga di piazzamento siano corretti
        Cell cell = grid.getCell(3, 4);

        assertNotNull(cell);
        assertEquals(3, cell.getRow());
        assertEquals(4, cell.getColumn());
    }

    // ---------- placeConstruction ----------

    @Test
    void placeConstructionOutsideGridThrowsException() {
        //controlla presenza eccezione per piazzamento fuori dalla griglia
        assertThrows(IllegalArgumentException.class,
                () -> grid.placeConstruction(road, -1, 0));
    }

    @Test
    void placeConstructionOnOccupiedCellThrowsException() {
        //controlla eccezione se piazzamento su celle già occupate
        grid.placeConstruction(road, 0, 0);

        assertThrows(IllegalStateException.class,
                () -> grid.placeConstruction(anotherRoad, 0, 0));
    }

    @Test
    void placeFirstRoadSucceedsWithoutAdjacentRoad() {
        //controlla che la prima strada sia piazzabile
        grid.placeConstruction(road, 5, 5);

        Cell cell = grid.getCell(5, 5);
        assertFalse(cell.isEmpty());
        assertEquals(road, cell.getConstruction());
        verify(road).initializeAfterPlacement(grid, 5, 5);
    }

    @Test
    void placeSecondRoadNotAdjacentToExistingRoadThrowsException() {
        //controlla lancio eccezione se strade non vicine
        grid.placeConstruction(road, 5, 5);

        assertThrows(IllegalStateException.class,
                () -> grid.placeConstruction(anotherRoad, 15, 15));
    }

    @Test
    void placeSecondRoadAdjacentToExistingRoadSucceeds() {
        //controlla che il piazzamento di una strada vicino ad un altra strada abbia successo
        grid.placeConstruction(road, 5, 5);

        grid.placeConstruction(anotherRoad, 5, 6);

        Cell cell = grid.getCell(5, 6);
        assertFalse(cell.isEmpty());
        assertEquals(anotherRoad, cell.getConstruction());
    }

    @Test
    void placeNonRoadConstructionWithoutAdjacentRoadThrowsException() {
        //costruzioni non adiacenti alla strada non possono essere costruite
        assertThrows(IllegalStateException.class,
                () -> grid.placeConstruction(building, 5, 5));
    }

    @Test
    void placeNonRoadConstructionAdjacentToRoadSucceedsAndConnectsToRoad() {
        //costruzioni adiacenti alla strada devono avere successo
        grid.placeConstruction(road, 5, 5);

        grid.placeConstruction(building, 5, 6);

        Cell cell = grid.getCell(5, 6);
        assertFalse(cell.isEmpty());
        assertEquals(building, cell.getConstruction());
        verify(building).initializeAfterPlacement(grid, 5, 6);
        verify(building).setRoadConnected(true);
    }

    // ---------- removeConstruction ----------

    @Test
    void removeConstructionOutsideGridThrowsException() {
        //deve lanciare eccezione se provi a rimuovere qualcosa fuori dalla griglia
        assertThrows(IllegalArgumentException.class,
                () -> grid.removeConstruction(-1, 0));
    }

    @Test
    void removeConstructionOnEmptyCellDoesNothing() {
        assertDoesNotThrow(() -> grid.removeConstruction(0, 0));
    }

    @Test
    void removeRoadThrowsException() {
        grid.placeConstruction(road, 5, 5);

        assertThrows(IllegalStateException.class,
                () -> grid.removeConstruction(5, 5));
    }

    @Test
    void removeConstructionThatCannotBeRemovedThrowsException() {
        grid.placeConstruction(road, 5, 5);
        when(road.canBeRemoved()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> grid.removeConstruction(5, 5));
    }

    @Test
    void removePowerPlantPreparesRemovalAndEmptiesCell() {
        //Rimuovere le centrali deve chiamare il metodo disconnecAll
        lenient().when(building.canBeRemoved()).thenReturn(true);
        lenient().when(powerPlant.canBeRemoved()).thenReturn(true);
        grid.placeConstruction(road, 5, 5);
        grid.placeConstruction(powerPlant, 5, 6);

        grid.removeConstruction(5, 6);

        verify(powerPlant).prepareForRemoval();
        assertTrue(grid.getCell(5, 6).isEmpty());
    }

    @Test
    void removeConstructionPreparesBuildingAndEmptiesCell() {
        when(building.canBeRemoved()).thenReturn(true);

        grid.placeConstruction(road, 5, 5);
        grid.placeConstruction(building, 5, 6);

        grid.removeConstruction(5, 6);

        verify(building).prepareForRemoval();
        assertTrue(grid.getCell(5, 6).isEmpty());
    }

    // ---------- hasAdjacentRoad ----------

    @Test
    void hasAdjacentRoadReturnsTrueWhenNeighborIsRoad() {
        grid.placeConstruction(road, 5, 5);

        Cell neighbor = grid.getCell(5, 6);
        assertTrue(grid.hasAdjacentRoad(neighbor));
    }

    @Test
    void hasAdjacentRoadReturnsFalseWhenNoNeighborIsRoad() {
        Cell cell = grid.getCell(10, 10);
        assertFalse(grid.hasAdjacentRoad(cell));
    }

    // ---------- hasAnyRoad ----------

    @Test
    void hasAnyRoadReturnsFalseWhenGridIsEmpty() {
        assertFalse(grid.hasAnyRoad());
    }

    @Test
    void hasAnyRoadReturnsTrueAfterPlacingARoad() {
        grid.placeConstruction(road, 5, 5);
        assertTrue(grid.hasAnyRoad());
    }

    // ---------- updateRoadConnections ----------

    @Test
    void updateRoadConnectionsSetsTrueForConstructionsAdjacentToRoad() {
        grid.placeConstruction(road, 5, 5);
        grid.placeConstruction(building, 5, 6);

        //verifica che aggiornando updateRoadConnection sia vero per costruzioni adiacenti alla strada
        grid.updateRoadConnections();
        verify(building, atLeastOnce()).setRoadConnected(true);
    }

    // ---------- updateOfOneTick ----------

    @Test
    void updateOfOneTickUpdatesPowerPlantAndRemovesItIfNecessary() {
        when(powerPlant.canBeRemoved()).thenReturn(true);
        when(powerPlant.mustBeRemoved()).thenReturn(true);

        grid.placeConstruction(road, 5, 5);
        grid.placeConstruction(powerPlant, 5, 6);

        assertTrue(grid.getCell(5, 6).getConstruction() == powerPlant);

        grid.updateOfOneTick();

        verify(powerPlant).updateOfOneTick();
        verify(powerPlant).prepareForRemoval();
        assertTrue(grid.getCell(5, 6).isEmpty());
    }

    @Test
    void updateOfOneTickUpdatesOtherConstructionAndRemovesItIfNecessary() {
        lenient().when(building.canBeRemoved()).thenReturn(true);
        lenient().when(powerPlant.canBeRemoved()).thenReturn(true);
        grid.placeConstruction(road, 5, 5);
        grid.placeConstruction(building, 5, 6);

        when(building.mustBeRemoved()).thenReturn(true);

        grid.updateOfOneTick();

        verify(building).updateOfOneTick();
        assertTrue(grid.getCell(5, 6).isEmpty());
    }

    @Test
    void updateOfOneTickDoesNotRemoveConstructionThatMustNotBeRemoved() {
        grid.placeConstruction(road, 5, 5);
        grid.placeConstruction(building, 5, 6);

        when(building.mustBeRemoved()).thenReturn(false);

        grid.updateOfOneTick();

        assertFalse(grid.getCell(5, 6).isEmpty());
    }

    // ---------- getAllPowerPlants ----------

    @Test
    void getAllPowerPlantsReturnsEmptyListWhenNoneArePlaced() {
        //metodo deve ritornare vuoto se non ci sono centrali elettriche sulla griglia
        List<PowerPlant> powerPlants = grid.getAllPowerPlants();
        assertTrue(powerPlants.isEmpty());
    }

    @Test
    void getAllPowerPlantsReturnsOnlyPlacedPowerPlants() {
        grid.placeConstruction(road, 5, 5);
        grid.placeConstruction(powerPlant, 5, 6);
        grid.placeConstruction(building, 5, 4);

        List<PowerPlant> powerPlants = grid.getAllPowerPlants();
//si assicuri che ritorni 1 se vi è una sola centrale piazzata
        assertEquals(1, powerPlants.size());
        assertTrue(powerPlants.contains(powerPlant));
    }

    // ---------- getNumberOfRows / getNumberOfColumns ----------

    @Test
    void getNumberOfRowsReturns20() {
        assertEquals(20, grid.getNumberOfRows());
    }

    @Test
    void getNumberOfColumnsReturns20() {
        assertEquals(20, grid.getNumberOfColumns());
    }

    // ---------- countBuildableCells ----------

    @Test
    void countBuildableCellsIsZeroWhenNoRoadIsPlaced() {
        assertEquals(0, grid.countBuildableCells());
    }

    @Test
    void countBuildableCellsCountsEmptyCellsAdjacentToRoads() {
        // Una singola strada in mezzo alla griglia ha 4 celle vuote adiacenti
        grid.placeConstruction(road, 10, 10);

        assertEquals(4, grid.countBuildableCells());
    }

    @Test
    void criminalActivityDoesNotTurnReachableAreaIntoGrass()
    {
        Grid localGrid = new Grid();

        localGrid.restoreConstruction(
                new Road(),
                10,
                8
        );

        int[][] wallCells =
                {
                        {9, 10},
                        {9, 11},
                        {11, 9},
                        {12, 10},
                        {12, 11},
                        {10, 12},
                        {11, 12}
                };

        for (int[] position : wallCells)
        {
            localGrid.restoreConstruction(
                    new Park(),
                    position[0],
                    position[1]
            );
        }

        localGrid.placeConstruction(
                new CriminalActivity(),
                10,
                9
        );

        assertTrue(
                localGrid.getCell(
                        10,
                        10
                ).isEmpty()
        );

        assertTrue(
                localGrid.getCell(
                        10,
                        11
                ).isEmpty()
        );

        assertTrue(
                localGrid.getCell(
                        11,
                        10
                ).isEmpty()
        );

        assertTrue(
                localGrid.getCell(
                        11,
                        11
                ).isEmpty()
        );
    }

    // ---------- restoreConstruction ----------

    @Test
    void restoreConstructionWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> grid.restoreConstruction(null, 0, 0));
    }

    @Test
    void restoreConstructionOutsideGridThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> grid.restoreConstruction(road, -1, 0));
    }

    @Test
    void restoreConstructionOnOccupiedCellThrowsException() {
        grid.placeConstruction(road, 5, 5);

        assertThrows(IllegalStateException.class,
                () -> grid.restoreConstruction(anotherRoad, 5, 5));
    }

    @Test
    void restoreConstructionPlacesConstructionAndInitializesIt() {
        grid.restoreConstruction(building, 8, 8);

        Cell cell = grid.getCell(8, 8);
        assertFalse(cell.isEmpty());
        assertEquals(building, cell.getConstruction());
        verify(building).initializeAfterPlacement(grid, 8, 8);
    }

    // ---------- rebuildConnectionsAfterLoad ----------

    @Test
    void rebuildConnectionsAfterLoadUpdatesRoadsAndEnergyConnections()
    {
        Grid localGrid =
                new Grid();

        Road localRoad =
                new Road();

        PowerPlant localPowerPlant =
                new PowerPlant();

        Residential residential =
                new Residential();

        localGrid.restoreConstruction(
                localRoad,
                5,
                5
        );

        localGrid.restoreConstruction(
                localPowerPlant,
                5,
                6
        );

        localGrid.restoreConstruction(
                residential,
                5,
                4
        );

        localGrid.rebuildConnectionsAfterLoad();

        assertTrue(
                residential.isRoadConnected()
        );

        assertTrue(
                residential.isPowerPlantConnected()
        );

        assertSame(
                localPowerPlant,
                residential.getConnectedPowerPlant()
        );
    }
}