package Config;

import InfoHandler.InfoHandler;
import InfoHandler.UserInfo;
import InfoHandler.BotInteraction;
import InfoHandler.QuestionTypes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config implements BotInteraction {
    /** Variables and their meanings:
     * userInfo - Instance of UserInfo that represents the user
     * pageNum - current section number the config is on (0 meaning genreal, 1 meaning file page, etc.)
     * subPage - current page within a section of the config
     * options - list of options within the dropdown menu
     */
    private final UserInfo userInfo;
    private int pageNum;
    private int subPage;
    private final List<Option> options = new ArrayList<>();
    private int maxPages;

    /** Constants and their meanings:
     * EXIT - String representing exit button id
     * BACK - String representing back button id (back to main page)
     * FORWARD - String representing forward button id
     * BACKWARD - String representing backward button id (back a subpage)
     */
    private static final String EXIT = "x";
    private static final String BACK = "/";
    private static final String FORWARD = ">";
    private static final String BACKWARD = "<";
    private static final String[] PAGES = new String[]{"General", "Files", "Question Structure"};

    /**
     * Constructor for Config
     * @param userInfo represents the user of this config
     */
    public Config(UserInfo userInfo) {
        this.userInfo = userInfo;
        pageNum = 0;
        subPage = 0;
    }

    /**
     * alters the subpage by a given amount and loops back to zero or back to the maximum value as necessary
     * @param amount amount by which subpage count changes
     */
    private void setSubPage(int amount) {
        subPage += amount;
        if (subPage >= maxPages) {
            subPage = 0;
        }
        if (subPage < 0) {
            subPage = maxPages - 1;
        }
    }

    /**
     * Creates a page of the config based on section number
     * @return embed representing the above
     */
    public EmbedBuilder createPage() {
        EmbedBuilder eb;
        options.clear();
        maxPages = 0;
        switch (pageNum) {
            case 3 -> eb = numQuestionsPage();
            case 2 -> eb = questionStructurePage();
            case 1 -> eb = filePage();
            default -> {
                eb = generalPage();
                subPage = 0;
            }
        }
        return eb;
    }

    /**
     * Creates the action components for the interaction
     * @return a list representing the above information
     */
    @Override
    public List<ActionComponent> createComponents() {
        String title;
        switch (pageNum) {
            case 3 -> title = "Other Config Settings";
            case 2 -> title = "Choose a question-answer order";
            case 1 -> title = "Choose a file";
            default -> title = "Choose an option";
        }

        if (maxPages > 1) {
            options.add(new Option(FORWARD, FORWARD));
            options.add(new Option(BACKWARD, BACKWARD));
        }
        if (pageNum > 0) options.add(new Option("Back", BACK));

        options.add(new Option("X - Exit Menu", EXIT));
        StringSelectMenu.Builder selectMenu = StringSelectMenu.create(String.valueOf(pageNum)).setPlaceholder(title)
                .setRequiredRange(1,1);
        for (Option option:options) {
            selectMenu.addOption(option.title(), option.description());
        }
        List<ActionComponent> interactions = new ArrayList<>();
        interactions.add(selectMenu.build());
        return interactions;
    }

    /**
     * General handler for config interactions
     * @param id id of the interaction
     */
    public void interact(String id) {
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
            case 2 -> {
                switch (id) {
                    case BACK -> setPageNum(0);
                    case FORWARD -> setSubPage(1);
                    case BACKWARD -> setSubPage(-1);
                    case "KANJI" -> userInfo.setQuestionType(QuestionTypes.KANJI, subPage);
                    case "READINGS" -> userInfo.setQuestionType(QuestionTypes.READINGS, subPage);
                    case "MEANINGS" -> userInfo.setQuestionType(QuestionTypes.MEANINGS, subPage);
                    default -> userInfo.setQuestionType(null, subPage);
                }
            }
        }
    }

    /**
     * Sets the section (main page) number
     * @param value value of the page
     */
    public void setPageNum(int value) {
        pageNum = value;
    }

    /**
     * Creates an embed for the general page
     * @return the above embed
     */
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

    /**
     * Edits the file config for the user
     * @param file the file being added or removed
     */
    public void editFileConfig(String file) {
        if (userInfo.hasFile(file)) {
            userInfo.removeFile(file);
        } else {
            userInfo.addFile(file);
        }
    }

    /**
     * Creates an embed for the file page
     * @return the above
     */
    private EmbedBuilder filePage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Files");
        HashMap<String, String> files = InfoHandler.getFiles();
        ArrayList<String> fileNames = new ArrayList<>(files.keySet());
        int numResults = 0;
        maxPages = (int) Math.ceil(fileNames.size()/10.0);
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

    /**
     * Creates an embed for the question structure
     * @return the above
     */
    private EmbedBuilder questionStructurePage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Question Structure");
        QuestionTypes[] questionTypes = userInfo.getQuestionType();
        maxPages = 2;
        eb.addField("Current Order (Question --> Answer):",
                questionTypes[0] + " --> " + questionTypes[1], false);
        if (subPage == 0) {
            eb.setDescription("Currently setting question");
        } else {
            eb.setDescription("Currently setting answer");
        }
        for (QuestionTypes questionType:QuestionTypes.values()) {
            options.add(new Option(String.valueOf(questionType), String.valueOf(questionType)));
        }
        // if (subPage >= 2) options.add(new Option("None", "None"));
        return eb;
    }

    /**
     * Creates an embed for the number of questions
     * @return the above
     */
    private EmbedBuilder numQuestionsPage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Number of Questions");
        return eb;
    }
}
