package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.commands.CommandCustomSprays;
import fun.LSDog.CustomSprays.commands.CommandSpray;
import fun.LSDog.CustomSprays.events.DoubleFEvent;
import fun.LSDog.CustomSprays.utils.Data;
import fun.LSDog.CustomSprays.utils.SprayUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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

        if (!config.exists()) saveResource("config.yml", false);
        if (!pluginData.exists()) saveResource("imageData.yml", false);
        if (getConfig().getBoolean("use_MySQL")) Data.createTableIfNotExist(SprayUtils.getConnection());

        prefix = getConfig().getString("custom_prefix");
        Data.usePapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        getCommand("customsprays").setExecutor(new CommandCustomSprays());
        getCommand("spray").setExecutor(new CommandSpray());

        if (getSubVer() > 8 && getConfig().getBoolean("F_spray")) {
            Bukkit.getPluginManager().registerEvents(new DoubleFEvent(), this);
            log("§8F_spray enabled.");
        }

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

    public static void log(String string) {
        Bukkit.getConsoleSender().sendMessage("[CustomSprays] "+string);
    }

    public static void debug(String string) {
        Bukkit.getConsoleSender().sendMessage("[CustomSprays]§c[DEBUG]§r "+string);
    }

}
