import java.util.*;

public class Fasta {
    String dbStr;
    String query;
    int hsThreshold;			// hot spot threshold
    int chainingThreshold;
    int indel;
    int dbLength;
    int queryLength;
    int ktup;                    //outside parameter
    int maxDiags;               //outside parameter
    HashMap<String,HashMap<Integer,List<Integer>>> dictionary;
    HashMap<Integer,HashMap<Integer,List<Diagonal>>> hotspots;
    String curQuery;
    Diagonal curDiag,nextDiag;
    ArrayList<String> db;
    List<Diagonal> tempDiagList,tempDiagListTotal;
    List<Integer> curList,values;
    HashMap<Integer,List<Integer>> curMap;
    HashMap<Integer,List<Diagonal>> tempDiagMap;

    public Fasta(String query, ArrayList<String> db, int hsThreshold, int chainingThreshold, int maxDiags, int ktup, int indel)
    {
        this.query = query;
        this.db = db;
        this.hsThreshold = hsThreshold;
        this.chainingThreshold = chainingThreshold;
        this.maxDiags = maxDiags;
        this.ktup = ktup;
        this.indel = indel;
    }
    public Fasta(){}

    public Rectangle chaining(List<Diagonal> diagList)
    {
        diagList.add(0, new Diagonal(0, -1,0, 0));			// source
        diagList.add(diagList.size(), new Diagonal((Integer.MAX_VALUE)/2, (Integer.MAX_VALUE)/2 ,0, 0));	//traget

        // sort vertices
        Collections.sort(diagList);

        // reduction - create adj lists
        for(int i=0;i<diagList.size()-1;i++)
        {
            Diagonal v1 = diagList.get(i);
            for(int j = i+1;j<diagList.size();j++)
            {
                Diagonal v2 = diagList.get(j);
                if(i==0)
                	v1.adj.add(new edge(v1,v2,0));
                else if(v1.i+v1.length-1 <= v2.i && v1.j+v1.length-1 <= v2.j)
            	{
                	if(j==diagList.size()-1)
                		v1.adj.add(new edge(v1,v2,0));
                	else
                		v1.adj.add(new edge(v1,v2,((v2.i - v1.i)+(v2.j - v1.j))*indel));
            	}
            }
        }

        // dag algo

        for(int i=0;i<diagList.size()-1;i++)
        {
            Iterator<edge> iter = diagList.get(i).adj.iterator();
            while(iter.hasNext())
            {
                edge e = iter.next();
                int c = e.vs.pathValue + e.vt.score + e.weight;
                if (c > e.vt.pathValue)
                {
                    e.vt.pathValue = c;
                    e.vt.predecessor = e.vs;
                }
            }
        }

        // trace back
        Rectangle rect = new Rectangle();
        Diagonal temp = diagList.get(diagList.size()-1).predecessor;
        rect.maxScore = temp.pathValue;
        rect.lowerI = temp.i + temp.length;
        rect.lowerJ = temp.j + temp.length;
        while(temp.predecessor != null)
        {
        	if(temp.j-temp.i > rect.maxBand)		// find band's min & max boundaries
        		rect.maxBand = temp.j-temp.i;
        	if(temp.j-temp.i < rect.minBand)
        		rect.minBand = temp.j-temp.i;
            
        	temp = temp.predecessor;
        }
        
        if(temp.i>0)
        	rect.upperI = temp.i -1;
        else
        	rect.upperI = temp.i;
        if(temp.j>0)
        	rect.upperI = temp.j - 1;
        else
        	rect.upperI = temp.j;

        return rect;
    }



    public void buildDict()
    {
        dictionary = new HashMap<String,HashMap<Integer,List<Integer>>>();
        for(String s:db)
        {
            int dbNum = db.indexOf(s);
            dbLength = s.length()-ktup+1;
            for(int j=0;j<dbLength;j++)
            {
                dbStr = s.substring(j,j+ktup);
                if((curMap = dictionary.get(dbStr)) != null)
                {
                    if((curList = curMap.get(dbNum)) != null)
                        curList.add(j);
                    else
                    {
                        curList = new ArrayList<Integer>();
                        curList.add(j);
                        curMap.put(dbNum,curList);
                    }
                }
                else{
                    curList = new ArrayList<Integer>();
                    curList.add(j);
                    curMap = new HashMap<Integer,List<Integer>>();
                    curMap.put(dbNum,curList);
                    dictionary.put(dbStr,curMap);
                }
            }
        }
    }

    public void checkQuery(){
        queryLength = query.length();
        hotspots = new HashMap<Integer,HashMap<Integer,List<Diagonal>>>();    //The first key is the DB str No., the second key is the i-j values
        for(int i=0;i<queryLength-ktup+1;i++){    //i is the current index in the Query string
            curQuery = query.substring(i,i+ktup);     //get the current sliding window view of the query
            if((curMap = dictionary.get(curQuery)) != null)
            {                               //get the current HashMap containing all the DB matches to curQuery (ktup substring of query)
                Set<Integer> keys = curMap.keySet();
                if(!keys.isEmpty()){
                    for(Integer k:keys)    //iterate over DB Strings, k is the current db String number
                    {
                        if((values = curMap.get(k))!=null)
                        {
                            if((tempDiagMap = hotspots.get(k)) == null)
                                tempDiagMap = new HashMap<Integer,List<Diagonal>>();  //this will hold all the diagonals for db string k and the query
                            for(Integer j:values)           //j is the current index in the db String for the match to curQuery
                            {
                                curDiag = new Diagonal(i,j,ktup,ktup*2);
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
            for(Map.Entry<Integer,List<Diagonal>> entry:tempDiagMap.entrySet())   //iterate over each list of Diagonals that are all on the same ratio
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
            if(tempDiagListTotal.size() > maxDiags)
                tempDiagListTotal.subList(0,tempDiagListTotal.size()-maxDiags).clear();
            diags.put(k,tempDiagListTotal);
        }
        return diags;
    }

}