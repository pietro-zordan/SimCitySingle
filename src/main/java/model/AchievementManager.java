package model;

import java.util.HashSet;
import java.util.Set;

public class AchievementManager {

    private final Set<Achievement> unlockedAchievements;

    public AchievementManager()
    {
        unlockedAchievements = new HashSet<>();
    }

    /* Ricostruisce il manager partendo dagli achievement
       letti dal file di persistenza. */
    public AchievementManager(Set<Achievement> unlockedAchievements)
    {
        this();

        if (unlockedAchievements != null)
        {
            this.unlockedAchievements.addAll(
                    unlockedAchievements
            );
        }
    }

    public boolean unlock(Achievement achievement)
    {
        if (achievement == null)
        {
            throw new IllegalArgumentException("Achievement cannot be null");
        }

        return unlockedAchievements.add(achievement);
    }

    public boolean isUnlocked(Achievement achievement)
    {
        return unlockedAchievements.contains(achievement);
    }

    public Set<Achievement> getUnlockedAchievements()
    {
        return new HashSet<>(
                unlockedAchievements
        );
    }

    public void reset()
    {
        unlockedAchievements.clear();
    }

    public boolean onConstructionPlaced(
            ConstructionType type)
    {
        if (type == ConstructionType.NUCLEAR_PLANT)
        {
            return unlock(
                    Achievement.FIRST_NUCLEAR_PLANT
            );
        }

        return false;
    }

}
