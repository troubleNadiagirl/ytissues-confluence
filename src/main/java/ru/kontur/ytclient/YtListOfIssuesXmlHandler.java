package ru.kontur.ytclient;

import com.google.common.collect.ListMultimap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author michael.plusnin
 */
public class YtListOfIssuesXmlHandler extends DefaultHandler {
    private YtIssueXmlHandler issueHandler;
    private boolean isInIssue;
    private Stack<String> elementsStack;
    private int depth;

    private List<ListMultimap<String, String>> parsedValue;

    public YtListOfIssuesXmlHandler() {
        elementsStack = new Stack<String>();
        parsedValue   = new LinkedList<ListMultimap<String, String>>();
        isInIssue     = false;
        depth         = 0;
    }

    @Override
    public void startElement(String uri, String localName,String qName,
            Attributes attributes) throws SAXException {
        if (depth == 1 && !elementsStack.empty()
                && "issueCompacts".equalsIgnoreCase(elementsStack.peek())
                && "issue".equalsIgnoreCase(qName)) {
            issueHandler = new YtIssueXmlHandler();
            isInIssue = true;
        }

        if (isInIssue)
            issueHandler.startElement(uri, localName, qName, attributes);

        elementsStack.push(qName);
        depth++;
    }

    @Override
    public void endElement(String uri, String localName,
            String qName) throws SAXException {
        depth--;
        elementsStack.pop();

        if (isInIssue)
            issueHandler.endElement(uri, localName, qName);

        if (depth == 1 && !elementsStack.empty()
                && "issueCompacts".equalsIgnoreCase(elementsStack.peek())) {
            isInIssue = false;
            parsedValue.add(issueHandler.getParsedValues());
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if (isInIssue)
            issueHandler.characters(ch, start, length);
    }

    public List<ListMultimap<String, String>> getParsedValue() {
        return parsedValue;
    }
}
