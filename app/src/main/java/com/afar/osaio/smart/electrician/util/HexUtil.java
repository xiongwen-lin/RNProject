package com.afar.osaio.smart.electrician.util;

public class HexUtil {

    public static String encodeToString(byte bytes[]) {
        char encodedChars[] = encode(bytes);
        return new String(encodedChars);
    }

    public static char[] encode(byte data[]) {
        int l = data.length;
        char out[] = new char[l << 1];
        int i = 0;
        int j = 0;
        for (; i < l; i++) {
            out[j++] = DIGITS[(240 & data[i]) >>> 4];
            out[j++] = DIGITS[15 & data[i]];
        }

        return out;
    }

    public static byte[] decode(String hex) {
        return decode(hex.toCharArray());
    }

    public static byte[] decode(char data[]) throws IllegalArgumentException {
        int len = data.length;
        if ((len & 1) != 0)
            throw new IllegalArgumentException("Odd number of characters.");
        byte out[] = new byte[len >> 1];
        int i = 0;
        for (int j = 0; j < len;) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f |= toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 255);
            i++;
        }

        return out;
    }

    protected static int toDigit(char ch, int index) throws IllegalArgumentException {
        int digit = Character.digit(ch, 16);
        if (digit == -1)
            throw new IllegalArgumentException((new StringBuilder()).append("Illegal hexadecimal charcter ").append(ch)
                    .append(" at index ").append(index).toString());
        else
            return digit;
    }

    private static final char DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

}
