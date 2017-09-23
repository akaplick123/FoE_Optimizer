package de.andre.data.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.util.Assertion;

public abstract class AbstractGameBoard implements IFoEGameboard {
    public static int numberOfBoardsCreated = 0;
    protected final int width;
    protected final int height;

    public AbstractGameBoard(int width, int height) {
	this.width = width;
	this.height = height;
	numberOfBoardsCreated++;
    }

    /**
     * @param x zero based
     * @param y zero based
     * @return may return <code>null</code> if x or y is outside of allowed grid
     */
    protected abstract TileState getBuildingInternal(int x, int y);

    /**
     * @param x zero based
     * @param y zero based
     */
    protected abstract void setBuildingInternal(int x, int y, TileState building);

    @Override
    public abstract AbstractGameBoard clone();

    @Override
    public final int getWidth() {
	return width;
    }

    @Override
    public final int getHeight() {
	return height;
    }

    @Override
    public int getRating() {
	int sum = 0;
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		sum += getBuildingInternal(x, y).getValue();
	    }
	}
	return sum;
    }
    
    @Override
    public int getOccupiedTiles() {
	int cnt = 0;
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		if (getBuildingInternal(x, y) != TileState.FREE) {
		    cnt++;
		}
	    }
	}
	return cnt;
    }
   
    @Override
    public TileState getBuilding(final int x, final int y) {
	TileState building = getBuildingInternal(x, y);
	Assertion.notNull(building, e -> "x (" + x + ") and/or y (" + y + ") are out of bounds");
	return building;
    }

    @Override
    public void placeBuildingWithoutAnyChecks(int x, int y, TileState building) {
	for (int ix = 0; ix < building.getWidth(); ix++) {
	    for (int iy = 0; iy < building.getHeight(); iy++) {
		setBuildingInternal(x + ix, y + iy, building);
	    }
	}
    }

    @Override
    public List<Tile> getPlacementOptions(TileState building) {
	switch (building) {
	case WAY:
	    return getWayPlacementOptions();
	case CASTLE:
	    return getCastlePlacementOptions(building);
	case HOUSE:
	    return getHousePlacementOptions(building);
	default:
	    return Collections.emptyList();
	}
    }

    /**
     * returns all options to place the castle (assuming the game field has no
     * buildings yet)
     * 
     * @param castleDim
     *            dimensions of castle
     * @return all top-left (north-west) most tiles of the castle
     */
    private List<Tile> getCastlePlacementOptions(TileState castleDim) {
	List<Tile> result = new ArrayList<>();
	// it's possible to place that castle at least somewhere
	for (int x = 0; x <= (width - castleDim.getWidth()); x++) {
	    for (int y = 0; y <= (height - castleDim.getHeight()); y++) {
		result.add(new Tile(x, y, castleDim));
	    }
	}

	return result;
    }

    /**
     * @return all options to place a way
     */
    private List<Tile> getWayPlacementOptions() {
	List<Tile> wayPlacementOptions = new ArrayList<>();

	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		TileState wayOption = getBuildingInternal(x, y);
		if (wayOption == TileState.FREE && (//
		containsWayOrCastle(x - 1, y) //
			|| containsWayOrCastle(x + 1, y) //
			|| containsWayOrCastle(x, y - 1) //
			|| containsWayOrCastle(x, y + 1))) {
		    wayPlacementOptions.add(new Tile(x, y, TileState.WAY));
		}
	    }
	}

	return wayPlacementOptions;
    }

    private boolean containsWayOrCastle(int x, int y) {
	TileState building = getBuildingInternal(x, y);
	return building == TileState.WAY || building == TileState.CASTLE;
    }

    /**
     * returns all options to place a house of the given size
     * 
     * @param dim
     *            dimensions of that building
     * @return all top-left (north-west) most tiles of the building
     */
    private List<Tile> getHousePlacementOptions(TileState dim) {
	List<Tile> result = new ArrayList<>();

	for (int x = 0; x <= (width - dim.getWidth()); x++) {
	    for (int y = 0; y <= (height - dim.getHeight()); y++) {
		if (canPlaceHouseHere(x, y, dim)) {
		    result.add(new Tile(x, y, dim));
		}
	    }
	}

	return result;
    }

    private boolean canPlaceHouseHere(final int x, final int y, final TileState dim) {
	if (!isBuildingAreaFree(x, y, dim)) {
	    // area is not free
	    return false;
	}

	final int buildingWidth = dim.getWidth();
	final int buildingHeight = dim.getHeight();

	// area is free, now check all borders for an street tile
	// check top (north) border
	for (int dx = 0; dx < buildingWidth; dx++) {
	    if (getBuildingInternal(x + dx, y - 1) == TileState.WAY) {
		return true;
	    }
	}

	// check bottom (south) border
	for (int dx = 0; dx < buildingWidth; dx++) {
	    if (getBuildingInternal(x + dx, y + dim.getHeight() + 1) == TileState.WAY) {
		return true;
	    }
	}

	// check left (west) border
	for (int dy = 0; dy < buildingHeight; dy++) {
	    if (getBuildingInternal(x - 1, y + dy) == TileState.WAY) {
		return true;
	    }
	}

	// check right (east) border
	for (int dy = 0; dy < buildingHeight; dy++) {
	    if (getBuildingInternal(x + dim.getWidth() + 1, y + dy) == TileState.WAY) {
		return true;
	    }
	}

	// no way found at the borders
	return false;
    }

    private boolean isBuildingAreaFree(final int x, final int y, final TileState dim) {
	for (int dx = 0; dx < dim.getWidth(); dx++) {
	    for (int dy = 0; dy < dim.getHeight(); dy++) {
		// check current tile (it can be null)
		if (getBuildingInternal(x + dx, y + dy) != TileState.FREE) {
		    // tile is not free or out of bounds, so we cannot place
		    // something of the given size hereF
		    return false;
		}
	    }
	}

	// all tiles are free, so you can build something of that size here
	return true;
    }
}
