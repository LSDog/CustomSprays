package fun.LSDog.CustomSprays;

import fun.LSDog.CustomSprays.Data.DataManager;
import fun.LSDog.CustomSprays.commands.CommandCustomSprays;
import fun.LSDog.CustomSprays.commands.CommandSpray;
import fun.LSDog.CustomSprays.manager.CoolDownManager;
import fun.LSDog.CustomSprays.map.MapViewId;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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

    @Override
    public void onEnable() {

        if (!config.exists()) {
            saveDefaultConfig();
        } else {
            /*每次更迭版本的时候别忘了改这里！！*/
            if (YamlConfiguration.loadConfiguration(config).getDouble("configVersion") < 1.5) {
                System.out.println("\n\n\n\n\n\n\n=====================\n");
                log("| 检测到不支持的配置！请删除 config.yml 重新配置！");
                log("| Unsupported config detected! please delete config.yml and re-config me! \n");
                log("| 检测到不支持的配置！请删除 config.yml 重新配置！");
                log("| Unsupported config detected! please delete config.yml and re-config me! \n");
                log("| 检测到不支持的配置！请删除 config.yml 重新配置！");
                log("| Unsupported config detected! please delete config.yml and re-config me! \n");
                System.out.println("=====================\n\n");
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

        // 1.13 及以上 支持int
        if (getSubVer() >= 13) {
            MapViewId.setNumbers(-2047483645,-2047483047);
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
        try { // ↓ SpraysManager.removeAllSpray();
            Class.forName("fun.LSDog.CustomSprays.manager.SpraysManager").getMethod("removeAllSpray").invoke(null);
        } catch (Exception ignored) { }
        Bukkit.getScheduler().getActiveWorkers().forEach(bukkitWorker -> {
            if (bukkitWorker.getOwner().getName().equals("CustomSprays")) //noinspection deprecation
                bukkitWorker.getThread().stop();
        });
        log("CustomSprays disabled.");
    }


    /**
     * 让玩家喷漆，若玩家进行大喷漆(3*3)却没有权限，则会变为小喷漆(1*1)，默认展示给全服玩家 <br>
     * <b>务必使用 runTaskAsynchronously 异步执行, 否则可能造成卡顿！！</b>
     * @param player 喷漆玩家
     * @param isBigSpray 是否为大型喷漆
     */
    public static void spray(Player player, boolean isBigSpray) {

        // 检测喷漆权限
        if (player.isPermissionSet("CustomSprays.spray") && !player.hasPermission("CustomSprays.spray")) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "NO_PERMISSION"));
            return;
        }
        // 检测禁止的世界
        if (!player.hasPermission("CustomSprays.nodisable") && DataManager.disableWorlds != null && DataManager.disableWorlds.contains(player.getWorld().getName())) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.DISABLED_WORLD"));
            return;
        }
        // 检测CD
        if (!player.hasPermission("CustomSprays.nocd") && CoolDownManager.isSprayCooling(player)) {
            player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "IN_COOLING")+" §7("+CoolDownManager.getSprayCool(player)+")");
            return;
        }


        try {
            // 如果 [不是大喷漆  或者  (是大喷漆却)没有大喷漆权限]
            if (!isBigSpray || (player.isPermissionSet("CustomSprays.bigspray") && !player.hasPermission("CustomSprays.bigspray"))) {

                // 小喷漆
                byte[] bytes = DataManager.get128pxImageBytes(player);
                if (bytes == null) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
                    return;
                }
                Spray spray = new Spray(player, bytes, Bukkit.getOnlinePlayers());
                if (spray.create((long) (CustomSprays.instant.getConfig().getDouble("destroy")*20L))) {
                    CoolDownManager.setSprayCooldown(player,1);
                    CustomSprays.debug("§f§l" + player.getName() + "§b spray §7->§r " + spray.location.getX() + " " + spray.location.getY() + " " + spray.location.getZ());
                }

            } else {

                // 大喷漆
                byte[] bytes = DataManager.get384pxImageBytes(player);
                if (bytes == null) {
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE"));
                    player.sendMessage(CustomSprays.prefix + DataManager.getMsg(player, "SPRAY.NO_IMAGE_TIP"));
                    return;
                }
                Spray spray = new BigSpray(player, bytes, Bukkit.getOnlinePlayers());
                if (spray.create((long) (CustomSprays.instant.getConfig().getDouble("destroy")*20L))) {
                    CoolDownManager.setSprayCooldown(player, CustomSprays.instant.getConfig().getDouble("bigspray_cooldown_multiple"));
                    CustomSprays.debug("§f§l" + player.getName() + "§b spray §7->§r " + spray.location.getX() + " " + spray.location.getY() + " " + spray.location.getZ() + " (big)");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
