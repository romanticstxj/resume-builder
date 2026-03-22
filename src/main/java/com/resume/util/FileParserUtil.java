package com.resume.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件解析工具类
 * 用于从PDF/Word/Text文件中提取文本内容
 */
@Slf4j
public class FileParserUtil {

    /**
     * 根据文件类型提取文本内容
     */
    public static String extractText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(fileName).toLowerCase();

        try (InputStream inputStream = file.getInputStream()) {
            return switch (extension) {
                case "pdf" -> extractPdfText(inputStream);
                case "doc" -> extractDocText(inputStream);
                case "docx" -> extractDocxText(inputStream);
                case "txt" -> extractTxtText(inputStream);
                default -> throw new IllegalArgumentException("不支持的文件格式: " + extension);
            };
        }
    }

    /**
     * 从文件字节数组提取文本（根据原始文件名推断扩展名）
     */
    public static String extractTextFromBytes(byte[] fileBytes, String originalFilename) throws IOException {
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        String extension = getFileExtension(originalFilename).toLowerCase();
        try (InputStream inputStream = new java.io.ByteArrayInputStream(fileBytes)) {
            return switch (extension) {
                case "pdf" -> extractPdfText(inputStream);
                case "doc" -> extractDocText(inputStream);
                case "docx" -> extractDocxText(inputStream);
                case "txt" -> extractTxtText(inputStream);
                default -> throw new IllegalArgumentException("不支持的文件格式: " + extension);
            };
        }
    }

    /**
     * 提取PDF文件文本
     */
    private static String extractPdfText(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            log.info("PDF文件加载成功，页数: {}", document.getNumberOfPages());
            PDFTextStripper stripper = new PDFTextStripper();
            // 设置排序，按阅读顺序提取文本
            stripper.setSortByPosition(true);
            // 设置行分隔符为换行符
            stripper.setLineSeparator("\n");
            // 从第一页开始
            stripper.setStartPage(1);
            // 提取所有页面
            stripper.setEndPage(document.getNumberOfPages());
            String text = stripper.getText(document);
            log.info("PDF文本提取完成，原始长度: {}", text.length());
            log.info("PDF文本前100字符: {}", text.length() > 100 ? text.substring(0, 100) : text);
            // 清理文本
            text = text.replaceAll("[\\r\\n]+", "\n");  // 统一换行符
            text = text.replaceAll("\\s+", " ");  // 多个空格合并为一个
            text = text.trim();
            log.info("PDF文本清理后长度: {}", text.length());
            if (text.isEmpty()) {
                log.warn("PDF文本为空，可能是扫描版、加密PDF或特殊格式");
            }
            return text;
        } catch (IOException e) {
            log.error("PDF解析失败", e);
            throw new IOException("PDF文件解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取Word (.doc) 文件文本
     */
    private static String extractDocText(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        }
    }

    /**
     * 提取Word (.docx) 文件文本
     */
    private static String extractDocxText(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    /**
     * 提取纯文本文件
     */
    private static String extractTxtText(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes());
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
