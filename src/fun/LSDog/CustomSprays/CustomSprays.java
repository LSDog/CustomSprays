package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.commands.CommandCustomSprays;
import fun.LSDog.CustomSprays.commands.CommandSpray;
import fun.LSDog.CustomSprays.utils.Data;
import fun.LSDog.CustomSprays.utils.SprayUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CustomSprays extends JavaPlugin {

    public static CustomSprays instant;
    public CustomSprays() { instant = this; }

    public static String prefix = "§6CustomSprays >>§r ";
    private static String version = null;

    public File config = new File(getDataFolder() + File.separator + "config.yml");
    public File pluginData = new File(getDataFolder() + File.separator + "imageData.yml");

    @Override
    public void onEnable() {
        if (!config.exists()) {
            saveResource("config.yml", false);
        }
        if (!pluginData.exists()) {
            saveResource("imageData.yml", false);
        }
        if (getConfig().getBoolean("use_MySQL")) {
            Data.createTableIfNotExist(SprayUtils.getConnection());
        }
        prefix = getConfig().getString("custom_prefix");
        getCommand("customsprays").setExecutor(new CommandCustomSprays());
        getCommand("spray").setExecutor(new CommandSpray());
        log("CustomSprays Enabled! plugin by §b§lLSDog§r.");
        log("Running on "+getVersion());
    }

    @Override
    public void onDisable() {
        // SprayManager.destroyAllSpray();
        try {
            Class.forName("fun.LSDog.CustomSprays.manager.SprayManager").getMethod("destroyAllSpray").invoke(null);
        } catch (Exception ignored) {
        }
        log("CustomSprays disabled.");
    }

    public static String getVersion() {
        return version == null ? version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] : version;
    }

    public static void log(String string) {
        Bukkit.getConsoleSender().sendMessage("[CustomSprays] "+string);
    }

    public static void debug(String string) {
        Bukkit.getConsoleSender().sendMessage("[CustomSprays]§c[DEBUG]§r "+string);
    }

}
