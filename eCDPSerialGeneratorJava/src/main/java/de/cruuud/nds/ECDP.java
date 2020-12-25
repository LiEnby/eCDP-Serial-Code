package de.cruuud.nds;

/**
 * ECDP Keygenerator (password generator) by Cruuud
 * ------------------------------------------------
 * <p>
 * Based on algorithm reverse engineered by KuromeSan (https://github.com/KuromeSan/eCDP-Serial-Code)
 * <p>
 * ---------------------------------------
 * Explanation of the optimized algorithm:
 * ---------------------------------------
 * The password generator algorithm works like this:
 * <p>
 * 01. Take MAC Address from Nintento DS(i) WiFi Adapter, convert to uppercase, remove all separators
 * Example result: 01438BADE227
 * 02. Take the inputted McDonalds Store Number (6 digit number)
 * Example result: 164332
 * 03. Take the inputted McDonalds Store Management Number of DS Card
 * Example result: 842231
 * 04. Concatenate result from 1,2 and 3:
 * Result: 01438BADE227164332842231
 * Length is always 24 bytes (each value is a hexadecimal value in the range [0-15] when string is converted to
 * hex values)
 * 05. Calculate the modulo 7 from the result of adding all hexadecimal values from step 4
 * There is one exception: if the added value is less than 7, we return from step 5 with value 0 (weird exception)
 * This exception will never occur however, as the MAC address itself will always yield a value bigger than 7
 * (just Google for MAC addresses to find out that you will never have a MAC address of 000000000006 for instance)
 * Result: (0+1+4+3+8+11+10+13+14+2+2+7+1+6+4+3+3+2+8+4+2+2+3+1) = 114
 * Result is bigger than 7, so we take the modulo 7, which is 2 and return that
 * <p>
 * NOTE: The order of the characters is irrelevant for determining this result, as the addition of values
 * is commutative. For determining the final password however, the order of these bytes are relevant!
 * 06. Use the result to select one of 7 LUTs (LookUp Tables) (2 = the third LUT (zero-indexed))
 * 07. Using the LUT, for all 24 byte from the input, rearrange the order of the bytes (shuffling):
 * LUT = 3 --> 0x0F, 0x04, 0x09, 0x03, 0x06, 0x07, 0x11, 0x12, 0x15, 0x16, 0x02, 0x08, 0x05, 0x17, 0x0C, 0x0D, 0x01, 0x18, 0x0B, 0x14, 0x0E, 0x10, 0x13, 0x0A
 * In decimal for clarity --> 15 4 9 3 6 7 17 18 21 22 2 8 5 23 12 13 1 24 11 20 14 16 19 10
 * Each entry in the LUT is one-index and used as a translation table. There are no duplicates in the table (it is a set), in
 * total 24 values in the range [1-24] to shuffle the input (result from step 4, which is also 24 bytes in length)
 * So, we start with an empty result as a shuffle result. Then we start with the first value in the LUT, which is 15.
 * We therefore take the 15th value (because the LUT has one-indexed values) from the input and add it to the result.
 * The 15th value is 4, so the shuffle result is "4".
 * We take the next value from the LUT, which is 4, the 4th value in the input is 3, so shuffle result is "43" now.
 * Final result is 43E4BA32221D837101246382
 * 08. Result from 7 we divide in groups of 4 bytes, so we get a total of 6 groups of 4:
 * 43E4 BA32 221D 8371 0124 6382
 * 09. For each group we generate the hexadecimal value from the string and take the modulo 33:
 * For example: 43E4 % 33 = 16 hexadecimal, 22 in decimal
 * Final result (in decimal) = 22 14 21 22 28 31
 * 10. We now have 6 values, which we use as a zero-indexed value to map it to characters from the following alphabet:
 * 123456789ABCDEFGHJKLMNPQRSTUVWXYZ
 * So this is just a simple Base33 alphabet, interesting to note that the 0, I, O are missing. Don't know why that
 * is.
 * <p>
 * So, for instance, first value is 22, so we take the 23rd (remember, zero-indexed!) value from the alphabet,
 * which is a P, then the 15th value, which is a F, and so on, which finally yields your password:
 * <p>
 * Final password = PFNPVY
 */
public class ECDP {
    private static final byte[][] SHUFFLE_LUT = {
            {0x01, 0x0A, 0x16, 0x04, 0x07, 0x18, 0x0C, 0x10, 0x05, 0x17, 0x09, 0x03, 0x12, 0x08, 0x15, 0x13, 0x0B, 0x02, 0x0F, 0x0D, 0x11, 0x0E, 0x06, 0x14},
            {0x07, 0x0C, 0x0E, 0x11, 0x09, 0x16, 0x10, 0x06, 0x14, 0x0D, 0x01, 0x02, 0x12, 0x08, 0x13, 0x0B, 0x0F, 0x0A, 0x18, 0x15, 0x04, 0x05, 0x03, 0x17},
            {0x0F, 0x04, 0x09, 0x03, 0x06, 0x07, 0x11, 0x12, 0x15, 0x16, 0x02, 0x08, 0x05, 0x17, 0x0C, 0x0D, 0x01, 0x18, 0x0B, 0x14, 0x0E, 0x10, 0x13, 0x0A},
            {0x02, 0x0A, 0x0E, 0x12, 0x0B, 0x03, 0x0C, 0x06, 0x13, 0x07, 0x11, 0x09, 0x15, 0x18, 0x10, 0x17, 0x14, 0x0F, 0x04, 0x01, 0x05, 0x08, 0x16, 0x0D},
            {0x0B, 0x02, 0x09, 0x16, 0x14, 0x01, 0x12, 0x11, 0x15, 0x06, 0x0F, 0x17, 0x07, 0x10, 0x0C, 0x0E, 0x08, 0x18, 0x13, 0x03, 0x0A, 0x0D, 0x04, 0x05},
            {0x09, 0x0F, 0x05, 0x0D, 0x16, 0x15, 0x12, 0x11, 0x03, 0x0A, 0x04, 0x10, 0x0E, 0x14, 0x02, 0x01, 0x13, 0x0C, 0x06, 0x0B, 0x17, 0x18, 0x07, 0x08},
            {0x12, 0x02, 0x0C, 0x09, 0x0D, 0x0E, 0x04, 0x07, 0x16, 0x14, 0x17, 0x01, 0x11, 0x03, 0x10, 0x15, 0x08, 0x0A, 0x05, 0x13, 0x0B, 0x18, 0x0F, 0x06}
    };

    public static final String MASTER_PASSWORD = "0QKDE9";

    public static final String PASSWORD_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    public static final ILogger NULL_LOGGER = data -> {
    };

    public static String generatePassword(final ILogger logger, final String macAddress, final String storeNumber, final String storeManagementNumber) {
        if (macAddress == null || !macAddress.toUpperCase().matches("[0123456789ABCDEF]{12}")) {
            throw new IllegalArgumentException("MAC-Address must be 12 hexadecimal characters in length, no separators!");
        }
        if (storeNumber == null || !storeNumber.matches("[0-9]{6}")) {
            throw new IllegalArgumentException("McDonalds Store Number must be a decimal number of exactly 6 digits!");
        }
        if (storeManagementNumber == null || !storeManagementNumber.matches("[0-9]{6}")) {
            throw new IllegalArgumentException("McDonalds Store Management Number of DS Card must be a decimal number of exactly 6 digits!");
        }
        final String moddedMac = macAddress.toUpperCase();

        logger.logString("- MAC used in generation: " + moddedMac);
        final String aggregate = moddedMac + storeNumber + storeManagementNumber;

        logger.logString("- McDonalds Store Number = " + storeNumber);
        logger.logString("- McDonalds Store Management Number of DS Card = " + storeManagementNumber);

        logger.logString("- Aggregated result for calculation: " + aggregate);
        int lutIndex = determineLUT(logger, aggregate);
        return getPassword(logger, shuffleInput(logger, aggregate, lutIndex));
    }

    private static int determineLUT(final ILogger logger, final String added) {
        int lutIndex = 0;
        String addString = "";
        for (int i = 0; i < added.length(); i++) {
            addString += Integer.parseInt("" + added.charAt(i), 16);
            if (i < (added.length() - 1)) {
                addString += "+";
            }
            lutIndex += Integer.parseInt("" + added.charAt(i), 16);
        }
        logger.logString("- Determine LUT from addition: " + addString);
        if (lutIndex < 7) {
            logger.logString("- Result LUT index = 0 (addition result is less than 7)");
            return 0;
        } else {
            logger.logString(String.format("- Result LUT index = %d (%d mod 7)", lutIndex % 7, lutIndex));
            return lutIndex % 7;
        }
    }

    private static String shuffleInput(final ILogger logger, final String input, int lutIndex) {
        final StringBuffer result = new StringBuffer();
        logger.logString(String.format("- Shuffling with LUT table %d (LUT index %d + 1)", lutIndex + 1, lutIndex));
        String table = "";
        for (int i = 0; i < SHUFFLE_LUT[0].length; i++) {
            table += (int) SHUFFLE_LUT[lutIndex][i];
            if (i < (SHUFFLE_LUT[0].length - 1)) {
                table += " ";
            }
        }
        logger.logString("  Table (decimal) = " + table);

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(SHUFFLE_LUT[lutIndex][i] - 1);
            logger.logString(String.format("  Input character %02d (one-indexed) = %c", (int) (SHUFFLE_LUT[lutIndex][i]), c));
            result.append(c);
        }
        logger.logString("  ------------------------------------");
        logger.logString("  Result = " + result.toString());
        logger.logString("  ------------------------------------");
        return result.toString();
    }

    private static String getPassword(final ILogger logger, final String shuffledResult) {
        final StringBuffer result = new StringBuffer();
        logger.logString("- Generating password ...");
        logger.logString("  Password alphabet = " + PASSWORD_ALPHABET);
        for (int i = 0; i < shuffledResult.length() / 4; i++) {
            int digit = Integer.parseInt(shuffledResult.substring(i * 4, i * 4 + 4), 16);
            logger.logString(String.format("  Calculate password char %d -> %04X modulo 33 = %02d", i + 1, digit, digit % 33));
            digit %= 33;
            logger.logString(String.format("     Char %02d (one-indexed) from password alphabet = %c", digit + 1, PASSWORD_ALPHABET.charAt(digit)));
            result.append(PASSWORD_ALPHABET.charAt(digit));
        }
        logger.logString("");
        logger.logString("  PASSWORD = " + result.toString());
        return result.toString();
    }

    public static long reverseFromPassword(final ILogger logger, final String mac, final String password, long maxPasswords, int forceTable) {
        if (logger == null || mac == null || password == null || password.length() != 6) {
            return 0;
        }
        //Calculate the expected remainders
        int expectedPasswordPartModulos[] = new int[6];
        for (int i = 0; i < password.length(); i++) {
            expectedPasswordPartModulos[i] = PASSWORD_ALPHABET.indexOf(password.toUpperCase().charAt(i));
            if (expectedPasswordPartModulos[i] < 0) {
                return 0;
            }
        }
        //Prefill the 7 tables with MAC address
        byte prefilledTables[][] = new byte[7][24];
        for (int table = 0; table < 7; table++) {
            for (int tableItem = 0; tableItem < 24; tableItem++) {
                int idx = SHUFFLE_LUT[table][tableItem] - 1;
                //Is it character from MAC?
                if (idx <= 11) {
                    prefilledTables[table][tableItem] = (byte) Integer.parseInt("" + mac.charAt(idx), 16);
                } else {
                    prefilledTables[table][tableItem] = (byte) 0xFF;
                }
            }
        }
        long currentCount = 0;

        if (currentCount < maxPasswords && (forceTable == 0 || forceTable == 1)) {
            currentCount = tryTable1(logger, mac, password, prefilledTables, expectedPasswordPartModulos, maxPasswords, currentCount);
        }
        if (currentCount < maxPasswords && (forceTable == 0 || forceTable == 2)) {
            currentCount = tryTable2(logger, mac, password, prefilledTables, expectedPasswordPartModulos, maxPasswords, currentCount);
        }
        if (currentCount < maxPasswords && (forceTable == 0 || forceTable == 3)) {
            currentCount = tryTable3(logger, mac, password, prefilledTables, expectedPasswordPartModulos, maxPasswords, currentCount);
        }
        if (currentCount < maxPasswords && (forceTable == 0 || forceTable == 4)) {
            currentCount = tryTable4(logger, mac, password, prefilledTables, expectedPasswordPartModulos, maxPasswords, currentCount);
        }
        if (currentCount < maxPasswords && (forceTable == 0 || forceTable == 5)) {
            currentCount = tryTable5(logger, mac, password, prefilledTables, expectedPasswordPartModulos, maxPasswords, currentCount);
        }
        if (currentCount < maxPasswords && (forceTable == 0 || forceTable == 6)) {
            currentCount = tryTable6(logger, mac, password, prefilledTables, expectedPasswordPartModulos, maxPasswords, currentCount);
        }
        if (currentCount < maxPasswords && (forceTable == 0 || forceTable == 7)) {
            currentCount = tryTable7(logger, mac, password, prefilledTables, expectedPasswordPartModulos, maxPasswords, currentCount);
        }
        return currentCount;
    }

    public static long tryTable1(final ILogger logger, final String mac, final String password, final byte[][] prefilledTables, final int[] expectedPasswordPartModulos, long maxPasswords, long currentCountPasswords) {
        int table = 0;
        for (int p1 = 0; p1 < 10; p1++) {
            int startIndex = 0;
            prefilledTables[table][2] = (byte) p1;
            int sum = prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex];
            if ((sum % 33) == expectedPasswordPartModulos[0]) {
                for (int p2 = 0; p2 < 10; p2++) {
                    for (int p3 = 0; p3 < 10; p3++) {
                        startIndex = 4;
                        prefilledTables[table][5] = (byte) p2;
                        prefilledTables[table][7] = (byte) p3;
                        sum = prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex];
                        if ((sum % 33) == expectedPasswordPartModulos[1]) {
                            for (int p4 = 0; p4 < 10; p4++) {
                                startIndex = 8;
                                prefilledTables[table][9] = (byte) p4;
                                sum = prefilledTables[table][startIndex++];
                                sum <<= 4;
                                sum += prefilledTables[table][startIndex++];
                                sum <<= 4;
                                sum += prefilledTables[table][startIndex++];
                                sum <<= 4;
                                sum += prefilledTables[table][startIndex];
                                if ((sum % 33) == expectedPasswordPartModulos[2]) {
                                    for (int p5 = 0; p5 < 10; p5++) {
                                        for (int p6 = 0; p6 < 10; p6++) {
                                            for (int p7 = 0; p7 < 10; p7++) {
                                                startIndex = 12;
                                                prefilledTables[table][12] = (byte) p5;
                                                prefilledTables[table][14] = (byte) p6;
                                                prefilledTables[table][15] = (byte) p7;
                                                sum = prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex];
                                                if ((sum % 33) == expectedPasswordPartModulos[3]) {
                                                    for (int p8 = 0; p8 < 10; p8++) {
                                                        for (int p9 = 0; p9 < 10; p9++) {
                                                            startIndex = 16;
                                                            prefilledTables[table][18] = (byte) p8;
                                                            prefilledTables[table][19] = (byte) p9;
                                                            sum = prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex];
                                                            if ((sum % 33) == expectedPasswordPartModulos[4]) {
                                                                for (int p10 = 0; p10 < 10; p10++) {
                                                                    for (int p11 = 0; p11 < 10; p11++) {
                                                                        for (int p12 = 0; p12 < 10; p12++) {
                                                                            startIndex = 20;
                                                                            prefilledTables[table][20] = (byte) p10;
                                                                            prefilledTables[table][21] = (byte) p11;
                                                                            prefilledTables[table][23] = (byte) p12;
                                                                            sum = prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex];
                                                                            //Found a possible correct table, matching the password and filled in
                                                                            //with valid store and store management numbers! Still need to check if the LUT index is still valid though!
                                                                            if ((sum % 33) == expectedPasswordPartModulos[5] && determineLUT(NULL_LOGGER, tableToHex(prefilledTables[table])) == table) {
                                                                                logger.logString(String.format("[shuffle-table=%d][mac-address=%s]%s[password=%s]", table + 1, mac, extractStoreAndStoreManagement(table, prefilledTables[table]), password));
                                                                                currentCountPasswords++;
                                                                                if (currentCountPasswords >= maxPasswords) {
                                                                                    return maxPasswords;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return currentCountPasswords;
    }

    public static long tryTable2(final ILogger logger, final String mac, final String password, final byte[][] prefilledTables, final int[] expectedPasswordPartModulos, long maxPasswords, long currentCountPasswords) {
        int table = 1;
        for (int p1 = 0; p1 < 10; p1++) {
            for (int p2 = 0; p2 < 10; p2++) {
                int startIndex = 0;
                prefilledTables[table][2] = (byte) p1;
                prefilledTables[table][3] = (byte) p2;
                int sum = prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex];
                if ((sum % 33) == expectedPasswordPartModulos[0]) {
                    for (int p3 = 0; p3 < 10; p3++) {
                        for (int p4 = 0; p4 < 10; p4++) {
                            startIndex = 4;
                            prefilledTables[table][5] = (byte) p3;
                            prefilledTables[table][6] = (byte) p4;
                            sum = prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex];
                            if ((sum % 33) == expectedPasswordPartModulos[1]) {
                                for (int p5 = 0; p5 < 10; p5++) {
                                    for (int p6 = 0; p6 < 10; p6++) {
                                        startIndex = 8;
                                        prefilledTables[table][8] = (byte) p5;
                                        prefilledTables[table][9] = (byte) p6;
                                        sum = prefilledTables[table][startIndex++];
                                        sum <<= 4;
                                        sum += prefilledTables[table][startIndex++];
                                        sum <<= 4;
                                        sum += prefilledTables[table][startIndex++];
                                        sum <<= 4;
                                        sum += prefilledTables[table][startIndex];
                                        if ((sum % 33) == expectedPasswordPartModulos[2]) {
                                            for (int p7 = 0; p7 < 10; p7++) {
                                                for (int p8 = 0; p8 < 10; p8++) {
                                                    startIndex = 12;
                                                    prefilledTables[table][12] = (byte) p7;
                                                    prefilledTables[table][14] = (byte) p8;
                                                    sum = prefilledTables[table][startIndex++];
                                                    sum <<= 4;
                                                    sum += prefilledTables[table][startIndex++];
                                                    sum <<= 4;
                                                    sum += prefilledTables[table][startIndex++];
                                                    sum <<= 4;
                                                    sum += prefilledTables[table][startIndex];
                                                    if ((sum % 33) == expectedPasswordPartModulos[3]) {
                                                        for (int p9 = 0; p9 < 10; p9++) {
                                                            for (int p10 = 0; p10 < 10; p10++) {
                                                                for (int p11 = 0; p11 < 10; p11++) {
                                                                    startIndex = 16;
                                                                    prefilledTables[table][16] = (byte) p9;
                                                                    prefilledTables[table][18] = (byte) p10;
                                                                    prefilledTables[table][19] = (byte) p11;
                                                                    sum = prefilledTables[table][startIndex++];
                                                                    sum <<= 4;
                                                                    sum += prefilledTables[table][startIndex++];
                                                                    sum <<= 4;
                                                                    sum += prefilledTables[table][startIndex++];
                                                                    sum <<= 4;
                                                                    sum += prefilledTables[table][startIndex];
                                                                    if ((sum % 33) == expectedPasswordPartModulos[4]) {
                                                                        for (int p12 = 0; p12 < 10; p12++) {
                                                                            startIndex = 20;
                                                                            prefilledTables[table][23] = (byte) p12;
                                                                            sum = prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex];
                                                                            //Found a possible correct table, matching the password and filled in
                                                                            //with valid store and store management numbers! Still need to check if the LUT index is still valid though!
                                                                            if ((sum % 33) == expectedPasswordPartModulos[5] && determineLUT(NULL_LOGGER, tableToHex(prefilledTables[table])) == table) {
                                                                                logger.logString(String.format("[shuffle-table=%d][mac-address=%s]%s[password=%s]", table + 1, mac, extractStoreAndStoreManagement(table, prefilledTables[table]), password));
                                                                                currentCountPasswords++;
                                                                                if (currentCountPasswords >= maxPasswords) {
                                                                                    return maxPasswords;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return currentCountPasswords;
    }

    public static long tryTable3(final ILogger logger, final String mac, final String password, final byte[][] prefilledTables, final int[] expectedPasswordPartModulos, long maxPasswords, long currentCountPasswords) {
        int table = 2;
        for (int p1 = 0; p1 < 10; p1++) {
            int startIndex = 0;
            prefilledTables[table][0] = (byte) p1;
            int sum = prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex];
            if ((sum % 33) == expectedPasswordPartModulos[0]) {
                for (int p3 = 0; p3 < 10; p3++) {
                    for (int p4 = 0; p4 < 10; p4++) {
                        startIndex = 4;
                        prefilledTables[table][6] = (byte) p3;
                        prefilledTables[table][7] = (byte) p4;
                        sum = prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex];
                        if ((sum % 33) == expectedPasswordPartModulos[1]) {
                            for (int p5 = 0; p5 < 10; p5++) {
                                for (int p6 = 0; p6 < 10; p6++) {
                                    startIndex = 8;
                                    prefilledTables[table][8] = (byte) p5;
                                    prefilledTables[table][9] = (byte) p6;
                                    sum = prefilledTables[table][startIndex++];
                                    sum <<= 4;
                                    sum += prefilledTables[table][startIndex++];
                                    sum <<= 4;
                                    sum += prefilledTables[table][startIndex++];
                                    sum <<= 4;
                                    sum += prefilledTables[table][startIndex];
                                    if ((sum % 33) == expectedPasswordPartModulos[2]) {
                                        for (int p7 = 0; p7 < 10; p7++) {
                                            for (int p8 = 0; p8 < 10; p8++) {
                                                startIndex = 12;
                                                prefilledTables[table][13] = (byte) p7;
                                                prefilledTables[table][15] = (byte) p8;
                                                sum = prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex];
                                                if ((sum % 33) == expectedPasswordPartModulos[3]) {
                                                    for (int p9 = 0; p9 < 10; p9++) {
                                                        for (int p10 = 0; p10 < 10; p10++) {
                                                            startIndex = 16;
                                                            prefilledTables[table][17] = (byte) p9;
                                                            prefilledTables[table][19] = (byte) p10;
                                                            sum = prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex];
                                                            if ((sum % 33) == expectedPasswordPartModulos[4]) {
                                                                for (int p12 = 0; p12 < 10; p12++) {
                                                                    for (int p13 = 0; p13 < 10; p13++) {
                                                                        for (int p14 = 0; p14 < 10; p14++) {
                                                                            startIndex = 20;
                                                                            prefilledTables[table][20] = (byte) p12;
                                                                            prefilledTables[table][21] = (byte) p13;
                                                                            prefilledTables[table][22] = (byte) p14;
                                                                            sum = prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex];
                                                                            //Found a possible correct table, matching the password and filled in
                                                                            //with valid store and store management numbers! Still need to check if the LUT index is still valid though!
                                                                            if ((sum % 33) == expectedPasswordPartModulos[5] && determineLUT(NULL_LOGGER, tableToHex(prefilledTables[table])) == table) {
                                                                                logger.logString(String.format("[shuffle-table=%d][mac-address=%s]%s[password=%s]", table + 1, mac, extractStoreAndStoreManagement(table, prefilledTables[table]), password));
                                                                                currentCountPasswords++;
                                                                                if (currentCountPasswords >= maxPasswords) {
                                                                                    return maxPasswords;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return currentCountPasswords;
    }

    public static long tryTable4(final ILogger logger, final String mac, final String password, final byte[][] prefilledTables, final int[] expectedPasswordPartModulos, long maxPasswords, long currentCountPasswords) {
        int table = 3;

        int startIndex = 4;
        int sum = prefilledTables[table][startIndex++];
        sum <<= 4;
        sum += prefilledTables[table][startIndex++];
        sum <<= 4;
        sum += prefilledTables[table][startIndex++];
        sum <<= 4;
        sum += prefilledTables[table][startIndex];
        //For this table, one part is completely filled with mac address, so if that is already wrong, we can return
        if (!((sum % 33) == expectedPasswordPartModulos[1])) {
            return currentCountPasswords;
        }

        for (int p1 = 0; p1 < 10; p1++) {
            for (int p2 = 0; p2 < 10; p2++) {
                startIndex = 0;
                prefilledTables[table][2] = (byte) p1;
                prefilledTables[table][3] = (byte) p2;
                sum = prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex];
                if ((sum % 33) == expectedPasswordPartModulos[0]) {
                    for (int p3 = 0; p3 < 10; p3++) {
                        for (int p4 = 0; p4 < 10; p4++) {
                            startIndex = 8;
                            prefilledTables[table][8] = (byte) p3;
                            prefilledTables[table][10] = (byte) p4;
                            sum = prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex];
                            if ((sum % 33) == expectedPasswordPartModulos[2]) {
                                for (int p5 = 0; p5 < 10; p5++) {
                                    for (int p6 = 0; p6 < 10; p6++) {
                                        for (int p52 = 0; p52 < 10; p52++) {
                                            for (int p62 = 0; p62 < 10; p62++) {
                                                startIndex = 12;
                                                prefilledTables[table][12] = (byte) p5;
                                                prefilledTables[table][13] = (byte) p6;
                                                prefilledTables[table][14] = (byte) p52;
                                                prefilledTables[table][15] = (byte) p62;
                                                sum = prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex++];
                                                sum <<= 4;
                                                sum += prefilledTables[table][startIndex];
                                                if ((sum % 33) == expectedPasswordPartModulos[3]) {
                                                    for (int p7 = 0; p7 < 10; p7++) {
                                                        for (int p8 = 0; p8 < 10; p8++) {
                                                            startIndex = 16;
                                                            prefilledTables[table][16] = (byte) p7;
                                                            prefilledTables[table][17] = (byte) p8;
                                                            sum = prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex++];
                                                            sum <<= 4;
                                                            sum += prefilledTables[table][startIndex];
                                                            if ((sum % 33) == expectedPasswordPartModulos[4]) {
                                                                for (int p9 = 0; p9 < 10; p9++) {
                                                                    for (int p10 = 0; p10 < 10; p10++) {
                                                                        startIndex = 20;
                                                                        prefilledTables[table][22] = (byte) p9;
                                                                        prefilledTables[table][23] = (byte) p10;
                                                                        sum = prefilledTables[table][startIndex++];
                                                                        sum <<= 4;
                                                                        sum += prefilledTables[table][startIndex++];
                                                                        sum <<= 4;
                                                                        sum += prefilledTables[table][startIndex++];
                                                                        sum <<= 4;
                                                                        sum += prefilledTables[table][startIndex];
                                                                        if ((sum % 33) == expectedPasswordPartModulos[5] && determineLUT(NULL_LOGGER, tableToHex(prefilledTables[table])) == table) {
                                                                            logger.logString(String.format("[shuffle-table=%d][mac-address=%s]%s[password=%s]", table + 1, mac, extractStoreAndStoreManagement(table, prefilledTables[table]), password));
                                                                            currentCountPasswords++;
                                                                            if (currentCountPasswords >= maxPasswords) {
                                                                                return maxPasswords;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return currentCountPasswords;
    }

    public static long tryTable5(final ILogger logger, final String mac, final String password, final byte[][] prefilledTables, final int[] expectedPasswordPartModulos, long maxPasswords, long currentCountPasswords) {
        int table = 4;
        for (int p1 = 0; p1 < 10; p1++) {
            int startIndex = 0;
            prefilledTables[table][3] = (byte) p1;
            int sum = prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex];
            if ((sum % 33) == expectedPasswordPartModulos[0]) {
                for (int p3 = 0; p3 < 10; p3++) {
                    for (int p4 = 0; p4 < 10; p4++) {
                        for (int p42 = 0; p42 < 10; p42++) {
                            startIndex = 4;
                            prefilledTables[table][4] = (byte) p3;
                            prefilledTables[table][6] = (byte) p4;
                            prefilledTables[table][7] = (byte) p42;
                            sum = prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex++];
                            sum <<= 4;
                            sum += prefilledTables[table][startIndex];
                            if ((sum % 33) == expectedPasswordPartModulos[1]) {
                                for (int p5 = 0; p5 < 10; p5++) {
                                    for (int p6 = 0; p6 < 10; p6++) {
                                        for (int p62 = 0; p62 < 10; p62++) {
                                            startIndex = 8;
                                            prefilledTables[table][8] = (byte) p5;
                                            prefilledTables[table][10] = (byte) p6;
                                            prefilledTables[table][11] = (byte) p62;
                                            sum = prefilledTables[table][startIndex++];
                                            sum <<= 4;
                                            sum += prefilledTables[table][startIndex++];
                                            sum <<= 4;
                                            sum += prefilledTables[table][startIndex++];
                                            sum <<= 4;
                                            sum += prefilledTables[table][startIndex];
                                            if ((sum % 33) == expectedPasswordPartModulos[2]) {
                                                for (int p7 = 0; p7 < 10; p7++) {
                                                    for (int p8 = 0; p8 < 10; p8++) {
                                                        startIndex = 12;
                                                        prefilledTables[table][13] = (byte) p7;
                                                        prefilledTables[table][15] = (byte) p8;
                                                        sum = prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex];
                                                        if ((sum % 33) == expectedPasswordPartModulos[3]) {
                                                            for (int p9 = 0; p9 < 10; p9++) {
                                                                for (int p10 = 0; p10 < 10; p10++) {
                                                                    startIndex = 16;
                                                                    prefilledTables[table][17] = (byte) p9;
                                                                    prefilledTables[table][18] = (byte) p10;
                                                                    sum = prefilledTables[table][startIndex++];
                                                                    sum <<= 4;
                                                                    sum += prefilledTables[table][startIndex++];
                                                                    sum <<= 4;
                                                                    sum += prefilledTables[table][startIndex++];
                                                                    sum <<= 4;
                                                                    sum += prefilledTables[table][startIndex];
                                                                    if ((sum % 33) == expectedPasswordPartModulos[4]) {
                                                                        for (int p12 = 0; p12 < 10; p12++) {
                                                                            startIndex = 20;
                                                                            prefilledTables[table][21] = (byte) p12;
                                                                            sum = prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex];
                                                                            //Found a possible correct table, matching the password and filled in
                                                                            //with valid store and store management numbers! Still need to check if the LUT index is still valid though!
                                                                            if ((sum % 33) == expectedPasswordPartModulos[5] && determineLUT(NULL_LOGGER, tableToHex(prefilledTables[table])) == table) {
                                                                                logger.logString(String.format("[shuffle-table=%d][mac-address=%s]%s[password=%s]", table + 1, mac, extractStoreAndStoreManagement(table, prefilledTables[table]), password));
                                                                                currentCountPasswords++;
                                                                                if (currentCountPasswords >= maxPasswords) {
                                                                                    return maxPasswords;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return currentCountPasswords;
    }

    public static long tryTable6(final ILogger logger, final String mac, final String password, final byte[][] prefilledTables, final int[] expectedPasswordPartModulos, long maxPasswords, long currentCountPasswords) {
        int table = 5;
        for (int p1 = 0; p1 < 10; p1++) {
            for (int p12 = 0; p12 < 10; p12++) {
                int startIndex = 0;
                prefilledTables[table][1] = (byte) p1;
                prefilledTables[table][3] = (byte) p12;
                int sum = prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex++];
                sum <<= 4;
                sum += prefilledTables[table][startIndex];
                if ((sum % 33) == expectedPasswordPartModulos[0]) {
                    for (int p3 = 0; p3 < 10; p3++) {
                        for (int p4 = 0; p4 < 10; p4++) {
                            for (int p42 = 0; p42 < 10; p42++) {
                                for (int p43 = 0; p43 < 10; p43++) {
                                    startIndex = 4;
                                    prefilledTables[table][4] = (byte) p3;
                                    prefilledTables[table][5] = (byte) p4;
                                    prefilledTables[table][6] = (byte) p42;
                                    prefilledTables[table][7] = (byte) p43;
                                    sum = prefilledTables[table][startIndex++];
                                    sum <<= 4;
                                    sum += prefilledTables[table][startIndex++];
                                    sum <<= 4;
                                    sum += prefilledTables[table][startIndex++];
                                    sum <<= 4;
                                    sum += prefilledTables[table][startIndex];
                                    if ((sum % 33) == expectedPasswordPartModulos[1]) {
                                        for (int p5 = 0; p5 < 10; p5++) {
                                            startIndex = 8;
                                            prefilledTables[table][11] = (byte) p5;
                                            sum = prefilledTables[table][startIndex++];
                                            sum <<= 4;
                                            sum += prefilledTables[table][startIndex++];
                                            sum <<= 4;
                                            sum += prefilledTables[table][startIndex++];
                                            sum <<= 4;
                                            sum += prefilledTables[table][startIndex];
                                            if ((sum % 33) == expectedPasswordPartModulos[2]) {
                                                for (int p7 = 0; p7 < 10; p7++) {
                                                    for (int p8 = 0; p8 < 10; p8++) {
                                                        startIndex = 12;
                                                        prefilledTables[table][12] = (byte) p7;
                                                        prefilledTables[table][13] = (byte) p8;
                                                        sum = prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex];
                                                        if ((sum % 33) == expectedPasswordPartModulos[3]) {
                                                            for (int p9 = 0; p9 < 10; p9++) {
                                                                startIndex = 16;
                                                                prefilledTables[table][16] = (byte) p9;
                                                                sum = prefilledTables[table][startIndex++];
                                                                sum <<= 4;
                                                                sum += prefilledTables[table][startIndex++];
                                                                sum <<= 4;
                                                                sum += prefilledTables[table][startIndex++];
                                                                sum <<= 4;
                                                                sum += prefilledTables[table][startIndex];
                                                                if ((sum % 33) == expectedPasswordPartModulos[4]) {
                                                                    for (int p10 = 0; p10 < 10; p10++) {
                                                                        for (int p102 = 0; p102 < 10; p102++) {
                                                                            startIndex = 20;
                                                                            prefilledTables[table][20] = (byte) p10;
                                                                            prefilledTables[table][21] = (byte) p102;
                                                                            sum = prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex];
                                                                            //Found a possible correct table, matching the password and filled in
                                                                            //with valid store and store management numbers! Still need to check if the LUT index is still valid though!
                                                                            if ((sum % 33) == expectedPasswordPartModulos[5] && determineLUT(NULL_LOGGER, tableToHex(prefilledTables[table])) == table) {
                                                                                logger.logString(String.format("[shuffle-table=%d][mac-address=%s]%s[password=%s]", table + 1, mac, extractStoreAndStoreManagement(table, prefilledTables[table]), password));
                                                                                currentCountPasswords++;
                                                                                if (currentCountPasswords >= maxPasswords) {
                                                                                    return maxPasswords;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return currentCountPasswords;
    }

    public static long tryTable7(final ILogger logger, final String mac, final String password, final byte[][] prefilledTables, final int[] expectedPasswordPartModulos, long maxPasswords, long currentCountPasswords) {
        int table = 6;
        for (int p1 = 0; p1 < 10; p1++) {
            int startIndex = 0;
            prefilledTables[table][0] = (byte) p1;
            int sum = prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex++];
            sum <<= 4;
            sum += prefilledTables[table][startIndex];
            if ((sum % 33) == expectedPasswordPartModulos[0]) {
                for (int p3 = 0; p3 < 10; p3++) {
                    for (int p4 = 0; p4 < 10; p4++) {
                        startIndex = 4;
                        prefilledTables[table][4] = (byte) p3;
                        prefilledTables[table][5] = (byte) p4;
                        sum = prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex++];
                        sum <<= 4;
                        sum += prefilledTables[table][startIndex];
                        if ((sum % 33) == expectedPasswordPartModulos[1]) {
                            for (int p5 = 0; p5 < 10; p5++) {
                                for (int p6 = 0; p6 < 10; p6++) {
                                    for (int p62 = 0; p62 < 10; p62++) {
                                        startIndex = 8;
                                        prefilledTables[table][8] = (byte) p5;
                                        prefilledTables[table][9] = (byte) p6;
                                        prefilledTables[table][10] = (byte) p62;
                                        sum = prefilledTables[table][startIndex++];
                                        sum <<= 4;
                                        sum += prefilledTables[table][startIndex++];
                                        sum <<= 4;
                                        sum += prefilledTables[table][startIndex++];
                                        sum <<= 4;
                                        sum += prefilledTables[table][startIndex];
                                        if ((sum % 33) == expectedPasswordPartModulos[2]) {
                                            for (int p7 = 0; p7 < 10; p7++) {
                                                for (int p8 = 0; p8 < 10; p8++) {
                                                    for (int p82 = 0; p82 < 10; p82++) {
                                                        startIndex = 12;
                                                        prefilledTables[table][12] = (byte) p7;
                                                        prefilledTables[table][14] = (byte) p8;
                                                        prefilledTables[table][15] = (byte) p82;
                                                        sum = prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex++];
                                                        sum <<= 4;
                                                        sum += prefilledTables[table][startIndex];
                                                        if ((sum % 33) == expectedPasswordPartModulos[3]) {
                                                            for (int p9 = 0; p9 < 10; p9++) {
                                                                startIndex = 16;
                                                                prefilledTables[table][19] = (byte) p9;
                                                                sum = prefilledTables[table][startIndex++];
                                                                sum <<= 4;
                                                                sum += prefilledTables[table][startIndex++];
                                                                sum <<= 4;
                                                                sum += prefilledTables[table][startIndex++];
                                                                sum <<= 4;
                                                                sum += prefilledTables[table][startIndex];
                                                                if ((sum % 33) == expectedPasswordPartModulos[4]) {
                                                                    for (int p12 = 0; p12 < 10; p12++) {
                                                                        for (int p13 = 0; p13 < 10; p13++) {
                                                                            startIndex = 20;
                                                                            prefilledTables[table][21] = (byte) p12;
                                                                            prefilledTables[table][22] = (byte) p13;
                                                                            sum = prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex++];
                                                                            sum <<= 4;
                                                                            sum += prefilledTables[table][startIndex];
                                                                            //Found a possible correct table, matching the password and filled in
                                                                            //with valid store and store management numbers! Still need to check if the LUT index is still valid though!
                                                                            if ((sum % 33) == expectedPasswordPartModulos[5] && determineLUT(NULL_LOGGER, tableToHex(prefilledTables[table])) == table) {
                                                                                logger.logString(String.format("[shuffle-table=%d][mac-address=%s]%s[password=%s]", table + 1, mac, extractStoreAndStoreManagement(table, prefilledTables[table]), password));
                                                                                currentCountPasswords++;
                                                                                if (currentCountPasswords >= maxPasswords) {
                                                                                    return maxPasswords;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return currentCountPasswords;
    }

    private static String tableToHex(final byte[] table) {
        final StringBuffer tableBuffer = new StringBuffer();
        for (int x = 0; x < table.length; x++) {
            tableBuffer.append(String.format("%X", table[x]));
        }
        return tableBuffer.toString();
    }

    private static String extractStoreAndStoreManagement(int tableIndex, final byte[] table) {
        byte store[] = new byte[6];
        byte storeManagement[] = new byte[6];
        for (int x = 0; x < SHUFFLE_LUT[tableIndex].length; x++) {
            int idx = SHUFFLE_LUT[tableIndex][x] - 1;
            if (idx <= 17 && idx >= 12) {
                store[idx - 12] = table[x];
            }
            if (idx <= 23 && idx >= 18) {
                storeManagement[idx - 18] = table[x];
            }
        }
        final StringBuffer storeString = new StringBuffer();
        final StringBuffer storeManagementString = new StringBuffer();
        for (int x = 0; x < 6; x++) {
            storeString.append(Integer.toString(store[x], 16));
            storeManagementString.append(Integer.toString(storeManagement[x], 16));
        }
        return String.format("[store=%s][store management=%s]", storeString, storeManagementString);
    }
}
