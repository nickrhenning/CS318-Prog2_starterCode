/**
* Represents a simple CPU based on the ARMv8 datapath.
*
* CS318 Programming Assignment 4
* @author Caleb Dimenstein
* @author Nick Henning
*
*/
import java.io.*;
import java.util.Arrays;

public class CPU {

    /** Memory unit for instructions */
    private Memory instructionMemory;

    /** Memory unit for data */
    private Memory dataMemory;

    /** Register unit */
    private Registers registers;

    /** Arithmetic and logic unit */
    private ALU alu;

    /** Adder for incrementing the program counter */
    private ALU adderPC;

    /** Adder for computing branches */
    private ALU adderBranch;

    /** Control unit */
    private SimpleControl control;

    /** Multiplexor output connects to Read Register 2 */
    private Multiplexor2 muxRegRead2;

    /** Mulitplexor ouptut connects to ALU input B */
    private Multiplexor2 muxALUb;

    /** Multiplexor output connects to Register Write Data */
    private Multiplexor2 muxRegWriteData;

    /** Multiplexor output connects to Program Counter */
    private Multiplexor2 muxPC;

    /** Program counter */
    private boolean[] pc;

    /**
    * STUDENT SHOULD NOT MODIFY THIS METHOD
    *
    * Constructor initializes all data members.
    *
    * @param iMemFile Path to the file with instruction memory contents.
    * @param dMemFile Path to the file with data memory contents.
    * @exception FileNotFoundException if a file cannot be opened.
    */
    public CPU(String iMemFile, String dMemFile) throws FileNotFoundException {

        // Create objects for all data members
        instructionMemory = new Memory(iMemFile);
        dataMemory = new Memory(dMemFile);
        registers = new Registers();
        alu = new ALU();
        control = new SimpleControl();
        muxRegRead2 = new Multiplexor2(5);
        muxALUb = new Multiplexor2(32);
        muxRegWriteData = new Multiplexor2(32);
        muxPC = new Multiplexor2(32);

        // Activate adderPC with ADD operation, and inputB set to 4
        // Send adderPC output to muxPC input 0
        adderPC = new ALU();
        adderPC.setControl(2);
        boolean[] four = Binary.uDecToBin(4L, 32);
        adderPC.setInputB(four);

        // Initalize adderBranch with ADD operation
        adderBranch = new ALU();
        adderBranch.setControl(2);

        // initialize program counter to 0
        pc = new boolean[32];
        for(int i = 0; i < 32; i++) {
            pc[i] = false;
        }
    }

    /**
    * STUDENT SHOULD NOT MODIFY THIS METHOD
    *
    * Runs the CPU in single cycle (non-pipelined) mode. Stops when a halt
    * instruction is decoded.
    *
    * This method can be used with any (assembled) assembly language program.
    */
    public void singleCycle() {

        int cycleCount = 0;

        // Start the first cycle.
        boolean[] instruction = fetch();
        boolean op = decode(instruction);

        // Loop until a halt instruction is decoded
        while(op) {
            execute();

            memoryAccess();

            writeBack();

            cycleCount++;

            // Start the next cycle
            instruction = fetch();

            op = decode(instruction);
        }

    }

    /**
    * STUDENT MUST ADD MORE TESTING CODE TO THIS METHOD AS INDICATED BY
    * COMMENTS WIHTIN THIS METHOD.
    *
    * DO NOT CHANGE the calls to the CPU private methods.
    *
    * The comments in this method indicate the minimum amount of testing code
    * that you must add. You are encouraged to add additional testing code
    * to help you develop and verify the correctness of the CPU private methods.
    * Tests for the first instruction in testProg3.s are included as an
    * example of how to test the correctness of the CPU private methods.
    *
    * Runs the CPU in single cycle (non-pipelined) mode. Stops when a halt
    * instruction is decoded.
    *
    * This method should only be used with the assembled testProg3.s
    * because this method verifies correct values based on that specific program.
    */
    public void runTestProg3() {

        int cycleCount = 0;
        
        // Start the first cycle.
        boolean[] instruction = fetch();

        // Example Test: Verify that when cycleCount is 0 the insruction returned by fetch is the
        // binary version of the first instruction from testProg3.s ADD R9,R31,R31
        boolean[] firstInstr = {true,false,false,true,false,true,true,true,true,true,false,false,false,false,false,false,true,true,true,true,true,false,false,false,true,true,false,true,false,false,false,true};
        if(cycleCount == 0 && !Arrays.equals(instruction,firstInstr)) {
            System.out.println("FAIL: cycle " + cycleCount + " did not fetch correct instruction:");
            System.out.println("------ fetch returned: " + Binary.toString(instruction));
            System.out.println("------ correct instruction: " + Binary.toString(firstInstr));
        }

        boolean op = decode(instruction);

        // Example Test: Verify that when cycleCount is 0 the control signals
        // are correctly set for an ADD instruction
        if(cycleCount == 0 && (control.Uncondbranch != false || control.RegWrite != true
            || control.Reg2Loc != false || control.MemWrite != false || control.MemtoReg != false
            || control.MemRead != false || control.Branch != false || control.ALUSrc != false
            || control.ALUControl != 2))
        {
        	System.out.println("FAIL: cycle " + cycleCount + " after decode, control lines incorrect");
        }

        // Loop until a halt instruction is decoded
        while(op) {
            boolean cycleFail = false;
        	execute();

            // Example Test: Verify that when cycleCount is 0 the ALU result is zero
            boolean[] correctALU = Binary.uDecToBin(0L, 32);
            if(cycleCount == 0 && !Arrays.equals(alu.getOutput(), correctALU)) {
                System.out.println("FAIL: cycle " + cycleCount + " incorrect ALU result:");
                System.out.println("------ ALU result: " + Binary.toString(alu.getOutput()));
                System.out.println("------ correct result: " + Binary.toString(correctALU));
                cycleFail = true;
            }

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the ALU result is the correct
            // data memory address (should be 16)
            //NOT CORRECT THE OUTPUT WAS 8 NOT 16
            
            boolean[] sixteen = {false, false, false, false, true, false};
            if(cycleCount==1 && (!Arrays.equals(to32(sixteen), alu.getOutput()))) {
            	System.out.println("FAIL ALU result at cycleCount "+ cycleCount+ " did not fetch the correct answer");
            	System.out.println("------ data memory address returned "+Binary.toString(alu.getOutput()));
            	System.out.println("------ correct return: " + Binary.toString(muxRegWriteData.output(false)));
            	cycleFail = true;
            }

            
            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 6, the branch adder's (adderBranch)
            // result is the offset of the branch destination instruction (should be 32)
            boolean[] thirtyTwo = {false, false, false, false, false, true};
            if(cycleCount==6 & Arrays.equals(muxPC.output(true),pc) && (Arrays.equals(to32(thirtyTwo), adderBranch.getOutput()))) {
            	System.out.println("cycle count: "+ cycleCount);
            	System.out.println("adderBranch Output: "+ adderBranch.getOutput());
            	System.out.println("FAIL branch adder's output at cycle "+ cycleCount+ " did not fetch the correct output");
            	System.out.println("------ adder branch returned "+Binary.toString(muxPC.output(true)));
            	System.out.println("------ correct data: " + Binary.toString(adderBranch.getOutput()));
            	cycleFail = true;
            }

            memoryAccess();

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the value that was read from
            // memory (should be 6) is in the register write multiplexor
            // (muxRegWriteData) at input 1
            boolean[] six = {false, true, true};
            if (cycleCount==1 && (muxRegWriteData.output(true)==registers.getReadReg2()) && (Arrays.equals(to32(six), muxRegWriteData.output(true)))){
            	System.out.println(muxRegWriteData.output(true));
            	System.out.println("Cycle Count "+ cycleCount);
            	System.out.println("FAIL: muxRegWriteData at cycle " + cycleCount + " did not fetch the correct number");
            	System.out.println("------ muxRegWriteData returned: "+ Binary.toString(muxRegWriteData.output(true)));
            	System.out.println("------ correct data:             " + Binary.toString(registers.getReadReg2()));
            	cycleFail = true;
            }
            writeBack();

            cycleCount++;

            // Start the next cycle
            instruction = fetch();

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 7, the instruction returned by fetch is
            // the last instruction in the program: STR R5,[R9,#8]
           
            boolean lastInstruction[]={false, false, false, false, false, false, false, false,false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, true, false, true, true };
            if(cycleCount==7 && (!Arrays.equals(instruction,lastInstruction))) {
            	System.out.println(cycleCount);
            	System.out.println("FAIL: cycle " + cycleCount + " did not fetch correct instruction:");
                System.out.println("------ fetch returned: " + Binary.toString(instruction));
                System.out.println("------ correct instruction: " + Binary.toString(lastInstruction));
                cycleFail = true;
            }


            op = decode(instruction);

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the the control signals are correctly
            // set for a LDR instruction
            
            if(cycleCount== 1 && (control.Uncondbranch != false || control.RegWrite != true
                    || control.Reg2Loc != false || control.MemWrite != false || control.MemtoReg != false
                    || control.MemRead != true || control.Branch != false || control.ALUSrc != true
                    || control.ALUControl != 2))
                {
            	System.out.println("FAIL: LDR cycle " + cycleCount + " after decode, control lines incorrect");
                cycleFail = true;
                }
            
            if (cycleFail == false) {
            	System.out.println("CYCLE "+cycleCount+" PASSED!");
            }

        }
        
       System.out.println("CPU halt after " + cycleCount + " cycles.");
        
    }


    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Instruction Fetch Step
    * Fetch the instruction from the instruction memory starting at address pc.
    * Activate the PC adder and place the adder's output into muxPC input 0.
    *
    * @return The instruction starting at address pc
    */
    private boolean[] fetch() {

        // Set inputA to current pc and inputB to 4
    	adderPC.setInputA(pc);
    	boolean four[] = {false, false, true, false, false};
    	adderPC.setInputB(to32(four));
    	
    	// Activate the adderPC to perform the operation and 
    	// place the result into input0 of muxPC
    	//adderPC.setControl(2);
    	
    	adderPC.activate();
    	muxPC.setInput0(adderPC.getOutput());
    	
        return instructionMemory.read32(pc);
    }

    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Instruction Decode and Register Read
    *
    * Decode the instruction. Sets the control lines and sends appropriate bits
    * from the instruction to various inputs within the processor.
    *
    * Set the Read Register inputs so that the values to be read from
    * the registers will be available in the next phase.
    *
    * @param instruction The 32-bit instruction to decode
    * @return false if the opcode is HLT; true for any other opcode
    */
    private boolean decode(boolean[] instruction) {
    	// System.out.println("Begin decode: program counter is: " + Long.toString(Binary.binToUDec(pc)));
    	
    	/*
    	 * Initialize opcodes and set control values 
    	 */
        boolean add[]= {false, false, false, true, true, false, true, false, false, false, true};
        boolean[] sub = {false, false, false, true, true, false, true, false, false, true, true};
    	boolean[] and = {false, false, false, false, true, false, true, false, false, false, true};
    	boolean[] orr = {false, false, false, false, true, false, true, false, true, false, true};
    	
    	boolean[] ldr = {false, true, false, false, false, false, true, true, true, true, true};
    	boolean[] str = {false, false, false, false, false, false, true, true, true, true, true};
    	
    	// boolean[] hlt= {true, true, false, true, false, true, false, false, false, true, false}; 
    	boolean[] hlt = {false, true, false, false, false, true, false, true, false, true, true};
    	
    	boolean[] opCode = new boolean[11];
    	for (int i = instruction.length - 11; i < instruction.length; i++) {
    		opCode[i - 21] = instruction[i];
    	}
    	
     	if (Arrays.equals(opCode, add)) {
    		control.Reg2Loc = false;
    		control.ALUSrc = false;
    		control.RegWrite = true;
    		control.MemWrite = false;
    		control.MemRead = false;
    		control.ALUControl = 2;
    		control.MemtoReg = false;
    		control.Uncondbranch = false;
    		control.Branch = false;
    	} else if (Arrays.equals(opCode, sub)) {
    		control.Reg2Loc = false;
    		control.ALUSrc = false;
    		control.RegWrite = true;
    		control.MemWrite = false;
    		control.MemRead = false;
    		control.ALUControl = 6;
    		control.MemtoReg = false;
    		control.Uncondbranch = false;
    		control.Branch = false;
    	} else if (Arrays.equals(opCode, and)) {
    		control.Reg2Loc = false;
    		control.ALUSrc = false;
    		control.RegWrite = true;
    		control.MemWrite = false;
    		control.MemRead = false;
    		control.ALUControl = 0;
    		control.MemtoReg = false;
    		control.Uncondbranch = false;
    		control.Branch = false;
    	} else if (Arrays.equals(opCode, orr)) {
    		control.Reg2Loc = false;
    		control.ALUSrc = false;
    		control.RegWrite = true;
    		control.MemWrite = false;
    		control.MemRead = false;
    		control.ALUControl = 1;
    		control.MemtoReg = false;
    		control.Uncondbranch = false;
    		control.Branch = false;
    	} else if (Arrays.equals(opCode, ldr)) {
    		control.Reg2Loc = false;
    		control.ALUSrc = true;
    		control.RegWrite = true;
    		control.MemWrite = false;
    		control.MemRead = true;
    		control.ALUControl = 2;
    		control.MemtoReg = false;
    		control.Uncondbranch = false;
    		control.Branch = false;
    	} else if (Arrays.equals(opCode, str)) {
    		control.Reg2Loc = true;
    		control.ALUSrc = true;
    		control.RegWrite = false;
    		control.MemWrite = true;
    		control.MemRead = false;
    		control.ALUControl = 2;
    		control.MemtoReg = false;
    		control.Uncondbranch = false;
    		control.Branch = false;
    	}
     
     	boolean[] CBZ = {true, false, true, true, false, true, false, true};
    	boolean[] B = {true, false, true, false, false, false};
     	
     	boolean[] opCodeCBZ = new boolean[8];
     	for (int i = instruction.length - 8; i < instruction.length; i++) {
    		opCodeCBZ[i - 24] = instruction[i];
    	}
     	boolean[] opCodeB = new boolean[6];
     	for (int i = instruction.length - 6; i < instruction.length; i++) {
    		opCodeCBZ[i - 26] = instruction[i];
    	}
     	
     	if(Arrays.equals(opCodeCBZ,CBZ)) {
     		control.Reg2Loc = true;
    		control.ALUSrc = false;
    		control.RegWrite = false;
    		control.MemWrite = false;
    		control.MemRead = false;
    		control.ALUControl = 2;
    		control.MemtoReg = false;
    		control.Uncondbranch = false; //look back to see if correct
    		control.Branch = true;
     	} else if (Arrays.equals(opCodeB, B)) {
     		control.Reg2Loc = false;
    		control.ALUSrc = false;
    		control.RegWrite = false;
    		control.MemWrite = false;
    		control.MemRead = false;
    		control.ALUControl = 2;
    		control.MemtoReg = false;
    		control.Uncondbranch = false;
    		control.Branch = true;
     	}
     	
     	
     	/*
     	 * Set Read and Write register addresses
     	 */
     
     	// Source and destination registers
     	boolean[] reg1 = new boolean[5];
     	boolean[] dest=new boolean[5];
     	boolean[] mux0=new boolean[5];
     	boolean[] mux1=new boolean[5];
     	
 		// Initialize source register 1 as bits 9-5
 		for(int i=5;i<10;i++) {
 			reg1[i-5]=instruction[i];
 		} 		
 		
 		// Initialize destination register as bits 0-4
 		for(int i=0;i<5;i++) {
 			dest[i]=instruction[i];
 		}
 		
 		// Initialize mux input 0 as bits 20-16
 		for(int i=16;i<21;i++) {
 			mux0[i-16]=instruction[i];
 		}
 		
 		// Initialize mux input 1 as bits 0-4
 		for(int i=0;i<5;i++) {
 			mux1[i]=instruction[i];
 		}
 		
 		registers.setRead1Reg(reg1);
 		registers.setWriteRegNum(dest);
 		muxRegRead2.setInput0(mux0);
 		muxRegRead2.setInput1(mux1);
 		
 		
 		// Set Read Register 2 (multiplexor output) conditional upon operation code
 		// Multiplexor Output is 0 if operation is AND, ORR, SUB, ADD --> 1 otherwise
     	if (Arrays.equals(opCode, add)||Arrays.equals(opCode, sub)||Arrays.equals(opCode, and)||Arrays.equals(opCode, orr)) {
     		registers.setRead2Reg(muxRegRead2.output(false));
     	} else if(Arrays.equals(opCode,ldr)||Arrays.equals(opCode,str)) {
     		registers.setRead2Reg(muxRegRead2.output(true));
     	} else if (Arrays.equals(opCodeCBZ, CBZ)) {
     		registers.setRead2Reg(muxRegRead2.output(true));
     	} else if(Arrays.equals(opCode,hlt)) {
    		return false;
    	} else {
    		throw new IllegalArgumentException("ERROR: The CPU is force quiting now");
    		// System.out.println("Unknown argument was presented to the CPU.");
    	}
     	
     	
     	/*
     	 * Set Read data addresses, and immediate values
     	 */
     	
     	boolean[] readData1 = registers.getReadReg1();
     	boolean[] readData2 = registers.getReadReg2();
     	
     	// Initialize immediate bits from instruction according to opCode 
     	boolean[] immediate = {false, false};
     	// Bits 20-12 in case of LDR or STR; Bits 23-5 in case of CBZ
     	if(Arrays.equals(opCode,ldr)||Arrays.equals(opCode,str)) {
     		immediate = new boolean[9];
     		for(int i=12;i<21;i++) {
     			immediate[i-12]=instruction[i];
     		}
     	} else if (Arrays.equals(opCodeCBZ, CBZ)) {
     		immediate = new boolean[19];
     		for(int i=5;i<24;i++) {
     			immediate[i-5]=instruction[i];
     		}
     	} 
     	
     	// Place immediate value into inputB of adderBranch 
     	adderBranch.setInputB(to32(immediate));

     	// Set muxALUB inputs, and then output based on ALUSrc
     	muxALUb.setInput0(readData2);
     	muxALUb.setInput1(to32(immediate));
     	
     	
        return true;
    }


    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Execute Phase
    * Activate the ALU to execute an arithmetic or logic operation, or to calculate
    * a memory address.
    *
    * The branch adder is activated during this phase, and the branch adder
    * result is placed into muxPC input 1.
    *
    * This method must make decisions based on the values of the control lines.
    * This method has no information about the opcode!
    *
    */
    private void execute() {
    	
    	/*
    	 * Code block for handling the adder Branch ALU, program counter, and muxPC
    	 */
    	
    	// Read the pc value into the adderBranch  ALU and activate to perform the operation
    	adderBranch.setInputA(pc);
    	adderBranch.setControl(2);
    	adderBranch.activate();
    	
    	// Set the branch adder result into muxPC input 1
    	muxPC.setInput1(adderBranch.getOutput());
    	// Set adderPC output into input 0
    	muxPC.setInput0(adderPC.getOutput()); 
    	   	
    	
    	
     	/*
     	 * Code block for handling the muxALUb ALU
     	 * 
     	 * Set ALU inputA based on the value in Read Register 1
     	 * 
     	 * Set ALU inputB based on muxALUb output, which is contingent on ALUSrc 
     	 * 
     	 * If ALUSrc is false, then operation is ADD, ORR, SUB, AND and mux should be false
     	 * Else if ALUSrc is true, then operation is CBZ, LDR, or STR and mux should be true
     	 */
    	
    	alu.setInputA(registers.getReadReg1()); 
    	
     	if (control.ALUSrc == false) {
     		alu.setInputB(muxALUb.output(false));
     	} else if (control.ALUSrc == true) {
     		alu.setInputB(muxALUb.output(true));
     	} 
     	
     	alu.setControl(control.ALUControl);
     	alu.activate();  // Perform operation once inputs are in 
     	
     	// Set ALU result into input0 of muxRegWriteData
     	muxRegWriteData.setInput0(alu.getOutput());
     	
    }

    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Memory Access Phase
    * Read or write from/to data memory.
    *
    * This method must make decisions based on the values of the control lines.
    * This method has no information about the opcode!
    */
    private void memoryAccess() {
    	// TO DO: deal with "Address" and "Write data"
    	
    	// Read data from memory into muxReadWriteData input
    	// muxRegWriteData.setInput1(dataMemory.read)
    	
       /*  
        * writing the data into data memory then setting the muxRegWriteData to 1
    	* to that data written in
    	*  
    	*  if MemtoReg is true then the muxRegWrite multiplexer is true
        */
    	
    	//made the setInput1 to muxALUb.output(false) because inside that is the readData2
    	
    	muxRegWriteData.setInput1(muxALUb.output(false));
    	
    	if(control.MemtoReg==false) {
    		muxRegWriteData.output(false);
    	}else if (control.MemtoReg=true) {
    		muxRegWriteData.output(true);
    	}
    	
    	
    	
    }
    
    /**
    * STUDENT MUST COMPLETE THIS METHOD
    *
    * Write Back Phase
    * Perform writes to registers: the PC and the processor registers.
    *
    * This method must make decisions based on the values of the control lines.
    * This method has no information about the opcode!
    */
    private void writeBack() {
    	
    	/*
    	 * writing back to write data
    	 */
    	
    	if(control.MemtoReg==false) {
    		registers.setWriteRegData(muxRegWriteData.output(false));
    	}else if (control.MemtoReg==true) {
    		registers.setWriteRegData(muxRegWriteData.output(true));
    	}
    	
    	registers.activateWrite();
    	
    	/*
    	 * write back to pc inside the if statement is (if CBZ and branch are true) OR (uncondbranch is true)
    	 * then set muxPC output to true 
    	 * 
    	 * Note for Nick: since CBZ control.branch is true do we need the second control.branch before the or statement?
    	 */
    	
    	if(((control.Reg2Loc== true & control.ALUSrc== false & control.RegWrite== false & 
    	   control.MemWrite== false & control.MemRead== false & control.ALUControl== 2 &
		   control.MemtoReg== false & control.Uncondbranch== false //look back to see if correct
		   & control.Branch== true) & control.Branch==true) || control.Uncondbranch==true) {
    		pc=muxPC.output(true);
    	}else {
    		pc=muxPC.output(false);
    	}
    	
    }
    
    
    /**
     * Method to turn an n-bit word into a 32 bit word
     * @param arg a boolean array representing a number 
     * @return 32 bit representation
     */
    public boolean[] to32(boolean[] arg) {
    	boolean[] result = new boolean[32];
    	for (int i = 0; i < result.length; i++) {
    		if (i < arg.length) {
    			result[i] = arg[i];
    		} else {
    			result[i] = false;
    		}
    	}
    	
    	
    	return result;
    }
    
}