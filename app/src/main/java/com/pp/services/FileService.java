package com.pp.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class FileService {
    public static URI createAndWriteFile(File filesDir, String filename, String content) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(filename, ".txt", filesDir);
            FileOutputStream fout = new FileOutputStream(tempFile);
            fout.write(content.getBytes());
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile != null ? tempFile.toURI() : null;
    }
}