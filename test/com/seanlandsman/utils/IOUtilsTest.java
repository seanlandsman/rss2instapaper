package com.seanlandsman.utils;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IOUtilsTest {
    @Test
    public void testCanReadInputStream() throws IOException {
        // given
        FileInputStream testInput = new FileInputStream("./test/resources/small.xml");

        // when
        String result = IOUtils.toString(testInput);

        // expect
        assertEquals("Result of read not what was expected",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<feed xml:lang=\"en\">\n" +
                        "</feed>\n",
                result);
    }
}
