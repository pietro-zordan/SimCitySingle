package controller;

import model.ConstructionType;
import model.FreemasonryChoice;
import model.Grid;
import model.MasonicLodge;
import model.Residential;
import model.Road;
import org.junit.jupiter.api.Test;
import policies.StandardPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasonicLodgeTest
{
    @Test
    void acceptedInvitationPlacesOneLodgeAndRestoresItWithoutDuplicates()
    {
        Grid grid = new Grid();
        grid.placeConstruction(new Road(), 10, 10);
        Controller controller = new Controller(
                grid, new StandardPolicy(), 2000, 500, 0
        );
        int originalBudget = controller.getBudget();

        assertTrue(controller.chooseFreemasonry(FreemasonryChoice.ACCEPTED));
        assertEquals(1, countLodges(grid));
        assertEquals(originalBudget, controller.getBudget());
        assertThrows(IllegalStateException.class,
                () -> controller.placeConstruction(
                        ConstructionType.MASONIC_LODGE, 9, 10
                ));

        Controller loaded = controller.createProgress().restoreController();
        assertEquals(FreemasonryChoice.ACCEPTED, loaded.getFreemasonryChoice());
        assertEquals(1, countLodges(loaded.createProgress().restoreGrid()));

        loaded.placeConstruction(ConstructionType.ROAD, 10, 11);
        assertEquals(1, countLodges(loaded.createProgress().restoreGrid()));
    }

    @Test
    void waitsForBuildableSpaceAndKeepsLastRoadSpaceFree()
    {
        Grid grid = new Grid();
        grid.placeConstruction(new Road(), 0, 0);
        grid.placeConstruction(new Residential(), 0, 1);
        Controller controller = new Controller(
                grid, new StandardPolicy(), 2000, 500, 0
        );

        assertEquals(1, grid.countBuildableCells());
        assertTrue(controller.chooseFreemasonry(FreemasonryChoice.ACCEPTED));
        assertEquals(0, countLodges(grid));

        controller.placeConstruction(ConstructionType.ROAD, 1, 0);
        assertEquals(1, countLodges(grid));
        assertTrue(grid.countBuildableCells() >= 1);
    }

    @Test
    void decliningTheInvitationNeverSpawnsTheLodge()
    {
        Grid grid = new Grid();
        grid.placeConstruction(new Road(), 10, 10);
        Controller controller = new Controller(
                grid, new StandardPolicy(), 2000, 500, 0
        );

        assertTrue(controller.chooseFreemasonry(FreemasonryChoice.DECLINED));
        controller.placeConstruction(ConstructionType.ROAD, 10, 11);
        assertEquals(0, countLodges(grid));
        assertFalse(controller.isFreemasonryInvitationPending());
    }

    private static long countLodges(Grid grid)
    {
        return grid.getConstructions().stream()
                .filter(MasonicLodge.class::isInstance)
                .count();
    }
}
