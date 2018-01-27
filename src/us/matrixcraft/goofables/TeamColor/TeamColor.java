package us.matrixcraft.goofables.TeamColor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

public class TeamColor extends JavaPlugin implements Listener {
   private boolean enabled = true;
   
   @Override
   public void onEnable() {
      getServer().getPluginManager().registerEvents(this, this);
   }
   
   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length < 1) return false;
      switch (args[0].toLowerCase()) {
         case "enable":
            if (testPermission(sender, "teamcolor.enable")) {
               enabled = true;
               sender.sendMessage(ChatColor.GREEN + "TeamColor enabled!");
            }
            return true;
         
         case "disable":
            if (testPermission(sender, "teamcolor.disable")) {
               enabled = false;
               sender.sendMessage(ChatColor.RED + "TeamColor disabled!");
            }
            return true;
         
         case "prefix":
         case "suffix":
            if (!(sender instanceof Player)) {
               sender.sendMessage(ChatColor.RED + "Error! Must be player for this command!");
               return true;
            }
            if (testPermission(sender, "teamcolor." + args[0].toLowerCase())) {
               boolean pre = args[0].toLowerCase().equals("prefix");
               Team team = getTeam((Player)sender);
               if (args.length == 1) {
                  String out = (pre)?"Prefix: `" + team.getPrefix():"Suffix: `" + team.getSuffix();
                  sender.sendMessage(ChatColor.GREEN + out.replace('§', '&') + "`!");
               } else {
                  StringBuilder sb = new StringBuilder("");
                  for (int i = 1; i < args.length; i++)
                     if (i == 1) sb.append(args[i]);
                     else sb.append(" ").append(args[i]);
                  String s = sb.toString();
                  s = s.replace('&', '§');
                  if (s.charAt(0) == '"' || s.charAt(0) == '\'') s = s.substring(1);
                  if (s.charAt(s.length() - 1) == '"' || s.charAt(s.length() - 1) == '\'')
                     s = s.substring(0, s.length() - 1);
                  if (s.length() <= 16) {
                     if (pre) team.setPrefix(s);
                     else team.setSuffix(s);
                     sender.sendMessage(
                             ChatColor.GREEN + ((pre)?"Prefix":"Suffix") + " for team " + team.getDisplayName() +
                                     " changed to `" + s.replace('§', '&') + "`!");
                  } else sender.sendMessage(ChatColor.RED + "Error! String is longer than 16 characters!");
               }
            }
            return true;
         case "show":
            if (!(sender instanceof Player)) {
               sender.sendMessage(ChatColor.RED + "Error! Must be player for this command!");
               return true;
            }
            if (testPermission(sender, "teamcolor." + args[0].toLowerCase())) {
               Team team = getTeam((Player)sender);
               String pre = team.getPrefix();
               String suf = team.getSuffix();
               sender.sendMessage(
                       ChatColor.GREEN + "Your team displays as `" + ChatColor.RESET + "<" + pre + "[username]" + suf +
                               ">" + ChatColor.GREEN + "`.");
               sender.sendMessage(ChatColor.GREEN + "Color codes: `" + ChatColor.RESET + "<" + pre.replace('§', '&') +
                                          "[username]" + suf.replace('§', '&') + ">" + ChatColor.GREEN + "`.");
            }
            return true;
      }
      return false;
   }
   
   @EventHandler
   public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
      updateName(event.getPlayer());
   }
   
   public void onPlayerJoin(PlayerJoinEvent event) {
      updateName(event.getPlayer());
   }
   
   private void updateName(Player player) {
      if (!enabled) return;
      Team team = getTeam(player);
      player.setDisplayName(((team == null)?ChatColor.RESET:team.getPrefix()) + player.getName() +
                                    ((team == null)?ChatColor.RESET:team.getSuffix()));
   }
   
   private boolean testPermission(CommandSender sender, String permission) {
      if (sender.hasPermission(permission)) return true;
      sender.sendMessage(
              ChatColor.RED + "Error! You need permission `" + ChatColor.ITALIC + permission + ChatColor.RED + "`!");
      return false;
   }
   
   private Team getTeam(Player player) {
      return Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
   }
}

