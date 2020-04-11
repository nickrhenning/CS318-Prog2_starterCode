/**
* Represents a simple CPU based on the ARMv8 datapath.
*
* CS318 Programming Assignment 4
* Name:
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

        System.out.println("CPU halt after " + cycleCount + " cycles.");
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
        System.out.println("CYCLE COUNT:   "+cycleCount);
        // Example Test: Verify that when cycleCount is 0 the control signals
        // are correctly set for an ADD instruction
        if(cycleCount == 0 && (control.Uncondbranch != false || control.RegWrite != true
            || control.Reg2Loc != false || control.MemWrite != false || control.MemtoReg != false
            || control.MemRead != false || control.Branch != false || control.ALUSrc != false
            || control.ALUControl != 2))
        {
        	System.out.println("cycleCount:   "+cycleCount);
        	System.out.println("control UncondBranch:   "+control.Uncondbranch);
        	System.out.println("RegWrite:   "+control.RegWrite);
        	System.out.println("Reg2Loc:   "+control.Reg2Loc);
        	System.out.println("MemWrite:   "+control.MemWrite);
        	System.out.println("MemtoReg:   "+control.MemtoReg);
        	System.out.println("MemRead:   "+control.MemRead);
        	System.out.println("Branch:   "+control.Branch);
        	System.out.println("ALUSrc:   "+control.ALUSrc);
        	System.out.println("ALUControl:   "+control.ALUControl);
           
        	System.out.println("FAIL: cycle " + cycleCount + " after decode, control lines incorrect");
        }

        // Loop until a halt instruction is decoded
        while(op) {
            execute();

            // Example Test: Verify that when cycleCount is 0 the ALU result is zero
            boolean[] correctALU = Binary.uDecToBin(0L, 32);
            if(cycleCount == 0 && !Arrays.equals(alu.getOutput(), correctALU)) {
                System.out.println("FAIL: cycle " + cycleCount + " incorrect ALU result:");
                System.out.println("------ ALU result: " + Binary.toString(alu.getOutput()));
                System.out.println("------ correct result: " + Binary.toString(correctALU));
            }

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the ALU result is the correct
            // data memory address (should be 16)

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 6, the branch adder's (adderBranch)
            // result is the offset of the branch destination instruction (should be 32)


            memoryAccess();

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the value that was read from
            // memory (should be 6) is in the register write multiplexor
            // (muxRegWriteData) at input 1

            writeBack();

            cycleCount++;

            // Start the next cycle
            instruction = fetch();

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 7, the instruction returned by fetch is
            // the last instruction in the program: STR R5,[R9,#8]


            op = decode(instruction);

            // ***** PROG. 4 STUDENT MUST ADD
            // Test that when cycleCount is 1, the the control signals are correctly
            // set for a LDR instruction
            
            if(cycleCount!= 1 && (control.Uncondbranch != false || control.RegWrite != true
                    || control.Reg2Loc != false || control.MemWrite != false || control.MemtoReg != false
                    || control.MemRead != true || control.Branch != false || control.ALUSrc != true
                    || control.ALUControl != 2))
                {
            	System.out.println("FAIL: LDR cycle " + cycleCount + " after decode, control lines incorrect");
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

        // placeholder return so that starter code will build.
        // student must replace with the instruction that is fetched.
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
    	System.out.println("Begin decode: program counter is: " + Long.toString(Binary.binToUDec(pc)));
    	
    	/*
    	 * Initialize opcodes and set control values 
    	 */
        boolean add[]= {false, false, false, true, true, false, true, false, false, false, true};
        boolean[] sub = {false, false, false, true, true, false, true, false, false, true, true};
    	boolean[] and = {false, false, false, false, true, false, true, false, false, false, true};
    	boolean[] orr = {false, false, false, false, true, false, true, false, true, false, true};
    	
    	boolean[] ldr = {false, true, false, false, false, false, true, true, true, true, true};
    	boolean[] str = {false, false, false, false, false, false, true, true, true, true, true};
    	boolean[] hlt= {true, true, false, true, false, true, false, false, false, true, false}; 
    	boolean[] opCode = new boolean[11];
    	for (int i = instruction.length - 11; i < instruction.length; i++) {
    		opCode[i - 21] = instruction[i];
    	}
    	
     	if (Arrays.equals(opCode, add) || Arrays.equals(opCode, sub) || 
    			Arrays.equals(opCode, and) || Arrays.equals(opCode, orr)) {
    		control.Reg2Loc = false;
    		control.ALUSrc = false;
    		control.RegWrite = true;
    		control.MemWrite = false;
    		control.MemRead = false;
    		control.ALUControl = 2;
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
     	boolean[] opCodeCBZ = new boolean[8];
     	boolean CBZ []= {true, false, true, true, false, true, false, false};
     	for (int i = instruction.length - 8; i < instruction.length; i++) {
    		opCodeCBZ[i - 24] = instruction[i];
    	}
     	if(Arrays.equals(opCodeCBZ,CBZ )) {
     		control.Reg2Loc = true;
    		control.ALUSrc = false;
    		control.RegWrite = false;
    		control.MemWrite = false;
    		control.MemRead = false;
    		control.ALUControl = 2;
    		control.MemtoReg = false;
    		control.Uncondbranch = false; //look back to see if correct
    		control.Branch = true;
     	}
     	boolean[] opCodeB = new boolean[6];
     	boolean B[]= {false, false, false, true, false, true};
     	for(int i=instruction.length-6;i<instruction.length; i++) {
     		opCodeB[i-26]=instruction[i];
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
 			System.out.print(String.valueOf(reg1[i-5]));
 		}
 		
 		// Initialize destination register as bits 0-4
 		for(int i=0;i<5;i++) {
 			dest[i]=instruction[i];
 			System.out.print(String.valueOf(reg1[i]));
 		}
 		
 		// Initialize mux input 0 as bits 20-16
 		for(int i=16;i<21;i++) {
 			mux0[i-16]=instruction[i];
 			System.out.print(String.valueOf(mux0[i-16]));
 		}
 		
 		// Initialize mux input 1 as bits 0-4
 		for(int i=0;i<5;i++) {
 			mux1[i]=instruction[i];
 			System.out.print(String.valueOf(mux1[i]));
 		}
 		
 		// Set Read Register 2 (multiplexor output) conditional upon operation code
 		// Multiplexor Output is 0 if operation is AND, ORR, SUB, ADD --> 1 otherwise
     	if (Arrays.equals(opCode, add)||Arrays.equals(opCode, sub)||Arrays.equals(opCode, and)||Arrays.equals(opCode, orr)) {
     		register.setRead2Reg(muxRegRead2.output(0));
     	} else if(Arrays.equals(opCode,ldr)||Arrays.equals(opCode,str)) {
     		register.setRead2Reg(muxRegRead2.output(1));
     	} else if (Arrays.equals(opCodeCBZ, CBZ)) {
     		register.setRead2Reg(muxRegRead2.output(1));
     	} else if(Arrays.equals(opCode,hlt)) {
    		System.out.println("op code was hlt");
    		return false;
    	} else {
    		throw new IllegalArgumentException("ERROR: Unknown opcode  was presented to the CPU.");
    	}
    	
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
    	
    	adderBranch.activate();
    	//If addition, subtraction, and, or or occures
    	if(control.Reg2Loc== false & control.ALUSrc== false & control.RegWrite== true & 
    			control.MemWrite== false & control.MemRead== false & control.ALUControl== 2 & 
    			control.MemtoReg== false & control.Uncondbranch== false & control.Branch== false) {
    		alu.activate();
    		alu.setInputA(registers.getReadReg1());
    		alu.setInputB(registers.getReadReg2());
    		System.out.println(registers.getReadReg1());
    			
    	}
    	//If LDR
    	if(control.Reg2Loc== false & control.ALUSrc== true & control.RegWrite== true & 
    			control.MemWrite== false & control.MemRead== true & control.ALUControl== 2 & 
    			control.MemtoReg== false & control.Uncondbranch== false & control.Branch== false) {
    		alu.activate();
    		alu.setInputA(registers.getReadReg1());
    		alu.setInputB(registers.getReadReg2());
    	}
    	//If STR
    	if(control.Reg2Loc== true & control.ALUSrc== true & control.RegWrite== false & 
    			control.MemWrite== true & control.MemRead== false & control.ALUControl== 2 & 
    			control.MemtoReg== false & control.Uncondbranch== false & control.Branch== false) {
    		alu.activate();
    		alu.setInputA(registers.getReadReg1());
    	//	alu.setInputB(muxRegRead2.getInput1());
    	}
    	
    	
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

    }
}