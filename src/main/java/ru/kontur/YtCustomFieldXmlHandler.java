/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kontur;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author michael.plusnin
 */
public class YtCustomFieldXmlHandler extends DefaultHandler {
    private int depth;
    private String type;

    public YtCustomFieldXmlHandler() {
        depth = 0;
        type = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        depth++;
        if (depth == 1 && "customFieldPrototype".equals(qName))
            if (type == null)
                type = attributes.getValue("type");
            else
                throw new SAXException("Custom field type XML contains "
                        + "over than one elements 'customFieldPrototype'");
    }

    @Override
    public void endElement(String uri, String localName,
            String qName) throws SAXException {
        depth--;
    }

    public String getParsedType() {
        return type;
    }
}
