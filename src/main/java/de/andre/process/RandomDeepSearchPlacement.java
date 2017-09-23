package de.andre.process;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.data.impl.Integer1DimArrayBoard;
import de.andre.process.util.BoardVisualizer;
import de.andre.process.util.ManuelPlacement;
import de.andre.process.util.ModifieableIterator;
import de.andre.process.util.PreferNewSortedLimitedList;
import lombok.extern.log4j.Log4j;

@Log4j
public class RandomDeepSearchPlacement {
    private static final GamefieldComparator GAMEFIELD_COMPARATOR = new GamefieldComparator();

    private static final int GAMEFIELD_WIDTH = 24;
    private static final int GAMEFIELD_HEIGHT = 20;
    private static final int MAX_SAVED_GAMEFIELDS_PER_STEP = 300;
    private static final int MAX_SAVED_GAMEFIELDS_PER_RESET = 2;
    private static final int MAX_ITERATIONS_BEFORE_RESET = 500;

    public static void main(String[] args) throws Exception {
	new RandomDeepSearchPlacement().start();
    }

    private Random r;
    private int maxRatingUntilNow = Integer.MIN_VALUE;
    private IFoEGameboard bestRatingGamefield;

    private void log(String msg) {
	log.info(msg);
    }

    private void start() {
	long seed = new Random().nextLong();
	// seed = -6542860481660332438L;
	log("start with Random.seed = " + seed);
	r = new Random(seed);
	log("MAX_SAVED_GAMEFIELDS_PER_STEP = " + MAX_SAVED_GAMEFIELDS_PER_STEP);
	log("MAX_SAVED_GAMEFIELDS_PER_RESET = " + MAX_SAVED_GAMEFIELDS_PER_RESET);
	log("MAX_ITERATIONS_BEFORE_RESET = " + MAX_ITERATIONS_BEFORE_RESET);

	ProgressLoggingThread logger = new ProgressLoggingThread(this);
	logger.start();

	BestGamefields queue = new BestGamefields();
	IFoEGameboard startingBoard = createRandomBoard(GAMEFIELD_WIDTH, GAMEFIELD_HEIGHT);
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

    private IFoEGameboard createRandomBoard(int gamefieldWidth, int gamefieldHeight) {
	return new Integer1DimArrayBoard(gamefieldWidth, gamefieldHeight);
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

    private void addGamefieldToTopList(IFoEGameboard gamefieldClone) {
	synchronized (this) {
	    if (gamefieldClone.getRating() > maxRatingUntilNow) {
		this.maxRatingUntilNow = gamefieldClone.getRating();
		this.bestRatingGamefield = gamefieldClone;
	    }
	}
    }

    public IFoEGameboard getBestRatedGamefield() {
	synchronized (this) {
	    return bestRatingGamefield;
	}
    }

    public int getBestRating() {
	synchronized (this) {
	    return maxRatingUntilNow;
	}
    }

    private static Tile choose(Random r, List<Tile> options) {
	if (options.isEmpty()) {
	    throw new NullPointerException("No options to choose from.");
	}

	int index = r.nextInt(options.size());
	return options.get(index);
    }

    private static class BestGamefields {
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

    public static class ProgressLoggingThread extends Thread {
	private final RandomDeepSearchPlacement experiment;

	public ProgressLoggingThread(RandomDeepSearchPlacement experiment) {
	    super("ProgressLogger");
	    this.experiment = experiment;
	}

	@Override
	public void run() {
	    try {
		IFoEGameboard bestLoggedGamefield = null;
		while (true) {
		    TimeUnit.SECONDS.sleep(10);
		    System.gc();
		    IFoEGameboard currentBestGamefield = experiment.getBestRatedGamefield();
		    if (currentBestGamefield.equals(bestLoggedGamefield)) {
			// don't repeat myself
			experiment.log(
				"no new best field (current rating to beat is " + experiment.getBestRating() + ")");
		    } else {
			experiment.log("best field until now is:");
			bestLoggedGamefield = currentBestGamefield;
			BoardVisualizer.print(bestLoggedGamefield);
		    }
		}
	    } catch (InterruptedException e) {
	    }
	}
    }
}
