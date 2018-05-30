package by.nicolay.lipnevich.noxbox.tools;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by nicolay.lipnevich on 18/12/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class SiteI18nizer {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("functions/translations/template.html");
        String content = new String(Files.readAllBytes(path), UTF_8);

        File[] translations = new File("functions/translations").listFiles();
        Map<String, String> languages = getLanguages(translations);
        for (File translation : translations) {
            if (translation.isFile() && !translation.getName().startsWith("template")) {
                translate(content, translation, languages);
            }
        }
    }

    private static Map<String, String> getLanguages(File[] files) throws IOException {
        Map<String, String> languages = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String first, String second) {
                if(first.equals(second)) return 0;
                if(first.equals("ru")) return 1;
                if(second.equals("ru")) return -1;

                return first.compareTo(second);
            }
        });
        for(File file : files) {
            if (file.isFile() && !file.getName().startsWith("template")) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(file));
                languages.put(file.getName().split("\\.")[0], properties.getProperty("language_desc"));
            }
        }
        return languages;
    }

    private static void translate(String content, File file, Map<String, String> languages) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));

        List<Map.Entry <String, String>> sorted = new ArrayList(properties.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry <String, String>>() {
            @Override
            public int compare(Map.Entry <String, String> o, Map.Entry <String, String> o1) {
                return o1.getKey().length() - o.getKey().length();
            }
        });

        for(Map.Entry entry : sorted) {
            content = content.replaceAll("\\$" + entry.getKey(), entry.getValue().toString());
        }

        String name = file.getName().split("\\.")[0];
        if(name.equals("en")) {
            name = "index";
        }

        Path translatedFile = Paths.get("functions/site/" + name + ".html");
        Files.write(translatedFile, content.getBytes(UTF_8));
    }

}
