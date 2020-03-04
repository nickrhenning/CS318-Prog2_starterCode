/**
* Assembler for the CS318 simple computer simulation
*/
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;


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
    	
    	
    	
        // PROGRAMMING ASSIGNMENT 2: COMPLETE THIS METHOD
	String fileName = "/Users/calebdimenstein/Desktop/"+inFile;
		
		File textFile = new File(fileName);
		
		Scanner in = new Scanner(textFile);
		
		String value = in.next();
	//	System.out.println("Read value: " + value);
		
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
				if((numbers[i].charAt(0)=='-' || numbers[i].charAt(0)=='0' || numbers[i].charAt(0)=='1' || numbers[i].charAt(0)=='2' ||numbers[i].charAt(0)=='3' ||numbers[i].charAt(0)=='4' ||numbers[i].charAt(0)=='5' ||numbers[i].charAt(0)=='6' ||numbers[i].charAt(0)=='7' ||numbers[i].charAt(0)=='8' ||numbers[i].charAt(0)=='9') && (Integer.parseInt(numbers[i])!=0)) {
					wordCount++;
					numList.add(Integer.parseInt(numbers[i]));
					countNum++;
					
				}
			}
			
			count++;
		}
		for(int i=0;i<numList.size();i++) {
			System.out.println(numList.get(i));
		}
		
		
		ArrayList<Boolean> numBin= new ArrayList<Boolean>(); //going to use to put the true false code in
		for(int i=0;i<numList.size();i++) {  //for loop to go through each number and convert to boolean statememtns 
			if(numList.get(i)<0) { //if number is negative it performs 2's complement
				Binary.sDecToBin(numList.get(i), 32);
				System.out.println("NUMBER "+numberCount);
				for(int j=31;j>=0;j--) {//new line after every 8 boolean statements
					if(j==23||j==15||j==7) {
						System.out.println();
					}
					System.out.print(Binary.sDecToBin(numList.get(i), 32)[j]+", ");
				}
				System.out.println();
				
				numberCount++;
			}if(numList.get(i)>=0) {//if number is positive just converts to boolean
				Binary.uDecToBin(numList.get(i), 32);
				System.out.println("NUMBER "+numberCount);
				for(int j=31;j>=0;j--) {
					if(j==23||j==15||j==7) {
						System.out.println();
					}
					System.out.print(Binary.uDecToBin(numList.get(i), 32)[j]+", ");
				}
				System.out.println();
				
				numberCount++;
			}
		}
		 
		wordCount=wordCount*4;
		System.out.println(wordCount);
		in.close();
		
		  
        // placeholder return. Student should replace with correct return.
        return new ArrayList<LabelOffset>();

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
}
