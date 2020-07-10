package Core;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class SurvivalPlayer {
	private String Name;

	private UUID Uuid;

	private Player _player;

	protected SurvivalPlayer(Player player) {
		this.Name = player.getName();
		this.Uuid = player.getUniqueId();
		this._player = player;
	}

	protected SurvivalPlayer(UUID player) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(player);
		this.Name = p.getName();
		this.Uuid = p.getUniqueId();
		try {
			this._player = Bukkit.getPlayer(player);
		} catch(Exception exception) {}
	}

	public String getName() {
		return this.Name;
	}

	public UUID getUUID() {
		return this.Uuid;
	}

	public void sendMessage(String message) {
		this._player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
}