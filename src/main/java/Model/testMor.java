package Model;

import java.io.*;

public class testMor {

    public static void main(String[] args) {
        testEngine();
//        splitQruels();
    }

    public static void testEngine(){
        String pathToParse = "C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\corpus\\corpus";
        String pathToSaveIndex = "C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\indexer";
        Engine engine = new Engine();
        engine.setParameters(pathToParse,pathToSaveIndex,false);
        try {
            engine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void splitQruels(){
        try {
            BufferedReader bf = new BufferedReader(new FileReader("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\qrels.txt"));
            String line = bf.readLine();
            String fileName = "";
            String[] split = line.split(" ");
            fileName = split[0];
            FileWriter writer = new FileWriter(new File("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\" + fileName + ".txt"));
            while (line != null) {
                if (split[3].equals("1"))
                    writer.write(split[2] + "\n");
                line = bf.readLine();
                if (line == null) break;
                split = line.split(" ");
                if (!split[0].equals(fileName)){
                    fileName = split[0];
                    writer.flush();
                    writer.close();
                    writer = new FileWriter(new File("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\" + fileName + ".txt"));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
