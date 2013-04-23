import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class FastaYariv {

    int dbLength;
    int queryLength;
    int ktup;
    HashMap<String,HashMap<Integer,ArrayList<Integer>>> dictionary;
    HashMap<Integer,ArrayList<Diagonal>> hotspots;

    String db;
    String query;
    String curStr;
    String curQuery;
    Diagonal curDiag;
    ArrayList<Diagonal> tempDiags;
    ArrayList<Integer> curList,values;
    HashMap<Integer,ArrayList<Integer>> curMap;


    public void buildDict(String dbFile){
        dictionary = new HashMap<String,HashMap<Integer,ArrayList<Integer>>>();

        try{
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(dbFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            int lineIndex = 1;
            int dbNum = -1;
            String curDB = "";
            while ((strLine = br.readLine()) != null)
            {
                if(strLine.charAt(0)=='>')
                {
                    if(!curDB.isEmpty())
                    {
                        dbLength = curDB.length()-ktup+1;
                        for(int j=0;j<dbLength;j++){
                            curStr = curDB.substring(j,j+ktup);
                            if((curList = dictionary.get(curStr).get(dbNum))!=null){
                                curList.add(j);
                            }
                            else{
                                curList = new ArrayList<Integer>();
                                curList.add(j);
                                curMap = new HashMap<Integer,ArrayList<Integer>>();
                                curMap.put(dbNum,curList);
                                dictionary.put(curStr,curMap);
                            }
                        }
                    }
                    dbNum++;
                    curDB = "";
                }
                else{
                    curDB+=strLine;
                }
            }
            //Close the input stream
            fstream.close();
            br.close();
            in.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

    public void checkQuery(String q){
        queryLength = q.length();
        hotspots = new HashMap<Integer,ArrayList<Diagonal>>();
        for(int i=0;i<queryLength-ktup+1;i++){    //i is the current index in the Query string
            curQuery = q.substring(i,i+ktup);     //get the current sliding window view of the query
            curMap =  dictionary.get(curQuery);
            if(curMap != null){
                Set<Integer> keys = curMap.keySet();
                if(keys != null && !keys.isEmpty()){
                    for(Integer k:keys)    //k is the current db String number
                    {
                        if((values = curMap.get(k))!=null)
                        {
                            tempDiags = new ArrayList<Diagonal>();
                            for(Integer j:values)           //j is the current index in the db String for the match to curQuery
                            {
                                curDiag = new Diagonal(i,j,ktup,ktup);
                                tempDiags.add(curDiag);
                            }
                            hotspots.put(k,tempDiags);
                        }
                    }
                }
            }
        }
    }

    public HashMap<Integer,ArrayList<Diagonal>> buildDiagonals(){
        HashMap<Integer,ArrayList<Diagonal>> diags = new HashMap<Integer,ArrayList<Diagonal>>();
        ArrayList<Diagonal> diagList;
        HashMap<Integer,ArrayList<Diagonal>> diagMap;
        for(int i=0;i<hotspots.size();i++){    //i is the current db String number
            tempDiags = hotspots.get(i);       //tempdiags is the list of hotspots found for the current db str and query
            diagMap = new  HashMap<Integer,ArrayList<Diagonal>>();  //diagMap holds the current Diagonals for this query and db str
            for(Diagonal d:tempDiags){        //sort Diagonals according to their i-j ratios - group same diagonal runs together
                int key = d.i-d.j;
                if(diagMap.containsKey(key))    //if there are other hotspots in the same diagonal, try to connect neighbouring hotspots
                {
                    diagList =  diagMap.get(key);
                    for(Diagonal g:diagList)                    //iterate over the current Diagonals already found
                    {
                        Diagonal upper = (d.j < g.j ? d : g);    //get the diagonal which is the upper-left of the two
                        Diagonal lower = (upper == d ? g : d);   //get the diagonal which is the lower-right of the two
                        int distance = lower.j-upper.j;      //get the distance between their indexes
                        if(distance<=upper.length)    //Diagonals are overlapping
                        {
                            diagList.remove(lower);
                            diagList.remove(upper);
                            upper.length = distance+lower.length;          //combine the diagonals
                            upper.score = upper.length;
                            diagList.add(upper);
                            break;
                        }
                        else if(distance > upper.length && distance - upper.length < lower.length){   //distance between Diagonals is smaller than the length of the lower
                            diagList.remove(lower);
                            diagList.remove(upper);
                            upper.score = upper.length - (distance-upper.length)+lower.length;
                            upper.length = distance+lower.length;
                            //TODO may need to add field to Diagonal about how many mismatches were included
                            diagList.add(upper);
                            break;
                        }
                    }
                   // diagList.add(d);
                }
                else
                {
                    diagList = new ArrayList<Diagonal>();
                    diagList.add(d);
                    diagMap.put(key,diagList);
                }
            }



        }

    return diags;
    }

}
