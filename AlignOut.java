public class AlignOut{
	
	final byte DIAG = 1;
	final byte UP = 2;
	final byte LEFT = 3;
	
	int dbIndex;
	int score;
    int i,j;
    byte [][] traceBack;
	
	public AlignOut(int dbIndex, int score, int i, int j, byte [][] traceBack)
	{
		this.dbIndex = dbIndex;
		this.score = score;
		this.i = j;
		this.j = i;
		this.traceBack = traceBack;
	}

	public AlignOut(AlignOut other) 
	{
		this.dbIndex = other.dbIndex;
		this.score = other.score;
		this.i = other.j;
		this.j = other.i;
		this.traceBack = other.traceBack;
	}
	
	public AlignOut()
	{
		
	}
	
	public void print(String s1,String s2)			// printing the two aligned sequences
	{
		String outString1 = "";
		String outString2 = "";
        while((i>1 || j>1) && traceBack[i][j]!=0)
        {        	
            if(traceBack[i][j]==DIAG)
            {
                outString1 = s1.substring(i-1,i) + outString1;
                outString2 = s2.substring(j-1,j) + outString2;
                i--;
                j--;
            }
            else if (traceBack[i][j]==UP)
            {
                outString1 = s1.substring(i-1,i) + outString1;
                outString2 = "_" + outString2;
                i--;
            }
            else
            {
                outString1 = "_" + outString1;
                outString2 = s2.substring(j-1,j) + outString2;
                j--;
            }
        }
        System.out.println();
		System.out.println(outString1);
		System.out.println(outString2);
		System.out.println("Score: "+score);
	}
}