package cte.testfaelle.modifier;

import cte.testfaelle.domain.TimelineLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class AB30XmlModifier {
    private final File inputXmlFile;
    private final File outputXmlFile;
    Set<String> xmlTagsSet = new HashSet<>();
    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();

    public AB30XmlModifier(File inputXmlFile, File outputXmlFile) {
        this.inputXmlFile = inputXmlFile;
        this.outputXmlFile = outputXmlFile;
    }

    public Set<String> modifyXml(BiFunction<String, String, Boolean> dateTimeFinderFunction, BiFunction<String, String, String> dateTimeModifierFunction) throws Exception {
        try (InputStream fileInputStream = new FileInputStream(inputXmlFile); OutputStream fileOutputStream = new FileOutputStream(outputXmlFile)) {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(fileInputStream);
            XMLEventWriter xmlEventWriter = xmlOutputFactory.createXMLEventWriter(fileOutputStream);

            String cuurentTagName = null;
            while (xmlEventReader.hasNext()) {
                XMLEvent event = (XMLEvent) xmlEventReader.next();
                if (event.isStartElement()) {
                    cuurentTagName = event.asStartElement().getName().getLocalPart();
                    event = getXmlEventForXmlTagWithAttributes(dateTimeFinderFunction, dateTimeModifierFunction, event);
                } else if (event.isEndElement()) {
                    cuurentTagName = null;
                } else if (event.isCharacters() && !event.asCharacters().isWhiteSpace() && (cuurentTagName != null)) {
                    event = getXmlEventForXmlTag(dateTimeFinderFunction, dateTimeModifierFunction, cuurentTagName, event);
                }
                xmlEventWriter.add(event);
            }
            xmlEventWriter.flush();
            return xmlTagsSet;
        }
    }

    private XMLEvent getXmlEventForXmlTagWithAttributes(BiFunction<String, String, Boolean> dateTimeFinderFunction, BiFunction<String, String, String> dateTimeModifierFunction, XMLEvent event) {
        StartElement startElement = event.asStartElement();
        Iterator<Attribute> attrIterator = startElement.getAttributes();
        List<Attribute> newAttributes = new ArrayList<>();
        if (attrIterator.hasNext()) {
            while (attrIterator.hasNext()) {
                Attribute nextAttribute = attrIterator.next();
                String attrName = nextAttribute.getName().toString();
                String oldAttrValue = nextAttribute.getValue();
                if (dateTimeFinderFunction.apply(attrName, oldAttrValue)) {
                    String newAttrValue = dateTimeModifierFunction.apply(attrName, oldAttrValue);
                    if (newAttrValue != null) {
                        nextAttribute = xmlEventFactory.createAttribute(attrName, newAttrValue);
                        xmlTagsSet.add(nextAttribute.getName().toString());
                        TimelineLogger.info(getClass(), "\t\tXML-Tag-Attribut '" + startElement.getName().getLocalPart() + "." + attrName + "' : " + oldAttrValue + "  --> '" + newAttrValue + "'");
                    }
                }
                newAttributes.add(nextAttribute);
            }
            event = xmlEventFactory.createStartElement(startElement.getName(), newAttributes.iterator(), startElement.getNamespaces());
        }
        return event;
    }

    private XMLEvent getXmlEventForXmlTag(BiFunction<String, String, Boolean> dateTimeFinderFunction, BiFunction<String, String, String> dateTimeModifierFunction, String cuurentTagName, XMLEvent event) {
        String strValue = event.asCharacters().getData();
        if (dateTimeFinderFunction.apply(cuurentTagName, strValue)) {
            xmlTagsSet.add(cuurentTagName);
            String strModified = dateTimeModifierFunction.apply(cuurentTagName, strValue);
            if (strModified != null) {
                TimelineLogger.info(getClass(), "\t\tXML-Tag '" + cuurentTagName + "' : '" + strValue + "' --> '" + strModified + "'");
                event = xmlEventFactory.createCharacters(strModified);
            }
        }
        return event;
    }

}
