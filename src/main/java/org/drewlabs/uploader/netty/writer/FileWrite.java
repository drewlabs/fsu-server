package org.drewlabs.uploader.netty.writer;

import java.io.*;

public class FileWrite {
    public static final String BASE_PATH = "/tmp/file-uploader/res/"; // TODO :: add all these to configuration file and make config reader for JAR version
    public static boolean writeFileFromByteArray(byte[] fileBytes, String fileName){
        System.out.println("Writing file with filename " + fileName);
        try {
            OutputStream os = new FileOutputStream(new File(BASE_PATH+fileName));
            os.write(fileBytes);
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
