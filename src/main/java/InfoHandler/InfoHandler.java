package InfoHandler;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class InfoHandler {

    public HashMap<String, Word> createWords(String filename) {
        HashMap<String, Word> dictionary = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename)) {
        }) {
            String line = reader.readLine();
            while(line != null) {
                String[] fields = line.split("\\|");
                if (fields.length >= 2) {
                    Word word;
                    String identifier = fields[0];
                    HashSet<String> readings = new HashSet<>(List.of(fields[1].split(",")));
                    if (fields.length >= 3) {
                        HashSet<String> meanings = new HashSet<>(List.of(fields[2].split(",")));
                        if (dictionary.containsKey(identifier)) {
                            Word kanji = dictionary.get(identifier);
                            if (kanji instanceof Kanji) {
                                ((Kanji) kanji).addReadings(readings);
                                kanji.addMeanings(meanings);
                            }
                        } else {
                            word = new Kanji(identifier, readings, meanings);
                            dictionary.put(identifier, word);
                        }
                    } else {
                        if (dictionary.containsKey(identifier)) {
                            Word kanji = dictionary.get(identifier);
                            kanji.addMeanings(readings);
                        } else {
                            word = new Word(identifier, readings);
                            dictionary.put(identifier, word);
                        }
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dictionary;
    }

    public HashMap<String, Word> readFiles(List<String> filenames) {
        HashMap<String, Word> dictionary = new HashMap<>();
        for (String filename:filenames) {
            dictionary.putAll(createWords(filename));
        }
        return dictionary;
    }


}
