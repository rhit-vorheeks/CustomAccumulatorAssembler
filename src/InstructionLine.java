/**
 * Instruction Line class that keeps track of a line of code and its data.
 */
public class InstructionLine {
    private TwoByteBuilder builder;
    private String labelName;
    private String instructionName;
    private String argument;
    private int lineNum;

    /**
     * Constructs an InstructionLine when given a TwoByteBuilder
     * @param bld
     */
    public InstructionLine(TwoByteBuilder bld) {
        this.builder = bld;
        this.labelName = "";
    }

    /**
     * Writes an address to the next bits in the Builder.
     * @param address
     */
    public void writeAddress(int address) {
        int[] addressArray = this.createBitArrayFrom16BitNum(address);
        for (int i = 5; i < addressArray.length; i++) {
            this.builder.setNextBit(addressArray[i]);
        }
    }

    /**
     * Turns a 16-bit number into its signed binary representation as an array of 1s and 0s
     * @param given
     * @return int[]
     */
    public int[] createBitArrayFrom16BitNum(int given) {
        // This is duplicated from main because I want to give different error messages and I don't feel like refactoring :P
        if (given > 32767 || given < -32768) {
            System.out.println("Number greater than 16 bit on label: "+this.labelName);
            System.exit(0);
        }
        String binaryString = Main.create16BitSignedStringFromInt(given);
        int[] binaryArray = new int[16];

        for (int i = 0; i < binaryArray.length; i++) {
            char ch = binaryString.charAt(i);
            if (ch == '0') {
                binaryArray[i] = 0;
            } else {
                binaryArray[i] = 1;
            }
        }

        return binaryArray;
    }

    /**
     * Returns the label of the instruction.
     * @return
     */
    public String getLabelName() {
        return this.labelName;
    }

    /**
     * Sets the label of the instruction.
     * @param str
     */
    public void setLabelName(String str) {
        this.labelName = str;
    }

    /**
     * Sets instruction name of the instruction
     * @param name
     */
    public void setInstructionName(String name) {
        this.instructionName = name;
    }

    /**
     * Sets the line number of the instruction, for reference when printing.
     * @param num
     */
    public void setLineNum(int num) {
        this.lineNum = num;
    }

    /**
     * Sets the argument name of the instruction.
     * @param arg
     */
    public void setArgument(String arg) {
        this.argument = arg;
    }

    /**
     * Prints out the data in the instruction.
     */
    public void print() {
        System.out.println("Line #" + this.lineNum+" | " + this.instructionName + " " + this.argument + ": \t" + this.builder.toString());
    }

    /**
     * Returns the TwoByteBuilder of the instruction.
     * @return TwoByteBuilder
     */
    public TwoByteBuilder getBuilder() {
        return this.builder;
    }
}
