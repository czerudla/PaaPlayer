package cz.hofmanladislav.paaplayer;

import java.io.File;

public class FileSystemUtils {
    public String getParentFolder(String folderPath) {
        try {
            File f = new File(folderPath);
            if (f.getParent() != null)
                return f.getParent();
            return "/";
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "/";
        }
    }

    public boolean isDirectory(String path) {
        File f = new File(path);
        return f.isDirectory();
    }
}
