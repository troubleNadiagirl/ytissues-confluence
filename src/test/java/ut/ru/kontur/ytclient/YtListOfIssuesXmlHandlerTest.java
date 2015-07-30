package ut.ru.kontur.ytclient;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import ru.kontur.ytclient.YtListOfIssuesXmlHandler;

/**
 *
 * @author michael.plusnin
 */
@Ignore // TODO(mp): reimplement
public class YtListOfIssuesXmlHandlerTest {
    public List<ListMultimap<String, String>> parse(InputStream is)
            throws ParserConfigurationException, SAXException, IOException {
        YtListOfIssuesXmlHandler handler = new YtListOfIssuesXmlHandler();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxParserFactory.newSAXParser();
        parser.parse(is, handler);
        return handler.getParsedValue();
    }

    @Test
    public void emptyIssuesTest() throws ParserConfigurationException, SAXException, IOException {
        String testData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                          "<issueCompacts>\n" +
                          "</issueCompacts>";
        List<ListMultimap<String, String>> parsedValue
                = parse(new ByteArrayInputStream(testData.getBytes()));
        Assert.assertEquals(new LinkedList<ListMultimap<String, String>>(), parsedValue);
    }

    @Test
    public void issuesTestFromFile() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
        String dataFilename = "src/test/resources/inputListOfXmlIssuesTest.xml";
        List<ListMultimap<String, String>> parsedValue
                = parse(new FileInputStream(dataFilename));

        String expectedFilename = "src/test/resources/expectedListOfXmlIssuesTest.txt";
        List<ListMultimap<String, String>> expectedValueWoSpaces
                = new LinkedList<ListMultimap<String, String>>();
        BufferedReader expectedFilenameReader
                = new BufferedReader(new InputStreamReader(new FileInputStream(expectedFilename), "utf-8"));
        String line;
        ListMultimap<String, String> currentIssue = null;
        while ((line = expectedFilenameReader.readLine()) != null) {
            if ("----->".equals(line))
                currentIssue = ArrayListMultimap.create();
            else if ("<-----".equals(line)) {
                expectedValueWoSpaces.add(currentIssue);
                currentIssue = null;
            } else {
                String[] keyValue = line.split("====");
                String key   = keyValue[0];
                String value = keyValue[1];
                currentIssue.put(key, value.replaceAll("\\s", ""));
            }
        }

        List<ListMultimap<String, String>> parsedValueWoSpaces
                = new LinkedList<ListMultimap<String, String>>();
        for (ListMultimap<String, String> issue : parsedValue) {
            ListMultimap<String, String> issueWoSpaces = ArrayListMultimap.create();
            for (Entry<String, String> entry : issue.entries()) {
                issueWoSpaces.put(entry.getKey(), entry.getValue().replaceAll("\\s", ""));
            }
            parsedValueWoSpaces.add(issueWoSpaces);
        }

        checkIssuesContains(expectedValueWoSpaces, parsedValueWoSpaces);
        checkIssuesContains(parsedValueWoSpaces,   expectedValueWoSpaces);
    }

    private void checkIssuesContains(
        List<ListMultimap<String, String>> subset,
        List<ListMultimap<String, String>> set
    ) {
        Iterator<ListMultimap<String, String>> subsetIt = subset.iterator();
        Iterator<ListMultimap<String, String>> setIt    = set.iterator();
        int idx = 1;
        while (subsetIt.hasNext() && setIt.hasNext()) {
            ListMultimap<String, String> subsetIssue = subsetIt.next();
            ListMultimap<String, String> setIssue    = setIt.next();
            for (Entry<String, String> subsetEntry : subsetIssue.entries()) {
                String key   = subsetEntry.getKey();
                String value = subsetEntry.getValue();
                Assert.assertTrue("Field '" + key + "' of " + idx + " issue not found in set values", setIssue.containsKey(key));
                Assert.assertTrue("Value '" + value + "' of field '" + key + "' and " + idx + " issue not found in set values", setIssue.containsEntry(key, value));
            }
            idx++;
        }
        Assert.assertFalse("Subset has more issues than set", subsetIt.hasNext());
    }
}
