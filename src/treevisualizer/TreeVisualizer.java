package treevisualizer;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * GUI based application that displays Tree structures.
 * 
 * @author Elliot Hatch and Samuel Davidson
 * @version March 19, 2014
 */
public class TreeVisualizer extends JPanel implements MouseMotionListener,
		MouseListener, ActionListener, ChangeListener, MouseWheelListener {
	//Thrown in to get rid of a serialVersionUID warning, does not do anything.
	private static final long serialVersionUID = 1L;
	
	private static double kZoomRate = 0.1;
	private static double kPanDuration = 1.0;

	private int lastMouseX, lastMouseY;
	private double m_targetCameraX, m_targetCameraY;
	private double m_beginCameraX, m_beginCameraY;
	private double m_cameraX, m_cameraY;
	private double m_beginZoom;
	private double m_targetZoom;

	private TreeController<String> m_treeController;

	private JFrame m_frame;
	private JButton m_quitButton;
	private JButton m_loadButton;
	private JButton m_resetButton;
	private JLabel m_zoomLabel;
	private JSlider m_zoomSlider;

	private Timer m_zoomSliderUpdateTimer;
	private boolean m_zoomSliderPressed;

	private double m_panInterpolateTime;

	private Timer m_panUpdateTimer;

	/**
	 * The application entry point.
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(String[] args) {
		// Set up the JFrame and throw a TreeVisualizer within it.
		JFrame frame = new JFrame("Tree Visualizer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		TreeVisualizer panel = new TreeVisualizer(frame);
		frame.add(panel, BorderLayout.CENTER);

		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Creates an Instance of TreeVisualizer JPanel
	 * 
	 * @param frame
	 *            - The frame containing this JPanel
	 */
	public TreeVisualizer(JFrame frame) {
		setMinimumSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));

		// Create stuff for the JMenuBar
		m_loadButton = new JButton("Load Tree");
		m_loadButton.addActionListener(this);

		m_quitButton = new JButton("Quit");
		m_quitButton.addActionListener(this);

		m_resetButton = new JButton("Reset");
		m_resetButton.addActionListener(this);

		m_zoomSlider = new JSlider();
		m_zoomSlider.addChangeListener(this);
		m_zoomSlider.addMouseListener(this);
		m_zoomSlider.setPaintTrack(false);
		m_zoomSlider.setToolTipText("Drag to zoom");
		m_zoomSlider.setPaintTicks(true);
		m_zoomSlider.setMajorTickSpacing(25);
		m_zoomSlider.setMinorTickSpacing(5);
		m_zoomSlider.setMaximumSize(new Dimension(200, 100));

		m_zoomLabel = new JLabel("1.00x");

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(m_loadButton);
		menuBar.add(m_quitButton);
		menuBar.add(new JLabel("Zoom:"));
		menuBar.add(m_zoomSlider);
		menuBar.add(m_zoomLabel);
		menuBar.add(m_resetButton);

		m_frame = frame;
		frame.add(menuBar, BorderLayout.NORTH);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);

		// Camera Stuff
		m_cameraX = 0;
		m_cameraY = 0;
		m_targetCameraX = 0;
		m_targetCameraY = 0;
		m_targetZoom = 1.0;

		m_zoomSliderUpdateTimer = new Timer(1000 / 30, this);

		m_panUpdateTimer = new Timer(1000 / 30, this);

		// Creates an example tree.
		displayExampleTree();

	}

	/**
	 * The paint method for drawing the panel.
	 * 
	 * @param g
	 *            a graphics object
	 */
	public void paint(Graphics g) {
		// safe cast - every graphics object in swift is a graphics2D object
		// Graphics2D g2 = (Graphics2D) g;

		int upperLeftX = g.getClipBounds().x;
		int upperLeftY = g.getClipBounds().y;
		int visibleWidth = g.getClipBounds().width;
		int visibleHeight = g.getClipBounds().height;

		// Clear the background.
		g.setColor(new Color(80, 80, 90, 255));
		g.fillRect(upperLeftX, upperLeftY, visibleWidth, visibleHeight);
		// Draw the actual tree
		m_treeController
				.draw(g, (long) m_cameraX + 400, (long) m_cameraY + 300);

		float fontSize = 12.0f;
		Font font = g.getFont().deriveFont(fontSize);
		g.setFont(font);

		// Draw the controls text.
		int textHeight = g.getFontMetrics().getHeight()
				+ g.getFontMetrics().getLeading();
		g.setColor(Color.WHITE);
		g.drawString("Pan: Drag Left Mouse", 21, 551);
		g.drawString("Zoom: Scroll Mouse Wheel", 21, 551 + textHeight);
		g.drawString("Zoom to Node: Double Click Node", 21, 551 + textHeight * 2);
	}

	/**
	 * Moves the center of the TreeController when the mouse is dragged.
	 */
	public void mouseDragged(MouseEvent e) {
		// Compute the offset from the last mouse location.
		int deltaX = e.getX() - lastMouseX;
		int deltaY = e.getY() - lastMouseY;

		m_cameraX += deltaX;
		m_cameraY += deltaY;

		m_beginCameraX = m_cameraX;
		m_beginCameraY = m_cameraY;
		m_targetCameraX = m_cameraX;
		m_targetCameraY = m_cameraY;
		m_beginZoom = m_treeController.getZoom();
		m_targetZoom = m_treeController.getZoom();

		m_panInterpolateTime = 0.0;
		m_panUpdateTimer.stop();

		lastMouseX = e.getX();
		lastMouseY = e.getY();
		repaint();
	}

	/**
	 * Keeps track of the last mouse position.
	 */
	public void mousePressed(MouseEvent e) {
		// Keep track of the last mouse location.

		lastMouseX = e.getX();
		lastMouseY = e.getY();

		// Adjusts the slider if pressed.
		if (e.getSource() == m_zoomSlider) {
			m_zoomSliderPressed = true;
			m_zoomSliderUpdateTimer.restart();
		}
	}

	// Unused event handlers
	public void mouseMoved(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void stateChanged(ChangeEvent e) {
	}

	// Double clicking on nodes handling.
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			TreeController<String>.Circle circle = m_treeController
					.getCircleAtPoint(e.getX(), e.getY());
			if (circle != null) {
				double targetZoom = m_treeController.getZoom()
						* ((double) TreeController.kPrimaryNodeRadius / (double) circle.radius);
				panCameraToPoint(circle.x, circle.y);
				zoomCameraSmooth(targetZoom);
			}
		}
	}

	// Resets the slider on mouseReleased.
	public void mouseReleased(MouseEvent e) {
		m_zoomSlider.setValue(50);
		m_zoomSliderPressed = false;
		m_zoomSliderUpdateTimer.stop();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Button Clicking
		if (e.getSource() == m_quitButton) { // Quit Button.
			setVisible(false);
			m_frame.dispose();
		} else if (e.getSource() == m_loadButton) {// Load Button.
			JFileChooser fileGetter = new JFileChooser();
			FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(
					"tree files (*.tree)", "tree");
			fileGetter.setFileFilter(fileFilter);
			fileGetter.setDialogTitle("Open tree file");
			int returnVal = fileGetter.showOpenDialog(null);
			if (returnVal == JFileChooser.CANCEL_OPTION) {

			} else if (returnVal == JFileChooser.APPROVE_OPTION) {
				Tree<String> tree = null;
				try {
					tree = TreeLoader
							.loadTreeFile(fileGetter.getSelectedFile());
				} catch (IOException exc) {
					System.out.println("Could not load Tree file!");
					System.out.println(exc);
				}
				if (tree != null) {
					m_treeController.setTree(tree);
					resetView();
				}
			}
		} else if (e.getSource() == m_zoomSliderUpdateTimer) // Trigger that
																// updates
																// slider zoom
																// every 1/30 of
																// a second.
		{

			zoomView((double) (m_zoomSlider.getValue() - 50) / 50.0, 0, 0);
			if (m_zoomSliderPressed) {
				m_zoomSliderUpdateTimer.restart();
			}
		} else if (e.getSource() == m_panUpdateTimer) // Trigger that updates
														// the camera panning
														// every 1/30 of a
														// second.
		{
			m_panInterpolateTime += m_panUpdateTimer.getDelay() / 1000.0;
			if (m_panInterpolateTime > kPanDuration)
				m_panInterpolateTime = kPanDuration;

			boolean useSpringy = m_beginZoom < m_targetZoom;
			// update zoom
			if (useSpringy)
				m_treeController.setZoom(interpolateBack(m_beginZoom,
						m_targetZoom, m_panInterpolateTime / kPanDuration));
			else
				m_treeController.setZoom(interpolateCos(m_beginZoom,
						m_targetZoom, m_panInterpolateTime / kPanDuration));

			DecimalFormat df = new DecimalFormat();
			df.setMinimumFractionDigits(2);
			df.setMaximumFractionDigits(2);
			m_zoomLabel.setText(df.format(m_treeController.getZoom()) + "x");

			// update pan
			if (useSpringy) {
				m_cameraX = interpolateBack(m_beginCameraX, m_targetCameraX,
						m_panInterpolateTime / kPanDuration);
				m_cameraY = interpolateBack(m_beginCameraY, m_targetCameraY,
						m_panInterpolateTime / kPanDuration);
			} else {
				m_cameraX = interpolateCos(m_beginCameraX, m_targetCameraX,
						m_panInterpolateTime / kPanDuration);
				m_cameraY = interpolateCos(m_beginCameraY, m_targetCameraY,
						m_panInterpolateTime / kPanDuration);
			}
			repaint();
			if (m_panInterpolateTime < kPanDuration) {
				m_panUpdateTimer.restart();
			} else {
				m_panUpdateTimer.stop();
			}
		} else if (e.getSource() == m_resetButton) { // Reset Button.
			resetView();
		}

	}

	/**
	 * Interpolates a value from begin to end along one quarter of a Sine
	 * wavelength
	 * 
	 * @param begin
	 *            - Begin value
	 * @param end
	 *            - End value
	 * @param t
	 *            - Percent distance from begin to end
	 * @return - Interpolated value
	 */
	public double interpolateCos(double begin, double end, double t) {
		return (end - begin) * (-0.5 * Math.cos(Math.PI * t) + 0.5) + begin;
	}

	/**
	 * Interpolates a value from begin to end along a smooth easeOut that
	 * slightly overshoots the target and richochets back
	 * 
	 * @param begin
	 *            - Begin value
	 * @param end
	 *            - End value
	 * @param t
	 *            - Percent distance from begin to end
	 * @return - Interpolated value
	 */
	public double interpolateBack(double begin, double end, double t) {
		// copied from gskinner easing function
		double s = 1.70158;
		double t2 = t - 1.0;
		return (t2 * t2 * ((s + 1.0) * t2 + s) + 1.0) * (end - begin) + begin;
	}

	/**
	 * Begin a smooth camera pan to a point
	 * 
	 * @param x
	 *            - X coordinate
	 * @param y
	 *            - Y coordinate
	 */
	public void panCameraToPoint(int x, int y) {
		m_targetCameraX = m_cameraX - (x - 400);
		m_targetCameraY = m_cameraY - (y - 300);
		m_beginCameraX = m_cameraX;
		m_beginCameraY = m_cameraY;

		m_panInterpolateTime = 0;
		m_panUpdateTimer.restart();

	}

	/**
	 * Immediately zoom the camera view a percent of the set zoomSpeed
	 * 
	 * @param zoomAmount
	 *            - Relative amount to change the zoom, as a percent of max zoom
	 *            speed
	 * @param x
	 *            - screen X to scroll toward
	 * @param x
	 *            - screen Y to scroll toward
	 */
	public void zoomView(double zoomAmount, int x, int y) {
		m_beginCameraX = m_cameraX;
		m_beginCameraY = m_cameraY;
		m_targetCameraX = m_cameraX;
		m_targetCameraY = m_cameraY;
		m_beginZoom = m_treeController.getZoom();
		m_targetZoom = m_treeController.getZoom();

		m_panInterpolateTime = 0.0;
		m_panUpdateTimer.stop();

		double oldZoom = m_treeController.getZoom();
		double deltaZoom = zoomAmount * kZoomRate * oldZoom;
		m_treeController.setZoom(oldZoom + deltaZoom);

		DecimalFormat df = new DecimalFormat();
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		m_zoomLabel.setText(df.format(m_treeController.getZoom()) + "x");

		m_cameraX = m_cameraX / oldZoom * m_treeController.getZoom();
		m_cameraY = m_cameraY / oldZoom * m_treeController.getZoom();

		// Repaints the changes made to the camera.
		repaint();
	}

	/**
	 * Begin a smooth camera zoom
	 * 
	 * @param targetZoom
	 *            - Absolute zoom factor
	 */
	public void zoomCameraSmooth(double targetZoom) {
		m_beginZoom = m_treeController.getZoom();

		m_treeController.setZoom(targetZoom);
		m_targetZoom = m_treeController.getZoom();
		m_treeController.setZoom(m_beginZoom);

		m_panInterpolateTime = 0;
		m_panUpdateTimer.restart();

		m_targetCameraX = m_targetCameraX / m_beginZoom * m_targetZoom;
		m_targetCameraY = m_targetCameraY / m_beginZoom * m_targetZoom;

		m_panInterpolateTime = 0;
		m_panUpdateTimer.restart();
	}

	/**
	 * Resets the camera view.
	 */
	public void resetView() {
		m_treeController.setZoom(1.0);
		m_cameraX = 0;
		m_cameraY = 0;
		m_targetCameraX = 0;
		m_targetCameraY = 0;
		m_targetZoom = 1.0;
		DecimalFormat df = new DecimalFormat();
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		m_zoomLabel.setText(df.format(m_treeController.getZoom()) + "x");
		repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// Handles mouse wheel zooming.
		int notches = e.getWheelRotation();
		zoomView(notches * -0.5, e.getX(), e.getY());

	}

	// This method exists to create an example tree for the program to display.
	public void displayExampleTree() {
		Tree<String> tree = new Tree<String>("Example");
		Tree.Node<String> nodea = tree.getRootNode().addChild("A");
		Tree.Node<String> nodeb = tree.getRootNode().addChild("B");
		Tree.Node<String> nodec = tree.getRootNode().addChild("C");
		Tree.Node<String> noded = tree.getRootNode().addChild("Fractal");
		nodea.addChild("A1");
		nodea.addChild("A2");
		nodea.addChild("A3");
		nodea.addChild("A4");
		nodeb.addChild("B1");
		nodeb.addChild("B2");
		nodeb.addChild("B3");
		nodec.addChild("C1");
		nodec.addChild("C2");
		nodec.addChild("C3");
		nodec.addChild("C4");
		noded.addChild("Whoa").addChild(tree.getRootNode());
		Tree.Node<String> nodec5 = nodec.addChild("C5");
		Tree.Node<String> nodec6 = nodec.addChild("C6");
		nodec5.addChild("C5A");
		nodec5.addChild("C5B");
		nodec6.addChild("C6A");
		nodec6.addChild("C6B");
		Tree.Node<String> nodec6a = nodec6.addChild("C6A");
		nodec6.addChild("C6B");
		nodec6.addChild("C6C");
		nodec6.addChild("C6D");
		nodec6.addChild("C6E");
		nodec6.addChild("C6F");
		nodec6a.addChild("C6A1");
		nodec6a.addChild("C6A2");
		nodec6a.addChild("C6A3");
		nodec6a.addChild("C6A4");
		nodec6a.addChild("C6A5");
		nodec6a.addChild("C6A6");
		nodec6a.addChild("C6A7");
		nodec6a.addChild("C6A8");
		Tree.Node<String> nodeFractal = nodec6a.addChild("F");
		nodeFractal.addChild("R").addChild("A").addChild("C").addChild("T")
				.addChild("A").addChild("L").addChild("S").addChild("A")
				.addChild("R").addChild("E").addChild("F").addChild("U")
				.addChild("N").addChild("!").addChild(nodeFractal);
		m_treeController = new TreeController<String>(tree);
	}
}