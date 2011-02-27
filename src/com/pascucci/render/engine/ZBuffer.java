package com.pascucci.render.engine;

import java.util.ArrayList;

/**
 * Provides a means of hidden face removal by sorting objects by their z values.
 * I know, it's not a true z-buffer, but hey, I couldn't think of another name.
 * 
 * @author Nick Pascucci <nick@kestrelrobotics.com>
 */

public class ZBuffer {
	private ArrayList<Face> sortedFaces;

	/**
	 * Creates an empty ZBuffer.
	 */
	public ZBuffer() {
		sortedFaces = new ArrayList<Face>();
	}

	/**
	 * Create a new ZBuffer for the entities in the given list.
	 * 
	 * @param list
	 */
	public ZBuffer(ArrayList<Entity3D> list) {
		sortedFaces = new ArrayList<Face>();
		for (Entity3D e : list) {
			sortedFaces.addAll(e.getFaces());
		}
		sort();
	}

	/**
	 * Sorts the buffer.
	 */
	public void sort() {
		quickSort(sortedFaces, 0, sortedFaces.size() - 1);
	}

	/**
	 * Sorts the array list using quicksort by average z value, largest first.
	 * 
	 * @param collection
	 * @param left
	 * @param right
	 */
	private void quickSort(ArrayList<Face> collection, int left, int right) {
		if (left >= right)
			return;
		int i = left;
		int j = right;
		double pivot = (collection.get((i + j) / 2)).zavg;
		while (i <= j) {
			// Notice the inversion here!
			while ((collection.get(i)).zavg < pivot) {
				i++;
			}
			// We want this to be sorted from greatest to least.
			while ((collection.get(j)).zavg > pivot) {
				j--;
			}
			if (i <= j) {
				swap(collection, i, j);
				i++;
				j--;
			}
		}
		if (left < i) {
			quickSort(collection, left, i - 1);
		}
		if (right > i) {
			quickSort(collection, i, right);
		}
	}

	/**
	 * Swaps two entities in an ArrayList
	 * 
	 * @param collection
	 * @param i
	 * @param j
	 */
	private void swap(ArrayList<Face> collection, int i, int j) {
		// Gets the object at i, and replaces it with the object at j
		Face iObj = collection.set(i, collection.get(j));
		// Then puts that object back in at position j.
		// Pretty standard.
		collection.set(j, iObj);
	}

	/**
	 * Gets the sorted ArrayList.
	 * 
	 * Be sure to call sort() first!
	 * @return An ArrayList of faces, sorted by Z value.
	 */
	public ArrayList<Face> getSortedFaces() {
		return sortedFaces;
	}

	/**
	 * Adds an entity to the list. The list must then be sorted by the user.
	 * 
	 * @param e
	 */
	public void addEntity(Entity3D e) {
		sortedFaces.addAll(e.getFaces());
	}

	/**
	 * Removes an entity from the list. This operation preserves order.
	 * 
	 * @param e
	 */
	public void removeEntity(Entity3D e) {
		sortedFaces.removeAll(e.getFaces());
	}

	/**
	 * Removes all faces from the ZBuffer.
	 */
	public void clear() {
		sortedFaces.clear();
	}
}
