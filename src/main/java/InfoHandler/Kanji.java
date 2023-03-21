package InfoHandler;

import java.util.HashSet;

public record Kanji(String kanji, HashSet<String> readings, HashSet<String> meanings) {

    public void addReading(String reading) {
        readings.add(reading);
    }

    public void removeReading(String reading) {
        readings.remove(reading);
    }

    public boolean hasReading(String reading) {
        return readings.contains(reading);
    }

    public void addMeaning(String meaning) {
        meanings.add(meaning);
    }

    public void removeMeaning(String meaning) {
        meanings.remove(meaning);
    }

    public boolean hasMeaning(String meaning) {
        return meanings.contains(meaning);
    }

}
