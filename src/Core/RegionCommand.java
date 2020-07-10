package Core;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;

public class RegionCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		if(cs instanceof Player) {
			Player p = (Player) cs;
			if(label.equalsIgnoreCase("sregion")) {
				if(args.length == 0) {
					p.sendMessage(ChatColor.RED + "/sregion add <name> - Requires worldedit selection");
					p.sendMessage(ChatColor.RED + "/sregion remove <name>");
				} else {
					if(args[0].equalsIgnoreCase("add")) {
						String name = args[1];
						if(Survival.pl.getConfig().contains("region." + name)) {
							p.sendMessage(ChatColor.RED + "A region with that name already exists!");
							return true;
						}
						BukkitPlayer bp = BukkitAdapter.adapt(p);
						Region rg;
						try {
							rg = WorldEdit.getInstance().getSessionManager().get(bp).getSelection(bp.getWorld());
						} catch (IncompleteRegionException e) {
							p.sendMessage(ChatColor.RED + "You don't have a WorldEdit region selected!");
							return true;
						}
						
						Survival.pl.getConfig().set("region." + name + ".minPoint.x", rg.getMinimumPoint().getBlockX());
						Survival.pl.getConfig().set("region." + name + ".minPoint.y", rg.getMinimumPoint().getBlockY());
						Survival.pl.getConfig().set("region." + name + ".minPoint.z", rg.getMinimumPoint().getBlockZ());
						Survival.pl.getConfig().set("region." + name + ".maxPoint.x", rg.getMaximumPoint().getBlockX());
						Survival.pl.getConfig().set("region." + name + ".maxPoint.y", rg.getMaximumPoint().getBlockY());
						Survival.pl.getConfig().set("region." + name + ".maxPoint.z", rg.getMaximumPoint().getBlockZ());
						List<String> regions = Survival.pl.getConfig().getStringList("Regions");
						regions.add(name);
						Survival.pl.getConfig().set("Regions", regions);
						Survival.pl.saveConfig();
						p.sendMessage(ChatColor.GREEN + "Region '"+name+"' added!");
					} else if(args[0].equalsIgnoreCase("remove")) {
						String name = args[1];
						if(!Survival.pl.getConfig().contains("region." + name)) {
							p.sendMessage(ChatColor.RED + "That region doesn't exist!");
							return true;
						}
						Survival.pl.getConfig().set("region." + name, null);
						List<String> regions = Survival.pl.getConfig().getStringList("Regions");
						regions.remove(name);
						Survival.pl.getConfig().set("Regions", regions);
						Survival.pl.saveConfig();
						p.sendMessage(ChatColor.GREEN + "Region '"+name+"' removed!");
					}
				}
			}
		}
		return true;
	}

}
