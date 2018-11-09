package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common;

/**
 * Represents an edge
 * @author Luis Ferrolho, fc41914, Faculdade de Ciencias da Universidade de Lisboa
 *
 * @param <T> Ids of the end point nodes
 */
public class Pair<T> {

	private T left;
	private T right;

	public Pair(T left, T right)
	{
		if (left == null || right == null) { 
			throw new IllegalArgumentException("Left and Right must be non-null!");
		}
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean equals(Object o)
	{
		if (! (o instanceof Pair)) { return false; }
		Pair p = (Pair)o;
		return left.equals(p.left) && right.equals(p.right);
	} 

	@Override
	public int hashCode()
	{
		return 7 * left.hashCode() + 13 * right.hashCode();
	}

	public T getLeft(){
		return left;
	}

	public T getRight(){
		return right;
	}
}