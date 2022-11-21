package de.tu_dresden.inf.iccl.slowl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;

import org.junit.Test;

public class SPExpressionHandlerTest {
	
	@Test
	public void givenSPName_whenIsSPName_thenReturnBoolean() {
		SPExpressionHandler spExpressionHandler = new SPExpressionHandler();
		assertEquals(true, spExpressionHandler.isSPName("*"));
		assertEquals(true, spExpressionHandler.isSPName("s"));
		assertEquals(true, spExpressionHandler.isSPName("s430"));
		assertEquals(true, spExpressionHandler.isSPName("aStandpoint"));
		assertEquals(false, spExpressionHandler.isSPName("1sp"));
		assertEquals(false, spExpressionHandler.isSPName("sp_45"));
	}
	
	@Test
	public void givenXMLFile_whenParseSPExpression_thenReturnSPNames() {
		File xmlFile1 = new File("./src/test/SPExpressionHandlerTest1.xml");
		File xmlFile2 = new File("./src/test/SPExpressionHandlerTest2.xml");
		
		HashSet<String> expectedSPNames1 = new HashSet<String>();
		expectedSPNames1.add("s1");
		expectedSPNames1.add("s2");
		
		HashSet<String> expectedSPNames2 = new HashSet<String>();
		expectedSPNames2.add("*");
		expectedSPNames2.add("spA");
		expectedSPNames2.add("spB");
		expectedSPNames2.add("s111");
		
		HashSet<String> actualSPNames1 = SPParser.parseSPExpression(xmlFile1);
		HashSet<String> actualSPNames2 = SPParser.parseSPExpression(xmlFile2);
		
		assertEquals(expectedSPNames1, actualSPNames1);
		assertEquals(expectedSPNames2, actualSPNames2);
	}
}