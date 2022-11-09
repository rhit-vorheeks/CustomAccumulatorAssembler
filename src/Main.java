
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

        // Keeps track of how many mult instructions are used so we dont have repeating labels.
        int currentMultInst = 0;

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
                    labelMap.put(labelName, startingAddress + (2*lineNum.getValue()));

//                    System.out.println("\tFound label '"+labelName+"' at line #"+lineNum.getValue());

                    // Increases spot by 1
                    startPos++;
                }


                switch (strList[startPos]) {
                    case "li":
                        // First we do andi 0 to set mn to 0.
                        // Creates a InstructionLine with the given instructions
                        // Adds current instruction to master instruction ArrayList
                        String[] andi = new String[2];
                        andi[0] = "andi";
                        andi[1] = "0";
                        InstructionLine instruction1 = Main.encodeInstruction(andi, lineNum, 0, toBeAddressed);
                        allInstruction.add(instruction1);


                        String[] ori = new String[2];
                        ori[0] = "ori";
                        ori[1] = strList[startPos+1];
                        InstructionLine instruction2 = Main.encodeInstruction(ori, lineNum, 0, toBeAddressed);
                        allInstruction.add(instruction2);
                        break;
                    case "multi":
                        String inputArg = strList[startPos+1];
                        String loopBranch = "LOOPONLYUSEFORPSEUDOMULT"+currentMultInst;
                        String endBranch = "RETURNTOENDOFPSEUDO"+currentMultInst;

                        String[] multiLn1 = new String[2];
                        multiLn1[0] = "sr";
                        multiLn1[1] = "t0";
                        InstructionLine multiInstruction1 = Main.encodeInstruction(multiLn1, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction1);


                        String[] multiLn2 = new String[2];
                        multiLn2[0] = "lr";
                        multiLn2[1] = "sp";
                        InstructionLine multiInstruction2 = Main.encodeInstruction(multiLn2, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction2);

                        String[] multiLn3 = new String[2];
                        multiLn3[0] = "addi";
                        multiLn3[1] = "-4";
                        InstructionLine multiInstruction3 = Main.encodeInstruction(multiLn3, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction3);

                        String[] multiLn4 = new String[2];
                        multiLn4[0] = "sr";
                        multiLn4[1] = "sp";
                        InstructionLine multiInstruction4 = Main.encodeInstruction(multiLn4, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction4);

                        String[] multiLn5 = new String[2];
                        multiLn5[0] = "sr";
                        multiLn5[1] = "sa";
                        InstructionLine multiInstruction5 = Main.encodeInstruction(multiLn5, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction5);

                        String[] multiLn6 = new String[2];
                        multiLn6[0] = "lr";
                        multiLn6[1] = "t0";
                        InstructionLine multiInstruction6 = Main.encodeInstruction(multiLn6, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction6);

                        String[] multiLn7 = new String[2];
                        multiLn7[0] = "sw";
                        multiLn7[1] = "0";
                        InstructionLine multiInstruction7 = Main.encodeInstruction(multiLn7, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction7);

                        String[] multiLn8 = new String[2];
                        multiLn8[0] = "andi";
                        multiLn8[1] = "0";
                        InstructionLine multiInstruction8 = Main.encodeInstruction(multiLn8, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction8);

                        String[] multiLn9 = new String[2];
                        multiLn9[0] = "ori";
                        multiLn9[1] = inputArg;
                        InstructionLine multiInstruction9 = Main.encodeInstruction(multiLn9, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction9);

                        String[] multiLn10 = new String[2];
                        multiLn10[0] = "sw";
                        multiLn10[1] = "2";
                        InstructionLine multiInstruction10 = Main.encodeInstruction(multiLn10, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction10);

                        //adds label
                        labelMap.put(loopBranch, startingAddress + (2*lineNum.getValue()));
                        String[] multiLn11 = new String[2];
                        multiLn11[0] = "lw";
                        multiLn11[1] = "2";
                        InstructionLine multiInstruction11 = Main.encodeInstruction(multiLn11, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction11);

                        String[] multiLn12 = new String[2];
                        multiLn12[0] = "addi";
                        multiLn12[1] = "-1";
                        InstructionLine multiInstruction12 = Main.encodeInstruction(multiLn12, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction12);

                        String[] multiLn13 = new String[2];
                        multiLn13[0] = "sw";
                        multiLn13[1] = "2";
                        InstructionLine multiInstruction13 = Main.encodeInstruction(multiLn13, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction13);

                        String[] multiLn14 = new String[2];
                        multiLn14[0] = "beq";
                        multiLn14[1] = endBranch;
                        InstructionLine multiInstruction14 = Main.encodeInstruction(multiLn14, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction14);

                        String[] multiLn15 = new String[2];
                        multiLn15[0] = "lr";
                        multiLn15[1] = "t0";
                        InstructionLine multiInstruction15 = Main.encodeInstruction(multiLn15, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction15);

                        String[] multiLn16 = new String[2];
                        multiLn16[0] = "add";
                        multiLn16[1] = "0";
                        InstructionLine multiInstruction16 = Main.encodeInstruction(multiLn16, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction16);

                        String[] multiLn17 = new String[2];
                        multiLn17[0] = "sr";
                        multiLn17[1] = "t0";
                        InstructionLine multiInstruction17 = Main.encodeInstruction(multiLn17, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction17);

                        String[] multiLn18 = new String[2];
                        multiLn18[0] = "jump";
                        multiLn18[1] = loopBranch;
                        InstructionLine multiInstruction18 = Main.encodeInstruction(multiLn18, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction18);

                        //adds label
                        labelMap.put(endBranch, startingAddress + (2*lineNum.getValue()));
                        String[] multiLn19 = new String[2];
                        multiLn19[0] = "lr";
                        multiLn19[1] = "sp";
                        InstructionLine multiInstruction19 = Main.encodeInstruction(multiLn19, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction19);

                        String[] multiLn20 = new String[2];
                        multiLn20[0] = "addi";
                        multiLn20[1] = "4";
                        InstructionLine multiInstruction20 = Main.encodeInstruction(multiLn20, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction20);

                        String[] multiLn21 = new String[2];
                        multiLn21[0] = "sr";
                        multiLn21[1] = "sp";
                        InstructionLine multiInstruction21 = Main.encodeInstruction(multiLn21, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction21);

                        String[] multiLn22 = new String[2];
                        multiLn22[0] = "sr";
                        multiLn22[1] = "sa";
                        InstructionLine multiInstruction22 = Main.encodeInstruction(multiLn22, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction22);


                        String[] multiLn23 = new String[2];
                        multiLn23[0] = "lr";
                        multiLn23[1] = "t0";
                        InstructionLine multiInstruction23 = Main.encodeInstruction(multiLn23, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction23);

                        currentMultInst++;
                        break;
                    case "mult":
                        String inputArgMem = strList[startPos+1];
                        int memSpot = Integer.parseInt(inputArgMem) + 4;
                        String getFrom = "" + memSpot;
                        String loopBranchMem = "LOOPONLYUSEFORPSEUDOMULT"+currentMultInst;
                        String endBranchMem = "RETURNTOENDOFPSEUDO"+currentMultInst;

                        String[] multiLn1Mem = new String[2];
                        multiLn1Mem[0] = "sr";
                        multiLn1Mem[1] = "t0";
                        InstructionLine multiInstruction1Mem = Main.encodeInstruction(multiLn1Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction1Mem);


                        String[] multiLn2Mem = new String[2];
                        multiLn2Mem[0] = "lr";
                        multiLn2Mem[1] = "sp";
                        InstructionLine multiInstruction2Mem = Main.encodeInstruction(multiLn2Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction2Mem);

                        String[] multiLn3Mem = new String[2];
                        multiLn3Mem[0] = "addi";
                        multiLn3Mem[1] = "-4";
                        InstructionLine multiInstruction3Mem = Main.encodeInstruction(multiLn3Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction3Mem);

                        String[] multiLn4Mem = new String[2];
                        multiLn4Mem[0] = "sr";
                        multiLn4Mem[1] = "sp";
                        InstructionLine multiInstruction4Mem = Main.encodeInstruction(multiLn4Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction4Mem);

                        String[] multiLn5Mem = new String[2];
                        multiLn5Mem[0] = "sr";
                        multiLn5Mem[1] = "sa";
                        InstructionLine multiInstruction5Mem = Main.encodeInstruction(multiLn5Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction5Mem);

                        String[] multiLn6Mem = new String[2];
                        multiLn6Mem[0] = "lr";
                        multiLn6Mem[1] = "t0";
                        InstructionLine multiInstruction6Mem = Main.encodeInstruction(multiLn6Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction6Mem);

                        String[] multiLn7Mem = new String[2];
                        multiLn7Mem[0] = "sw";
                        multiLn7Mem[1] = "0";
                        InstructionLine multiInstruction7Mem = Main.encodeInstruction(multiLn7Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction7Mem);

                        String[] multiLn8Mem = new String[2];
                        multiLn8Mem[0] = "andi";
                        multiLn8Mem[1] = "0";
                        InstructionLine multiInstruction8Mem = Main.encodeInstruction(multiLn8Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction8Mem);

                        String[] multiLn9Mem = new String[2];
                        multiLn9Mem[0] = "lw";
                        multiLn9Mem[1] = getFrom;
                        InstructionLine multiInstruction9Mem = Main.encodeInstruction(multiLn9Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction9Mem);

                        String[] multiLn10Mem = new String[2];
                        multiLn10Mem[0] = "sw";
                        multiLn10Mem[1] = "2";
                        InstructionLine multiInstruction10Mem = Main.encodeInstruction(multiLn10Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction10Mem);

                        //adds label
                        labelMap.put(loopBranchMem, startingAddress + (2*lineNum.getValue()));
                        String[] multiLn11Mem = new String[2];
                        multiLn11Mem[0] = "lw";
                        multiLn11Mem[1] = "2";
                        InstructionLine multiInstruction11Mem = Main.encodeInstruction(multiLn11Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction11Mem);

                        String[] multiLn12Mem = new String[2];
                        multiLn12Mem[0] = "addi";
                        multiLn12Mem[1] = "-1";
                        InstructionLine multiInstruction12Mem = Main.encodeInstruction(multiLn12Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction12Mem);

                        String[] multiLn13Mem = new String[2];
                        multiLn13Mem[0] = "sw";
                        multiLn13Mem[1] = "2";
                        InstructionLine multiInstruction13Mem = Main.encodeInstruction(multiLn13Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction13Mem);

                        String[] multiLn14Mem = new String[2];
                        multiLn14Mem[0] = "beq";
                        multiLn14Mem[1] = endBranchMem;
                        InstructionLine multiInstruction14Mem = Main.encodeInstruction(multiLn14Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction14Mem);

                        String[] multiLn15Mem = new String[2];
                        multiLn15Mem[0] = "lr";
                        multiLn15Mem[1] = "t0";
                        InstructionLine multiInstruction15Mem = Main.encodeInstruction(multiLn15Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction15Mem);

                        String[] multiLn16Mem = new String[2];
                        multiLn16Mem[0] = "add";
                        multiLn16Mem[1] = "0";
                        InstructionLine multiInstruction16Mem = Main.encodeInstruction(multiLn16Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction16Mem);

                        String[] multiLn17Mem = new String[2];
                        multiLn17Mem[0] = "sr";
                        multiLn17Mem[1] = "t0";
                        InstructionLine multiInstruction17Mem = Main.encodeInstruction(multiLn17Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction17Mem);

                        String[] multiLn18Mem = new String[2];
                        multiLn18Mem[0] = "jump";
                        multiLn18Mem[1] = loopBranchMem;
                        InstructionLine multiInstruction18Mem = Main.encodeInstruction(multiLn18Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction18Mem);

                        //adds label
                        labelMap.put(endBranchMem, startingAddress + (2*lineNum.getValue()));
                        String[] multiLn19Mem = new String[2];
                        multiLn19Mem[0] = "lr";
                        multiLn19Mem[1] = "sp";
                        InstructionLine multiInstruction19Mem = Main.encodeInstruction(multiLn19Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction19Mem);

                        String[] multiLn20Mem = new String[2];
                        multiLn20Mem[0] = "addi";
                        multiLn20Mem[1] = "4";
                        InstructionLine multiInstruction20Mem = Main.encodeInstruction(multiLn20Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction20Mem);

                        String[] multiLn21Mem = new String[2];
                        multiLn21Mem[0] = "sr";
                        multiLn21Mem[1] = "sp";
                        InstructionLine multiInstruction21Mem = Main.encodeInstruction(multiLn21Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction21Mem);

                        String[] multiLn22Mem = new String[2];
                        multiLn22Mem[0] = "sr";
                        multiLn22Mem[1] = "sa";
                        InstructionLine multiInstruction22Mem = Main.encodeInstruction(multiLn22Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction22Mem);


                        String[] multiLn23Mem = new String[2];
                        multiLn23Mem[0] = "lr";
                        multiLn23Mem[1] = "t0";
                        InstructionLine multiInstruction23Mem = Main.encodeInstruction(multiLn23Mem, lineNum, 0, toBeAddressed);
                        allInstruction.add(multiInstruction23Mem);

                        currentMultInst++;
                        break;
                    default:
                        // Creates a InstructionLine with the given instructions
                        // Adds current instruction to master instruction ArrayList
                        allInstruction.add(Main.encodeInstruction(strList, lineNum, startPos, toBeAddressed));
                        break;
                }

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
            System.out.println("Unknown Instruction! What is... \"" + instruction + "\" on line #" + lineNum.getValue());
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

