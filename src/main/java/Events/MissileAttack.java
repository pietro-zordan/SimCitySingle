package Events;

import model.Cell;
import model.City;
import model.Grid;
import model.NuclearPlant;

import java.util.ArrayList;
import java.util.List;
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
        launchNumber= 2;
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
    public boolean canStart()
    {
        return super.canStart()
                && !getNuclearPlantCells().isEmpty();
    }

    @Override
    public void start()
    {
        Random random = new Random();

        List<Cell> nuclearPlantCells =
                getNuclearPlantCells();

        Cell nuclearPlantCell =
                nuclearPlantCells.get(
                        random.nextInt(
                                nuclearPlantCells.size()
                        )
                );

        if (random.nextBoolean())
        {
            targetRow = nuclearPlantCell.getRow();
            targetColumn = nuclearPlantCell.getColumn();
        }
        else
        {
            chooseTargetNearNuclearPlant(
                    nuclearPlantCell,
                    random
            );
        }

        grid.destroyArea(
                targetRow,
                targetColumn,
                1
        );

        city.refreshStatistics();
        city.decreaseGlobalHappiness(HAPPINESS_DECREASE);
    }

    private List<Cell> getNuclearPlantCells()
    {
        List<Cell> nuclearPlantCells =
                new ArrayList<>();

        for (int row = 0;
             row < grid.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Cell cell =
                        grid.getCell(
                                row,
                                column
                        );

                if (!cell.isEmpty()
                        && cell.getConstruction()
                        instanceof NuclearPlant)
                {
                    nuclearPlantCells.add(cell);
                }
            }
        }

        return nuclearPlantCells;
    }

    private void chooseTargetNearNuclearPlant(
            Cell nuclearPlantCell,
            Random random)
    {
        List<int[]> possibleTargets =
                new ArrayList<>();

        for (int rowOffset = -1;
             rowOffset <= 1;
             rowOffset++)
        {
            for (int columnOffset = -1;
                 columnOffset <= 1;
                 columnOffset++)
            {
                if (rowOffset != 0
                        || columnOffset != 0)
                {
                    int row =
                            nuclearPlantCell.getRow()
                                    + rowOffset;

                    int column =
                            nuclearPlantCell.getColumn()
                                    + columnOffset;

                    if (grid.isInside(
                            row,
                            column
                    ))
                    {
                        possibleTargets.add(
                                new int[] {
                                        row,
                                        column
                                }
                        );
                    }
                }
            }
        }

        int[] target =
                possibleTargets.get(
                        random.nextInt(
                                possibleTargets.size()
                        )
                );

        targetRow = target[0];
        targetColumn = target[1];
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
