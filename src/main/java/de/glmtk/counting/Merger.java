package de.glmtk.counting;

import static de.glmtk.Config.CONFIG;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.glmtk.Status;
import de.glmtk.common.Pattern;
import de.glmtk.util.NioUtils;

public class Merger {

    private static final long B = 1L;

    private static final long KB = 1024 * B;

    private static final long MB = 1024 * KB;

    private static final int AVAILABLE_MEMORY_PERCENT = 40;

    private static final int NUM_PARALLEL_READERS = 10;

    private static final Logger LOGGER = LogManager.getLogger(Merger.class);

    private boolean continuation;

    /* package */Merger(
            boolean continuation) {
        this.continuation = continuation;
    }

    public void merge(
            Status status,
            Set<Pattern> patterns,
            Path chunkedDir,
            Path countedDir) throws IOException {
        if (patterns.isEmpty()) {
            LOGGER.debug("No chunks to merge, returning.");
            return;
        }
        LOGGER.debug("patterns = {}", patterns);
        Files.createDirectories(countedDir);

        // Calculate Memory ////////////////////////////////////////////////////
        LOGGER.debug("Calculating Memory...");
        Runtime r = Runtime.getRuntime();
        r.gc();

        long totalFreeMemory = r.maxMemory() - r.totalMemory() + r.freeMemory();
        long availableMemory =
                (AVAILABLE_MEMORY_PERCENT * totalFreeMemory) / 100;
        long readerMemory = availableMemory / CONFIG.getNumberOfCores() / 2;
        long writerMemory = availableMemory / CONFIG.getNumberOfCores() / 2;

        LOGGER.debug("totalFreeMemory = {}MB", totalFreeMemory / MB);
        LOGGER.debug("availableMemory = {}MB", availableMemory / MB);
        LOGGER.debug("readerMemory    = {}MB", readerMemory / MB);
        LOGGER.debug("writerMemory    = {}MB", writerMemory / MB);

        // Prepare Threads /////////////////////////////////////////////////////
        LOGGER.debug("Preparing Threads...");
        BlockingQueue<Pattern> queue =
                new LinkedBlockingDeque<Pattern>(patterns);
        List<MergerThread> mergerThreads = new LinkedList<MergerThread>();
        for (int i = 0; i != CONFIG.getNumberOfCores(); ++i) {
            mergerThreads.add(new MergerThread(this, status, queue, chunkedDir,
                    countedDir, readerMemory, writerMemory,
                    NUM_PARALLEL_READERS, continuation));
        }

        // Launch Threads //////////////////////////////////////////////////////
        LOGGER.debug("Launching Threads...");
        try {
            ExecutorService executorService =
                    Executors.newFixedThreadPool(CONFIG.getNumberOfCores());

            for (MergerThread mergerThread : mergerThreads) {
                executorService.execute(mergerThread);
            }

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }

        if (NioUtils.isDirEmpty(chunkedDir)) {
            Files.deleteIfExists(chunkedDir);
        }
    }
}