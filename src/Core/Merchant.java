package Core;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class Merchant {

	private String name;
	private Location loc;
	private ClickableInventory inv;
	private Villager ent;
	
	public Merchant(String name, Location location, ClickableInventory inventory) {
		this.name = name;
		loc = location;
		inv = inventory;
	}
	
	public String getName() {
		return name;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public ClickableInventory getInventory() {
		return inv;
	}
	
	public boolean isAlive() {
		return (ent == null ? false : ent.isValid());
	}
	
	public void Spawn() {
		if(!isAlive()) {
			ent = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
			ent.setCustomName(ChatColor.AQUA + ChatColor.BOLD.toString() + name);
			ent.setCustomNameVisible(true);
			ent.setInvulnerable(true);
			ent.setAI(false);
			ent.setCollidable(false);
			ent.setAware(false);
			ent.setCanPickupItems(false);
			ent.setBreed(false);
			ent.setRemoveWhenFarAway(false);
		}
	}
	
	public void Despawn() {
		if(isAlive()) {
			ent.remove();
			ent = null;
		}
	}
}
