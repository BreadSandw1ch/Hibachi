package CommandsHandler;

import InfoHandler.*;
import Quiz.Quiz;
import Dictionary.KanjiDictionary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.*;

public class CommandHandler extends ListenerAdapter {
    private final HashMap<Long, Quiz> runningGames = new HashMap<>();
    private final HashMap<Long, KanjiDictionary> runningDictionaries = new HashMap<>();

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        User author = event.getUser();
        switch (commandName) {
            case "info"->
                event.reply(String.valueOf(InfoHandler.getFiles()))
                        .addActionRow(Button.primary("test", "test")).queue();
            case "dictionary" -> {
                HashMap<String, String> files = InfoHandler.getFiles();
                Collection<String> filenames = files.values();
                HashMap<String, Word> words = InfoHandler.readFiles(filenames);
                KanjiDictionary dictionary = new KanjiDictionary(words);
                EmbedBuilder eb = dictionary.createPage();
                List<Button> buttons = createDictionaryButtons();
                event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                runningDictionaries.put(author.getIdLong(), dictionary);
            }
            case "search" -> {
                String[] command = event.getCommandString().split("\\s+");
                String key = command[command.length - 1].toLowerCase();
                HashMap<String, String> files = InfoHandler.getFiles();
                Collection<String> filenames = files.values();
                HashMap<String, Word> dictionary = InfoHandler.readFiles(filenames);
                Set<Word> results = new HashSet<>();
                for (Word word: dictionary.values()) {
                    if (word.hasMeaning(key) || key.equals(word.getWord())) {
                        results.add(word);
                    } else if (word instanceof Kanji) {
                        if (((Kanji) word).hasReading(key)) {
                            results.add(word);
                        }
                    }
                }
                event.reply("results: \n" + results).queue();
            }
            case "quiz" -> {
                HashMap<String, String> files = InfoHandler.getFiles();
                Collection<String> filenames = files.values();
                UserInfo user = new UserInfo(author, filenames);
                Quiz quiz = new Quiz(user);
                EmbedBuilder eb = quiz.buildQuestion();
                ArrayList<Word> options = quiz.getMcOptions();
                List<Button> buttons = createGameButtons(options);
                event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                runningGames.put(author.getIdLong(), quiz);
            }
            case "keyword" ->
                event.reply("keyword").queue();
            case "help" ->
                event.reply("test").queue();
        }
    }

    public void onButtonInteraction(ButtonInteractionEvent event) {
        MessageChannel channel = event.getChannel();
        long userID = event.getUser().getIdLong();
        String id = event.getButton().getId();
        Message message = event.getMessage();
        assert id != null;
        if (runningGames.containsKey(userID)) {
            Quiz quiz = runningGames.get(userID);
            List<Word> options = quiz.getMcOptions();
            if (id.equals("x")) {
                event.reply("Exiting game").queue();
                runningGames.remove(userID);
            } else {
                int choiceNum = Integer.parseInt(id);
                Word choice = options.get(choiceNum - 1);
                EmbedBuilder eb = quiz.verifyCorrect(choice);
                message.editMessage(" ").setEmbeds(eb.build()).queue();
                if (quiz.getCurrentQuestion() < 10) {
                    eb = quiz.buildQuestion();
                    options = quiz.getMcOptions();
                    List<Button> buttons = createGameButtons(options);
                    event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                } else {

                    event.reply("Congratulations! You got " + quiz.getNumCorrect() + "/10 correct").queue();
                }
            }
        }
        if (runningDictionaries.containsKey(userID)) {
            if (id.equals("x")) {
                channel.sendMessage("Exiting dictionary").queue();
                runningDictionaries.remove(userID);
            } else {
                int num = Integer.parseInt(id);
                KanjiDictionary dictionary = runningDictionaries.get(userID);
                EmbedBuilder eb = dictionary.turnPage(num);
                List<Button> buttons = createDictionaryButtons();
                event.reply(" ").setEmbeds(eb.build()).addActionRow(createDictionaryButtons()).queue();
                message.delete().queue();
            }
        }
    }

    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("info", "displays info about this bot"));
        commandData.add(Commands.slash("dictionary", "pulls up a dictionary"));
        commandData.add(Commands.slash("quiz", "starts a quiz"));
        commandData.add(Commands.slash("search", "searches for a given word")
                .addOption(OptionType.STRING, "keyword", "word (in English, hiragana, " +
                        "or kanji) being searched for", true));
        commandData.add(Commands.slash("help", "gets a list of commands"));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    public void onGuildJoin(GuildJoinEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("info", "displays info about this bot"));
        commandData.add(Commands.slash("dictionary", "pulls up a dictionary"));
        commandData.add(Commands.slash("quiz", "starts a quiz"));
        commandData.add(Commands.slash("search", "searches for a given word")
                .addOption(OptionType.STRING, "keyword", "word (in English, hiragana, " +
                        "or kanji) being searched for", true));
        commandData.add(Commands.slash("help", "gets a list of commands"));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    public List<Button> createGameButtons(List<Word> options) {
        List<Button> buttons = new ArrayList<>(){};
        for (int i = 0; i < options.size() && i < 4; i++) {
            buttons.add(Button.primary(String.valueOf(i+1), options.get(i).getWord()));
        }
        buttons.add(Button.danger("x", "Exit"));
        return buttons;
    }

    public List<Button> createDictionaryButtons() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.secondary("-1", "<"));
        buttons.add(Button.secondary("1", ">"));
        // buttons.add(Button.danger("x", "Exit"));
        return buttons;
    }
}
