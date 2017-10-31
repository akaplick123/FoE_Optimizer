package de.andre.process;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.andre.data.IFoEGameboard;
import de.andre.data.db.DBExperiment;
import de.andre.data.db.DBExperimentParameter;
import de.andre.data.db.DBRepository;
import de.andre.data.db.DBSnapshot;
import de.andre.data.impl.Integer1DimArrayBoard;
import de.andre.process.util.BoardVisualizer;
import lombok.extern.log4j.Log4j;

@Log4j
public abstract class AbstractOptimization {
    @Value("${exp.gamefield.width}")
    private int GAMEFIELD_WIDTH = 24;
    @Value("${exp.gamefield.height}")
    private int GAMEFIELD_HEIGHT = 20;

    @Autowired
    private DBRepository repository;
    private DBExperiment exp;
    private int maxRatingUntilNow = Integer.MIN_VALUE;
    private IFoEGameboard bestRatingGamefield;

    protected abstract void start();

    private int getGamefieldWidth() {
	return GAMEFIELD_WIDTH;
    }

    private int getGamefieldHeight() {
	return GAMEFIELD_HEIGHT;
    }

    /**
     * @return an empty game field
     */
    protected IFoEGameboard createRandomBoard() {
	return new Integer1DimArrayBoard(getGamefieldWidth(), getGamefieldHeight());
    }

    protected void log(String msg) {
	log.info(msg);
    }

    private void initExperiment() {
	DBExperiment exp = new DBExperiment();
	exp.setDriver(getClass().getSimpleName());
	this.exp = repository.save(exp);
    }

    protected void logParameter(String name, Number value) {
	logParameter(name, "" + value);
    }

    protected void logParameter(String name, String value) {
	log(name + " = " + value);
	DBExperimentParameter param = new DBExperimentParameter();
	param.setKey(name);
	param.setValue(value);
	param.setExperiment(exp);
	repository.save(param);
    }

    protected void logAllParameter() {
	logParameter("GAMEFIELD_WIDTH", GAMEFIELD_WIDTH);
	logParameter("GAMEFIELD_HEIGHT", GAMEFIELD_HEIGHT);
    }

    /**
     * evaluate rating for the given game field and will add might add it to the
     * top-list
     * 
     * @param gamefield
     *            the game field to evaluate
     */
    protected void addGamefieldToTopList(IFoEGameboard gamefield) {
	synchronized (this) {
	    if (gamefield.getRating() > maxRatingUntilNow) {
		this.maxRatingUntilNow = gamefield.getRating();
		this.bestRatingGamefield = gamefield;
	    }
	}
    }

    private IFoEGameboard getBestRatedGamefield() {
	synchronized (this) {
	    return bestRatingGamefield;
	}
    }

    private int getBestRating() {
	synchronized (this) {
	    return maxRatingUntilNow;
	}
    }

    private void logSnapshot(IFoEGameboard board) {
	// collect data
	int houses = 0;
	int ways = 0;
	int tilesOccupied = 0;
	StringBuilder encodedField = new StringBuilder();

	for (int y = 0; y < board.getHeight(); y++) {
	    for (int x = 0; x < board.getWidth(); x++) {
		switch (board.getBuilding(x, y)) {
		case FREE:
		    encodedField.append(".");
		    break;
		case CASTLE:
		    encodedField.append("C");
		    tilesOccupied++;
		    break;
		case WAY:
		    encodedField.append("w");
		    ways++;
		    tilesOccupied++;
		    break;
		case HOUSE:
		    encodedField.append("H");
		    houses++;
		    tilesOccupied++;
		    break;
		}
	    }
	    encodedField.append("\n");
	}

	DBSnapshot snapshot = new DBSnapshot();
	snapshot.setExperiment(exp);
	snapshot.setRating(board.getRating());
	snapshot.setNumberOfHouses(houses);
	snapshot.setWays(ways);
	snapshot.setTilesOccupied(tilesOccupied);
	snapshot.setEncodedField(encodedField.toString());
	snapshot.setMemUsage(getMemUsage());
	repository.save(snapshot);

	// print board to sysout
	BoardVisualizer.print(board);
    }

    private long getMemUsage() {
	Runtime runtime = Runtime.getRuntime();
	return (runtime.totalMemory() - runtime.freeMemory());
    }

    public void runDBExperiment() {
	initExperiment();
	logAllParameter();

	ProgressLoggingThread logger = new ProgressLoggingThread();
	logger.start();

	try {
	    start();
	} finally {
	    logger.interrupt();
	    logSnapshot(getBestRatedGamefield());
	}
    }

    private class ProgressLoggingThread extends Thread {
	public ProgressLoggingThread() {
	    super("ProgressLogger");
	}

	@Override
	public void run() {
	    try {
		IFoEGameboard bestLoggedGamefield = null;
		while (true) {
		    TimeUnit.SECONDS.sleep(10);
		    System.gc();
		    IFoEGameboard currentBestGamefield = getBestRatedGamefield();
		    if (currentBestGamefield != null && currentBestGamefield.equals(bestLoggedGamefield)) {
			// don't repeat myself
			log("no new best field (current rating to beat is " + getBestRating() + ")");
		    } else if (currentBestGamefield != null) {
			log("best field until now is:");
			bestLoggedGamefield = currentBestGamefield;
			logSnapshot(bestLoggedGamefield);
		    }
		}
	    } catch (InterruptedException e) {
	    }
	}
    }
}
