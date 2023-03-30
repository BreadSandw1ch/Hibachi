package Config;

import InfoHandler.UserInfo;
import net.dv8tion.jda.api.EmbedBuilder;

public class Config {
    private final UserInfo userInfo;
    private int pageNum;
    private int subPage;

    public Config(UserInfo userInfo) {
        this.userInfo = userInfo;
        pageNum = 0;
        subPage = 0;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public int getPageNum() {
        return pageNum;
    }

    private void setSubPage(int amount) {
        subPage += amount;
    }

    public EmbedBuilder createPage() {
        EmbedBuilder eb;
        switch (pageNum) {
            case 3 -> eb = numQuestionsPage();
            case 2 -> eb = questionStructurePage();
            case 1 -> eb = filePage();
            default -> eb = generalPage();
        }
        return eb;
    }

    public void setPageNum(int value) {
        pageNum = value;
    }

    private EmbedBuilder generalPage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Config");
        eb.setDescription("Settings to help configure what you study " +
                "and how (to some extent)");
        eb.addField(":one: - Scroll through and select/deselect available sets",
                "Includes a list of all available sets the bot can pull from",
                false);
        eb.addField(":two: - Alter (to some degree) how your questions are structured",
                "Includes options to select what you are given and what you are answering"
                , false);
        return eb;
    }

    private EmbedBuilder filePage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Files");
        return eb;
    }

    private EmbedBuilder questionStructurePage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Question Structure");
        return eb;
    }

    private EmbedBuilder numQuestionsPage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Number of Questions");
        return null;
    }
}
