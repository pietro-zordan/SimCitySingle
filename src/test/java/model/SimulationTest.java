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
    void invitationStartsAtTick500AndChoiceCannotBeChanged() {
        Simulation before = new Simulation(city, grid, 499, 0);
        Simulation at500 = new Simulation(city, grid, 500, 0);

        assertFalse(before.isFreemasonryInvitationPending());
        assertFalse(before.chooseFreemasonry(FreemasonryChoice.ACCEPTED));
        before.updateOfOneTick();
        assertEquals(500, before.getCurrentTick());
        assertTrue(before.isFreemasonryInvitationPending());
        assertTrue(at500.isFreemasonryInvitationPending());
        clearInvocations(city);
        assertThrows(IllegalStateException.class, at500::updateOfOneTick);
        verify(city, never()).updateOfOneTick();

        assertTrue(at500.chooseFreemasonry(FreemasonryChoice.DECLINED));
        assertEquals(FreemasonryChoice.DECLINED,
                at500.getFreemasonryChoice());
        assertFalse(at500.isFreemasonryInvitationPending());
        assertFalse(at500.chooseFreemasonry(FreemasonryChoice.ACCEPTED));
    }

    @Test
    void gameOverStopsTheSimulation()
    {
        simulation.restoreBankruptcyState(15);

        assertThrows(IllegalStateException.class,
                simulation::updateOfOneTick);
        assertEquals(0, simulation.getCurrentTick());
        verify(city, never()).updateOfOneTick();
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

    @Test
    void militaryBasesDiscountOnlyPurchase() {
        when(grid.getNumberOfMilitaryBases()).thenReturn(1, 3, 6, 8);

        assertEquals(19000, simulation.getMissileDefensePurchaseCost());
        assertEquals(17000, simulation.getMissileDefensePurchaseCost());
        assertEquals(14000, simulation.getMissileDefensePurchaseCost());
        assertEquals(14000, simulation.getMissileDefensePurchaseCost());
        assertEquals(300, simulation.getMissileDefenseRepairCost());
    }

    @Test
    void purchaseUsesDiscountedBudgetThreshold() {
        Simulation unlocked = new Simulation(city, grid, 198, 0);
        when(grid.hasMilitaryBase()).thenReturn(true);
        when(grid.getNumberOfMilitaryBases()).thenReturn(6);
        when(city.getBudget()).thenReturn(13999, 14000);

        assertFalse(unlocked.buyMissileDefense());
        assertTrue(unlocked.buyMissileDefense());
        verify(city).updateBudget(-14000);
        assertEquals(3, unlocked.getMissileDefenseHitsRemaining());
    }

    @Test
    void fullRepairChargesEveryMissingHitWithoutDiscount() {
        when(grid.getNumberOfMilitaryBases()).thenReturn(8);
        simulation.restoreMissileDefenseState(true, 0);
        when(city.getBudget()).thenReturn(899, 900);

        assertEquals(14000, simulation.getMissileDefensePurchaseCost());
        assertEquals(900, simulation.getMissileDefenseFullRepairCost());
        assertFalse(simulation.repairMissileDefenseFully());
        assertEquals(0, simulation.getMissileDefenseHitsRemaining());
        assertTrue(simulation.repairMissileDefenseFully());
        verify(city).updateBudget(-900);
        assertEquals(3, simulation.getMissileDefenseHitsRemaining());
        assertEquals(0, simulation.getMissileDefenseFullRepairCost());
        assertFalse(simulation.repairMissileDefenseFully());
    }
}
