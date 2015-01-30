package ets.genielog.appwatcher_3;

import java.util.ArrayList;

import ets.genielog.appwatcher_3.TreeModel.TreeNode;



 
/**
 * Version of tree model wich sorts all n-gram by decrasing frequency when 
 * a tree is updated.
 * @author trivh
 *
 */
public class TreeModelSorted extends TreeModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2477109039700848515L;

	/**
	 * Calls super constructor and specifies it app name and model type ID.
	 * @param appname
	 */
	public TreeModelSorted(String appname) {
		super(appname, 2, 0.1);
	}
	public TreeModelSorted(String appname, int depth){
		super(appname, depth, 0.1);
	}

	public TreeModelSorted(String appname, int treeDepth, int nGramTotal, TreeNode root){
		super(appname, treeDepth, nGramTotal, root);
	}

	/**
	 * Same as addInTree, but makes sure tree children are ordered
	 * by decrasing frequency
	 * @param ngram: n-gram inserted in the tree
	 */
	@Override
	public void addInTree(ArrayList<String> ngram, TreeNode root){
		TreeNode parentNode = root;
		TreeNode childNode = null;
		int k;
		for(int i = 0; i < ngram.size(); i++){
			childNode = find(parentNode, ngram.get(i));
			if(childNode == null){
				// If node does not exist, create it and add it in the tree.
				childNode = new TreeNode(ngram.get(i));
				childNode.setIndex(parentNode.children.size());
				parentNode.add(childNode);
				nGramDistinct ++;
				nGramPerLen[i] ++; //TODO
			}
			childNode.incFreq(); // Increment apparition counter
			k = 1;
			while(childNode.getIndex() - k > -1 && childNode.getFrequency() > 
			parentNode.children.get(childNode.getIndex() - k).getFrequency()){
				k++;
			}
			if(k > 1){
				parentNode.children.remove(childNode);
				parentNode.children.add(childNode.getIndex() - (k - 1),childNode);
			}

			parentNode = childNode; // One step deeper in the tree
		}
	}



}
