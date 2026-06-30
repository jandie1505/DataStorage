package net.jandie1505.datastorage.serialization;

import net.jandie1505.datastorage.DataStorage;
import net.jandie1505.datastorage.IDataStorage;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Serializes/deserializes a DataStorage to/from JSON.
 */
public final class DSJsonSerializer {

    private DSJsonSerializer() {}

    /**
     * Serializes a DataStorage to a Bukkit YamlConfiguration.
     * @param storage storage
     * @return yaml configuration
     */
    public static JSONObject serialize(@NotNull IDataStorage storage) {
        JSONObject config = new JSONObject();

        // Add values
        for (Map.Entry<String, Object> entry : storage.getTopLevelEntryStorage().entrySet()) {
            config.put(entry.getKey(), entry.getValue());
        }

        // Sections
        for (Map.Entry<String, IDataStorage> entry : storage.getSections().entrySet()) {

            if (storage.get(entry.getKey()) != null) {
                throw new IllegalStateException("Key " +  entry.getKey() + " is used as value key and section key for the same time. JSON does not support that.");
            }

            JSONObject subsection = serialize(entry.getValue());
            config.put(entry.getKey(), subsection);

        }

        // Return
        return config;
    }

    /**
     * Deserializes a DataStorage from a Bukkit YamlConfiguration.
     * @param config config
     * @return storage
     */
    public static DataStorage deserialize(@NotNull JSONObject config) {
        DataStorage storage = new DataStorage();

        for (String key : config.keySet()) {
            Object value = config.get(key);

            // JSON Objects need to be merged as subsections
            if (value instanceof JSONObject jsonObject) {
                storage.mergeSection(key, deserialize(jsonObject));
            } else {
                storage.set(key, value);
            }

        }

        return storage;
    }

    /**
     * Loads a JSONObject file and deserializes it to a DataStorage.
     * @param path path to the config file
     * @return storage
     * @throws IOException file error
     * @throws JSONException malformed config
     */
    public static DataStorage loadConfig(@NotNull Path path) throws IOException, JSONException {
        if (!Files.exists(path)) return null;

        String content = Files.readString(path); // Java 11+
        JSONObject config = new JSONObject(content);
        return deserialize(config);
    }

    /**
     * Loads a JSONObject file and deserializes it to a DataStorage.
     * @param file path to the config file
     * @return storage
     * @throws IOException file error
     * @throws JSONException malformed config
     */
    public static DataStorage loadConfig(@NotNull File file) throws IOException, JSONException {
        return loadConfig(file.toPath());
    }

    /**
     * Deserializes a DataStorage and saves it as a JSONObject.
     * @param storage storage
     * @param path file path
     * @param indentFactor See {@link JSONObject#toString(int)}.
     * @throws IOException file error
     */
    public static void saveConfig(@NotNull DataStorage storage, @NotNull Path path, int indentFactor) throws IOException {
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        JSONObject config = serialize(storage);

        Files.writeString(
                path,
                config.toString(indentFactor),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    /**
     * Deserializes a DataStorage and saves it as a JSONObject.
     * @param storage storage
     * @param file path to the config file
     * @param indentFactor See {@link JSONObject#toString(int)}.
     * @throws IOException file error
     */
    public static void saveConfig(@NotNull DataStorage storage, @NotNull File file, int indentFactor) throws IOException {
        saveConfig(storage, file.toPath(), indentFactor);
    }

}
