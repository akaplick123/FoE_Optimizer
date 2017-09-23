package de.andre.process.util;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This iterator can be expanded with new items during iteration.
 * 
 * @author Andre
 *
 * @param <T>
 */
public class ModifieableIterator<T> implements Iterator<T> {
	private Deque<T> queue = new LinkedList<>();

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public T next() {
		return queue.poll();
	}

	public void offer(T v) {
		this.queue.offerFirst(v);
	}

	public void clear() {
		this.queue.clear();
	}
}