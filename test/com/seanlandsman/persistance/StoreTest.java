package com.seanlandsman.persistance;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StoreTest {
    @Test
    public void testWriteToStoreRetrievesSameResult() {
        // given
        String id = "id";
        String data = "data";

        Store store = new InMemoryStore();

        // when
        store.write(id, data);
        String readData = store.read(id);

        // expect
        assertEquals("Read data not what was written", data, readData);
    }
}
