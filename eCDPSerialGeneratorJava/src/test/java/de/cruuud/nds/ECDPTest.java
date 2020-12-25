package de.cruuud.nds;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testing the ECDP Password Generator
 * <p>
 * All expected values are from running the original C version (https://github.com/KuromeSan/eCDP-Serial-Code)
 * which was used as the source to create this Java version
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ECDPTest {

    private static ILogger logger;

    @BeforeAll
    public static void setup() {
        logger = System.out::println;
    }

    @Test
    @Order(1)
    public void tc1_zeroTest() {
        String actualPassword = ECDP.generatePassword(logger, "000000000000", "000000", "000000");
        assertEquals("111111", actualPassword);
    }

    @Test
    @Order(2)
    public void tc2_basecaseLessThan7() {
        String actualPassword = ECDP.generatePassword(logger, "600000000000", "000000", "000000");
        assertEquals("R11111", actualPassword);
    }

    @Test
    @Order(3)
    public void tc3_basecaseEquals7() {
        String actualPassword = ECDP.generatePassword(logger, "700000000000", "000000", "000000");
        assertEquals("V11111", actualPassword);
    }

    @Test
    @Order(4)
    public void tc4_random() {
        String actualPassword = ECDP.generatePassword(logger, "01438BADE227", "164332", "842231");
        assertEquals("PFNPVY", actualPassword);
    }

    @Test
    @Order(5)
    public void tc5_random_2() {
        String actualPassword = ECDP.generatePassword(logger, "8F2B4EFCA756", "649132", "376440");
        assertEquals("YZUMZV", actualPassword);
    }

    @Test
    @Order(6)
    public void tc6_maxValues() {
        String actualPassword = ECDP.generatePassword(logger, "FFFFFFFFFFFF", "999999", "999999");
        assertEquals("UGNA4R", actualPassword);
    }

    @Test
    @Order(7)
    public void tc7_allValues_part1() {
        String actualPassword = ECDP.generatePassword(logger, "123456789ABC", "123456", "789123");
        assertEquals("ZMVD87", actualPassword);
    }

    @Test
    @Order(8)
    public void tc8_allValues_part1() {
        String actualPassword = ECDP.generatePassword(logger, "DEF012345678", "121121", "223344");
        assertEquals("SR87LF", actualPassword);
    }

    @Test
    @Order(9)
    public void tc9_reverse_table_1() {
        String mac = "123456789ABC";
        checkReverseTable(mac, "UF3PVX", 1, 1);
    }

    @Test
    @Order(10)
    public void tc10_reverse_table_2() {
        String mac = "123456789ABC";
        checkReverseTable(mac, "YAK53R", 1, 2);
    }

    @Test
    @Order(11)
    public void tc11_reverse_table_3() {
        String mac = "123456789ABC";
        checkReverseTable(mac, "H98F8B", 1, 3);
    }

    @Test
    @Order(12)
    public void tc12_reverse_table_4() {
        String mac = "123456789ABC";
        checkReverseTable(mac, "VMLSZP", 1, 4);
    }

    @Test
    @Order(13)
    public void tc13_reverse_table_5() {
        String mac = "123456789ABC";
        checkReverseTable(mac, "8RKPUB", 1, 5);
    }

    @Test
    @Order(14)
    public void tc14_reverse_table_6() {
        String mac = "123456789ABC";
        checkReverseTable(mac, "JGW1CE", 1, 6);
    }

    @Test
    @Order(15)
    public void tc15_reverse_table_7() {
        String mac = "123456789ABC";
        checkReverseTable(mac, "462AZA", 1, 7);
    }

    @Test
    @Order(16)
    public void tc16_reverse_table_all() {
        String mac = "123456789ABC";
        checkReverseTable(mac, "ABCDEF", 1, 0);
    }

    @Test
    @Order(17)
    public void tc17_reverse_table_no_result() {
        final List<String> result = new ArrayList<>();
        ILogger logger = data -> result.add(data);
        String mac = "123456789ABC";
        String password = "QQQQQQ";
        ECDP.reverseFromPassword(logger, mac, password, 1, 0);
        assertEquals(0, result.size(), "No password was expected!");
    }

    private void checkReverseTable(String mac, String password, int maxPasswords, int table) {
        final List<String> result = new ArrayList<>();
        ILogger logger = data -> result.add(data);
        ECDP.reverseFromPassword(logger, mac, password, maxPasswords, table);
        checkPasswordResult(result, mac, password);
    }

    private void checkPasswordResult(List<String> result, String mac, String expectedPassword) {
        if (result.size() == 1) {
            int idxStore = result.get(0).indexOf("store=");
            if (idxStore >= 0) {
                String fromStore = result.get(0).substring(idxStore + 6, idxStore + 6 + 6);
                int idxStoreManagement = result.get(0).indexOf("store management=");
                if (idxStoreManagement >= 0) {
                    String fromStoreManagement = result.get(0).substring(idxStoreManagement + 17, idxStoreManagement + 6 + 17);
                    assertEquals(expectedPassword, ECDP.generatePassword(ECDP.NULL_LOGGER, mac, fromStore, fromStoreManagement), "Password from reversing store and store management numbers based on MAC address does not match generated password with the MAC address,store and store management combination!");
                } else {
                    fail("No store management number in result in expected format!");
                }
            } else {
                fail("No store number in result in expected format!");
            }
        } else {
            fail("No password found, but expected exactly one!");
        }
    }
}
