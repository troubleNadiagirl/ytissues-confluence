package ut.ru.kontur.ytclient;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.junit.Assert;
import org.junit.Test;
import ru.kontur.ytclient.YtIssueXmlHandler;

/**
 *
 * @author michael.plusnin
 */
public class YtIssueXmlHandlerUnitTest {
    public void test(ListMultimap<String, String> expected) throws Exception {
        String xml = getTestXml();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        YtIssueXmlHandler handler = new YtIssueXmlHandler();
        saxParser.parse(new ByteArrayInputStream(xml.getBytes()), handler);
        Assert.assertEquals(expected, handler.getParsedValues());
    }

    @Test
    public void testXml() throws Exception {
        ListMultimap<String, String> expected = LinkedListMultimap.create();
        expected.put("id",          "HBR-63");
        expected.put("attachments", "uploadFile.html");
        expected.put("Priority",    "Normal");
        expected.put("Fix versions", "2.0.7");
        expected.put("Fix versions", "2.0");
        expected.put("Fix versions", "2.0.5");
        expected.put("TestAmp", "Asdf&Zxcv");
        test(expected);
    }

    private String getTestXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<issue id=\"HBR-63\">\n" +
        "    <field name=\"attachments\">\n" +
        "        <value url=\"/_persistent/uploadFile.html?file=45-46&amp;v=0&amp;c=true\">uploadFile.html</value>\n" +
        "    </field>\n" +
        "    <comment id=\"42-306\" author=\"root\" issueId=\"HBR-63\" deleted=\"false\" text=\"comment 1!\" shownForIssueAuthor=\"false\"\n" +
        "             created=\"1267030230127\">\n" +
        "        <replies/>\n" +
        "    </comment>\n" +
        "    <field name=\"TestAmp\">\n" +
        "        <value>Asdf&amp;Zxcv</value>\n" +
        "    </field>\n" +
        "    <field name=\"Fix versions\">\n" +
        "        <value>2.0.7</value>\n" +
        "        <value>2.0</value>\n" +
        "        <value>2.0.5</value>\n" +
        "    </field>\n" +
        "    <comment id=\"42-307\" author=\"root\" issueId=\"HBR-63\" deleted=\"false\" text=\"comment 2?\" shownForIssueAuthor=\"false\"\n" +
        "             created=\"1267030238721\" updated=\"1267030230127\">\n" +
        "        <replies/>\n" +
        "    </comment>\n" +
        "    <field name=\"Priority\">\n" +
        "        <value>Normal</value>\n" +
        "    </field>\n</issue>";
    }
}
