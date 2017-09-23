package de.andre.data;

import java.util.List;

public interface IFoEGameboard extends Cloneable {

	int getWidth();
	
	int getHeight();
	
	int getRating();

	int getOccupiedTiles();
	
	/**
	 * @param x zero based index
	 * @param y zero based index
	 * @return
	 */
	TileState getBuilding(int x, int y);
	
	/**
	 * @param x zero based index
	 * @param y zero based index
	 * @param building
	 */
	void placeBuildingWithoutAnyChecks(int x, int y, TileState building);
	
	List<Tile> getPlacementOptions(TileState building);

	IFoEGameboard clone();
}
