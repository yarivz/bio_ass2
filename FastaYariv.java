import java.io.*;
import java.util.*;

public class FastaYariv {

    int dbLength;
    int queryLength;
    int ktup;
    int maxDiags;
    HashMap<String,HashMap<Integer,List<Integer>>> dictionary;
    HashMap<Integer,HashMap<Integer,List<Diagonal>>> hotspots;

    String db;
    String query;
    String curStr;
    String curQuery;
    Diagonal curDiag,nextDiag;
    List<Diagonal> tempDiagList,tempDiagListTotal;
    List<Integer> curList,values;
    HashMap<Integer,List<Integer>> curMap;
    HashMap<Integer,List<Diagonal>> tempDiagMap;

    public void buildDict(String dbFile){
        dictionary = new HashMap<String,HashMap<Integer,List<Integer>>>();

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
                                curMap = new HashMap<Integer,List<Integer>>();
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
        hotspots = new HashMap<Integer,HashMap<Integer,List<Diagonal>>>();    //The first key is the DB str No., the second key is the i-j values
        for(int i=0;i<queryLength-ktup+1;i++){    //i is the current index in the Query string
            curQuery = q.substring(i,i+ktup);     //get the current sliding window view of the query
            curMap =  dictionary.get(curQuery);   //get the current HashMap containing all the DB matches to curQuery (ktup substring of query)
            if(curMap != null){
                Set<Integer> keys = curMap.keySet();
                if(keys != null && !keys.isEmpty()){
                    for(Integer k:keys)    //iterate over DB Strings, k is the current db String number
                    {
                        if((values = curMap.get(k))!=null)
                        {
                            if((tempDiagMap = hotspots.get(k)) == null)
                                tempDiagMap = new HashMap<Integer,List<Diagonal>>();  //this will hold all the diagonals for db string k and the query
                            for(Integer j:values)           //j is the current index in the db String for the match to curQuery
                            {
                                curDiag = new Diagonal(i,j,ktup,ktup);
                                if(tempDiagMap.containsKey(i-j))
                                {
                                    tempDiagMap.get(i-j).add(curDiag);
                                }
                                else
                                {
                                    tempDiagList = new ArrayList<Diagonal>();
                                    tempDiagList.add(curDiag);
                                    tempDiagMap.put(i-j,tempDiagList);
                                }
                            }
                            hotspots.put(k,tempDiagMap);
                        }
                    }
                }
            }
        }
    }

    public HashMap<Integer,List<Diagonal>> buildDiagonals(){
        HashMap<Integer,List<Diagonal>> diags = new HashMap<Integer,List<Diagonal>>();
        for(Integer k:hotspots.keySet()){    //k is the current db String number
            tempDiagMap = hotspots.get(k);       //tempdiagmap is the Map of Diagonals found for the current db str and query
            tempDiagListTotal = new ArrayList<Diagonal>();
            for(Map.Entry entry:tempDiagMap.entrySet())   //iterate over each list of Diagonals that are all on the same ratio
            {
                tempDiagList = (ArrayList<Diagonal>)entry.getValue();     //get the current list of Diagonals
                ListIterator<Diagonal> litr = tempDiagList.listIterator();
                while(litr.hasNext())       //get the current diagonal
                {
                    curDiag = litr.next();
                    while(litr.hasNext())
                    {
                        nextDiag = litr.next();     //get the next diagonal
                        int distance = nextDiag.i-curDiag.i;
                        if(curDiag.length > distance)   //Diagonals are overlapping
                        {
                            curDiag.length = distance+nextDiag.length;          //combine the diagonals
                            curDiag.score = curDiag.length*2;
                            litr.remove();
                        }
                        else if(distance > curDiag.length && (distance - curDiag.length) < nextDiag.length)   //distance between Diagonals is smaller than the length of the lower
                        {
                            curDiag.score = (curDiag.length - (distance-curDiag.length)+nextDiag.length)*2;      //combine the diagonals and account for mismatches
                            curDiag.length = distance+curDiag.length;
                            litr.remove();
                        }
                        else
                        {
                            curDiag = litr.previous();    //go back with the iterator so in the next iteration of outer loop it will point to the correct diagonal
                            break;
                        }
                    }
                }
                tempDiagListTotal.addAll(tempDiagList);
            }
            Collections.sort(tempDiagListTotal,new DiagComparator());
            tempDiagListTotal.subList(0,tempDiagListTotal.size()-maxDiags).clear();
            diags.put(k,tempDiagListTotal);
        }
    return diags;
    }



}






                       /* Diagonal upper = (d.j < g.j ? d : g);    //get the diagonal which is the upper-left of the two
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
                       // diagList.add(d);
                    }
                    else
                    {
                        diagList = new ArrayList<Diagonal>();
                        diagList.add(d);
                        diagMap.put(key,diagList);
                    }   */