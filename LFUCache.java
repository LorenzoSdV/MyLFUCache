import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Hashtable;

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
        return this.freq - e.freq;
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
    
    private SortedMap<Entry, Integer> mySortedMap;
    private Hashtable<Integer, Entry> myTable;
    private int capacity;

    public LFUCache(int capacity) {
        mySortedMap = new TreeMap<>();
        myTable = new Hashtable<>();
        this.capacity = capacity;
    }

    public String get(int key) {
        if (myTable.containsKey(key)) {
            Entry currEntry = myTable.get(key);
            currEntry.increment();
            return currEntry.getVal();
        }
        else {
            String val = "fromClient"; // String val = DBClient.getKey(key);
            put(key, val);
            return val;
        }
    }

    public void put(int key, String value) {
        // Update existing key with new value
        if (myTable.containsKey(key) && !myTable.get(key).getVal().equals(value)) {
            Entry currEntry = myTable.get(key); 
            currEntry.setVal(value);
            return;
        }

        if (capacity == myTable.size()) {
            Entry minEntry = mySortedMap.firstKey();
            mySortedMap.remove(minEntry);
            myTable.remove(minEntry.getKey());
        }
        Entry newEntry = new Entry(1, key, value);
        mySortedMap.put(newEntry, key);
        myTable.put(key, newEntry);
        return;
    }
}

class Main {
    public static void main(String[] args) {
        LFUCache myCache = new LFUCache(6);
        myCache.put(5, "hi");
        myCache.put(5, "hello");
        myCache.put(3, "woo");
        myCache.put(49, "lorenzo");
        myCache.get(49);
        myCache.put(9, "bye");
        myCache.get(9);
        myCache.get(5);
        myCache.get(5);
        System.out.println(myCache.get(5));   
    }
}