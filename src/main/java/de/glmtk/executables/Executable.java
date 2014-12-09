package de.glmtk.executables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.glmtk.Config;
import de.glmtk.Termination;
import de.glmtk.utils.LogUtils;
import de.glmtk.utils.StringUtils;

/* package */abstract class Executable {

    private static Logger LOGGER = LogManager.getLogger(Executable.class);

    protected static final String OPTION_HELP_SHORT = "h";

    protected static final String OPTION_HELP_LONG = "help";

    protected static final String OPTION_VERSION_SHORT = "v";

    protected static final String OPTION_VERSION_LONG = "version";

    protected Config config = null;

    protected CommandLine line = null;;

    protected abstract List<Option> getOptions();

    protected abstract String getUsage();

    protected abstract void exec() throws Exception;

    public void run(String[] args) {
        try {
            config = Config.get();

            parseArguments(args);

            configureLogging();

            printLogHeader(args);
            exec();
            printLogFooter();
        } catch (Termination e) {
            if (e.getMessage() != null) {
                System.err.println(e.getMessage());
            }
            // Terminate
        } catch (Exception e) {
            try (StringWriter stackTrace = new StringWriter();
                    PrintWriter stackTraceWriter = new PrintWriter(stackTrace)) {
                e.printStackTrace(stackTraceWriter);
                LOGGER.error("Exception " + stackTrace.toString());
            } catch (IOException ee) {
            }
        }
    }

    protected void configureLogging() {
        LogUtils.setUpExecLogging();
    }

    protected void parseArguments(String[] args) {
        Options options = new Options();
        for (Option option : getOptions()) {
            options.addOption(option);
        }

        try {
            CommandLineParser parser = new PosixParser();
            line = parser.parse(options, args);
        } catch (ParseException e) {
            throw new Termination(e.getMessage());
        }

        if (line.hasOption(OPTION_VERSION_LONG)) {
            System.out
            .println("GLMTK (generalized language modeling toolkit) version 0.1.");
            throw new Termination();
        }

        if (line.hasOption(OPTION_HELP_LONG)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setSyntaxPrefix("Usage: ");
            formatter.setWidth(80);
            formatter.setOptionComparator(new Comparator<Option>() {

                @Override
                public int compare(Option o1, Option o2) {
                    return getOptions().indexOf(o1) - getOptions().indexOf(o2);
                }

            });
            formatter.printHelp(getUsage(), options);
            throw new Termination();
        }
    }

    private void printLogHeader(String[] args) throws IOException,
    InterruptedException {
        LOGGER.info(StringUtils.repeat("=", 80));
        LOGGER.info(getClass().getSimpleName());

        LOGGER.info(StringUtils.repeat("-", 80));

        // log git commit
        Process gitLogProc = Runtime.getRuntime().exec(new String[] {
                "git", "log", "-1", "--format=%H: %s"
        }, null, config.getGlmtkDir().toFile());
        gitLogProc.waitFor();
        try (BufferedReader gitLogReader =
                new BufferedReader(new InputStreamReader(
                        gitLogProc.getInputStream()))) {
            String gitCommit = gitLogReader.readLine();
            LOGGER.info("Git Commit: {}", gitCommit);
        }

        // log user dir
        LOGGER.info("User Dir: {}", config.getUserDir());

        // log glmtk dir
        LOGGER.info("Glmtk Dir: {}", config.getGlmtkDir());

        // log arguments
        LOGGER.info("Arguments: {}", StringUtils.join(args, " "));

        // log config
        LOGGER.info("Config: {}", config);

        LOGGER.info(StringUtils.repeat("-", 80));
    }

    private void printLogFooter() {
        LOGGER.info("done.");
    }

}
