package com.services.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class TextExtractorService {

    public String extractText(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name is null");
        }

        log.info("Extracting text from file: {}", fileName);

        try (InputStream inputStream = file.getInputStream()) {
            return extractTextFromInputStream(inputStream, fileName);
        }
    }

    public String extractTextFromBytes(byte[] bytes, String fileName) throws Exception {
        log.info("Extracting text from bytes for file: {}", fileName);

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("File bytes are empty or null");
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return extractTextFromInputStream(inputStream, fileName);
        }
    }

    private String extractTextFromInputStream(InputStream inputStream, String fileName)
            throws IOException, SAXException, TikaException {

        String lowerFileName = fileName.toLowerCase();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        if (lowerFileName.endsWith(".pdf")) {
            PDFParser parser = new PDFParser();
            parser.parse(inputStream, handler, metadata, context);
        } else if (lowerFileName.endsWith(".docx")) {
            OOXMLParser parser = new OOXMLParser();
            parser.parse(inputStream, handler, metadata, context);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + fileName +
                    ". Please upload PDF or DOCX files.");
        }

        String text = handler.toString();
        log.info("Text extracted successfully. Length: {} characters", text.length());
        return text;
    }

    public boolean isSupportedFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".docx");
    }

    public String getFileType(String fileName) {
        if (fileName == null) return "unknown";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "PDF";
        if (lower.endsWith(".docx")) return "DOCX";
        return "unknown";
    }
}