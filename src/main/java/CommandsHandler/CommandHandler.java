package CommandsHandler;

import Config.Config;
import InfoHandler.*;
import Quiz.Quiz;
import Dictionary.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CommandHandler extends ListenerAdapter {
    /**
     * users - HashMap mapping user Discord IDs to UserInfo objects
     */
    private final HashMap<Long, UserInfo> users = new HashMap<>();

    /**
     * Slash Command Event Handler
     * @param event slash command event
     * currently running commands:
     *              /info - gets info about this bot
     *              /dictionary - creates a dictionary
     *              /search - searches for a given keyword
     *              /quiz - starts a quiz
     *              /config - starts a config thing (personal settings)
     *              /help - pulls up a list of commands
     */
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        User author = event.getUser();
        UserInfo user;
        LinkedHashMap<String, String> files = InfoHandler.getFiles();
        Collection<String> filenames = files.values();
        if (users.containsKey(author.getIdLong())) {
            user = users.get(author.getIdLong());
        } else {
            user = new UserInfo(author);
            users.put(author.getIdLong(), user);
        }
        user.setInteraction(null);
            switch (commandName) {
            case "info"-> {
                EmbedBuilder eb = InfoHandler.botInfo();
                event.reply(" ").setEmbeds(eb.build()).queue();
            }
            case "dictionary" -> {
                boolean searchFilter = false;
                if (!event.getOptions().isEmpty()) {
                    OptionMapping optionMapping = event.getOptions().get(0);
                    searchFilter = optionMapping.getAsBoolean();
                    if (searchFilter && user.getWords().isEmpty()) {
                        event.reply("Do you expect me to create a dictionary out of nothing?").queue();
                        return;
                    }
                }
                LinkedHashMap<String, Word> words = new LinkedHashMap<>();
                if (searchFilter) {
                    words.putAll(user.getWords());
                } else {
                    words.putAll(InfoHandler.readFiles(filenames));
                }
                KanjiDictionary dictionary = new KanjiDictionary(words);
                EmbedBuilder eb = dictionary.createPage();
                List<ActionComponent> buttons = dictionary.createComponents();
                event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                user.setInteraction(dictionary);
            }
            case "search" -> {
                String[] command = event.getCommandString().split("\\s+");
                String key = command[command.length - 1];
                LinkedHashMap<String, Word> dictionary = InfoHandler.readFiles(filenames);
                LinkedHashMap<String, Word> results = new LinkedHashMap<>();
                if (dictionary.containsKey(key)) {
                    results.put(key, dictionary.get(key));
                }
                for (Word word: dictionary.values()) {
                    if (word.hasMeaning(key) || key.equals(word.getWord())) {
                        results.put(word.getWord(), word);
                    } else if (word instanceof Kanji) {
                        if (((Kanji) word).hasReading(key)) {
                            results.put(word.getWord(), word);
                        }
                    }
                }
                KanjiSearch search = new KanjiSearch(results, key);
                EmbedBuilder eb = search.createPage();
                List<ActionComponent> searchButtons = search.createComponents();
                event.reply(" ").setEmbeds(eb.build()).addActionRow(searchButtons).queue();
                user.setInteraction(search);
            }
            case "quiz" -> {
                if (user.getWords().isEmpty()) {
                    event.reply("Do you expect me to create a quiz out of nothing?").queue();
                    return;
                }
                Quiz quiz = new Quiz(user);
                EmbedBuilder eb = quiz.buildQuestion();
                if (quiz.isMultipleChoice()) {
                    List<ActionComponent> buttons = quiz.createComponents();
                    event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                } else {
                    event.reply(" ").setEmbeds(eb.build()).
                            addActionRow(Button.danger("x", "Exit")).queue();
                }
                user.setInteraction(quiz);
            }
            case "config" -> {
                Config config = new Config(user);
                EmbedBuilder eb = config.createPage();
                List<ActionComponent> dropdown = config.createComponents();
                event.reply("").setEmbeds(eb.build()).addActionRow(dropdown).
                setEphemeral(true).queue();
                user.setInteraction(config);
            }
            case "help" ->{
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Commands List:");
                eb.addField("Commands: ", """
                        /info - gets info about this bot
                        /dictionary (from-user-lists) - creates a dictionary based on either your enabled lists or all lists
                        /search [keyword] - searches for a given [keyword]
                        /quiz - starts a quiz
                        /config - starts a config thing (personal settings)
                        /help - pulls up a list of commands
                        """, false);
                eb.setColor(Color.YELLOW);
                event.reply("").setEmbeds(eb.build()).queue();
            }
        }
    }

    /**
     * Button Event Handler
     * @param event button interaction event
     */
    public void onButtonInteraction(ButtonInteractionEvent event) {
        long userID = event.getUser().getIdLong();
        String id = event.getButton().getId();
        Message message = event.getMessage();
        UserInfo userInfo = users.get(userID);
        assert id != null;
        if (!userInfo.isInInteraction()) return;
        BotInteraction interaction = userInfo.getInteraction();
        if (interaction instanceof Quiz) {
            List<Word> options = ((Quiz) interaction).getMcWordOptions();
            if (id.equals("x")) {
                event.reply("Exiting game (answered " + ((Quiz) interaction).getNumCorrect() + "/"
                        + ((Quiz) interaction).getCurrentQuestion() + " questions correctly)").queue();
                userInfo.setInteraction(null);
            } else {
                int choiceNum = Integer.parseInt(id);
                int numQuestions = ((Quiz) interaction).getNumQuestions();
                Word choice = options.get(choiceNum - 1);
                EmbedBuilder eb = ((Quiz) interaction).verifyCorrect(choice);
                message.editMessage(" ").setEmbeds(eb.build()).setComponents().queue();
                if (((Quiz) interaction).getCurrentQuestion() < numQuestions || numQuestions == 0) {
                    eb = ((Quiz) interaction).buildQuestion();
                    if (((Quiz) interaction).isMultipleChoice()) {
                        List<ActionComponent> buttons = interaction.createComponents();
                        event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                    } else {
                        event.reply(" ").setEmbeds(eb.build()).
                                addActionRow(Button.danger("x", "Exit")).queue();
                    }
                } else {
                    event.reply("Congratulations! You got " + ((Quiz) interaction).getNumCorrect() + "/" +
                            ((Quiz) interaction).getCurrentQuestion() + " correct").queue();
                    userInfo.setInteraction(null);
                }
            }
        }
        if (interaction instanceof KanjiDictionary) {
            if (id.equals("x")) {
                message.editMessage(" ").setComponents().queue();
                event.reply("Closing...").queue();
                userInfo.setInteraction(null);
            } else {
                int num = Integer.parseInt(id);
                EmbedBuilder eb = ((KanjiDictionary) interaction).turnPage(num);
                //List<ActionComponent> buttons = interaction.createComponents();
                event.editMessage(" ").setEmbeds(eb.build()).queue();//setActionRow(buttons).queue();
            }
        }
    }

    /**
     * String Select Interaction Event Handler
     * @param event string select interaction event
     */
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long userID = event.getUser().getIdLong();
        UserInfo userInfo = users.get(userID);
        BotInteraction interaction = userInfo.getInteraction();
        if (interaction instanceof Config) {
            // getLabel - gets selected option's label
            // getValue - gets selected option's value
            // getComponent.getId - gets the select menu's id
            String id = event.getInteraction().getSelectedOptions().get(0).getValue();
            if (id.equals("x")) {
                event.editMessage("Done!").setEmbeds().setComponents().queue();
                userInfo.setInteraction(null);
            } else {
                ((Config) interaction).interact(id);
                EmbedBuilder eb = ((Config) interaction).createPage();
                List<ActionComponent> selectMenu = interaction.createComponents();
                event.editMessage(" ").setEmbeds(eb.build()).setActionRow(selectMenu).queue();
            }
        }
    }

    /**
     * Activates when the bot loads up a server (upon startup)
     * @param event guild ready event
     */
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = initializeCommands();
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    /**
     * Activates when the bot joins a server (while the bot is active)
     * @param event guild join event
     */
    public void onGuildJoin(GuildJoinEvent event) {
        MessageChannel messageChannel = event.getGuild().getSystemChannel();
        EmbedBuilder eb = InfoHandler.botInfo();
        if (messageChannel != null) {
            messageChannel.sendMessage(" ").setEmbeds(eb.build()).queue();
        }
        List<CommandData> commandData = initializeCommands();
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    /**
     * Initializes commands
     * @return list of commands to initialize
     */
    public List<CommandData> initializeCommands() {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("info", "displays info about this bot"));
        commandData.add(Commands.slash("dictionary", "pulls up a dictionary")
                .addOption(OptionType.BOOLEAN, "from-user-lists",
                        "do you want to only view words from sets you have enabled?" +
                                " (True: yes; False or empty: No)"));
        commandData.add(Commands.slash("quiz", "starts a quiz")
        //        .addOption(OptionType.INTEGER, "num-questions", "number of questions " +
        //                "(0: Infinite)", false)
        //        .addOption(OptionType.BOOLEAN, "is-multiple-choice", "sets whether the questions" +
        //                " are multiple choice", false)
        );
        commandData.add(Commands.slash("config", "allows you to alter quiz configurations"));
        commandData.add(Commands.slash("search", "searches for a given word")
                .addOption(OptionType.STRING, "keyword", "word (in English, kana, " +
                        "or kanji) being searched for", true));
        commandData.add(Commands.slash("help", "gets a list of commands"));
        return commandData;
    }
}
