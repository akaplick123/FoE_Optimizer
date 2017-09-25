package de.andre.process;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.andre.data.IFoEGameboard;
import de.andre.data.Tile;
import de.andre.data.TileState;
import de.andre.data.impl.Integer1DimArrayBoard;
import de.andre.process.util.ManuelPlacement;

@Component
public class DeepSearchPlacement extends AbstractOptimization {
    @Value("${exp.max.queue.size.lower.bound}")
    private int MAX_QUEUE_SIZE_LOWER_BOUND = 900_000;
    @Value("${exp.max.queue.size.upper.bound}")
    private int MAX_QUEUE_SIZE_UPPER_BOUND = 2_000_000;

    private Random r;
    private Deque<IFoEGameboard> queue = new LinkedList<>();
    private boolean queueIsFull = false;

    public void start() {
	long seed = new Random().nextLong();
	logParameter("MAX_QUEUE_SIZE_LOWER_BOUND", MAX_QUEUE_SIZE_LOWER_BOUND);
	logParameter("MAX_QUEUE_SIZE_UPPER_BOUND", MAX_QUEUE_SIZE_UPPER_BOUND);
	logParameter("random.seed", seed);

	// seed = -6542860481660332438L;
	r = new Random(seed);

	IFoEGameboard startingBoard = createRandomBoard();
	queue.offer(startingBoard);
	ManuelPlacement.createAndPrintReference(startingBoard);

	while (!queue.isEmpty()) {
	    IFoEGameboard gf = queue.poll();
	    addGamefieldToTopList(gf);

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

    private IFoEGameboard createRandomBoard() {
	return new Integer1DimArrayBoard(getGamefieldWidth(), getGamefieldHeight());
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
}
