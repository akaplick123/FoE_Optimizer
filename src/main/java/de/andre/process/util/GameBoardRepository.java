package de.andre.process.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import de.andre.data.IFoEGameboard;

public class GameBoardRepository {
    private final HashMap<Integer, List<IFoEGameboard>> groups = new HashMap<>();
    private int size = 0;
    private int nextStartingKey = 0;

    public void add(IFoEGameboard board) {
	int key = board.getOccupiedTiles();
	List<IFoEGameboard> group = groups.get(key);
	if (group == null) {
	    group = new ArrayList<>();
	    groups.put(key, group);
	}

	group.add(board);
	size++;
    }

    public int size() {
	return size;
    }

    private void recalcSize() {
	AtomicInteger cnt = new AtomicInteger(0);
	groups.values().stream().forEach(e -> {
	    cnt.getAndAdd(e.size());
	});
	this.size = cnt.get();
    }

    public void shrink(int desiredItems) {
	ArrayList<Integer> allKeys = new ArrayList<>(groups.keySet());
	for (int key : allKeys) {
	    List<IFoEGameboard> currentList = groups.get(key);
	    if (currentList.size() > desiredItems) {
		// sort descending by rating within
		currentList.sort((a, b) -> {
		    return b.getRating() - a.getRating();
		});

		List<IFoEGameboard> newList = new ArrayList<>(currentList.subList(0, desiredItems));
		currentList.clear();
		groups.put(key, newList);
	    }
	}

	recalcSize();
    }

    public IFoEGameboard nextStartingBoard(Random r) {
	if (groups.containsKey(nextStartingKey)) {
	    List<IFoEGameboard> choices = groups.get(nextStartingKey);
	    if (!choices.isEmpty()) {
		nextStartingKey++;
		return chooseAtRandom(choices, r);
	    }
	}

	ArrayList<Integer> allKeys = new ArrayList<>(groups.keySet());
	Collections.sort(allKeys);
	// search for first Key that is greater or equal to 'nextStartingKey'
	// and a none empty group
	for (int key : allKeys) {
	    if (key >= nextStartingKey && !groups.get(key).isEmpty()) {
		List<IFoEGameboard> choices = groups.get(key);
		nextStartingKey = key + 1;
		return chooseAtRandom(choices, r);
	    }
	}

	// we haven't found anything, so may be nextStartingKey is to big. So
	// choose first key that has a none empty group
	for (int key : allKeys) {
	    if (!groups.get(key).isEmpty()) {
		List<IFoEGameboard> choices = groups.get(key);
		nextStartingKey = key + 1;
		return chooseAtRandom(choices, r);
	    }
	}

	// now we haven't found anything, so throw an Exception
	throw new UnsupportedOperationException("Cannot choode from an empty Repository.");
    }

    /**
     * For UnitTests only.
     * 
     * @return
     */
    Collection<IFoEGameboard> collect() {
	final ArrayList<IFoEGameboard> result = new ArrayList<>();
	groups.values().stream().forEach(e -> {
	    result.addAll(e);
	});
	return result;
    }

    private static IFoEGameboard chooseAtRandom(List<IFoEGameboard> choices, Random r) {
	return choices.get(r.nextInt(choices.size()));
    }

}
