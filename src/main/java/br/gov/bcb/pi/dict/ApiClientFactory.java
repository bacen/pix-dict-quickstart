package br.gov.bcb.pi.dict;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.Lists;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

public class ApiClientFactory {

    public static <T> T createApiClient(String apiBaseAddress, Class<T> clazz) {
        XmlMapper xmlMapper = (XmlMapper) new XmlMapper()
                .registerModule(new JaxbAnnotationModule())
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonJaxbXMLProvider provider = new JacksonJaxbXMLProvider();
        provider.setMapper(xmlMapper);
        return JAXRSClientFactory.create(apiBaseAddress, clazz, Lists.newArrayList(provider));
    }

}
