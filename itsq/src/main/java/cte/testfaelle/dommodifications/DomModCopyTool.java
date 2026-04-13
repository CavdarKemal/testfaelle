package cte.testfaelle.dommodifications;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import cte.testfaelle.dommodifications.dommodlisteners.DomModNodeListener;
import cte.testfaelle.dommodifications.dommodmarshalling.DomModMarshallingStrategy;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.w3c.dom.Document;

public class DomModCopyTool {
    private final DomModMarshallingStrategy marshallingStrategy;
    private final Path targetSubDir;

    public DomModCopyTool(DomModMarshallingStrategy marshallingStrategy, Path targetSubDir) {
        if (marshallingStrategy == null) {
            throw new DomModException("marshallingStrategy cannot be null");
        }
        this.marshallingStrategy = marshallingStrategy;
        if (targetSubDir == null || !Files.exists(targetSubDir) || !Files.isDirectory(targetSubDir)) {
            throw new DomModException("targetSubDir must be an existing directory");
        }
        this.targetSubDir = targetSubDir;
    }

    public int copyDirectoryContent(DomModContext ctx, DomModNodeListener nodeListener, Path srcDir, Predicate<Path> filter)
            throws DomModException {
        if (srcDir == null) {
            throw new DomModException("srcDir cannot be null ");
        } else if (!Files.exists(srcDir)) {
            throw new DomModException("srcDir does not exist: " + srcDir);
        } else if (!Files.isDirectory(srcDir)) {
            throw new DomModException("srcDir does not refer to a directory: " + srcDir);
        }
        // Erstelle eine Liste der passenden 'Kinder' von srcDir...
        List<Path> srcList;
        try (Stream<Path> streamOfChildren = Files.list(srcDir)) {
            srcList = streamOfChildren.filter(filter).collect(Collectors.toList());
        } catch (IOException e) {
            throw new DomModException(getClass().getSimpleName() + "#copyDirectoryContent scheitert bei der Iteration über Dateien in einem Verzeichnis", e);
        }
        // Bearbeite die gefilterte Liste...
        int anzFiles = 0;
        for (Path srcFile : srcList) {
            copySingle(ctx, nodeListener, srcFile);
            anzFiles++;
        }
        return anzFiles;
    }

    public Path copySingle(DomModContext ctx, DomModNodeListener nodeListener, Path src)
            throws DomModException {
        // Das Einlesen der Daten sowie das Durchlaufen des Baumes übernimmt der DomModTreeWalker
        DomModTreeWalker treeWalker = new DomModTreeWalker(nodeListener);
        Document doc = treeWalker.walkDOMTree(ctx, marshallingStrategy, src);
        // Ergebnis wegschreiben...
        final Path outFile = targetSubDir.resolve(src.getFileName());
        try (OutputStream os = Files.newOutputStream(outFile)) {
            marshallingStrategy.writeToStream(doc, os);
        } catch (IOException e) {
            throw new DomModException(getClass().getSimpleName() + "#copySingle scheitert bei der Erzeugung des OutputStream", e);
        }
        return outFile;
    }

}
