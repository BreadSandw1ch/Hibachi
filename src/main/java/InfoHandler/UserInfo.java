package InfoHandler;

import net.dv8tion.jda.api.entities.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class UserInfo {
    private final User user;
    private final Collection<String> files;
    private HashMap<String, Word> words;

    public UserInfo(User user) {
        this.user = user;
        HashMap<String, String> fileMap = InfoHandler.getFiles();
        files = fileMap.values();
        words = new HashMap<>();
    }

    public User user() {
        return user;
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
