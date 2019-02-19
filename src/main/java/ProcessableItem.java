import java.io.InputStream;

public class ProcessableItem {

    final String fileName;

    final InputStream inputStream;

    public ProcessableItem(String fileName, InputStream inputStream) {
        this.fileName = fileName;
        this.inputStream = inputStream;
    }
}
