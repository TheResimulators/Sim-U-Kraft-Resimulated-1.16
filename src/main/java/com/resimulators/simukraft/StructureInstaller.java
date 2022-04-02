package com.resimulators.simukraft;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StructureInstaller {
    public StructureInstaller() {
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(new URL("https://github.com/TheResimulators/SimUKraftStructures/archive/refs/heads/dl.zip").openStream())) {
            String resourceFolder = System.getProperty("user.dir") + "/resources/simukraft";
            if (new File(resourceFolder + "/dl.zip").exists()) {
                throw new IOException("Failed to download dl.zip since it already exists");
            }
            if (!new File(resourceFolder).mkdirs() && !new File(resourceFolder).exists()) {
                throw new IOException("Failed to create directory " + resourceFolder);
            }
            FileOutputStream fileOutputStream = new FileOutputStream(resourceFolder + "/dl.zip");
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            String zipFile = resourceFolder + "/dl.zip";
            File destinationDir = new File(resourceFolder + "/structures");
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destinationDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            if (!move(new File(resourceFolder + "/structures/SimUKraftStructures-dl"), new File(resourceFolder + "/structures"))) {}
            fileOutputStream.close();
            if (!new File(resourceFolder + "/dl.zip").delete()) {
                throw new IOException("Failed to delete " + resourceFolder + "/dl.zip");
            }
            File tempDirForDeletion = new File(resourceFolder + "/structures/SimUKraftStructures-dl");
            if(tempDirForDeletion.exists()) {
                File[] sub = tempDirForDeletion.listFiles();
                if (sub != null) {
                    for (File file : sub) {
                        file.delete();
                    }
                }
                if (!tempDirForDeletion.delete()) {
                    throw new IOException("Failed to delete " + tempDirForDeletion);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    private boolean move(File source, File destination) {
        return moveDir(source.toPath(), destination.toPath());
    }

    private boolean moveDir(Path source, Path destination) {
        if (source.toFile().isDirectory()) {
            for (File file : source.toFile().listFiles()) {
                moveDir(file.toPath(), destination.resolve(source.relativize(file.toPath())));
            }
        }
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
