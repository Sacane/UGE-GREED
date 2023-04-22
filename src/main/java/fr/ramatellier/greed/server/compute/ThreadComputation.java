package fr.ramatellier.greed.server.compute;

import java.util.concurrent.LinkedBlockingQueue;

public final class ThreadComputation {
    private final LinkedBlockingQueue<TaskComputation> task;
    private final LinkedBlockingQueue<ResponseTaskComputation> responses;

    public ThreadComputation(int nbThread) {
        task = new LinkedBlockingQueue<>();
        responses = new LinkedBlockingQueue<>();

        for(var i = 0; i < nbThread; i++) {
            Thread.ofPlatform().daemon().start(() -> {
                for(;;) {
                    try {
                        var computation = task.take();

                        try {
                            var response = computation.checker().check(computation.value());

                            responses.put(new ResponseTaskComputation(computation.packet(), computation.id(), computation.value(), response, (byte) 0x00));
                        } catch(Exception e) {
                            responses.put(new ResponseTaskComputation(computation.packet(), computation.id(), computation.value(), "null", (byte) 0x01));
                        }
                    } catch (InterruptedException e) {
                        // Ignore exception
                    }
                }
            });
        }
    }

    public void putTask(TaskComputation computation) throws InterruptedException {
        task.put(computation);
    }

    public ResponseTaskComputation takeResponse() throws InterruptedException {
        return responses.take();
    }
}
