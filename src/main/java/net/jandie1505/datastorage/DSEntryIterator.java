package net.jandie1505.datastorage;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Iterator for DataStorage iterators which returns {@link DSEntry} instead of the internal {@link Map.Entry}.
 */
public class DSEntryIterator implements Iterator<Map.Entry<String, Object>> {
    @NotNull private final Iterator<Map.Entry<String, Object>> delegate;

    /**
     * Creates a new entry iterator.
     * @param delegate delegate
     */
    public DSEntryIterator(@NotNull Iterator<Map.Entry<String, Object>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return this.delegate.hasNext();
    }

    @Override
    public Map.Entry<String, Object> next() {
        return new DSEntry(this.delegate.next());
    }

    @Override
    public void remove() {
        this.delegate.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<String, Object>> action) {
        this.delegate.forEachRemaining(entry -> action.accept(new DSEntry(entry)));
    }

}
