package Quiz;

import InfoHandler.*;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Quiz {
    private final UserInfo user;
    private Word correct;
    private HashMap<String, Word> wordList;
    private static final int TERMINATING_CHECK = 1000;
    private final ArrayList<Word> mcWordOptions = new ArrayList<>();
    private final ArrayList<String> mcButtonLabels = new ArrayList<>();
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
        Random random = new Random();
        String display = null;
        switch (questionTypes[value]) {
            case MEANINGS -> {
                List<String> meanings = new ArrayList<>(word.getMeanings());
                int randInt = random.nextInt(meanings.size());
                display = meanings.get(randInt);
            }
            case KANJI -> display = word.getWord();
            case READINGS -> {
                if (word instanceof Kanji) {
                    List<String> readings = new ArrayList<>(((Kanji) word).getReadings());
                    int randInt = random.nextInt(readings.size());
                    display = readings.get(randInt);
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

    public ArrayList<Word> getMcWordOptions() {
        return mcWordOptions;
    }

    public void clearOptions() {
        mcWordOptions.clear();
        mcButtonLabels.clear();
    }

    public EmbedBuilder buildQuestion() {
        embedBuilder = new EmbedBuilder();
        wordList = user.getWords();
        correct = getAnswer();
        Random random = new Random();
        int correctChoice = random.nextInt(1,5);
        String question = getAnswerDisplay(0, correct);
        if (questionTypes[0] == QuestionTypes.MEANINGS) embedBuilder.setTitle("What means \"" + question + "\"?");
        else {
            embedBuilder.setTitle("What does " + question + " mean?");
        }
        String[] optionEmotes = new String[]{":one:", ":two:", ":three:", ":four:"};
        int i = 0;
        while(i < optionEmotes.length) {
            Word word = getAnswer();
            String answer;
            if (word == null || mcWordOptions.size() >= wordList.size()) break;
            if (mcWordOptions.contains(word) || Objects.equals(word, correct)) continue;
            if (i+1 == correctChoice) {
                answer = getAnswerDisplay(1, correct);
                embedBuilder.addField(optionEmotes[i] + " - **" + answer + "**", " ", false);
                mcWordOptions.add(correct);
                mcButtonLabels.add(answer);
                i++;
                if (i >= optionEmotes.length) break;
            }
            answer = getAnswerDisplay(1, word);
            embedBuilder.addField(optionEmotes[i] + " - **" + answer + "**", " ", false);
            mcWordOptions.add(word);
            mcButtonLabels.add(answer);
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
            int answer = mcWordOptions.indexOf(correct);
            String buttonLabel = mcButtonLabels.get(answer);
            embedBuilder.setFooter("Incorrect. Answer: " + buttonLabel);
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

    public ArrayList<String> getMcButtonLabels() {
        return mcButtonLabels;
    }
}
