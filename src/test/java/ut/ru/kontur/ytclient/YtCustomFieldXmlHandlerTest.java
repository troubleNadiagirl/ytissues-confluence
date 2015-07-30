package ut.ru.kontur.ytclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import ru.kontur.ytclient.YtCustomFieldXmlHandler;

/**
 *
 * @author michael.plusnin
 */
public class YtCustomFieldXmlHandlerTest {
    public String parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        YtCustomFieldXmlHandler handler = new YtCustomFieldXmlHandler();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxParserFactory.newSAXParser();
        parser.parse(is, handler);
        return handler.getParsedType();
    }

    @Test
    public void testDate() throws ParserConfigurationException, SAXException, IOException {
        String type = "date";
        Assert.assertEquals(type, parse(getTestXmlInputStream(type)));
    }

    @Test
    public void testEnumStar() throws ParserConfigurationException, SAXException, IOException {
        String type = "enum[*]";
        Assert.assertEquals(type, parse(getTestXmlInputStream(type)));
    }

    public InputStream getTestXmlInputStream(String type) {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<customFieldPrototype name=\"Type\" type=\"" + type + "\" isPrivate=\"false\" visibleByDefault=\"true\" autoAttached=\"true\">\n" +
                "	<defaultParam name=\"attachBundlePolicy\" value=\"0\"/><defaultParam name=\"defaultBundle\" value=\"Types\"/>\n" +
                "</customFieldPrototype>";
        return new ByteArrayInputStream(xml.getBytes());
    }
}
