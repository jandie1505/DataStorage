package net.jandie1505.datastorage;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataStorageTest {

    // --- BASIC OPERATIONS ---

    @Test
    void setAndGet() {
        DataStorage ds = new DataStorage();
        ds.set("key", "value");
        assertEquals("value", ds.get("key"));
    }

    @Test
    void getMissingReturnsNull() {
        assertNull(new DataStorage().get("missing"));
    }

    @Test
    void setNullRemovesKey() {
        DataStorage ds = new DataStorage();
        ds.set("key", "value");
        ds.set("key", null);
        assertNull(ds.get("key"));
        assertFalse(ds.asMap().containsKey("key"));
    }

    @Test
    void removeReturnsPreviousValue() {
        DataStorage ds = new DataStorage();
        ds.set("key", 42);
        assertEquals(42, ds.remove("key"));
        assertNull(ds.get("key"));
    }

    @Test
    void clearRemovesEverything() {
        DataStorage ds = new DataStorage();
        ds.set("a", 1);
        ds.set("b", 2);
        ds.clear();
        assertTrue(ds.asMap().isEmpty());
    }

    // --- KEY VALIDATION ---

    @Test
    void keyWithSpaceThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DataStorage().set("a b", 1));
    }

    @Test
    void keyWithIllegalCharThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DataStorage().set("a/b", 1));
    }

    @Test
    void emptyKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DataStorage().set("", 1));
    }

    @Test
    void keyWithAllowedSpecialCharsIsAccepted() {
        DataStorage ds = new DataStorage();
        assertDoesNotThrow(() -> ds.set("a.b,c:d;e-f_g", 1));
        assertEquals(1, ds.get("a.b,c:d;e-f_g"));
    }

    // --- VALUE CONVERSION ---

    @Test
    void primitiveAndStringTypesArePreserved() {
        DataStorage ds = new DataStorage();
        ds.set("int", 1);
        ds.set("long", 2L);
        ds.set("double", 3.5d);
        ds.set("float", 4.5f);
        ds.set("bool", true);
        ds.set("string", "s");
        ds.set("byte", (byte) 5);
        ds.set("short", (short) 6);
        ds.set("char", 'c');

        assertEquals(Integer.class, ds.get("int").getClass());
        assertEquals(Long.class, ds.get("long").getClass());
        assertEquals(Double.class, ds.get("double").getClass());
        assertEquals(Float.class, ds.get("float").getClass());
        assertEquals(Boolean.class, ds.get("bool").getClass());
        assertEquals(String.class, ds.get("string").getClass());
        assertEquals(Byte.class, ds.get("byte").getClass());
        assertEquals(Short.class, ds.get("short").getClass());
        assertEquals(Character.class, ds.get("char").getClass());
    }

    @Test
    void unknownTypeIsConvertedToString() {
        DataStorage ds = new DataStorage();
        ds.set("list", List.of(1, 2, 3));
        assertEquals("[1, 2, 3]", ds.get("list"));
    }

    @Test
    void convertObjectStaticMatchesInstanceBehaviour() {
        assertEquals(5, IDataStorage.convertObject(5));
        assertEquals("[1]", IDataStorage.convertObject(List.of(1)));
        // deprecated delegate must behave identically
        assertEquals(IDataStorage.convertObject(5), DataStorage.convertObject(5));
    }

    // --- SECTIONS ---

    @Test
    void dataStorageValueIsMergedAsSection() {
        DataStorage inner = new DataStorage();
        inner.set("a", 1);
        inner.set("b", 2);

        DataStorage ds = new DataStorage();
        ds.set("sec", inner);

        assertEquals(1, ds.get("sec.a"));
        assertEquals(2, ds.get("sec.b"));
        assertNull(ds.get("sec"));
    }

    @Test
    void mapValueIsMergedAsSection() {
        DataStorage ds = new DataStorage();
        ds.set("cfg", Map.of("x", 1));
        assertEquals(1, ds.get("cfg.x"));
    }

    @Test
    void getSectionReturnsStrippedSubsection() {
        DataStorage ds = new DataStorage();
        ds.set("sec.a", 1);
        ds.set("sec.b", 2);
        ds.set("other", 3);

        DataStorage section = ds.getSection("sec");
        assertEquals(Map.of("a", 1, "b", 2), section.asMap());
    }

    @Test
    void getSectionIsIndependentFromParent() {
        DataStorage ds = new DataStorage();
        ds.set("sec.a", 1);

        DataStorage section = ds.getSection("sec");
        section.set("a", 999);

        assertEquals(1, ds.get("sec.a"), "modifying the returned section must not affect the parent");
    }

    @Test
    void mergeSectionWithOverwrite() {
        DataStorage ds = new DataStorage();
        ds.set("s.a", 1);

        DataStorage section = new DataStorage();
        section.set("a", 2);

        ds.mergeSection("s", section, true);
        assertEquals(2, ds.get("s.a"));
    }

    @Test
    void mergeSectionWithoutOverwriteKeepsExisting() {
        DataStorage ds = new DataStorage();
        ds.set("s.a", 1);

        DataStorage section = new DataStorage();
        section.set("a", 2);
        section.set("b", 3);

        ds.mergeSection("s", section, false);
        assertEquals(1, ds.get("s.a"), "existing key must be kept");
        assertEquals(3, ds.get("s.b"), "new key must be added");
    }

    @Test
    void getSectionsReturnsFirstLevelSectionsOnly() {
        DataStorage ds = new DataStorage();
        ds.set("s.a", 1);
        ds.set("t.b", 2);
        ds.set("top", 3);

        Map<String, IDataStorage> sections = ds.getSections();
        assertEquals(Map.of("a", 1), sections.get("s").asMap());
        assertEquals(Map.of("b", 2), sections.get("t").asMap());
        assertFalse(sections.containsKey("top"), "top-level value must not appear as a section");
    }

    @Test
    void getTopLevelEntryStorageExcludesSections() {
        DataStorage ds = new DataStorage();
        ds.set("s.a", 1);
        ds.set("top", 3);

        assertEquals(Map.of("top", 3), ds.getTopLevelEntryStorage().asMap());
    }

    // --- MERGE ---

    @Test
    void mergeWithOverwrite() {
        DataStorage a = new DataStorage();
        a.set("x", 1);

        DataStorage b = new DataStorage();
        b.set("x", 2);
        b.set("y", 3);

        a.merge(b, true);
        assertEquals(2, a.get("x"));
        assertEquals(3, a.get("y"));
    }

    @Test
    void mergeWithoutOverwriteKeepsExisting() {
        DataStorage a = new DataStorage();
        a.set("x", 1);

        DataStorage b = new DataStorage();
        b.set("x", 2);
        b.set("y", 3);

        a.merge(b, false);
        assertEquals(1, a.get("x"));
        assertEquals(3, a.get("y"));
    }

    // --- VIEWS ---

    @Test
    void asMapIsImmutable() {
        DataStorage ds = new DataStorage();
        ds.set("a", 1);
        Map<String, Object> map = ds.asMap();
        assertThrows(UnsupportedOperationException.class, () -> map.put("b", 2));
    }

    @Test
    void asMapIsSnapshot() {
        DataStorage ds = new DataStorage();
        ds.set("a", 1);
        Map<String, Object> map = ds.asMap();
        ds.set("b", 2);
        assertFalse(map.containsKey("b"), "asMap must return a point-in-time snapshot");
    }

    @Test
    void iteratorYieldsAllEntries() {
        DataStorage ds = new DataStorage();
        ds.set("a", 1);
        ds.set("b", 2);

        int count = 0;
        for (Map.Entry<String, Object> ignored : ds) count++;
        assertEquals(2, count);
    }

    @Test
    void keySetContainsAllKeys() {
        DataStorage ds = new DataStorage();
        ds.set("a", 1);
        ds.set("b", 2);
        assertEquals(java.util.Set.of("a", "b"), ds.keySet());
    }

    // --- CONSTRUCTORS ---

    @Test
    void mapConstructorCopiesEntries() {
        DataStorage ds = new DataStorage(Map.of("a", 1, "b", "x"));
        assertEquals(1, ds.get("a"));
        assertEquals("x", ds.get("b"));
    }

    @Test
    void iDataStorageConstructorCopiesEntries() {
        DataStorage src = new DataStorage();
        src.set("a", 1);
        DataStorage copy = new DataStorage((IDataStorage) src);
        assertEquals(1, copy.get("a"));
    }

    // --- OPT ---

    @Test
    void optReturnsValueForMatchingType() {
        DataStorage ds = new DataStorage();
        ds.set("i", 1);
        ds.set("s", "txt");
        ds.set("b", true);

        assertEquals(1, ds.optInt("i", -1));
        assertEquals("txt", ds.optString("s", null));
        assertTrue(ds.optBoolean("b", false));
    }

    @Test
    void optReturnsDefaultForWrongType() {
        DataStorage ds = new DataStorage();
        ds.set("l", 5L); // Long, not Integer
        assertEquals(-1, ds.optInt("l", -1), "optInt must be strict about the exact type");
    }

    @Test
    void optReturnsDefaultForMissingKey() {
        assertEquals(99, new DataStorage().optInt("missing", 99));
    }

    // --- CLONE ---

    @Test
    void cloneIsIndependent() {
        DataStorage ds = new DataStorage();
        ds.set("a", 1);

        DataStorage clone = ds.clone();
        ds.set("a", 2);

        assertEquals(1, clone.get("a"), "clone must not reflect later changes to the original");
    }
}