package net.tropolia.woodensword;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class WoodenSwordHelper extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("WoodenSwordHelper enabled!");
    }

    private void giveSword(Player player) {
        if (player.getWorld().getName().equalsIgnoreCase("lobby")) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!player.isOnline()) {
                return;
            }

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.WOODEN_SWORD) {
                    return;
                }
            }

            player.getInventory().addItem(
                new ItemStack(Material.WOODEN_SWORD)
            );
        }, 10L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        giveSword(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            giveSword(event.getPlayer());
        }, 2L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        giveSword(event.getPlayer());
    }
}
