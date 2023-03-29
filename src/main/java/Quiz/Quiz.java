package Quiz;

import InfoHandler.*;
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
    private final ArrayList<Word> mcOptions = new ArrayList<>();
    private final int numQuestions;
    private int currentQuestion = 0;
    private int numCorrect = 0;
    private EmbedBuilder embedBuilder;
    private final QuestionTypes[] questionTypes;
    private final boolean isMultipleChoice;

    public Quiz(UserInfo user) {
        this.user = user;
        numQuestions = user.getNumQuestions();
        questionTypes = user.getQuestionType();
        isMultipleChoice = user.isMultipleChoice();
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

    public String getAnswerDisplay(int value, Word word) {
        String display = null;
        switch (questionTypes[value]) {
            case MEANINGS -> display = word.getMeanings().toString();
            case KANJI -> display = word.getWord();
            case READINGS -> {
                if (word instanceof Kanji) {
                    display = ((Kanji) word).getReadings().toString();
                } else {
                    display = word.getWord();
                }
            }
        }
        return display;
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
        wordList = InfoHandler.readFiles(user.getFiles());
        correct = getAnswer();
        Random random = new Random();
        int correctChoice = random.nextInt(1,5);
        String question = getAnswerDisplay(0, correct);
        embedBuilder.setTitle("What means " + question + "?");
        String[] optionEmotes = new String[]{":one:", ":two:", ":three:", ":four:"};
        int i = 0;
        while(i < optionEmotes.length) {
            Word word = getAnswer();
            String answer = null;
            if (word == null || mcOptions.size() >= wordList.size()) break;
            if (mcOptions.contains(word) || Objects.equals(word, correct)) continue;
            if (i+1 == correctChoice) {
                answer = getAnswerDisplay(1, correct);
                embedBuilder.addField(optionEmotes[i] + " - **" + answer + "**", " ", false);
                mcOptions.add(correct);
                i++;
                if (i >= optionEmotes.length) break;
            }
            answer = getAnswerDisplay(1, word);
            embedBuilder.addField(optionEmotes[i] + " - **" + answer + "**", " ", false);
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
        clearOptions();
        return embedBuilder;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public QuestionTypes[] getQuestionTypes() {
        return questionTypes;
    }

    public boolean isMultipleChoice() {
        return isMultipleChoice;
    }
}
