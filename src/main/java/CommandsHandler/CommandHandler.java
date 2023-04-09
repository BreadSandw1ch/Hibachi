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
    private final HashMap<Long, Quiz> runningGames = new HashMap<>();
    private final HashMap<Long, KanjiDictionary> runningDictionaries = new HashMap<>();
    private final HashMap<Long, UserInfo> users = new HashMap<>();
    private final HashMap<Long, Config> runningConfigs = new HashMap<>();

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
                List<Button> buttons = createDictionaryButtons();
                event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                runningDictionaries.put(author.getIdLong(), dictionary);
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
                event.reply(" ").setEmbeds(eb.build()).addActionRow(createDictionaryButtons()).queue();
                runningDictionaries.put(author.getIdLong(), search);
            }
            case "quiz" -> {
                Quiz quiz = new Quiz(user);
                EmbedBuilder eb = quiz.buildQuestion();
                ArrayList<Word> options = quiz.getMcWordOptions();
                if (quiz.isMultipleChoice()) {
                    List<Button> buttons = createGameButtons(quiz, options);
                    event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                } else {
                    event.reply(" ").setEmbeds(eb.build()).
                            addActionRow(Button.danger("x", "Exit")).queue();
                }
                runningGames.put(author.getIdLong(), quiz);
            }
            case "config" -> {
                Config config = new Config(user);
                EmbedBuilder eb = config.createPage();
                ActionComponent dropdown = config.createConfigDropdown();
                event.reply("").setEmbeds(eb.build()).addActionRow(dropdown).
                setEphemeral(true).queue();
                runningConfigs.put(author.getIdLong(), config);
            }
            case "help" ->
                event.reply("test").queue();
        }
    }

    public void onButtonInteraction(ButtonInteractionEvent event) {
        long userID = event.getUser().getIdLong();
        String id = event.getButton().getId();
        Message message = event.getMessage();
        assert id != null;
        if (runningGames.containsKey(userID)) {
            Quiz quiz = runningGames.get(userID);
            List<Word> options = quiz.getMcWordOptions();
            if (id.equals("x")) {
                event.reply("Exiting game (answered " + quiz.getNumCorrect() + "/"
                        + quiz.getCurrentQuestion() + " questions correctly)").queue();
                runningGames.remove(userID);
            } else {
                int choiceNum = Integer.parseInt(id);
                int numQuestions = quiz.getNumQuestions();
                Word choice = options.get(choiceNum - 1);
                EmbedBuilder eb = quiz.verifyCorrect(choice);
                message.editMessage(" ").setEmbeds(eb.build()).queue();
                if (quiz.getCurrentQuestion() < numQuestions || numQuestions == 0) {
                    eb = quiz.buildQuestion();
                    options = quiz.getMcWordOptions();
                    if (quiz.isMultipleChoice()) {
                        List<Button> buttons = createGameButtons(quiz, options);
                        event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                    } else {
                        event.reply(" ").setEmbeds(eb.build()).
                                addActionRow(Button.danger("x", "Exit")).queue();
                    }
                } else {
                    event.reply("Congratulations! You got " + quiz.getNumCorrect() + "/" +
                            quiz.getCurrentQuestion() + " correct").queue();
                    runningGames.remove(userID);
                }
            }
        }
        if (runningDictionaries.containsKey(userID)) {
            if (id.equals("x")) {
                event.reply("Closing...").queue();
                runningDictionaries.remove(userID);
            } else {
                int num = Integer.parseInt(id);
                KanjiDictionary dictionary = runningDictionaries.get(userID);
                EmbedBuilder eb = dictionary.turnPage(num);
                List<Button> buttons = createDictionaryButtons();
                event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                message.delete().queue();
            }
        }
    }

    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long userID = event.getUser().getIdLong();
        if (runningConfigs.containsKey(userID)) {
            // getLabel - gets selected option's label
            // getValue - gets selected option's value
            // getComponent.getId - gets the select menu's id
            String id = event.getInteraction().getSelectedOptions().get(0).getValue();
            if (id.equals("x")) {
                event.editMessage("Done!").setEmbeds().setActionRow().queue();
                runningConfigs.remove(userID);
            } else {
                int pageNum = Integer.parseInt(event.getComponentId());
                Config config = runningConfigs.get(userID);
                config.interact(pageNum, id);
                EmbedBuilder eb = config.createPage();
                ActionComponent selectMenu = config.createConfigDropdown();
                event.editMessage(" ").setEmbeds(eb.build()).setActionRow(selectMenu).queue();
            }
        }
    }

    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = initializeCommands();
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    public void onGuildJoin(GuildJoinEvent event) {
        MessageChannel messageChannel = event.getGuild().getSystemChannel();
        EmbedBuilder eb = InfoHandler.botInfo();
        if (messageChannel != null) {
            messageChannel.sendMessage(" ").setEmbeds(eb.build()).queue();
        }
        List<CommandData> commandData = initializeCommands();
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    public List<Button> createGameButtons(Quiz quiz, List<Word> options) {
        List<Button> buttons = new ArrayList<>();
        List<String> buttonLabels = quiz.getMcButtonLabels();
        for (int i = 0; i < options.size() && i < 4; i++) {
            buttons.add(Button.primary(String.valueOf(i+1), buttonLabels.get(i)));
        }
        buttons.add(Button.danger("x", "Exit"));
        return buttons;
    }

    public List<Button> createDictionaryButtons() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.secondary("-1", "<"));
        buttons.add(Button.secondary("1", ">"));
        buttons.add(Button.danger("x", "Exit"));
        return buttons;
    }

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
