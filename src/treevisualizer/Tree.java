package treevisualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * A Tree structure that holds Nodes, each of which stores data of type T.
 * 
 * @author Elliot Hatch and Samuel Davidson
 * @version March 19, 2014
 * @param <T>
 *            - Data type stored in the Tree
 */
public class Tree<T> {
	private Node<T> m_root;

	/**
	 * Creates an instance of a Tree with a root node.
	 * 
	 * @param rootData
	 *            - Data to be stored in the root node
	 */
	public Tree(T rootData) {
		m_root = new Node<T>(rootData, null);
	}

	/**
	 * Returns a reference to the root node.
	 * 
	 * @return - The root node of the Tree.
	 */
	public Node<T> getRootNode() {
		return m_root;
	}

	/**
	 * Nodes stored in a Tree structure that hold data. Each node acts as a
	 * tree.
	 * 
	 * @param <T>
	 *            - Type of data stored in Node.
	 */
	public static class Node<T> {
		private T m_data;
		private Node<T> m_parent;
		private List<Node<T>> m_children;

		/**
		 * Creates an instance of a Node with data and a parent.
		 * 
		 * @param data
		 *            - Data stored in the node
		 * @param parent
		 *            - Parent node
		 */
		public Node(T data, Node<T> parent) {
			m_data = data;
			m_parent = parent;
			m_children = new ArrayList<Node<T>>();
		}

		/**
		 * Creates a Node with data and adds it as a child to this Node
		 * 
		 * @param data
		 *            - Data stored in the new child Node.
		 * @return - The newly created child Node.
		 */
		public Node<T> addChild(T data) {
			Node<T> child = new Node<T>(data, this);
			m_children.add(child);
			return child;
		}

		/**
		 * Adds a node as a child of this node. Nodes already in the tree can be
		 * added so fractal trees can be made.
		 * 
		 * @param node
		 *            - Node to be added as a child.
		 * @return - The node added
		 */
		public Node<T> addChild(Node<T> node) {
			m_children.add(node);
			return node;
		}

		/**
		 * Get data stored in Node.
		 * 
		 * @return - Data stored in the Node.
		 */
		public T getData() {
			return m_data;
		}

		/**
		 * Get this Node's parent
		 * 
		 * @return - The Node's parent
		 */
		public Node<T> getParent() {
			return m_parent;
		}

		/**
		 * Get the children of this Node
		 * 
		 * @return - A List containing this Node's children. Nodes are in the
		 *         order they were added as children to this Node.
		 */
		public List<Node<T>> getChildren() {
			return m_children;
		}

		/**
		 * Set new data in the Node. Overwrites old data.
		 * 
		 * @param data
		 *            - Data stored in the Node.
		 */
		public void setData(T data) {
			m_data = data;
		}

	}
}
