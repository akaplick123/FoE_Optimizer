package de.andre.process;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.data.impl.AbstractGameBoard;
import de.andre.data.impl.Integer1DimArrayBoard;

public class TestConstructing {
    public static int choice = 3;
    private static final int MAX_ITEMS = 1_000_000;
    private static final int GAMEFIELD_WIDTH = 16;
    private static final int GAMEFIELD_HEIGHT = 16;

    public static void main(String[] args) {
	new TestConstructing().build().waitForEverAndPrint();
    }

    private final Collection<IFoEGameboard> boards = new ArrayList<>();

    public IFoEGameboard createRandomBoard(int gamefieldWidth, int gamefieldHeight) {
	switch (choice) {
	case 3:
	    return new Integer1DimArrayBoard(gamefieldWidth, gamefieldHeight);
	default:
	    return null;
	}
    }

    public static void log(String msg) {
	System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + ": " + msg);
    }

    private TestConstructing build() {
	log("==== start ====");
	Random r = new Random(2383284L);
	IFoEGameboard startingBoard = createRandomBoard(GAMEFIELD_WIDTH, GAMEFIELD_HEIGHT);
	IFoEGameboard currentBoard = startingBoard;
	boards.add(currentBoard);
	for (int i = 1; i < MAX_ITEMS; i++) {
	    // clone board ...
	    currentBoard = startingBoard.clone();

	    TileState desiredBuilding = TileState.WAY;
	    if (currentBoard.getOccupiedTiles() == 0) {
		// place an CASTLE anywhere
		desiredBuilding = TileState.CASTLE;
	    } else if (i % 5 == 0) {
		// place an House now and then (20%)
		desiredBuilding = TileState.HOUSE;
	    }

	    List<Tile> options = currentBoard.getPlacementOptions(desiredBuilding);
	    if (options.isEmpty()) {
		// start all over
		currentBoard = startingBoard;
	    } else {
		// choose one option at random
		int randomIndex = r.nextInt(options.size());
		Tile tile = options.get(randomIndex);
		currentBoard.placeBuildingWithoutAnyChecks(tile.getX(), tile.getY(), desiredBuilding);
	    }

	    // add new board
	    boards.add(currentBoard);

	    // apply garbage collection every now and then
	    if (i % 100_000 == 0) {
		System.gc();
	    }
	}
	System.gc();
	log("==== finished ====");
	log("Boards created: " + AbstractGameBoard.numberOfBoardsCreated + " / boards stored: " + boards.size());
	return this;
    }

    public TestConstructing waitForEverAndPrint() {
	// wait for finish
	try {
	    while (true) {
		Thread.sleep(1000);
		System.gc();

	    }
	} catch (InterruptedException e) {
	}

	// we will never reach this code, but the optimizer cannot eliminate
	// board list
	log("Boards: " + boards.size());
	return this;
    }
}
