package model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AchievementManagerTest
{
    @Test
    void unlocksAchievementOnlyOnce()
    {
        AchievementManager manager =
                new AchievementManager();

        assertTrue(
                manager.unlock(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );

        assertFalse(
                manager.unlock(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );

        assertTrue(
                manager.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );
    }

    @Test
    void restoresUnlockedAchievements()
    {
        AchievementManager manager =
                new AchievementManager(
                        Set.of(
                                Achievement.FIRST_NUCLEAR_PLANT
                        )
                );

        assertTrue(
                manager.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );
    }

    @Test
    void resetClearsAchievements()
    {
        AchievementManager manager =
                new AchievementManager();

        manager.unlock(
                Achievement.FIRST_NUCLEAR_PLANT
        );

        manager.reset();

        assertFalse(
                manager.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );
    }
}
