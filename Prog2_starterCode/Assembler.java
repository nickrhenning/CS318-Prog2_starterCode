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
		
		// For each line, match on register values and increment regsCount for each UNIQDUE register
		while(in.hasNextLine()) {
			String line = in.nextLine();
			Pattern pattern = Pattern.compile("(R[0-9]*)");  
			Matcher matcher = pattern.matcher(line);
			
			// Registers array tracks the registers whose bytes have already been accounted for 
			String[] regsArr = {"test"};
			
			int matchNum = 0;
			// If a register hasn't been accounted for, increment register Count
			while (matcher.find()) {
			    boolean found = Arrays.stream(regsArr).anyMatch(t -> t.equals(matcher.group(0)));
			    if ((found) && (!matcher.group(0).equals("R"))) {
			    	continue;
			    } else {
			    	regsArr[matchNum] = matcher.group(0);
			    	regCount++;
			    }
			}
		}
		
		// Multiply register count by 4 to get number of bytes for all unique registers
		int codeSize = regCount * 4;
		System.out.println("Size of the code file is " + Integer.toString(codeSize));  // Verify result
		
		
		
		// Assign values into a new ArrayList<LabelOffset>() object
		ArrayList<LabelOffset> result = new ArrayList<LabelOffset>();
		
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

        // PROGRAMMING ASSIGNMENT 2: COMPLETE THIS METHOD

        }
    
    public static void main(String[] args) {
    	Assembler object = new Assembler();
    	
    	String progOneFile = "C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg1.s";
    	
    	try {
			object.pass1("C:/Users/nickh/OneDrive/Documents/CS318/Prog2_starterCode/Prog2_starterCode/testProg1.s",
					"", "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
