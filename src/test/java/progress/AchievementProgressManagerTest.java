package progress;

import model.Achievement;
import model.AchievementManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AchievementProgressManagerTest
{
    private AchievementProgressManager manager;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp()
    {
        manager = new AchievementProgressManager();
    }

    @Test
    void saveAndLoadPreserveAchievements()
            throws IOException
    {
        AchievementManager achievementManager =
                new AchievementManager();

        achievementManager.unlock(
                Achievement.FIRST_NUCLEAR_PLANT
        );

        Path file =
                tempDirectory.resolve(
                        "achievements.json"
                );

        manager.save(
                achievementManager,
                file.toString()
        );

        AchievementManager loaded =
                manager.load(file.toString());

        assertTrue(Files.exists(file));
        assertTrue(
                loaded.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );
    }

    @Test
    void missingFileCreatesEmptyManager()
            throws IOException
    {
        Path file =
                tempDirectory.resolve(
                        "missing.json"
                );

        AchievementManager loaded =
                manager.load(file.toString());

        assertFalse(
                loaded.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );
    }

    @Test
    void resetClearsMemoryAndFile()
            throws IOException
    {
        AchievementManager achievementManager =
                new AchievementManager();

        achievementManager.unlock(
                Achievement.FIRST_NUCLEAR_PLANT
        );

        Path file =
                tempDirectory.resolve(
                        "achievements.json"
                );

        manager.reset(
                achievementManager,
                file.toString()
        );

        AchievementManager loaded =
                manager.load(file.toString());

        assertFalse(
                achievementManager.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );

        assertFalse(
                loaded.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT
                )
        );
    }
}
