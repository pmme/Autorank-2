package me.armar.plugins.autorank.pathbuilder.requirement;

import me.armar.plugins.autorank.language.Lang;
import me.staartvin.plugins.pluginlibrary.Library;
import me.staartvin.plugins.pluginlibrary.hooks.QuestsFatPigsAreFatHook;
import org.bukkit.entity.Player;

public class QuestsFPAFActiveQuestsRequirement extends AbstractRequirement {

    private QuestsFatPigsAreFatHook handler = null;
    private int activeQuests = -1;

    @Override
    public String getDescription() {
        return Lang.QUESTS_ACTIVE_QUESTS_REQUIREMENT.getConfigValue(activeQuests);
    }

    @Override
    public String getProgress(final Player player) {
        return handler.getNumberOfActiveQuests(player.getUniqueId()) + "/" + activeQuests;
    }

    @Override
    public boolean meetsRequirement(final Player player) {

        if (!handler.isAvailable())
            return false;

        return handler.getNumberOfActiveQuests(player.getUniqueId()) >= activeQuests;
    }

    @Override
    public boolean setOptions(final String[] options) {

        // Add dependency
        addDependency(Library.QUESTS_FATPIGSAREFAT);

        handler = (QuestsFatPigsAreFatHook) this.getDependencyManager().getLibraryHook(Library.QUESTS_FATPIGSAREFAT);

        if (options.length > 0) {
            try {
                activeQuests = Integer.parseInt(options[0]);
            } catch (NumberFormatException e) {
                this.registerWarningMessage("An invalid number is provided");
                return false;
            }
        }

        if (activeQuests < 0) {
            this.registerWarningMessage("No number is provided or smaller than 0.");
            return false;
        }

        return true;
    }
}
