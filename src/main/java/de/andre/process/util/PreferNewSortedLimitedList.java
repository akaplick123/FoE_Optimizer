package de.andre.process.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * A list with the following features:
 * <ul>
 * <li>sorted with an {@link Comparator}</li>
 * <li>last added item will be preferred over earlier added items, when items
 * are equal by {@link Comparator}</li>
 * <li>limited in size</li>
 * </ul>
 * 
 * @author Andre
 *
 * @param <T>
 */
public class PreferNewSortedLimitedList<T> implements Iterable<T> {
    private final int maxSize;
    private final Comparator<T> comparator;
    private Node root;
    private int size;

    /**
     * @param maxSize
     *            the maximum size
     * @param comparator
     *            the comparator to sort all items
     */
    public PreferNewSortedLimitedList(int maxSize, Comparator<T> comparator) {
	assert maxSize >= 1 : "maxSize must greater than 0";
	assert comparator != null : "comparator must not be null";

	this.maxSize = maxSize;
	this.comparator = comparator;
	this.size = 0;
	this.root = null;

	new TreeMap<String, String>().put("", "");
    }

    /**
     * adds the new element based on {@link Comparable#compareTo(Object)} value.
     * So that the last element has the lowest rating.
     * 
     * @param obj
     *            the new element to add
     */
    public void add(T obj) {
	if (root == null) {
	    root = new Node(obj);
	} else {
	    root.add(obj);
	}

	size++;
	if (size > maxSize) {
	    removeLast();
	}
    }

    /**
     * removes the last element. this is the element with the lowest value
     * (based on {@link Comparable#compareTo(Object)}).
     */
    public void removeLast() {
	if (root != null) {
	    // remove lowest value
	    if (root.lower == null) {
		// root is the lowest value
		root = root.greaterOrEqual;
		if (root != null) {
		    root.parent = null;
		}
	    } else {
		Node current = root;
		while (current.lower != null) {
		    current = current.lower;
		}
		// we have to remove Node "current"
		Node currentParent = current.parent;
		Node currentGreater = current.greaterOrEqual;

		currentParent.lower = currentGreater;
		if (currentGreater != null) {
		    currentGreater.parent = currentParent;
		}
		current.parent = null;
		current.greaterOrEqual = null;
	    }

	    size--;
	}
    }

    public void shrink(int shrinkSize) {
	while (size > shrinkSize) {
	    removeLast();
	}
    }

    public boolean contains(T obj) {
	if (root == null) {
	    return false;
	}

	return root.contains(obj);
    }

    @Override
    public Iterator<T> iterator() {
	return new ReverseTreeIterator(root);
    }

    public int size() {
	return size;
    }

    private class Node {
	private final T value;
	private Node parent;
	private Node lower;
	private Node greaterOrEqual;

	public Node(T value) {
	    this.value = value;
	}

	public boolean contains(T obj) {
	    int comp = comparator.compare(obj, value);
	    if (comp < 0) {
		// obj is lower than this.value
		if (lower == null) {
		    return false;
		}
		return lower.contains(obj);
	    } else if (comp > 0) {
		// obj is greater than this.value
		if (greaterOrEqual == null) {
		    return false;
		}
		return greaterOrEqual.contains(obj);
	    } else {
		// obj is equal to this.value (by compareTo)
		if (value.equals(obj)) {
		    return true;
		}
		if (greaterOrEqual == null) {
		    return false;
		}
		return greaterOrEqual.contains(obj);
	    }
	}

	public void add(T newValue) {
	    int comp = comparator.compare(newValue, value);
	    if (comp < 0) {
		// new value is lower than
		addLowerValue(newValue);
	    } else {
		// new value is greater than or equal to this.value this.value
		addGreaterValue(newValue);
	    }
	}

	private void addLowerValue(T newValue) {
	    if (lower == null) {
		// we can it that node
		Node newNode = new Node(newValue);
		this.lower = newNode;
		newNode.parent = this;
		return;
	    }

	    lower.add(newValue);
	}

	private void addGreaterValue(T newValue) {
	    if (greaterOrEqual == null) {
		// we can it that node
		Node newNode = new Node(newValue);
		this.greaterOrEqual = newNode;
		newNode.parent = this;
		return;
	    }

	    greaterOrEqual.add(newValue);
	}

	@Override
	public String toString() {
	    return "[this=" + value + "; lowerOrEqual=" + (lower == null ? "NULL" : lower.value) + "; greater="
		    + (greaterOrEqual == null ? "NULL" : greaterOrEqual.value) + "]";
	}
    }

    private class ReverseTreeIterator implements Iterator<T> {
	private Iterator<T> iterator;

	public ReverseTreeIterator(Node root) {
	    if (root == null) {
		// go to lowest value
		this.iterator = new ArrayList<T>().iterator();
	    } else {
		// collect all values
		ArrayList<T> list = new ArrayList<>();
		addAll(list, root);
		this.iterator = list.iterator();
	    }
	}

	private void addAll(ArrayList<T> list, Node root) {
	    if (root.greaterOrEqual != null) {
		addAll(list, root.greaterOrEqual);
	    }
	    list.add(root.value);
	    if (root.lower != null) {
		addAll(list, root.lower);
	    }
	}

	@Override
	public boolean hasNext() {
	    return iterator.hasNext();
	}

	@Override
	public T next() {
	    return iterator.next();
	}
    }
}