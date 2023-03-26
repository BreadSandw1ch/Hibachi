package Quiz;

import InfoHandler.InfoHandler;
import InfoHandler.UserInfo;
import InfoHandler.Word;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Quiz {
    private final UserInfo user;
    private Word correct;
    private HashMap<String, Word> wordList;
    private static final int TERMINATING_CHECK = 1000;
    private ArrayList<Word> mcOptions = new ArrayList<>();
    private static final int NUM_QUESTIONS = 10;
    private int currentQuestion = 0;
    private int numCorrect = 0;
    private EmbedBuilder embedBuilder;

    public Quiz(UserInfo user) {
        this.user = user;
    }


    public UserInfo getUserInfo() {
        return user;
    }

    public Word getCorrect() {
        return correct;
    }

    public int getCurrentQuestion() {
        return currentQuestion;
    }

    private Word getAnswer() {
        for(int i = 0; i < TERMINATING_CHECK; i++) {
            Random random = new Random();
            int num = random.nextInt(wordList.size());
            Object[] words = wordList.values().toArray();
            Object chosen = words[num];
            if (chosen instanceof Word) {
                return (Word) chosen;
            }
        }
        return null;
    }

    public int getNumCorrect() {
        return numCorrect;
    }

    public ArrayList<Word> getMcOptions() {
        return mcOptions;
    }

    public void clearOptions() {
        mcOptions.clear();
    }

    public EmbedBuilder buildQuestion() {
        embedBuilder = new EmbedBuilder();
        wordList = InfoHandler.readFiles(user.files());
        correct = getAnswer();
        Random random = new Random();
        int correctChoice = random.nextInt(1,5);
        embedBuilder.setTitle("What means " + correct.getMeanings() + "?");
        String[] optionEmotes = new String[]{":one:", ":two:", ":three:", ":four:"};
        int i = 0;
        while(i < optionEmotes.length) {
            Word word = getAnswer();
            if (word == null || mcOptions.size() >= wordList.size()) break;
            if (mcOptions.contains(word) || Objects.equals(word, correct)) continue;
            if (i+1 == correctChoice) {
                embedBuilder.addField(optionEmotes[i] + " - **" + correct.getWord() + "**", " ", false);
                mcOptions.add(correct);
                i++;
                if (i >= optionEmotes.length) break;
            }
            embedBuilder.addField(optionEmotes[i] + " - **" + word.getWord() + "**", " ", false);
            mcOptions.add(word);
            i++;
        }
        embedBuilder.setColor(Color.YELLOW);
        return embedBuilder;
    }

    public EmbedBuilder verifyCorrect(Word word) {
        if (Objects.equals(word, correct)) {
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setFooter("Correct!");
            numCorrect += 1;
        } else {
            embedBuilder.setColor(Color.RED);
            embedBuilder.setFooter("Incorrect. Answer: " + correct.getWord());
        }
        currentQuestion += 1;
        mcOptions = new ArrayList<>();
        return embedBuilder;
    }


}
