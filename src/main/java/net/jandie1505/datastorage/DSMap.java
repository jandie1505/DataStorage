package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * A linked map for {@link DataStorage}.
 */
public class DSMap extends AbstractMap<String, Object> {
    @NotNull private final Map<String, Object> internalMap;

    /**
     * Creates a new linked map.
     * @param internalMap internal map from datastorage
     */
    public DSMap(@NotNull Map<String, Object> internalMap) {
        this.internalMap = internalMap;
    }

    @Override
    public @NotNull Set<Entry<String, Object>> entrySet() {
        return new DSEntrySet(this.internalMap);
    }

}
