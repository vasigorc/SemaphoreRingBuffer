package ca.vgorcinschi.ringBuffer;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * Created by vgorcinschi on 18/09/16.
 */
public class RingBuffer<Item> {

    //the actual Ring Buffer
    private final Item[] elements;
    private final Semaphore addSemaphore = new Semaphore(1, true);
    private final Semaphore takeSemaphore = new Semaphore(1, true);

    //the write pointer, represented as an offset into the array
    private int offset = 0;

    /*The read pointer is encoded implicitly by keeping track of the number
    * of unconsumed elements. We can then determine its position by backing up that many
    * positions before the read position
    */
    private int unconsumedElements = 0;

    /**
     * Constructs a new RingBuffer with the specified capacity, which must be
     * positive.
     *
     * @param size The capacity of the new ring buffer.
     * @throws IllegalArgumentException If the capacity is negative
     */
    @SuppressWarnings("uncheked")
    public RingBuffer(int size){
        //Validate the size
        if (size <= 0)
            throw new IllegalArgumentException("RingBuffer capacity must be positive.");

        //Constructs the array to be that size.
        elements = (Item[]) new Object[size];
        /**
         * we need to acquire the take semaphore from the start, otherwise
         * we will end up pulling nulls
         */
        try {
            takeSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends an element to the ring buffer, blocking until space becomes
     * available.
     *
     * @param elem The element to add to the ring buffer
     */
    public void add(Item elem){
        /*
            Block until the capacity is non-zero. Otherwise we don't have any space to write.
         */
        while (addSemaphore.availablePermits()!=1){
            //Waiting for the 'Add semaphore' to be released
        }
            /*
                Write the element into the next open spot, then advance the write
                pointer forward a step.
             */
            elements[offset] = elem;
            offset = (offset +1) % elements.length;
            increaseBuffer();
        System.out.println("Added to buffer: "+elem);
    }

    /**
     * Returns the maximum capacity of the ring buffer.
     *
     * @return The maximum capacity of the ring buffer.
     */
    public int capacity(){
        return elements.length;
    }

    /**
     * Observes but does not dequeue the next available element, blocking until data becomes available
     *
     * @return The next available element.
     */
    public Optional<Item> peek(){
        if (takeSemaphore.availablePermits()==1){
            return Optional.of(elements[(offset + (capacity() - unconsumedElements)) % capacity()]);
        }
        return Optional.empty();
    }

    /**
     * Removes and returns the next available element, blocking until data
     * becomes available.
     *
     * @return The next available element
     */
    public Optional<Item> take(){
        Optional<Item> result = peek();
        if (result.isPresent()){}
            decreaseBuffer();
        return result;
    }

    public int size(){
        return unconsumedElements;
    }

    public boolean isEmpty(){
        return size()==0;
    }

    public void increaseBuffer(){
        ++unconsumedElements;
        if (unconsumedElements == elements.length){
            try {
                addSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (takeSemaphore.availablePermits()==0)
            takeSemaphore.release();
    }

    public void decreaseBuffer(){
        --unconsumedElements;
        if (addSemaphore.availablePermits()==0)
            addSemaphore.release();

        if (unconsumedElements == 0)
            try {
                takeSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
