package de.andre.process.util;

import de.andre.data.IFoEGameboard;
import de.andre.data.TileState;
import lombok.extern.log4j.Log4j;

@Log4j
public class ManuelPlacement {
    private static void log(String msg) {
	log.info(msg);
    }

    public static void createAndPrintReference(IFoEGameboard emptyBoard) {
	IFoEGameboard gf = emptyBoard.clone();

	// build zone behind castle
	for (int x = TileState.HOUSE.getWidth() + TileState.CASTLE.getWidth(); x < gf
		.getWidth(); x += (TileState.HOUSE.getWidth() + 1 + TileState.HOUSE.getWidth())) {
	    int xHouseLeft = x - TileState.HOUSE.getWidth();
	    int xWay = x;
	    int xHouseRight = (x + TileState.HOUSE.getWidth() < gf.getWidth() ? x + 1 : xHouseLeft);

	    // use last row to connect all ways
	    for (int y = 0; y < TileState.CASTLE.getHeight(); y++) {
		if (y % TileState.HOUSE.getHeight() == (TileState.HOUSE.getHeight() - 1)) {
		    // build houses
		    gf.placeBuildingWithoutAnyChecks(xHouseLeft, y - TileState.HOUSE.getHeight() + 1, TileState.HOUSE);
		    gf.placeBuildingWithoutAnyChecks(xHouseRight, y - TileState.HOUSE.getHeight() + 1, TileState.HOUSE);
		}
		// always build ways
		gf.placeBuildingWithoutAnyChecks(xWay, y, TileState.WAY);
	    }
	}

	// build a way directly behind castle
	for (int xWay = 0; xWay < gf.getWidth(); xWay++) {
	    int yWay = TileState.CASTLE.getHeight();
	    gf.placeBuildingWithoutAnyChecks(xWay, yWay, TileState.WAY);
	}

	// fill the rest with pattern H H w (H H)?
	for (int x = 0; x < gf.getWidth(); x += (TileState.HOUSE.getWidth() + 1 + TileState.HOUSE.getWidth())) {
	    int xHouseLeft = x - TileState.HOUSE.getWidth();
	    int xWay = x;
	    int xHouseRight = (x + TileState.HOUSE.getWidth() < gf.getWidth() ? x + 1 : xHouseLeft);

	    // use last row to connect all ways
	    for (int y = TileState.CASTLE.getHeight() + TileState.WAY.getHeight(); y < gf.getHeight(); y++) {
		int yDeltaToWay = y - TileState.CASTLE.getHeight() - TileState.WAY.getHeight();

		if (yDeltaToWay % TileState.HOUSE.getHeight() == (TileState.HOUSE.getHeight() - 1)) {
		    // build houses
		    gf.placeBuildingWithoutAnyChecks(xHouseLeft, y - TileState.HOUSE.getHeight() + 1, TileState.HOUSE);
		    gf.placeBuildingWithoutAnyChecks(xHouseRight, y - TileState.HOUSE.getHeight() + 1, TileState.HOUSE);
		}
		// always build ways
		gf.placeBuildingWithoutAnyChecks(xWay, y, TileState.WAY);
	    }
	}

	// set castle at 0 / 0
	gf.placeBuildingWithoutAnyChecks(0, 0, TileState.CASTLE);

	log("Reference to be challenged");
	BoardVisualizer.print(gf);

    }

    public static void createAndPrintReference_v1(IFoEGameboard emptyBoard) {
	// build a cross
	// pattern: H H w H H
	IFoEGameboard gf = emptyBoard.clone();

	// build zone behind castle
	for (int x = TileState.HOUSE.getWidth() + TileState.CASTLE.getWidth(); x < gf
		.getWidth(); x += (TileState.HOUSE.getWidth() + 1 + TileState.HOUSE.getWidth())) {
	    int xHouseLeft = x - TileState.HOUSE.getWidth();
	    int xWay = x;
	    int xHouseRight = (x + TileState.HOUSE.getWidth() < gf.getWidth() ? x + 1 : xHouseLeft);

	    // use last row to connect all ways
	    for (int y = 0; y < gf.getHeight() - 1; y++) {
		if (y % TileState.HOUSE.getHeight() == (TileState.HOUSE.getHeight() - 1)) {
		    // build houses
		    gf.placeBuildingWithoutAnyChecks(xHouseLeft, y - TileState.HOUSE.getHeight() + 1, TileState.HOUSE);
		    gf.placeBuildingWithoutAnyChecks(xHouseRight, y - TileState.HOUSE.getHeight() + 1, TileState.HOUSE);
		}
		// always build ways
		gf.placeBuildingWithoutAnyChecks(xWay, y, TileState.WAY);
	    }
	}

	// build zone in shadow of castle
	for (int x = TileState.HOUSE.getWidth(); x < TileState.CASTLE
		.getWidth(); x += (TileState.HOUSE.getWidth() + 1 + TileState.HOUSE.getWidth())) {
	    int xHouseLeft = x - TileState.HOUSE.getWidth();
	    int xWay = x;
	    int xHouseRight = (x + TileState.HOUSE.getWidth() < TileState.CASTLE.getWidth() ? x + 1 : xHouseLeft);

	    // use last row to connect all ways
	    for (int yWay = TileState.CASTLE.getHeight(), yHouse = 0; yWay < gf.getHeight() - 1; yWay++, yHouse++) {
		if (yHouse % TileState.HOUSE.getHeight() == (TileState.HOUSE.getHeight() - 1)) {
		    // build houses
		    gf.placeBuildingWithoutAnyChecks(xHouseLeft,
			    yHouse + TileState.CASTLE.getHeight() - TileState.HOUSE.getHeight() + 1, TileState.HOUSE);
		    gf.placeBuildingWithoutAnyChecks(xHouseRight,
			    yHouse + TileState.CASTLE.getHeight() - TileState.HOUSE.getHeight() + 1, TileState.HOUSE);
		}
		// always build ways
		gf.placeBuildingWithoutAnyChecks(xWay, yWay, TileState.WAY);
	    }
	}

	// build last row
	for (int xWay = 0; xWay < gf.getWidth(); xWay++) {
	    int yWay = gf.getHeight() - 1;
	    gf.placeBuildingWithoutAnyChecks(xWay, yWay, TileState.WAY);
	}

	// set castle at 0 / 0
	gf.placeBuildingWithoutAnyChecks(0, 0, TileState.CASTLE);

	log("Reference to be challenged");
	BoardVisualizer.print(gf);
    }
}
