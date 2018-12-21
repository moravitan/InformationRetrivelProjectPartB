package Model;

public class testMor {

    public static void main(String[] args) {
        testEngine();

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
}
