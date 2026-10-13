
package net.tropolia.woodensword;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Locale;

public final class TropoliaArmor extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
            getLogger().severe("BedWars1058 not found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("TropoliaArmor enabled!");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()) {
                equipTeamArmor(player);
            }
        }, 2L);
    }

    private void equipTeamArmor(Player player) {
        String team = getTeamName(player);

        if (team == null || team.isEmpty()) {
            return;
        }

        Color color = getTeamColor(team);
        if (color == null) {
            return;
        }

        player.getInventory().setHelmet(makeArmor(Material.LEATHER_HELMET, color));
        player.getInventory().setChestplate(makeArmor(Material.LEATHER_CHESTPLATE, color));
        player.getInventory().setLeggings(makeArmor(Material.LEATHER_LEGGINGS, color));
        player.getInventory().setBoots(makeArmor(Material.LEATHER_BOOTS, color));
    }

    private ItemStack makeArmor(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }

        return item;
    }

    private String getTeamName(Player player) {
        try {
            Class<?> api = Class.forName("com.andrei1058.bedwars.api.BedWars");
            Method getApi = api.getMethod("getAPI");
            Object apiInstance = getApi.invoke(null);

            Method getArenaUtil = apiInstance.getClass().getMethod("getArenaUtil");
            Object arenaUtil = getArenaUtil.invoke(apiInstance);

            Method getArena = arenaUtil.getClass().getMethod("getArenaByPlayer", Player.class);
            Object arena = getArena.invoke(arenaUtil, player);

            if (arena == null) return null;

            Method getTeam = arena.getClass().getMethod("getTeam", Player.class);
            Object team = getTeam.invoke(arena, player);

            if (team == null) return null;

            Method getName = team.getClass().getMethod("getName");
            Object name = getName.invoke(team);

            return name == null ? null : name.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private Color getTeamColor(String team) {
        String name = team.toLowerCase(Locale.ROOT).replace("_", "").replace(" ", "");

        switch (name) {
            case "blue": return Color.BLUE;
            case "aqua": return Color.AQUA;
            case "yellow": return Color.YELLOW;
            case "lime": return Color.LIME;
            case "pink": return Color.FUCHSIA;
            case "white": return Color.WHITE;
            case "red": return Color.RED;
            case "lightgray":
            case "lightgrey": return Color.SILVER;
            default: return null;
        }
    }
}

