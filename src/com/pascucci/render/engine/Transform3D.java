package com.pascucci.render.engine;

import java.util.ArrayList;

/**
 * Utility class for 3D transforms.
 * 
 * @author Nick Pascucci <nick@kestrelrobotics.com>
 */
public class Transform3D {

	public static final int X_AXIS = 0;
	public static final int Y_AXIS = 1;
	public static final int Z_AXIS = 2;

	private double d;
	private double alpha;
	private double beta;
	private static boolean debug = false;

	/**
	 * Rotates a 3D entity around the given axis by the given angle.
	 * 
	 * @param axis
	 *            The axis of rotation.
	 * @param e
	 *            The entity to rotate.
	 * @param theta
	 *            The angle to rotate.
	 */
	public static void rotate(int axis, Entity3D e, double theta) {
		switch (axis) {
		case X_AXIS:
			rotateX(e, theta);
			break;
		case Y_AXIS:
			rotateY(e, theta);
			break;
		case Z_AXIS:
			rotateZ(e, theta);
			break;
		}
	}

	public static void rotate(Vector3D axis, Entity3D e, double theta) {
		Vector3D unit = axis.toUnitVector();
		if (debug)
			System.out.println("Axis unit vector: " + unit);
		/*
		 * We can apply a series of rotations to the object to place it on the Z
		 * axis, and then perform our desired rotation around that axis and
		 * rotate the object back.
		 */
		double d = Math.sqrt(unit.y * unit.y + unit.z * unit.z);
		if (debug)
			System.out.println("d: " + d);
		double alpha = Math.acos(unit.z / d);
		if (debug)
			System.out.println("Alpha: " + alpha);
		double beta = Math.atan(unit.x / d);
		if (debug)
			System.out.println("Beta: " + beta);
		// Rotate onto the z/x plane
		rotateX(e, alpha);
		// Rotate onto z axis
		rotateY(e, beta);
		// Perform rotation
		rotateZ(e, theta);
		// rotate back
		rotateY(e, -beta);
		rotateX(e, -alpha);
	}

	public static void rotateScene(Scene s, double thetaX, double thetaY) {
		rotateSceneX(s, thetaX);
		rotateSceneY(s, thetaY);
	}

	public static void rotateSceneX(Scene s, double theta) {
		ArrayList<Entity3D> entities = s.getEntities();
		for (Entity3D e : entities) {
			rotateX(e, theta);
		}
		rotateX(s.getLight(), theta);
	}

	public static void rotateSceneY(Scene s, double theta) {
		ArrayList<Entity3D> entities = s.getEntities();
		for (Entity3D e : entities) {
			rotateY(e, theta);
		}
		rotateY(s.getLight(), theta);
	}

	public void rotate(Entity3D e, double theta) {
		// Rotate onto the z/x plane
		rotateX(e, alpha);
		// Rotate onto z axis
		rotateY(e, beta);
		// Perform rotation
		rotateZ(e, theta);
		// rotate back
		rotateY(e, -beta);
		rotateX(e, -alpha);
	}

	public void setAxis(Vector3D axis) {
		Vector3D unit = axis.toUnitVector();
		if (debug)
			System.out.println("Axis unit vector: " + unit);
		/*
		 * We can apply a series of rotations to the object to place it on the Z
		 * axis, and then perform our desired rotation around that axis and
		 * rotate the object back.
		 */
		d = Math.sqrt(unit.y * unit.y + unit.z * unit.z);
		if (debug)
			System.out.println("d: " + d);
		alpha = Math.atan(unit.y / unit.z);
		if (debug)
			System.out.println("Alpha: " + alpha);
		beta = Math.atan(unit.x / d);
		if (debug)
			System.out.println("Beta: " + beta);
	}

	private static void rotateX(Entity3D e, double theta) {
		ArrayList<Point3D> points = e.getPoints();
		for (Point3D p : points) {
			double x = p.x;
			double y = p.y;
			double z = p.z;
			p.setCoordinates(x, y * Math.cos(theta) - z * Math.sin(theta), y
					* Math.sin(theta) + z * Math.cos(theta));
		}
		ArrayList<Face> faces = e.getFaces();
		for (Face f : faces) {
			f.computeAverageZ();
		}
	}

	private static void rotateX(Point3D p, double theta) {
		double x = p.x;
		double y = p.y;
		double z = p.z;
		p.setCoordinates(x, y * Math.cos(theta) - z * Math.sin(theta),
				y * Math.sin(theta) + z * Math.cos(theta));
	}

	private static void rotateY(Entity3D e, double theta) {
		ArrayList<Point3D> points = e.getPoints();
		for (Point3D p : points) {
			double x = p.x;
			double y = p.y;
			double z = p.z;
			p.setCoordinates((x * Math.cos(theta) + z * Math.sin(theta)), y, (z
					* Math.cos(theta) - x * Math.sin(theta)));
		}
		ArrayList<Face> faces = e.getFaces();
		for (Face f : faces) {
			f.computeAverageZ();
		}
	}

	private static void rotateY(Point3D p, double theta) {
		double x = p.x;
		double y = p.y;
		double z = p.z;
		p.setCoordinates((x * Math.cos(theta) + z * Math.sin(theta)), y, (z
				* Math.cos(theta) - x * Math.sin(theta)));
	}

	private static void rotateZ(Entity3D e, double theta) {
		ArrayList<Point3D> points = e.getPoints();
		for (Point3D p : points) {
			double x = p.x;
			double y = p.y;
			double z = p.z;
			p.setCoordinates(x * Math.cos(theta) - y * Math.sin(theta), y
					* Math.cos(theta) + x * Math.sin(theta), z);
		}
		ArrayList<Face> faces = e.getFaces();
		for (Face f : faces) {
			f.computeAverageZ();
		}
	}

	private static void rotateZ(Point3D p, double theta) {
		double x = p.x;
		double y = p.y;
		double z = p.z;
		p.setCoordinates(x * Math.cos(theta) - y * Math.sin(theta),
				y * Math.cos(theta) + x * Math.sin(theta), z);
	}

	public static void translate(Entity3D e, Vector3D t) {
		ArrayList<Point3D> points = e.getPoints();
		for (Point3D p : points) {
			p.setCoordinates(p.x + t.x, p.y + t.y, p.z + t.z);
		}
		ArrayList<Face> faces = e.getFaces();
		for (Face f : faces) {
			f.computeAverageZ();
		}
	}

	/**
	 * Scales the given entity by the values of v.
	 * 
	 * @param e
	 * @param v
	 */
	public static void scale(Entity3D e, Vector3D v) {
		ArrayList<Point3D> points = e.getPoints();
		for (Point3D p : points) {
			p.setCoordinates(v.x * p.x, v.y * p.y, v.z * p.z);
		}
		ArrayList<Face> faces = e.getFaces();
		for (Face f : faces) {
			f.computeAverageZ();
		}
	}
}
