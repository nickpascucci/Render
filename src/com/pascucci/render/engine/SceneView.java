package com.pascucci.render.engine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.pascucci.render.utils.Utils3D;

/**
 * Performs the render and calls for various transformations on the world data.
 * 
 * @author Nick Pascucci <nick@kestrelrobotics.com>
 */
@SuppressWarnings("serial")
public class SceneView extends JPanel implements MouseListener,
		MouseWheelListener, MouseMotionListener {

	private Camera camera;

	// View variables
	private double scalefactor = 1;
	private boolean wireframe = false;
	private boolean orthogonal = false;
	private boolean debug = false;

	// Scene
	private Scene scene;
	private Entity3D selectedEntity;
	private Face selectedFace;

	// View rotation parameters
	private double RADIANS_PER_PIXEL = Math.PI / 360;

	/**
	 * Creates a new SceneView object with a cube as the only object.
	 */
	public SceneView() {
		camera = new Camera(0, 0, 800);
		// Testing

		scene = new Scene();
		int cubesPerSide = 2;
		int spacing = 100;
		
		for(int x = -cubesPerSide * spacing; x < cubesPerSide * spacing; x+=spacing){
			for(int y = -cubesPerSide * spacing; y < cubesPerSide * spacing; y+=spacing){
				for(int z = -cubesPerSide * spacing; z < cubesPerSide * spacing; z+=spacing){
					Entity3D cube = Utils3D.cube(50, x, y, z);
					cube.setColor(Color.WHITE);
					scene.addEntity(cube);
				}
			}
		}
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		renderScene(g2);
	}

	/**
	 * Carries out the render using the given graphics object.
	 * 
	 * Nicely, this means that the scene can be rendered to any
	 * output by passing this method an arbitrary Graphics2D object.
	 * 
	 * @param g
	 */
	private void renderScene(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// Put the origin in the middle, flip axes
		g2.translate(getWidth() / 2, getHeight() / 2);
		g2.scale(scalefactor, -scalefactor);

		if (scene != null) {
			ZBuffer buffer = scene.getBuffer();
			buffer.sort();
			ArrayList<Face> faces = buffer.getSortedFaces();
			for (Face f : faces) {
				g2.setStroke(new BasicStroke(.2f, BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_BEVEL));
				if (!wireframe) {
					Vector3D normal = Utils3D.normal(f);
					if (orthogonal && normal.z <= 0)
						continue;
					else {
						Point3D p = f.getPoints()[0];
						if (Utils3D.dotProduct(normal,
								new Vector3D(p, camera.getLocation())) <= 0)
							continue;
					}
				}
				Point3D[] points = f.getPoints();
				Path2D faceShape = new Path2D.Double();

				for (Point3D p : points) {
					Point2D p2 = convertToScreenCoordinates(p);
					if (p == points[0]) {
						faceShape.moveTo(p2.getX(), p2.getY());
					} else {
						faceShape.lineTo(p2.getX(), p2.getY());
					}
				}
				faceShape.closePath();
				if (!wireframe) {
					g2.setPaint(getShading(f));
					g2.fill(faceShape);
				} else {
					g2.setPaint(f.getBorderColor());
				}
				g2.draw(faceShape);
			}
			Point2D light = convertToScreenCoordinates(scene.getLight());
			Ellipse2D.Double lightIcon = new Ellipse2D.Double(light.getX(), light.getY(), 3, 3);
			g2.setColor(Color.YELLOW);
			g2.fill(lightIcon);
		}
		// Bring it back to normal, and flip the axes
		g2.scale(1.0 / scalefactor, -1.0 / scalefactor);

		// Paint render information to render window
		g2.setPaint(Color.BLACK);
		g2.drawString("Scale Factor: " + scalefactor, -(getWidth() / 2) + 20,
				-(getHeight() / 2) + 20);
		g2.drawString("Camera Location: " + camera.getLocation(),
				-(getWidth() / 2) + 20, -(getHeight() / 2) + 40);
	}

	/**
	 * Converts a world-coordinate to a perspective screen coordinate for
	 * rendering.
	 * 
	 * @param p
	 *            The point to convert.
	 * @return The new screen coordinate.
	 */
	private Point2D convertToScreenCoordinates(Point3D p) {
		double xScreen = 0;
		double yScreen = 0;
		double conversionRatio = camera.getLocation().z
				/ (camera.getLocation().z - p.z);
		if (orthogonal)
			conversionRatio = 1;
		xScreen = conversionRatio * p.x;
		yScreen = conversionRatio * p.y;

		return new Point2D.Double(xScreen, yScreen);
	}

	/**
	 * Calculates the color of a face with diffuse shading.
	 * 
	 * @param f
	 *            The face.
	 * @return The color of the face.s
	 */
	private Color getShading(Face f) {
		Color faceColor = f.getColor();
		int r = faceColor.getRed();
		int g = faceColor.getGreen();
		int b = faceColor.getBlue();
		Vector3D normal = Utils3D.normal(f);
		Vector3D light = new Vector3D(f.getPoints()[0], scene.getLight()).toUnitVector();
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
		return new Color(r, g, b, 255);
	}

	public void setScene(Scene s) {
		scene = s;
	}

	public Scene getScene() {
		return scene;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setWireframe(boolean on) {
		this.wireframe = on;
	}

	public void setOrthogonal(boolean on) {
		this.orthogonal = on;
	}

	public void setSelectedEntity(Entity3D e) {
		if (selectedFace != null) {
			selectedFace.setBorderColor(selectedFace.getColor());
			selectedFace = null;
		}
		ArrayList<Face> faces;
		if (selectedEntity != null) {
			faces = selectedEntity.getFaces();
			for (Face f : faces) {
				f.setBorderColor(f.getColor());
			}
		}
		selectedEntity = e;
		if (e != null) {
			faces = e.getFaces();
			for (Face f : faces) {
				f.setBorderColor(Color.YELLOW);
			}
		}
	}

	public Entity3D getSelectedEntity() {
		return selectedEntity;
	}

	/**
	 * Rotates the view around the origin by the given angles.
	 * 
	 * @param angleUp
	 * @param angleOver
	 */
	public void rotateView(double angleUp, double angleOver) {
		Transform3D.rotateSceneX(scene, angleUp);
		Transform3D.rotateSceneY(scene, angleOver);
	}

	private int oldX;
	private int oldY;
	private boolean rotateView = false;

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if (rotateView) {
			int x = e.getX() - oldX; // If the view is going in the wrong
										// direction, invert these.
			int y = e.getY() - oldY;
			oldX = e.getX();
			oldY = e.getY();
			if (debug)
				System.out.println("Rotating view " + y * RADIANS_PER_PIXEL
						+ " radians up, " + x * RADIANS_PER_PIXEL
						+ " radians over.");
			rotateView(y * RADIANS_PER_PIXEL, x * RADIANS_PER_PIXEL);
			repaint();
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			oldX = e.getX();
			oldY = e.getY();
			rotateView = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		rotateView = false;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Allow zooming.
	 * 
	 * @param e
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		int clicks = e.getWheelRotation();
		if (scalefactor >= 1) {
			scalefactor -= clicks;
		}
		if (scalefactor < 1)
			scalefactor = 1.0;
		repaint();
	}
}
