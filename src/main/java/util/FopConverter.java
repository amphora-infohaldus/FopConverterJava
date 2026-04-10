package util;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Pattern;

public class FopConverter {

    private static final Logger logger = Logger.getLogger(FopConverter.class);

    public static Boolean convertAktToPdf(FopFactory fopFactory, String inputFile, String convertedFile) {

        logger.info("Starting to convert to FOP");
        byte[] xslBytes;
        try {
            xslBytes = getXSLFileFromAkt(inputFile);
            logger.info("XSL file found");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.debug(e.getMessage(), e);
            return false;
        }

        if (xslBytes == null) {
            logger.debug("Could not access XSLT file");
            return false;
        }

        if (!isFOP(xslBytes)) {
            logger.debug("Xsl file is not FOP compatible");
            return false;
        }

        InputStream isXsl = new ByteArrayInputStream(xslBytes);
        OutputStream outStream;
        try {
            outStream =
                    new BufferedOutputStream(
                        new FileOutputStream(
                            new File(convertedFile)));
        } catch (FileNotFoundException e) {
            logger.debug(e.getMessage(), e);
            return false;
        }

        try {
            logger.info("Creating user agent");
            FOUserAgent agent = fopFactory.newFOUserAgent();

            // pdf
            Fop fop;
            try {
                fop = fopFactory.newFop("application/pdf", agent, outStream);
                logger.info("Fop object created");
            } catch (FOPException e) {
                logger.debug(e.getMessage(), e);
                return false;
            }

            TransformerFactory trFactory = TransformerFactory.newInstance();
            logger.info("New transformerFactory created");

            // xsl
            Source xslSource = new StreamSource(isXsl);
            Transformer transformer;
            try {
                transformer = trFactory.newTransformer(xslSource);
                logger.info("new transformer object created");
            } catch (TransformerConfigurationException e) {
                logger.debug(e.getMessage(), e);
                return false;
            }

            // xml
            Source xmlSource = new StreamSource(new File(inputFile));
            logger.info("Starting to convert ...");

            // output
            Result result;
            try {
                result = new SAXResult(fop.getDefaultHandler());
                logger.info("SAXResult done");
                transformer.transform(xmlSource, result);
                logger.info("Transformation done");
            } catch (FOPException | TransformerException | PDFConformanceException e) {
                logger.debug(e.getMessage(), e);
                return false;
            }

            logger.info("Converting successful");
            return true;
        } finally {
            try {
                outStream.close();
                logger.info("Stream closed");
            } catch (IOException e) {
                logger.debug(e.getMessage(), e);
            }
        }
    }

    private static boolean isFOP(byte[] xslBytes) {
        String xmlString = new String(xslBytes, StandardCharsets.UTF_8);
        return Pattern.compile("(<!--FOP-->)").matcher(xmlString).find();
    }

    private static byte[] getXSLFileFromAkt(String inputFile) throws ParserConfigurationException, IOException, SAXException {

        Path filePath = Paths.get(inputFile);
        if (!Files.exists(filePath)) {
            logger.debug("getXSLFileFromAkt: Could not find xslt file");
            throw new IOException("Could not find xslt file");
        }

        InputStream stream = new FileInputStream(inputFile);
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(is);
        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        String xslValue;
        try {
            xslValue = xp.evaluate("//oigusakt/xslFile/text()", root);
        } catch (XPathExpressionException e) {
            logger.debug(e.getMessage(), e);
            return null;
        }
        return Base64.getDecoder().decode(xslValue);
    }
}
