package model;

import java.util.HashSet;
import java.util.Set;

public class AchievementManager {

    private final Set<Achievement> unlockedAchievements;

    public AchievementManager()
    {
        unlockedAchievements = new HashSet<>();
    }

    public boolean unlock(Achievement achievement)
    {
        return unlockedAchievements.add(achievement);
    }

    public boolean isUnlocked(Achievement achievement)
    {
        return unlockedAchievements.contains(achievement);
    }

    public void reset()
    {
        unlockedAchievements.clear();
    }

}
