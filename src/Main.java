import api.Logger;
import api.LoggerFactory;
import config.LoggerConfig;
import core.LogManager;
import core.appender.AsyncAppender;
import core.appender.ConsoleAppender;
import core.appender.FileAppender;
import core.filter.LevelFilter;
import core.formatter.JsonFormatter;
import core.formatter.PatternFormatter;
import core.rotation.RotationManager;
import core.rotation.SizeRotationStrategy;
import core.rotation.TimeRotationStrategy;
import level.LogLevel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final Logger log =
            LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        /*
         * ============================================================
         * 1. ROTATION CONFIGURATION
         * ============================================================
         *
         * Rotate when:
         *
         *     Size OR Time condition is satisfied.
         *
         * Size = 10 KB
         * Time = 30 seconds
         */
        RotationManager rotationManager = new RotationManager(
                List.of(
                        new SizeRotationStrategy(10 * 1024),
                        new TimeRotationStrategy(30_000)
                )
        );

        /*
         * ============================================================
         * 2. LOGGER CONFIGURATION
         * ============================================================
         */

        LoggerConfig config = LoggerConfig.builder()
                .setLogLevel(LogLevel.INFO)

                /*
                 * Console:
                 *
                 * Only ERROR and above
                 */
                .addAppender(
                        new ConsoleAppender(
                                new PatternFormatter(),
                                List.of(
                                        new LevelFilter(LogLevel.ERROR)
                                )
                        )
                )

                /*
                 * Normal File:
                 *
                 * INFO and above
                 *
                 * JSON format
                 */
                .addAppender(
                        new FileAppender(
                                "application.log",
                                new JsonFormatter(),
                                List.of(
                                        new LevelFilter(LogLevel.INFO)
                                ),
                                rotationManager,
                                3
                        )
                )

                /*
                 * Async File:
                 *
                 * INFO and above
                 *
                 * Pattern format
                 *
                 * Queue size = 10,000
                 */
                .addAppender(
                        new AsyncAppender(
                                new FileAppender(
                                        "async.log",
                                        new PatternFormatter(),
                                        List.of(
                                                new LevelFilter(LogLevel.INFO)
                                        ),
                                        rotationManager,
                                        3
                                ),
                                10_000
                        )
                )

                .build();

        /*
         * ============================================================
         * 3. SET GLOBAL CONFIGURATION
         * ============================================================
         */

        LogManager logManager =
                LogManager.getInstance();

        logManager.setLoggerConfig(config);


        /*
         * ============================================================
         * 4. BASIC LOGGING
         * ============================================================
         */

        log.info("Application started");

        log.info(
                "User %s logged in",
                "Anil"
        );

        log.info(
                "User %s has %d points",
                "Anil",
                1500
        );

        log.info(
                "Transaction amount = %.2f",
                1250.50
        );


        /*
         * ============================================================
         * 5. DIFFERENT LOG LEVELS
         * ============================================================
         */

        log.trace("This is TRACE");

        log.debug("This is DEBUG");

        log.info("This is INFO");

        log.warn("This is WARNING");

        log.error("This is ERROR");

        log.fatal("This is FATAL");


        /*
         * ============================================================
         * 6. TEST FILTERING
         * ============================================================
         *
         * Console has:
         *
         *     LevelFilter(ERROR)
         *
         * So only:
         *
         *     ERROR
         *     FATAL
         *
         * should appear in console.
         *
         * File has:
         *
         *     LevelFilter(INFO)
         *
         * So:
         *
         *     INFO
         *     WARN
         *     ERROR
         *     FATAL
         *
         * should go to file.
         */

        log.info("INFO filtering test");

        log.warn("WARN filtering test");

        log.error("ERROR filtering test");

        log.fatal("FATAL filtering test");


        /*
         * ============================================================
         * 7. EXCEPTION LOGGING
         * ============================================================
         */

        try {

            int result = 10 / 0;

        } catch (Exception e) {

            log.error(
                    "Division failed",
                    e
            );
        }


        /*
         * ============================================================
         * 8. EXCEPTION + FORMATTING
         * ============================================================
         */

        try {

            String value = null;

            value.length();

        } catch (Exception e) {

            log.error(
                    "Operation failed for user %s: %s",
                    e,
                    "Anil",
                    e.getMessage()
            );
        }


        /*
         * ============================================================
         * 9. ANOTHER EXCEPTION
         * ============================================================
         */

        try {

            int[] numbers = {1, 2, 3};

            System.out.println(numbers[10]);

        } catch (Exception e) {

            log.error(
                    "Array operation failed",
                    e
            );
        }


        /*
         * ============================================================
         * 10. MULTIPLE LOGGER INSTANCES
         * ============================================================
         */

        Logger userLogger =
                LoggerFactory.getLogger("UserService".getClass());

        Logger paymentLogger =
                LoggerFactory.getLogger("PaymentService".getClass());

        Logger orderLogger =
                LoggerFactory.getLogger("OrderService".getClass());


        userLogger.info(
                "User service started"
        );

        paymentLogger.info(
                "Payment service started"
        );

        orderLogger.info(
                "Order service started"
        );


        /*
         * ============================================================
         * 11. SIMULATE USER OPERATIONS
         * ============================================================
         */

        for (int i = 1; i <= 20; i++) {

            userLogger.info(
                    "Processing user id=%d",
                    i
            );

            if (i % 5 == 0) {

                userLogger.warn(
                        "User id=%d took longer than expected",
                        i
                );
            }
        }


        /*
         * ============================================================
         * 12. SIMULATE PAYMENTS
         * ============================================================
         */

        for (int i = 1; i <= 20; i++) {

            paymentLogger.info(
                    "Processing payment id=%d amount=%.2f",
                    i,
                    i * 250.75
            );

            if (i % 7 == 0) {

                paymentLogger.error(
                        "Payment failed paymentId=%d",
                        i
                );
            }
        }


        /*
         * ============================================================
         * 13. LARGE NUMBER OF LOGS
         * ============================================================
         *
         * This helps test:
         *
         *     SizeRotationStrategy
         *     AsyncAppender
         *     FileAppender
         */

        for (int i = 0; i < 5000; i++) {

            log.info(
                    "Bulk operation id=%d user=%s status=%s",
                    i,
                    "user-" + (i % 100),
                    "SUCCESS"
            );
        }


        /*
         * ============================================================
         * 14. LARGE LOG MESSAGE
         * ============================================================
         *
         * This should make the file reach the size
         * rotation threshold faster.
         */

        String largeMessage = "A".repeat(2000);

        for (int i = 0; i < 20; i++) {

            log.info(
                    "Large message %d: %s",
                    i,
                    largeMessage
            );
        }


        /*
         * ============================================================
         * 15. MULTI-THREADED LOGGING
         * ============================================================
         */

        ExecutorService executor =
                Executors.newFixedThreadPool(5);

        for (int thread = 1; thread <= 5; thread++) {

            int threadId = thread;

            executor.submit(() -> {

                Logger threadLogger =
                        LoggerFactory.getLogger(
                                ("Worker-" + threadId).getClass()
                        );

                for (int i = 0; i < 1000; i++) {

                    threadLogger.info(
                            "Thread=%d processing task=%d",
                            threadId,
                            i
                    );

                    if (i % 200 == 0) {

                        threadLogger.warn(
                                "Thread=%d reached task=%d",
                                threadId,
                                i
                        );
                    }
                }
            });
        }

        executor.shutdown();


        /*
         * ============================================================
         * 16. WAIT FOR THREADS
         * ============================================================
         */

        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }


        /*
         * ============================================================
         * 17. FINAL LOG
         * ============================================================
         */

        log.info("All operations completed");

        log.error("Testing final error message");

        log.fatal("Application shutting down");


        /*
         * ============================================================
         * 18. WAIT FOR ASYNC LOGGER
         * ============================================================
         *
         * Important:
         *
         * AsyncAppender has its own worker thread.
         *
         * Give it some time to process remaining logs.
         */

        Thread.sleep(2000);

        System.out.println(
                "===================================="
        );

        System.out.println(
                "Logging test completed!"
        );

        System.out.println(
                "Check:"
        );

        System.out.println(
                "  application.log"
        );

        System.out.println(
                "  async.log"
        );

        System.out.println(
                "  application.log.1"
        );

        System.out.println(
                "  application.log.2"
        );

        System.out.println(
                "  async.log.1"
        );

        System.out.println(
                "  async.log.2"
        );

        System.out.println(
                "===================================="
        );
    }
}