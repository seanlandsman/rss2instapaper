package com.seanlandsman.persistance;

import com.google.appengine.api.datastore.*;

import java.util.Date;
import java.util.List;

public class GoogleDataStore implements Store {

    private final DatastoreService datastore;
    private final Key storeKey;

    public GoogleDataStore() {
        storeKey = KeyFactory.createKey("Store", "StoreKey");
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @Override
    public void write(String id, String data) {
        Entity entry = getEntryById(id);

        // already saved?
        if (entry != null) {
            return;
        }

        entry = new Entity("Entry", storeKey);
        entry.setProperty("id", id);
        entry.setProperty("data", data);
        entry.setProperty("date", new Date());

        datastore.put(entry);
    }

    @Override
    public String read(String id) {
        Entity entry = getEntryById(id);
        if (entry != null) {
            return (String) entry.getProperty("data");
        }
        return null;
    }

    private Entity getEntryById(String id) {
        Query query = new Query("Entry", storeKey).setFilter(Query.FilterOperator.EQUAL.of("id", id));
        List<Entity> entries = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));
        if (entries != null && entries.size() == 1) {
            return entries.get(0);
        }
        return null;
    }

    @Override
    public void deleteEntriesOlderThan(Date date) {
        Query query = new Query("Entry", storeKey).setFilter(Query.FilterOperator.LESS_THAN.of("date", date));
        List<Entity> entries = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        if (entries != null) {
            for (Entity entry : entries) {
                datastore.delete(entry.getKey());
            }
        }
    }
}
