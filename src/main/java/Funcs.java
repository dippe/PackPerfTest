import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
            final String outFileName = item.fileName + ".out.txt";

            FileOutputStream fileOutputStream = new FileOutputStream(outFileName);
            item.inputStream.transferTo(fileOutputStream);
            fileOutputStream.close();
            item.inputStream.close();

            Path inFile = Path.of(item.fileName);
            Path outFile = Path.of(outFileName);
            log.accept("Output file: " + outFile.getParent() + "\\" + outFile.getFileName() + "(" + Files.size(outFile) / 1000 / 1000 + "MB)");
        } catch (IOException e) {
            log.accept("Cannot Write Data: " + item.fileName.toString());
            throw new RuntimeException(e.getMessage());
        }
    }


    static Predicate<Path> isGzipFile = path -> path.getFileName().toString().endsWith(".gz");

    static Predicate<Path> isSnappyFile = path -> path.getFileName().toString().endsWith(".snappy");

    static Predicate<Path> isLz4File = path -> path.getFileName().toString().endsWith(".lz4");

    // wrap checked exceptions
    static <T, R> Function<T, R> exceptionWrapper(Function<T, R> processor) {
        return t -> {
            try{
                return processor.apply(t);
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        };
    }

    static <T> Function<T, T> printRunTimeWrapper(String msg, Function<T, T> processor) {
        return (T param) -> {
            log.accept("*** Start: " + msg + " ***");

            long startTime = System.nanoTime();

            T apply = processor.apply(param);

            long endTime = System.nanoTime();
            Long deltaUs = (endTime - startTime) / 10000;
            Long deltaMs = (endTime - startTime) / 1000000;

            log.accept("Elapsed time: " + deltaUs.toString() + "μs / " + deltaMs.toString() + "ms");
            log.accept("*** End: " + msg + " ***");
            return apply;
        };
    }

    static <T> Consumer<T> printRunTimeWrapper(String msg, Consumer<T> processor) {
        return (T param) -> {
            log.accept("*** Start: " + msg + " ***");

            long startTime = System.nanoTime();

            processor.accept(param);

            long endTime = System.nanoTime();
            Long deltaUs = (endTime - startTime) / 10000;
            Long deltaMs = (endTime - startTime) / 1000000;

            log.accept("Elapsed time: " + deltaUs.toString() + "μs / " + deltaMs.toString() + "ms");
            log.accept("*** End: " + msg + " ***");
        };
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

    static String actTime(long initTime){
        return ((System.nanoTime() - initTime) / 1000000) + "ms";
    }

}
