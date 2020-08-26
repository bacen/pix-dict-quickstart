package br.gov.bcb.pi.dict.xml;

import lombok.SneakyThrows;
import org.apache.cxf.staxutils.DelegatingXMLStreamWriter;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class SigningXMLStreamWriter extends DelegatingXMLStreamWriter {

    private static final DocumentBuilderFactory dbf;
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    static {
        try {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            dbf.setNamespaceAware(true);
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private final DOMResult writerResult;
    private final OutputStream signedOutputStream;
    private final XMLSigner signer;

    @SneakyThrows
    public static SigningXMLStreamWriter create(XMLOutputFactory xmlOutputFactory,
                                                OutputStream signedOutputStream,
                                                String keyStoreFile,
                                                String keyStorePassword) {
        DOMResult writerResult = new DOMResult(newDocument());
        XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(writerResult);
        XMLSigner signer = new XMLSigner(keyStoreFile, keyStorePassword);
        return new SigningXMLStreamWriter(signer, xmlStreamWriter, writerResult, signedOutputStream);
    }

    private SigningXMLStreamWriter(XMLSigner signer,
                                   XMLStreamWriter xmlStreamWriter,
                                   DOMResult writerResult,
                                   OutputStream signedOutputStream) {
        super(xmlStreamWriter);
        this.signer = signer;
        this.writerResult = writerResult;
        this.signedOutputStream = signedOutputStream;
    }

    @Override
    public void close() throws XMLStreamException {
        super.flush();
        super.close();
        try {
            signer.sign(this.writerResult.getNode());
            Writer writer = new OutputStreamWriter(signedOutputStream);
            newTransformer().transform(new DOMSource(this.writerResult.getNode()), new StreamResult(writer));
            writer.flush();
        } catch (TransformerException | IOException e) {
            throw new XMLStreamException(e);
        }
    }

    @SneakyThrows
    private static Document newDocument() {
        return dbf.newDocumentBuilder().newDocument();
    }

    @SneakyThrows
    private static Transformer newTransformer() {
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setParameter(OutputKeys.ENCODING, "utf-8");
        return transformer;
    }
}
