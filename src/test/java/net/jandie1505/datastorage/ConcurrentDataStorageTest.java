package net.jandie1505.datastorage;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentDataStorageTest {

    // --- BASIC DELEGATION ---

    @Test
    void behavesLikeDataStorage() {
        ConcurrentDataStorage ds = new ConcurrentDataStorage();
        ds.set("a", 1);
        ds.set("sec.x", 2);

        assertEquals(1, ds.get("a"));
        assertEquals(Map.of("x", 2), ds.getSection("sec").asMap());
        assertEquals(Map.of("a", 1), ds.getTopLevelEntryStorage().asMap());
    }

    @Test
    void returnedSectionIsIndependentAndPlain() {
        ConcurrentDataStorage ds = new ConcurrentDataStorage();
        ds.set("sec.a", 1);

        DataStorage section = ds.getSection("sec");
        section.set("a", 999);

        assertEquals(1, ds.get("sec.a"), "returned snapshot must be detached from the concurrent storage");
    }

    @Test
    void mergeFromPlainStorageWorks() {
        ConcurrentDataStorage ds = new ConcurrentDataStorage();
        ds.set("x", 1);

        DataStorage other = new DataStorage();
        other.set("x", 2);
        other.set("y", 3);

        ds.merge(other, true);
        assertEquals(2, ds.get("x"));
        assertEquals(3, ds.get("y"));
    }

    // --- CONCURRENCY ---

    /**
     * Regression test for the cross-object lock-ordering deadlock: two ConcurrentDataStorages
     * merged into each other from two threads must not deadlock.
     */
    @Test
    void reciprocalMergeDoesNotDeadlock() throws InterruptedException {
        ConcurrentDataStorage a = new ConcurrentDataStorage();
        ConcurrentDataStorage b = new ConcurrentDataStorage();
        a.set("a", 1);
        b.set("b", 2);

        final int iterations = 5_000;
        Runnable mergeBIntoA = () -> { for (int i = 0; i < iterations; i++) a.merge(b); };
        Runnable mergeAIntoB = () -> { for (int i = 0; i < iterations; i++) b.merge(a); };

        Thread t1 = new Thread(mergeBIntoA, "merge-b-into-a");
        Thread t2 = new Thread(mergeAIntoB, "merge-a-into-b");
        t1.start();
        t2.start();

        t1.join(15_000);
        t2.join(15_000);

        assertFalse(t1.isAlive(), "reciprocal merge deadlocked (thread 1 still running)");
        assertFalse(t2.isAlive(), "reciprocal merge deadlocked (thread 2 still running)");
    }

    @Test
    void concurrentSetsOnDistinctKeysAreNotLost() throws InterruptedException {
        ConcurrentDataStorage ds = new ConcurrentDataStorage();
        final int threads = 8;
        final int perThread = 1_000;

        CountDownLatch start = new CountDownLatch(1);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        Thread[] workers = new Thread[threads];

        for (int t = 0; t < threads; t++) {
            final int id = t;
            workers[t] = new Thread(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) ds.set("t" + id + "_k" + i, i);
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
            workers[t].start();
        }

        start.countDown();
        for (Thread w : workers) w.join();

        assertTrue(errors.isEmpty(), () -> "unexpected errors: " + errors);
        assertEquals(threads * perThread, ds.asMap().size());
    }

    @Test
    void concurrentReadsDuringWritesDoNotThrow() throws InterruptedException {
        ConcurrentDataStorage ds = new ConcurrentDataStorage();
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        AtomicInteger reads = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);

        Thread writer = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < 50_000; i++) ds.set("k" + (i % 100), i);
            } catch (Throwable e) {
                errors.add(e);
            }
        });

        Thread reader = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < 50_000; i++) {
                    for (Map.Entry<String, Object> ignored : ds.asMap().entrySet()) reads.incrementAndGet();
                }
            } catch (Throwable e) {
                errors.add(e);
            }
        });

        writer.start();
        reader.start();
        start.countDown();
        writer.join();
        reader.join();

        assertTrue(errors.isEmpty(), () -> "concurrent read/write produced errors: " + errors);
    }
}