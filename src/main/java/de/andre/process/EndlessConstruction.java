package de.andre.process;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.process.util.GameBoardRepository;
import de.andre.process.util.ManuelPlacement;

@Component
public class EndlessConstruction extends AbstractOptimization {
    @Value("${exp.endlessconstruction.items.before.shrink}")
    private int ITEMS_BEFORE_SHRINK = 1_000_000;
    @Value("${exp.endlessconstruction.elements.to.not.shrink}")
    private int ELEMENTS_TO_NOT_SHRINK = 5;
    @Value("${exp.endlessconstruction.ways.likelyhood}")
    private int WAYS_LIKELYHOOD = 2;

    private final GameBoardRepository repository = new GameBoardRepository();

    public void start() {
	long seed = new Random().nextLong();
	logParameter("ITEMS_BEFORE_SHRINK", ITEMS_BEFORE_SHRINK);
	logParameter("ELEMENTS_TO_NOT_SHRINK", ELEMENTS_TO_NOT_SHRINK);
	logParameter("WAYS_LIKELYHOOD", WAYS_LIKELYHOOD);
	logParameter("random.seed", seed);

	// seed = -6542860481660332438L;
	Random r = new Random(seed);
	IFoEGameboard startingBoard = createRandomBoard();
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
	    addGamefieldToTopList(currentBoard);

	    // apply garbage collection every now and then
	    if (repository.size() >= ITEMS_BEFORE_SHRINK) {
		repository.shrink(ELEMENTS_TO_NOT_SHRINK);
		System.gc();
	    }
	}
    }
}
