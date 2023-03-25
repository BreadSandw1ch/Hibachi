package Quiz;

import InfoHandler.InfoHandler;
import InfoHandler.UserInfo;
import InfoHandler.Word;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Quiz {
    private final UserInfo user;
    private Word correct;
    private HashMap<String, Word> wordList;
    private static final int TERMINATING_CHECK = 1000;
    private final ArrayList<Word> mcOptions = new ArrayList<>();
    private static final int NUM_CHOICES = 4;
    public Quiz(UserInfo user) {
        this.user = user;
    }


    public UserInfo getUserInfo() {
        return user;
    }

    public Word getCorrect() {
        return correct;
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

    public ArrayList<Word> getMcOptions() {
        return mcOptions;
    }

    public void clearOptions() {
        mcOptions.clear();
    }

    public EmbedBuilder buildQuestion() {
        EmbedBuilder eb = new EmbedBuilder();
        wordList = InfoHandler.readFiles(user.files());
        correct = getAnswer();
        mcOptions.add(correct);
        Random random = new Random();
        int correctChoice = random.nextInt(1,5);
        eb.setTitle("Currently a mock-up for what is to come:");
        String[] optionEmotes = new String[]{":one:", ":two:", ":three:", ":four:"};
        int i = 0;
        while(i < optionEmotes.length) {
            Word word = getAnswer();
            if (word == null || mcOptions.size() >= wordList.size()) break;
            if (mcOptions.contains(word)) continue;
            mcOptions.add(word);
            if (i+1 == correctChoice) {
                eb.addField(optionEmotes[i] + " - **" + correct.getWord() + "**", " ", false);
                i++;
                if (i >= optionEmotes.length) break;
            }
            eb.addField(optionEmotes[i] + " - **" + word.getWord() + "**", " ", false);
            i++;
        }

        return eb;
    }

    public boolean verifyCorrect(Word word) {
        return Objects.equals(word, correct);
    }


}
