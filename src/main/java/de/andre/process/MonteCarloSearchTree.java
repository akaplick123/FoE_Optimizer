package de.andre.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.process.util.ManuelPlacement;

@Component
public class MonteCarloSearchTree extends AbstractOptimization {

    @Value("${exp.mc.max.options.per.turn}")
    private int MAX_OPTIONS_PER_TURN = 10000;
    private Random r;

    @Override
    protected void start() {
	long seed = new Random().nextLong();
	logParameter("MAX_OPTIONS_PER_TURN", MAX_OPTIONS_PER_TURN);
	logParameter("random.seed", seed);

	// seed = -6542860481660332438L;
	r = new Random(seed);

	IFoEGameboard startingBoard = createRandomBoard();

	// print my reference
	ManuelPlacement.createAndPrintReference(startingBoard);

	// place castle in the top-left corner
	IFoEGameboard baseBoard = placeCastle(startingBoard);
	addGamefieldToTopList(baseBoard);

	while (true) {
	    // generate all first level options and boards
	    List<IFoEGameboard> allNextLevelBoards = createNextLevelBoards(baseBoard);
	    if (allNextLevelBoards.isEmpty()) {
		// exit loop, because no other options are available
		System.err.println("finished");
		return;
	    }

	    int maxOptionsPerBoard = MAX_OPTIONS_PER_TURN / allNextLevelBoards.size();
	    if (maxOptionsPerBoard < 1) {
		maxOptionsPerBoard = 1;
	    }

	    EvaluationRecord bestRecord = null;
	    for (IFoEGameboard board : allNextLevelBoards) {
		EvaluationRecord record = evaluateBoard(board, maxOptionsPerBoard);

		if (record.isBetterThen(bestRecord)) {
		    bestRecord = record;
		}
	    }

	    // go with best board into the next iteration
	    baseBoard = bestRecord.baseBoard;
	    addGamefieldToTopList(baseBoard);
	}
    }

    private EvaluationRecord evaluateBoard(IFoEGameboard baseBoard, int maxOptions) {
	EvaluationRecord record = new EvaluationRecord(baseBoard);
	int sumOptionsDone = 0;
	while (sumOptionsDone < maxOptions) {
	    // simulate one complete game with all actions at random until there
	    // are no options left
	    PlaydownRecord playdownRec = playdown(baseBoard.clone());
	    
	    record.numPlaydowns++;
	    record.sumScore += playdownRec.score;

	    sumOptionsDone += playdownRec.placedBuildings;

	    if (playdownRec.placedBuildings == 0) {
		// end loop
		sumOptionsDone = maxOptions;
	    }
	}
	return record;
    }

    private PlaydownRecord playdown(IFoEGameboard board) {
	int optionsTaken = 0;
	int optionCnt = 1;
	while (optionCnt > 0) {
	    List<Tile> houseOptions = board.getPlacementOptions(TileState.HOUSE);
	    List<Tile> wayOptions = board.getPlacementOptions(TileState.WAY);
	    optionCnt = houseOptions.size() + wayOptions.size();
	    if (optionCnt > 0) {
		// choose one action
		int randomOptionIdx = r.nextInt(optionCnt);
		if (randomOptionIdx < houseOptions.size()) {
		    // build an house
		    Tile option = houseOptions.get(randomOptionIdx);
		    board.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.HOUSE);
		} else {
		    // build an way
		    randomOptionIdx -= houseOptions.size();
		    Tile option = wayOptions.get(randomOptionIdx);
		    board.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.WAY);
		}
		optionsTaken++;
	    }
	}
	addGamefieldToTopList(board);

	PlaydownRecord result = new PlaydownRecord();
	result.placedBuildings = optionsTaken;
	result.score = board.getRating();
	return result;
    }

    /**
     * @param baseBoard
     * @return all collection of all boards reachable with one move (placement)
     */
    private List<IFoEGameboard> createNextLevelBoards(IFoEGameboard baseBoard) {
	ArrayList<IFoEGameboard> firstLevelBoards = new ArrayList<>();
	List<Tile> houseOptions = baseBoard.getPlacementOptions(TileState.HOUSE);
	for (Tile option : houseOptions) {
	    IFoEGameboard gfClone = baseBoard.clone();
	    gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.HOUSE);
	    firstLevelBoards.add(gfClone);
	}

	List<Tile> wayOptions = baseBoard.getPlacementOptions(TileState.WAY);
	for (Tile option : wayOptions) {
	    IFoEGameboard gfClone = baseBoard.clone();
	    gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.WAY);
	    firstLevelBoards.add(gfClone);
	}
	return firstLevelBoards;
    }

    private IFoEGameboard placeCastle(IFoEGameboard startingBoard) {
	// there is not tile set until now, so we have to place the castle
	List<Tile> options = startingBoard.getPlacementOptions(TileState.CASTLE);
	IFoEGameboard gfClone = startingBoard.clone();
	Tile option = options.get(0);
	gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.CASTLE);
	return gfClone;
    }

    private static final class PlaydownRecord {
	int placedBuildings = 0;
	int score = 0;
    }

    private static final class EvaluationRecord {
	private final IFoEGameboard baseBoard;
	private int sumScore = 0;
	private int numPlaydowns = 0;

	public EvaluationRecord(IFoEGameboard baseBoard) {
	    this.baseBoard = baseBoard;
	}

	public boolean isBetterThen(EvaluationRecord oldRecord) {
	    if (oldRecord == null) {
		return true;
	    }
	    if (oldRecord.numPlaydowns == 0) {
		return true;
	    }
	    if (this.numPlaydowns == 0) {
		return false;
	    }
	    double oldAvgScore = oldRecord.sumScore / (double) oldRecord.numPlaydowns;
	    double avgScore = this.sumScore / (double) this.numPlaydowns;
	    return (avgScore > oldAvgScore);
	}
    }
}
