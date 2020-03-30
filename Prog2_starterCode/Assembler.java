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
    	String fileName = "/Users/calebdimenstein/Desktop/Prog2_starterCode/"+inFile;
		Scanner in = new Scanner(new File(fileName));
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
		in.close();
		
		/*
		 * Retrieve size of the code file and verify result
		 */
		in = new Scanner(new File(fileName));
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
		in.close();
		
		/*
		 * Determine code segment labels and relative offsets
		 */
		in = new Scanner(new File(fileName));
		value = in.next();
		in.nextLine();
		
		// ArrayList for labels and offsets
		Map<String,Integer> labelsZero = new HashMap<String,Integer>();
		
		int offset = Integer.MAX_VALUE;  // denotes byte offset from beginning of code		

		while(in.hasNextLine()) {
			String line = in.nextLine();
			String numbers[]=line.split("[\\s,]+"); //splits for commas and spaces
			String op = numbers[0];
			
			if (op.equals("main:")) {
				offset = -4;
			} 
			
			// Initialize values of labels map with label and current line offset
			
			
			
			for (int i = 0; i < numbers.length; i++) {
				// Get current position of instruction
				if (numbers[i].equals("if")) {
					labelsZero.put("if", offset);
				} else if (numbers[i].equals("afterif")) {
					labelsZero.put("afterif", offset);
				} else if (numbers[i].equals("labelA")) {
					labelsZero.put("labelA", offset);
					System.out.println("INSERTED LabelA");
				} else if (numbers[i].equals("labelB")) {
					labelsZero.put("labelB", offset);
					System.out.println("INSERTED LabelB");
				} else if (numbers[i].equals("labelC")) {
					labelsZero.put("labelC", offset);
					System.out.println("INSERTED LabelC");
				} else if (numbers[i].equals("labelD")) {
					labelsZero.put("labelD", offset);
					System.out.println("INSERTED LabelD");
				}
				/*
				// Overwrite labelsOne with instruction - currentposition
				if (numbers[i].equals("if:")) {
					int immediate = offset - labelsOne.get("if");
					labelsOne.put("if", immediate);
				} else if (numbers[i].equals("afterif:")) {
					int immediate = offset - labelsOne.get("afterif");
					labelsOne.put("afterif", immediate);
				} else if (numbers[i].equals("labelA:")) {
					int immediate = offset - labelsOne.get("labelA");
					labelsOne.put("labelA", immediate);
				} else if (numbers[i].equals("labelB:")) {
					int immediate = offset - labelsOne.get("labelB");
					labelsOne.put("labelB", immediate);
				} else if (numbers[i].equals("labelC:")) {
					int immediate = offset - labelsOne.get("labelC");
					labelsOne.put("labelC", immediate);
				} else if (numbers[i].equals("labelD:")) {
					int immediate = offset - labelsOne.get("labelD");
					labelsOne.put("labelD", immediate);
				}
				*/
			}
			
			//calculates the number of lines since the label was called to when it was declared
			if (!numbers[0].equals("afterif:") && !numbers[0].equals("labelA:") && !numbers[0].equals("labelB:") && !numbers[0].equals("labelC:") && !numbers[0].equals("labelD:") && !numbers[0].equals("if:")) {
				offset += 4;				
			}

		}
		
		// IMPORTANT
		// Delete if issues
		
		Map<String,Integer> labelsOne = new HashMap<String,Integer>();
		
		in = new Scanner(new File(fileName));
		value = in.next();
		in.nextLine();
		
		while(in.hasNextLine()) {
			String line = in.nextLine();
			String numbers[]=line.split("[\\s,]+"); //splits for commas and spaces
			String op = numbers[0];
			
			if (op.equals("main:")) {
				offset = -4;
			} 
			//sets the immediates 
			for (int i = 0; i < numbers.length; i++) {
				if (numbers[i].equals("if:")) {
					int immediate = offset - labelsZero.get("if");
					labelsOne.put("if", immediate);
				} else if (numbers[i].equals("afterif:")) {
					int immediate = offset - labelsZero.get("afterif");
					labelsOne.put("afterif", immediate);
				} else if (numbers[i].equals("labelA:")) {
					int immediate = offset - labelsZero.get("labelA");
					labelsOne.put("labelA", immediate);
					System.out.println("CALCULATED LabelA as " + immediate);
				} else if (numbers[i].equals("labelB:")) {
					int immediate = offset - labelsZero.get("labelB");
					labelsOne.put("labelB", immediate);
					System.out.println("CALCULATED LabelB as " + immediate);
				} else if (numbers[i].equals("labelC:")) {
					int immediate = offset - labelsZero.get("labelC");
					labelsOne.put("labelC", immediate);
					System.out.println("CALCULATED LabelC as " + immediate);
				} else if (numbers[i].equals("labelD:")) {
					int immediate = offset - labelsZero.get("labelD");
					labelsOne.put("labelD", immediate);
					System.out.println("CALCULATED LabelD as " + immediate);
				}
			}
			if (!numbers[0].equals("afterif:") && !numbers[0].equals("labelA:") && !numbers[0].equals("labelB:") && !numbers[0].equals("labelC:") && !numbers[0].equals("labelD:") && !numbers[0].equals("if:")) {
				offset += 4;				
			}
		}
		
		// End delete
		
		
		// Assign values of code segment labels and offsets into a new ArrayList<LabelOffset>() object
		ArrayList<LabelOffset> result = new ArrayList<LabelOffset>();

		System.out.println("Assigning Offsets");
		// Iterate through labels and assign values to result
		for (String key : labelsOne.keySet() ) {
			LabelOffset temp = new LabelOffset();
			temp.label = key;
			temp.offset = labelsOne.get(key);
			System.out.println("Offset key set to: " + key);
			System.out.println("Offset value set to: " + temp.offset);
			result.add(temp);
		}
		
		/*
		for (int i = 0; i < labelsOne.size(); i++) {
			
			try {
				LabelOffset temp = new LabelOffset();
				temp.label = labelsOne.
			}
			
			
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
			*/

		/*
		// Verify correct values in the LabelOffset array
		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i).label + ": " + result.get(i).offset);
		}
		in.close();
		*/
		
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
       System.out.println(labels);
		writeData(inFile, dataFile);
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
		
    	String fileName = "/Users/calebdimenstein/Desktop/Prog2_starterCode/"+inFile;

		Scanner in = new Scanner(new File(fileName));
		
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

		
		BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));
		
		for(int i=0;i<numList.size();i++) {  //for loop to go through each number and convert to boolean statememtns 
			boolean[] numBin= new boolean[32]; //going to use to put the true false code in
			
			// Convert numbers from numList into boolean array
			if(numList.get(i)<0) { //if number is negative it performs 2's complement
				sDecToBin(numList.get(i), 32);
				for(int j=31;j>=0;j--) {
					numBin[j] = sDecToBin(numList.get(j), 32)[j];
				}
				
				numberCount++;
			}if(numList.get(i)>=0) {//if number is positive just converts to boolean
				uDecToBin(numList.get(i), 32);
				for(int j=31;j>=0;j--) {
					numBin[j] = sDecToBin(numList.get(j), 32)[j];
				}
				
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
    	System.out.println("TEST");
    	String fileName = "/Users/calebdimenstein/Desktop/Prog2_starterCode/"+inFile;
		Scanner in = new Scanner(new File(fileName));
		String value = in.next();
		in.nextLine();
		
		// Convert instructions into a string of 1's and 0's
		String content = "";
		while(in.hasNextLine()){
			String line = in.nextLine();
			String pieces[]=line.split("[\\s,]+"); //splits for commas and spaces
			
			try {
			} catch (Exception e) {
				continue;
			}
			
			
			ArrayList<String> opCodes = new ArrayList<>(Arrays.asList("ADD", "SUB", "AND", "ORR"));
			ArrayList<String> otherCodes = new ArrayList<>(Arrays.asList("LDR", "STR", "CBZ", "B"));
			for (int i=0; i < opCodes.size(); i++) {
				try {
					if (pieces[1].equals(opCodes.get(i))) {
						line = logicLineToBinary(pieces);
						content += line;
					} else if (pieces[1].equals(otherCodes.get(i))) {
						line = otherLineToBinary(pieces, labels);
						content += line;
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
				if ((i%8) == 0) {
						String bite = content.substring(i,i+8);
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
    	for (int i = 0; i < input.length(); i++) {
    		if (input.substring(i,i+1).equals("0")) {
    			result = result + "false";
    		} else if (input.substring(i,i+1).equals("1")) {
    			result = result + "true";
    		} 
    		if (i != (input.length() - 1)) { result = result + " ";}
    	}
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
		src1 = boolToBin(uDecToBin(Long.parseLong(input[3].substring(1)), 5));
		src2 = boolToBin(uDecToBin(Long.parseLong(input[4].substring(1)), 5));	
		result = dest + src1 + "000000" + src2 + opcode; 
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
    	
		if (input[1].equals("LDR")) {
			opcode = "01000011111";
			dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5)); //Dest Register
			base = boolToBin(uDecToBin(Long.parseLong(input[3].substring(2)), 5)); //Source Register
			//immediate = boolToBin(uDecToBin(Long.parseLong(input[4].substring(1,2)), 9));
			immediate = boolToBin(uDecToBin(Long.parseLong(extractNumAsString(input[4])), 9));
			result = dest + base + "00" + immediate + opcode;
		} else if (input[1].equals("STR")) {
			opcode = "00000011111";
			dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5)); //Dest Register
			base = boolToBin(uDecToBin(Long.parseLong(input[3].substring(2)), 5)); //Base Register
			//immediate = boolToBin(uDecToBin(Long.parseLong(input[4].substring(1,2)), 9));
			immediate = boolToBin(uDecToBin(Long.parseLong(extractNumAsString(input[4])), 9));
			result = dest + base + "00" + immediate + opcode;
		} else if (input[1].equals("CBZ")) {
			opcode = "00101101";
			dest = boolToBin(uDecToBin(Long.parseLong(input[2].substring(1)), 5)); //Dest Register
			System.out.println("CBZ");
			String val1 = Integer.toString(getOffset(input[3], labels));
			immediate = boolToBin(uDecToBin(Long.parseLong(val1), 19));
			//If the value is less than one convert to two's complement
			if (Integer.parseInt(val1) < 0) {
				// immediate = boolToBin(trueBinArray(26);
				immediate = "0001011111111111111";
				
				immediate = boolToBin(convertToTwosComplement(uDecToBin(Long.parseLong(val1) * -1, 19)));
				// System.out.println(immediate);
			}
			
			//long test = 0;
			//immediate = boolToBin(uDecToBin(test, 19));
			result = dest + immediate + opcode;
			System.out.println(dest);
			System.out.println(immediate);
			System.out.println(opcode);
		} else if (input[1].equals("B")) {
			System.out.println("B");
			opcode = "101000";
			String val1 = Integer.toString(getOffset(input[2], labels));
			System.out.println("val1 is " + val1);
			immediate = boolToBin(uDecToBin(Long.parseLong(val1), 26));
			
			if (Integer.parseInt(val1) < 0) {
				immediate = "00001111111111111111111111";
				
				immediate = boolToBin(convertToTwosComplement(uDecToBin(Long.parseLong(val1) * -1, 26)));
				//System.out.println(immediate);
			}
			
			//long test = 0;
			//immediate = boolToBin(uDecToBin(test, 26));
			result = immediate + opcode;
			System.out.println(immediate);
		} 
    	
		
		// result = dest + base + "00" + immediate + opcode;
		
    	return result;
    }
    //DELETE?
    public static boolean[] trueBinArray(int length) {
    	boolean[] result = new boolean[length];
    	for (int i = 0; i < result.length-1; i++) {
    		result[i] = true;
    		System.out.print(i);
    	}
    	return result;
    }
   
    /**
     * Converts boolean array to into twos complement form
     * @param b
     * @return
     */
    private static boolean[] convertToTwosComplement(boolean[] b) {
        boolean[] result = new boolean[b.length];
        // Flip bits after a 1 is found
        boolean oneFound = false;
        // Revert the bits of inputB
        for (int i = b.length-1; i >= 0; i--){
          System.out.println(Integer.toString(i) + " index has bool value " + String.valueOf(b[i]));
          if (oneFound) {
	    	if (b[i]==true) {
	            result[i] = false;
            } else if (b[i]==false){
                result[i]=true;
            }
          } else if (oneFound == false) {
        	  if (b[i]==true) {
        		  oneFound = true;
        		  result[i] = true;
        		  System.out.println("Onefound set to positive at index " + Integer.toString(i));
        		  System.out.println("Index 15 is " + String.valueOf(b[i]));
        	  }
          } else {
              throw new IllegalArgumentException("ERROR WHILE flipping bits of input b in subtraction method.");
          }
        }
        
        //Verify 
        for (int i = b.length-1; i >= 0; i--){
            System.out.println("Index "+ Integer.toString(i)+" is " + String.valueOf(result[i]));
        }

        return result;
      }
    
    /**
     * Method uses a regex to extract a single number from a String 
     * @param input
     * @return
     */
    public static String extractNumAsString(String input) {
    	String result = "";
    	result = input.replaceAll("[^0-9]", "");
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
    
    
    /**
     * Searches an ArrayList of LabelOffsets to return the offset of a given label
     * @param label
     * @param labels
     * @return
     */
    public static int getOffset(String label, ArrayList<LabelOffset> labels) {
    	System.out.println("GetOffset method started. Looking for label " + label);
    	int offset = 0;
    	for (int i = 0; i < labels.size(); i++) {
    		if (labels.get(i).label.equals(label)) {
    			offset = labels.get(i).offset;
    			System.out.println("Offset " + Integer.toString(offset) + " will be returned.");
    		}
    	}
    	if (offset == 0) {
    		System.out.println("ERROR: correct offset not set");
    	}
    	return offset;
    }
    
    public static void main(String[] args) {
    	    	
    	try {
    		
    		ArrayList<LabelOffset> placeHolder1 = pass1("/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg1.s",
    				"/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg1.data", "/Users/calebDimenstein/Desktop/Prog2_StarterCode/testProg1.code");
				 pass2("/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg1.s",
		    				"/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg1.data", "/Users/calebDimenstein/Desktop/Prog2_StarterCode/testProg1.code", placeHolder1);
				
			ArrayList<LabelOffset> placeHolder2 = pass1("/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg2.s",
    				"/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg2.data", "/Users/calebDimenstein/Desktop/Prog2_StarterCode/testProg2.code");
				pass2("/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg1.s",
	    				"/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg2.data", "/Users/calebDimenstein/Desktop/Prog2_StarterCode/testProg2.code", placeHolder2);
			
			ArrayList<LabelOffset> placeHolder3 = pass1("/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg3.s",
    				"/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg3.data/", "/Users/calebDimenstein/Desktop/Prog2_StarterCode/testProg3.code");
				pass2("/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg3.s",
	    				"/Users/calebdimenstein/Desktop/Prog2_starterCode/testProg3.data", "/Users/calebDimenstein/Desktop/Prog2_StarterCode/testProg3.code", placeHolder3);
			
			ArrayList<LabelOffset> placeHolderAll = pass1("/Users/calebdimenstein/Desktop/Prog2_starterCode/testAllProg.s",
    				"/Users/calebdimenstein/Desktop/Prog2_starterCode/testAllProg.data", "/Users/calebDimenstein/Desktop/Prog2_StarterCode/testAllProg.code");
				pass2("/Users/calebdimenstein/Desktop/Prog2_starterCode/testAllProg.s",
	    				"/Users/calebdimenstein/Desktop/Prog2_starterCode/testAllProg.data", "/Users/calebDimenstein/Desktop/Prog2_StarterCode/testAllProg.code", placeHolderAll);
			
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



    public static void writeData(String inFile, String dataFile) throws FileNotFoundException {
    	String fileName = "/Users/calebdimenstein/Desktop/Prog2_starterCode/"+inFile;
		
		File textFile = new File(fileName);
		
		Scanner in = new Scanner(textFile);
		
		String value = in.next();
	//	System.out.println("Read value: " + value);
		
		in.nextLine();
		
		int count = 2;
		int numberCount=1; 
		ArrayList<Integer> numList=new ArrayList<Integer>();
		int wordCount = 0;
		ArrayList<String> strNums=new ArrayList<String>();
		while(in.hasNextLine()){
			String line = in.nextLine();
			String numbers[]=line.split("[\\s,]+"); //splits for commas and spaces
			String noSpaces[]=new String[numbers.length];
			for(int i=0;i<numbers.length;i++) {
				strNums.add(numbers[i]);
			}
			
		}
			

			
			
			
			for(int i=1;i<strNums.size();i++) { //only counting indexes that start with a number or - sign
				if((strNums.get(i).startsWith("-") || strNums.get(i).startsWith("0") || strNums.get(i).startsWith("1") || strNums.get(i).startsWith("2") ||strNums.get(i).startsWith("3") ||strNums.get(i).startsWith("4") ||strNums.get(i).startsWith("5") ||strNums.get(i).startsWith("6") ||strNums.get(i).startsWith("7") ||strNums.get(i).startsWith("8") ||strNums.get(i).startsWith("9"))) {
					System.out.println(strNums.get(i));
					wordCount++;
					numList.add(Integer.parseInt(strNums.get(i)));
					
					
				}
			}
			
			count++;
		
	
		wordCount=wordCount*4;
	
		
		String testFileReader="";
		ArrayList<Boolean> numBin= new ArrayList<Boolean>(); //going to use to put the true false code in
		for(int i=0;i<numList.size();i++) {  //for loop to go through each number and convert to boolean statememtns 
			if(numList.get(i)<0) { //if number is negative it performs 2's complement
				Binary.sDecToBin(numList.get(i), 32);
				System.out.println("NUMBER "+numberCount);
				for(int j=31;j>=0;j--) {//new line after every 8 boolean statements
					if(j==23||j==15||j==7) {
						
						testFileReader+="\n";
						System.out.println();
					}
					testFileReader+=Binary.sDecToBin(numList.get(i), 32)[j]+" ";
					System.out.print(Binary.sDecToBin(numList.get(i), 32)[j]+", ");
				}
				testFileReader+="\n";
				System.out.println();
				
				numberCount++;
			}if(numList.get(i)>=0) {//if number is positive just converts to boolean
				Binary.uDecToBin(numList.get(i), 32);
				System.out.println("NUMBER "+numberCount);
				for(int j=31;j>=0;j--) {
					if(j==23||j==15||j==7) {
						testFileReader+="\n";
						System.out.println();
					}
					testFileReader+=(Binary.uDecToBin(numList.get(i), 32)[j])+" ";
					System.out.print(Binary.uDecToBin(numList.get(i), 32)[j]+", ");
				}
				testFileReader+="\n";
				System.out.println();
				
				numberCount++;
			}
		}
		// System.out.println(testFileReader);
		
		
		try {
			BufferedWriter outTEST = new BufferedWriter(new FileWriter(dataFile));
			outTEST.write(Integer.toString(wordCount));
			outTEST.newLine();
			outTEST.write(testFileReader);
			outTEST.close();
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
	
		
		//System.out.println(wordCount);
		in.close();

        }
   
    
}
