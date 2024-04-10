import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Hashtable;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

class Entry implements Comparable<Entry>{
    int freq;
    int key;
    String val;

    public Entry(int f, int k, String v) {
        this.freq = f;
        this.key = k;
        this.val = v;
    }

    public int compareTo(Entry e) {
        if (this.freq != e.freq) {
            return Integer.compare(this.freq, e.freq);
        } else {
            return Integer.compare(this.key, e.key);
        }
    }

    public int getFreq() {
        return freq;
    }

    public String getVal() {
        return val;
    }

    public int getKey() {
        return key;
    }

    public void increment() {
        freq += 1;
    }

    public void setVal(String v) {
        val = v;
        return;
    }

    public int hashCode() {
        return key;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Entry other = (Entry) obj;

        return key == other.key;
    }
}

public class LFUCache {
    
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private SortedMap<Entry, Integer> mySortedMap;
    // private Hashtable<Integer, Entry> myTable;
    private ConcurrentHashMap<Integer, Entry> myTable;
    private int capacity;

    public LFUCache(int capacity) {
        mySortedMap = new TreeMap<>();
        // myTable = new Hashtable<>();
        myTable = new ConcurrentHashMap<>();
        this.capacity = capacity;
    }

    public String get(int key) {
        lock.readLock().lock();
        try {
            if (myTable.containsKey(key)) {
                Entry currEntry = myTable.get(key);
                mySortedMap.remove(currEntry);
                currEntry.increment();
                mySortedMap.put(currEntry, key);
                return currEntry.getVal();
            }
            else {
                String val = "fromClient"; // String val = DBClient.getKey(key);
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    put(key, val);
                } finally {
                    lock.writeLock().unlock();
                }
                lock.readLock().lock();
                return val;
            }
        } finally {
            lock.readLock().unlock();
        }
        
    }
    public void printSortedMap() {
        System.out.println("SORTED MAP:");
        for (Map.Entry<Entry, Integer> entry : mySortedMap.entrySet()) {
            System.out.println("Key: " + entry.getKey().getKey() + ", Value: " + entry.getKey().getVal() + ", Freq: " + entry.getKey().getFreq());
        }
        System.out.println();
    }

    public void put(int key, String value) {
        lock.writeLock().lock();
        try {
            // Update existing key with new value
            if (myTable.containsKey(key)) {
                Entry currEntry = myTable.get(key); 
                mySortedMap.remove(currEntry);
                currEntry.setVal(value);
                currEntry.increment();
                mySortedMap.put(currEntry, key);
                return;
            }
            if (capacity == myTable.size()) {
                Entry minEntry = mySortedMap.firstKey();
                mySortedMap.remove(minEntry);
                myTable.remove(minEntry.getKey());
                System.out.println("Evicted: Key: " + minEntry.getKey() + ", Value: " + minEntry.getVal());
            }
            Entry newEntry = new Entry(1, key, value);
            mySortedMap.put(newEntry, key);
            myTable.put(key, newEntry);
            return;
        } finally {
            lock.writeLock().unlock();
        }
        
    }
}

// SINGLE THREAD CASE
// class Main {
//     public static void main(String[] args) {
//         LFUCache myCache = new LFUCache(3);
//         myCache.put(5, "apple");
//         myCache.put(5, "banana");
//         myCache.put(3, "strawberry");
//         myCache.put(49, "pear");
//         myCache.get(49);
//         myCache.get(49);
//         myCache.put(9, "kiwi");   
//         myCache.put(17, "orange");
//         myCache.get(9);
//         myCache.get(5);
//         myCache.get(5);
//         myCache.put(80, "peach");
//         System.out.println(myCache.get(3)); 
//         System.out.println(myCache.get(50));
//         myCache.printSortedMap();   
//     }
// }

class LFUCacheTester extends Thread {

    private final int key;
    private final String value;
    private final LFUCache cache;

    public LFUCacheTester(int key, String value, LFUCache cache) {
        this.key = key;
        this.value = value;
        this.cache = cache;
    }

    @Override
    public void run() {
        cache.put(key, value);
        System.out.println(getName() + " put: (" + key + ", " + value + ")");
        String result = cache.get(key);
        System.out.println(getName() + " get: (" + key + ") -> " + result);
    }
}

class Main {

    public static void main(String[] args) throws InterruptedException {
        LFUCache cache1 = new LFUCache(2);
        LFUCache cache2 = new LFUCache(4);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int key = i % 4;
            String value = "Value_" + i;
            LFUCache cache = (i % 3 == 0) ? cache1 : cache2;
            LFUCacheTester tester = new LFUCacheTester(key, value, cache);
            threads.add(tester);
            tester.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {

            }
        }
        System.out.println('\n');
        cache1.printSortedMap();
        cache2.printSortedMap();
    }
}