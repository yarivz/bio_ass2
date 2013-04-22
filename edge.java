
public class edge {
	Diagonal vs;
	Diagonal vt;
	int weight;
	
	public edge(Diagonal v1,Diagonal v2,int weight)
	{
		vs = v1;
		vt = v2;
		this.weight = weight;
	}
}