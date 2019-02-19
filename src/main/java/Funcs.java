import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public interface Funcs {

    static final int FILE_STREAM_BUFFER = 64 * 1024;

    static Stream<Path> listFiles(Path dir) throws Exception {
        try {
            return Files.walk(dir);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * https://www.mkyong.com/java/how-to-decompress-file-from-gzip-file/
     */
    static InputStream gunzipFile(Path path) {
        try {
            return new GZIPInputStream(new FileInputStream(path.toString()), FILE_STREAM_BUFFER);
        } catch (IOException e) {
            log.accept("The actual file is unreadable: " + path.getFileName());
            throw new RuntimeException(e.getMessage());
        }
    }


    static Consumer<List<String>> writeToFile(Path outFile) {
        return lines -> {
            try {
                Files.write(outFile, lines, StandardOpenOption.CREATE);
            } catch (IOException e) {
                log.accept("Cannot Write Data: " + outFile.toString());
                throw new RuntimeException(e.getMessage());
            }
        };

    }

    static Predicate<Path> isGzipFile = path -> path.getFileName().toString().endsWith(".gz");

    static void printRunTime(String msg, Path fileToProcess, Consumer<Path> processor){
        Date startTime = new Date();

        processor.accept(fileToProcess);

        Date endTime = new Date();
        Long delta = endTime.getTime() - startTime.getTime();

//        log.accept("Unpack file: " + path.getParent() + "\\" + path.getFileName() + "(" + Files.size(path) / 1000 / 1000 + "MB)");

        log.accept("*** " + msg + " ***");
        log.accept("Elapsed time: " + delta.toString() + "ms");
        log.accept("*** End " + msg + " ***");
    }

    static final Consumer<String> log = s -> System.out.println(s);

}
