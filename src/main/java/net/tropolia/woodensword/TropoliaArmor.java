
package net.tropolia.woodensword;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Locale;

public final class TropoliaArmor extends JavaPlugin implements Listener {

    
@Override
public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, this);

    Bukkit.getScheduler().runTaskTimer(this, () -> {
        for (Player player : Bukkit.getOnlinePlayers()) {
            equipTeamArmor(player);
        }
    }, 20L, 20L);

    getLogger().info("TropoliaArmor enabled!");
}
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        scheduleArmor(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scheduleArmor(event.getPlayer());
    }

    private void scheduleArmor(Player player) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) {
                equipTeamArmor(player);
            }
        }, 2L);
    }

    private void equipTeamArmor(Player player) {
        String team = getTeamName(player);
        if (team == null) return;

        Color color = getTeamColor(team);
        if (color == null) return;

        setLeatherIfEmpty(player, 0, Material.LEATHER_BOOTS, color);
        setLeatherIfEmpty(player, 1, Material.LEATHER_LEGGINGS, color);
        setLeatherIfEmpty(player, 2, Material.LEATHER_CHESTPLATE, color);
        setLeatherIfEmpty(player, 3, Material.LEATHER_HELMET, color);
    }

    private void setLeatherIfEmpty(Player player, int slot,
                                   Material material, Color color) {
        ItemStack current = player.getInventory().getArmorContents()[slot];

        if (current != null && current.getType() != Material.AIR) {
            return;
        }

        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }

        switch (slot) {
            case 0: player.getInventory().setBoots(item); break;
            case 1: player.getInventory().setLeggings(item); break;
            case 2: player.getInventory().setChestplate(item); break;
            case 3: player.getInventory().setHelmet(item); break;
        }
    }

    private String getTeamName(Player player) {
        try {
            Class<?> api = Class.forName("com.andrei1058.bedwars.api.BedWars");
            Object apiInstance = api.getMethod("getAPI").invoke(null);
            Object arenaUtil = apiInstance.getClass()
                    .getMethod("getArenaUtil").invoke(apiInstance);
            Object arena = arenaUtil.getClass()
                    .getMethod("getArenaByPlayer", Player.class)
                    .invoke(arenaUtil, player);

            if (arena == null) return null;

            Object team = arena.getClass()
                    .getMethod("getTeam", Player.class).invoke(arena, player);

            if (team == null) return null;

            Object name = team.getClass().getMethod("getName").invoke(team);
            return name == null ? null : name.toString();

        } catch (Exception e) {
            getLogger().fine("Could not determine team for " + player.getName()
                    + ": " + e.getMessage());
            return null;
        }
    }


private Color getTeamColor(String team) {
    String name = team.toLowerCase(Locale.ROOT)
            .replace("_", "")
            .replace(" ", "");

    switch (name) {
        case "blue": return Color.BLUE;
        case "aqua": return Color.AQUA;
        case "yellow": return Color.YELLOW;
        case "pink": return Color.FUCHSIA;
        case "white": return Color.WHITE;
        case "red": return Color.RED;
        case "gray":
        case "grey":
        case "lightgray":
        case "lightgrey": return Color.GRAY;
        case "green": return Color.GREEN;
        default: return null;
    }
}
}    
        
    


