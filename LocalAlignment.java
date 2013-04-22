
public class LocalAlignment extends Alignment{
	
	public LocalAlignment(String scoreMatrixFile,String s1,String s2,String option1,String option2)
	{
		super(scoreMatrixFile,s1,s2,option1,option2);
	}
	
	public void local() throws RuntimeException
	{
		int max=0;
		for(int i=1;i<s1.length()+1;i++)	
			for(int j=1;j<s2.length()+1;j++)
			{
				intOutMatrix[i][j] = Math.max(0, Math.max(intOutMatrix[i-1][j]+scoreMatrix[letters.get(s1.charAt(i-1))][6], Math.max(intOutMatrix[i-1][j-1]+scoreMatrix[letters.get(s1.charAt(i-1))][letters.get(s2.charAt(j-1))], intOutMatrix[i][j-1]+scoreMatrix[6][letters.get(s2.charAt(j-1))])));
				// updating our trace back
				if(intOutMatrix[i][j]==intOutMatrix[i-1][j-1]+scoreMatrix[letters.get(s1.charAt(i-1))][letters.get(s2.charAt(j-1))])
					traceBack[i][j] = DIAG;
				else if(intOutMatrix[i][j]==intOutMatrix[i-1][j]+scoreMatrix[6][letters.get(s1.charAt(i-1))])
					traceBack[i][j] = UP;
				else if(intOutMatrix[i][j] == intOutMatrix[i][j-1]+scoreMatrix[letters.get(s2.charAt(j-1))][6])
					traceBack[i][j] = LEFT;	
				max = Math.max(max, intOutMatrix[i][j]);
			}
		tracePath(max);		// restoring the path of our local alignment
		
	}
	
	public void localGap()
	{
	    double vValue=0;  //Variables for global alignment with gaps
        int size = Math.max(s1.length(), s2.length());
        gaps = new double[size+1];
        gaps[0]=0;
        for (int i = 1; i <= size; i++) {
            gaps[i] = gapPenalty(i);
        }
        for(int i=1;i<s1.length()+1;i++){    //iterate over the table row after row starting from (1,1)
            for(int j=1;j<s2.length()+1;j++){
                eValue = E(i,j);   //Insert Case - with Gap
                fValue = F(i,j);   //Delete Case - With Gap
                gValue = G(i,j);   //Replace/Match Case
                doubleOutMatrix[i][j] = Math.max(gValue[0],Math.max(eValue[0],fValue[0])); //current cell get the max value

                if(doubleOutMatrix[i][j] == 0)
                	continue;
            	else if (doubleOutMatrix[i][j]==gValue[0]){         //check which neighbour is the parent and mark path for trace
                    traceBack[i][j] = DIAG;
                }
                else if (doubleOutMatrix[i][j]==eValue[0]){
                    traceBack[i][j] = LEFT;
                    gapSize[i][j] = (int) eValue[1];    //check the gap size
                }
                else if (doubleOutMatrix[i][j]==fValue[0]){
                    traceBack[i][j] = UP;
                    gapSize[i][j] = (int) fValue[1];    //check the gap size
                }
                vValue = Math.max(vValue, doubleOutMatrix[i][j]);
            }
        }
		gapTracePath(vValue);			// restoring the path of our local alignment
	}
	
	public void localAffine()
	{
		int max=0;
		for(int i=1;i<s1.length()+1;i++)
			for(int j=1;j<s2.length()+1;j++)
			{
				eValue = E(i,j);
				fValue = F(i,j);
				gValue = G(i,j);
				intOutMatrix[i][j] = (int) Math.max(0, Math.max(eValue[0],Math.max(fValue[0],gValue[0])));
				// updating our trace back and the gap size we used for our current output matrix value
				if(intOutMatrix[i][j] == 0)
                	continue;
				else if(intOutMatrix[i][j]==gValue[0])
				{
					traceBack[i][j] = DIAG;
				}
				else if(intOutMatrix[i][j]==eValue[0])
				{
					gapSize[i][j] = (int) eValue[1];
					traceBack[i][j] = UP;
				}
				else if(intOutMatrix[i][j]==fValue[0])
				{
					gapSize[i][j] = (int) fValue[1];
					traceBack[i][j] = LEFT;
				}
				max = Math.max(max, intOutMatrix[i][j]);
			}
		gapTracePath(max);			// restoring the path of our local alignment
	}
	
	public void tracePath(int max)
	{
		// find calculated max value
		if(max!=0)
		{
			outerloop:
			for(int i=s1.length();i>0;i--)
				for(int j=s2.length();j>0;j--)
					if(intOutMatrix[i][j] == max)
					{
						print(i,j);
						System.out.println("Score: "+max);
						break outerloop;
					}
		}
		else
		{
			System.out.println("Empty string");
			System.out.println("Empty string");
			System.out.println("Score: 0");
		}
	}
	
	public void gapTracePath(double max)
	{
		// find calculated max value
		if(max!=0)
		{
			outerloop:
			for(int i=s1.length();i>0;i--)
				for(int j=s2.length();j>0;j--)
					if(option2.equals("-p"))		// for the arbitrary gap case
					{
						if(doubleOutMatrix[i][j] == max)
						{
							print(i,j);
							System.out.println("Score: "+max);
							break outerloop;
						}
					}		
					else							// for the affine gap case
					{
						if(intOutMatrix[i][j] == max)
						{
							print(i,j);
							System.out.println("Score: "+max);
							break outerloop;
						}
					}
		}
		else										// if our max value is 0 we'll print out 2 empty strings 
		{
			System.out.println("Empty string");
			System.out.println("Empty string");
			System.out.println("Score: 0");
		}
	}
}