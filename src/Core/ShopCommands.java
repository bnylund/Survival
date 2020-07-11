package Core;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if(cs instanceof Player) {
			Player p = (Player) cs;
			if(label.equalsIgnoreCase("shop")) {
				// teleport?
			} else if(label.equalsIgnoreCase("shopsetup") && p.isOp()) {
				if(args.length == 0) {
					p.sendMessage(ChatColor.RED + "/shopsetup set_npc_location <name>");
					p.sendMessage(ChatColor.RED + "/shopsetup remove_npc <name>");
				} else {
					if(args[0].equalsIgnoreCase("set_npc_location")) {
						String name = args[1];
						Location loc = p.getLocation();
						
						Survival.pl.getConfig().set("shop." + name + ".npc.world", loc.getWorld().getName());
						Survival.pl.getConfig().set("shop." + name + ".npc.x", loc.getX());
						Survival.pl.getConfig().set("shop." + name + ".npc.y", loc.getY());
						Survival.pl.getConfig().set("shop." + name + ".npc.z", loc.getZ());
						Survival.pl.getConfig().set("shop." + name + ".npc.yaw", loc.getYaw());
						Survival.pl.getConfig().set("shop." + name + ".npc.pitch", loc.getPitch());
						Survival.pl.saveConfig();
						
						Survival.reloadMerchant(name);
						p.sendMessage(ChatColor.GREEN + "NPC location set!");
					} else if(args[0].equalsIgnoreCase("remove_npc")) {
						String name = args[1];
						
						Survival.pl.getConfig().set("shop." + name, null);
						Survival.pl.saveConfig();
						
						Survival.reloadMerchant(name);
						p.sendMessage(ChatColor.GREEN + "NPC location removed!");
					}
				}
			}
		}
		return true;
	}

}
