package com.pascucci.render.utils;

import java.awt.Color;

import com.pascucci.render.engine.Entity3D;
import com.pascucci.render.engine.Face;
import com.pascucci.render.engine.Point3D;
import com.pascucci.render.engine.Vector3D;

/**
 * Utility class for manipulating 3D objects.
 * 
 * @author Nick Pascucci <nick@kestrelrobotics.com>
 */

public class Utils3D {

	/**
	 * Creates a cube with the given size, centered at the given coordinates.
	 * 
	 * @param sideLength
	 *            The length of any given side of the cube.
	 * @return A new 3D entity which forms a cube.
	 */
	public static Entity3D cube(double sideLength, double x, double y, double z) {
		double length = sideLength / 2;
		Entity3D cube = new Entity3D();
		Point3D[] points = { new Point3D(length + x, length + y, length + z),
				new Point3D(-length + x, length + y, length + z),
				new Point3D(length + x, -length + y, length + z),
				new Point3D(length + x, length + y, -length + z),
				new Point3D(-length + x, -length + y, length + z),
				new Point3D(length + x, -length + y, -length + z),
				new Point3D(-length + x, length + y, -length + z),
				new Point3D(-length + x, -length + y, -length + z) };
		cube.addPoints(points);
		//Doing this manually is a pain, but largely unavoidable.
		//This ordering works, I'd recommend not changing it.
		Face[] faces = { new Face(points[1], points[6], points[7]),
				new Face(points[1], points[7], points[4]),
				new Face(points[0], points[2], points[5]),
				new Face(points[0], points[5], points[3]),
				new Face(points[0], points[1], points[4]),
				new Face(points[0], points[4], points[2]),
				new Face(points[3], points[5], points[7]),
				new Face(points[3], points[7], points[6]),
				new Face(points[3], points[6], points[0]),
				new Face(points[0], points[6], points[1]),
				new Face(points[7], points[5], points[2]),
				new Face(points[7], points[2], points[4]) };
		//Change these colors to change the side colors.
		Color[] paints = { Color.RED, Color.ORANGE, Color.GREEN, Color.WHITE,
				Color.YELLOW, Color.DARK_GRAY, Color.PINK, Color.BLUE,
				Color.CYAN, Color.MAGENTA, Color.LIGHT_GRAY };
		for (int i = 0; i < faces.length; i += 2) {
			faces[i].setColor(paints[i / 2 % paints.length]);
			faces[i + 1].setColor(paints[i / 2 % paints.length]);
			faces[i].ambientSensitivity = 1;
			faces[i].diffuseSensitivity = 1;
			faces[i + 1].ambientSensitivity = 1;
			faces[i + 1].diffuseSensitivity = 1;
			faces[i].setBorderColor(paints[i / 2 % paints.length]);
			faces[i + 1].setBorderColor(paints[i / 2 % paints.length]);
		}
		cube.addFaces(faces);
		return cube;
	}

	/**
	 * Creates a cube with the given size, centered at the origin.
	 * 
	 * @param sideLength
	 *            The length of any given side of the cube.
	 * @return A new 3D entity which forms a cube.
	 */
	public static Entity3D cube(double sideLength) {
		return cube(sideLength, 0, 0, 0);
	}
	
	/**
	 * Calculates the normal vector to a face.
	 * 
	 * @param f
	 *            The face, with points represented in counter-clockwise fashion
	 *            around the normal.
	 * @return A vector normal to the face.
	 */
	public static Vector3D normal(Face f) {
		Point3D[] points = f.getPoints();
		Vector3D a = new Vector3D(points[0], points[1]);
		Vector3D b = new Vector3D(points[0], points[2]);
		return crossProduct(a, b).toUnitVector();
	}

	/**
	 * Calculates the vector cross product of the given vectors.
	 * @param a The first vector in 3 dimensional space.
	 * @param b The second vector in 3 dimensional space.
	 * @return The vector which is perpendicular to both vectors.
	 */
	public static Vector3D crossProduct(Vector3D a, Vector3D b) {
		double x, y, z;
		double ia = a.x;
		double ja = a.y;
		double ka = a.z;
		double ib = b.x;
		double jb = b.y;
		double kb = b.z;
		x = (ja * kb) - (ka * jb);
		y = (ka * ib) - (ia * kb);
		z = (ia * jb) - (ja * ib);
		return new Vector3D(x, y, z);
	}

	/**
	 * Calculates the dot product of two vectors.
	 * @param a The first vector in 3 dimensional space.
	 * @param b The second vector in 3 dimensional space.
	 * @return The double version of the dot product.
	 */
	public static double dotProduct(Vector3D a, Vector3D b) {
		double cp = a.x * b.x + a.y * b.y + a.z * b.z;
		return cp;
	}

	/**
	 * Projects the vector a onto the vector b.
	 * @param a The first vector in 3 dimensional space.
	 * @param b The second vector in 3 dimensional space.
	 * @return
	 */
	public static Vector3D projection(Vector3D a, Vector3D b) {
		double scale = dotProduct(a, b) / (b.length() * b.length());
		return new Vector3D(b.x * scale, b.y * scale, b.z * scale);
	}
}
