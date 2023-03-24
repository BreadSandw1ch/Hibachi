package Quiz;

import InfoHandler.UserInfo;
import net.dv8tion.jda.api.EmbedBuilder;

public class Question {
    private final UserInfo user;
    public Question(UserInfo user) {
        this.user = user;
    }

    public UserInfo getUserInfo() {
        return user;
    }

    public EmbedBuilder buildQuestion() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("What does ");
        return eb;
    }

}
