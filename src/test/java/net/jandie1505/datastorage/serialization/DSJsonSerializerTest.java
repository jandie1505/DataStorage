package net.jandie1505.datastorage.serialization;

import net.jandie1505.datastorage.DataStorage;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DSJsonSerializerTest {

    @Test
    void serializeTopLevelValues() {
        DataStorage ds = new DataStorage();
        ds.set("name", "test");
        ds.set("port", 25565);

        JSONObject json = DSJsonSerializer.serialize(ds);
        assertEquals("test", json.getString("name"));
        assertEquals(25565, json.getInt("port"));
    }

    @Test
    void serializeNestsSections() {
        DataStorage ds = new DataStorage();
        ds.set("server.port", 25565);
        ds.set("server.limits.max", 100);

        JSONObject json = DSJsonSerializer.serialize(ds);
        assertTrue(json.get("server") instanceof JSONObject);
        assertEquals(25565, json.getJSONObject("server").getInt("port"));
        assertEquals(100, json.getJSONObject("server").getJSONObject("limits").getInt("max"));
    }

    @Test
    void roundTripPreservesFlatStructure() {
        DataStorage ds = new DataStorage();
        ds.set("name", "test");
        ds.set("enabled", true);
        ds.set("server.port", 25565);
        ds.set("server.motd", "hello");
        ds.set("server.limits.max", 100);

        DataStorage restored = DSJsonSerializer.deserialize(DSJsonSerializer.serialize(ds));
        assertEquals(ds.asMap(), restored.asMap());
    }

    @Test
    void keyUsedAsValueAndSectionThrows() {
        DataStorage ds = new DataStorage();
        ds.set("a", "value");
        ds.set("a.b", "x");

        assertThrows(IllegalStateException.class, () -> DSJsonSerializer.serialize(ds));
    }

    @Test
    void deserializeFlattensNestedObjects() {
        JSONObject json = new JSONObject();
        json.put("name", "test");
        json.put("server", new JSONObject().put("port", 25565));

        DataStorage ds = DSJsonSerializer.deserialize(json);
        assertEquals("test", ds.get("name"));
        assertEquals(25565, ds.get("server.port"));
    }

    @Test
    void saveAndLoadRoundTrip(@TempDir Path dir) throws IOException {
        DataStorage ds = new DataStorage();
        ds.set("name", "test");
        ds.set("server.port", 25565);

        Path file = dir.resolve("config.json");
        DSJsonSerializer.saveConfig(ds, file, 2);

        DataStorage loaded = DSJsonSerializer.loadConfig(file);
        assertNotNull(loaded);
        assertEquals(ds.asMap(), loaded.asMap());
    }

    @Test
    void loadConfigReturnsNullForMissingFile(@TempDir Path dir) throws IOException {
        assertNull(DSJsonSerializer.loadConfig(dir.resolve("does-not-exist.json")));
    }
}