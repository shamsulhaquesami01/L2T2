package temp_others;

enum FileFormat {
    PDF, DOCX, MARKDOWN
}

class Document {
    private final String id;
    private final FileFormat format;
    private final int sizeKb;
    private final String content;

    public Document(String id, FileFormat format, int sizeKb, String content) {
        this.id = id;
        this.format = format;
        this.sizeKb = sizeKb;
        this.content = content;
    }

    public String getId() { return id; }
    public FileFormat getFormat() { return format; }
    public int getSizeKb() { return sizeKb; }
    public String getContent() { return content; }
}

// --- Abstract Template ---
abstract class DocumentProcessor {

    // Non-overridable template method enforcing invariant lifecycle
    public final void processDocument(Document doc) {
        System.out.println("Processing Document: " + doc.getId() + " (" + doc.getFormat() + ")");
        validate(doc);
        sanitize(doc);
        parseContent(doc);

        if (shouldCompress(doc)) {
            compress(doc);
        } else {
            System.out.println("Skipping compression step.");
        }

        archive(doc);
        System.out.println("Document " + doc.getId() + " processing completed successfully.\n");
    }

    // Fixed Step 1: Validation
    protected void validate(Document doc) {
        if (doc.getSizeKb() <= 0) {
            throw new IllegalArgumentException("Invalid file size for doc: " + doc.getId());
        }
        System.out.println("Validated headers and size constraints (" + doc.getSizeKb() + " KB).");
    }

    // Fixed Step 2: Sanitization
    protected void sanitize(Document doc) {
        System.out.println("Sanitized script tags and stripped malicious code.");
    }

    // Abstract Step 3: Implemented by subclasses
    protected abstract void parseContent(Document doc);

    // Hook Step 4: Subclasses override to change compression policy
    protected boolean shouldCompress(Document doc) {
        return doc.getSizeKb() > 1024; // Default constraint
    }

    private void compress(Document doc) {
        System.out.println("Compressed file buffer to minimize storage footprint.");
    }

    // Fixed Step 5: Archival
    protected void archive(Document doc) {
        System.out.println("Encrypted with AES-256 and committed to secure archive.");
    }
}

// --- Concrete Template Implementations ---

class PDFProcessor extends DocumentProcessor {
    @Override
    protected void validate(Document doc) {
        super.validate(doc);
        System.out.println("Verified ISO 32000 PDF compliance structure.");
    }

    @Override
    protected void parseContent(Document doc) {
        System.out.println("Parsed PDF object cross-reference tables and embedded font streams.");
    }

    @Override
    protected boolean shouldCompress(Document doc) {
        // PDF always compresses regardless of size
        return true;
    }
}

class DOCXProcessor extends DocumentProcessor {
    @Override
    protected void parseContent(Document doc) {
        System.out.println("Extracted XML Zip container and parsed table/paragraph relationships.");
    }

    @Override
    protected boolean shouldCompress(Document doc) {
        // Compresses only if strictly > 2048 KB
        return doc.getSizeKb() > 2048;
    }
}

class MarkdownProcessor extends DocumentProcessor {
    @Override
    protected void parseContent(Document doc) {
        System.out.println("Tokenized plaintext headers, code blocks, and markdown links.");
    }

    @Override
    protected boolean shouldCompress(Document doc) {
        // Plaintext bypasses compression entirely
        return false;
    }
}

public class DocumentPipelineApp {
    public static void main(String[] args) {
        Document pdfDoc = new Document("DOC-01", FileFormat.PDF, 500, "%PDF-1.4...");
        Document docxDoc = new Document("DOC-02", FileFormat.DOCX, 1500, "PK...");
        Document mdDoc = new Document("DOC-03", FileFormat.MARKDOWN, 20, "# Title");

        DocumentProcessor pdfProc = new PDFProcessor();
        DocumentProcessor docxProc = new DOCXProcessor();
        DocumentProcessor mdProc = new MarkdownProcessor();

        pdfProc.processDocument(pdfDoc);
        docxProc.processDocument(docxDoc);
        mdProc.processDocument(mdDoc);
    }
}