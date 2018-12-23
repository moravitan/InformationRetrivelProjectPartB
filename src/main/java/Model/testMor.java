package Model;

import java.util.HashMap;

public class testMor {

    public static void main(String[] args) {
        String line = "sdfsdfds,12";
        String docId = line.substring(0,line.indexOf(","));
        line = line.substring(line.indexOf(","));
        int docLength = Integer.valueOf(line.substring(line.indexOf(",") + 1));
        System.out.println(docLength);

    }

    public static void test(HashMap<String,Integer> hashMap){
        hashMap.put("aa",2);

    }

}
