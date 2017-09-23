package de.andre.process;

import java.util.List;
import java.util.Random;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.data.impl.Integer1DimArrayBoard;
import de.andre.process.util.BoardVisualizer;
import de.andre.process.util.GameBoardRepository;
import de.andre.process.util.ManuelPlacement;
import lombok.extern.log4j.Log4j;

@Log4j
public class EndlessConstruction {
    private static final int ITEMS_BEFORE_SHRINK = 1_000_000;
    private static final int ELEMENTS_TO_NOT_SHRINK = 5;
    private static final int WAYS_LIKELYHOOD = 2;
    private static final int GAMEFIELD_WIDTH = 24;
    private static final int GAMEFIELD_HEIGHT = 20;

    private final GameBoardRepository repository = new GameBoardRepository();

    public static void main(String[] args) {
	new EndlessConstruction().start();
    }

    private static void log(String msg) {
	log.info(msg);
    }

    private void start() {
	Random r = new Random(2383284L);
	IFoEGameboard startingBoard = createRandomBoard(GAMEFIELD_WIDTH, GAMEFIELD_HEIGHT);
	IFoEGameboard currentBoard = startingBoard;
	repository.add(currentBoard);

	ManuelPlacement.createAndPrintReference(startingBoard);

	log("-----  start  ----");
	while (true) {
	    // clone board ...
	    currentBoard = currentBoard.clone();

	    TileState desiredBuilding = TileState.WAY;
	    if (currentBoard.getOccupiedTiles() == 0) {
		// place an CASTLE anywhere
		desiredBuilding = TileState.CASTLE;
	    } else if (r.nextInt(WAYS_LIKELYHOOD) == 0) {
		// place an House now and then (20%)
		desiredBuilding = TileState.HOUSE;
	    }

	    List<Tile> options = currentBoard.getPlacementOptions(desiredBuilding);
	    if (options.isEmpty()) {
		// start all over
		currentBoard = repository.nextStartingBoard(r);
	    } else {
		// choose one option at random
		int randomIndex = r.nextInt(options.size());
		Tile tile = options.get(randomIndex);
		currentBoard.placeBuildingWithoutAnyChecks(tile.getX(), tile.getY(), desiredBuilding);
	    }

	    // add new board
	    repository.add(currentBoard);

	    // apply garbage collection every now and then
	    if (repository.size() >= ITEMS_BEFORE_SHRINK) {
		repository.shrink(ELEMENTS_TO_NOT_SHRINK);
		BoardVisualizer.print(repository.getTopRatedBoard());
		log("");
		System.gc();
	    }
	}
    }

    public IFoEGameboard createRandomBoard(int gamefieldWidth, int gamefieldHeight) {
	return new Integer1DimArrayBoard(gamefieldWidth, gamefieldHeight);
    }
}
