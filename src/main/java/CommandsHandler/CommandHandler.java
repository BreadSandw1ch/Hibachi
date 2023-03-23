package CommandsHandler;

import InfoHandler.InfoHandler;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        switch (commandName) {
            case "info"->
                event.reply(String.valueOf(InfoHandler.getFiles())).queue();
            case "dictionary" ->
                event.reply("dictionary").queue();
            case "search" ->
                event.reply("search").queue();
            case "quiz" ->
                event.reply("quiz").queue();
            case "keyword" ->
                event.reply("keyword").queue();
            case "help" ->
                event.reply("test").queue();
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
}
