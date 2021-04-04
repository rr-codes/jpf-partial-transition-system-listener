package partialtransitionsystemlistener;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class PTSLTest extends TestJPF {
	private static class Tester {
		private static boolean attribute;
	}

	private final static String[] properties = {
			"+cg.enumerate_random=true",
			"+listener=partialtransitionsystemlistener.PartialTransitionSystemListener",
			"+partialtransitionsystemlistener.use_dot=true",
			"+partialtransitionsystemlistener.max_new_states=2",
	};

	private static final String dottyFileName = PTSLTest.class.getName() + ".dot";

	private static String path;

	@BeforeAll
	public static void setUpBeforeClass() {
		path = System.getProperty("user.dir") + "/src/test/resources/";
	}

	@AfterAll
	public static void afterAll() {
		File dottyFile = new File(dottyFileName);
		boolean wasDeleted = dottyFile.delete();
		if (!wasDeleted) {
			System.err.println("File not deleted");
		}
	}

	@Test
	public void initialTest() {
		if (verifyNoPropertyViolation(properties)) {
			Random random = new Random();
			Tester.attribute = random.nextBoolean();
		} else {
			assertFilesEqual(dottyFileName, path + "initial.dot");
		}
	}

	private void assertFilesEqual(String actual, String expected) {
		try {
			String actualLines = Files.lines(new File(actual).toPath()).collect(Collectors.joining("\n"));
			String expectedLines = Files.lines(new File(expected).toPath()).collect(Collectors.joining("\n"));

			Assertions.assertEquals(expectedLines, actualLines);
		} catch (IOException e) {
			fail();
		}
	}
}
