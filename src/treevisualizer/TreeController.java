package treevisualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class that holds onto an instance of Tree and knows how to draw
 * it.
 * 
 * @author Elliot Hatch and Samuel Davidson
 * @version March 19, 2014
 * @param <T>
 *            - The type of data stored in the Tree to be displayed
 */
public class TreeController<T> {

	private Tree<T> m_tree;

	public static int kPrimaryNodeRadius = 50;
	private static int kPrimaryNodeDistance = 200;
	private static int kMinimumRadiusText = 10;
	private static int kMinimumRadiusDraw = 2;

	private ArrayList<Circle> m_drawnCircles;
	private double m_zoom;

	/**
	 * Creates an instance of TreeController
	 * 
	 * @param tree
	 *            - The Tree data structure to be drawn
	 */
	public TreeController(Tree<T> tree) {
		m_tree = tree;
		m_zoom = 1.0;
		m_drawnCircles = new ArrayList<Circle>();
	}

	/**
	 * Sets the Tree that will be drawn.
	 * 
	 * @param tree
	 *            - The new tree
	 */
	public void setTree(Tree<T> tree) {
		m_tree = tree;
	}

	/**
	 * Takes a point and returns the Circle the point is contained in. The list
	 * of circles it chooses from is refreshed every draw() call and coordinates
	 * are relative to the Graphic passed to draw(). If there is no circle,
	 * returns null, if two circles are overlapping, it returns the circle first
	 * drawn (beneath).
	 * 
	 * @param x
	 *            - X coordinate
	 * @param y
	 *            - Y Coordinate
	 * @return - A circle containing the point, or null if the point is not in a
	 *         circle.
	 */
	public Circle getCircleAtPoint(int x, int y) {
		for (Circle c : m_drawnCircles) {
			if (isPointInCircle(x, y, c)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Returns true if the (x,y) coordinate is inside the circle
	 * 
	 * @param x
	 *            - X coordinate of point
	 * @param y
	 *            - Y coordinate of point
	 * @param c
	 *            - The circle tested
	 * @return - True if (x,y) is inside the circle, otherwise false
	 */
	private boolean isPointInCircle(int x, int y, Circle c) {
		double deltaX = x - c.x;
		double deltaY = y - c.y;
		double distanceFromCenter = Math.pow(
				Math.pow(deltaX, 2) + Math.pow(deltaY, 2), 0.5);
		return (distanceFromCenter < c.radius);
	}

	/**
	 * Draw the entire tree.
	 * 
	 * @param g
	 *            - The graphics context in which to paint.
	 * @param x
	 *            - X coordinate of the center of the root node.
	 * @param y
	 *            - Y coordinate of the center of the root node.
	 */
	public void draw(Graphics g, long x, long y) {
		Tree.Node<T> rootNode = m_tree.getRootNode();
		m_drawnCircles.clear();
		g.setFont(new Font("Arial", Font.PLAIN, 32));
		drawNode(g, x, y, rootNode, 0, 1.0, 0.0, true);
	}

	/**
	 * Draws a node and recursively draws its children.
	 * 
	 * @param g
	 *            - The graphics context in which to paint.
	 * @param x
	 *            - X coordinate of the center of the node.
	 * @param y
	 *            - Y coordinate of the center of the node.
	 * @param node
	 *            - The Node to draw.
	 * @param depth
	 *            - The depth of the node in the tree. Root is depth 0.
	 * @param nodeSizePercent
	 *            - Percent size of default node size to draw this node.
	 * @param angleRadians
	 *            - Angle from parent.
	 * @param isRoot
	 *            - Is this the root node? (Must be answered true/false, not
	 *            yes/no)
	 */
	private void drawNode(Graphics g, long x, long y, Tree.Node<T> node,
			int depth, double nodeSizePercent, double angleRadians,
			boolean isRoot) {
		// safe cast - every graphics object in swift is a graphics2D object
		Graphics2D g2 = (Graphics2D) g;

		long radius = (long) ((double) kPrimaryNodeRadius * nodeSizePercent * m_zoom);

		boolean doDrawOval = true;
		// don't draw shapes off the edge of the screen
		if (x + radius < 0 || x - radius > 800 || y + radius < 0
				|| y - radius > 600)
			doDrawOval = false;

		float hue = (float) (depth % 7) / 7.0f;
		Color fillColor = new Color(Color.HSBtoRGB(hue, 0.5f, 1.0f));
		Color outlineColor = new Color(Color.HSBtoRGB(hue, 1.0f, 0.8f));

		if (doDrawOval) {
			g2.setStroke(new BasicStroke(radius / 20.f));
			g.setColor(fillColor);
			g.fillOval((int) (x - radius), (int) (y - radius),
					(int) radius * 2, (int) radius * 2);
			g.setColor(outlineColor);
			g.drawOval((int) (x - radius), (int) (y - radius),
					(int) radius * 2, (int) radius * 2);

			m_drawnCircles.add(new Circle((int) x, (int) y, (int) radius));
		}

		// don't draw the children if radius is smaller than the threshold
		if (radius < kMinimumRadiusDraw) {
			return;
		}

		// only draw text if radius is larger than the threshold
		if (radius >= kMinimumRadiusText && doDrawOval) {
			String str = node.getData().toString();

			// determine font size to use
			float fontSize = 20.0f;
			Font oldFont = g.getFont().deriveFont(fontSize);
			int oldTextWidth = g.getFontMetrics(oldFont).stringWidth(str);
			fontSize = ((float) radius / oldTextWidth) * fontSize;
			Font newFont = g.getFont().deriveFont(fontSize);
			g.setFont(newFont);
			int textWidth = g.getFontMetrics(newFont).stringWidth(str);
			int textHeight = g.getFontMetrics(newFont).getAscent();
			g.setColor(Color.BLACK);
			// Draw the node text
			g.drawString(str, (int) x - textWidth / 2, (int) y + textHeight / 2);
		}

		// Children Drawing:

		List<Tree.Node<T>> children = node.getChildren();

		if (children.size() == 0)
			return;

		// non-root nodes calculate angles as if the parent edge is a node
		int isRootInt = 1;
		if (isRoot)
			isRootInt = 0;
		int numChildren = children.size() + isRootInt;

		double childrenAngle = (2.0 * Math.PI) / ((double) numChildren);

		// if the angle is greater than 180 degrees, cap it out at 180.
		// This forces the correct size of children when the root only has one
		// child
		if (childrenAngle > Math.PI)
			childrenAngle = Math.PI;

		// calculate size of children nodes
		double childrenSideLength = 2.0 * kPrimaryNodeDistance
				* Math.sin(childrenAngle / 2.0);
		double newDistance = childrenSideLength * 0.32;
		double childNodeSizePercent = (newDistance / kPrimaryNodeDistance)
				* nodeSizePercent;

		long childRadius = (long) ((double) kPrimaryNodeRadius
				* childNodeSizePercent * m_zoom);
		long edgeLength = (long) (((double) kPrimaryNodeDistance
				* nodeSizePercent * m_zoom) - ((double) radius + (double) childRadius));
		// draw the edges and children nodes
		for (int i = 0; i < children.size(); i++) {

			double currentAngle = childrenAngle * (i + 1) + angleRadians;
			long lineX1 = (long) ((double) radius * Math.cos(currentAngle)) + x;
			long lineY1 = (long) ((double) radius * Math.sin(currentAngle)) + y;

			long lineX2 = (long) ((double) (radius + edgeLength) * Math
					.cos(currentAngle)) + x;
			long lineY2 = (long) ((double) (radius + edgeLength) * Math
					.sin(currentAngle)) + y;

			// only draw edges that appear on the screen
			boolean doDrawLine = true;
			if (((lineX1 < 0 || lineX1 > 800) && (lineY1 < 0 || lineY1 > 600)
					&& (lineX2 < 0 || lineX2 > 800) && (lineY2 < 0 || lineY2 > 600))) {
				boolean intersectsWithScreen = (doLinesIntersect(lineX1,
						lineY1, lineX2, lineY2, 0, 0, 800, 0)
						|| doLinesIntersect(lineX1, lineY1, lineX2, lineY2,
								800, 0, 800, 600)
						|| doLinesIntersect(lineX1, lineY1, lineX2, lineY2, 0,
								0, 0, 600) || doLinesIntersect(lineX1, lineY1,
						lineX2, lineY2, 0, 600, 800, 600));
				if (!intersectsWithScreen) {
					doDrawLine = false;
				}
			}
			if (doDrawLine) {
				g2.setStroke(new BasicStroke(radius / 20.f));
				g.setColor(outlineColor);
				g.drawLine((int) lineX1, (int) lineY1, (int) lineX2,
						(int) lineY2);
			}
			long nextX = (long) (((double) kPrimaryNodeDistance * nodeSizePercent)
					* Math.cos(currentAngle) * m_zoom)
					+ x;
			long nextY = (long) (((double) kPrimaryNodeDistance * nodeSizePercent)
					* Math.sin(currentAngle) * m_zoom)
					+ y;

			// RECURSION
			drawNode(g, nextX, nextY, children.get(i), depth + 1,
					childNodeSizePercent, currentAngle + Math.PI, false);
		}
	}

	/**
	 * Returns true if the line segments (x1,y1,x2,y2) and (a1,b1,a2,b2)
	 * intersect
	 * 
	 * @param x1
	 *            - X1 of first line
	 * @param y1
	 *            - Y1 of first line
	 * @param x2
	 *            - X2 of first line
	 * @param y2
	 *            - Y2 of first line
	 * @param a1
	 *            - X1 of second line
	 * @param b1
	 *            - Y1 of second line
	 * @param a2
	 *            - X2 of second line
	 * @param b2
	 *            - Y2 of second line
	 * @return - True if the lines intersect, false if they don't or are
	 *         parallel
	 */
	private static boolean doLinesIntersect(long x1, long y1, long x2, long y2,
			long a1, long b1, long a2, long b2) {
		long line1X = x2 - x1;
		long line1Y = y2 - y1;
		long line2X = a2 - a1;
		long line2Y = b2 - b1;
		double line1Dotline2Perp = line1X * line2Y - line1Y * line2X;

		// if b dot d == 0, it means the lines are parallel so they have
		// infinite intersection points
		if (line1Dotline2Perp == 0)
			return false;

		long deltaX1 = a1 - x1;
		long deltaY1 = b1 - y1;
		double t = (deltaX1 * line2Y - deltaY1 * line2X) / line1Dotline2Perp;
		if (t < 0 || t > 1)
			return false;

		double u = (deltaX1 * line1Y - deltaY1 * line1X) / line1Dotline2Perp;
		if (u < 0 || u > 1)
			return false;

		return true;
	}

	/**
	 * Get current zoom factor.
	 * 
	 * @return - Current zoom factor in range (0.0,infinity).
	 */
	public double getZoom() {
		return m_zoom;
	}

	/**
	 * Set the zoom factor.
	 * 
	 * @param zoom
	 *            - Desired zoom amount, values less than or equal to zero are
	 *            ignored
	 */
	public void setZoom(double zoom) {
		if (zoom > 0)
			m_zoom = zoom;
	}

	/**
	 * Class Representing a circle with center (X,Y) and radius.
	 * 
	 * @author Elliot Hatch and Samuel Davidson
	 */
	public class Circle {
		public int x;
		public int y;
		public int radius;

		/**
		 * Creates a circle with initial values
		 * 
		 * @param x
		 *            - X coordinate of center
		 * @param y
		 *            - Y coordinate of center
		 * @param radius
		 *            - Radius of circle
		 */
		Circle(int x, int y, int radius) {
			this.x = x;
			this.y = y;
			this.radius = radius;
		}
	}
}
