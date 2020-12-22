package de.cruuud.nds;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the ECDP Password Generator
 *
 * All expected values are from running the original C version (https://github.com/KuromeSan/eCDP-Serial-Code)
 * which was used as the source to create this Java version
 */
public class ECDPTest {

    private static ILogger logger;

    @BeforeAll
    public static void setup() {
        logger= System.out::println;
    }

    @Test
    public void tc1_zeroTest() {
        String actualPassword=ECDP.generatePassword(logger,"000000000000","000000","000000");
        assertEquals("111111",actualPassword);
    }

    @Test
    public void tc2_basecaseLessThan7() {
        String actualPassword=ECDP.generatePassword(logger,"600000000000","000000","000000");
        assertEquals("R11111",actualPassword);
    }

    @Test
    public void tc3_basecaseEquals7() {
        String actualPassword=ECDP.generatePassword(logger,"700000000000","000000","000000");
        assertEquals("V11111",actualPassword);
    }

    @Test
    public void tc4_random() {
        String actualPassword=ECDP.generatePassword(logger,"01438BADE227","164332","842231");
        assertEquals("PFNPVY",actualPassword);
    }

    @Test
    public void tc8_random_2() {
        String actualPassword=ECDP.generatePassword(logger,"8F2B4EFCA756","649132","376440");
        assertEquals("YZUMZV",actualPassword);
    }


    @Test
    public void tc5_maxValues() {
        String actualPassword=ECDP.generatePassword(logger,"FFFFFFFFFFFF","999999","999999");
        assertEquals("UGNA4R",actualPassword);
    }

    @Test
    public void tc6_allValues_part1() {
        String actualPassword=ECDP.generatePassword(logger,"123456789ABC","123456","789123");
        assertEquals("ZMVD87",actualPassword);
    }

    @Test
    public void tc7_allValues_part1() {
        String actualPassword=ECDP.generatePassword(logger,"DEF012345678","121121","223344");
        assertEquals("SR87LF",actualPassword);
    }
}
