package util;

import domain.ConversionRequest;
import enums.FileType;
import org.apache.log4j.Logger;

public class RequestValidator {

    private static final Logger logger = Logger.getLogger(RequestValidator.class);

    public static String isValid(ConversionRequest request) {
        String errors = "";

        if (request.getTo() == request.getFrom()) {
            errors = "Conversion from and to cannot be the same file format";
            logger.debug("Conversion from and to cannot be the same file format");
        } else if (request.getFrom() != FileType.Akt) {
            errors = "Only .akt files can be converted!";
            logger.debug("Only .akt files can be converted!");
        } else if (request.getTo() != FileType.Pdf) {
            errors = "Only .pdf is supported as output format";
            logger.debug("Only .pdf is supported as output format");
        } else if (request.getData() == null) {
            errors = "Document is null or empty";
            logger.debug("Document is null or empty");
        }

        return errors;
    }
}
