import java.util.ArrayList;

public class Diagonal implements Comparable<Diagonal>{
	
	int i;
	int j;
	int length;
	int score;
	ArrayList<edge> adj;
	int pathValue;
	Diagonal predecessor;
	
	public Diagonal(int i, int j,int length, int score)
	{
		this.i = i;
		this.j = j;
		this.length = length;
		this.score = score;
		adj = new ArrayList<edge>();
		pathValue = 0;
		predecessor = null;
	}

	@Override
	public int compareTo(Diagonal other) {
		return (this.i+this.j) - (other.i + other.j);
	}
}