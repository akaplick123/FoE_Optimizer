package de.andre.process;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.data.impl.Integer1DimArrayBoard;
import de.andre.process.util.ManuelPlacement;
import de.andre.process.util.ModifieableIterator;
import de.andre.process.util.PreferNewSortedLimitedList;

@Component
public class RandomDeepSearchPlacement extends AbstractOptimization {
    private static final GamefieldComparator GAMEFIELD_COMPARATOR = new GamefieldComparator();

    @Value("${exp.deep.max.saved.gamefields.per.step}")
    private int MAX_SAVED_GAMEFIELDS_PER_STEP = 300;
    @Value("${exp.deep.max.saved.gamefields.per.reset}")
    private int MAX_SAVED_GAMEFIELDS_PER_RESET = 2;
    @Value("${exp.deep.max.iterations.before.reset}")
    private int MAX_ITERATIONS_BEFORE_RESET = 500;

    private Random r;

    public void start() {
	long seed = new Random().nextLong();
	logParameter("MAX_SAVED_GAMEFIELDS_PER_STEP", MAX_SAVED_GAMEFIELDS_PER_STEP);
	logParameter("MAX_SAVED_GAMEFIELDS_PER_RESET", MAX_SAVED_GAMEFIELDS_PER_RESET);
	logParameter("MAX_ITERATIONS_BEFORE_RESET", MAX_ITERATIONS_BEFORE_RESET);
	logParameter("random.seed", seed);

	// seed = -6542860481660332438L;
	r = new Random(seed);

	BestGamefields queue = new BestGamefields();
	IFoEGameboard startingBoard = createRandomBoard();
	queue.offer(startingBoard);
	ManuelPlacement.createAndPrintReference(startingBoard);

	int iteration = 0;
	int step = 0;

	while (true) {
	    iteration++;
	    step = 0;

	    Iterator<IFoEGameboard> iterator = queue.getIterator();
	    while (iterator.hasNext()) {
		step++;
		IFoEGameboard gamefield = iterator.next();
		IFoEGameboard newGamefield = placeBuilding(gamefield);
		if (newGamefield != null) {
		    queue.offer(newGamefield);
		    addGamefieldToTopList(newGamefield);
		}
	    }

	    if (iteration >= MAX_ITERATIONS_BEFORE_RESET) {
		log("restart after " + iteration + " iterations. Last iteration had " + step + " steps.");
		queue.reset();
		iteration = 0;
	    }
	}
    }

    private IFoEGameboard createRandomBoard() {
	return new Integer1DimArrayBoard(getGamefieldWidth(), getGamefieldHeight());
    }

    /**
     * try to add a new building to the game field
     * 
     * @param gamefield
     *            the game field to place the building at. But do not change
     *            that field.
     * @return a new game field with the added building or <code>null</code> if
     *         no building was added.
     */
    private IFoEGameboard placeBuilding(IFoEGameboard gamefield) {
	if (gamefield.getOccupiedTiles() == 0) {
	    // there is not tile set until now, so we have to place the castle
	    List<Tile> options = gamefield.getPlacementOptions(TileState.CASTLE);
	    IFoEGameboard gfClone = gamefield.clone();
	    Tile option = choose(r, options);
	    gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.CASTLE);
	    return gfClone;
	}

	int buildOps = r.nextInt(2);
	if (buildOps == 0) {
	    // try to build an house
	    List<Tile> options = gamefield.getPlacementOptions(TileState.HOUSE);
	    if (options.isEmpty()) {
		return null;
	    }
	    Tile option = choose(r, options);
	    IFoEGameboard gfClone = gamefield.clone();
	    gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.HOUSE);
	    return gfClone;
	} else {
	    // try to place an way tile
	    List<Tile> options = gamefield.getPlacementOptions(TileState.WAY);
	    if (options.isEmpty()) {
		return null;
	    }
	    Tile option = choose(r, options);
	    IFoEGameboard gfClone = gamefield.clone();
	    gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.WAY);
	    return gfClone;
	}
    }

    private static Tile choose(Random r, List<Tile> options) {
	if (options.isEmpty()) {
	    throw new NullPointerException("No options to choose from.");
	}

	int index = r.nextInt(options.size());
	return options.get(index);
    }

    private class BestGamefields {
	private HashMap<Integer, PreferNewSortedLimitedList<IFoEGameboard>> mapByUsedTiles = new HashMap<>();

	/**
	 * add a new game field to the queue
	 * 
	 * @param field
	 *            the new game field
	 * @return the game field including some meta data
	 */
	public void offer(IFoEGameboard gamefield) {
	    int usedTiles = gamefield.getOccupiedTiles();

	    PreferNewSortedLimitedList<IFoEGameboard> allGamefields = mapByUsedTiles.get(usedTiles);
	    if (allGamefields == null) {
		allGamefields = new PreferNewSortedLimitedList<>(MAX_SAVED_GAMEFIELDS_PER_STEP, GAMEFIELD_COMPARATOR);
		mapByUsedTiles.put(usedTiles, allGamefields);
	    }

	    allGamefields.add(gamefield);
	}

	public void reset() {
	    for (PreferNewSortedLimitedList<IFoEGameboard> entry : mapByUsedTiles.values()) {
		entry.shrink(MAX_SAVED_GAMEFIELDS_PER_RESET);
	    }
	}

	public Iterator<IFoEGameboard> getIterator() {
	    ModifieableIterator<IFoEGameboard> iterator = new ModifieableIterator<>();
	    for (PreferNewSortedLimitedList<IFoEGameboard> gamefields : mapByUsedTiles.values()) {
		for (IFoEGameboard gamefield : gamefields) {
		    iterator.offer(gamefield);
		}
	    }

	    return iterator;
	}
    }

    private static class GamefieldComparator implements Comparator<IFoEGameboard> {
	@Override
	public int compare(IFoEGameboard o1, IFoEGameboard o2) {
	    int delta = 0;
	    if (delta == 0) {
		delta = o1.getRating() - o2.getRating();
	    }
	    if (delta == 0) {
		delta = o1.getOccupiedTiles() - o2.getOccupiedTiles();
	    }

	    return delta;
	}
    }
}
