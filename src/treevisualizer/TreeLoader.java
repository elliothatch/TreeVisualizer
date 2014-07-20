package treevisualizer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Class for loading .tree Files
 * 
 * @author Elliot Hatch and Samuel Davidson
 * @version March 19, 2014
 */
public class TreeLoader {

	/**
	 * Constructs a tree with String data from a .tree File
	 * 
	 * @param file
	 *            - .tree file to be loaded
	 * @return - Fully constructed Tree<String>
	 * @throws IOException
	 *             - Throws if .tree file could not be read.
	 */
	public static Tree<String> loadTreeFile(File file) throws IOException {

		Scanner fileScanner = new Scanner(file);

		Tree<String> tree = new Tree<String>(null);
		fileScanner.useDelimiter("<");

		if (fileScanner.hasNext()) {
			// initialize root node
			String nodeStr = fileScanner.next();
			Scanner nodeScanner = new Scanner(nodeStr);
			String id = nodeScanner.next();
			String nodeName = nodeScanner.next();
			nodeName = nodeName.substring(0, nodeName.length() - 1);
			tree.getRootNode().setData(nodeName);
			nodeScanner.close();
			// add all the other nodes
			Tree.Node<String> currentNode = tree.getRootNode();
			while (fileScanner.hasNext()) {
				nodeStr = fileScanner.next();
				nodeScanner = new Scanner(nodeStr);
				id = nodeScanner.next();
				if (id.substring(0, 1).equals("/")) {
					// end branch
					if (currentNode == tree.getRootNode()) {
						nodeScanner.close();
						break;
					}
					currentNode = currentNode.getParent();
					nodeScanner.close();
					continue;
				}
				nodeName = nodeScanner.next();
				nodeName = nodeName.substring(0, nodeName.length() - 1);
				currentNode = currentNode.addChild(nodeName);
				nodeScanner.close();
			}
		}

		fileScanner.close();
		return tree;
	}
}
