package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.command.CommandCustomSprays;
import fun.LSDog.CustomSprays.command.CommandSpray;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.listener.*;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.util.MapColors;
import fun.LSDog.CustomSprays.util.NMS;
import fun.LSDog.CustomSprays.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomSprays extends JavaPlugin {

    public static final double CONFIG_VERSION = 1.8;
    public static CustomSprays plugin;

    public static String prefix = "§3§lCustomSprays >>§r ";

    public File config = new File(getDataFolder() + File.separator + "config.yml");
    public File playerDataFolder = new File(getDataFolder() + File.separator + "playerData");
    public CustomSprays() { plugin = this; }

    public static String latestVersion = null;

    @Override
    public void onDisable() {
        try { // ↓ SpraysManager.removeAllSpray();
            Class.forName("fun.LSDog.CustomSprays.spray.SprayManager").getMethod("removeAllSpray").invoke(null);
        } catch (Exception ignored) { }
        // cancel async tasks
        Bukkit.getScheduler().getActiveWorkers().forEach(bukkitWorker -> {
            if (bukkitWorker.getOwner().getName().equals("CustomSprays")) bukkitWorker.getThread().interrupt();
        });
        if (NMS.getSubVer() >= 8) getServer().getOnlinePlayers().forEach(PacketListener::removePlayer);
        else getServer().getOnlinePlayers().forEach(PacketListener7::removePlayer);
        log("CustomSprays disabled.");
    }

    @Override
    public void onEnable() {

        if (!config.exists()) {
            saveDefaultConfig();
        } else {
            //TODO 每次更迭配置版本的时候别忘了改这里！！
            if (YamlConfiguration.loadConfiguration(config).getDouble("configVersion") < CONFIG_VERSION) {
                log("\n\n\n" +
                        "=====================\n" +
                        "| 检测到不支持的配置！请删除 config.yml 并重启服务器！\n" +
                        "| Unsupported config detected! Please delete config.yml and restart server!\n" +
                        "=====================\n\n\n");
                return;
            }
        }

        try {
            getConfig().load(config);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        DataManager.initialize(getConfig().getString("storage"));

        getCommand("customsprays").setExecutor(new CommandCustomSprays());
        getCommand("spray").setExecutor(new CommandSpray());

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

        // 检测条件并启用 双击F 喷漆
        if (NMS.getSubVer() >= 9 && getConfig().getBoolean("F_spray")) {
            Bukkit.getPluginManager().registerEvents(new EventListenerNew(), this);
            log("§8[F_spray] enabled.");
        }

        // 1.13 及以上 MapView 支持int
        if (NMS.getSubVer() >= 13) {
            MapViewId.setIdRange(-2048_000_000,-2048_000_999);
            MapViewId.sprayViewId = -2048_001_000;
        }

        // 检测已在服务器中的玩家
        getServer().getOnlinePlayers().forEach(EventListener::playerJoin);

        // 信息统计
        // https://bstats.org/plugin/bukkit/CustomSprays/13633
        new Metrics(this, 13633);

        // 检查更新
        if (getConfig().getBoolean("check_update")) Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
           String pluginVersion = CustomSprays.plugin.getDescription().getVersion();
           String latestVersion = UpdateChecker.checkGithub();
           if (latestVersion == null) latestVersion = UpdateChecker.checkGitee();
           if (latestVersion == null) return;
           int compareResult = UpdateChecker.compareVersions(latestVersion, pluginVersion);
           if (compareResult > 0) CustomSprays.latestVersion = latestVersion;
        });

        // 计算颜色板
        if (getConfig().getBoolean("better_color") && NMS.getSubVer() >= 8) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                log("Loading Color Palette");
                if (!MapColors.loadColorPalette()) {
                    MapColors.calculateColorPalette();
                } else {
                    log("Color Palette Loaded! current state: " + MapColors.isColorPaletteAvailable());
                }
            });
        }

        log("§eCustomSprays§r Enabled! plugin by §b§lLSDog§r."+" §8(Running on "+ NMS.getMcVer()+")");
    }

    public static void log(Object object) {
        Bukkit.getConsoleSender().sendMessage("[CustomSprays] "+object);
    }

    public static void debug(Object object) {
        if (DataManager.debug) Bukkit.getConsoleSender().sendMessage("[CustomSprays]§c[DEBUG]§r "+object);
    }

}
