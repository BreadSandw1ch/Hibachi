package Dictionary;

import InfoHandler.Word;

import java.util.HashMap;

public class KanjiSearch extends KanjiDictionary{
    private final String search;
    public KanjiSearch(HashMap<String, Word> wordList, String search) {
        super(wordList);
        this.search = search;
    }

    public String getSearch() {
        return search;
    }


}
