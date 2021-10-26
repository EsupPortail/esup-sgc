package org.esupportail.sgc.tools;

import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * Spring Service helping Java synchronizing based on a parameter (named mutex/lock)
 * 
 * @see https://stackoverflow.com/questions/12450402/java-synchronizing-based-on-a-parameter-named-mutex-lock
 *
 * @param <K>
 */
@Component
public class MutexFactory<K> {

    private ConcurrentReferenceHashMap<K, Object> map;

    public MutexFactory() {
        this.map = new ConcurrentReferenceHashMap<>();
    }

    public Object getMutex(K key) {
        return this.map.compute(key, (k, v) -> v == null ? new Object() : v);
    }
}	
