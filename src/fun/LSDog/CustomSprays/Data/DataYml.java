package fun.LSDog.CustomSprays.Data;

import fun.LSDog.CustomSprays.CustomSprays;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Base64;

public class DataYml implements IData {

    public DataYml() throws IOException {
        if (!CustomSprays.instant.playerDataFolder.exists()) {
            if (!CustomSprays.instant.playerDataFolder.mkdirs()) {
                throw new IOException("CustomSprays: can not create data folder!");
            }
        }
    }

    /**
     * 都是384*384大小
     */
    @Override
    public int saveImageBytes(Player player, byte[] imgBytes) {
        File dataFile;
        try {
            dataFile = getOrCreateData(player);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        byte[] data = DataManager.compressBytes(imgBytes); /* 压缩bytes */
        dataConfig.set("name", player.getName());
        dataConfig.set("image", Base64.getEncoder().encodeToString(data));
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return data.length;
    }

    /**
     * 都是384*384大小
     */
    public byte[] getImageBytes(Player player) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(getDataPath(player)), StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("image: ")) {
                    return DataManager.decompressBytes(Base64.getDecoder().decode(line.substring("image: ".length())));
                }
            }
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setCopyAllowed(Player player, boolean flag) {
        String name = player.getName();
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(getOrCreateData(player).getAbsolutePath()), StandardCharsets.ISO_8859_1)) {
            String line;
            boolean haveName = false, haveBoolean = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("name: ")) {
                    haveName = true;
                    line = "name: "+name;
                } else if (line.startsWith("allow_copy: ")) {
                    haveBoolean = true;
                    line = "allow_copy: "+flag;
                }
                builder.append(line).append("\n");
            }
            reader.close();
            if (!haveName) builder.append("name: ").append(name).append("\n");
            if (!haveBoolean) builder.append("allow_copy: ").append(flag).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream out = new FileOutputStream(getDataPath(player))) {
            out.write(builder.toString().getBytes(StandardCharsets.ISO_8859_1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean getCopyAllowed(Player player) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(getDataPath(player)), StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("allow_copy: ")) {
                    return Boolean.parseBoolean(line.substring("allow_copy: ".length()));
                }
            }
        } catch (NoSuchFileException e) {
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String getDataPath(Player player) {
        return CustomSprays.instant.playerDataFolder.getAbsolutePath() + File.separator + player.getUniqueId() + ".yml";
    }

    private static File getOrCreateData(Player player) throws IOException {
        File dataFile = new File(getDataPath(player));
        if (!dataFile.exists()) {
            if (!dataFile.createNewFile()) throw new IOException("CustomSprays: Can not create data file!");
        }
        return dataFile;
    }

}
