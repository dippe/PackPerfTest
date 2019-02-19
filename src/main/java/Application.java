import picocli.CommandLine;

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

        Funcs.log.accept("*** Process started ***");

        Funcs.listFiles(Path.of(inputDir))
                .filter(Funcs.isGzipFile)
                .map(Funcs::toProcessableItem)
                .map(Funcs::toGunzipStream)
                .forEach(Funcs::writeItemToFile);

        return null;
    }

}

