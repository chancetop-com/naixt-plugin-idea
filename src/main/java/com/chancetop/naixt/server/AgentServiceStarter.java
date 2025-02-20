package com.chancetop.naixt.server;

import com.intellij.openapi.application.PathManager;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author stephen
 */
public class AgentServiceStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentServiceStarter.class);

    public static Process start(String url) throws IOException {
        var dir = getPluginDir();
        if (agentServicePackageNotExists(dir)) {
            ensureDirectoryExists(dir);
            extract(download(url, dir), dir);
        }
        return startService(dir);
    }

    private static boolean agentServicePackageNotExists(String dir) {
        var execPath = new File(dir + "/example-service/bin", "example-service.bat");
        return !execPath.exists();
    }

    private static String getPluginDir() {
        return PathManager.getPluginsPath() + File.separator + "naixt" + File.separator + "agent-service";
    }

    private static void ensureDirectoryExists(String dir) throws IOException {
        var directory = new File(dir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + dir);
            }
        }
    }

    private static File download(String urlStr, String dir) throws IOException {
        var parentDir = Paths.get(dir).getParent().toAbsolutePath();
        var tmp = new File(parentDir.toString(), "agent-service.tar");
        if (urlStr.startsWith("/") || urlStr.matches("[A-Za-z]:.*")) {
            File sourceFile = new File(urlStr);
            if (!sourceFile.exists()) {
                throw new FileNotFoundException("Local file not found: " + urlStr);
            }
            Files.copy(sourceFile.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
        var url = new URL(urlStr);
        if ("file".equals(url.getProtocol())) {
            Files.copy(new File(url.getFile()).toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
        if (isFileAlreadyDownloaded(tmp, url)) {
            LOGGER.info("File already exist, skipping download.");
            return tmp;
        }
        return downloadHttp(urlStr, tmp);
    }

    private static boolean isFileAlreadyDownloaded(File localFile, URL remoteUrl) throws IOException {
        if (localFile.exists()) {
            long localFileSize = localFile.length();
            HttpURLConnection connection = (HttpURLConnection) remoteUrl.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                long remoteFileSize = connection.getContentLengthLong();
                return localFileSize == remoteFileSize;
            }
        }
        return false;
    }

    private static File downloadHttp(String urlStr, File tempFile) throws IOException {
        var url = new URL(urlStr);
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download file. Server responded with code: " + responseCode);
        }

        try (var in = connection.getInputStream();
             var out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private static void extract(File tarFile, String dir) throws IOException {
        deleteDirectoryContents(new File(dir));
        try (var tarIn = new TarArchiveInputStream(new FileInputStream(tarFile))) {
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                var outputFile = new File(dir, entry.getName());

                if (!outputFile.toPath().normalize().startsWith(new File(dir).toPath())) {
                    throw new IOException("Invalid entry detected: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    if (!outputFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + outputFile.getAbsolutePath());
                    }
                } else {
                    File parent = outputFile.getParentFile();
                    if (!parent.exists()) {
                        if (!parent.mkdirs()) {
                            throw new IOException("Failed to create directory: " + parent.getAbsolutePath());
                        }
                    }
                    Files.copy(tarIn, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void deleteDirectoryContents(File dir) throws IOException {
        if (!dir.exists()) return;

        File[] allContents = dir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteRecursively(file);
            }
        }
    }

    private static void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete file or directory: " + file.getPath());
        }
    }

    private static Process startService(String dir) throws IOException {
        var binDir = new File(dir, "example-service");
        var batFile = new File(binDir, "bin/example-service.bat");

        if (!batFile.exists()) {
            throw new FileNotFoundException("Batch file not found: " + batFile.getAbsolutePath());
        }

        var pb = new ProcessBuilder(batFile.toString());
        pb.directory(binDir);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        return pb.start();
    }
}