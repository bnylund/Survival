package Core;

import org.bukkit.ChatColor;
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
					p.sendMessage(ChatColor.RED + "/shopsetup add_npc <name>");
					p.sendMessage(ChatColor.RED + "/shopsetup remove_npc <name>");
				} else {
					if(args[0].equalsIgnoreCase("add_npc")) {
						String name = args[1];
					} else if(args[0].equalsIgnoreCase("remove_npc")) {
						String name = args[1];
					}
				}
			}
		}
		return true;
	}

}
