import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Compare different packaging algorithms. (dp)",
        name = "PackPerfTest",
        mixinStandardHelpOptions = true,
        version = "v1.0",
        descriptionHeading = "Large text files for testing can be downloaded from here: https://dumps.wikimedia.org/enwiki/latest/"

)
public class Application implements Callable<Void> {

    @CommandLine.Option(required = true, names = {"-i", "--inputDir"}, description = "Input directory which contains text files.")
    private String inputDir;

    @CommandLine.Option(required = false, names = {"-o", "--outputDir"}, description = "Output dir for results.", defaultValue = "./output")
    private String outputDir;

    public static void main(String[] args) throws Exception {
        CommandLine.call(new Application(), args);
    }

    @Override
    public Void call() throws Exception {

        final long initTime = System.nanoTime();

        Funcs.log.accept("*** Process started ***");

        Funcs.listFiles(Path.of(inputDir))
                .filter(Funcs.isGzipFile)
                .peek(path -> {
                    // fixme: eliminate try catch with exceptionWrapper
                    try {
                        Funcs.log.accept("Input file: " + path.getParent() + "\\" + path.getFileName() + "(" + Files.size(path) / 1000 / 1000 + "MB)");
                        Funcs.log.accept("Start time: " + Funcs.actTime(initTime));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .map(Funcs::toProcessableItem)
//                .peek(item -> Funcs.log.accept(new Date().getTime()))
                .map(Funcs.printRunTimeWrapper("gunzip", Funcs::toGunzipStream))
                .peek(item -> Funcs.log.accept("End time (ms): " + Funcs.actTime(initTime)))
                .forEach(Funcs.printRunTimeWrapper("file save", Funcs::writeItemToFile));

        return null;
    }

}

