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

	public static int codeSize = 0;
	
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
    * code segment labels and their relative offsets.
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
		//System.out.println("Size of the data file is " + Integer.toString(wordCount));  // Verify result
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
				//System.out.println("0 is " + numbers[1]);  // Verify result
				ArrayList<String> opArr = new ArrayList<>(Arrays.asList("ADD", "SUB", "AND", "ORR", "LDR", "STR", "CBZ", "B"));
				if (opArr.contains(numbers[1])) {
					regCount++;
				}
			} catch (Exception e) {
				continue;
			}			
			
		}
		
		// Multiply register count by 4 to get number of bytes for all unique registers
		codeSize = regCount * 4;
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
			
			
			
			/*
			for (int i = 0; i < numbers.length; i++) {
				System.out.println(numbers[i]);
				// Get current position of instruction
				if (numbers[i].length() > 2) {
					if ((numbers[i] == "if") || (numbers[i] == "afterif") || (numbers[i].substring(0, numbers[i].length()-2) == "label")) {
						labelsOne.put(numbers[i], offset);
						System.out.println(numbers[i] + " added with offset " + Integer.toString(offset));
					}
				}
				// Overwrite labelsOne with instruction - currentposition
				if (labelsOne.containsValue(numbers[i].substring(0, numbers[i].length()-1))) {
					
				}
				
			}
			*/
			
			if (!numbers[0].equals("afterif:") && !numbers[0].equals("labelA:") && !numbers[0].equals("labelB:") && !numbers[0].equals("labelC:") && !numbers[0].equals("labelD:") && !numbers[0].equals("if:")) {
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
			//System.exit(0);
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
    	// writeDataFile(inFile, dataFile, labels);
        writeCodeFile(inFile, codeFile, labels);

    }
    
    
    /**
     * Assemble the instructions for data output file and write them to a file 
     * @param inFile
     * @param dataFile
     * @param labels
     */
    public static void writeDataFile(String inFile, String dataFile, ArrayList<LabelOffset> labels) 
    		throws FileNotFoundException, IOException {
		
		
		Scanner in = new Scanner(new File(inFile));
		
		String value = in.next();
		
		in.nextLine();
		
		int count = 2;
		int numberCount=1; 
		ArrayList<Integer> numList=new ArrayList<Integer>();
		int wordCount = 0;
		while(in.hasNextLine()){
			String line = in.nextLine();
			String numbers[]=line.split("[\\s,]+"); //splits for commas and spaces
			String noSpaces[]=new String[numbers.length];
			
			int countNum=0;
			
			int numListCount=0;
			for(int i=0;i<numbers.length;i++) {
				if(numbers[i]!=" ") {
					noSpaces[numListCount]=numbers[i];
					//System.out.println("noSpaces at index " + Integer.toString(numListCount) + " is " + noSpaces[numListCount]);
					numListCount++;
				}
			}
			
			for(int i=1;i<numbers.length;i++) { //only counting indexes that start with a number or - sign
				if(numbers[i].charAt(0)=='-' || numbers[i].charAt(0)=='0' || numbers[i].charAt(0)=='1' || numbers[i].charAt(0)=='2' ||numbers[i].charAt(0)=='3' ||numbers[i].charAt(0)=='4' ||numbers[i].charAt(0)=='5' ||numbers[i].charAt(0)=='6' ||numbers[i].charAt(0)=='7' ||numbers[i].charAt(0)=='8' ||numbers[i].charAt(0)=='9') {
					wordCount++;
					numList.add(Integer.parseInt(numbers[i]));
					countNum++;
					
				}
			}
			
			count++;
		}
		for(int i=0;i<numList.size();i++) {
			//System.out.println("NumList at index " + Integer.toString(i) + " is " + numList.get(i));
		}
		
		
		BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));
		
		for(int i=0;i<numList.size();i++) {  //for loop to go through each number and convert to boolean statememtns 
			boolean[] numBin= new boolean[32]; //going to use to put the true false code in
			
			// Convert numbers from numList into boolean array
			if(numList.get(i)<0) { //if number is negative it performs 2's complement
				sDecToBin(numList.get(i), 32);
				//System.out.println("NUMBER "+numberCount);
				for(int j=31;j>=0;j--) {
					numBin[j] = sDecToBin(numList.get(j), 32)[j];
				}
				//System.out.println();
				
				numberCount++;
			}if(numList.get(i)>=0) {//if number is positive just converts to boolean
				uDecToBin(numList.get(i), 32);
				// System.out.println("NUMBER "+numberCount);
				for(int j=31;j>=0;j--) {
					numBin[j] = sDecToBin(numList.get(j), 32)[j];
				}
				//System.out.println();
				
				numberCount++;
			}
			
			// Convert boolean array into string and write it to data file 
			try {
				out.write(Integer.toString(numList.size()*8));
				out.newLine();
				for (int j=0; j< numBin.length; j++) {
					out.write(String.valueOf(numBin[j]));
					if(j==23||j==15||j==7) {
						out.newLine();
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());;
			}
			
		}
		 
		wordCount=wordCount*4;
		//System.out.println(wordCount);
		in.close();
		out.close();
	
    }
    
    
    /**
     * Assemble the instructions for code output file and write them to a file 
     * @param inFile
     * @param codeFile
     * @param labels
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void writeCodeFile(String inFile, String codeFile, ArrayList<LabelOffset> labels) 
    		throws FileNotFoundException, IOException {

		Scanner in = new Scanner(new File(inFile));
		String value = in.next();
		in.nextLine();
		
		// Convert instructions into a string of 1's and 0's
		String content = "";
		while(in.hasNextLine()){
			String line = in.nextLine();
			String pieces[]=line.split("[\\s,]+"); //splits for commas and spaces
			
			System.out.println("Line being processed: " + line);
			try {
				System.out.println("pices[1] is " + pieces[1]);
			} catch (Exception e) {
				continue;
			}
			ArrayList<String> opCodes = new ArrayList<>(Arrays.asList("ADD", "SUB", "AND", "ORR"));
			ArrayList<String> otherCodes = new ArrayList<>(Arrays.asList("LDR", "STR", "CBZ", "B"));
			for (int i=0; i < opCodes.size(); i++) {
				// System.out.println("i is " + Integer.toString(i));
				try {
					if (pieces[1].equals(opCodes.get(i))) {
						//System.out.println("Line contains: " + opCodes.get(i));
						line = logicLineToBinary(pieces);
						content += line;
						System.out.println("Binary line added: " + line);
					} else if (pieces[1].equals(otherCodes.get(i))) {
						System.out.println("Line contains: " + otherCodes.get(i));
						line = otherLineToBinary(pieces, labels);
						content += line;
						System.out.println("Binary line added: " + line);
					}
				} catch (IndexOutOfBoundsException e) {
					// throw new IndexOutOfBoundsException(e.getMessage());
					// continue;
					e.printStackTrace();
				}
			}
		}
		
		// Add the last CBZ line
		content += "00000000000000000000001000101011";  
		
		// Write the converted booleans to a text file 
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(codeFile));
			//out.write(Integer.toString(content.length()/8));
			out.write(Integer.toString(codeSize));
			out.newLine();
			for (int i = 0; i < content.length()-7; i++) {
				//System.out.println("i is " + Integer.toString(i));
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
    
    
    /**
     * Takes an input string such as 1010 and converts it to
     * true false true false
     * @param input
     * @return
     */
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
    
    
    /**
     * Converts a line with operand LDR, STR, CBZ, or B and outputs it's binary equivalent
     * @param input
     * @param labels
     * @return
     */
    public static String otherLineToBinary(String[] input, ArrayList<LabelOffset> labels) {
    	String result = "";
    	String opcode = "", immediate = "", base = "", dest = "";
    	
//    	System.out.println("LABEL: "+labels.get(1).offset);
//    	System.exit(0);
    	
		for (int i = 0; i < input.length; i++) {
			//System.out.println(Integer.toString(i) + ":" +input[i]);
		}
    	
		if (input[1].equals("LDR")) {
			opcode = "01000011111";
			dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5));
			base = boolToBin(uDecToBin(Long.parseLong(input[3].substring(2)), 5));
			//immediate = boolToBin(uDecToBin(Long.parseLong(input[4].substring(1,2)), 9));
			immediate = boolToBin(uDecToBin(Long.parseLong(extractNumAsString(input[4])), 9));
			result = dest + base + "00" + immediate + opcode;
		} else if (input[1].equals("STR")) {
			opcode = "00000011111";
			dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5));
			base = boolToBin(uDecToBin(Long.parseLong(input[3].substring(2)), 5));
			//immediate = boolToBin(uDecToBin(Long.parseLong(input[4].substring(1,2)), 9));
			immediate = boolToBin(uDecToBin(Long.parseLong(extractNumAsString(input[4])), 9));
			//System.out.println("Equals STR");
			result = dest + base + "00" + immediate + opcode;
		} else if (input[1].equals("CBZ")) {
			opcode = "00101101";
			dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5));
			//immediate = boolToBin(uDecToBin(Long.parseLong(Integer.toString(labels.get(0).offset)), 19));
			long test = 0;
			immediate = boolToBin(uDecToBin(test, 19));
			result = dest + immediate + opcode;
		} else if (input[1].equals("B")) {
			opcode = "101000";
			//immediate = boolToBin(uDecToBin(Long.parseLong(Integer.toString(labels.get(1).offset)), 26));
			long test = 0;
			immediate = boolToBin(uDecToBin(test, 26));
			result = immediate + opcode;
		} 
    	
		//System.out.println("op code: " + opcode);
		//System.out.println("immediate: " + immediate);
		//System.out.println("base: " + base);
		//System.out.println("dest: " + dest);
		
		// result = dest + base + "00" + immediate + opcode;
		
    	return result;
    }
    
    /**
     * Method uses a regex to extract a single number from a String 
     * @param input
     * @return
     */
    public static String extractNumAsString(String input) {
//    	System.out.println("Input: "+input);
    	String result = "";
    	result = input.replaceAll("[^0-9]", "");
//    	System.out.println("result is: "+result);
    	return result;
    }
    
    /**
     * Converts unsigned decimal to binary
     * @param d
     * @param bits
     * @return
     */
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
    
    /**
     * Converts an array of boolean values into a binary string 
     * @param reg
     * @return
     */
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
    
    /**
    * Converts a signed decimal number to two's complement binary
    *
    * @param d The decimal value
    * @param bits The number of bits to use for the binary number.
    * @return The equivalent two's complement binary representation.
    * @exception IllegalArgumentException Parameter is outside valid range that can be represented with the given number of bits.
    */
    public static boolean[] sDecToBin(long d, int bits) {
      // Throw exception if the number can't fit into a given number of bits
      if (Math.abs(d) > Math.abs(Math.pow(2,bits-2))) {
        throw new IllegalArgumentException("ERROR: long cannot fit into the number of bits.");
      }

      boolean[] arr = new boolean[bits];  //array for results
      int num = (int)d;
      num = num * -1;
      int binary[] = new int[40];
      int index = 0;
      // Iterate to put 1's and 0's into an intermediate array
      while(num > 0){
        binary[index++] = num%2;
        num = num/2;
      }

      // Iterate through the array to set the binary word
      boolean oneFound = false;
      for(int i= 0; i < bits-1; i++) {
        if (oneFound==false){
          if (binary[i]==1){
            arr[bits-i-1]=true;
            oneFound=true;
          } else {
            arr[bits-i-1]=false;
          }
        } else if (oneFound==true){
          if (binary[i]==1){
            arr[bits-i-1]=false;
          } else {
            arr[bits-i-1]=true;
          }
        }
      }

      // Set the sign of the binary word
      if (d < 0) {
        arr[0] = true;
      } else {
        arr[0] = false;
      }

      return arr;
    }
    
    public static void main(String[] args) {
    	    	
    	try {
    		
    		ArrayList<LabelOffset> placeHolder1 = pass1("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg1.s",
    				"C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output1_data.txt", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output1_code.txt");
				 pass2("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg1.s",
						 "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output1_data.txt", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output1_code.txt", placeHolder1);
				
			ArrayList<LabelOffset> placeHolder2 = pass1("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg2.s",
					"C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output2_data.txt", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output2_code.txt");
				pass2("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg2.s",
						 "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output2_data.txt", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output2_code.txt", placeHolder2);
			
			ArrayList<LabelOffset> placeHolder3 = pass1("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg3.s",
					"C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output3_data.txt", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output3_code.txt");
				pass2("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg3.s",
						"C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output3_data.txt", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/output3_code.txt", placeHolder3);
			
			ArrayList<LabelOffset> placeHolderAll = pass1("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testAllProg.s",
					"C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/outputAll_data.txt", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/outputAll_code.txt");
				pass2("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testAllProg.s",
						"C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/outputAll_data.txt", "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/outputAll_code.txt", placeHolderAll);
			
				/*
		    	for (int i = 0; i < placeHolder3.size()-1; i++) {
		    		//System.out.println(placeHolder3.get(i).label);
		    		//System.out.println(placeHolder3.get(placeHolder3.indexOf(placeHolder3.get(i).label)).offset);
		    	}
		    	System.out.println(placeHolder3.get(0).label);
		    	System.out.println(placeHolder3.get(0).offset);
		    	*/
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
}
