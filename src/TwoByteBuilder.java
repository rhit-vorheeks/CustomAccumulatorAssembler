/**
 * Creates a wrapper that contains the two bytes that make up an instruction.
 */
public class TwoByteBuilder {
    int count;
    private byte byteOne;
    private byte byteTwo;

    /**
     * Constructs a TwoByteBuilder, defaulted to 0x00
     */
    public TwoByteBuilder() {
        byteOne = 0;
        byteTwo = 0;
        int count = 0;
    }

    /**
     * Returns the first byte of the TwoByteBuilder
     * @return byte
     */
    public byte getByteOne() {
        return byteOne;
    }

    /**
     * Returns the second byte of the TwoByteBuilder
     * @return byte
     */

    public byte getByteTwo() {
        return byteTwo;
    }

    /**
     * Sets the next bit of the builder to the given bit, must be 0 or 1
     * @param bit
     */
    public void setNextBit(int bit) {
        if (count <= 7) {
            int newCount = 7 - count;
            byteOne = (byte) (byteOne | (bit << newCount));
        } else {
            int newCount1 = count - 8;
            int newCount2 = 7 - newCount1;
            byteTwo = (byte) (byteTwo | (bit << newCount2));
        }
        count++;
    }

    /**
     * Prints out the two bytes currently stored in the builder
     */
    public void printBytes() {
        String s1 = String.format("%8s", Integer.toBinaryString(this.byteOne & 0xFF)).replace(' ', '0');
        String s2 = String.format("%8s", Integer.toBinaryString(this.byteTwo & 0xFF)).replace(' ', '0');
        System.out.println(s1+s2);
    }
}
