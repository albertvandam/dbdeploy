package io.vandam.dbdeploy.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryListing {
    static List<String> getFiles(final String base) {
        return getFiles(base, true);
    }

    public static List<String> getFiles(final String base, final boolean includeDirectoryName) {
        final File baseDir = new File(base);
        return getFilesInDirectory(baseDir, includeDirectoryName);
    }

    private static List<String> getFilesInDirectory(final File currentDirectory, final boolean includeDirectoryName) {
        final List<String> fileList = new ArrayList<>();

        final File[] files = currentDirectory.listFiles();
        if (null != files) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    fileList.addAll(getFilesInDirectory(file, includeDirectoryName));
                } else if (file.isFile()) {
                    fileList.add((includeDirectoryName ? currentDirectory + "/" : "") + file.getName());
                }
            }
        }

        return fileList;
    }

    static List<String> getDirectories(final String base) {
        final File baseDir = new File(base);
        return getDirectoriesInDirectory(baseDir);
    }

    private static List<String> getDirectoriesInDirectory(final File currentDirectory) {
        final List<String> fileList = new ArrayList<>();

        final File[] files = currentDirectory.listFiles();
        if (null != files) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    fileList.add(currentDirectory + "/" + file.getName());
                    fileList.addAll(getDirectoriesInDirectory(file));
                }
            }
        }

        return fileList;
    }
}
