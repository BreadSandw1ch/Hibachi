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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.*;

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
        HashMap<String, String> files = InfoHandler.getFiles();
        Collection<String> filenames = files.values();
        if (users.containsKey(author.getIdLong())) {
            user = users.get(author.getIdLong());
        } else {
            user = new UserInfo(author);
            users.put(author.getIdLong(), user);
        }


            switch (commandName) {
            case "info"-> {
                EmbedBuilder eb = InfoHandler.botInfo();
                event.reply(" ").setEmbeds(eb.build()).queue();
            }

            case "dictionary" -> {
                HashMap<String, Word> words = InfoHandler.readFiles(filenames);
                KanjiDictionary dictionary = new KanjiDictionary(words);
                EmbedBuilder eb = dictionary.createPage();
                List<ActionComponent> buttons = dictionary.createComponents();
                event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                user.setInteraction(dictionary);
            }
            case "search" -> {
                String[] command = event.getCommandString().split("\\s+");
                String key = command[command.length - 1];
                HashMap<String, Word> dictionary = user.getWords();
                HashMap<String, Word> results = new HashMap<>();
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
            case "help" ->
                event.reply("test").queue();
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
                message.editMessage(" ").setEmbeds(eb.build()).queue();
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
                event.reply("Closing...").queue();
                userInfo.setInteraction(null);
            } else {
                int num = Integer.parseInt(id);
                EmbedBuilder eb = ((KanjiDictionary) interaction).turnPage(num);
                List<ActionComponent> buttons = interaction.createComponents();
                event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                message.delete().queue();
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
                event.editMessage("Done!").setEmbeds().queue();
                userInfo.setInteraction(null);
            } else {
                int pageNum = Integer.parseInt(event.getComponentId());
                ((Config) interaction).interact(pageNum, id);
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
                .addOption(OptionType.BOOLEAN, "user-lists",
                        "do you want to only view words from sets you have enabled?"));
        commandData.add(Commands.slash("quiz", "starts a quiz"));
        commandData.add(Commands.slash("config", "allows you to alter quiz configurations"));
        commandData.add(Commands.slash("search", "searches for a given word")
                .addOption(OptionType.STRING, "keyword", "word (in English, hiragana, " +
                        "or kanji) being searched for", true));
        commandData.add(Commands.slash("help", "gets a list of commands"));
        return commandData;
    }
}
