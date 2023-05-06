package fr.ramatellier.greed.server.compute;

import java.util.concurrent.LinkedBlockingQueue;

public final class ThreadComputationHandler {
    private final LinkedBlockingQueue<TaskComputation> task = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ResponseTaskComputation> responses = new LinkedBlockingQueue<>();

    public ThreadComputationHandler(int nbThread) {

        for(var i = 0; i < nbThread; i++) {
            Thread.ofPlatform().daemon().start(() -> {
                for(;;) {
                    try {
                        var computation = task.take();

                        if(computation.checker() == null) {
                            responses.put(new ResponseTaskComputation(computation.packet(), computation.id(), computation.value(), "Value: " + computation.value() + " -> " + "Cannot get the checker\n", (byte) 0x03));
                        }
                        else {
                            try {
                                var response = computation.checker().check(computation.value());

                                responses.put(new ResponseTaskComputation(computation.packet(), computation.id(), computation.value(), "Value: " + computation.value() + " -> " + response + "\n", (byte) 0x00));
                            } catch(Exception e) {
                                responses.put(new ResponseTaskComputation(computation.packet(), computation.id(), computation.value(), "Value: " + computation.value() + " -> " + "Exception raised\n", (byte) 0x01));
                            }
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
