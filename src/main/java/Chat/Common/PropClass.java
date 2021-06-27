package Chat.Common;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropClass {

   public static String properties(String key) {
        File file = new File("src/main/resources/config.properties");
        Properties proper = new Properties();
        try {
            proper.load(new FileReader(file));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return (String) proper.get(key);
    }

}
