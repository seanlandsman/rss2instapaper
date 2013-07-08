package com.seanlandsman.persistance;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InMemoryStore implements Store {
    private Map<String, String> store = new HashMap<String, String>();

    @Override
    public void write(String id, String data) {
        store.put(id, data);

    }

    @Override
    public String read(String id) {
        return store.get(id);
    }

    @Override
    public void deleteEntriesOlderThan(Date date) {

    }
}
