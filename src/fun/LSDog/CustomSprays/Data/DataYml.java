package fun.LSDog.CustomSprays.Data;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Base64;

public class DataYml implements IData {

    private FileConfiguration config;

    public DataYml() {
        reloadConfig();
    }

    @Override
    public int saveImageBytes(Player player, byte[] imgBytes) {
        byte[] data = DataManager.compressBytes(imgBytes);
        String uuid = player.getUniqueId().toString();
        config.set(uuid+".name", player.getName());
        config.set(uuid+".image", Base64.getEncoder().encodeToString(data));
        try {
            config.save(CustomSprays.instant.pluginData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
        return data.length;
    }

    @Override
    public byte[] getImageBytes(Player player) {
        String originalString = config.getString(player.getUniqueId().toString() + ".image");
        if (originalString == null) return null;
        return DataManager.decompressBytes(Base64.getDecoder().decode(originalString));
    }

    private void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(CustomSprays.instant.pluginData);
    }

}
