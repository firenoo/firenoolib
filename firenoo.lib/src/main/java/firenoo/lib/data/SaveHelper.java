package firenoo.lib.data;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * I/O Helper with writing in and out of streams
 * NOTE THAT THIS REVERSES THE ORDER OF BYTES WHEN WRITING,
 * AND UNREVERSES IT WHEN READING.
 * @author Firenoo 6/27/2018.
 */
public final class SaveHelper {
 
    /*-------------Writer-------------*/

    public static int writeByte(byte b, OutputStream stream) throws IOException {
        stream.write(b);
        return 1;
    }

    public static int writeShort(short s, OutputStream stream) throws IOException {
        for(int i = 0; i < 2; i++) {
            stream.write((byte) s);
            s >>>= 8;
        }
        return 2;
    }

    public static int writeFloat(float f, OutputStream stream) throws IOException {
        int target = Float.floatToRawIntBits(f);
        for(int i = 0; i < 4; i++) {
            stream.write((byte) target);
            target >>>= 8;
        }
        return 4;
    }
    
    public static int writeChar(char c, OutputStream stream) throws IOException {
        for(int i = 0; i < 2; i++) {
            stream.write((byte) c);
            c >>>= 8;
        }
        return 2;
    }    

    public static int writeInt(int i, OutputStream stream) throws IOException {
        for(int j = 0; j < 4; j++) {
            stream.write((byte) i);
            i >>>= 8;
        }
        return 4;
    }
    
    public static int writeLong(long l, OutputStream stream) throws IOException {
        for(int i = 0; i < 8; i++) {
            stream.write((byte) l);
            l >>>= 8;
        }
        return 8;
    }
    
    public static int writeDouble(double d, OutputStream stream) throws IOException {
        long l = Double.doubleToRawLongBits(d);
        for(int i = 0; i < 8; i++) {
            stream.write((byte) l);
            l >>>= 8;
        }
        return 8;
    }

    public static int writeString(String s, OutputStream stream) throws IOException {
        int totalBytes = writeInt(s.length(), stream);
        char[] chars = s.toCharArray();
        for(char c : chars) {
            totalBytes += writeChar(c, stream);
        }
        return totalBytes;
    }
    
    /*----------------Readers----------------*/

    
    
    private static long read(InputStream stream, int byteCount) throws IOException {
        byte b[] = new byte[byteCount];
        int s = stream.read(b);
        if(s == byteCount) {
            long result = 0;
            long mask = 0xFF;
            for(int i = 0; i < b.length; i++) {
                mask |= (long)0xFF << 8 * i;
                // 1. Append the byte to the tail end of the long.
                // 2. Mask to clean output
                result |= ((long)b[i]) << (8 * i);
                result &= mask;
            }

            return result;
        }else throw new IOException("Could not read the specified number" +
        "of bytes, Expected " + byteCount + ", received " + s + ".");
    }

    public static char readChar(InputStream stream) throws IOException {
        return (char) read(stream, 2);
    }

    public static short readShort(InputStream stream) throws IOException {
        return (short) read(stream, 2);
    }
    
    public static float readFloat(InputStream stream) throws IOException {
        return Float.intBitsToFloat(readInt(stream));
    }

    public static int readInt(InputStream stream) throws IOException {
        return (int) read(stream, 4);
    }

    public static long readLong(InputStream stream) throws IOException {
        return read(stream, 8);
    }

    public static double readDouble(InputStream stream) throws IOException {
        return Double.longBitsToDouble(readLong(stream));
    }

    public static String readString(InputStream stream) throws IOException {
        int length = readInt(stream);
        StringBuilder result = new StringBuilder();
        char c1 = 0, c2 = 0;
        boolean lower = false;
        for(int i = 0; i < length; i++) {
            if(!lower) {
                c1 = readChar(stream);
                if(Character.isLowSurrogate(c1)) {
                    lower = true;
                }else {
                    result.append(c1);
                }
            }else{
                c2 = readChar(stream);
                if(Character.isHighSurrogate(c2)) {
                    lower = false;
                    result.appendCodePoint(Character.toCodePoint(c1, c2));
                }else {
                    result.append(c1);
                    result.append(c2);
                    lower = false;
                }
            }
        }
        return result.toString();
    }
    
}
