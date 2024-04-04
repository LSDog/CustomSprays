package fun.LSDog.CustomSprays.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;

public class VaultChecker {

    private static boolean haveVaultClass;
    private static Class<?> Economy;
    private static Method Economy_getBalance;
    private static Method Economy_withdrawPlayer;
    private static Method EconomyResponse_transactionSuccess;

    private static RegisteredServiceProvider<?> rsp;

    static {
        reload();
    }

    public static void reload() {
        try {
            Economy = Class.forName("net.milkbowl.vault.economy.Economy");
            Class<?> economyResponse = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
            Economy_getBalance = Economy.getMethod("getBalance", OfflinePlayer.class);
            Economy_withdrawPlayer = Economy.getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            EconomyResponse_transactionSuccess = economyResponse.getMethod("transactionSuccess");
            haveVaultClass = true;
        } catch (ReflectiveOperationException e) {
            haveVaultClass = false;
        }
    }

    public static boolean isVaultEnabled() {
        if (!haveVaultClass) return false;
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return false;
        if (rsp == null) rsp = Bukkit.getServicesManager().getRegistration(Economy);
        return rsp != null;
    }

    public static boolean costMoney(Player player, double cost) {
        try {
            if (!isVaultEnabled()) return false;
            Object eco = rsp.getProvider();
            if ((double) Economy_getBalance.invoke(eco, player) < cost) return false;
            return (boolean) EconomyResponse_transactionSuccess.invoke(Economy_withdrawPlayer.invoke(eco, player, cost));
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

}
