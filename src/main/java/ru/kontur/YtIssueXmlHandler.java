package ru.kontur;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author michael.plusnin
 */
public class YtIssueXmlHandler extends DefaultHandler {
    private static enum States { EMPTY, ISSUE, ISSUE_FIELD, ISSUE_FIELD_VALUE }

    private int                      depth;
    private boolean                  isIssueWasOpened;
    private YtIssueXmlHandler.States state;

    private String lastFieldName;
    private ListMultimap<String, String> result;
    private StringBuilder characters;

    public YtIssueXmlHandler() {
        depth = 0;
        isIssueWasOpened = false;
        state = YtIssueXmlHandler.States.EMPTY;
        result = ArrayListMultimap.create();
        characters = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName,String qName,
            Attributes attributes) throws SAXException {
        if (depth == 0 && "issue".equalsIgnoreCase(qName)
                && state == YtIssueXmlHandler.States.EMPTY) {
            if (isIssueWasOpened)
                throw new SAXException("Unexpected more than one isuue");
            isIssueWasOpened = true;
            if (attributes.getValue("id") != null)
                result.put("id", attributes.getValue("id"));
            if (attributes.getValue("jiraId") != null)
                result.put("jiraId", attributes.getValue("jiraId"));
            state = YtIssueXmlHandler.States.ISSUE;
            depth++;
        } else if (depth == 1 && "field".equalsIgnoreCase(qName)
                && state == YtIssueXmlHandler.States.ISSUE) {
            String nameAttrib = attributes.getValue("name");
            if (nameAttrib != null) {
                lastFieldName = nameAttrib;
                state = YtIssueXmlHandler.States.ISSUE_FIELD;
            }
            depth++;
        } else if (depth == 2 && "value".equalsIgnoreCase(qName)
                && state == YtIssueXmlHandler.States.ISSUE_FIELD) {
            state = YtIssueXmlHandler.States.ISSUE_FIELD_VALUE;
            depth++;
        } else if (depth == 3 && state == YtIssueXmlHandler.States.ISSUE_FIELD_VALUE) {
            throw new SAXException("Issue-field-value value contains xml code, "
                    + "at field '" + lastFieldName + "'");
        } else {
            depth++;
        }
    }

    @Override
    public void endElement(String uri, String localName,
            String qName) throws SAXException {
        depth--;
        if (depth == 0 && "issue".equalsIgnoreCase(qName)
                && state == YtIssueXmlHandler.States.ISSUE) {
            state = YtIssueXmlHandler.States.EMPTY;
        } else if (depth == 1 && "field".equalsIgnoreCase(qName)
                && state == YtIssueXmlHandler.States.ISSUE_FIELD) {
            lastFieldName = null;
            state = YtIssueXmlHandler.States.ISSUE;
        } else if (depth == 2 && "value".equalsIgnoreCase(qName)
                && state == YtIssueXmlHandler.States.ISSUE_FIELD_VALUE) {
            result.put(lastFieldName, characters.toString());
            characters.delete(0, characters.length());
            state = YtIssueXmlHandler.States.ISSUE_FIELD;
        } else {
            // all ok
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if (depth == 3 && state == YtIssueXmlHandler.States.ISSUE_FIELD_VALUE)
            characters.append(ch, start, length);
        else {
            // all ok
        }
    }

    public ListMultimap<String, String> getParsedValues() {
        return result;
    }
}