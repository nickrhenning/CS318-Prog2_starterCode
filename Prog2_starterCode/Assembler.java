/**
* Assembler for the CS318 simple computer simulation
*/
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;
import java.util.*;


public class Assembler {

    /**
    * Assembles the code file. When this method is finished, the dataFile and
    * codeFile contain the assembled data segment and code segment, respectively.
    *
    * @param inFile The pathname to the assembly language file to be assembled.
    * @param dataFile The pathname where the data segment file should be written.
    * @param codeFile The pathname where the code segment file should be written.
    */
    public static void assemble(String inFile, String dataFile, String codeFile)
                                    throws FileNotFoundException, IOException {

        // DO NOT MAKE ANY CHANGES TO THIS METHOD

        ArrayList<LabelOffset> labels = pass1(inFile, dataFile, codeFile);
        pass2(inFile, dataFile, codeFile, labels);
    }

    /**
    * First pass of the assembler. Writes the number of bytes in the data segment
    * and code segment to their respective output files. Returns a list of
    * code segment labels and thier relative offsets.
    *
    * @param inFile The pathname of the file containing assembly language code.
    * @param dataFile The pathname for the data segment binary file.
    * @param codeFile The pathname for the code segment binary file.
    * @return List of the code segment labels and relative offsets.
    * @exception RuntimeException if the assembly code file does not have the
    * correct format, or another error while processing the assembly code file.
    */
    private static ArrayList<LabelOffset> pass1(String inFile, String dataFile, String codeFile)
                                    throws FileNotFoundException {
		
		/*
		 * Retrieve the size of the data file, and verify the result
		 */
		Scanner in = new Scanner(new File(inFile));
		String value = in.next();
		in.nextLine();

		int count = 2;
		int wordCount = 0;
		
		while(in.hasNextLine()){
			String line = in.nextLine();
			String numbers[]=line.split("[\\s,]+"); //splits for commas and spaces
			for(int i=1;i<numbers.length;i++) { //only counting indexes that start with a number or - sign
				
				if(numbers[i].charAt(0)=='-' || numbers[i].charAt(0)=='0' || numbers[i].charAt(0)=='1' || 
						numbers[i].charAt(0)=='2' ||numbers[i].charAt(0)=='3' ||numbers[i].charAt(0)=='4' 
						||numbers[i].charAt(0)=='5' ||numbers[i].charAt(0)=='6' ||numbers[i].charAt(0)=='7' 
						||numbers[i].charAt(0)=='8' ||numbers[i].charAt(0)=='9') {
					wordCount++;
				}
			}
			count++;
		}
		
		wordCount=wordCount*4;
		System.out.println("Size of the data file is " + Integer.toString(wordCount));  // Verify result
		in.close();
		
		/*
		 * Retrieve size of the code file and verify result
		 */
		in = new Scanner(new File(inFile));
		value = in.next();
		in.nextLine();
		
		int regCount = 0;
		//int matchNum = 0;
		// Registers array tracks the registers whose bytes have already been accounted for 
		// ArrayList<String> regsArr = new ArrayList<String>();
		// For each line, match on register values and increment regsCount for each UNIQDUE register
		while(in.hasNextLine()) {
			String line = in.nextLine();
			String numbers[]=line.split("[\\s,]+"); //splits for commas and spaces
			
			try {
				System.out.println("0 is " + numbers[1]);  // Verify result
				ArrayList<String> opArr = new ArrayList<>(Arrays.asList("ADD", "SUB", "AND", "ORR", "LDR", "STR", "CBZ", "B"));
				if (opArr.contains(numbers[1])) {
					regCount++;
				}
			} catch (Exception e) {
				continue;
			}			
			
		}
		
		// Multiply register count by 4 to get number of bytes for all unique registers
		int codeSize = regCount * 4;
		codeSize = codeSize + 4;
		System.out.println("Size of the code file is " + Integer.toString(codeSize));  // Verify result
		in.close();
		
		/*
		 * Determine code segment labels and relative offsets
		 */
		in = new Scanner(new File(inFile));
		value = in.next();
		in.nextLine();
		
		// ArrayList for labels and offsets
		Map<String,Integer> labelsOne = new HashMap<String,Integer>();
		
		int offset = Integer.MAX_VALUE;  // denotes byte offset from beginning of code		

		while(in.hasNextLine()) {
			String line = in.nextLine();
			String numbers[]=line.split("[\\s,]+"); //splits for commas and spaces
			String op = numbers[0];
			
			// System.out.println("op is:"+op);
			if (op.equals("main:")) {
				offset = -4;
				//System.out.println("offset init to -4");
			} 
			
			// Initialize values of labels map with label and current line offset
			for (int i = 0; i < numbers.length; i++) {
				//System.out.println(numbers[i]);
				// Get current position of instruction
				if (numbers[i].equals("if")) {
					labelsOne.put("if", offset);
					//System.out.println("if added with offset"+Integer.toString(offset));
				} else if (numbers[i].equals("afterif")) {
					labelsOne.put("afterif", offset);
					//System.out.println("afterif added with offset"+Integer.toString(offset));
				}
				// Overwrite labelsOne with instruction - currentposition
				if (numbers[i].equals("if:")) {
					int immediate = offset - labelsOne.get("if");
					labelsOne.put("if", immediate);
					//System.out.println("if added with immediate"+Integer.toString(immediate));
				} else if (numbers[i].equals("afterif:")) {
					int immediate = offset - labelsOne.get("afterif");
					labelsOne.put("afterif", immediate);
					//System.out.println("afterif added with immediate"+Integer.toString(immediate));
				}
			}
			
			if (!numbers[0].equals("afterif:") && !numbers[0].equals("if:")) {
				offset += 4;				
			}

		}
		
		
		
		// Assign values of code segment labels and offsets into a new ArrayList<LabelOffset>() object
		ArrayList<LabelOffset> result = new ArrayList<LabelOffset>();

		// Iterate through labels and assign values to result
		for (int i = 0; i < labelsOne.size(); i++) {
			try {
				LabelOffset temp = new LabelOffset();
				temp.label = "if";
				temp.offset = labelsOne.get("if");
				result.add(temp);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			try {
				LabelOffset temp = new LabelOffset();
				temp.label = "afterif";
				temp.offset = labelsOne.get("afterif");
				result.add(temp);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			// System.out.println(labelsOne.get("if"));
			// System.out.println();
		}
		
		// Verify correct values in the LabelOffset array
		for (int i = 0; i < result.size(); i++) {
			//System.out.println("Index: " + Integer.toString(i) + "Value: " + result.get(i).get(result[));
			System.out.println(result.get(i).label + ": " + result.get(i).offset);
		}
		in.close();
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(codeFile));
			out.write(Integer.toString(codeSize));
			out.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
        // placeholder return. Student should replace with correct return.
        return result;
    }

        /**
        * Second pass of the assembler. Writes the binary data and code files.
        * @param inFile The pathname of the file containing assembly language code.
        * @param dataFile The pathname for the data segment binary file.
        * @param codeFile The pathname for the code segment binary file.
        * @param labels List of the code segment labels and relative offsets.
        * @exception RuntimeException if there is an error when processing the assembly
        * code file.
        */



    public static void pass2(String inFile, String dataFile, String codeFile,
                    ArrayList<LabelOffset> labels) throws FileNotFoundException, IOException {

        // Dictionary<String, String> regexDict = new Hashtable<String, String>();
        // regexDict.put("ADD", )
        
		/*
		 * Assemble instructions
		 */
		Scanner in = new Scanner(new File(inFile));
		String value = in.next();
		in.nextLine();
		
		// Convert instructions into a string of 1's and 0's
		String content = "";
		while(in.hasNextLine()){
			String line = in.nextLine();
			String pieces[]=line.split("[\\s,]+"); //splits for commas and spaces
			
			ArrayList<String> opCodes = new ArrayList<>(Arrays.asList("ADD", "SUB", "AND", "ORR"));
			ArrayList<String> otherCodes = new ArrayList<>(Arrays.asList("LDR", "STR", "CBZ", "B"));
			for (int i=0; i < opCodes.size(); i++) {
				try {
					if (pieces[1].equals(opCodes.get(i))) {
//						System.out.println("Line contains: " + opCodes.get(i));
						line = logicLineToBinary(pieces);
						content += line;
						System.out.println("Binary line added: " + line);
					} else if (pieces[1].equals(otherCodes.get(i))) {
						System.out.println("Line contains: " + otherCodes.get(i));
						line = otherLineToBinary(pieces, labels);
						content += line;
						System.out.println("Binary line added: " + line);
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		
		// Add the last CBZ line
		content += "00000000000000000000000000101011";  
		
		// Write the converted booleans to a text file 
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(codeFile));
			out.write(Integer.toString(content.length()/8));
			out.newLine();
			for (int i = 0; i < content.length()-7; i++) {
				System.out.println("i is " + Integer.toString(i));
				if ((i%8) == 0) {
						String bite = content.substring(i,i+8);
						//System.out.println(bite);
						out.write(stringToBoolList(bite));
						out.newLine();
				}
			}
			// out.write("test");
			out.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
    }
    
    public static String stringToBoolList(String input) {
    	String result = "";
//    	System.out.println("input is: " + input);
//    	System.out.println(input.substring(0,1));
    	for (int i = 0; i < input.length(); i++) {
//        	System.out.println(input.substring(i,i+1));
    		if (input.substring(i,i+1).equals("0")) {
    			result = result + "false";
    		} else if (input.substring(i,i+1).equals("1")) {
    			result = result + "true";
    		} 
    		if (i != (input.length() - 1)) { result = result + " ";}
    	}
//    	System.out.println("result is: " + result);
    	return result;
    }
    
    /**
     * Method takes a line with an arithmetic or logical operator and 
     * returns the assembled binary format
     * @param input
     * @return
     */
    public static String logicLineToBinary(String input[]) {
    	String result = "";
    	
    	String opcode = "";
    	String src2 = "";
    	String src1 = "";
    	String dest = "";
//		System.out.println("Input is:");
//		for (int i = 0; i < input.length; i++) {
//			System.out.println(Integer.toString(i) + ":" +input[i]);
//		}

		// Append binary for the logical operators
		if (input[1].equals("ADD")) {
			opcode = "00011010001";
		} else if (input[1].equals("SUB")) {
			opcode = "00011010011";
		} else if (input[1].equals("AND")) {
			opcode = "00001010001";
		} else if (input[1].equals("ORR")) {
			opcode = "00001010101";
		} 
		//Append binary 
		
		dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5));
//		System.out.println("dest is: " + dest);		
		src1 = boolToBin(uDecToBin(Long.parseLong(input[3].substring(1)), 5));
		//System.out.println("src1 is: "+src1);
		src2 = boolToBin(uDecToBin(Long.parseLong(input[4].substring(1)), 5));	
		//System.out.println("src2 is: "+src2);
		result = dest + src1 + "000000" + src2 + opcode; 
		//System.out.println("Returned: " + result);
    	return result;
    }
    
    
    public static String otherLineToBinary(String[] input, ArrayList<LabelOffset> labels) {
    	String result = "";
    	String opcode = "", immediate = "", base = "", dest = "";
    	
//    	System.out.println("LABEL: "+labels.get(1).offset);
//    	System.exit(0);
    	
		for (int i = 0; i < input.length; i++) {
			System.out.println(Integer.toString(i) + ":" +input[i]);
		}
    	
		if (input[1].equals("LDR")) {
			opcode = "01000011111";
			dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5));
			base = boolToBin(uDecToBin(Long.parseLong(input[3].substring(2)), 5));
			immediate = boolToBin(uDecToBin(Long.parseLong(input[4].substring(1,2)), 9));
		} else if (input[1].equals("STR")) {
			opcode = "00000011111";
			dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5));
			base = boolToBin(uDecToBin(Long.parseLong(input[3].substring(2)), 5));
			immediate = boolToBin(uDecToBin(Long.parseLong(input[4].substring(1,2)), 9));
		} else if (input[1].equals("CBZ")) {
			opcode = "00101101";
			immediate = boolToBin(uDecToBin(Long.parseLong(Integer.toString(labels.get(1).offset)), 19));
		} else if (input[1].equals("B")) {
			opcode = "101000";
			immediate = boolToBin(uDecToBin(Long.parseLong(Integer.toString(labels.get(2).offset)), 26));
		} 
    	
		System.out.println("op code: " + opcode);
		System.out.println("immediate: " + immediate);
		System.out.println("base: " + base);
		System.out.println("dest: " + dest);
		
		result = dest + base + "00" + immediate + opcode;
		
    	return result;
    }
    
    public static boolean[] uDecToBin(long d, int bits) {
        // Throw exception if the number can't fit into a given number of bits
        if (Math.abs(d) > Math.abs(Math.pow(2,bits))) {
          throw new IllegalArgumentException("ERROR: long cannot fit into the number of bits.");
        }

        // Intermediate array of ones and ZEROS
        boolean[] arr = new boolean[bits];
        int num = (int)d;
        int binary[] = new int[40];
        int index = 0;
        while(num > 0){
          binary[index++] = num%2;
          num = num/2;
        }

        // Transfer array of ones and zeroes into trues and falsess
        for(int i= bits-1; i >= 0; i--) {
          if (binary[i]==1){
            arr[bits-i-1]=true;
          } else {
            arr[bits-i-1]=false;
          }
        }
        return arr;
      }
    
    public static String boolToBin(boolean reg[]) {
		String src = "";
    	for (int i = reg.length-1; i >= 0; i--) {
			if (reg[i]==true) {
				src = src + "1";
			} else if (reg[i]==false) {
				src = src + "0";				
			}
		}
    	return src;
    }
    
    public static void main(String[] args) {
    	Assembler object = new Assembler();
    	
    	String progOneFile = "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg3.s";
    	
    	try {
    		ArrayList<LabelOffset> placeHolder1 = object.pass1("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg1.s",
					 "", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output1.txt");
				object.pass2("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg1.s",
						"", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output1.txt", placeHolder1);
				
			ArrayList<LabelOffset> placeHolder2 = object.pass1("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg2.s",
					 "", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output2.txt");
				object.pass2("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg2.s",
						"", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output2.txt", placeHolder2);
			
			ArrayList<LabelOffset> placeHolder3 = object.pass1("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg3.s",
					 "", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output3.txt");
				object.pass2("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg3.s",
						"", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output3.txt", placeHolder3);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}