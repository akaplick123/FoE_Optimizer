package de.andre.process;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.andre.data.IFoEGameboard;
import de.andre.data.db.DBExperiment;
import de.andre.data.db.DBExperimentParameter;
import de.andre.data.db.DBRepository;
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

    protected int getGamefieldWidth() {
	return GAMEFIELD_WIDTH;
    }

    protected int getGamefieldHeight() {
	return GAMEFIELD_HEIGHT;
    }

    protected void log(String msg) {
	log.info(msg);
    }

    protected void logParameter(String name, Number value) {
	logParameter(name, "" + value);
    }

    protected void initExperiment() {
	DBExperiment exp = new DBExperiment();
	exp.setDriver(getClass().getSimpleName());
	this.exp = repository.save(exp);
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

    protected void addGamefieldToTopList(IFoEGameboard gamefield) {
	synchronized (this) {
	    if (gamefield.getRating() > maxRatingUntilNow) {
		this.maxRatingUntilNow = gamefield.getRating();
		this.bestRatingGamefield = gamefield;
	    }
	}
    }

    protected IFoEGameboard getBestRatedGamefield() {
	synchronized (this) {
	    return bestRatingGamefield;
	}
    }

    protected int getBestRating() {
	synchronized (this) {
	    return maxRatingUntilNow;
	}
    }

    protected void logSnapshot(IFoEGameboard board) {
	// TODO
	BoardVisualizer.print(board);
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
	}
    }

    public class ProgressLoggingThread extends Thread {
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
