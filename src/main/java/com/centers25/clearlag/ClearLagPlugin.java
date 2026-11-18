package com.centers25.clearlag;

import com.centers25.core.logging.PluginLogger;
import com.centers25.core.logging.PluginLogs;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ClearLagPlugin extends JavaPlugin {
    private static final String LEGACY_FOLDER = "ClearLag";
    private static final long DEFAULT_INTERVAL_SECONDS = 300L;
    private static final long TICKS_PER_SECOND = 20L;

    private PluginLogger log;

    @Override
    public void onEnable() {
        log = PluginLogs.get(this);
        migrateLegacyFolder();
        saveDefaultConfig();

        long intervalSeconds = getConfig().getLong("interval-seconds", DEFAULT_INTERVAL_SECONDS);
        if (intervalSeconds <= 0L) {
            intervalSeconds = DEFAULT_INTERVAL_SECONDS;
        }

        long intervalTicks = secondsToTicks(intervalSeconds);
        Bukkit.getScheduler().runTaskTimer(this, this::removeConfiguredItems, intervalTicks, intervalTicks);
        log.info("ClearLag enabled; clearing items every " + intervalSeconds + " second(s).");
    }

    private void migrateLegacyFolder() {
        File legacy = new File(getDataFolder().getParentFile(), LEGACY_FOLDER);
        if (!legacy.isDirectory() || getDataFolder().exists()) {
            return;
        }
        if (legacy.renameTo(getDataFolder())) {
            log.info("Moved existing data from plugins/" + LEGACY_FOLDER + " to plugins/" + getDataFolder().getName() + ".");
        } else {
            log.warn("Could not move plugins/" + LEGACY_FOLDER + " to plugins/" + getDataFolder().getName() + ".");
        }
    }

    private long secondsToTicks(long seconds) {
        if (seconds > Long.MAX_VALUE / TICKS_PER_SECOND) {
            return Long.MAX_VALUE;
        }
        return seconds * TICKS_PER_SECOND;
    }

    private void removeConfiguredItems() {
        int removed = 0;
        for (String worldName : getConfig().getStringList("worlds")) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                continue;
            }

            for (Item item : world.getEntitiesByClass(Item.class)) {
                item.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("Removed " + removed + " item(s).");
        }
    }
}
