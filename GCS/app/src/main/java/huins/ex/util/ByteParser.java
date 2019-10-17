package huins.ex.util;

import java.util.List;

/**
 * Created by suhak on 15. 7. 23.
 */
public class ByteParser {
/***************************************************************************/
    /*************************** type casting & parser ****************/
    /***************************************************************************/
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
        {
            sb.append(String.format("%02X ", b&0xff));
        }
        return sb.toString();
    }

    public static String byteArrayToNHex(byte[] a, int length) {
        int index = 1;
        StringBuilder sb = new StringBuilder();

        for(final byte b: a)
        {
            sb.append(String.format("%02X ", b&0xff));
            if(index++ == length)
                break;
        }
        return sb.toString();
    }
    public static String ByteArrayToHex(Byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
            sb.append(String.format("0x%02X ", b&0xff));
        return sb.toString();
    }

    public static String charArrayToHex(char[] a) {
        StringBuilder sb = new StringBuilder();
        for(final char b: a)
            sb.append(String.format("0x%02X ", b&0xff));
        return sb.toString();
    }
    public static String byteArrayToString(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
            sb.append(String.format("%c", b&0xff));
        return sb.toString();
    }

    public static String byteArrayToString(byte[] a, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < length + offset && i <= a.length; i++) {
            sb.append(String.format("%c", a[i]&0xff));

        }
        return sb.toString();
    }

    public static String charArrayToString(char[] a) {
        StringBuilder sb = new StringBuilder();
        for(final char b: a)
            sb.append(String.format("%c", b&0xff));
        return sb.toString();
    }

    public static byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }
}
