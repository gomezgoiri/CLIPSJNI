package net.sf.clipsrules.jni;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EnvironmentTest {
	
	Environment env;
	
	@Before
	public void setUp() {
		this.env = new Environment();
	}
	
	@After
	public void tearDown() {
		this.env.destroy();
	}

	private String getFilePath(String filename) {
		final URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		return url.getPath();
	}
	
	@Test
	public void testLoadCorrectSyntax() throws CLIPSError {
		this.env.load( getFilePath("rule_right_syntax.clp") );
	}
	
	@Test
	public void testLoadIncorrectSyntax() {
		try {
			this.env.load( getFilePath("rule_wrong_syntax.clp") );
			fail();
		} catch(CLIPSError e) {
			assertEquals( e.getCode(), 2 );
			assertEquals( e.getModule(), "PRNTUTIL" );
		}
	}
	
	@Test
	public void testEval() throws CLIPSError {
		final PrimitiveValue result = this.env.eval( "(+ 3 4)" );
		assertEquals(new IntegerValue(7), result);
	}
	
	@Test
	public void testEvalNotExistingCommand() {
		try {
			this.env.eval( "(notexistingcommand cid)" );
			fail();
		} catch(CLIPSError e) {
			assertEquals( e.getCode(), 3 );
			assertEquals( e.getModule(), "EXPRNPSR" );
		}
	}
	
	@Test
	public void testBuild() throws CLIPSError {
		assertTrue( this.env.build( "(defrule foo (a) => (assert (b)))" ) );
	}
	
	@Test
	public void testBuildIncorrectSyntax() throws CLIPSError {
		try {
			this.env.build( "(defrule foo (a) ==> (assert (b)))" );
			fail();
		} catch(CLIPSError e) {
			assertEquals( e.getCode(), 2 );
			assertEquals( e.getModule(), "PRNTUTIL" );
		}
	}
}