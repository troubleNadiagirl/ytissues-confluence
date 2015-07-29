package ru.kontur;

import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author michael.plusnin
 */
public class YtXmlIssue extends YtListMultimapIssue {
    public YtXmlIssue(InputStream xmlIn) throws IOException, ParseException {
        super(parseIssue(xmlIn));
    }

    private static ListMultimap<String, String> parseIssue(InputStream xmlIn) throws IOException, ParseException {
        YtIssueXmlHandler handler = new YtIssueXmlHandler();
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(xmlIn, handler);
        } catch (ParserConfigurationException e) {
            throw new ParseException(e);
        } catch (SAXException e) {
            throw new ParseException(e);
        }
        return handler.getParsedValues();
    }
}
