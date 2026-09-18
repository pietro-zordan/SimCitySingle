package model;

import model.Cell;
import model.Construction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CellTest {

    @Mock
    private Construction construction;

    private Cell cell;

    @BeforeEach
    void setUp() {
        cell = new Cell(2, 3);
    }

    @Test
    void newCellHasCorrectRowAndColumn() {
        assertEquals(2, cell.getRow());
        assertEquals(3, cell.getColumn());
    }

    @Test
    void newCellIsEmpty() {
        assertTrue(cell.isEmpty());
        assertNull(cell.getConstruction());
    }

    @Test
    void placeConstructionFillsCell() {
        cell.placeConstruction(construction);
        //si assicura del corretto output di isEmpty
        assertFalse(cell.isEmpty());
        assertEquals(construction, cell.getConstruction());
    }

    @Test
    void placeConstructionWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> cell.placeConstruction(null));
    }

    @Test
    void placeConstructionOnOccupiedCellThrowsException() {
        cell.placeConstruction(construction);

        Construction anotherConstruction = mock(Construction.class);

        assertThrows(IllegalStateException.class,
                () -> cell.placeConstruction(anotherConstruction));
    }

    @Test
    void removeConstructionReturnsRemovedConstructionAndEmptiesCell() {
        cell.placeConstruction(construction);

        Construction removed = cell.removeConstruction();

        assertEquals(construction, removed);
        assertTrue(cell.isEmpty());
    }

    @Test
    void removeConstructionOnEmptyCellReturnsNull() {
        Construction removed = cell.removeConstruction();

        assertNull(removed);
    }

    @Test
    void updateOfOneTickOnEmptyCellReturnsFalse() {
        assertFalse(cell.updateOfOneTick());
    }

    @Test
    void updateOfOneTickCallsUpdateOnConstruction() {
        cell.placeConstruction(construction);

        cell.updateOfOneTick();

        verify(construction).updateOfOneTick();
    }

    //controlla updateOfOneTick rispetto al valore di MustBeRemuved
    @Test
    void updateOfOneTickReturnsTrueWhenConstructionMustBeRemoved() {
        when(construction.mustBeRemoved()).thenReturn(true);
        cell.placeConstruction(construction);

        boolean result = cell.updateOfOneTick();

        assertTrue(result);
    }

    @Test
    void updateOfOneTickReturnsFalseWhenConstructionMustNotBeRemoved() {
        when(construction.mustBeRemoved()).thenReturn(false);
        cell.placeConstruction(construction);

        boolean result = cell.updateOfOneTick();

        assertFalse(result);
    }
}