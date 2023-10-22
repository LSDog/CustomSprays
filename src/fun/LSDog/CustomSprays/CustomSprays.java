package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.commands.CommandCustomSprays;
import fun.LSDog.CustomSprays.commands.CommandSpray;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.listeners.ListenerBasic;
import fun.LSDog.CustomSprays.listeners.ListenerBasicNew;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.utils.MapColors;
import fun.LSDog.CustomSprays.utils.NMS;
import fun.LSDog.CustomSprays.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomSprays extends JavaPlugin {

    public static final double LEAST_CONFIG_VERSION = 1.71;
    public static CustomSprays instance;

    public static String prefix = "§3§lCustomSprays >>§r ";

    public File config = new File(getDataFolder() + File.separator + "config.yml");
    public File playerDataFolder = new File(getDataFolder() + File.separator + "playerData");
    public CustomSprays() { instance = this; }

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
        log("CustomSprays disabled.");
    }

    @Override
    public void onEnable() {

        if (!config.exists()) {
            saveDefaultConfig();
        } else {
            //TODO 每次更迭配置版本的时候别忘了改这里！！
            if (YamlConfiguration.loadConfiguration(config).getDouble("configVersion") < LEAST_CONFIG_VERSION) {
                log("\n\n\n\n\n\n\n=====================\n");
                log("| 检测到不支持的配置！请删除 config.yml 重新配置！");
                log("| Unsupported config detected! please delete config.yml and re-config me! \n");
                log("| 检测到不支持的配置！请删除 config.yml 重新配置！");
                log("| Unsupported config detected! please delete config.yml and re-config me! \n");
                log("| 检测到不支持的配置！请删除 config.yml 重新配置！");
                log("| Unsupported config detected! please delete config.yml and re-config me! \n");
                log("=====================\n\n");
                Bukkit.shutdown();
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

        Bukkit.getPluginManager().registerEvents(new ListenerBasic(), this);

        // 检测条件并启用 双击F 喷漆
        if (NMS.getSubVer() >= 9 && getConfig().getBoolean("F_spray")) {
            Bukkit.getPluginManager().registerEvents(new ListenerBasicNew(), this);
            log("§8[F_spray] enabled.");
        }

        // 1.13 及以上 MapView 支持int
        if (NMS.getSubVer() >= 13) {
            MapViewId.setIdRange(-2048_000_000,-2048_000_999);
            MapViewId.shortViewId = -2048_001_000;
        }

        // 信息统计
        // https://bstats.org/plugin/bukkit/CustomSprays/13633
        new Metrics(this, 13633);

        if (getConfig().getBoolean("check_update")) Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
           String pluginVersion = CustomSprays.instance.getDescription().getVersion();
           String latestVersion = UpdateChecker.checkGithub();
           if (latestVersion == null) latestVersion = UpdateChecker.checkGitee();
           if (latestVersion == null) return;
           if (pluginVersion.equals(latestVersion)) return;
           CustomSprays.latestVersion = latestVersion;
        });

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
