package util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileSavingUtil {

    private static final Logger logger = Logger.getLogger(FileSavingUtil.class);

    public static String getUniqueFileName(String directory, String extension) {
        String fileName = UUID.randomUUID() + "." + extension;
        String originalFile = Paths.get(directory, fileName).toString();

        while (new File(originalFile).exists()) {
            fileName = UUID.randomUUID() + "." + extension;
            originalFile = Paths.get(directory, fileName).toString();
        }

        return originalFile;
    }

    public static void deleteConvertedFiles(String originalFile, String convertedFile) {

        Path orig = Paths.get(originalFile);
        Path conv = Paths.get(convertedFile);

        if (Files.exists(orig)) {
            try {
                Files.delete(orig);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        if (Files.exists(conv)) {
            try {
                Files.delete(conv);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
