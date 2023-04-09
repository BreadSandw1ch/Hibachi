package Dictionary;

import InfoHandler.Word;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.HashMap;

public class KanjiSearch extends KanjiDictionary{
    private final String search;
    public KanjiSearch(HashMap<String, Word> wordList, String search) {
        super(wordList);
        this.search = search;
    }

    @Override
    public EmbedBuilder createPage() {
        EmbedBuilder eb = super.createPage();
        if (getWordList().size() == 0) {
            eb.setTitle("Error: Cannot find " + search + "!");
            eb.setColor(Color.RED);
            eb.setFooter("Please try searching for a different word or let us know something is missing");
        } else {
            eb.setTitle("Search Results: " + search);
        }
        return eb;
    }


}
