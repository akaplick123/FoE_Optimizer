package de.andre.data.impl;

import de.andre.data.IFoEGameboard;
import de.andre.data.TileState;

public class Integer1DimArrayBoard extends AbstractGameBoard implements IFoEGameboard {
    private final int[] board;

    public Integer1DimArrayBoard(int width2, int height2) {
	super(width2, height2);
	// an int is 32 bit long, we need 2 bit per field, so one int has 16
	// fields
	this.board = new int[MathUtil.ceilDiv(width2 * height2, 16)];
    }

    private static TileState toTileState(int building) {
	switch (building) {
	case 0B00:
	    // it's Crucial that FREE is at index 0
	    return TileState.FREE;
	case 0B01:
	    return TileState.WAY;
	case 0B10:
	    return TileState.HOUSE;
	case 0B11:
	    return TileState.CASTLE;
	default:
	    throw new ArrayIndexOutOfBoundsException(building);
	}
    }

    private static int toValue(TileState building) {
	switch (building) {
	case FREE:
	    return 0B00;
	case WAY:
	    return 0B01;
	case HOUSE:
	    return 0B10;
	case CASTLE:
	    return 0B11;
	default:
	    throw new RuntimeException("No matching found: " + building);
	}
    }

    /**
     * @param x
     *            zero based index
     * @param y
     *            zero based index
     * @return field value
     */
    private final int extractBitsOfField(int x, int y) {
	int logicalFieldIndex = y * width + x;
	// an int is 32 bit long, we need 2 bit per field, so one int has 16
	// fields
	int boardIndex = MathUtil.floorDiv(logicalFieldIndex, 16);
	int fieldPosition = MathUtil.mod(logicalFieldIndex, 16) * 2;
	int result = MathUtil.extractBits(board[boardIndex], fieldPosition, 0B11);
	return result;
    }

    @Override
    protected TileState getBuildingInternal(int x, int y) {
	if (0 <= x && x < width && 0 <= y && y < height) {
	    return toTileState(extractBitsOfField(x, y));
	}

	return null;
    }

    @Override
    protected void setBuildingInternal(int x, int y, TileState building) {
	if (0 <= x && x < width && 0 <= y && y < height) {
	    int logicalFieldIndex = y * width + x;
	    // an int is 32 bit long, we need 2 bit per field, so one int has 16
	    // fields
	    int boardIndex = MathUtil.floorDiv(logicalFieldIndex, 16);
	    int fieldPosition = MathUtil.mod(logicalFieldIndex, 16) * 2;
	    int buildingValue = toValue(building);
	    int oldValue = board[boardIndex];
	    int newValue = MathUtil.setBits(oldValue, fieldPosition, 0B11, buildingValue);
	    board[boardIndex] = newValue;
	}
    }

    @Override
    public Integer1DimArrayBoard clone() {
	Integer1DimArrayBoard clone = new Integer1DimArrayBoard(width, height);
	for (int idx = 0; idx < board.length; idx++) {
	    clone.board[idx] = board[idx];
	}

	return clone;
    }
}
