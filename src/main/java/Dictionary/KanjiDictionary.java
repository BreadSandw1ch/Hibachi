package Dictionary;

import InfoHandler.BotInteraction;
import InfoHandler.Word;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KanjiDictionary implements BotInteraction {
    private final ArrayList<Word> wordList;
    private final int maxPages;
    private int currentPage = 0;
    public KanjiDictionary(HashMap<String, Word> wordMap) {
        wordList = new ArrayList<>();
        wordList.addAll(wordMap.values());
        maxPages = (int) Math.ceil(wordList.size()/10.0);
    }

    public ArrayList<Word> getWordList() {
        return wordList;
    }

    public EmbedBuilder createPage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Dictionary");
        int numResults = 0;
        for (int i = 0; i < 10 && currentPage * 10 + i < wordList.size(); i++) {
            Word word = wordList.get(currentPage * 10 + i);
            eb.addField(word.getWord(), word.toString(), false);
            numResults += 1;
        }
        eb.setFooter("Page " + (currentPage + 1) + "/" + maxPages + " | Showing Results " +
                (currentPage * 10 + 1) + "-" + (currentPage * 10 + numResults) + " out of " +
                wordList.size());
        eb.setColor(Color.YELLOW);
        return eb;
    }

    public EmbedBuilder turnPage(int amount) {
        currentPage += amount;
        if (currentPage >= maxPages) currentPage = 0;
        if (currentPage < 0) currentPage = maxPages-1;
        return createPage();
    }

    /**
     * Creates buttons for the dictionary
     * @return list of buttons from the dictionary
     */
    @Override
    public List<ActionComponent> createComponents() {
        List<ActionComponent> buttons = new ArrayList<>();
        buttons.add(Button.secondary("-1", "<"));
        buttons.add(Button.secondary("1", ">"));
        buttons.add(Button.danger("x", "Exit"));
        return buttons;
    }
}
