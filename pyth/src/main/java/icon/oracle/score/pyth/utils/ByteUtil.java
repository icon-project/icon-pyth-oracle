package icon.oracle.score.pyth.utils;

public class ByteUtil {
    // Constants for the length of each unsigned data type
    public static final int U8_LENGTH = 1;
    public static final int U16_LENGTH = 2;
    public static final int U32_LENGTH = 4;
    public static final int U64_LENGTH = 8;

    // Read an unsigned 8-bit integer
    public static int readU8(byte[] data, int position) {
        return data[position] & 0xFF;
    }

    // Read an unsigned 16-bit integer
    public static int readU16(byte[] data, int position) {
        return ((data[position] & 0xFF) << 8) |
                (data[position + 1] & 0xFF);
    }

    // Read an unsigned 32-bit integer
    public static long readU32(byte[] data, int position) {
        return ((data[position] & 0xFFL) << 24) |
                ((data[position + 1] & 0xFFL) << 16) |
                ((data[position + 2] & 0xFFL) << 8) |
                (data[position + 3] & 0xFFL);
    }

    // Read an unsigned 64-bit integer
    public static long readU64(byte[] data, int position) {
        return ((data[position] & 0xFFL) << 56) |
                ((data[position + 1] & 0xFFL) << 48) |
                ((data[position + 2] & 0xFFL) << 40) |
                ((data[position + 3] & 0xFFL) << 32) |
                ((data[position + 4] & 0xFFL) << 24) |
                ((data[position + 5] & 0xFFL) << 16) |
                ((data[position + 6] & 0xFFL) << 8) |
                (data[position + 7] & 0xFFL);
    }

    // Read a signed 32-bit integer
    public static int readI32(byte[] data, int position) {
        return (data[position] << 24) |
                ((data[position + 1] & 0xFF) << 16) |
                ((data[position + 2] & 0xFF) << 8) |
                (data[position + 3] & 0xFF);
    }

    public static byte[] encodePacked(byte[]... data) {
        int length = 0;
        for (byte[] bs : data) {
            length += bs.length;
        }

        byte[] result = new byte[length];
        int index = 0;
        for (byte[] bs : data) {
            System.arraycopy(bs, 0, result, index, bs.length);
            index += bs.length;
        }

        return result;
    }
}
