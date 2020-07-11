package Core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

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
	}
	
	public void Start() {
		BukkitTask bt = Bukkit.getScheduler().runTaskTimer(Survival.pl, new Runnable() {

			@Override
			public void run() {
				updateHologramLocation();
			}
			
		}, 0L, 1L);
		Bukkit.getScheduler().runTaskLater(Survival.pl, new Runnable() {

			@Override
			public void run() {
				applyEnchants();
			}
			
		}, 20 * 5);
		Bukkit.getScheduler().runTaskLater(Survival.pl, new Runnable() {

			@Override
			public void run() {
				bt.cancel();
				Finish();
			}
			
		}, 20 * 9);
	}
	
	private void updateHologramLocation() {
		hg.teleport(item.getWorld(), item.getLocation().getX(), item.getLocation().getY() + 1, item.getLocation().getZ());
	}
	
	@SuppressWarnings("deprecation")
	private void applyEnchants() {
		hg.appendTextLine(ChatColor.GREEN + "");
		List<Enchantment> enchants = Survival.validEnchantments.get(itemstack.getType());
		// Determine size of all possible enchants
		int num = enchants.size();
		
		// Determine chances on 1 enchant, 2 enchants, 3 enchants, etc via exponential equation
		double[] chances = new double[num];
		for(int i = 1; i <= num; i++) {
			double chance = 0.0158114 * Math.pow(56.5047, (double)i * (1 / (double)num));	
			chances[i - 1] = chance;
		}
		
		// Generate random number
		double randNum = Math.random();
		
		int toAdd = 1;
		
		// chances[0] = 0.052566, chances[1] = 0.2456986
		for(int i = 0; i <= chances.length - 1; i++) {
			Bukkit.broadcastMessage("Checking " + randNum + " >= " + chances[i]);
			if(randNum <= chances[i]) {
				toAdd = chances.length - i;
				Bukkit.broadcastMessage("true, adding " + toAdd);
				break;
			}
		}
		
		List<Enchantment> adding = new ArrayList<Enchantment>();
		
		// Apply enchants.
		for(int i = 0; i < toAdd; i++) {
			int random = new Random().nextInt(enchants.size());
			adding.add(enchants.get(random));
			Bukkit.broadcastMessage("adding " + enchants.get(random).getName());
			enchants.remove(random);
		}
		
		for(Enchantment ench : adding) {
			itemstack.addUnsafeEnchantment(ench, ench.getMaxLevel());
			hg.appendTextLine(ChatColor.YELLOW + Survival.getFormattedName(ench.getName()));
		}
	}
	
	public void Finish() {
		hg.delete();
		p.getInventory().addItem(itemstack);
		item.remove();
	}
}
