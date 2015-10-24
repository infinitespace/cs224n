package cs224n.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cs224n.ling.Tree;
import cs224n.ling.Trees;
import cs224n.ling.Trees.MarkovizationAnnotationStripper;
import cs224n.util.Filter;

/**
 * Class which contains code for annotating and binarizing trees for
 * the parser's use, and debinarizing and unannotating them for
 * scoring.
 */
public class TreeAnnotations {

	public static Tree<String> annotateTree(Tree<String> unAnnotatedTree) {

		// Currently, the only annotation done is a lossless binarization

		// TODO: change the annotation from a lossless binarization to a
		// finite-order markov process (try at least 1st and 2nd order)

		// TODO : mark nodes with the label of their parent nodes, giving a second
		// order vertical markov process
		Tree<String> tree = VerticalMarkovTreeThird(unAnnotatedTree);
		return binarizeTreeHorizontalOne(tree);
	}


	private static Tree<String> VerticalMarkovTreeSecond(Tree<String> tree) {
		String label = tree.getLabel();
		// leaf or unary
		if (tree.isLeaf())
			return tree;
		// unary && non-PreTerminal
		if (tree.getChildren().size() == 1 && !tree.getChildren().get(0).isPreTerminal()){
			VerticalMarkovTreeSecond(tree.getChildren().get(0));
			return tree;
		}

		// otherwise, it's a binary-or-more local tree, get parent
		String parent;
		int del = label.indexOf("^");
		if(del > -1) parent = label.substring(0, del);
		else parent = label;

		// annotate children
		for(int i =0; i<tree.getChildren().size(); ++i){
			if (tree.getChildren().get(i).isLeaf()) continue;
			String childname = tree.getChildren().get(i).getLabel() + "^" + parent;
			tree.getChildren().get(i).setLabel(childname);
			VerticalMarkovTreeSecond(tree.getChildren().get(i));
		}
		return tree;
	}

	private static Tree<String> VerticalMarkovTreeThrid(Tree<String> tree) {
		String label = tree.getLabel();
		// leaf
		if (tree.isLeaf()) return tree;
		// unary && non-PreTerminal
		if (tree.getChildren().size() == 1 && !tree.getChildren().get(0).isPreTerminal()) {
			VerticalMarkovTreeThrid(tree.getChildren().get(0));
			return tree;
		}

		// otherwise, it's a binary-or-more local tree, get parent
		String parent = "";
		int idxp = label.indexOf("^");
		if(idxp > -1){
			int idxgp = label.substring(idxp + 1, label.length() - 1).indexOf("^");
			if(idxgp > -1) parent += label.substring(0, idxp + 1 + idxgp);
			else parent = label;
		}
		else parent = label;

		// annotate children
		for(int i =0; i<tree.getChildren().size(); ++i){
			if (tree.getChildren().get(i).isLeaf()) continue;
			String childname = tree.getChildren().get(i).getLabel() + "^" + parent;
			tree.getChildren().get(i).setLabel(childname);
			VerticalMarkovTreeThrid(tree.getChildren().get(i));
		}
		return tree;
	}

	private static Tree<String> binarizeTree(Tree<String> tree) {
		String label = tree.getLabel();
		if (tree.isLeaf())
			return new Tree<String>(label);
		if (tree.getChildren().size() == 1) {
			return new Tree<String>
			(label, 
					Collections.singletonList(binarizeTree(tree.getChildren().get(0))));
		}
		// otherwise, it's a binary-or-more local tree, 
		// so decompose it into a sequence of binary and unary trees.
		String intermediateLabel = "@"+label+"->";
		Tree<String> intermediateTree =
				binarizeTreeHelper(tree, 0, intermediateLabel);
		return new Tree<String>(label, intermediateTree.getChildren());
	}

	private static Tree<String> binarizeTreeHelper(Tree<String> tree,
			int numChildrenGenerated, 
			String intermediateLabel) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree));
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			Tree<String> rightTree = 
					binarizeTreeHelper(tree, numChildrenGenerated + 1, 
							intermediateLabel + "_" + leftTree.getLabel());
			children.add(rightTree);
		}
		return new Tree<String>(intermediateLabel, children);
	} 

	private static Tree<String> binarizeTreeHorizontalOne(Tree<String> tree) {
		String label = tree.getLabel();
		if (tree.isLeaf())
			return new Tree<String>(label);
		if (tree.getChildren().size() == 1) {
			return new Tree<String>(label, Collections.singletonList(binarizeTreeHorizontalOne(tree.getChildren().get(0))));
		}
		// otherwise, it's a binary-or-more local tree, 
		// so decompose it into a sequence of binary and unary trees.
		String intermediateLabel = "@"+label+"->";
		Tree<String> intermediateTree =
				binarizeTreeHorizontalOneHelper(tree, 0, intermediateLabel);
		return new Tree<String>(label, intermediateTree.getChildren());
	}

	private static Tree<String> binarizeTreeHorizontalOneHelper(Tree<String> tree,
			int numChildrenGenerated, 
			String intermediateLabel) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree));
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			int idxp = intermediateLabel.indexOf("_");
			String newname;
			if(idxp > -1){
				int idxgp = intermediateLabel.substring(idxp + 1, intermediateLabel.length() - 1).indexOf("_");
				if(idxgp > -1) newname = intermediateLabel.substring(0, idxp + 1 + idxgp) + "_" + leftTree.getLabel();
				else newname = intermediateLabel + "_" + leftTree.getLabel();
			}
			else newname = intermediateLabel + "_" + leftTree.getLabel();
			
			Tree<String> rightTree = binarizeTreeHorizontalOneHelper(tree, numChildrenGenerated + 1, newname);
			children.add(rightTree);
		}
		return new Tree<String>(intermediateLabel, children);
	} 

	public static Tree<String> unAnnotateTree(Tree<String> annotatedTree) {

		// Remove intermediate nodes (labels beginning with "@"
		// Remove all material on node labels which follow their base symbol
		// (cuts at the leftmost - or ^ character)
		// Examples: a node with label @NP->DT_JJ will be spliced out, 
		// and a node with label NP^S will be reduced to NP

		Tree<String> debinarizedTree =
				Trees.spliceNodes(annotatedTree, new Filter<String>() {
					public boolean accept(String s) {
						return s.startsWith("@");
					}
				});
		Tree<String> unAnnotatedTree = 
				(new Trees.FunctionNodeStripper()).transformTree(debinarizedTree);
		Tree<String> unMarkovizedTree =
		    (new Trees.MarkovizationAnnotationStripper()).transformTree(unAnnotatedTree);
		return unMarkovizedTree;
	}
}
