package Model;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class testMor {

    public static void main(String[] args) {
        TreeMap<Integer,String> topFiveEntities = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        topFiveEntities.put(1,"sfs");
        topFiveEntities.put(4,"sfs");
        topFiveEntities.put(3,"sfs");


        for (Map.Entry<Integer,String> entry: topFiveEntities.entrySet()){
            if (entry.getKey() == 3)
                topFiveEntities.remove(3);

        }
        for (Map.Entry<Integer,String> entry: topFiveEntities.entrySet()){
            System.out.println(entry.getKey() + "," + entry.getValue());

        }
    }
}
