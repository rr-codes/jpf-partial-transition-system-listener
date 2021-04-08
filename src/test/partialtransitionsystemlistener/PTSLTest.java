package partialtransitionsystemlistener;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/*
    Possible way to determine which states should be left out:
    Based on the created Digraph and the max_new_states property...

    Formula: S_Not_Explored <- S(Digraph) \ S(DFS(Digraph, max_new_states))
    Where, S(F) is States of the digraph F
    and, DFS(F, i) is states of the digraph 'F' explored when performing a DFS for 'i' iterations

    This could then be converted to a dot file somehow, or used in another way.

    Might be better *not* to test dot output and instead check if last line of text output
    satisfies the above formula
 */


public class PTSLTest extends TestJPF {

    private static final String dottyFileName = PTSLTest.class.getName() + ".dot";

    private static String path;

    private static String currentTest = "";

    private static String[] properties = new String[]{
            "+cg.enumerate_random=true",
            "+listener=partialtransitionsystemlistener.PartialTransitionSystemListener",
            "+partialtransitionsystemlistener.use_dot=true",
            "+partialtransitionsystemlistener.max_new_states="
    };

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

    /**
     * Used for saving dot files after each test case for testing purposes
     *
     * Commend out to disable, the @Disabled tag doesn't work with JPF
     */
    @AfterEach
    private void saveDotFile() {
//        File source = new File(dottyFileName);
//        File dest = new File(path + "tmp/" + currentTest + ".dot");
//        try {
//            Files.copy(source.toPath(), dest.toPath());
//        } catch (IOException e) {
//            System.err.println("Error saving dot file: " + currentTest);
//        }
    }

    /*
    ====================================== BEGIN TEST SECTION ======================================
     */

    @Test
    public void TSCustom1() {
        currentTest = "TSCustom1";
        properties[3] = "+partialtransitionsystemlistener.max_new_states=" + 1;
        if (verifyNoPropertyViolation(properties)) {
            TSCustom(1, 1);
        } else {
            assertFilesEqual(dottyFileName, path + "expected/" + currentTest + ".dot");
        }
    }

    @Test
    public void TSCustom2() {
        currentTest = "TSCustom2";
        properties[3] = "+partialtransitionsystemlistener.max_new_states=" + 2;
        if (verifyNoPropertyViolation(properties)) {
            TSCustom(1, 1);
        } else {
            assertFilesEqual(dottyFileName, path + "expected/" + currentTest + ".dot");
        }
    }

    @Test
    public void TSCustom3() {
        currentTest = "TSCustom3";
        properties[3] = "+partialtransitionsystemlistener.max_new_states=" + 3;
        if (verifyNoPropertyViolation(properties)) {
            TSCustom(1, 1);
        } else {
            assertFilesEqual(dottyFileName, path + "expected/" + currentTest + ".dot");
        }
    }

    @Test
    public void TSCustom4() {
        currentTest = "TSCustom4";
        properties[3] = "+partialtransitionsystemlistener.max_new_states=" + 4;
        if (verifyNoPropertyViolation(properties)) {
            TSCustom(1, 1);
        } else {
            assertFilesEqual(dottyFileName, path + "expected/" + currentTest + ".dot");
        }
    }

    /*
    ====================================== END TEST SECTION ======================================
     */

    public void TSCustom(Integer width, Integer depth) {
        Random r = new Random();

        for (int i = 0; i < width; i++) {
            if (r.nextBoolean()) {
                for (int j = 0; j < depth; j++) {
                    if (r.nextBoolean()) {
                        String tmp = "Old McDonald ";
                    } else {
                        String tmp = "had a farm, ";
                    }
                }
            } else {
                for (int j = 0; j < depth; j++) {
                    if (r.nextBoolean()) {
                        String tmp = "E-I-";
                    } else {
                        String tmp = "E-I-O ";
                    }
                }
            }
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
