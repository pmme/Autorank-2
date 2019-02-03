package me.armar.plugins.autorank.commands;

import me.armar.plugins.autorank.Autorank;
import me.armar.plugins.autorank.commands.manager.AutorankCommand;
import me.armar.plugins.autorank.language.Lang;
import me.armar.plugins.autorank.pathbuilder.Path;
import me.armar.plugins.autorank.pathbuilder.holders.CompositeRequirement;
import me.armar.plugins.autorank.pathbuilder.result.AbstractResult;
import me.armar.plugins.autorank.permissions.AutorankPermission;
import me.armar.plugins.autorank.util.AutorankTools;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The command delegator for the '/ar view' command.
 */
public class ViewCommand extends AutorankCommand {

    private final Autorank plugin;

    public ViewCommand(final Autorank instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {

        // This command will give a preview of a certain path of ranking.
        if (!this.hasPermission(AutorankPermission.VIEW_PATH, sender)) {
            return true;
        }

//        if (args.length < 2) {
//            sender.sendMessage(Lang.INVALID_FORMAT.getConfigValue("/ar view <path name> or /ar view list"));
//            return true;
//        }

        String pathName = (args.length == 2) ? AutorankTools.getStringFromArgs(args, 1) : "";

        boolean isPlayer = false;

        // Check if sender is player or console
        if (sender instanceof Player) {
            isPlayer = true;
        }

        // /ar view list (or a name of a path)

        // Get a list of possible paths that a player can take?
        if (args.length == 1 || pathName.equals("list")) {

            final List<Path> paths = new ArrayList<>();

            // Loop through all paths and check if we can show it or not.
            for (Path path : plugin.getPathManager().getAllPaths()) {
                if (isPlayer && path.onlyShowIfPrerequisitesMet() && path.meetsPrerequisites((Player) sender)) {
                    continue;
                }

                paths.add(path);
            }

            if (paths.isEmpty()) {
                sender.sendMessage(Lang.NO_PATHS_TO_CHOOSE.getConfigValue());
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "The following paths are possible: ");

            final String pathsString = AutorankTools.createStringFromList(paths);
            sender.sendMessage(ChatColor.WHITE + pathsString);
            sender.sendMessage(ChatColor.GOLD + "To view the requirements of a specific path, use /ar view <path name>.");
            return true;
        } else {
            // Third argument is probably a name of a path

            // Show details of path

            Path targetPath = plugin.getPathManager().findPathByDisplayName(pathName, false);

            if (targetPath == null) {
                sender.sendMessage(Lang.NO_PATH_FOUND_WITH_THAT_NAME.getConfigValue());
                return true;
            }

            List<CompositeRequirement> prerequisites = targetPath.getPrerequisites();
            List<CompositeRequirement> completedRequirements = null;
            if(isPlayer) {
                UUID uuid = ((Player)sender).getUniqueId();
                if (plugin.getPathManager().hasActivePath(uuid, targetPath)) {
                    plugin.getPlayerChecker().checkPlayer((Player)sender);
                    completedRequirements = targetPath.getCompletedRequirements(uuid);
                }
            }
            if(completedRequirements == null){
                completedRequirements = new ArrayList<>();
            }

            List<String> messages = plugin.getPlayerChecker().formatRequirementsToList(prerequisites, completedRequirements);

            sender.sendMessage(ChatColor.GREEN + "Prerequisites of path '" + ChatColor.GRAY
                    + targetPath.getDisplayName() + ChatColor.GREEN + "':");

            if (messages.isEmpty()) {
                AutorankTools.sendColoredMessage(sender, "none");
            } else {
                for (final String message : messages) {
                    AutorankTools.sendColoredMessage(sender, message);
                }
            }


            List<CompositeRequirement> requirements = targetPath.getRequirements();

            messages = plugin.getPlayerChecker().formatRequirementsToList(requirements, new
                    ArrayList<>());

            sender.sendMessage(ChatColor.GREEN + "Requirements of path '" + ChatColor.GRAY
                    + targetPath.getDisplayName() + ChatColor.GREEN + "':");

            if (messages.isEmpty()) {
                AutorankTools.sendColoredMessage(sender, "none");
            } else {
                for (final String message : messages) {
                    AutorankTools.sendColoredMessage(sender, message);
                }
            }

//            List<AbstractResult> results = targetPath.getResults();
//
//            // Set messages depending on console or player
//            messages = plugin.getPlayerChecker().formatResultsToList(results);
//
//            sender.sendMessage(ChatColor.GREEN + "Results of path '" + ChatColor.GRAY + targetPath.getDisplayName()
//                    + ChatColor.GREEN + "':");
//
//            if (messages.isEmpty()) {
//                AutorankTools.sendColoredMessage(sender, "none");
//            } else {
//                for (final String message : messages) {
//                    AutorankTools.sendColoredMessage(sender, message);
//                }
//            }

        }
        return true;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * me.armar.plugins.autorank.commands.manager.AutorankCommand#onTabComplete(
     * org.bukkit.command.CommandSender, org.bukkit.command.Command,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String commandLabel,
                                      final String[] args) {
        final List<String> possibilities = new ArrayList<String>();

        // There are no words entered, so give all paths
        if (args.length <= 1) {
            for (final Path path : plugin.getPathManager().getAllPaths()) {
                possibilities.add(path.getDisplayName());
            }

            // List shows a list of changegroups to view
            possibilities.add("list");
        } else {
            // Something was typed, so use that to suggest paths.
            for (final Path path : plugin.getPathManager().getAllPaths()) {

                // Only suggest paths that start with the given word.
                if (path.getDisplayName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    possibilities.add(path.getDisplayName());
                }

                if (args[1].trim().equals("")) {
                    possibilities.add("list");
                }
            }
        }

        return possibilities;
    }

    @Override
    public String getDescription() {
        return "Gives a preview of a certain ranking path";
    }

    @Override
    public String getPermission() {
        return AutorankPermission.VIEW_PATH;
    }

    @Override
    public String getUsage() {
        return "/ar view <path name>";
    }

}
