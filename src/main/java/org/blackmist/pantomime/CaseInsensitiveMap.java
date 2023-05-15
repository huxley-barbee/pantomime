package org.blackmist.pantomime;

import java.util.LinkedHashMap;

class CaseInsensitiveMap<V> extends LinkedHashMap<String,V> {

    public V put(String key, V value) {
        return super.put(key.toLowerCase(), value);
    }

    public V get(String key) {
        return super.get(key.toLowerCase());
    }

    public boolean containsKey(String key) {
        return super.containsKey(key.toLowerCase());
    }

}
