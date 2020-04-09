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

        // Example Test: Verify that when cycleCount is 0 the instruction returned by fetch is the
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
            if (cycleCount == 1) {
            	if (control.ALUControl != 16) {
            		System.out.println("Cycle 1: Incorrect ALU control. ALU Control was supposed to be 16 but is: " + Integer.toString(control.ALUControl));
            	} else {
            		System.out.println("Correct ALU Control of 16 at Cycle 1.");
            	}
            }
            
            
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
    	
    	// OpCode representations
    	boolean[] add = {false, false, false, true, true, false, true, false, false, false, true};
    	boolean[] sub = {false, false, false, true, true, false, true, false, false, true, true};
    	boolean[] and = {false, false, false, false, true, false, true, false, false, false, true};
    	boolean[] orr = {false, false, false, false, true, false, true, false, true, false, true};
    	
    	boolean[] ldr = {false, true, false, false, false, false, true, true, true, true, true};
    	boolean[] str = {false, false, false, false, false, false, true, true, true, true, true};
    	
    	// Retrieve opCode
    	boolean[] opCode = new boolean[11];
    	for (int i = instruction.length - 11; i < instruction.length; i++) {
    		opCode[i - 21] = instruction[i];
    	}
    	
    	// Set control lines according to opCodes
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
    	
    	
        // placeholder return so that starter code will build.
        // student must replace with value based on opcode.
        return false;
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
