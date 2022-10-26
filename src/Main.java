
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Constructs Main
 */
public class Main {

    /**
     * A map of all the instructions along with an array of integers to represent their opcode.
     */
    static Map<String, int[]> instructionMap = new HashMap<String, int[]>() {{
        put("add", new int[]{0, 0, 0, 0, 0});
        put("sub", new int[]{0, 0, 0, 0, 1});
        put("sml", new int[]{0, 0, 0, 1, 0});
        put("smr", new int[]{0, 0, 0, 1, 1});
        put("blt", new int[]{0, 0, 1, 0, 0});
        put("bge", new int[]{0, 0, 1, 0, 1});
        put("beq", new int[]{0, 0, 1, 1, 0});
        put("bne", new int[]{0, 0, 1, 1, 1});
        put("lw", new int[]{0, 1, 0, 0, 0});
        put("sw", new int[]{0, 1, 0, 0, 1});
        put("lr", new int[]{0, 1, 0, 1, 0});
        put("sr", new int[]{0, 1, 0, 1, 1});
        put("and", new int[]{0, 1, 1, 0, 0});
        put("or", new int[]{0, 1, 1, 0, 1});
        put("jal", new int[]{0, 1, 1, 1, 0});
        put("jump", new int[]{0, 1, 1, 1, 1});
        put("addi", new int[]{1, 0, 0, 0, 0});
        put("andi", new int[]{1, 0, 0, 0, 1});
        put("ori", new int[]{1, 0, 0, 1, 0});
        put("lui", new int[]{1, 0, 0, 1, 1});
        put("jral", new int[]{1, 0, 1, 0, 0});
        put("jra", new int[]{1, 0, 1, 0, 1});
        put("in", new int[]{1, 0, 1, 1, 0});
        put("out", new int[]{1, 0, 1, 1, 1});
        //etc
    }};


    /**
     * Takes an input file name located inside /inputs, and creates machine code and exports it into /outputs under the same name.
     * @param args
     */

    public static void main(String[] args) {
        // We only want 1 argument, the filename.txt to be opened for the input.
        // filename.txt must be located inside the input directory.
/*
        // If there is more than 1 input, we want to end early.
        if (args.length > 1) {
            System.out.println("Only supports 1 input argument.");
            return;
        }

        // If there is less than 1 input, we want to end early.
        else if (args.length < 1) {
            System.out.println("Requires input argument: filename.txt");
            return;
        }

        // Print out the file name to verify
        String filename = args[0];
 */

        //delete test file later
        String filename = "example_loop.txt";
        System.out.println("Assembling... "+filename);

        // Set up variables
        HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
        int startingAddress = 0x00040000;

        try {
            // Connect the filename to the path
            Path currentPath = Paths.get("inputs/" + filename);
            String filepath = currentPath.toAbsolutePath().toString();

            System.out.println(filepath);

            // Attempts to find file
            File file = new File(filepath);

            // Opens a scanner of the file
            Scanner sc = new Scanner(file);

            // Creates a wrapper that keeps track of the current instruction line.
            IntegerWrapper lineNum = new IntegerWrapper(0);

            while (sc.hasNextLine()) {

                // Grabs current line in input file
                String currentLine = sc.nextLine();
                System.out.println("line "+lineNum.getValue()+": "+currentLine);

                // Splits up the instruction by spaces
                String[] strList = currentLine.split(" ");

                // Let's clean up the strings incase there's any leftover space/indent junk.
                for (int i = 0; i < strList.length; i++) {
                    strList[i].trim();
                }

                // Starting position for searching for instruction and arguments
                // Set here because it will change depending on if a label exists
                int startPos = 0;

                // Checks for labels
                if (strList[0].contains(":")) {
                    String label = strList[0];
                    String labelName = label.substring(0, label.length()-1).toUpperCase();

                    // Puts the address in storage.
                    // This definitely doesn't work properly yet but it's a wip.
                    labelMap.put(labelName, startingAddress + (2*lineNum.getValue()));

                    System.out.println("\tFound label '"+labelName+"' at line #"+lineNum.getValue());
                    startPos++;
                }

                // Creates a TwoByteBuilder with the given instructions
                TwoByteBuilder instruction1 = Main.encodeInstruction(strList, lineNum, startPos);

            }
            sc.close();
        } catch(Exception e){
            // Error :(
            e.printStackTrace();
        }



    }

    /**
     * Creates a TwoByteBuilder with the given instructions, line number, and starting position.
     * @param instList
     * @param lineNum
     * @param startPos
     * @return TwoByteBuilder
     */
    public static TwoByteBuilder encodeInstruction(String[] instList, IntegerWrapper lineNum, int startPos) {
        // Pulls instruction from list
        String instruction = instList[startPos];

        // Checks if instruction exists. If not, ends the script.
        if (!instructionMap.containsKey(instruction)){
            System.out.println("Unknown Instruction!");
        }

        // Creates TwoByteBuilder
        TwoByteBuilder twoByte = new TwoByteBuilder();

        // Gets the bits for the instruction
        int[] instArray = instructionMap.get(instruction);
        for (int i = 0; i < instArray.length; i++) {
            twoByte.setNextBit(instArray[i]);
        }

        // Grabs the argument strings
        String argument1 = instList[startPos + 1];

        twoByte.printBytes();

        // Increases the line counter by 1
        lineNum.incrementValue(1);

        return twoByte;
    }

    /**
     * Turns a 16-bit number into its signed binary representation as an array of 1s and 0s
     * @param given
     * @param lineNum
     * @return int[]
     */
    public static int[] createBitsFrom16BitNum(int given, IntegerWrapper lineNum) {
        if (given > 32767 || given < -32768) {
            System.out.println("Number greater than 16 bit on line: "+lineNum.getValue());
            System.exit(0);
        }
        String binaryString = create16BitSignedStringFromInt(given);
        return new int[5];
    }

    /**
     * Given a 16-bit number, turns it into a String of its 16-bit signed representation.
     * @param given
     * @return String
     */
    public static String create16BitSignedStringFromInt(int given) {
        String testString;
        if (given >= 0) {
            testString = String.format("%16s",Integer.toBinaryString(given)).replaceAll(" ", "0");
        } else {
            testString = Integer.toBinaryString(given);
            testString = testString.substring(16);
        };
        return testString;
    }
}

