package InfoHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Word {
    private final String word;
    private final ArrayList<String> meanings;

    public Word(String kanji, ArrayList<String> meanings) {
        this.word = kanji;
        this.meanings = meanings;
    }

    public void addMeanings(HashSet<String> meaning) {
        meanings.addAll(meaning);
    }

    public boolean hasMeaning(String meaning) {
        for (String wordMeaning:meanings) {
            if (meaning.equalsIgnoreCase(wordMeaning)) return true;
        }
        return false;
    }

    public String getWord() {
        return word;
    }

    public ArrayList<String> getMeanings() {
        return meanings;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Word) obj;
        return Objects.equals(this.word, that.word) &&
                Objects.equals(this.meanings, that.meanings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, meanings);
    }

    @Override
    public String toString() {
        return word + "; meanings:" + meanings;
    }


}
