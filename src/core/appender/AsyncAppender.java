package core.appender;

import core.LogEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncAppender implements Appender {

    private final Appender delegate;
    private final BlockingQueue<LogEvent> queue;
    private final Thread worker;

    private volatile boolean running = true;

    public AsyncAppender(Appender delegate, int queueSize) {
        this.delegate = delegate;
        this.queue = new LinkedBlockingQueue<>(queueSize);

        this.worker = Thread.ofVirtual()
                .name("async-log-worker")
                .start(this::processLogs);
    }

    @Override
    public void append(LogEvent event) {

        // Try asynchronous logging first
        if (!queue.offer(event)) {

            // Queue full → synchronous fallback
            delegate.append(event);
        }
    }

    private void processLogs() {

        while (running || !queue.isEmpty()) {

            try {
                LogEvent event = queue.take();
                delegate.append(event);

            } catch (InterruptedException e) {

                if (!running) {
                    // Shutdown requested.
                    // Loop condition will decide whether
                    // there are remaining events to process.
                    continue;
                }

                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void close() {

        running = false;

        // Wake up worker if it is waiting on queue.take()
        worker.interrupt();

        try {
            // Wait for remaining logs to be processed
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        delegate.close();
    }
}