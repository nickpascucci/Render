package com.pascucci.render.engine;

import java.util.ArrayList;

/**
 * Contains data on entities contained in a 3D scene, including a ZBuffer which
 * it maintains automatically.
 * 
 * @author Nick Pascucci <nick@kestrelrobotics.com>
 */

public class Scene {
	private ArrayList<Entity3D> entities;
	private Point3D light;
	private ZBuffer buffer;
	public double ambient; // Ambient lighting

	/**
	 * Creates a new scene with no objects and default lighting settings.
	 */
	public Scene() {
		entities = new ArrayList<Entity3D>();
		buffer = new ZBuffer();
		light = new Point3D(0, 0, 800);
		ambient = .3;
	}

	/**
	 * Gets an ArrayList containing all of the entities in this scene.
	 * @return
	 */
	public ArrayList<Entity3D> getEntities() {
		return entities;
	}

	/**
	 * Gets a reference to the scene light.
	 * @return
	 */
	public Point3D getLight() {
		return light;
	}

	/**
	 * Sets a new light to the scene.
	 * @param l
	 */
	public void setLight(Point3D l) {
		light = l;
	}
	
	/**
	 * Adds an entity to the scene, so it can be rendered.
	 * @param entity
	 */
	public void addEntity(Entity3D entity) {
		entities.add(entity);
		buffer.addEntity(entity);
	}

	/**
	 * Removes an entity from the scene.
	 * @param entity
	 */
	public void removeEntity(Entity3D entity) {
		entities.remove(entity);
		buffer.removeEntity(entity);
	}

	/**
	 * Removes all entities from the scene.
	 */
	public void removeAll() {
		entities.clear();
		buffer.clear();
	}

	/**
	 * Gets the ZBuffer for the scene.
	 * @return
	 */
	public ZBuffer getBuffer() {
		return buffer;
	}
}
