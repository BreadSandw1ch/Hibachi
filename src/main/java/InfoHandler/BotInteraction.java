package InfoHandler;

import net.dv8tion.jda.api.interactions.components.ActionComponent;

import java.util.List;

public interface BotInteraction {
    /**
     * Creates the action components for the interaction
     * @return a list representing the above information
     */
    List<ActionComponent> createComponents();
}
