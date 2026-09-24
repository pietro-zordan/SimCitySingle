package model;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class AchievementManager {

    private final Set<Achievement> unlockedAchievements;
    private final Queue<Achievement> newlyUnlockedAchievements;

    public AchievementManager()
    {
        unlockedAchievements = new HashSet<>();

        //achievements appena sbloccati che la GUI deve ancora mostrare
        newlyUnlockedAchievements = new ArrayDeque<>();
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

        boolean unlocked = unlockedAchievements.add(achievement);

        if (unlocked)
        {
            newlyUnlockedAchievements.add(achievement);
        }

        return unlocked;
    }

    //recupera achievement da mostrare
    public Achievement consumeNewlyUnlockedAchievement()
    {
        return newlyUnlockedAchievements.poll();
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

    public boolean populationOfThousand(City city)
    {
        if(city.getGlobalPopulation() >= 1000)
            return unlock(Achievement.FIRST_1000_INHABITANTS);

        return false;
    }

}
