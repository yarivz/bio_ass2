import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


public class Main {



    public static void main(String[] args){
        FileInputStream fstream;
        DataInputStream in;
        BufferedReader br;
        String query;
        int diagonalThreshold;			// hot spot threshold
        int chainingThreshold;      //cutoff
        int ktup;                   //outside parameter
        int maxDiags;               //outside parameter
        int indel;
        int bandSize;
        LocalAlignment align;
        int[][] scoreMatrix = new int[7][7];
        HashMap<Integer,List<Diagonal>> diags = new HashMap<Integer,List<Diagonal>>();
        Fasta f = new Fasta();
        if(args[0].equals("-l")||args[0].equals("-h"))
        {
            try{
                /*
                Read Parameters from file
                 */
                Properties prop = new Properties();
                fstream = new FileInputStream("params.txt");
                in = new DataInputStream(fstream);
                prop.load(in);
                diagonalThreshold = Integer.parseInt(prop.getProperty("diagonalThreshold"));
                chainingThreshold = Integer.parseInt(prop.getProperty("chainingThreshold"));
                ktup = Integer.parseInt(prop.getProperty("ktup"));
                maxDiags = Integer.parseInt(prop.getProperty("maxDiags"));
                bandSize = Integer.parseInt(prop.getProperty("bandSize"));
                in.close();
                fstream.close();
                /*
                Read Score Matrix from file
                 */
                fstream = new FileInputStream(args[1]);
                // Get the object of DataInputStream
                in = new DataInputStream(fstream);
                br = new BufferedReader(new InputStreamReader(in));
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
                indel = scoreMatrix[0][6];
                //Close the input stream
                in.close();
                br.close();
                fstream.close();

                /*
                Read DB Strings from file
                 */
                fstream = new FileInputStream(args[2]);
                // Get the object of DataInputStream
                in = new DataInputStream(fstream);
                br = new BufferedReader(new InputStreamReader(in));
                ArrayList<String> DB = new ArrayList<String>();
                while (br.readLine() != null) 	// read db
                {
                    String dbStr = br.readLine();
                    dbStr = dbStr.replaceAll("[^acgtuACTGU]", "");
                    DB.add(dbStr);
                }
                in.close();
                br.close();
                fstream.close();

                query = "";
                ArrayList<AlignOut> alignList = new ArrayList<AlignOut>();
                for(int i=0;i<5;i++)
                    alignList.add(new AlignOut());
                String option1 = args[0];
                /*
                Read Queries from file
                 */
                fstream = new FileInputStream(args[3]);     //read query
                // Get the object of DataInputStream
                in = new DataInputStream(fstream);
                br = new BufferedReader(new InputStreamReader(in));
                //Read File Line By Line
                if(option1.equals("-h"))		//heuristic Local Alignment is used
                {
                    f = new Fasta(query,DB,diagonalThreshold,chainingThreshold,maxDiags,ktup,indel);
                    System.out.println("Building Dictionary from DB...");
                    f.buildDict();
                }
                System.out.println("Running queries...");
                while(br.readLine() != null)	// read query
                {
                    long start = System.currentTimeMillis();
                    query = br.readLine();
                    query = query.replaceAll("[^acgtuACTGU]", "");
                    if(option1.equals("-h"))		//heuristic Local Alignment is used
                    {
                        f.query = query;
                        f.checkQuery();
                        diags = f.buildDiagonals();
                    }
                    for(int j=0;j<DB.size();j++)
                    {
                        Rectangle rect = new Rectangle(0,0,query.length(),DB.get(j).length(),0,0,0);
                        if(option1.equals("-h"))		//heuristic Local Alignment is used
                        {
                            List<Diagonal> diagList = diags.get(j);
                            if(diagList != null && diagList.get(diagList.size()-1).score > diagonalThreshold)
                                rect = f.chaining(diagList);
                        }
                        if(rect.maxScore < chainingThreshold)
                        {
                            rect = new Rectangle(0,0,query.length(),DB.get(j).length(),0,0,0);
                            align = new LocalAlignment(scoreMatrix,query.substring(rect.upperI,rect.lowerI),DB.get(j).substring(rect.upperJ,rect.lowerJ),"-l","");
                        }
                        else
                        	align = new LocalAlignment(scoreMatrix,query.substring(rect.upperI,rect.lowerI),DB.get(j).substring(rect.upperJ,rect.lowerJ),option1,"");
                        int max = align.local(bandSize,rect.maxBand,rect.minBand);
                        AlignOut out = align.tracePath(max);
                        out.dbIndex = j;

                        int i = alignList.size();
                        for(;i>0;i--)
                        {
                            if(out.score <= alignList.get(i-1).score)
                                break;
                        }
                        if(i < 5)	//shift if in top-5
                        {
                            for(int k=alignList.size()-1;k>i;k--)
                            {
                                alignList.set(k, alignList.get(k-1));
                            }
                            alignList.set(i, new AlignOut(out));
                        }
                    }

                    //print top-5
                    for(int i=0;i<5;i++)
                    {
                    	if(alignList.get(i).score > 0)
                    		alignList.get(i).print(query, DB.get(alignList.get(i).dbIndex));
                        alignList.set(i, new AlignOut());
                    }
                    long end = System.currentTimeMillis();
                    System.out.println("Execution time was " + (end - start) + " ms.");
                }
                //Close the input streams
                in.close();
                br.close();
                fstream.close();
                }catch (Exception e){//Catch exception if any
                  System.err.println("Error: " + e.toString());
                    e.printStackTrace();
                }
        } else
        {
            System.out.println("Usage: java -jar Alignments.jar <option> Score.matrix2 virus_DB.fasta query.fasta");
            System.out.println("option: [-l = Local Alignment | -h = Heuristic Alignment]");
        }
    	/*}
    	catch (Exception e){//Catch exception if any
    			System.err.println("Please check your syntax. Your sequence may contain illegal characters or your arguments may not be valid.");
			  }*/
    }



}
