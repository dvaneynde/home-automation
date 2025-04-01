package eu.dlvm.domotics.factories;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import eu.dlvm.iohardware.IHardware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import eu.dlvm.domotics.base.ConfigurationException;
import eu.dlvm.domotics.base.IDomoticLayoutBuilder;

public class XmlDomoticConfigurator {

	private static Logger logger = LoggerFactory.getLogger(XmlDomoticConfigurator.class);

	public static void configure(String cfgFilepath, IHardware hardware, IDomoticLayoutBuilder builder) {
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			// Load the XSD file from the classpath
			ClassLoader classLoader = XmlDomoticConfigurator.class.getClassLoader();
			try (InputStream xsdStream = classLoader.getResourceAsStream("DomoticConfig.xsd")) {
				if (xsdStream == null) {
					throw new IOException("XSD file 'DomoticConfig.xsd' not found in classpath.");
				}
				Schema schema = schemaFactory.newSchema(new StreamSource(xsdStream));
				parserFactory.setSchema(schema);
			}

			parserFactory.setNamespaceAware(true);

			SAXParser parser = parserFactory.newSAXParser();
			DefaultHandler2 handler = new XmlElementHandlers(builder, hardware);
			parser.parse(cfgFilepath, handler);

		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error("Configuration Failed: ", e);
			throw new ConfigurationException(e.getMessage());
		}
	}
}
