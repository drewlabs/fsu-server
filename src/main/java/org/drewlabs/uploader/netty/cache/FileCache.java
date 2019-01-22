package org.drewlabs.uploader.netty.cache;

import java.util.HashMap;

public class FileCache {
    public static HashMap<String, byte[]> fileBytesMap = new HashMap<>();
    public static HashMap<String, String> fileNameMap = new HashMap<>();
    public static HashMap<String, Integer> fileSize = new HashMap<>();
    public static HashMap<String, Integer> fileUpdateSize = new HashMap<>();
}
