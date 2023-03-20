import CommandsHandler.CommandHandler;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main extends CommandHandler {
    public static void main(String[] args) throws FileNotFoundException {
        File textFile = new File("C:\\Users\\gavqu\\Documents\\Discord Bot Things\\tokenhibachi.txt");
        Scanner Reader = new Scanner(textFile);
        String token = Reader.nextLine();
        System.out.println("token acquired");
        JDABuilder api = JDABuilder.createDefault(token)
                .addEventListeners(new Main())
                .setStatus(OnlineStatus.ONLINE);
        api.setActivity(Activity.playing("sudoku | /help")).build();


    }
}