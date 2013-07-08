package com.seanlandsman.persistance;

import java.util.Date;

public interface Store {
    void write(String id, String data);

    String read(String id);

    void deleteEntriesOlderThan(Date date);
}
