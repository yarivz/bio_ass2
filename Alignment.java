import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;



public class Alignment{
	final byte DIAG = 1;
	final byte UP = 2;
	final byte LEFT = 3;
	
	String s1,s2,option1,option2;
	Hashtable<Character,Integer> letters;
	int [][] scoreMatrix,intOutMatrix,gapSize,E,F;
	double [][] doubleOutMatrix;
	byte [][] traceBack;
	int a,b,eGapSize,fGapSize;
    double[] eValue,fValue,gValue,gaps; //Variables for global alignment with gaps

    public Alignment(String scoreMatrixFile,String s1,String s2,String option1, String option2)
	{
		this.s1 = s1.toUpperCase();			// making sure the sequences are consisted of capital letters
		this.s2 = s2.toUpperCase();
		letters = new Hashtable<Character,Integer>();
		letters.put('A',0);
		letters.put('T',1);
		letters.put('G',2);
		letters.put('C',3);
		letters.put('U',4);
		letters.put('N',5);
		letters.put('*',6);
		scoreMatrix = new int [7][7];
		this.option1 = option1;
        this.option2 = option2;

        if(option2.equals("-p"))		// if we're in the arbitrary gap function we'll init a double output matrix
		{
			doubleOutMatrix = new double[s1.length()+1][s2.length()+1];
			gapSize = new int[s1.length()+1][s2.length()+1];
		}
		else if(option2.equals("-a"))
		{
			intOutMatrix = new int[s1.length()+1][s2.length()+1];
			gapSize = new int[s1.length()+1][s2.length()+1];
			E = new int[s1.length()+1][s2.length()+1];
			F = new int[s1.length()+1][s2.length()+1];
		}
		else					// if we're in the regular local alignment we'll init an int output matrix
			intOutMatrix = new int[s1.length()+1][s2.length()+1];		

        traceBack = new byte[s1.length()+1][s2.length()+1];
		
		
		try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(scoreMatrixFile);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  //Read File Line By Line
			  int lineIndex = 1;
			  String[] split;
			  while ((strLine = br.readLine()) != null && lineIndex<16) 
			  {
				if(lineIndex<9)			// for the unused lines in the beginning of the score matrix file
				{
					lineIndex++;
					continue;
				}
				else
				{
					split = strLine.split(" +");
					for(int i=1;i<split.length;i++)
					{
						scoreMatrix[lineIndex-9][i-1] = Integer.parseInt(split[i]);
					}
					lineIndex++;
				}
			  }
			  if(option2.equals("-a"))		// parsing the affine function coefficients
				{
					strLine = br.readLine();
					strLine = br.readLine();
					a = Integer.parseInt(strLine.substring(2));
					strLine = br.readLine();
					b = Integer.parseInt(strLine.substring(2));
				}
			  //Close the input stream
			  in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
	}
	
	public double[] G(int i, int j) throws RuntimeException{		// calculating match/mismatch value
		if(option2.equals("-p"))
	        return new double[]{doubleOutMatrix[i-1][j-1]+scoreMatrix[letters.get(s1.charAt(i-1))][letters.get(s2.charAt(j-1))],DIAG};
		else
	        return new double[]{intOutMatrix[i-1][j-1]+scoreMatrix[letters.get(s1.charAt(i-1))][letters.get(s2.charAt(j-1))],DIAG};
	    }

    public double[] E(int i, int j){	// calculating a gap within the same matrix row
    	double temp,kVal=0,max=0;
        if(option2.equals("-p"))       //the arbitrary gap case
        {
        	if(option1.equals("-g"))
        	{
                max=Double.NEGATIVE_INFINITY;
        	}
        	for(int k=0;k<j;k++)
        	{
                temp = doubleOutMatrix[i][k]-gaps[j-k];
                if(max <= temp){
                    max = temp;
                    kVal = j-k;
                }
            }
            return new double[]{max,kVal};
            
        }
        else				// affine case
        {
        	eGapSize = 1;
            E[i][j] = Math.max(E[i][j-1],intOutMatrix[i][j-1]-a) -b;
            if(E[i][j] == E[i][j-1]-b)
            {
                if(traceBack[i][j-1] == LEFT)
                    eGapSize = gapSize[i][j-1]+1;
            }
            return new double[]{E[i][j],eGapSize};
        }
    }

    public double[] F(int i, int j){		// calculating a gap within the same matrix column
    	double temp,kVal=0,max=0;
        if(option2.equals("-p"))			// the arbitrary gap case
        {
        	if(option1.equals("-g"))
        	{
                max=Double.NEGATIVE_INFINITY;
            }
            for(int k=0;k<i;k++)
            {
                temp = doubleOutMatrix[k][j]-gaps[i-k];
                if(max <= temp){
                    max = temp;
                    kVal = i-k;
                }
            }
            return new double[]{max,kVal};
        }
        else								// the affine gap case
        {
        	fGapSize = 1;
            F[i][j] = Math.max(F[i-1][j],intOutMatrix[i-1][j]-a) -b;
            if(F[i][j] == F[i-1][j]-b)
            {
                if(traceBack[i-1][j] == UP)
                    fGapSize = gapSize[i-1][j]+1;
            }
            return new double[]{F[i][j],fGapSize};
        }
    }
    
    public double gapPenalty(int k){		// gap penalty for the arbitrary gap function
        return (Math.log(k)+10);
    }
	
	public void print(int i,int j)			// printing the two aligned sequences
	{
		String outString1 = "";
		String outString2 = "";
        while((i>0 || j>0) && traceBack[i][j]!=0)
        {
        	if(option1.equals("-l") && (i==1 || j==1))		// making sure not to be out of boundaries
                 break;
            if(option2.equals("")) // alignment without gaps
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
            else					// alignment with gaps
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
                    for(int k=0;k<gapSize[i][j];k++)
                    {
                        outString2 = "_" + outString2;
                        outString1 = s1.substring(i-1-k,i-k) + outString1;
                    }
                    i = i-gapSize[i][j];
                }
                else
                {
                    for(int k=0;k<gapSize[i][j];k++)
                    {
                        outString1 = "_" + outString1;
                        outString2 = s2.substring(j-1-k,j-k) + outString2;
                    }
                    j = j-gapSize[i][j];
                }
            }
        }
        System.out.println();
		System.out.println(outString1);
		System.out.println(outString2);
	}
}