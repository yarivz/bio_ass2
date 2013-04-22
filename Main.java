
public class Main {

    public static void main(String[] args){
    	
    	//try{
    	String option1 = args[0];
        if(option1.equals("-l")){    //Local Alignment is used
        	LocalAlignment align = new LocalAlignment(args[1],args[2],args[3],option1,"");
			align.local();
		}
		else if(option1.equals("-h")){   //heuristic Local Alignment is used
			String s1 = "";
			String s2 = "";
			FastaAlon fasta = new FastaAlon("","",0,0,0,0);
			Rectangle limits = fasta.chaining();
			LocalAlignment align = new LocalAlignment(args[1],s1.substring(limits.upperI, limits.lowerI),s2.substring(limits.upperJ, limits.lowerJ),option1,"");
			align.local();
		}
		else{
			System.out.println("Usage: java -jar Alignments.jar <option> Score.matrix sequence1 sequence2");
            System.out.println("option: [-l = Local Alignment | -h = Heuristic Alignment]");
            }
    	/*}
    	catch (Exception e){//Catch exception if any
    			System.err.println("Please check your syntax. Your sequence may contain illegal characters or your arguments may not be valid.");
			  }*/
    }
}

