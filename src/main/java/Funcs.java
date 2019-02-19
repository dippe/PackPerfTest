import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public interface Funcs {

    static final int FILE_BUFFER_SIZE = 64 * 1024;

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
            return new GZIPInputStream(new FileInputStream(path.toString()), FILE_BUFFER_SIZE);
        } catch (IOException e) {
            log.accept("The actual file is unreadable: " + path.getFileName());
            throw new RuntimeException(e.getMessage());
        }
    }

    static ProcessableItem toGunzipStream(ProcessableItem item) {
        try {
            return new ProcessableItem(item.fileName, new GZIPInputStream(item.inputStream, FILE_BUFFER_SIZE));
        } catch (IOException e) {
            log.accept("The actual file cannot be gunzipped: " + item.fileName);
            throw new RuntimeException(e.getMessage());
        }
    }

    static ProcessableItem toProcessableItem(Path path) {
        try {
            return new ProcessableItem(path.toString(), new FileInputStream(path.toString()));
        } catch (IOException e) {
            log.accept("The actual file is unreadable: " + path.getFileName());
            throw new RuntimeException(e.getMessage());
        }
    }


    static Consumer<Stream<String>> writeToFile(Path outFile) {
        return (stream) -> {
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile.toString()), FILE_BUFFER_SIZE);

                stream.forEach(line -> {
                    try {
                        bufferedWriter.write(line);
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                });

//                int len;
//                byte[] buffer = new byte[1024*1024];
//
//                while ((len = gzis.read(buffer)) > 0) {
//                    out.write(buffer, 0, len);
//                }

            } catch (IOException e) {
                log.accept("Cannot Write Data: " + outFile.toString());
                throw new RuntimeException(e.getMessage());
            }
        };
    }

    static void writeItemToFile(ProcessableItem item) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(item.fileName + ".out.txt");
            item.inputStream.transferTo(fileOutputStream);
            fileOutputStream.close();
            item.inputStream.close();
        } catch (IOException e) {
            log.accept("Cannot Write Data: " + item.fileName.toString());
            throw new RuntimeException(e.getMessage());
        }
    }


    static Predicate<Path> isGzipFile = path -> path.getFileName().toString().endsWith(".gz");

    static Predicate<Path> isSnappyFile = path -> path.getFileName().toString().endsWith(".snappy");

    static Predicate<Path> isLz4File = path -> path.getFileName().toString().endsWith(".lz4");


    static void printRunTime(String msg, Path fileToProcess, Consumer<Path> processor) {
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

    static Function<InputStream, Stream<String>> processLine(Function<String, String> lineMapper) {
        return (inputStream -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), FILE_BUFFER_SIZE);
            Stream<String> stringStream = bufferedReader
                    .lines()
                    .map(lineMapper);
            return stringStream;
        });
    }

}
