package controller;
import model.*;
import org.junit.jupiter.api.Test;
import policies.StandardPolicy;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    /*
     * Implementazione concreta di GameObserver usata solo nei test.
     * Serve per verificare che refreshGameView() venga chiamato.
     */
    private static class TestObserver implements GameObserver {

        private int refreshCount = 0;

        @Override
        public void refreshGameView() {
            refreshCount++;
        }

        public int getRefreshCount() {
            return refreshCount;
        }
    }

    @Test
    void addObserver() {
        Controller controller = new Controller();
        TestObserver observer = new TestObserver();

        controller.addObserver(observer);
        controller.updateOfOneTick();

        assertEquals(1, observer.getRefreshCount());
    }

    @Test
    void addObserverDoesNotAddSameObserverTwice() {
        Controller controller = new Controller();
        TestObserver observer = new TestObserver();

        controller.addObserver(observer);
        controller.addObserver(observer);
        controller.updateOfOneTick();

        assertEquals(1, observer.getRefreshCount());
    }

    @Test
    void addObserverNull() {
        Controller controller = new Controller();

        assertThrows(
                NullPointerException.class,
                () -> controller.addObserver(null)
        );
    }

    @Test
    void removeObserver() {
        Controller controller = new Controller();
        TestObserver observer = new TestObserver();

        controller.addObserver(observer);
        controller.removeObserver(observer);
        controller.updateOfOneTick();

        assertEquals(0, observer.getRefreshCount());
    }

    @Test
    void placeConstruction() {
        Controller controller = new Controller();
        TestObserver observer = new TestObserver();

        controller.addObserver(observer);

        Controller.CellState state =
                controller.placeConstruction(
                        ConstructionType.ROAD,
                        0,
                        0
                );

        assertFalse(state.empty());
        assertEquals(ConstructionType.ROAD, state.type());
        assertEquals(1, observer.getRefreshCount());
    }

    @Test
    void removeConstruction() {
        Controller controller = new Controller();
        controller.placeConstruction(
                ConstructionType.ROAD,
                0,
                1
        );
        controller.placeConstruction(
                ConstructionType.PARK,
                0,
                0
        );

        // Registriamo l'observer per tracciare la rimozione
        TestObserver observer = new TestObserver();
        controller.addObserver(observer);

        // Rimozione del PARK
        controller.removeConstruction(0, 0);

        Controller.CellState state =
                controller.getCellState(0, 0);

        assertTrue(state.empty());
        assertEquals(1, observer.getRefreshCount());
    }

    @Test
    void updateOfOneTick() {
        Controller controller = new Controller();
        TestObserver observer = new TestObserver();

        controller.addObserver(observer);

        int initialTick = controller.getCurrentTick();

        controller.updateOfOneTick();

        assertEquals(initialTick + 1, controller.getCurrentTick());
        assertEquals(1, observer.getRefreshCount());
    }

    @Test
    void canChangePolicy() {
        Controller controller = new Controller();

        // Inizialmente non è possibile cambiare policy
        assertFalse(controller.canChangePolicy());

        // Avanza di 11 turni: la policy non deve essere ancora modificabile
        for (int i = 0; i < 11; i++) {
            controller.updateOfOneTick();
            assertFalse(controller.canChangePolicy());
        }

        // Al 12° turno la policy diventa finalmente modificabile
        controller.updateOfOneTick();
        assertTrue(controller.canChangePolicy());
    }

    @Test
    void changePolicy() {
        Controller controller = new Controller();
        // 1. Facciamo passare 12 turni per abilitare il cambio policy
        for (int i = 0; i < 12; i++) {
            controller.updateOfOneTick();
        }
        // 2. Registriamo l'observer solo ora per tracciare unicamente la notifica di cambio policy
        TestObserver observer = new TestObserver();
        controller.addObserver(observer);
        // 3. Cambiamo policy usando un PolicyType diverso da quello iniziale (StandardPolicy)
        boolean changed = controller.changePolicy(
                policies.PolicyType.INDUSTRIAL
        );

        assertTrue(changed);
        assertEquals(1, observer.getRefreshCount());
    }

    @Test
    void getTicksToPolicyChange() {
        Controller controller = new Controller();

        assertEquals(
                12,
                controller.getTicksToPolicyChange()
        );
    }

    @Test
    void getCurrentPolicyName() {
        Controller controller = new Controller();

        assertEquals(
                new StandardPolicy().getName(),
                controller.getCurrentPolicyName()
        );
    }

    @Test
    void getCurrentTick() {
        Controller controller = new Controller();

        assertEquals(0, controller.getCurrentTick());
    }

    @Test
    void getPlacementCost() {
        Controller controller = new Controller();

        int cost = controller.getPlacementCost(
                ConstructionType.ROAD
        );

        assertTrue(cost <= 0);
    }

    @Test
    void getActiveTsunamiDirection() {
        Controller controller = new Controller();

        assertNull(controller.getActiveTsunamiDirection());
    }

    @Test
    void getActiveTsunamiAdvancementLength() {
        Controller controller = new Controller();

        assertEquals(
                0,
                controller.getActiveTsunamiAdvancementLength()
        );
    }

    @Test
    void createProgress() {
        Controller controller = new Controller();

        assertNotNull(controller.createProgress());
    }

    @Test
    void getActiveEventType() {
        Controller controller = new Controller();

        assertNull(controller.getActiveEventType());
    }
}

