package com.alicloud.databox.opensdk.io;

import com.alicloud.databox.opensdk.AliyunpanException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The BufferedOutputStream implemented using RandomAccessFile.
 * 用于下载场景文件内容保存
 */
public class BufferRandomAccessFile {
    private BufferedOutputStream out;
    private FileDescriptor fd;
    private RandomAccessFile randomAccess;

    public BufferRandomAccessFile(File file) throws IOException {
        try {
            randomAccess = new RandomAccessFile(file, "rw");
            fd = randomAccess.getFD();
            out = new BufferedOutputStream(new FileOutputStream(randomAccess.getFD()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (out != null) {
            out.write(b, off, len);
        }
    }

    public void flushAndSync() throws IOException {
        if (out != null) {
            out.flush();
        }
        if (fd != null) {
            fd.sync();
        }
    }

    public void close() throws IOException {
        if (out != null) {
            out.close();
        }
        if (randomAccess != null) {
            randomAccess.close();
        }
    }

    public void seek(long offset) throws IOException {
        if (randomAccess != null) {
            randomAccess.seek(offset);
        }
    }
}
