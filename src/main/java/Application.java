import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Compare different packaging algorithms. (dp)",
        name = "PackPerfTest",
        mixinStandardHelpOptions = true,
        version = "v1.0"
)
public class Application implements Callable<Void> {

    @CommandLine.Option(required = true, names = {"-i", "--inputDir"}, description = "The file whose checksum to calculate.")
    private String inputDir;

    public static void main(String[] args) throws Exception {
        CommandLine.call(new Application(), args);
    }

    @Override
    public Void call() throws Exception {

        Funcs.log.accept("*** Process started ***");

        Funcs.listFiles(Path.of(inputDir))
                .filter(Funcs.isGzipFile);
        //.parallel()

        return null;
    }

}

