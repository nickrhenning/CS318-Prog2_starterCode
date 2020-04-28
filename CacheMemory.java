/**
 * Simulates cache memory
 *
 * @author student's name
 */

import java.util.*;
import static java.lang.Math.toIntExact;

public class CacheMemory {

  /** Set to true to print additional messages for debugging purposes */
  private static final boolean DEBUG = false;

  /** The number of bytes read or written by one CPU operation */
  public static final int WORD_SIZE = 4; // 4 bytes = 32 bits

  /** The Main Memory this cache is connected to. */
  private MainMemory mainMemory;

  /** Simulate cache as an array of CacheSet objects. */
  private CacheSet[] cache;

  /**
   * Number of bits used for selecting one byte within a cache line. These are the least significant
   * bits of the memory address.
   */
  private int numByteBits;

  /**
   * Number of bits used for specifying the cache set that a memory adddress belongs to. These are
   * the middle bits of the memory address.
   */
  private int numSetBits;

  /**
   * Number of bits used for specifying the tag associated with the memory address. These are the
   * most significant bits of the memory address.
   */
  private int numTagBits;

  /**
   * Count of the total number of cache requests. This is used for implementing the least recently
   * used replacement algorithm; and for reporting information about the cache simulation.
   */
  private int requestCount;

  /**
   * Count of the number of times a cache request is a hit. This is used for reporting information
   * about the cache simulation.
   */
  private int hitCount;

  /**
   * Track the "cost" of a hit. For each cache hit, record the number of cache lines that are
   * searched in order to determine this is a hit. This data member is an accumulator for the hit
   * cost (each hit will add its cost to this data member). This is used for reporting information
   * about the cache simulation.
   */
  private int hitCost;

  /**
   * Count of the number of cache requests that are performed during the warmUp method. This is used
   * for reporting information about the cache simulation.
   */
  private int warmUpRequests;

  /**
   * DO NOT MODIFY THIS METHOD
   *
   * Constructor creates a CacheMemory object. Note the design rules for valid values of each
   * parameter. The simulated computer reads or writes a unit of one WORD_SIZE.
   *
   * @param m The MainMemory object this cache is connected to.
   * @param size The size of this cache, in Bytes. Must be a multiple of the lineSize.
   * @param lineSize The size of one cache line, in Bytes. Must be a multiple of 4 Bytes.
   * @param linesPerSet The number of lines per set. The number of lines in the cache must be a
   *        multiple of the linesPerSet.
   *
   * @exception IllegalArgumentExcepction if a parameter value violates a design rule.
   */
  public CacheMemory(MainMemory m, int size, int lineSize, int linesPerSet) {

    if (lineSize % WORD_SIZE != 0) {
      throw new IllegalArgumentException("lineSize is not a multiple of " + WORD_SIZE);
    }

    if (size % lineSize != 0) {
      throw new IllegalArgumentException("size is not a multiple of lineSize.");
    }

    // number of lines in the cache
    int numLines = size / lineSize;

    if (numLines % linesPerSet != 0) {
      throw new IllegalArgumentException("number of lines is not a multiple of linesPerSet.");
    }

    // number of sets in the cache
    int numSets = numLines / linesPerSet;

    // Set the main memory
    mainMemory = m;

    // Initialize the counters to zero
    requestCount = 0;
    warmUpRequests = 0;
    hitCount = 0;
    hitCost = 0;

    // Determine the number of bits required for the byte within a line,
    // for the set, and for the tag.
    int value;
    numByteBits = 0; // initialize to zero
    value = 1; // initialize to 2^0
    while (value < lineSize) {
      numByteBits++;
      value *= 2; // increase value by a power of 2
    }

    numSetBits = 0;
    value = 1;
    while (value < numSets) {
      numSetBits++;
      value *= 2;
    }

    // numTagBits is the remaining memory address bits
    numTagBits = 32 - numSetBits - numByteBits;

    System.out.println("CacheMemory constructor:");
    System.out.println("    numLines = " + numLines);
    System.out.println("    numSets = " + numSets);
    System.out.println("    numByteBits = " + numByteBits);
    System.out.println("    numSetBits = " + numSetBits);
    System.out.println("    numTagBits = " + numTagBits);
    System.out.println();

    // Create the array of CacheSet objects and initialize each CacheSet object
    cache = new CacheSet[numSets];
    for (int i = 0; i < cache.length; i++) {
      cache[i] = new CacheSet(lineSize, linesPerSet, numTagBits);
    }
  } // end of constructor

  /**
   * DO NOT MODIFY THIS METHOD
   *
   * "Warm Up" the cache by reading random memory addresses. This method is used by programs that do
   * not want to run on a "cold" cache. The cache performance statistics do not include requests
   * from this warm up phase.
   *
   * @param numReads The number of warm-up read operations to perform.
   * @param random A random number generator object.
   */
  public void warmUp(int numReads, Random random) {
    int wordsInMainMem = (mainMemory.getSize() / WORD_SIZE);

    for (int i = 0; i < numReads; i++) {
      // Generate a random line address
      int wordAddress = random.nextInt(wordsInMainMem) * WORD_SIZE;
      boolean[] address = Binary.uDecToBin(wordAddress, 32);
      readWord(address, false);
    }
    warmUpRequests = requestCount;
  }

  /**
   * DO NOT MODIFY THIS METHOD
   *
   * Prints the total number of requests and the number of requests that resulted in a cache hit.
   */
  public void reportStats() {
    System.out.println("Number of requests: " + (requestCount - warmUpRequests));
    System.out.println("Number of hits: " + hitCount);
    System.out.println("hit ratio: " + (double) hitCount / (requestCount - warmUpRequests));
    System.out.println("Average hit cost: " + (double) hitCost / hitCount);
  }

  /**
   * DO NOT MODIFY THIS METHOD
   *
   * Returns the word that begins at the specified memory address.
   *
   * This is the public version of readWord. It calls the private version of readWord with the
   * recordStats parameter set to true so that the cache statistics information will be recorded.
   *
   * @param address The byte address where the 32-bit value begins.
   * @return The word read from memory. Index 0 of the array holds the least significant bit of the
   *         binary value.
   *
   */
  public boolean[] readWord(boolean[] address) {
    return readWord(address, true);
  }

  /**
   * STUDENT MUST COMPLETE THIS METHOD
   *
   * Returns the word that begins at the specified memory address.
   *
   * This is the private version of readWord that includes the cache statistic tracking parameter.
   * When recordStats is false, this method should not update the cache statistics data members
   * (hitCount and hitCost).
   *
   * @param address The byte address where the 32-bit value begins.
   * @param recordStats Set to true if cache statistics tracking data members (hitCount and hitCost)
   *        should be updated.
   * @return The word read from memory. Index 0 of the array holds the least significant bit of the
   *         binary value.
   * @exception IllegalArgumentExcepction if the address is not valid.
   */
  private boolean[] readWord(boolean[] address, boolean recordStats) {
    if (address.length > 32) {
      throw new IllegalArgumentException("address parameter must be 32 bits");
    }
    // Programming Assignment 5: Complete this method
    // The comments provide a guide for this method.


    /*
     * Where does the address map in the cache? --> Determine the cache set that corresponds with
     * the requested memory address
     */

    // Get set bits from the address that was passed in
    boolean[] setBits = new boolean[numSetBits];
    for (int i = numByteBits; i < numByteBits + numSetBits; i++) {
      setBits[i - numByteBits] = address[i];
      System.out.println("setBits[" + Integer.toString(i - numByteBits) + "] is "
          + String.valueOf(setBits[i - numByteBits]));
    }

    long cacheSet = Binary.binToUDec(setBits);
    System.out.println("Cache set is " + Long.toString(cacheSet));

    boolean[] tagBits = new boolean[numTagBits];
    // Get tag from the address that was passed in
    for (int i = numByteBits + numSetBits; i < numByteBits + numSetBits + numTagBits; i++) {
      tagBits[i - numByteBits - numSetBits] = address[i];
      System.out.println("tagbits[" + Integer.toString(i - numByteBits - numSetBits) + "] is "
          + String.valueOf(tagBits[i - numByteBits - numSetBits]));
    }

    /*
     * Determine whether or not the line that corresponds with the requested memory address is
     * currently in the cache
     */
    int lineCount=0;
    boolean inCache = false;
    // Iterate through the correct Cache Set
    for (int i = 0; i < cache[toIntExact(cacheSet)].size(); i++) {
      System.out.println("\n Line " + Integer.toString(i) + ":");
      lineCount++;
      // Get the tag the line
      boolean[] lineTag = cache[toIntExact(cacheSet)].getLine(i).getTag();
      // If the tag from the line equals the tag from the parameter, then the line that corresponds
      // to the memory address is currently in the cache
      if (Arrays.equals(lineTag, tagBits)) {
        System.out.println("Line sorresponding to the memory address is currently in the cache");
        inCache = true;
        // System.exit(0);
      }
    }
    //if it is in the cache the hit count increases
    
    // If the line corresponding to the memory address is not in the cache, call readLineFromMemory 
    if (inCache==false) {
      readLineFromMemory(address, toIntExact(cacheSet), tagBits);
    }
    //System.out.println(Binary.toString(toIntExact(cacheSet)));

    System.exit(0);

    // TODO:
    // Update CacheMemory data members (requestCount, hitCount, hitCost)
    // and CacheLine data members (using the various set methods)
    // as needed for tracking cache hit rate and implementing the
    // least recently used replacement algorithm in the cache set.
    if(inCache==false) {
    	hitCount++;
    	hitCost=lineCount-1;
    }
    requestCount++;
    
    
    // replace this placeholder return with the data copied from the cache line
    return new boolean[32];
  }


  /**
   * STUDENT MUST COMPLETE THIS METHOD
   *
   * Copies a line of data from memory into cache. Selects the cache line to replace. Uses the Least
   * Recently Used (LRU) algorithm when a choice must be made between multiple cache lines that
   * could be replaced.
   *
   * @param address The requested memory address.
   * @param setNum The set number where the address maps to.
   * @param tagBits The tag bits from the memory address.
   *
   * @return The line that was read from memory. This line is also written into the cache.
   */
  private CacheLine readLineFromMemory(boolean[] address, int setNum, boolean[] tagBits) {

    // Use the LRU (least recently used) replacement scheme to select a line
    // within the set.

    // Read the line from memory. The memory address to read is the
    // first memory address of the line that contains the requested address.
    // The MainMemory read method should be called.

    // Copy the line read from memory into the cache

    // replace this placeholder return with the correct line to return
	
	/*COMMENT FOR NICK: The least recently used I don't now how to change. The code below
	 * sets the first line in the cache set to the LRU but I don't know how to update it 
	 * to the next line over once address is put into the LRU line 
	 */
	  
	  int leastRecent = -1;
	  int index = 0;
	  boolean hasEmpty = false;
	  
	  for(int i=0;i<cache[setNum].size(); i++) {
		  if(!cache[setNum].getLine(i).isValid()) {
			  hasEmpty = true;
			  index = i;
		  }
	  }
	  
	  if(!hasEmpty) {
		  for(int i=0; i<cache[setNum].size(); i++) {
			  if(leastRecent < cache[setNum].getLine(i).getLastUsed()) {
				  leastRecent = i;
			  }
		  }
	  }
	  boolean [][] temp = mainMemory.read(address,numByteBits);
	  System.out.println("Temp Length: "+Integer.toString(temp[0].length));
	  System.out.println("Memory Length: "+ Integer.toString(mainMemory.read(address,numByteBits)[0].length));
	  cache[setNum].getLine(index).setData(temp);
	  
	  
	  
	  
	  return cache[setNum].getLine(index);

  }

}