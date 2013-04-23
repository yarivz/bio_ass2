import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args){
    	try{
			  // Open the file
			  FileInputStream fstream = new FileInputStream(args[2]);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  
			  ArrayList<String> DB = new ArrayList<String>();
			  while (br.readLine() != null) 	// read db
				  DB.add(br.readLine());
			  in.close();
			  
			  String query = "";
			  ArrayList<AlignOut> alignList = new ArrayList<AlignOut>();
			  for(int i=0;i<5;i++)
				  alignList.add(new AlignOut());
			  
			  String option1 = args[0];
		      if(option1.equals("-l"))		 //Local Alignment is used
		      {
		    	// Open the file
				fstream = new FileInputStream(args[3]);
				// Get the object of DataInputStream
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in));
		    	//Read File Line By Line
	  			 
  				
  				while(br.readLine() != null)	// read query
  				{
  					query = br.readLine();
  					for(int j=0;j<DB.size();j++)
  					{
  					
	  					LocalAlignment align = new LocalAlignment(args[1],query,DB.get(j),option1,"");
	  					int max = align.local();
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
  	  					alignList.get(i).print(query, DB.get(alignList.get(i).dbIndex));
  				}
  			  }
		      
				else if(option1.equals("-h"))		//heuristic Local Alignment is used
				{   
		
				}
				else
				{
					System.out.println("Usage: java -jar Alignments.jar <option> Score.matrix2 virus_DB.fasta.txt query.fasta.txt");
		            System.out.println("option: [-l = Local Alignment | -h = Heuristic Alignment]");
		        }
		      
		      //Close the input streams
			  in.close();
			  
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
    	/*}
    	catch (Exception e){//Catch exception if any
    			System.err.println("Please check your syntax. Your sequence may contain illegal characters or your arguments may not be valid.");
			  }*/
    }
}

