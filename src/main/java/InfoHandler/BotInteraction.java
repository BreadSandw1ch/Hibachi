package InfoHandler;

import net.dv8tion.jda.api.interactions.components.ActionComponent;

import java.util.List;

public interface BotInteraction {
    List<ActionComponent> createComponents();
}
