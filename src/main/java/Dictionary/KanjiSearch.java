package Dictionary;

import InfoHandler.Word;
import net.dv8tion.jda.api.EmbedBuilder;

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

    @Override
    public EmbedBuilder createPage() {
        EmbedBuilder eb = super.createPage();
        eb.setTitle("Search Results: " + search);
        return eb;
    }


}
