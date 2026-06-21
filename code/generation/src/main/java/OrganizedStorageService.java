import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 组织化存储服务
 * 按日期和时间组织输出文件，形成类似数据库的目录结构
 */
public class OrganizedStorageService {
    private String outputBase = "output";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");

    public void setOutputBase(String outputBase) {
        this.outputBase = outputBase;
    }

    /**
     * 保存转换结果到组织化目录
     *
     * @param sourceFileName 源文件名
     * @param dslType DSL类型
     * @param dsl DSL内容
     * @param log 日志内容
     * @param errors 错误信息
     * @param svgPath SVG文件路径（如果存在）
     * @param dotPath DOT文件路径（如果存在）
     * @return 保存的目录路径
     */
    public Path saveConversionResult(String sourceFileName, String dslType, String dsl,
                                    String log, String errors, String svgPath, String dotPath) {
        try {
            // 创建目录结构: output/yyyy-MM-dd/HH-mm-ss-filename/
            LocalDateTime now = LocalDateTime.now();
            String datePart = now.format(DATE_FORMATTER);
            String timePart = now.format(TIME_FORMATTER);

            String baseName = cleanFileName(sourceFileName != null ? sourceFileName : "unknown");
            String sessionDir = timePart + "-" + baseName;

            Path targetDir = Path.of(outputBase, datePart, sessionDir);
            Files.createDirectories(targetDir);

            // 保存元数据
            saveMetadata(targetDir, sourceFileName, dslType, now);

            // 保存各类文件
            if (dsl != null && !dsl.isBlank()) {
                Files.writeString(targetDir.resolve("output.dsl"), dsl, StandardCharsets.UTF_8);
            }

            if (log != null && !log.isBlank()) {
                Files.writeString(targetDir.resolve("conversion.log"), log, StandardCharsets.UTF_8);
            }

            if (errors != null && !errors.isBlank()) {
                Files.writeString(targetDir.resolve("errors.txt"), errors, StandardCharsets.UTF_8);
            }

            // 复制SVG和DOT文件（如果存在）
            if (svgPath != null) {
                copyIfExists(Path.of(svgPath), targetDir.resolve("result.svg"));
            }

            if (dotPath != null) {
                copyIfExists(Path.of(dotPath), targetDir.resolve("result.dot"));
            }

            System.out.println("📁 转换结果已保存到: " + targetDir.toAbsolutePath());
            return targetDir;

        } catch (IOException e) {
            System.err.println("❌ 保存转换结果失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 保存元数据文件
     */
    private void saveMetadata(Path targetDir, String sourceFile, String dslType, LocalDateTime timestamp) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceFile", sourceFile);
        metadata.put("dslType", dslType);
        metadata.put("timestamp", timestamp.toString());
        metadata.put("llmModel", LlmConfig.getInstance().getActiveLlmName());

        StringBuilder sb = new StringBuilder();
        sb.append("=== 转换元数据 ===\n");
        sb.append("源文件: ").append(sourceFile).append("\n");
        sb.append("DSL类型: ").append(dslType).append("\n");
        sb.append("转换时间: ").append(timestamp).append("\n");
        sb.append("LLM模型: ").append(LlmConfig.getInstance().getActiveLlmName()).append("\n");

        Files.writeString(targetDir.resolve("metadata.txt"), sb.toString(), StandardCharsets.UTF_8);
    }

    /**
     * 复制文件（如果存在）
     */
    private void copyIfExists(Path source, Path target) {
        try {
            if (Files.exists(source)) {
                Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("⚠️ 复制文件失败 " + source + " -> " + target + ": " + e.getMessage());
        }
    }

    /**
     * 清理文件名，移除非法字符
     */
    private String cleanFileName(String fileName) {
        // 移除扩展名
        fileName = fileName.replaceAll("\\.(txt|png|pdf|md)$", "");
        // 只保留字母、数字、下划线、中文
        fileName = fileName.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}_-]+", "_");
        // 限制长度
        if (fileName.length() > 50) {
            fileName = fileName.substring(0, 50);
        }
        return fileName;
    }

    /**
     * 获取今天的转换记录目录
     */
    public Path getTodayDirectory() {
        String today = LocalDateTime.now().format(DATE_FORMATTER);
        return Path.of(outputBase, today);
    }

    /**
     * 获取指定日期的转换记录目录
     */
    public Path getDirectoryByDate(LocalDateTime date) {
        String dateStr = date.format(DATE_FORMATTER);
        return Path.of(outputBase, dateStr);
    }

    /**
     * 清理旧数据（保留最近N天）
     */
    public void cleanOldData(int keepDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(keepDays);
            Path outputDir = Path.of(outputBase);

            if (!Files.exists(outputDir)) {
                return;
            }

            Files.list(outputDir)
                .filter(Files::isDirectory)
                .filter(p -> {
                    try {
                        String dirName = p.getFileName().toString();
                        // 跳过非日期格式的目录
                        if (!dirName.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            return false;
                        }
                        LocalDateTime dirDate = LocalDateTime.parse(dirName + "T00:00:00");
                        return dirDate.isBefore(cutoffDate);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(p -> {
                    try {
                        deleteDirectory(p);
                        System.out.println("🗑️ 已删除旧数据: " + p.getFileName());
                    } catch (IOException e) {
                        System.err.println("⚠️ 删除目录失败: " + p + " - " + e.getMessage());
                    }
                });

        } catch (IOException e) {
            System.err.println("⚠️ 清理旧数据失败: " + e.getMessage());
        }
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.isDirectory(directory)) {
            Files.walk(directory)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        System.err.println("⚠️ 删除文件失败: " + p);
                    }
                });
        }
    }
}
