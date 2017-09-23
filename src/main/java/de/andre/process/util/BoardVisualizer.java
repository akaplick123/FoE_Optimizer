package de.andre.process.util;

import de.andre.data.IFoEGameboard;
import de.andre.data.TileState;
import lombok.extern.log4j.Log4j;

@Log4j
public class BoardVisualizer {
    private static void log(String msg) {
	log.info(msg);
    }

    public static void print(IFoEGameboard board) {
	log("==============================");
	log("- size    : " + board.getWidth() + " x " + board.getHeight());
	log("- rating  : " + board.getRating());
	log("- occupied: " + board.getOccupiedTiles() + "  (Houses = " + houses(board) + ", Ways = " + ways(board)
		+ ", Free = " + free(board) + ")");
	for (int y = 0; y < board.getHeight(); y++) {
	    StringBuilder line = new StringBuilder();
	    for (int x = 0; x < board.getWidth(); x++) {
		switch (board.getBuilding(x, y)) {
		case FREE:
		    line.append(" .");
		    break;
		case CASTLE:
		    line.append(" C");
		    break;
		case WAY:
		    line.append(" w");
		    break;
		case HOUSE:
		    line.append(" H");
		    break;
		default:
		    line.append(" ?");
		    break;
		}
	    }
	    log(line.toString());
	}
	log("");
    }

    private static int free(IFoEGameboard board) {
	return count(board, TileState.FREE);
    }

    private static int ways(IFoEGameboard board) {
	return count(board, TileState.WAY);
    }

    private static int houses(IFoEGameboard board) {
	return count(board, TileState.HOUSE);
    }

    private static int count(IFoEGameboard board, TileState building) {
	int cnt = 0;
	for (int y = 0; y < board.getHeight(); y++) {
	    for (int x = 0; x < board.getWidth(); x++) {
		if (board.getBuilding(x, y) == building) {
		    ++cnt;
		}
	    }
	}

	int areaPerBuilding = building.getHeight() * building.getWidth();
	return cnt / areaPerBuilding;
    }
}
