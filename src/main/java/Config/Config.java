package Config;

import InfoHandler.InfoHandler;
import InfoHandler.UserInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {
    private final UserInfo userInfo;
    private int pageNum;
    private int subPage;

    private final List<Option> options = new ArrayList<>();
    private static final String EXIT = "x";
    private static final String BACK = "/";
    private static final String FORWARD = ">";
    private static final String BACKWARD = "<";
    private static final String[] PAGES = new String[]{"General", "Files", "Question Structure"};

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
        options.clear();
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
        if (pageNum != 0) {
            options.add(new Option(FORWARD, FORWARD));
            options.add(new Option(BACKWARD, BACKWARD));
            options.add(new Option("Back", BACK));
        }
        options.add(new Option("X - Exit Menu", EXIT));
        StringSelectMenu.Builder selectMenu = StringSelectMenu.create(String.valueOf(pageNum)).setPlaceholder(title)
                .setRequiredRange(1,1);
        for (Option option:options) {
            selectMenu.addOption(option.title(), option.description());
        }
        return selectMenu.build();
    }

    public void interact(int pageNum, String id) {
        if (id.equals(EXIT)) return;
        switch (pageNum) {
            case 0 -> {
                int num = Integer.parseInt(id);
                setPageNum(num);
            }
            case 1 -> {
                switch (id) {
                    case BACK -> setPageNum(0);
                    case FORWARD -> setSubPage(1);
                    case BACKWARD -> setSubPage(-1);
                    default -> editFileConfig(id);
                }
            }
        }
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
        for (int i = 1; i < PAGES.length; i++) {
            options.add(new Option(PAGES[i], String.valueOf(i)));
        }
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
        eb.setTitle("Files");
        HashMap<String, String> files = InfoHandler.getFiles();
        ArrayList<String> fileNames = new ArrayList<>(files.keySet());
        int numResults = 0;
        int maxPages = (int) Math.ceil(fileNames.size()/10.0);
        for (int i = 0; i < 10 && 10 * subPage + i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            String enabled = " - Disabled";
            if (userInfo.hasFile(files.get(fileName))) {
                enabled = " - Enabled";
            }
            eb.addField(fileName + enabled, files.get(fileName), false);
            options.add(new Option(fileName, files.get(fileName)));
            numResults += 1;
        }
        eb.setFooter("Page " + (subPage + 1) + "/" + maxPages + " | Showing Results " +
                (subPage * 10 + 1) + "-" + (subPage * 10 + numResults) + " out of " +
                fileNames.size());
        eb.setColor(Color.YELLOW);

        return eb;
    }

    public List<Option> getFileOptions() {
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
        return eb;
    }
}
