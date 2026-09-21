package model;

// Contiene le informazioni necessarie alla GUI per mostrare un'esplosione.
public class ExplosionInfo
{
    private final int row;
    private final int column;
    private final int radius;

    public ExplosionInfo(int row, int column, int radius)
    {
        this.row = row;
        this.column = column;
        this.radius = radius;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }

    public int getRadius()
    {
        return radius;
    }
}
