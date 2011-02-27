package com.pascucci.render.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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

	// Scene
	private Scene scene;
	private Entity3D selectedEntity;
	private Face selectedFace;
	private Renderer renderer;
	private boolean debug;

	// View rotation parameters
	private double RADIANS_PER_PIXEL = Math.PI / 360;

	/**
	 * Creates a new SceneView object with a cube as the only object.
	 */
	public SceneView() {
		Camera camera = new Camera(0, 0, 800);
		// Testing

		renderer = new Renderer();
		scene = new Scene();
		scene.setCamera(camera);
		int cubesPerSide = 2;
		int spacing = 100;
		
		for(int x = -cubesPerSide * spacing; x < cubesPerSide * spacing; x+=spacing){
			for(int y = -cubesPerSide * spacing; y < cubesPerSide * spacing; y+=spacing){
				for(int z = -cubesPerSide * spacing; z < cubesPerSide * spacing; z+=spacing){
					Entity3D cube = Utils3D.cube(50, x, y, z);
					cube.setName("Cube " + x + ", " + y + ", " + z);
					cube.setColor(new Color(255, 255, 255, 180));
					cube.setBorderColor(new Color(0, 0, 0, 0));
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
		renderer.renderScene(g2, scene, getWidth(), getHeight());
	}

	public Renderer getRenderer(){
		return renderer;
	}

	public void setScene(Scene s) {
		scene = s;
	}

	public Scene getScene() {
		return scene;
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
		double scalefactor = renderer.getScale();
		if (scalefactor >= 1) {
			scalefactor -= clicks;
		}
		if (scalefactor < 1)
			scalefactor = 1.0;
		renderer.setScale(scalefactor);
		repaint();
	}
}
