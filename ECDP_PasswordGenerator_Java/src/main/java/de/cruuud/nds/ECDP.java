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

    private static final String PASSWORD_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

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
        StringBuffer result = new StringBuffer();
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
        StringBuffer result = new StringBuffer();
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


}
