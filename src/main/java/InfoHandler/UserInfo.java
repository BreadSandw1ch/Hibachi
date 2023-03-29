package InfoHandler;

import net.dv8tion.jda.api.entities.User;

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

    public UserInfo(User user) {
        this.user = user;
        HashMap<String, String> fileMap = InfoHandler.getFiles();
        files = fileMap.values();
        words = new HashMap<>();
        numQuestions = 10;
        isMultipleChoice = true;
        questionType = new QuestionTypes[] {QuestionTypes.KANJI, QuestionTypes.MEANINGS, null};
    }

    public User user() {
        return user;
    }

    public QuestionTypes[] getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionTypes question, int value) {
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
