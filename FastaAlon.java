import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;



public class FastaAlon{
	String dbStr;
	String query;
	Vector<Diagonal> diagVec;
	int hsThreshold;			// hot spot threshold
	int chainingThreshold;
	int diagonalNum;
	int indel;
	
	public FastaAlon(String dbStr,String query, int hsThreshold, int chainingThreshold, int diagonalNum, int indel)
	{
		this.dbStr = dbStr;
		this.query = query;
		diagVec = new Vector<Diagonal>(diagonalNum+2);		// including source and target diagonal vertices
		diagVec.add(0, new Diagonal(0, -1,0, 0));			// source
		diagVec.add(diagonalNum+1, new Diagonal(0,Integer.MAX_VALUE ,0, 0));	//traget
		
		this.hsThreshold = hsThreshold;
		this.chainingThreshold = chainingThreshold;
		this.diagonalNum = diagonalNum;
		this.indel = indel;
	}
	
	public Rectangle chaining()
	{
		// sort vertices
		Collections.sort(diagVec);
		
		// reduction - create adj lists
		for(int i=0;i<diagonalNum;i++)
		{
			Diagonal v1 = diagVec.elementAt(i);
			for(int j = i+1;j<diagonalNum;j++)
			{
				Diagonal v2 = diagVec.elementAt(j);
				if(v1.i+v1.length-1 <= v2.i && v1.j+v1.length-1 <= v2.j)
					v1.adj.add(new edge(v1,v2,((v2.i - v1.i)+(v2.j - v1.j))*indel));
			}	//**** should we add a limit to the chaining so it won't get too low???****///
		}
	    
	    // dag algo

		for(int i=0;i<diagonalNum;i++)
		{
			Iterator<edge> iter = diagVec.elementAt(i).adj.iterator();
			while(iter.hasNext())
			{
				edge e = iter.next();
				int c = e.vs.pathValue + e.weight;
				if (c > e.vt.pathValue)
				{
					e.vt.pathValue = c;
					e.vt.predecessor = e.vs;
				}
			}
		}
		
		// trace back
		Rectangle rect = new Rectangle();
		Diagonal temp = diagVec.elementAt(diagonalNum-1).predecessor;
		rect.lowerI = temp.i + temp.length;
		rect.lowerJ = temp.j + temp.length;
		while(temp.predecessor.j == -1)
			temp = temp.predecessor;
		
		rect.upperI = temp.i;
		rect.upperI = temp.j;
		
		return rect;
	}
}