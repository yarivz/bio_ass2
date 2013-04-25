import java.util.Comparator;

public class DiagComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        return ((Diagonal)o1).score - ((Diagonal)o2).score;

    }

    @Override
    public boolean equals(Object obj) {
        return  false;
    }
}
