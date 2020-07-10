package Core;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.mysql.fabric.xmlrpc.base.Array;

public class Enchant {

	private Item item;
	private ItemStack itemstack;
	private Hologram hg;
	private Player p;
	
	public Enchant(PlayerDropItemEvent e) {
		item = e.getItemDrop();
		itemstack = e.getItemDrop().getItemStack();
		hg = HologramsAPI.createHologram(Survival.pl, new Location(item.getWorld(), item.getLocation().getX(), item.getLocation().getY() + 1, item.getLocation().getZ()));
		hg.appendTextLine(Survival.getFormattedName(itemstack.getType().toString()));
		p = e.getPlayer();
		Bukkit.getScheduler().runTaskLater(Survival.pl, new Runnable() {

			@Override
			public void run() {
				applyEnchant();
			}
			
		}, 20 * 3);
		Bukkit.getScheduler().runTaskLater(Survival.pl, new Runnable() {

			@Override
			public void run() {
				Finish();
			}
			
		}, 20 * 6);
	}
	
	public void updateHologramLocation() {
		hg.teleport(item.getWorld(), item.getLocation().getX(), item.getLocation().getY() + 1, item.getLocation().getZ());
	}
	
	public void applyEnchant() {
		hg.appendTextLine(ChatColor.GREEN + "");
		List<Enchantment> enchants = Survival.validEnchantments.get(itemstack.getType());
		// Determine size of all possible enchants
		int num = enchants.size();
		
		// Determine chances on 1 enchant, 2 enchants, 3 enchants, etc via exponential equation
		double[] chances = new double[num];
		for(int i = 1; i <= num; i++) {
			double chance = Math.pow(2, i * (1 / num));	
			chances[i - 1] = chance;
		}
		
		// Generate random number
		double randNum = Math.random();
		
		// Since the smallest numbers are generated first, we want to start at the high end of the array.
		for(int i = chances.length - 1; i >= 0; i--) {
			if(randNum >= chances[i]) {
				
				break;
			}
		}
	}
	
	public void Finish() {
		hg.delete();
		p.getInventory().addItem(itemstack);
		item.remove();
	}
}
