package CommandsHandler;

import InfoHandler.*;
import Quiz.Quiz;
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
    private final HashMap<Long, Quiz> runningGames = new HashMap<Long, Quiz>();

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        switch (commandName) {
            case "info"->
                event.reply(String.valueOf(InfoHandler.getFiles()))
                        .addActionRow(Button.primary("test", "test")).queue();
            case "dictionary" -> {
                HashMap<String, String> files = InfoHandler.getFiles();
                Collection<String> filenames = files.values();
                HashMap<String, Word> dictionary = InfoHandler.readFiles(filenames);
                StringBuilder stringBuilder = new StringBuilder();
                for (Word word:dictionary.values()) {
                    if(stringBuilder.length() + word.toString().length() > 2000) break;
                    stringBuilder.append(word).append("\n");
                }
                event.reply(stringBuilder.toString()).queue();
            }
            case "search" -> {
                String[] command = event.getCommandString().split("\\s+");
                String key = command[command.length - 1];
                event.reply("search " + key).queue();
            }
            case "quiz" -> {
                HashMap<String, String> files = InfoHandler.getFiles();
                Collection<String> filenames = files.values();
                User author = event.getUser();
                UserInfo user = new UserInfo(author, filenames);
                Quiz quiz = new Quiz(user);
                EmbedBuilder eb = quiz.buildQuestion();
                ArrayList<Word> options = quiz.getMcOptions();
                List<Button> buttons = new ArrayList<>(){};
                buttons.add(Button.primary("1", options.get(0).getWord()));
                buttons.add(Button.primary("2", options.get(1).getWord()));
                buttons.add(Button.primary("3", options.get(2).getWord()));
                buttons.add(Button.primary("4", options.get(3).getWord()));
                buttons.add(Button.danger("x", "Exit"));
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
        if (runningGames.containsKey(userID)) {
            Quiz quiz = runningGames.get(userID);
            List<Word> options = quiz.getMcOptions();
            String id = event.getButton().getId();
            assert id != null;
            if (id.equals("x")) {
                channel.sendMessage("Exiting game").queue();
                runningGames.remove(userID);
            } else {
                int choiceNum = Integer.parseInt(id);
                Word choice = options.get(choiceNum - 1);
                EmbedBuilder eb = quiz.verifyCorrect(choice);
                Message message = event.getMessage();
                message.editMessage(" ").setEmbeds(eb.build()).queue();
                // gonna have to bring this part into its own function
                if (quiz.getCurrentQuestion() < 10) {
                    eb = quiz.buildQuestion();
                    options = quiz.getMcOptions();
                    List<Button> buttons = new ArrayList<>(){};
                    buttons.add(Button.primary("1", options.get(0).getWord()));
                    buttons.add(Button.primary("2", options.get(1).getWord()));
                    buttons.add(Button.primary("3", options.get(2).getWord()));
                    buttons.add(Button.primary("4", options.get(3).getWord()));
                    buttons.add(Button.danger("x", "Exit"));
                    event.reply(" ").setEmbeds(eb.build()).addActionRow(buttons).queue();
                    runningGames.replace(userID, quiz);
                } else {

                    event.reply("Congratulations! You got " + quiz.getNumCorrect() + "/10 correct").queue();
                }
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
}
