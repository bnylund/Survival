package Core;

import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.md_5.bungee.api.ChatColor;

public class ShopListeners implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		if(e.getRightClicked() instanceof Villager) {
			for(Merchant merchant : Survival.merchants) {
				Villager ent = (Villager) e.getRightClicked();
				if(ChatColor.stripColor(ent.getCustomName()).equals(merchant.getName())) {
					e.getPlayer().openInventory(merchant.getInventory());
				}
			}
		}
	}
	
}
