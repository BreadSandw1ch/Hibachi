package InfoHandler;

import net.dv8tion.jda.api.entities.User;

public record UserInfo(User user, java.util.Collection<String> files) {


}
