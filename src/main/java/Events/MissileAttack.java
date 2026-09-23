package Events;

import model.City;
import model.Grid;

import java.util.Random;

public class MissileAttack extends Event
{

    private final Grid grid;
    private static final int TICK_DURATION = 1;
    private static final int HAPPINESS_DECREASE = 800;
    private int targetRow;
    private int targetColumn;

    public MissileAttack(City city, Grid grid)
    {
        super(TICK_DURATION, city, 200);

        this.grid=grid;
        launchNumber= 8;
    }

    @Override
    public EventType getType() {
        return EventType.MISSILE_ATTACK;
    }


    @Override
    public void updateOfOneTick()
    {

    }

    @Override
    public void start() {

        Random random = new Random();
        targetRow = random.nextInt(grid.getNumberOfRows());
        targetColumn = random.nextInt(grid.getNumberOfColumns());

        grid.destroyArea(targetRow, targetColumn, 1);

        city.refreshStatistics();
        city.decreaseGlobalHappiness(HAPPINESS_DECREASE);
    }

    public int getTargetRow()
    {
        return targetRow;
    }

    public int getTargetColumn()
    {
        return targetColumn;
    }
}
