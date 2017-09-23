package de.andre.data;

public enum TileState {

    /** auf dieser kachel steht ein Schloss */
    CASTLE(0, 7, 6), /** auf dieser Kachel steht ein Weg */
    WAY(-1, 1, 1), /** auf dieser Kachel steht ein Haus */
    HOUSE(20, 2, 2), /** diese Kachel ist noch nicht belegt */
    FREE(-5, 1, 1);

    private final int value;
    private final int width;
    private final int height;

    private TileState(int value, int width, int height) {
	this.value = value;
	this.width = width;
	this.height = height;
    }

    public int getValue() {
	return value;
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

}
