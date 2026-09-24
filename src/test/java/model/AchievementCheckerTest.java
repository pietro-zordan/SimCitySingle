package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AchievementCheckerTest
{
    @Test
    void nuclearPlantPlacementUnlocksAchievement()
    {
        AchievementManager manager =
                new AchievementManager();

        AchievementChecker checker =
                new AchievementChecker(manager);

        checker.onConstructionPlaced(
                ConstructionType.NUCLEAR_PLANT
        );

        assertTrue(
                manager.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );
    }

    @Test
    void otherConstructionDoesNotUnlockNuclearAchievement()
    {
        AchievementManager manager =
                new AchievementManager();

        AchievementChecker checker =
                new AchievementChecker(manager);

        checker.onConstructionPlaced(
                ConstructionType.ROAD
        );

        assertFalse(
                manager.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );
    }

    @Test
    void thousandInhabitantsUnlocksAchievement()
    {
        AchievementManager manager =
                new AchievementManager();

        AchievementChecker checker =
                new AchievementChecker(manager);

        City city = mock(City.class);
        Grid grid = mock(Grid.class);
        Simulation simulation = mock(Simulation.class);

        when(city.getGlobalPopulation())
                .thenReturn(1000);

        checker.checkState(
                city,
                grid,
                simulation
        );

        assertTrue(
                manager.isUnlocked(
                        Achievement.FIRST_1000_INHABITANTS
                )
        );
    }

    @Test
    void populationBelowThresholdDoesNotUnlockAchievement()
    {
        AchievementManager manager =
                new AchievementManager();

        AchievementChecker checker =
                new AchievementChecker(manager);

        City city = mock(City.class);
        Grid grid = mock(Grid.class);
        Simulation simulation = mock(Simulation.class);

        when(city.getGlobalPopulation())
                .thenReturn(999);

        checker.checkState(
                city,
                grid,
                simulation
        );

        assertFalse(
                manager.isUnlocked(
                        Achievement.FIRST_1000_INHABITANTS
                )
        );
    }
}
