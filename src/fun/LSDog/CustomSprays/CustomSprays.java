package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.commands.CommandCustomSprays;
import fun.LSDog.CustomSprays.commands.CommandSpray;
import fun.LSDog.CustomSprays.data.DataManager;
import fun.LSDog.CustomSprays.map.MapViewId;
import fun.LSDog.CustomSprays.metrics.Metrics;
import fun.LSDog.CustomSprays.utils.MapColors;
import fun.LSDog.CustomSprays.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomSprays extends JavaPlugin {

    public static CustomSprays instant;
    public CustomSprays() { instant = this; }

    public static String prefix = "§3§lCustomSprays >>§r ";
    private static String version = null;
    private static int subVer = 0;

    public File config = new File(getDataFolder() + File.separator + "config.yml");
    public File playerDataFolder = new File(getDataFolder() + File.separator + "playerData");

    public static String latestVersion = null;

    @Override
    public void onDisable() {
        try { // ↓ SpraysManager.removeAllSpray();
            Class.forName("fun.LSDog.CustomSprays.spray.SpraysManager").getMethod("removeAllSpray").invoke(null);
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
            if (YamlConfiguration.loadConfiguration(config).getDouble("configVersion") < 1.6) {
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

        // 检测条件并启用 双击F 喷漆
        if (getSubVer() >= 9 && getConfig().getBoolean("F_spray")) {
            Bukkit.getPluginManager().registerEvents(new Events(), this);
            log("§8[F_spray] enabled.");
        }

        // 1.13 及以上 MapView 支持int
        if (getSubVer() >= 13) {
            MapViewId.setNumbers(-2048_000_000,-2048_000_999);
        }

        // 信息统计
        // https://bstats.org/plugin/bukkit/CustomSprays/13633
        new Metrics(this, 13633);

        if (getConfig().getBoolean("check_update")) Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
           String nVersion = CustomSprays.instant.getDescription().getVersion();
           String lVersion = UpdateChecker.check();
           if (lVersion == null) return;
           if (nVersion.equals(lVersion)) return;
           latestVersion = lVersion;
        });

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            if (!MapColors.isColorPaletteAvailable()) {
                log("Loading Color Palette");
                if (!MapColors.loadColorPalette()) {
                    MapColors.calculateColorPalette();
                } else {
                    log("Color Palette Loaded! :" + MapColors.isColorPaletteAvailable());
                }
            }
        });

        log("§eCustomSprays§r Enabled! plugin by §b§lLSDog§r."+" §8(Running on "+getMcVer()+")");
    }

    /**
     * 返回 NMS 版本 (例如 v1_12_R1)
     */
    public static String getMcVer() {
        return version == null ? version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] : version;
    }

    /**
     * 返回版本号的第二项<br>
     * v1_12_R1 -> 12
     */
    public static int getSubVer() {
        return subVer == 0 ? subVer = Integer.parseInt(getMcVer().split("_")[1]) : subVer;
    }

    public static void log(Object object) {
        Bukkit.getConsoleSender().sendMessage("[CustomSprays] "+object);
    }

    public static void debug(Object object) {
        if (DataManager.debug) Bukkit.getConsoleSender().sendMessage("[CustomSprays]§c[DEBUG]§r "+object);
    }

}
