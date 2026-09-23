package model;

public interface DefenceSystem
{
    boolean canRemoveCriminalActivity(int currentTick);

    void registerCriminalActivityRemoval(int currentTick);
}
