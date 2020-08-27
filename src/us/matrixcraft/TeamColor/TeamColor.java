package us.matrixcraft.TeamColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

public class TeamColor extends JavaPlugin implements Listener {
    private static final char USER_CHAR = '&';
    private final List<String> mainArgs0 = Arrays.asList("enable", "disable", "prefix", "suffix", "show");
    private Command mainCommand;
    private boolean disabled;
    
    private static void notOnTeam(CommandSender sender) {
        sender.sendMessage(RED + "Error! You are not on a team.");
        sender.sendMessage(RED + "  to create a new team /scoreboard teams add <teamName>");
        sender.sendMessage(RED + "  to join a team /scoreboard teams join <teamName>");
    }
    
    private static boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        sender.sendMessage(RED + "Error! You need permission `" + ITALIC + permission + RED + "`!");
        return false;
    }
    
    private static Team getTeam(Player player) {
        return Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        getServer().getPluginManager().registerEvents(this, this);
        mainCommand = getCommand("teamcolor");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ("colors".equalsIgnoreCase(command.getLabel())) {
            sender.sendMessage("§a< === Usable Color Codes === >");
            sender.sendMessage("  §0&0   §1&1   §2&2   §3&3");
            sender.sendMessage("  §4&4   §5&5   §6&6   §7&7");
            sender.sendMessage("  §8&8   §9&9   §a&a   §b&b");
            sender.sendMessage("  §c&c   §d&d   §e&e   §f&f");
            sender.sendMessage("&k §kMagic§r     &l §lBold");
            sender.sendMessage("&m §mStrike§r    &n §nUline");
            sender.sendMessage("&o §oItalics§r    &r §rReset");
            sender.sendMessage("&aExample:&r &2Hello&6&lWorld&d! = §2Hello§6§lWorld§d!");
            return true;
        }
        if (args.length < 1) return false;
        final String arg0 = args[0].toLowerCase();
        if (!hasPermission(sender, "teamcolor." + arg0)) return true;
        switch (arg0) {
            case "enable":
            case "disable":
                disabled = ("enable".equals(arg0));
                sender.sendMessage(GREEN + "TeamColor " + (disabled?"enabled":(RED + "disabled")) + GREEN + '!');
                return true;
            case "prefix":
            case "suffix": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(RED + "Error! Must be player for this command!");
                    return true;
                }
                boolean pre = "prefix".equals(arg0);
                Team team = getTeam((Player)sender);
                if (team == null) {
                    notOnTeam(sender);
                    return true;
                }
                if (args.length == 1) {
                    String out = (pre)?("Prefix: `" + team.getPrefix()):("Suffix: `" + team.getSuffix());
                    sender.sendMessage(GREEN + out.replace(COLOR_CHAR, USER_CHAR) + "`!");
                } else {
                    StringBuilder sb = new StringBuilder("");
                    for (int i = 1; i < args.length; i++)
                        if (i == 1) sb.append(args[1]);
                        else sb.append(' ').append(args[i]);
                    String s = sb.toString();
                    s = s.replace(USER_CHAR, COLOR_CHAR);
                    if (s.charAt(0) == '"' || s.charAt(0) == '\'') s = s.substring(1);
                    if (s.charAt(s.length() - 1) == '"' || s.charAt(s.length() - 1) == '\'')
                        s = s.substring(0, s.length() - 1);
                    if (s.length() <= 16) { // 16 = Max name length in minecraft
                        if (pre) {
                            team.setPrefix(s);
                            if (team.getSuffix().isEmpty()) team.setSuffix("§r");
                        } else team.setSuffix(s);
                        sender.sendMessage(GREEN + ((pre)?"Prefix":"Suffix") + " for team " + team.getDisplayName() +
                                " changed to `" + s.replace(COLOR_CHAR, USER_CHAR) + "`!");
                    } else sender.sendMessage(RED + "Error! String is longer than 16 characters!");
                }
            }
            return true;
            case "show": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(RED + "Error! Must be player for this command!");
                    return true;
                }
                Team team = getTeam((Player)sender);
                if (team == null) {
                    notOnTeam(sender);
                    return true;
                }
                String pre = team.getPrefix();
                String suf = team.getSuffix();
                sender.sendMessage(
                        GREEN + "Your team displays as `" + RESET + '<' + pre + "[username]" + suf + '>' + GREEN +
                                "`.");
                sender.sendMessage(
                        GREEN + "Color codes: `" + RESET + '<' + pre.replace(COLOR_CHAR, USER_CHAR) + "[username]" +
                                suf.replace(COLOR_CHAR, USER_CHAR) + '>' + GREEN + "`.");
                
                return true;
            }
        }
        return false;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        updateName(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateName(event.getPlayer());
    }
    
    private void updateName(Player player) {
        if (disabled) return;
        Team team = getTeam(player);
        player.setDisplayName(((team == null)?RESET:team.getColor() + team.getPrefix()) + player.getName() +
                ((team == null)?RESET:team.getSuffix() + RESET));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return (command != mainCommand || args.length > 1)?Collections.emptyList():(mainArgs0.stream().filter(
                str -> str.toLowerCase().startsWith((args.length == 0)?"":args[0].toLowerCase())).collect(
                Collectors.toList()));
    }
}

