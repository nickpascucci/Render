package com.pascucci.render.engine;

/**
 * Represents a plane in 3D space, as characterized by the equation ax + by + cz
 * + d = 0. This element is not yet renderable.
 * 
 * @author Nick Pascucci <nick@kestrelrobotics.com>
 */

// This was supposed to help with creating a BSP Tree, but never got used.
public class Plane3D {

	private double a;
	private double b;
	private double c;
	private double d;
	private Vector3D normal;

	public static final int COPLANAR = 0;
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;

	/**
	 * Creates a new plane which contains the two vectors.
	 * 
	 * @param a
	 *            A vector in 3D space.
	 * @param b
	 *            Another one.
	 */
	public Plane3D(Vector3D a, Vector3D b) {
		this.a = 1;
		this.b = 1;
		c = 1;
		d = 1;
	}

	/**
	 * Checks to see which side of the plane this point is on. If the point
	 * satisfies the inequality ax + by +cz + d > 0, it is considered to be on
	 * the <i>positive</i> side of the plane; if it is less than 0, it is on the
	 * negative; and if it equals 0 it is coplanar.
	 * 
	 * @param p
	 * @return
	 */
	public int evaluatePoint(Point3D p) {
		double e = a * p.x + b * p.y + c * p.z + d;
		if (e > 0) {
			return POSITIVE;
		} else if (e < 0) {
			return NEGATIVE;
		} else {
			return COPLANAR;
		}
	}

	/**
	 * Gets the normal to this plane. Normals are calculated dynamically.
	 * 
	 * @return
	 */
	public Vector3D getNormal() {
		if (normal == null) {
			normal = new Vector3D(a, b, c);
		}
		return normal;
	}
}
