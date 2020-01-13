package no.kh498.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Various utility functions that revolve around {@link ConfigurationSection}
 * <p>
 * Taken from <a href=https://github.com/kh498/BukkitUtil>BukkitUtil</a> commit <a href="https://github.com/kh498/BukkitUtil/commit/9c54340c3b2af56629d9e9467c454f2d71475161">9c54340</a>
 *
 * @author Elg
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ConfigUtil {

    public static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    /**
     * A warning will be printed if the given YAML is invalid.
     *
     * @return A FileConfiguration from the given file or {@code null} if invalid yaml or no file found found
     */
    @Nullable
    public static FileConfiguration getYaml(@NotNull File file) {
        try {
            YamlConfiguration conf = new YamlConfiguration();
            conf.load(file);
            return conf;
        } catch (InvalidConfigurationException e) {
            logger.warn("YAML in file '{}' is invalid.\n{}", file, e.getMessage());
        } catch (FileNotFoundException e) {
            logger.debug("Failed to find given file '{}'", file.getPath());
        } catch (IOException e) {
            logger.debug("An IO exception occurred when trying to load file '{}'", file.getPath());
        }
        return null;
    }

    /**
     * A warning will be printed if the given YAML is invalid.
     *
     * @return A FileConfiguration of the given file or the default argument if the YAML in the file is invalid or an
     * {@link IOException} occurred
     */
    @NotNull
    public static FileConfiguration getYamlOrDefault(@NotNull File file, @NotNull FileConfiguration def) {
        FileConfiguration conf = getYaml(file);
        return conf == null ? def : conf;
    }

    public static void saveYaml(@Nullable FileConfiguration conf, @Nullable File file) {
        if (file == null || conf == null) {
            logger.error("Failed to save Yaml. Got invalid parameters: conf = '{}' file = = '{}'", conf, file);
            return;
        }
        try {
            conf.save(file);
        } catch (IOException e) {
            logger.error("Failed to save file '{}' to '{}'", file.getName(), file.getPath());
            e.printStackTrace();
        }
    }

    /**
     * @param conf The config to load the values from
     * @param path The path to section to get
     * @return A map of all nodes at the given path, if an error occurred an empty map will be returned
     */
    @NotNull
    public static Map<String, Object> getMapSection(@NotNull ConfigurationSection conf, @NotNull String path) {
        return getMapSection(conf.get(path));
    }

    /**
     * @param obj The object to get the map from. NOTE: this must be a {@link ConfigurationSection} or a
     *            {@link Map}{@code <String, Object>} in order to return something else than an empty map
     * @return A map of all nodes at the given path, if an error occurred an empty map will be returned
     */
    @NotNull
    public static Map<String, Object> getMapSection(@Nullable Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        try {
            ConfigurationSection section = (ConfigurationSection) obj;
            return section.getValues(true);
        } catch (ClassCastException e1) {
            try {
                //noinspection unchecked
                return (Map<String, Object>) obj;
            } catch (ClassCastException e) {
                return new HashMap<>();
            }
        }
    }

    @NotNull
    public static ConfigurationSection getSection(@NotNull Object obj) {
        if (obj instanceof ConfigurationSection) {
            return (ConfigurationSection) obj;
        }
        return getSectionFromMap(getMapSection(obj));
    }

    /**
     * Convert a map into a configuration section
     */
    @NotNull
    public static ConfigurationSection getSectionFromMap(@NotNull Map<String, Object> map) {
        YamlConfiguration conf = new YamlConfiguration();
        map.forEach((path, obj) -> {
            if (obj instanceof Map) {
                //recursively find sections
                conf.set(path, getSectionFromMap(getMapSection(obj)));
            } else {
                conf.set(path, obj);
            }
        });
        return conf;
    }

    /**
     * @param conf The conf to convert
     * @return A YamlConfiguration version of the given conf
     */
    @NotNull
    public static FileConfiguration toFileConf(@NotNull ConfigurationSection conf) {
        FileConfiguration fileConf = new YamlConfiguration();
        for (Map.Entry<String, Object> entry : getMapSection(conf, "").entrySet()) {
            fileConf.set(entry.getKey(), entry.getValue());
        }
        return fileConf;
    }

    /**
     * @param conf The config to convert
     * @return YAML string version of the given config
     */
    public static String saveToString(@NotNull ConfigurationSection conf) {
        return toFileConf(conf).saveToString();
    }
}