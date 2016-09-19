package ca.vgorcinschi.ringBuffer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.lang.Integer.valueOf;

/**
 * Created by vgorcinschi on 18/09/16.
 */
public class AppRunner {
    public static void main(String[] args) {

        //initialize a buffer of 10 elements
        RingBuffer<Integer> buffer = new RingBuffer(10);

        //asynchronously push 20 elements
        CompletableFuture<Void> addSome = CompletableFuture.runAsync(()->{
            for (int i = 0; i < 20; i++) {
                buffer.add(valueOf(i));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //in the same time pull 15 elements
        for (int i = 0; i < 15; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Optional<Integer> opt = buffer.take();
            System.out.println(opt.isPresent() ? "Retrieved: "+opt.get() : "Empty buffer");
        }
    }
}
