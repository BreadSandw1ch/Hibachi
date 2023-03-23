package InfoHandler;

import java.util.HashSet;
import java.util.Objects;

public class Kanji extends Word {
    private final HashSet<String> readings;
    public Kanji(String kanji, HashSet<String> meanings, HashSet<String> readings) {
        super(kanji, meanings);
        this.readings = readings;
    }

    public void addReadings(HashSet<String> reading) {
        readings.addAll(reading);
    }

    public void removeReadings(HashSet<String> reading) {
        readings.removeAll(reading);
    }

    public boolean hasReading(String reading) {
        return readings.contains(reading);
    }

    public HashSet<String> readings() {
        return readings;
    }

    @Override
    public boolean equals(Object obj) {
        boolean value = super.equals(obj);
        if (value && obj.getClass() == this.getClass()) {
            value = Objects.equals(this.getMeanings(), ((Kanji) obj).getMeanings());
        }
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.getWord(), readings, super.getMeanings());
    }

    @Override
    public String toString() {
        return super.toString() + "; readings:" + readings;
    }
}
