package Events;

import model.City;
import model.Grid;
import model.MissileDefense;

import java.util.Random;

public class MissileAttack extends Event
{
    private final Grid grid;
    private final MissileDefense missileDefense;
    private static final int TICK_DURATION = 1;
    private static final int HAPPINESS_DECREASE = 800;

    private int targetRow;
    private int targetColumn;
    private boolean intercepted;

    public MissileAttack(
            City city,
            Grid grid,
            MissileDefense missileDefense)
    {
        super(TICK_DURATION, city, 200);

        this.grid = grid;
        this.missileDefense = missileDefense;
        launchNumber = 5;
    }

    @Override
    public EventType getType()
    {
        return EventType.MISSILE_ATTACK;
    }

    @Override
    public void updateOfOneTick()
    {
    }

    @Override
    public void start()
    {
        Random random = new Random();

        targetRow =
                random.nextInt(
                        grid.getNumberOfRows()
                );

        targetColumn =
                random.nextInt(
                        grid.getNumberOfColumns()
                );

        intercepted =
                missileDefense != null
                        && missileDefense.absorbMissile();

        if (!intercepted)
        {
            grid.destroyArea(
                    targetRow,
                    targetColumn,
                    1
            );

            city.refreshStatistics();
            city.decreaseGlobalHappiness(
                    HAPPINESS_DECREASE
            );
        }
    }

    public int getTargetRow()
    {
        return targetRow;
    }

    public int getTargetColumn()
    {
        return targetColumn;
    }

    public boolean isIntercepted()
    {
        return intercepted;
    }
}
