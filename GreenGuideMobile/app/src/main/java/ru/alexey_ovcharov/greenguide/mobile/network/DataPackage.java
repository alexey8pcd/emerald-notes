package ru.alexey_ovcharov.greenguide.mobile.network;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

/**
 * Created by Admin on 04.06.2017.
 */

public class DataPackage {
    public static final String DATA_SIZE = "data-size";
    public static final String CRC_32 = "crc32";
    private final byte[] binary;
    private final int size;
    private final long checksum;
    private Map<String, String> headers = new HashMap<>();

    public DataPackage(InputStream inputStream) throws IOException {
        this.binary = IOUtils.toByteArray(inputStream);
        this.size = binary.length;
        this.checksum = checksum(binary);
    }

    private static long checksum(byte[] binary) {
        CRC32 crc32 = new CRC32();
        crc32.update(binary);
        return crc32.getValue();
    }

    public Set<Map.Entry<String, String>> getHeaders() {
        headers.put(DATA_SIZE, String.valueOf(size));
        headers.put(CRC_32, String.valueOf(checksum));
        return headers.entrySet();
    }

    public byte[] getBinary() {
        return binary;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
}

