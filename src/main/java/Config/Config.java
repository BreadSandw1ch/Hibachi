package Config;

import InfoHandler.InfoHandler;
import InfoHandler.UserInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {
    private final UserInfo userInfo;
    private int pageNum;
    private int subPage;

    private final List<String> options = new ArrayList<>();

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

    public ActionComponent createConfigDropdown() {
        String title;
        switch (pageNum) {
            case 3 -> title = "Other Config Settings";
            case 2 -> title = "Choose a question-answer order";
            case 1 -> title = "Choose a file";
            default -> title = "Choose an option";
        }
        options.add("test");
        options.add("test2");
        StringSelectMenu.Builder selectMenu = StringSelectMenu.create(String.valueOf(pageNum)).setPlaceholder(title)
                .setRequiredRange(1,1);
        for (String option:options) {
            selectMenu.addOption(option, option);
        }
        return selectMenu.build();
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

    public void editFileConfig(String file) {
        if (userInfo.hasFile(file)) {
            userInfo.removeFile(file);
        } else {
            userInfo.addFile(file);
        }
    }

    private EmbedBuilder filePage() {
        EmbedBuilder eb = new EmbedBuilder();
        options.clear();
        eb.setTitle("Files");
        HashMap<String, String> files = InfoHandler.getFiles();
        ArrayList<String> fileNames = new ArrayList<>(files.keySet());
        for (int i = 0; i < 10 || 10 * subPage + i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            eb.addField(fileName, files.get(fileName), false);
            options.add(fileName);
        }
        return eb;
    }

    public List<String> getFileOptions() {
        return options;
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
