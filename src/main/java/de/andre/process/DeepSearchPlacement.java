package de.andre.process;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.data.impl.Integer1DimArrayBoard;
import de.andre.process.util.BoardVisualizer;
import de.andre.process.util.ManuelPlacement;
import lombok.extern.log4j.Log4j;

@Log4j
public class DeepSearchPlacement {
    private static final int GAMEFIELD_WIDTH = 24;
    private static final int GAMEFIELD_HEIGHT = 20;

    private static final int MAX_QUEUE_SIZE_LOWER_BOUND = 900_000;
    private static final int MAX_QUEUE_SIZE_UPPER_BOUND = 2_000_000;

    public static void main(String[] args) throws Exception {
	new DeepSearchPlacement().start();
    }

    private int maxRatingUntilNow = Integer.MIN_VALUE;
    private IFoEGameboard bestRatingGamefield;

    private void log(String msg) {
	log.info(msg);
    }

    private Random r;
    private Deque<IFoEGameboard> queue = new LinkedList<>();
    private boolean queueIsFull = false;

    private void start() {
	log("start");
	r = new Random();

	ProgressLoggingThread logger = new ProgressLoggingThread(this);
	logger.start();

	IFoEGameboard startingBoard = createRandomBoard(GAMEFIELD_WIDTH, GAMEFIELD_HEIGHT);
	queue.offer(startingBoard);
	ManuelPlacement.createAndPrintReference(startingBoard);

	while (!queue.isEmpty()) {
	    IFoEGameboard gf = queue.poll();
	    addGamefieldToTopList(gf.getRating(), gf);

	    if (gf.getOccupiedTiles() == 0) {
		// get all options for castle placement
		List<Tile> options = gf.getPlacementOptions(TileState.CASTLE);
		for (Tile option : options) {
		    IFoEGameboard gfClone = gf.clone();
		    gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.CASTLE);
		    offer(queue, gfClone);
		}
	    } else {
		// get all options for way placement
		List<Tile> options = gf.getPlacementOptions(TileState.WAY);
		for (Tile option : options) {
		    IFoEGameboard gfClone = gf.clone();
		    gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.WAY);
		    offer(queue, gfClone);
		}

		// get all options for house placement
		options = gf.getPlacementOptions(TileState.HOUSE);
		for (Tile option : options) {
		    IFoEGameboard gfClone = gf.clone();
		    gfClone.placeBuildingWithoutAnyChecks(option.getX(), option.getY(), TileState.HOUSE);
		    offer(queue, gfClone);
		}
	    }
	}
    }

    private IFoEGameboard createRandomBoard(int gamefieldWidth, int gamefieldHeight) {
	return new Integer1DimArrayBoard(gamefieldWidth, gamefieldHeight);
    }

    private void offer(Deque<IFoEGameboard> queue, IFoEGameboard gf) {
	if (queueIsFull) {
	    if (queue.size() <= MAX_QUEUE_SIZE_LOWER_BOUND) {
		queueIsFull = false;
	    }

	    // skip that fields
	    return;
	}

	if (queue.size() >= MAX_QUEUE_SIZE_UPPER_BOUND) {
	    queueIsFull = true;
	    try {
		log("Queue is full. So sleep and make GC.");
		System.gc();
		TimeUnit.SECONDS.sleep(10);
	    } catch (InterruptedException e) {
	    }
	}

	// offer new field at beginning or the end of queue 
	if (r.nextInt(5) == 0) {
	    queue.offerLast(gf);
	} else {
	    queue.offerFirst(gf);
	}
    }

    private void addGamefieldToTopList(int rating, IFoEGameboard gamefieldClone) {
	synchronized (this) {
	    if (rating > maxRatingUntilNow) {
		this.maxRatingUntilNow = rating;
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

    public static class ProgressLoggingThread extends Thread {
	private final DeepSearchPlacement experiment;

	public ProgressLoggingThread(DeepSearchPlacement experiment) {
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
			experiment.log("No new best field. Current rating to beat is " + experiment.getBestRating()
				+ ". Queue.size = " + experiment.queue.size());
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
