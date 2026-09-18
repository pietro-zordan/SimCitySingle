package Events;

import model.City;
import model.Grid;
import model.PowerPlant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HackerAttackTest {

    @Mock
    private City city;
    @Mock
    private Grid grid;
    @Mock
    private PowerPlant powerPlant;

    private HackerAttack hackerAttack;

    @BeforeEach
    void setUp() {
        hackerAttack = new HackerAttack(city, grid);
    }

    @Test
    void testCanStart_WithPowerPlants_ReturnsTrue() {
        hackerAttack.setProbability(21); // Forza canStart() a true tramite probability == launchNumber - 1
        when(grid.getAllPowerPlants()).thenReturn(List.of(powerPlant));

        assertTrue(hackerAttack.canStart(), "L'evento deve poter iniziare se ci sono centrali");
    }

    @Test
    void testCanStart_WithoutPowerPlants_ReturnsFalse() {
        hackerAttack.setProbability(21);
        when(grid.getAllPowerPlants()).thenReturn(Collections.emptyList());

        assertFalse(hackerAttack.canStart(), "L'evento non deve iniziare senza centrali");
    }

    @Test
    void testStart_SuspendsPowerPlant() {
        when(grid.getAllPowerPlants()).thenReturn(List.of(powerPlant));

        hackerAttack.start();

        verify(powerPlant, times(1)).suspend();
    }

    @Test
    void testEnd_ResumesPowerPlant() {
        when(grid.getAllPowerPlants()).thenReturn(List.of(powerPlant));
        hackerAttack.start(); // Seleziona e sospende la centrale

        hackerAttack.end();

        verify(powerPlant, times(1)).resume();
    }

    @Test
    void testTickDurationAndIsFinished() {
        assertEquals(5, hackerAttack.getTickDuration());
        assertFalse(hackerAttack.isFinished(3));
        assertTrue(hackerAttack.isFinished(5));
    }
}