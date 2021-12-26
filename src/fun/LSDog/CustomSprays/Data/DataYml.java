package fun.LSDog.CustomSprays.Data;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;

public class DataYml implements IData {

    private final FileConfiguration config;

    public DataYml() {
        this.config = YamlConfiguration.loadConfiguration(CustomSprays.instant.pluginData);
    }

    @Override
    public void saveImageString(Player player, String imageString) {
        String uuid = player.getUniqueId().toString();
        config.set(uuid+".name", player.getName());
        config.set(uuid+".image", imageString);
        try {
            config.save(CustomSprays.instant.pluginData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getImageString(Player player) {
        return config.getString(player.getUniqueId().toString()+".image");
    }

}
