
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    static Map<String, int[]> registerMap = new HashMap<String, int[]>() {{
        // Register Ids
        put("x0", new int[]{0, 0, 0, 0});
        put("x1", new int[]{0, 0, 0, 1});
        put("x2", new int[]{0, 0, 1, 0});
        put("x3", new int[]{0, 0, 1, 1});
        put("x4", new int[]{0, 1, 0, 0});
        put("x5", new int[]{0, 1, 0, 1});
        put("x6", new int[]{0, 1, 1, 0});
        put("x7", new int[]{0, 1, 1, 1});
        put("x8", new int[]{1, 0, 0, 0});
        put("x9", new int[]{1, 0, 0, 1});
        put("x10", new int[]{1, 0, 1, 0});
        put("x11", new int[]{1, 0, 1, 1});
        put("x12", new int[]{1, 1, 0, 0});
        put("x13", new int[]{1, 1, 0, 1});
        put("x14", new int[]{1, 1, 1, 0});
        put("x15", new int[]{1, 1, 1, 1});
        // Named Registers
        put("zero", new int[]{0, 0, 0, 0});
        put("mn", new int[]{0, 0, 0, 1});
        put("sa", new int[]{0, 0, 1, 0});
        put("sp", new int[]{0, 0, 1, 1});
        put("t0", new int[]{0, 1, 0, 0});
        put("ra", new int[]{0, 1, 0, 1});
        put("a0", new int[]{0, 1, 1, 0});
        put("a1", new int[]{0, 1, 1, 1});
        put("a2", new int[]{1, 0, 0, 0});
        put("a3", new int[]{1, 0, 0, 1});
        put("a4", new int[]{1, 0, 1, 0});
        put("a5", new int[]{1, 0, 1, 1});
        put("a6", new int[]{1, 1, 0, 0});
        put("a7", new int[]{1, 1, 0, 1});
        put("a8", new int[]{1, 1, 1, 0});
        put("a9", new int[]{1, 1, 1, 1});
    }};

    /**
     * Takes an input file name located inside /inputs, and creates machine code and exports it into /outputs under the same name.
     * @param args
     */

    public static void main(String[] args) {
        // We only want 1 argument, the filename.txt to be opened for the input.
        // filename.txt must be located inside the input directory.

        args = new String[2];

        Scanner reader = new Scanner(System.in);  // Reading from System.in

        System.out.print("Enter file name: ");
        args[0] = reader.nextLine();


        System.out.print("Enter output type[hex, bytes]: ");
        args[1] = reader.nextLine();

        if (!args[1].equals("bytes") && !args[1].equals("hex")) {
            System.out.println("Unknown output type!");
            return;
        }

        // Save off filename
        String filename = args[0];

        System.out.println("Assembling... "+filename);

        // Set up variables
        HashMap<String, Integer> labelMap = new HashMap<String, Integer>();
        ArrayList<InstructionLine> allInstruction = new ArrayList<InstructionLine>();

        ArrayList<InstructionLine> toBeAddressed = new ArrayList<InstructionLine>();

        int startingAddress = 0x0000;

        try {
            // Connect the filename to the path
            Path currentPath = Paths.get("../inputs/" + filename);
            String filepath = currentPath.toAbsolutePath().toString();

//            System.out.println(filepath);

            // Attempts to find file
            File file = new File(filepath);

            // Opens a scanner of the file
            Scanner sc = new Scanner(file);

            // Creates a wrapper that keeps track of the current instruction line.
            IntegerWrapper lineNum = new IntegerWrapper(0);

            while (sc.hasNextLine()) {

                // Grabs current line in input file
                String currentLine = sc.nextLine();
                int startPos = 0;

                // Splits up the instruction by spaces
                String[] strList = currentLine.split("\\s+");

                // Let's clean up the strings incase there's any leftover space/indent junk.
                for (int i = 0; i < strList.length; i++) {
                    strList[i].trim();
                }


                //  Check if blank line
                if (strList.length <= 1) {
                    continue;
                }

                // Checks for empty spaces before commands.
                while (strList[startPos].length() == 0) {
                    startPos++;
                }

                // Checks for comment line.
                if (strList[startPos].charAt(0) == '/') {
                    continue;
                }
                // Starting position for searching for instruction and arguments
                // Set here because it will change depending on if a label exists
                String labelName = "";

                // Checks for labels
                if (strList[0].contains(":")) {
                    String label = strList[0];
                    labelName = label.substring(0, label.length()-1).toUpperCase();

                    // Puts the address in storage.
                    // This definitely doesn't work properly yet but it's a wip.
                    labelMap.put(labelName, startingAddress + (2*lineNum.getValue()));

//                    System.out.println("\tFound label '"+labelName+"' at line #"+lineNum.getValue());

                    // Increases spot by 1
                    startPos++;
                }

                // Creates a InstructionLine with the given instructions
                InstructionLine instruction1 = Main.encodeInstruction(strList, lineNum, startPos, toBeAddressed);

                // Adds current instruction to master instruction ArrayList
                allInstruction.add(instruction1);

            }

            // Writes addresses now that we know the location of the labels.
            for (InstructionLine inst : toBeAddressed) {
                String label = inst.getLabelName();
                inst.writeAddress(labelMap.get(label));
            }

            if (args[1].equals("bytes")) {
                // Sets up output path file, and removes the file extension.
                Path outputPath = Paths.get("../outputs/" + filename.split("\\.")[0]);
                String outFilepath = outputPath.toAbsolutePath().toString();

                // Creates the file.
                File outputFile = new File(outFilepath);

                try (FileOutputStream fos = new FileOutputStream(outputFile))
                {
                    for (InstructionLine inst : allInstruction) {
                        // Print for reading.
                        inst.print();

                        TwoByteBuilder builder = inst.getBuilder();
                        // Writes the bytes to the file.
                        fos.write(builder.getByteOne());
                        fos.write(builder.getByteTwo());
                    }


                    System.out.println("Successfully written data to " + outputPath.toString());
                } catch (IOException e) {
                    // Error :(
                    e.printStackTrace();
                }
            } else {
                Path outputPath = Paths.get("../outputs/" + filename);
                String outFilepath = outputPath.toAbsolutePath().toString();

                // Creates the file.
//                File outputFile = new File(outFilepath);

                try (PrintStream out = new PrintStream(new FileOutputStream(outFilepath))) {
                    for (InstructionLine inst : allInstruction) {
                        // Print for reading.
                        inst.print();

                        TwoByteBuilder builder = inst.getBuilder();
                        // Writes the bytes to the file.
                        out.println(builder.getHex());
                    }
                    System.out.println("Successfully written data to " + outputPath.toString());
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }

            sc.close();
        } catch(Exception e){
            // Error :(
            e.printStackTrace();
        }



    }

    /**
     * Creates a InstructionLine with the given instructions, line number, and starting position.
     * @param instList
     * @param lineNum
     * @param startPos
     * @param toBeLabeled
     * @return InstructionLine
     */
    public static InstructionLine encodeInstruction(String[] instList, IntegerWrapper lineNum, int startPos, ArrayList<InstructionLine> toBeLabeled) {
        // Pulls instruction from list
        String instruction = instList[startPos].toLowerCase();

        // Checks if instruction exists. If not, ends the script.
        if (!instructionMap.containsKey(instruction)){
            System.out.println("Unknown Instruction!");
        }

        // Creates TwoByteBuilder
        TwoByteBuilder twoByte = new TwoByteBuilder();

        // Creates InstructionLine
        InstructionLine instLine = new InstructionLine(twoByte);
        instLine.setInstructionName(instruction);

        // Gets the bits for the instruction
        int[] instArray = instructionMap.get(instruction);
        for (int i = 0; i < instArray.length; i++) {
            twoByte.setNextBit(instArray[i]);
        }

        // Grabs the argument strings
        String argument1 = instList[startPos + 1];
        instLine.setArgument(argument1);

        if (instruction.equals("sr") || instruction.equals("lr") || instruction.equals("in") ||instruction.equals("out")) {
            // Handle R type instructions here
            // Gets the bits for the register address
            int[] regArray = registerMap.get(argument1);
            for (int i = 0; i < regArray.length; i++) {
                twoByte.setNextBit(regArray[i]);
            }
        } else if (instruction.equals("lui")) {
            // Put integer in instruction
            // First convert the argument to an integer
            int value = Integer.parseInt(argument1);

            // Gets the array of bits from the integer
            int[] arrayOfBits = createBitArrayFrom16BitNum(value, lineNum);

            // Assigns the bits to our builder
            for (int i = 0; i < 5; i++) {
                twoByte.setNextBit(arrayOfBits[i]);
            }
        } else {
            // Anything else must be A type

            // First check if we're loading a label or an integer
            if (instruction.charAt(0) == 'b' || instruction.equals("jal") || instruction.equals("jump")) {
                String labelName = argument1.toUpperCase();
                instLine.setLabelName(labelName);
                // Mark instruction to be adjusted later.
                toBeLabeled.add(instLine);
            } else {
                // Put integer in instruction
                // First convert the argument to an integer
                int value = Integer.parseInt(argument1);

                // Gets the array of bits from the integer
                int[] arrayOfBits = createBitArrayFrom16BitNum(value, lineNum);

                // Assigns the bits to our builder
                for (int i = 5; i < arrayOfBits.length; i++) {
                    twoByte.setNextBit(arrayOfBits[i]);
                }

            }
        }

        // Mark the line # in the Instruction Line
        instLine.setLineNum(lineNum.getValue());
        // Increases the line counter by 1
        lineNum.incrementValue(1);

        return instLine;
    }

    /**
     * Turns a 16-bit number into its signed binary representation as an array of 1s and 0s
     * @param given
     * @param lineNum
     * @return int[]
     */
    public static int[] createBitArrayFrom16BitNum(int given, IntegerWrapper lineNum) {
        if (given > 32767 || given < -32768) {
            System.out.println("Number greater than 16 bit on line: "+lineNum.getValue());
            System.exit(0);
        }
        String binaryString = create16BitSignedStringFromInt(given);
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

