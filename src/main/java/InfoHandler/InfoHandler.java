package InfoHandler;

import net.dv8tion.jda.api.EmbedBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class InfoHandler {

    private static final LinkedHashMap<String, String> files = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> defaultFiles = new LinkedHashMap<>();
    private static final String DEFAULT = "D";
    private static final String ENABLED = "E";
    private static final String DISABLED = "X";
    public static LinkedHashMap<String, Word> createWords(String filename) {
        LinkedHashMap<String, Word> dictionary = new LinkedHashMap<>();
        try (
                FileInputStream fis = new FileInputStream(filename);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr) {
        }) {
            String line = reader.readLine();
            while(line != null) {
                String[] fields = line.split("\\|");
                line = reader.readLine();
                if (fields.length < 2) continue;
                String word = fields[0];
                if(dictionary.containsKey(word)) {
                    updateWord(fields, dictionary);
                } else {
                    addWord(fields, dictionary);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dictionary;
    }

    public static void updateWord(String[] fields, LinkedHashMap<String, Word> dictionary) {
        String identifier = fields[0];
        HashSet<String> meanings = new HashSet<>(List.of(fields[1].split(",")));
        if (fields.length > 2) {
            HashSet<String> readings = new HashSet<>(List.of(fields[2].split(",")));
            Word kanji = dictionary.get(identifier);
            if (kanji instanceof Kanji) {
                ((Kanji) kanji).addReadings(readings);
                kanji.addMeanings(meanings);
            }
        } else {
            Word word = dictionary.get(identifier);
            word.addMeanings(meanings);
        }
    }

    public static void addWord(String[] fields, LinkedHashMap<String, Word> dictionary) {
        String identifier = fields[0];
        ArrayList<String> meanings = new ArrayList<>(List.of(fields[1].split(",")));
        Word word;
        if (fields.length > 2) {
            ArrayList<String> readings = new ArrayList<>(List.of(fields[2].split(",")));
            word = new Kanji(identifier, meanings, readings);
            dictionary.put(identifier, word);
        } else {
            word = new Word(identifier, meanings);
            dictionary.put(identifier, word);
        }
    }

    public static LinkedHashMap<String, Word> readFiles(Collection<String> filenames) {
        LinkedHashMap<String, Word> dictionary = new LinkedHashMap<>();
        for (String file:filenames) {
            String filename = "input/" + file + ".txt";
            dictionary.putAll(createWords(filename));
        }
        return dictionary;
    }

    public static void initializeFiles(String filename) {
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))){
            String line = reader.readLine();
            while(line != null) {
                String[] fields = line.split("\\|");
                String name = fields[0];
                String file = fields[1];
                String status = fields[2];
                if (!status.equals(DISABLED)) files.put(name, file);
                if (status.equals(DEFAULT)) defaultFiles.put(name, file);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedHashMap<String, String> getFiles() {
        return files;
    }
    public static LinkedHashMap<String, String> getDefaultFiles() {
        return defaultFiles;
    }

    public static EmbedBuilder botInfo() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Thanks for inviting me to this server!");
        eb.addField("Who? What? Where? When? Why? How?",
                """
                        Who: Hibachi
                        What: A bot specialized around Japanese kanji and kana
                        Where: Here
                        When: Since [REDACTED]
                        Why: My creator felt like it
                        How: I make quizzes and dictionaries
                        """, false);
        eb.setColor(0xeeac41);
        return eb;
    }

}
