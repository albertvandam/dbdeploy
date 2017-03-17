package io.vandam.dbdeploy.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtility {
    public static String readFileContent(final String fileName) throws IOException {
        final StringBuilder response = new StringBuilder();

        final List<String> fileContent = readFileLines(fileName);
        for (final String line : fileContent) {
            response.append(line).append('\n');
        }

        return response.toString();
    }

    public static List<String> readFileLines(final String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            final List<String> sb = new ArrayList<>();
            String line = br.readLine();

            while (null != line) {
                sb.add(line);
                line = br.readLine();
            }

            return sb;
        }
    }

    /**
     * File exists.
     *
     * @param filename the filename
     * @return true, if successful
     */
    public static boolean fileExists(final String filename) {
        final File file = new File(filename);

        return file.exists();
    }

    public static boolean deleteDirectory(final String name) {
        boolean response = true;

        final List<String> fileList = DirectoryListing.getFiles(name);
        for (final String fileName : fileList) {
            response &= deleteFile(fileName);
        }

        final List<String> dirList = DirectoryListing.getDirectories(name);
        for (final String dirName : dirList) {
            response &= deleteDirectory(dirName);
        }

        final File directory = new File(name);

        return response && directory.exists() && directory.delete();
    }

    public static boolean createDirectory(final String name) {
        final File directory = new File(name);

        return directory.exists() || directory.mkdir();
    }

    public static boolean deleteFile(final String name) {
        final File file = new File(name);

        return file.exists() && file.delete();
    }
}
