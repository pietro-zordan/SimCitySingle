package model;

import java.util.Objects;

/* Centralizza tutte le condizioni di sblocco degli achievement.
   Le altre classi comunicano cosa è successo, senza decidere quale achievement sbloccare. */
public class AchievementChecker
{
    private final AchievementManager achievementManager;

    public AchievementChecker(AchievementManager achievementManager)
    {
        this.achievementManager = Objects.requireNonNull(achievementManager,
                        "Achievement manager cannot be null");
    }

    /* Controlla gli achievement ricavabili dallo stato corrente del gioco.
       Grid e Simulation sono già disponibili per regole future che ne avranno bisogno. */
    public void checkState(
            City city,
            Grid grid,
            Simulation simulation)
    {
        Objects.requireNonNull(city, "City cannot be null");

        Objects.requireNonNull(grid, "Grid cannot be null");

        Objects.requireNonNull(simulation, "Simulation cannot be null");

        checkPopulation(city);
        checkBudget(city);
        checkMetropolis(grid);
    }

    /* Riceve il piazzamento riuscito di una costruzione e valuta
       gli achievement collegati a questo tipo di azione. */
    public void onConstructionPlaced(
            ConstructionType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException(
                    "Construction type cannot be null"
            );
        }

        if (type == ConstructionType.NUCLEAR_PLANT
                && !achievementManager.isUnlocked(
                        Achievement.FIRST_NUCLEAR_PLANT))
        {
            achievementManager.unlock(
                    Achievement.FIRST_NUCLEAR_PLANT
            );
        }
    }

    private void checkPopulation(City city)
    {
        if (city.getGlobalPopulation() >= 1000
                && !achievementManager.isUnlocked(
                        Achievement.FIRST_1000_INHABITANTS))
        {
            achievementManager.unlock(Achievement.FIRST_1000_INHABITANTS);
        }
    }

    private void checkBudget(City city)
    {
        if (city.getBudget() >= 50000
                && !achievementManager.isUnlocked(
                Achievement.A_LOT_OF_MONEY))
        {
            achievementManager.unlock(Achievement.A_LOT_OF_MONEY);
        }
    }

    public void onConstructionRemovedByPlayer(
            ConstructionType type)
    {
        if (type == ConstructionType.NUCLEAR_PLANT
                && !achievementManager.isUnlocked(
                Achievement.WHY_WOULD_YOU_DO_THAT))
        {
            achievementManager.unlock(Achievement.WHY_WOULD_YOU_DO_THAT);
        }
    }

    public void checkMetropolis(Grid grid)
    {
        Objects.requireNonNull(
                grid,
                "Grid cannot be null"
        );

        boolean expanded =
                grid.getNumberOfRows() > 20
                        || grid.getNumberOfColumns() > 20;

        if (expanded
                && !achievementManager.isUnlocked(
                        Achievement.METROPOLIS))
        {
            achievementManager.unlock(
                    Achievement.METROPOLIS
            );
        }
    }

    public void onFreemasonryChoice(
            FreemasonryChoice choice)
    {
        if (choice == FreemasonryChoice.ACCEPTED
                && !achievementManager.isUnlocked(
                        Achievement.ILLUMINATED))
        {
            achievementManager.unlock(
                    Achievement.ILLUMINATED
            );
        }
    }



}
