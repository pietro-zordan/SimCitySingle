package Events;

import model.Cell;
import model.City;
import model.Grid;
import model.Construction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnergyCrisisTest
{
    @Mock
    private City city;

    @Mock
    private Grid grid;

    @Mock
    private Cell cell;

    @Mock
    private Construction construction;

    private EnergyCrisis energyCrisis;

    @BeforeEach
    void setUp()
    {
        energyCrisis = new EnergyCrisis(city, grid);
    }

    @Test
    void testUpdateOfOneTickAppliesExtraCost()
    {
        when(grid.getNumberOfRows()).thenReturn(1);
        when(grid.getNumberOfColumns()).thenReturn(1);
        when(grid.getCell(0, 0)).thenReturn(cell);
        when(cell.isEmpty()).thenReturn(false);
        when(cell.getConstruction()).thenReturn(construction);

        when(construction.getPowerConsumption()).thenReturn(100);
        when(construction.isPowered()).thenReturn(true);
        when(city.getBudget()).thenReturn(1000);

        energyCrisis.updateOfOneTick();

        verify(city, times(1)).updateBudget(-50);
    }

    @Test
    void testCanStartWithPoweredBuildings()
    {
        energyCrisis.setProbability(21);
        when(grid.getNumberOfRows()).thenReturn(1);
        when(grid.getNumberOfColumns()).thenReturn(1);
        when(grid.getCell(0, 0)).thenReturn(cell);
        when(cell.isEmpty()).thenReturn(false);
        when(cell.getConstruction()).thenReturn(construction);

        when(construction.getPowerConsumption()).thenReturn(100);
        when(construction.isPowered()).thenReturn(true);

        assertTrue(energyCrisis.canStart());
    }
}