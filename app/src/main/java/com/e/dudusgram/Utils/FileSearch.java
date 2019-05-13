package com.e.dudusgram.Utils;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {

    /**
     * Search directory and return a list of all directories contained inside @param
     * @param directory
     * @return
     */

    public static ArrayList<String> getDirectoryPaths(String directory){

        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();

        for (File listFile : listFiles) {

            if (listFile.isDirectory()) {
                pathArray.add(listFile.getAbsolutePath());
            }
        }

        return pathArray;
    }

    /**
     * Search @param and return all files inside
     * @param directory
     * @return
     */

    public static ArrayList<String> getFilePaths(String directory){

        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();

        for (File listFile : listFiles) {

            if (listFile.isFile()) {
                pathArray.add(listFile.getAbsolutePath());
            }
        }

        return pathArray;
    }
}
