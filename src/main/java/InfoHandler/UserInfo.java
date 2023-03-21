package InfoHandler;

import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

public record UserInfo(User user, HashMap<String, Boolean> settings) {


}
