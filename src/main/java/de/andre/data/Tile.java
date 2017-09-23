package de.andre.data;

public class Tile {

    private final int x;

    private final int y;

    private final TileState building;

    /**
     * @param x topLeft corner, zero based
     * @param y topLeft corner, zero based
     * @param building
     */
    public Tile(int x, int y, TileState building) {
	this.x = x;
	this.y = y;
	this.building = building;
    }

    /**
     * @return zero based index
     */
    public int getX() {
        return x;
    }

    /**
     * @return zero based index
     */
    public int getY() {
        return y;
    }

    public TileState getBuilding() {
        return building;
    }
}
