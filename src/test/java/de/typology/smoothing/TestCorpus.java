package de.typology.smoothing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.typology.counting.AbsoluteCounter;
import de.typology.counting.ContinuationCounter;
import de.typology.indexing.Index;
import de.typology.indexing.IndexBuilder;
import de.typology.patterns.Pattern;
import de.typology.patterns.PatternElem;
import de.typology.sequencing.Sequencer;
import de.typology.utils.StringUtils;

public abstract class TestCorpus {

    protected static Path resourcesDir = Paths.get("src/test/resources");

    protected Path workingDir;

    protected Index index;

    protected Path absoluteDir;

    protected Path continuationDir;

    public TestCorpus(
            Path trainingFile,
            Path workingDir) throws IOException, InterruptedException {
        if (!Files.exists(workingDir)) {
            Files.createDirectory(workingDir);
        }

        Path indexFile = workingDir.resolve("index.txt");
        Path sequencesDir = workingDir.resolve("sequences");
        absoluteDir = workingDir.resolve("absolute");
        continuationDir = workingDir.resolve("continuation");

        // index
        if (!Files.exists(indexFile)) {
            try (InputStream input = Files.newInputStream(trainingFile);
                    OutputStream output = Files.newOutputStream(indexFile)) {
                IndexBuilder indexBuilder = new IndexBuilder(false, false, 5);
                indexBuilder.buildIndex(input, output, 1, 1);
            }
        }
        try (InputStream input = Files.newInputStream(indexFile)) {
            index = new Index(input);
        }

        // sequences
        if (!Files.exists(sequencesDir)) {
            Sequencer sequencer =
                    new Sequencer(trainingFile, sequencesDir, index, 1, false,
                            false);
            sequencer.sequence(Pattern.getCombinations(5, new PatternElem[] {
                PatternElem.CNT, PatternElem.SKP
            }));
        }

        // absolute
        if (!Files.exists(absoluteDir)) {
            AbsoluteCounter absoluteCounter =
                    new AbsoluteCounter(sequencesDir, absoluteDir, "\t", 1,
                            false, true);
            absoluteCounter.count();
        }

        // continuation
        if (!Files.exists(continuationDir)) {
            ContinuationCounter continuationCounter =
                    new ContinuationCounter(absoluteDir, continuationDir,
                            index, "\t", 1, false, true);
            continuationCounter.count();
        }
    }

    public abstract String[] getWords();

    public String getSequence(int n, int length) {
        List<String> result = new LinkedList<String>();
        for (int k = 0; k != length; ++k) {
            result.add(getWords()[n % getWords().length]);
            n /= getWords().length;
        }
        Collections.reverse(result);
        return StringUtils.join(result, " ");
    }

    public Path getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(Path workingDir) {
        this.workingDir = workingDir;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Path getAbsoluteDir() {
        return absoluteDir;
    }

    public void setAbsoluteDir(Path absoluteDir) {
        this.absoluteDir = absoluteDir;
    }

    public Path getContinuationDir() {
        return continuationDir;
    }

    public void setContinuationDir(Path continuationDir) {
        this.continuationDir = continuationDir;
    }

}