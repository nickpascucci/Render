/*
 * All rights reserved by the author.
 * Unauthorized distribution is prohibited.
 */

package com.pascucci.render.utils;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.ProgressMonitor;

import com.pascucci.render.engine.Entity3D;
import com.pascucci.render.engine.Face;
import com.pascucci.render.engine.Point3D;
import com.pascucci.render.engine.Vector3D;

/**
 * Provides utilities for manipulating STL files and parsing them into
 * com.pascucci.render.Entity3D objects.
 * 
 * @author Nick Pascucci <nick@kestrelrobotics.com>
 */

public class STLUtils {

	public static final int STL_HEADER_BYTES = 80;
	public static final int LITTLE_ENDIAN = 0;
	public static final int BIG_ENDIAN = 1;
	private static boolean debug = false;

	public static final int order = LITTLE_ENDIAN;

	/**
	 * Reads in an entity from a stored STL file. Adapted from the code at
	 * http://www.resplendent.com/StlFile.java
	 * 
	 * @param input
	 *            The STL file.
	 * @return
	 */
	public static Entity3D entityFromSTL(File input, Component parent) {
		FileInputStream file;
		ByteBuffer buffer;
		byte[] fileInfo = new byte[80]; // Stores the header
		byte[] faces = new byte[4]; // Stores the number of faces
		byte[] tmp;

		int NUM_FACES;

		try {
			file = new FileInputStream(input);
			if (file.read(fileInfo) != 80) // The header is 80 bytes
			{
				System.out.println("File malformed, read header failed.");
			} else {
				file.read(faces);
				buffer = ByteBuffer.wrap(faces);
				buffer.order(ByteOrder.nativeOrder());
				NUM_FACES = buffer.getInt();
				ProgressMonitor pm = new ProgressMonitor(parent,
						"Importing file " + input.getName(), "", 0,
						NUM_FACES - 1);
				pm.setMillisToPopup(0);

				tmp = new byte[50 * NUM_FACES]; // Each face contains 50 bytes
												// of data
				file.read(tmp);
				buffer = ByteBuffer.wrap(tmp);
				buffer.order(ByteOrder.nativeOrder());

				Entity3D entity = new Entity3D();
				for (int i = 0; i < NUM_FACES; i++) {
					readFace(buffer, entity);
					if (i < NUM_FACES - 1) { // Each face has 2 bytes of data
												// after it that we don't need.
						buffer.get();
						buffer.get();
					}
					String message = "Read " + i + " of " + (NUM_FACES - 1)
							+ " faces.";
					pm.setProgress(i);
					pm.setNote(message);
				}
				entity.setName(input.getName());
				return entity;
			}
		} catch (FileNotFoundException fnf) {
			System.out.println("File not found exception: " + fnf.getMessage());
		} catch (IOException ioe) {
			System.out.println("IOException: " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * Helper method for reading in a face from the buffer.
	 * 
	 * @param bb
	 * @param e
	 */
	private static void readFace(ByteBuffer bb, Entity3D e) {
		Vector3D fileNormal = new Vector3D(bb.getFloat(), bb.getFloat(),
				bb.getFloat());
		if (debug)
			System.out.println("Read normal: " + fileNormal);
		Point3D[] points = new Point3D[3];
		points[0] = new Point3D(bb.getFloat(), bb.getFloat(), bb.getFloat());
		if (debug)
			System.out.println("Read vertex " + points[0]);

		points[1] = new Point3D(bb.getFloat(), bb.getFloat(), bb.getFloat());
		if (debug)
			System.out.println("Read vertex " + points[1]);
		points[2] = new Point3D(bb.getFloat(), bb.getFloat(), bb.getFloat());
		if (debug)
			System.out.println("Read vertex " + points[2]);
		Face f = new Face(points);
		f.setColor(Color.DARK_GRAY);
		f.ambientSensitivity = 1;
		f.diffuseSensitivity = 1;
		Vector3D faceNormal = Utils3D.normal(f);
		if (debug)
			System.out.println("Calculated normal " + faceNormal);
		e.addPoints(points);
		e.addFace(f);
	}
}
