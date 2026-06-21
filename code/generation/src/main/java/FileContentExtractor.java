import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

/**
 * 文件内容提取器
 * 支持多种文件格式：txt、png、pdf、md
 */
public class FileContentExtractor {
    
    /**
     * 提取文件内容
     * @param file 文件
     * @return 提取的内容
     * @throws IOException 读取文件失败
     */
    public static String extractContent(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            // 文本文件和Markdown文件直接读取
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || 
                   fileName.endsWith(".jpeg") || fileName.endsWith(".gif") || 
                   fileName.endsWith(".bmp")) {
            // 图片文件转换为Base64（完整数据，用于Gemini Vision API）
            byte[] imageBytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            // 返回完整的base64数据，用特殊标记包裹
            return "[图片文件: " + file.getName() + "]\n[Base64数据: " + base64 + "]\n\n请分析此图片内容。";
        } else if (fileName.endsWith(".pdf")) {
            // PDF文件处理（简化版，实际应该使用PDF解析库）
            // 这里先返回占位符，提示用户手动提取文本
            return "[PDF文件: " + file.getName() + "]\n\n请手动提取PDF文本内容，或使用专门的PDF转换工具。\n\n如果是翻译树DSL类型，请确保包含：\n1. 逻辑节点图\n2. 条件表格\n3. 动作表格\n\n然后将提取的文本内容粘贴到此处。";
        } else {
            throw new IOException("不支持的文件格式: " + fileName);
        }
    }
    
    /**
     * 检查文件是否为图片格式
     */
    public static boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || fileName.endsWith(".gif") || 
               fileName.endsWith(".bmp");
    }
    
    /**
     * 检查文件是否为PDF格式
     */
    public static boolean isPdfFile(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }
    
    /**
     * 检查文件是否为文本格式
     */
    public static boolean isTextFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".txt") || fileName.endsWith(".md");
    }
    
    /**
     * 获取文件类型描述
     */
    public static String getFileTypeDescription(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".txt")) {
            return "文本文件";
        } else if (fileName.endsWith(".md")) {
            return "Markdown文档";
        } else if (fileName.endsWith(".png")) {
            return "PNG图片";
        } else if (fileName.endsWith(".pdf")) {
            return "PDF文档";
        } else {
            return "未知格式";
        }
    }
}