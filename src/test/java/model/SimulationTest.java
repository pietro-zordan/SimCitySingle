package model;

import Events.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import policies.Policy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationTest {

    @Mock
    private City city;

    @Mock
    private Grid grid;

    @Mock
    private Policy policy;

    @Mock
    private Event event;

    private Simulation simulation;

    @BeforeEach
    void setUp() {
        // Inizializza una simulazione a tick 0[cite: 33]
        simulation = new Simulation(city, grid);
    }

    @Test
    void testCanChangePolicy_TooEarly_ReturnsFalse() {
        // Al tick 0, non sono passati i 12 tick necessari (POLICY_CHANGE_INTERVAL)[cite: 33]
        assertFalse(simulation.canChangePolicy());
        assertEquals(12, simulation.getTicksToPolicyChange());
    }

    @Test
    void testCanChangePolicy_AfterInterval_ReturnsTrue() {
        // Creiamo una simulazione dove sono passati esattamente 12 tick dall'ultimo cambio[cite: 33]
        Simulation advancedSimulation = new Simulation(city, grid, 12, 0);

        assertTrue(advancedSimulation.canChangePolicy());
        assertEquals(0, advancedSimulation.getTicksToPolicyChange());
    }

    @Test
    void testChangePolicy_Success() {
        Simulation advancedSimulation = new Simulation(city, grid, 12, 0);

        boolean result = advancedSimulation.changePolicy(policy);

        assertTrue(result);
        verify(city, times(1)).setPolicy(policy);
        assertEquals(12, advancedSimulation.getLastPolicyChangeTick());
    }

    @Test
    void testChangePolicy_Failure() {
        // Non si può cambiare policy prima dell'intervallo[cite: 33]
        boolean result = simulation.changePolicy(policy);

        assertFalse(result);
        verify(city, never()).setPolicy(any());
    }

    @Test
    void testStartEvent_Success() {
        when(event.canStart()).thenReturn(true);

        simulation.startEvent(event);

        assertTrue(simulation.isEventActive());
        verify(event, times(1)).start();
    }

    @Test
    void testUpdateOfOneTick_WithActiveEvent_FinishesEvent() {
        // Configuriamo l'evento in modo che possa iniziare e poi finisca dopo 1 tick
        when(event.canStart()).thenReturn(true);
        when(event.isFinished(1)).thenReturn(true);

        simulation.startEvent(event);
        assertTrue(simulation.isEventActive());

        // Eseguiamo il tick
        simulation.updateOfOneTick();

        // L'evento deve essersi aggiornato e poi concluso[cite: 33]
        verify(city, times(1)).updateOfOneTick();
        verify(event, times(1)).updateOfOneTick();
        verify(event, times(1)).end();
        assertFalse(simulation.isEventActive());
        assertEquals(1, simulation.getCurrentTick());
    }
}