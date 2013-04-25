import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


public class FastaAlon{
	String dbStr;
	String query;
	ArrayList<Diagonal> diagList;
	int hsThreshold;			// hot spot threshold
	int chainingThreshold;
	int diagonalNum;
	int indel;
	
	public FastaAlon(String query, String dbStr, int hsThreshold, int chainingThreshold, int diagonalNum, int indel)
	{
		this.query = query;
		this.dbStr = dbStr;
		diagList = new ArrayList<Diagonal>(diagonalNum+2);		// including source and target diagonal vertices
		diagList.add(0, new Diagonal(0, -1,0, 0));			// source
		diagList.add(diagonalNum+1, new Diagonal(0,Integer.MAX_VALUE ,0, 0));	//traget
		
		this.hsThreshold = hsThreshold;
		this.chainingThreshold = chainingThreshold;
		this.diagonalNum = diagonalNum;
		this.indel = indel;
	}
	
	public Rectangle chaining()
	{
		// sort vertices
		Collections.sort(diagList);
		
		// reduction - create adj lists
		for(int i=0;i<diagonalNum;i++)
		{
			Diagonal v1 = diagList.get(i);
			for(int j = i+1;j<diagonalNum;j++)
			{
				Diagonal v2 = diagList.get(j);
				if(v1.i+v1.length-1 <= v2.i && v1.j+v1.length-1 <= v2.j)
					v1.adj.add(new edge(v1,v2,((v2.i - v1.i)+(v2.j - v1.j))*indel));
			}
		}
	    
	    // dag algo

		for(int i=0;i<diagonalNum;i++)
		{
			Iterator<edge> iter = diagList.get(i).adj.iterator();
			while(iter.hasNext())
			{
				edge e = iter.next();
				int c = e.vs.pathValue + e.vs.score + e.weight;
				if (c > e.vt.pathValue)
				{
					e.vt.pathValue = c;
					e.vt.predecessor = e.vs;
				}
			}
		}
		
		// trace back
		Rectangle rect = new Rectangle();
		Diagonal temp = diagList.get(diagonalNum-1).predecessor;
		rect.lowerI = temp.i + temp.length;
		rect.lowerJ = temp.j + temp.length;
		while(temp.predecessor.j == -1)
			temp = temp.predecessor;
		
		rect.upperI = temp.i;
		rect.upperI = temp.j;
		
		return rect;
	}
}