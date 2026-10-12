package net.tropolia.woodensword;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.UUID;

public final class WoodenSwordHelper extends JavaPlugin implements Listener {
    private static final String[] START_EVENT_CLASSES = {
        "com.andrei1058.bedwars.api.events.gameplay.ArenaStatusChangeEvent",
        "com.andrei1058.bedwars.api.events.gameplay.ArenaStatusChangeEvent",
        "com.andrei1058.bedwars.api.events.ArenaStatusChangeEvent"
    };

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
            getLogger().severe("BedWars1058 not found; disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        hookMatchStart();
        getLogger().info("Enabled. Wooden swords are only added if the player has no sword.");
    }

    @org.bukkit.event.EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(this, () -> {
            if (player.isOnline() && isInBedWarsArena(player)) {
                giveSwordIfNoSword(player);
            }
        });
    }

    private void hookMatchStart() {
        Plugin bedwars = Bukkit.getPluginManager().getPlugin("BedWars1058");
        for (String className : START_EVENT_CLASSES) {
            try {
                Class<?> raw = Class.forName(className, false, bedwars.getClass().getClassLoader());
                if (!Event.class.isAssignableFrom(raw)) continue;
                @SuppressWarnings("unchecked")
                Class<? extends Event> eventClass = (Class<? extends Event>) raw;
                Bukkit.getPluginManager().registerEvent(eventClass, this, EventPriority.MONITOR,
                    (listener, event) -> onArenaStatusChange(event), this, true);
                getLogger().info("Hooked match-start event: " + className);
                return;
            } catch (ClassNotFoundException ignored) {
                // Try another known package name.
            } catch (Throwable ex) {
                getLogger().warning("Could not hook " + className + ": " + ex.getMessage());
            }
        }
        getLogger().warning("Could not find the BedWars arena status event. Respawn handling may still work.");
    }

    private void onArenaStatusChange(Event event) {
        try {
            Object status = call(event, "getNewStatus");
            if (status == null) status = call(event, "getStatus");
            String statusName = String.valueOf(status).toUpperCase();
            if (!(statusName.contains("RUNNING") || statusName.contains("INGAME") ||
                  statusName.contains("IN_GAME") || statusName.contains("PLAYING"))) return;

            Object arena = call(event, "getArena");
            if (arena == null) return;
            Object players = call(arena, "getPlayers");
            if (!(players instanceof Iterable<?> iterable)) return;

            Bukkit.getScheduler().runTask(this, () -> {
                for (Object entry : iterable) {
                    Player player = null;
                    if (entry instanceof Player p) player = p;
                    else if (entry instanceof UUID id) player = Bukkit.getPlayer(id);
                    else if (entry instanceof String name) player = Bukkit.getPlayerExact(name);
                    if (player != null && player.isOnline()) giveSwordIfNoSword(player);
                }
            });
        } catch (Throwable ex) {
            getLogger().warning("Could not handle arena start: " + ex.getMessage());
        }
    }

    private boolean isInBedWarsArena(Player player) {
        try {
            Plugin bedwars = Bukkit.getPluginManager().getPlugin("BedWars1058");
            if (bedwars == null) return false;
            Class<?> apiClass = Class.forName("com.andrei1058.bedwars.api.BedWars", false,
                    bedwars.getClass().getClassLoader());
            Object registration = Bukkit.getServicesManager().getRegistration(apiClass);
            if (registration == null) return false;
            Object api = registration.getClass().getMethod("getProvider").invoke(registration);
            Object arenaUtil = call(api, "getArenaUtil");
            if (arenaUtil == null) return false;
            for (Method method : arenaUtil.getClass().getMethods()) {
                if (method.getName().equals("getArenaByPlayer") && method.getParameterCount() == 1 &&
                    method.getParameterTypes()[0].isAssignableFrom(Player.class)) {
                    return method.invoke(arenaUtil, player) != null;
                }
            }
        } catch (Throwable ignored) { }
        return false;
    }

    private void giveSwordIfNoSword(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().endsWith("_SWORD")) return;
        }
        player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD, 1));
    }

    private Object call(Object target, String methodName) {
        try { return target.getClass().getMethod(methodName).invoke(target); }
        catch (Throwable ignored) { return null; }
    }
}
