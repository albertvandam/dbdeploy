package io.vandam.dbdeploy.xml;

import com.sun.org.apache.xml.internal.serialize.DOMSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XsdGenerator {
    public static void createXsd(final Class className, final String filename) throws JAXBException, IOException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(className);

        final List<DOMResult> domResults = new ArrayList<>();

        // generate the schema
        jaxbContext.generateSchema(
                // need to define a SchemaOutputResolver to store to
                new SchemaOutputResolver() {
                    @Override
                    public Result createOutput(final String namespaceUri, final String suggestedFileName)
                            throws IOException {
                        // save the schema to the list

                        final DOMResult domResult = new DOMResult();
                        domResult.setSystemId(suggestedFileName);
                        domResults.add(domResult);
                        return domResult;
                    }
                });

        // output schema via System.out
        final DOMResult domResult = domResults.get(0);
        final Document document = (Document) domResult.getNode();
        final OutputFormat outputFormat = new OutputFormat(document);
        outputFormat.setIndenting(true);
        final FileWriter fileWriter = new FileWriter(filename);
        final DOMSerializer xmlSerializer = new XMLSerializer(fileWriter, outputFormat);
        xmlSerializer.serialize(document);
        fileWriter.close();
    }
}
