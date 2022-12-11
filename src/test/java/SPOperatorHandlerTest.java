package de.tu_dresden.inf.iccl.slowl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;

public class SPOperatorHandlerTest {
	
	@Test
	public void givenSPName_whenIsSPName_thenReturnBoolean() {
		SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
		assertEquals(true, spOperatorHandler.isSPName("*"));
		assertEquals(true, spOperatorHandler.isSPName("s"));
		assertEquals(true, spOperatorHandler.isSPName("s430"));
		assertEquals(true, spOperatorHandler.isSPName("aStandpoint"));
		assertEquals(false, spOperatorHandler.isSPName("1sp"));
		assertEquals(false, spOperatorHandler.isSPName("sp_45"));
		assertEquals(false, spOperatorHandler.isSPName("§sp"));
	}
	
	@Test
	public void givenSPAxiomName_whenIsSPAxiomName_thenReturnBoolean() {
		SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
		assertEquals(true, spOperatorHandler.isSPAxiomName("§ax1"));
		assertEquals(true, spOperatorHandler.isSPAxiomName("§ax"));
		assertEquals(true, spOperatorHandler.isSPAxiomName("§SPAxiom234"));
		assertEquals(false, spOperatorHandler.isSPAxiomName("ax1"));
		assertEquals(false, spOperatorHandler.isSPAxiomName("*"));
		assertEquals(false, spOperatorHandler.isSPAxiomName("§9"));		
	}
	
	@Test
	public void givenXMLString_whenParseSPOperator_thenReturnSPNames() {
		
		final String xmlString1 = "<?xml version=\"1.0\"?>\n" +
			"<standpointAxiom name=\"§ax1\">\n" +
			"  <Box>\n" +
			"    <UNION>\n" +
			"      <Standpoint name=\"s1\"/>\n" +
			"      <Standpoint name=\"s2\"/>\n" +
			"    </UNION>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
			
		final String xmlString2	 = "<?xml version=\"1.0\"?>\n" +
			"<standpointAxiom>\n" +
			"  <Diamond>\n" +
			"    <MINUS>\n" +
			"      <Standpoint name=\"*\"/>\n" +
			"      <UNION>\n" +
			"        <INTERSECTION>\n" +
			"          <Standpoint name=\"spA\"/>\n" +
			"          <Standpoint name=\"spB\"/>\n" +
			"        </INTERSECTION>\n" +
			"        <Standpoint name=\"s111\"/>\n" +
			"      </UNION>\n" +
			"    </MINUS>\n" +
			"  </Diamond>\n" +
			"</standpointAxiom>";
		
		Set<String> expectedSPNames1 = new HashSet<String>();
		expectedSPNames1.add("s1");
		expectedSPNames1.add("s2");
		
		Set<String> expectedSPNames2 = new HashSet<String>();
		expectedSPNames2.add("spA");
		expectedSPNames2.add("spB");
		expectedSPNames2.add("s111");
		
		Set<String> expectedSPNames3 = new HashSet<String>();
		expectedSPNames3.addAll(expectedSPNames1);
		expectedSPNames3.addAll(expectedSPNames2);
		
		SPParser parser = new SPParser();
		parser.parseSPOperator(xmlString1);
		Set<String> actualSPNames1 = parser.spNames;
		
		parser = new SPParser();
		parser.parseSPOperator(xmlString2);
		Set<String> actualSPNames2 = parser.spNames;
		
		parser = new SPParser();
		parser.parseSPOperator(xmlString1);
		parser.parseSPOperator(xmlString2);
		Set<String> actualSPNames3 = parser.spNames;
		
		assertEquals(expectedSPNames1, actualSPNames1);
		assertEquals(expectedSPNames2, actualSPNames2);
		assertEquals(expectedSPNames3, actualSPNames3);
	}
}