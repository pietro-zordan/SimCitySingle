package Events;

import model.City;
import model.Grid;

public class MissileAttack extends Event
{

    private final Grid grid;
    private static final int TICK_DURATION = 1;
    private static final int HAPPINESS_DECREASE = 800;

    public MissileAttack(City city, Grid grid)
    {
        super(TICK_DURATION, city, 200);

        this.grid=grid;
        launchNumber= 10;
    }

    @Override
    public EventType getType() {
        return EventType.MISSILE_ATTACK;
    }


    @Override
    public void updateOfOneTick()
    {
        city.decreaseGlobalHappiness(HAPPINESS_DECREASE);
    }

}
