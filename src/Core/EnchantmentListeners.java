package Core;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.Vector;

public class EnchantmentListeners implements Listener {

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onItemDrop(PlayerDropItemEvent e) {
		// Region check
		if(Survival.isInRegion(e.getPlayer().getLocation(), "enchanter")) {
			// Item Check
			List<String> allowed = Survival.pl.getConfig().getStringList("EnchantmentItems");
			String item_type = e.getItemDrop().getItemStack().getType().toString();
			if(allowed.contains(item_type)) {
				e.getItemDrop().setVelocity(new Vector());
				e.getItemDrop().teleport(new Location(e.getPlayer().getWorld(), 1567.763, 96, 132.945));
				e.getItemDrop().setGravity(false);
				e.getItemDrop().setGlowing(true);
				Enchant ench = new Enchant(e);
			}
		}
	}
	
}
