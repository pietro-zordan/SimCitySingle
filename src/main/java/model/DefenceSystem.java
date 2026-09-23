package model;

public interface DefenceSystem {

    public boolean canRemoveCriminalActivity(int currentTick);

    public void registerCriminalActivityRemoval(int currentTick);

}
