
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
//        int startingAddress =

        try {
            // Connect the filename to the path
            Path currentPath = Paths.get("inputs/" + filename);
            String filepath = currentPath.toAbsolutePath().toString();

            System.out.println(filepath);

            // Attempts to find file
            File file=new File(filepath);

            // Opens a scanner of the file
            Scanner sc = new Scanner(file);

            IntegerWrapper lineNum = new IntegerWrapper(0);

            while (sc.hasNextLine()) {

                // Grabs current line
                String currentLine = sc.nextLine();
                System.out.println("line "+lineNum.getValue()+": "+currentLine);

                // Splits up the string by spaces
                String[] strList = currentLine.split(" ");

                // Starting position for searching for instruction and arguments
                // Set here because it will change depending on if a label exists
                int startPos = 0;

                // Checks for labels
                if (strList[0].contains(":")) {
                    String label = strList[0];
                    String labelName = label.substring(0, label.length()-1);
                    labelMap.put(labelName, lineNum.getValue());
                    System.out.println("\tFound label '"+labelName+"' at line #"+lineNum.getValue());
                    startPos++;
                }

                // Creates a TwoByteBuilder with the given instructions
                TwoByteBuilder instruction1 = Main.encodeInstruction(strList, lineNum, startPos);

            }
            sc.close();
        } catch(Exception e){
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

        int[] instArray = instructionMap.get(instruction);

        for (int i = 0; i < instArray.length; i++) {
            twoByte.setNextBit(instArray[i]);
        }

        String argument1 = instList[startPos + 1];

        twoByte.printBytes();

        lineNum.incrementValue(1);

        return twoByte;
    }


}

