package com.pascucci.render.view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.pascucci.render.engine.Entity3D;
import com.pascucci.render.engine.Face;
import com.pascucci.render.engine.Scene;
import com.pascucci.render.engine.SceneView;
import com.pascucci.render.engine.Transform3D;
import com.pascucci.render.engine.Vector3D;
import com.pascucci.render.utils.STLLoader;

/**
 * Provides the main window and UI for the renderer.
 * 
 * @author Nick Pascucci <nick@kestrelrobotics.com>
 */

@SuppressWarnings("serial")
public class RenderInterface extends JFrame implements ActionListener,
		TreeSelectionListener, ChangeListener {

	// Menu LAF
	private Color menuBarColor = Color.DARK_GRAY;
	private Color menuItemColor = Color.LIGHT_GRAY;

	// Menu Items
	// File
	private JMenuItem stlImport;
	// private JMenuItem stlExport;
	private JMenuItem quit;
	private JMenuItem clear;
	private JMenuItem delete;

	// Render
	private JMenuItem wireframe;
	private JMenuItem solid;
	private JMenuItem orthogonal;
	private JMenuItem perspective;

	// Scene
	private JMenuItem lightSource;

	// Entity
	private JMenuItem translate;
	private JMenuItem rotate;
	private JMenuItem scale;

	// Scene Tree
	private JScrollPane scrollPane;
	private JTree tree;

	// Render View
	SceneView view;

	// STL Import
	JFileChooser fc;

	public RenderInterface() {
		super();
		this.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/images/frameicon.png")));

		fc = new JFileChooser();

		// Menu
		JMenuBar topMenu = new JMenuBar();
		createMenus(topMenu);
		topMenu.setBackground(menuBarColor);
		this.setJMenuBar(topMenu);

		// Render Area
		view = new SceneView();
		view.setBackground(Color.LIGHT_GRAY);
		constraints.weightx = .9;
		constraints.weighty = 1;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		add(view, constraints);

		// Scene Tree
		createTree();

	}

	private void createMenus(JMenuBar top) {
		/*
		 * Create File Menu
		 */

		JMenu fileMenu = new JMenu("File");
		fileMenu.setForeground(menuItemColor);
		delete = new JMenuItem("Delete Entity");
		delete.addActionListener(this);
		fileMenu.add(delete);
		stlImport = new JMenuItem("Import STL");
		stlImport.addActionListener(this);
		fileMenu.add(stlImport);
		clear = new JMenuItem("Erase All");
		clear.addActionListener(this);
		fileMenu.add(clear);
		quit = new JMenuItem("Quit");
		quit.addActionListener(this);
		fileMenu.add(quit);

		top.add(fileMenu);

		/*
		 * Create render menu
		 */

		JMenu renderMenu = new JMenu("Render");
		renderMenu.setForeground(menuItemColor);
		JMenu viewStyleMenu = new JMenu("View Style");
		renderMenu.add(viewStyleMenu);
		wireframe = new JMenuItem("Wireframe");
		solid = new JMenuItem("Solid");
		orthogonal = new JMenuItem("Orthogonal");
		perspective = new JMenuItem("Perspective");
		viewStyleMenu.add(wireframe);
		viewStyleMenu.add(solid);
		viewStyleMenu.add(orthogonal);
		viewStyleMenu.add(perspective);
		wireframe.addActionListener(this);
		solid.addActionListener(this);
		orthogonal.addActionListener(this);
		perspective.addActionListener(this);
		top.add(renderMenu);

		/*
		 * Create scene menu
		 */

		JMenu sceneMenu = new JMenu("Scene");
		sceneMenu.setForeground(menuItemColor);
		lightSource = new JMenuItem("Light");
		sceneMenu.add(lightSource);
		lightSource.addActionListener(this);
		top.add(sceneMenu);

		/*
		 * Create entity menu
		 */

		JMenu entityMenu = new JMenu("Entity");
		entityMenu.setForeground(menuItemColor);
		JMenu transformMenu = new JMenu("Transform");
		entityMenu.add(transformMenu);
		translate = new JMenuItem("Translate");
		transformMenu.add(translate);
		translate.addActionListener(this);
		rotate = new JMenuItem("Rotate");
		transformMenu.add(rotate);
		rotate.addActionListener(this);
		scale = new JMenuItem("Scale");
		transformMenu.add(scale);
		scale.addActionListener(this);

		top.add(entityMenu);
	}

	/**
	 * Initializes the scene tree.
	 */
	private void createTree() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Scene");
		DefaultMutableTreeNode entity;
		Scene scene = view.getScene();
		ArrayList<Entity3D> entities = scene.getEntities();
		for (Entity3D e : entities) {
			entity = new DefaultMutableTreeNode(e);
			top.add(entity);
		}

		tree = new JTree(top);
		tree.addTreeSelectionListener(this);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		if (scrollPane != null) {
			remove(scrollPane);
		}
		scrollPane = new JScrollPane(tree);
		scrollPane.setSize(getHeight(), 400);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = .1;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		add(scrollPane, constraints);
	}

	public void addEntityToTree(Entity3D e) {
		// We can't just add nodes by adding children, we have to go through the
		// model or it won't display correctly.
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(e);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		model.insertNodeInto(node, root, root.getChildCount());
		tree.scrollPathToVisible(new TreePath(node.getPath()));
	}

	public void removeNodeFromTree(DefaultMutableTreeNode n) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.removeNodeFromParent(n);
	}

	/**
	 * Listens for the menu items.
	 * 
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source.equals(quit)) {
			System.exit(0);
		} else if (source.equals(solid)) {
			view.getRenderer().setWireframe(false);
			view.repaint();
		} else if (source.equals(wireframe)) {
			view.getRenderer().setWireframe(true);
			view.repaint();
		} else if (source.equals(orthogonal)) {
			view.getRenderer().setOrthogonal(true);
			view.repaint();
		} else if (source.equals(perspective)) {
			view.getRenderer().setOrthogonal(false);
			view.repaint();
		} else if (source.equals(stlImport)) {
			int userChoice = fc.showOpenDialog(this);
			if (userChoice == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				STLLoader loader = new STLLoader(this, file, view.getScene());
				loader.addChangeListener(this);
				Thread loadThread = new Thread(loader);
				loadThread.start();
			}
		} else if (source.equals(lightSource)) {
			moveLightSource();
		} else if (source.equals(translate)) {
			translateEntity();
		} else if (source.equals(rotate)) {
			rotateEntity();
		} else if (source.equals(scale)) {
			scaleEntity();
		} else if (source.equals(delete)) {
			deleteEntity();
		} else if (source.equals(clear)) {
			deleteAll();
		}
	}

	/*
	 * Moves the light source. This uses the standard getVectorFromUser method
	 * because it's convenient!
	 */
	private void moveLightSource() {
		Vector3D v = getVectorFromUser("Set Light Location",
				"New location (x, y, z):");
		if (v != null) {
			view.getScene().getLight().setCoordinates(v.x, v.y, v.z);
			view.repaint();
		}
	}

	private void translateEntity() {
		Vector3D v = getVectorFromUser("Translate Entity",
				"Translation vector (x, y, z):");
		if (v != null) {
			Entity3D entity = view.getSelectedEntity();
			Transform3D.translate(entity, v);
			view.repaint();
		}
	}

	private void rotateEntity() {
		Vector3D v = getVectorFromUser("Rotate Entity",
				"Rotation axis vector (x, y, z):");
		if (v != null) {
			try {
				float rotation = getFloatFromUser("Rotate Entity",
						"Rotation amount (Radians):");
				Entity3D entity = view.getSelectedEntity();
				Transform3D.rotate(v, entity, rotation);
				view.repaint();
			} catch (NumberFormatException nfe) {
			}
		}
	}

	private void scaleEntity() {
		Vector3D v = getVectorFromUser("Scale Entity",
				"Scaling vector (x, y, z):");
		if (v != null) {
			Entity3D entity = view.getSelectedEntity();
			Transform3D.scale(entity, v);
			view.repaint();
		}
	}

	/*
	 * Removes an entity entirely from the scene.
	 */
	private void deleteEntity() {
		int input = JOptionPane.showConfirmDialog(this, "Are you sure?",
				"Delete Entity", JOptionPane.OK_CANCEL_OPTION);
		if (input == JOptionPane.OK_OPTION) {
			Entity3D entity = view.getSelectedEntity();
			view.getScene().removeEntity(entity);
			removeNodeFromTree((DefaultMutableTreeNode) tree
					.getLastSelectedPathComponent());
			validate();
			view.repaint();
		} else {

		}
	}

	/*
	 * Removes every entity from the scene.
	 */
	private void deleteAll() {
		int input = JOptionPane.showConfirmDialog(this, "Are you sure?",
				"Delete Entity", JOptionPane.OK_CANCEL_OPTION);
		if (input == JOptionPane.OK_OPTION) {
			view.getScene().removeAll();
			createTree();
			tree.validate();
			validate();
			view.repaint();
		} else {

		}
	}

	/*
	 * Listens for changes in the selected item in the tree.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node == null)
			return;
		Object nodeObject = node.getUserObject();
		if (nodeObject instanceof Face) {
			Face face = (Face) nodeObject;
			face.setBorderColor(Color.YELLOW);
		} else if (nodeObject instanceof Entity3D) {
			Entity3D entity = (Entity3D) nodeObject;
			System.out.println("Selecting entity " + entity);
			view.setSelectedEntity(entity);
		} else {
			view.setSelectedEntity(null);
		}
		view.repaint();
	}

	/**
	 * Gets a vector from the user by prompting with a dialog box.
	 * 
	 * @param windowTitle
	 * @param windowMessage
	 * @return Null if the user gave malformed input.
	 */
	private Vector3D getVectorFromUser(String windowTitle, String windowMessage) {
		Vector3D vector = null;
		String location = JOptionPane.showInputDialog(this, windowMessage,
				windowTitle, JOptionPane.QUESTION_MESSAGE);
		if (location != null) {
			String[] components = location.split(",");
			if (components.length == 3) {
				try {
					float xComponent = Float.parseFloat(components[0]);
					float yComponent = Float.parseFloat(components[1]);
					float zComponent = Float.parseFloat(components[2]);
					vector = new Vector3D(xComponent, yComponent, zComponent);
				} catch (NumberFormatException f) {
					JOptionPane.showMessageDialog(this,
							"Incorrect number format.", "Malformed Input",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this,
						"Incorrect number of entries.", "Malformed Input",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return vector;
	}

	/**
	 * Gets a float from the user using a dialog box.
	 * 
	 * @param windowTitle
	 * @param windowMessage
	 * @return
	 * @throws NumberFormatException
	 *             If the float couldn't be parsed.s
	 */
	private float getFloatFromUser(String windowTitle, String windowMessage)
			throws NumberFormatException {
		float number = 0;
		String input = JOptionPane.showInputDialog(this, windowMessage,
				windowTitle, JOptionPane.INFORMATION_MESSAGE);
		input.trim();
		try {
			number = Float.parseFloat(input);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Incorrect number format.",
					"Malformed Input", JOptionPane.ERROR_MESSAGE);
			throw new NumberFormatException(nfe.getMessage());
		}
		return number;
	}

	/**
	 * Allows loading of STL files to be put into another thread.
	 * 
	 * @param e
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		STLLoader loader = (STLLoader) e.getSource();
		addEntityToTree(loader.getEntity());
		view.repaint();
	}
}
