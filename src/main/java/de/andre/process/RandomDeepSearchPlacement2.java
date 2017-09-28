package de.andre.process;

import java.util.ArrayList;
import java.util.Collections;
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
import de.andre.process.util.ManuelPlacement;

@Component
public class RandomDeepSearchPlacement2 extends AbstractOptimization {
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

	    // iterate over all currently known game fields and place a new tile
	    // (if possible)
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

    /**
     * try to add a new building to the game field
     * 
     * @param gamefield
     *            the game field to place the building at. Won't be changed.
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

	// it's 50/50 whether we try an house or an way tile
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

    /**
     * Choose at random
     * 
     * @param r
     *            the random number generator
     * @param options
     *            the set of options to choose from (shall never be empty)
     * @return exactly one option
     */
    private static Tile choose(Random r, List<Tile> options) {
	int index = r.nextInt(options.size());
	return options.get(index);
    }

    private class BestGamefields {
	/**
	 * a "key-value" store. Key is the number of used tiles and the value is
	 * an list of some game fields that uses that much titles
	 */
	private HashMap<Integer, LimitedList<IFoEGameboard>> mapByUsedTiles = new HashMap<>();

	/**
	 * add a new game field to the queue
	 * 
	 * @param field
	 *            the new game field
	 * @return the game field including some meta data
	 */
	public void offer(IFoEGameboard gamefield) {
	    int usedTiles = gamefield.getOccupiedTiles();

	    LimitedList<IFoEGameboard> allGamefields = mapByUsedTiles.get(usedTiles);
	    if (allGamefields == null) {
		allGamefields = new LimitedList<>(MAX_SAVED_GAMEFIELDS_PER_STEP, GAMEFIELD_COMPARATOR);
		mapByUsedTiles.put(usedTiles, allGamefields);
	    }

	    allGamefields.add(gamefield);
	}

	/**
	 * removes every game field but not the top N (by rating) from this
	 * queue
	 */
	public void reset() {
	    for (LimitedList<IFoEGameboard> entry : mapByUsedTiles.values()) {
		entry.shrink(MAX_SAVED_GAMEFIELDS_PER_RESET);
	    }
	    System.gc();
	}

	/**
	 * @return an iterator over all game fields that are currently checked.
	 */
	public Iterator<IFoEGameboard> getIterator() {
	    ArrayList<IFoEGameboard> tmp = new ArrayList<>();
	    for (LimitedList<IFoEGameboard> gamefields : mapByUsedTiles.values()) {
		for (IFoEGameboard gamefield : gamefields) {
		    tmp.add(gamefield);
		}
	    }
	    return tmp.iterator();
	}
    }

    /**
     * Compare by rating and occupied tiles. A higher rating and less occupied
     * tiles are better.
     * 
     * @author andre
     */
    public static class GamefieldComparator implements Comparator<IFoEGameboard> {
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

    public static class LimitedList<T> implements Iterable<T> {
	private final ArrayList<T> data;
	private final Comparator<T> comparator;
	private final int maxSize;

	public LimitedList(int maxSize, Comparator<T> comparator) {
	    this.maxSize = maxSize;
	    this.comparator = comparator;
	    this.data = new ArrayList<>(maxSize + 1);
	}

	/**
	 * Adds an item. When there is no space left, the item with the lowest
	 * rating will be removed.
	 * 
	 * @param item
	 *            the item to add
	 */
	public void add(T item) {
	    this.data.add(item);

	    if (this.data.size() > maxSize) {
		removeItemWithLowestRating();
	    }
	}

	private void removeItemWithLowestRating() {
	    int lowestItemIdx = 0;
	    T lowestItem = data.get(0);

	    for (int idx = 1; idx < data.size(); idx++) {
		T currentItem = data.get(idx);
		int compareValue = comparator.compare(currentItem, lowestItem);
		if (compareValue < 0) {
		    lowestItemIdx = idx;
		    lowestItem = currentItem;
		}
	    }

	    data.remove(lowestItemIdx);
	}

	/**
	 * shrinks that list and removes all items, but not the top-N ones
	 * 
	 * @param shrinkSize
	 *            number of items to keep in list
	 */
	public void shrink(int shrinkSize) {
	    if (this.data.size() <= shrinkSize) {
		// nothing to do
	    }

	    ArrayList<T> tmp = new ArrayList<>(this.data);
	    Collections.sort(tmp, comparator);
	    this.data.clear();
	    for (int idx = 0; idx < shrinkSize; idx++) {
		this.data.add(tmp.get(idx));
	    }
	}

	@Override
	public Iterator<T> iterator() {
	    return data.iterator();
	}
    }
}
