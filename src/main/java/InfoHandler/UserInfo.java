package InfoHandler;

import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class UserInfo {
    private final User user;
    private final Collection<String> files;
    private HashMap<String, Word> words;
    private int numQuestions;
    private boolean isMultipleChoice;
    private final QuestionTypes[] questionType;
    BotInteraction interaction;

    public UserInfo(User user) {
        this.user = user;
        HashMap<String, String> fileMap = InfoHandler.getDefaultFiles();
        files = new ArrayList<>();
        files.addAll(fileMap.values());
        words = new HashMap<>();
        numQuestions = 10;
        isMultipleChoice = true;
        questionType = new QuestionTypes[] {QuestionTypes.KANJI, QuestionTypes.MEANINGS, null};
    }

    public User user() {
        return user;
    }

    public void setInteraction(BotInteraction botInteraction) {
        interaction = botInteraction;
    }

    public BotInteraction getInteraction() {
        return interaction;
    }

    public boolean isInInteraction() {
        return interaction != null;
    }

    public QuestionTypes[] getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionTypes question, int value) {
        if (questionType[value] == question) return;
        for (int i = 0; i < questionType.length; i++) {
            if (questionType[i] == question) {
                questionType[i] = questionType[value];
                break;
            }
        }
        questionType[value] = question;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public void setNumQuestion(int num) {
        numQuestions = num;
    }

    public boolean isMultipleChoice() {
        return isMultipleChoice;
    }

    public void setMultipleChoice(boolean value) {
        isMultipleChoice = value;
    }

    public Collection<String> getFiles() {
        return files;
    }

    public HashMap<String, Word> getWords() {
        words = InfoHandler.readFiles(files);
        return words;
    }

    public boolean hasFile(String file) {
        return files.contains(file);
    }

    public void addFile(String file) {
        files.add(file);
    }

    public void removeFile(String file) {
        files.remove(file);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UserInfo) obj;
        return Objects.equals(this.user, that.user) &&
                Objects.equals(this.files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, files);
    }

    @Override
    public String toString() {
        return "UserInfo[" +
                "user=" + user + ", " +
                "files=" + files + ']';
    }


}
