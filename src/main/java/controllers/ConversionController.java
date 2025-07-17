package controllers;

import domain.ConversionRequest;
import domain.ConversionResponse;
import exceptions.RequestNotValidException;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.FopFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import util.FileSavingUtil;
import util.FopConverter;
import util.RequestValidator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping
public class ConversionController {

    private static final Logger logger = Logger.getLogger(ConversionController.class);

    @Autowired
    private FopFactory fopFactory;

    @Autowired
    private Environment env;

    @PostMapping(value = "conversion",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ConversionResponse> convertDocument(@RequestBody ConversionRequest request) throws Exception {

        ConversionResponse response = new ConversionResponse();

        String error = RequestValidator.isValid(request);
        if (!error.equals("")) {
            logger.debug(error);
            throw new RequestNotValidException(error);
        }

        String convertedFile;
        String originalFile;
        String tempdir = env.getProperty("tempDir");

        if (tempdir == null) {
            logger.debug("Cannot access tempDir property");
            throw new Exception("Cannot access tempDir property");
        }

        try {
            Path tempDir = Paths.get(tempdir);
            System.out.println(tempDir.getFileName());
            if (!Files.exists(tempDir)) {
                boolean dirCreated = new File(tempdir).mkdir();
                if (!dirCreated) {
                    logger.debug("Could not create temp directory");
                    throw new Exception("Could not create temp directory");
                }
            }

            originalFile = FileSavingUtil.getUniqueFileName(tempDir.toString(), request.getFromExtension());
            logger.info("original file: " + originalFile);
            convertedFile = originalFile
                    .replace("." + request.getFromExtension(), "." + request.getToExtension());
            logger.info("converted file: " + convertedFile);

            FileUtils.writeByteArrayToFile(new File(originalFile), request.getDataBytes());
            Path originalFilePath = Paths.get(originalFile);

            if (!Files.exists(originalFilePath)) {
                logger.debug("Could not write original file to disk");
                throw new Exception("Could not write original file to disk");
            }

            boolean fileConverted = FopConverter.convertAktToPdf(fopFactory, originalFile, convertedFile);

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            if (fileConverted) {
                byte[] content = FileUtils.readFileToByteArray(new File(convertedFile));
                logger.info("File contents read to bytes. Lenght = " + content.length);
                response.setFile(content);
                response.setMessage("");
                FileSavingUtil.deleteConvertedFiles(originalFile, convertedFile);
                return new ResponseEntity<>(response, httpHeaders, HttpStatus.OK);
            } else {
                logger.info("Conversion failed!");
                response.setFile(null);
                response.setMessage("Unable to convert document");
                FileSavingUtil.deleteConvertedFiles(originalFile, convertedFile);
                return new ResponseEntity<>(response, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    @GetMapping(value = "/stillAlive")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String stillAlive() {
        return "Elvis is still alive!";
    }
}
