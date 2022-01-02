package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.Data.DataManager;
import fun.LSDog.CustomSprays.commands.CommandCustomSprays;
import fun.LSDog.CustomSprays.commands.CommandSpray;
import fun.LSDog.CustomSprays.events.DoubleFEvent;
import fun.LSDog.CustomSprays.map.MapViewId;
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
    public File pluginData = new File(getDataFolder() + File.separator + "imageData.yml");

    @Override
    public void onEnable() {

        if (!config.exists()) {
            saveDefaultConfig();
        } else {
            /*每次更迭版本的时候别忘了改这里！！*/
            if (YamlConfiguration.loadConfiguration(config).getDouble("configVersion") < 1.4) {
                log("\n\n\n\n\n\n\n=====================\n");
                log("| 检测到不支持的配置！请删除 config.yml 重新配置！");
                log("| Unsupported config detected! please delete config.yml and re-config me!");
                log("\n=====================\n\n\n\n\n\n\n");
                Bukkit.shutdown();
                return;
            }
        }

        try {
            getConfig().load(config);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        prefix = getConfig().getString("msg_prefix");
        DataManager.initialize(getConfig().getString("storage"));
        DataManager.debug = getConfig().getBoolean("debug");
        DataManager.urlRegex = getConfig().getString("url_regex");
        DataManager.usePapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        getCommand("customsprays").setExecutor(new CommandCustomSprays());
        getCommand("spray").setExecutor(new CommandSpray());

        // 检测条件并启用 双击F 喷漆
        if (getSubVer() >= 9 && getConfig().getBoolean("F_spray")) {
            Bukkit.getPluginManager().registerEvents(new DoubleFEvent(), this);
            log("§8[F_spray] enabled.");
        }

        // 1.13 及以上 支持int
        if (getSubVer() >= 13) {
            //MapViewId.setNumbers(2147483347,2147483645);
            MapViewId.setNumbers(-2147483645,-2147483347);
        }

        // In progress
        // Bukkit.getPluginManager().registerEvents(new SwapListener(), this);

        // 信息统计
        // https://bstats.org/plugin/bukkit/CustomSprays/13633
        new Metrics(this, 13633);

        log("§eCustomSprays§r Enabled! plugin by §b§lLSDog§r."+" §8(Running on "+getMcVer()+")");
    }

    @Override
    public void onDisable() {
        try {
            // ↓ SprayManager.destroyAllSpray();
            Class.forName("fun.LSDog.CustomSprays.manager.SprayManager").getMethod("destroyAllSpray").invoke(null);
        } catch (Exception ignored) {
        }
        log("CustomSprays disabled.");
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
