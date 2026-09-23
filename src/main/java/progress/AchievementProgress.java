package progress;

import model.Achievement;
import model.AchievementManager;

import java.util.Set;

/* Rappresenta gli achievement sbloccati indipendentemente
   dal salvataggio della singola partita. */
public class AchievementProgress
{
    private Set<Achievement> unlockedAchievements;

    public AchievementProgress(
            Set<Achievement> unlockedAchievements)
    {
        this.unlockedAchievements = unlockedAchievements;
    }

    public static AchievementProgress fromManager(
            AchievementManager achievementManager)
    {
        if (achievementManager == null)
        {
            throw new IllegalArgumentException(
                    "Achievement manager cannot be null"
            );
        }

        return new AchievementProgress(
                achievementManager
                        .getUnlockedAchievements()
        );
    }

    public AchievementManager restoreManager()
    {
        return new AchievementManager(
                unlockedAchievements
        );
    }
}
