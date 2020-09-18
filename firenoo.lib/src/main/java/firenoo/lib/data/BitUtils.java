package firenoo.lib.data;

/**
 * Provides a set of utilities for performing bit-manipulation.
 */
public final class BitUtils {

    private BitUtils() {}

    /**
     * Converts an integer into an array of 4 bytes.
     * @param input integer to be converted
     * @return a byte array representing the integer starting from the
     *         least-significant byte (at index 0) to the most-siginificant 
     *         byte (at index 3).
     */
    public static byte[] intToByteArray(int input) {
        byte[] result = new byte[4];
        for(int i = 0; i < 4; i++) {
            result[i] = (byte) (input);
            input >>>= 8;
        }
        return result;
    }

    public static int byteArrayToInt(byte[] input) {
        int result = 0;
        for(int i = 0; i < 4; i++) {
            result <<= 8;
            result |= input[3-i] & 0xFF;
        }
        return result;
    }

    /**
     * Gets the value of the bit at the specified position.
     * @param in   Data to test
     * @param pos  Bit position. Position = 0 is the Least-significant bit of the
     *             first byte. Position = 8 is the least-significant bit of the second
     *             byte, etc. 
     * @throws ArrayOutOfBoundsException if position is outside of array bounds
     */
    public static boolean valueAt(byte[] in, int pos) {
        return valueAt(in[pos / 32], pos % 8);
    }

    /**
     * Gets the value of the bit at the specified position.
     * @param in    Data to use
     * @param pos   Bit position. {@code pos=0} is the Least-significant bit.
     * @return
     */
    public static boolean valueAt(int in, int pos) {
        return (in & (1 << pos)) == (1 << (pos));
    }

    /**
     * Same as {@code valueAt(int, int)} but returns 1 if
     * true and 0 if false.
     */
    public static int intValueAt(int in, int pos) {
        return valueAt(in, pos) ? 1 : 0;
    }

    /**
     * Inverts the bit at the specified position
     * @param in    Data to use for inversion
     * @param pos   Bit position. {@code pos=0} is the Least-significant bit,
     *              {@code pos=7} is the Most-significant bit.
     * @return      The modified byte
     */
    public static byte invert(byte in, int pos) {
        return (byte) (in ^ (1 << pos));
        //0 + 0 -> 0
        //1 + 0 -> 1
        //1 + 1 -> 0
    }

    /**
     * Sets the bit at the specified position (to 1).
     * @param in    the data to modify
     * @param pos   Bit position. {@code pos=0} is the Least-significant bit,
     *              {@code pos=7} is the Most-significant bit.
     * @return      The modified byte
     */
    public static byte setBit(byte in, int pos){
        return (byte) (in & (0x1 << pos));
        
    }

    /**
     * Clears the bit at the specified position (set to 0)
     * @param in    the data to modify
     * @param pos   Bit position. {@code pos=0} is the Least-significant bit,
     *              {@code pos=7} is the Most-significant bit.
     * @return      The modified byte
     */
    public static byte clearBit(byte in, int pos) {
        return (byte) (in & (0x0 << pos));
    }

    /**
     * Maps a region of the input data to an integer, masking other parts
     * and moving the resultant data shifted as far right as possible.
     * <p>
     *  Examples:
     *  <p>
     *      <p>
     *          Data: [1 1 1 0 1 0 0 1], begin: 0, end: 4 
     *      </p>
     *      <p>
     *          Result: [0 0 0 0 1 0 0 1].
     *      </p>
     *      <p>
     *          Data: [1 1 1 0 1 0 0 1], begin: 4, end: 8 
     *      </p>
     *      <p>
     *          Result: [0 0 0 0 1 1 1 0].
     *      </p>
     *  </p>
     * </p>
     * @param data  The source region
     * @param start The lower bound (inclusive). This will represent the
     *              least-significant bit of the result.
     * @param end   The upper bound (exclusive). This will represent the
     *              most-significant bit of the result
     * @return  The data within the region specified, shifted as far to the
     * right    as possible without losing data.
     *          Returns 0 if <code>start >= end.</code>
     */
    public static int getRegionAsInt(int in, int begin, int end){
        if(begin >= end) return 0;
        return (in << begin) >>> (32 + begin - end);
    }

    /**
     * Computes the Hamming Distance between 0 and the specified binary input.<br/>
     * Equivalent to the cardinality
     * Brian Kerninghan's Algorithm Implementation - Find the number of
     * set bits in an integer
     * @param in        the data to inspect as a binary number
     * @param begin     where to begin inspection. 0 = first position
     * @param end       where to end inspection, exclusive. Must be at most
     *                  equal to in.length * 32.
     * @return The number of 1s found in the input
     */
    public static int hammingDist(int in, int begin, int end){
        int count = 0;
        checkBounds(begin, 0, 32);
        checkBounds(end, 1, 33);
        int data = getRegionAsInt(in,  begin, end);
        //Brian Kerninghan's Algorithm
        while(data != 0){
            data &= data-1;
            count++;
        }
        return count;
    }

    public static int shiftRight(int in){
        return shiftRight(in, 32);
    }

    /**
     * Shifts all 1's in a binary string as far right as possible, eliminating
     * any 0's in between (compacts them).
     * @param in            data input
     * @param pos           Shifts bits up to this position.
     * @return A binary string with 1's shifted to the right up to <code>pos</code>
     *         (Index starts at 0)
     */
    public static int shiftRight(int in, int pos){
        if(pos >= Integer.SIZE) return 0;
        if(pos < 0) return in;
        int ones = hammingDist(in, 0, Integer.SIZE);
        return createMask(ones, false) >>> pos - ones + 1;
    }

    public static int shiftLeft(int in){
        return shiftLeft(in, 32);
    }
    
    /**
     * Shifts all 1's in a binary string as far right as possible, eliminating
     * any 0's in between (compacts them).
     * @param in            data input
     * @param pos           Shifts bits up to this position.
     * @return A binary string with 1's shifted to the right up to <code>pos</code>
     *         (as counted from the least-significant bit)
     */
    public static int shiftLeft(int in, int pos){
        if(pos > Integer.SIZE) return 0;
        if(pos < 0) return in;
        int ones = hammingDist(in, 0, Integer.SIZE);

        return createMask(ones, false) >>> pos;
    }

    /**
     * Creates a mask of the specified size and side.
     * @param size T      The number of 1-bits the mask will include.
     * @param rightToLeft If true, the mask will be shifted as far right as possible.
     *                    Else, the mask will be shifted as far left as possible.
     * @return
     */
    public static int createMask(int size, boolean rightToLeft){
        if(rightToLeft){
            return (0x01 << size) - 1;
        } else {
            //Two's complement to the rescue
            return -(0x01 << (Integer.SIZE - size));
        }
    }



    /**
     * Condition: specified position is in between <code>start</code> (incl)
     * and <code>end</code>(excl).
     * @throws IllegalArgumentException if the condition is not true.
     */
    private static void checkBounds(int toCheck, int start, int end) throws IllegalArgumentException{
        if(toCheck < start || toCheck >= end) throw new IllegalArgumentException("Out of bounds");
    }


}
