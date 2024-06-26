import CommandsHandler.CommandHandler;
import InfoHandler.InfoHandler;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main extends CommandHandler {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Need token file");
        }
        try {
            File textFile = new File(args[0]);
            Scanner Reader = new Scanner(textFile);
            String token = Reader.nextLine();
            System.out.println("token acquired");
            JDABuilder api = JDABuilder.createDefault(token)
                    .addEventListeners(new Main())
                    .setStatus(OnlineStatus.ONLINE);
            api.setActivity(Activity.playing("anime | /help")).build();
            InfoHandler.initializeFiles("input/list-of-files.txt");
        } catch (FileNotFoundException exception) {
            System.err.println("Cannot find token");
        }
    }
}