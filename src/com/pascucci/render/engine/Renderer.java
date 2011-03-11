package com.pascucci.render.engine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.pascucci.render.utils.Utils3D;

/**
 * Provides the actual renderering engine for a scene.
 * 
 * @author nick
 * 
 */
public class Renderer {

	// View variables
	private double scalefactor;
	private boolean wireframe = false;
	private boolean orthogonal = false;

	/**
	 * Turns wireframe rendering on or off.
	 * 
	 * @param on
	 */
	public void setWireframe(boolean on) {
		this.wireframe = on;
	}

	/**
	 * Turns on orthogonal rendering.
	 * 
	 * @param on
	 */
	public void setOrthogonal(boolean on) {
		this.orthogonal = on;
	}

	/**
	 * Sets the scaling factor for the renderer.
	 * 
	 * @param factor
	 */
	public void setScale(double factor) {
		this.scalefactor = factor;
	}

	public double getScale() {
		return scalefactor;
	}

	public Renderer() {

		scalefactor = 1;
	}

	/**
	 * Carries out the render using the given graphics object.
	 * 
	 * Nicely, this means that the scene can be rendered to any output by
	 * passing this method an arbitrary Graphics2D object.
	 * 
	 * @param g
	 */
	public void renderScene(Graphics2D g2, Scene scene, int width, int height) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, width, height);
		// Put the origin in the middle, flip axes
		g2.translate(width / 2, height / 2);
		g2.scale(scalefactor, -scalefactor);
		
		

		Camera camera = scene.getCamera();

		if (scene != null) {
			// We'll use our ZBuffer to get the faces in ascending Z order,
			// Then use the painter's algorithm to draw them.
			ZBuffer buffer = scene.getBuffer();
			buffer.sort();
			ArrayList<Face> faces = buffer.getSortedFaces();

			for (Face f : faces) {
				g2.setStroke(new BasicStroke(.5f, BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_BEVEL));
				Point3D[] points = f.getPoints();
				if (!wireframe) {
					Vector3D normal = Utils3D.normal(f);
					// If we're in orthogonal mode, we don't paint faces
					// pointing away from us.
					if (orthogonal && normal.z <= 0)
						continue;
					else {
						Point3D p = points[0];
						/*
						 * If the face isn't visible the cosine of the angle
						 * between the normal and a vector to our camera will be
						 * less than 0: cos(90) == 0 && cos(270) == 0. Any angle
						 * which 90 < x < 270 is pointing away from us.
						 */
						if (Utils3D.dotProduct(normal,
								new Vector3D(p, camera.getLocation())) <= 0)
							continue;
					}
				}
				Path2D faceShape = new Path2D.Double();

				for (Point3D p : points) {
					Point2D p2 = convertToScreenCoordinates(p, camera);
					if (p == points[0]) {
						faceShape.moveTo(p2.getX(), p2.getY());
					} else {
						faceShape.lineTo(p2.getX(), p2.getY());
					}
				}
				faceShape.closePath();
				if (!wireframe) {
					g2.setPaint(getShading(f, scene));
					g2.fill(faceShape);
				}
				g2.setPaint(getBorderShading(f, scene));
				g2.draw(faceShape);
			}
			Point2D light = convertToScreenCoordinates(scene.getLight(), camera);
			Ellipse2D.Double lightIcon = new Ellipse2D.Double(light.getX(),
					light.getY(), 3, 3);
			g2.setColor(Color.YELLOW);
			g2.fill(lightIcon);
		}
		// Bring it back to normal, and flip the axes
		g2.scale(1.0 / scalefactor, -1.0 / scalefactor);
	}

	/**
	 * Converts a world-coordinate to a perspective screen coordinate for
	 * rendering.
	 * 
	 * @param p
	 *            The point to convert.
	 * @return The new screen coordinate.
	 */
	private Point2D convertToScreenCoordinates(Point3D p, Camera camera) {
		double xScreen = 0;
		double yScreen = 0;
		// If we're in perspective, we want to have a perspective transform
		// here. Otherwise, we just ignore it and go straight from world x,y
		// coordinates.
		double conversionRatio = orthogonal ? 1 : camera.getLocation().z
				/ (camera.getLocation().z - p.z);
		xScreen = conversionRatio * p.x;
		yScreen = conversionRatio * p.y;

		return new Point2D.Double(xScreen, yScreen);
	}

	/**
	 * Calculates the color of a face with both ambient and diffuse shading.
	 * 
	 * @param f
	 *            The face.
	 * @return The color of the face.
	 */
	private Color getShading(Face f, Scene scene) {
		Color faceColor = f.getColor();
		int r = faceColor.getRed();
		int g = faceColor.getGreen();
		int b = faceColor.getBlue();
		int a = faceColor.getAlpha();
		Vector3D normal = Utils3D.normal(f);
		Vector3D light = new Vector3D(f.getPoints()[0], scene.getLight())
				.toUnitVector();
		// Diffuse lighting is calculated by getting the cosine
		// between the normal and the vector to the light source
		double diffuse = Utils3D.dotProduct(normal, light);
		if (diffuse < 0)
			diffuse = 0;
		r = (int) ((scene.ambient * r * f.ambientSensitivity) + (diffuse * r * f.diffuseSensitivity));
		g = (int) ((scene.ambient * g * f.ambientSensitivity) + (diffuse * g * f.diffuseSensitivity));
		b = (int) ((scene.ambient * b * f.ambientSensitivity) + (diffuse * b * f.diffuseSensitivity));
		// Let's catch values that are too large to use.
		r = r > 255 ? 255 : r;
		g = g > 255 ? 255 : g;
		b = b > 255 ? 255 : b;
		return new Color(r, g, b, a);
	}

	/**
	 * Calculates the border color of a face with diffuse shading.
	 * 
	 * @param f
	 *            The face.
	 * @return The color of the face.s
	 */
	private Color getBorderShading(Face f, Scene scene) {
		Color borderColor = f.getBorderColor();
		int r = borderColor.getRed();
		int g = borderColor.getGreen();
		int b = borderColor.getBlue();
		int a = borderColor.getAlpha();
		Vector3D normal = Utils3D.normal(f);
		Vector3D light = new Vector3D(f.getPoints()[0], scene.getLight())
				.toUnitVector();
		// Diffuse lighting is calculated by getting the cosine
		// between the normal and the vector to the light source
		double diffuse = Utils3D.dotProduct(normal, light);
		if (diffuse < 0)
			diffuse = 0;
		r = (int) ((scene.ambient * r * f.ambientSensitivity) + (diffuse * r * f.diffuseSensitivity));
		g = (int) ((scene.ambient * g * f.ambientSensitivity) + (diffuse * g * f.diffuseSensitivity));
		b = (int) ((scene.ambient * b * f.ambientSensitivity) + (diffuse * b * f.diffuseSensitivity));
		// Let's catch values that are too large to use.
		r = r > 255 ? 255 : r;
		g = g > 255 ? 255 : g;
		b = b > 255 ? 255 : b;
		return new Color(r, g, b, a);
	}
}
