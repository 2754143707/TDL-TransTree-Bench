import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 转换历史记录管理器
 * 使用JSON文件存储所有转换记录，提供查询和统计功能
 */
public class ConversionHistory {
    private static final String HISTORY_FILE = "output/conversion-history.json";
    private static final String STATS_FILE = "output/statistics.json";

    private List<ConversionRecord> history;
    private ObjectMapper objectMapper;

    public ConversionHistory() {
        this.history = new ArrayList<>();
        this.objectMapper = createObjectMapper();
        loadFromFile();
    }

    /**
     * 创建配置好的ObjectMapper
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 使用反射加载 JavaTimeModule，避免运行时 ClassNotFoundError 导致进程崩溃
        try {
            Class<?> javaTimeModuleClass = Class.forName("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule");
            mapper.registerModule((com.fasterxml.jackson.databind.Module) javaTimeModuleClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            System.err.println("⚠️ 加载 JavaTimeModule 失败: " + e.getMessage());
        }
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    /**
     * 添加转换记录
     */
    public synchronized void addRecord(ConversionRecord record) {
        history.add(record);
        saveToFile();
        updateStatistics();
    }

    /**
     * 从文件加载历史记录
     */
    private void loadFromFile() {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("📝 历史记录文件不存在，将创建新文件");
            return;
        }

        try {
            HistoryWrapper wrapper = objectMapper.readValue(file, HistoryWrapper.class);
            this.history = wrapper.getHistory();
            System.out.println("✅ 已加载 " + history.size() + " 条历史记录");
        } catch (IOException e) {
            System.err.println("⚠️ 加载历史记录失败: " + e.getMessage());
            this.history = new ArrayList<>();
        }
    }

    /**
     * 保存到文件
     */
    private synchronized void saveToFile() {
        try {
            ensureOutputDirectory();
            HistoryWrapper wrapper = new HistoryWrapper();
            wrapper.setHistory(history);
            wrapper.setLastUpdate(LocalDateTime.now());
            wrapper.setTotalRecords(history.size());

            objectMapper.writeValue(new File(HISTORY_FILE), wrapper);
            System.out.println("💾 历史记录已保存 (共 " + history.size() + " 条)");
        } catch (IOException e) {
            System.err.println("❌ 保存历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        try {
            ConversionStatistics stats = calculateStatistics();
            objectMapper.writeValue(new File(STATS_FILE), stats);
            System.out.println("📊 统计信息已更新");
        } catch (IOException e) {
            System.err.println("⚠️ 更新统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 计算统计信息
     */
    private ConversionStatistics calculateStatistics() {
        ConversionStatistics stats = new ConversionStatistics();
        stats.setTotalConversions(history.size());
        stats.setLastUpdate(LocalDateTime.now());

        long successCount = history.stream().filter(r -> "success".equals(r.getStatus())).count();
        stats.setSuccessCount((int) successCount);
        stats.setFailedCount(history.size() - (int) successCount);
        stats.setSuccessRate(history.isEmpty() ? 0 : (double) successCount / history.size());

        // 平均尝试次数
        double avgAttempts = history.stream()
                .mapToInt(ConversionRecord::getAttempts)
                .average()
                .orElse(0.0);
        stats.setAverageAttempts(avgAttempts);

        // 按模型统计
        Map<String, ModelStats> modelStatsMap = new HashMap<>();
        for (ConversionRecord record : history) {
            String model = record.getLlmModel();
            modelStatsMap.putIfAbsent(model, new ModelStats());
            ModelStats modelStat = modelStatsMap.get(model);
            if ("success".equals(record.getStatus())) {
                modelStat.incrementSuccess();
            } else {
                modelStat.incrementFailed();
            }
        }
        stats.setModelStats(modelStatsMap);

        // 按类型统计
        Map<String, TypeStats> typeStatsMap = new HashMap<>();
        for (ConversionRecord record : history) {
            String type = record.getDslType();
            typeStatsMap.putIfAbsent(type, new TypeStats());
            TypeStats typeStat = typeStatsMap.get(type);
            if ("success".equals(record.getStatus())) {
                typeStat.incrementSuccess();
            } else {
                typeStat.incrementFailed();
            }
        }
        stats.setTypeStats(typeStatsMap);

        return stats;
    }

    /**
     * 查询指定日期的记录
     */
    public List<ConversionRecord> queryByDate(LocalDate date) {
        return history.stream()
                .filter(r -> r.getTimestamp().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * 查询指定日期范围的记录
     */
    public List<ConversionRecord> queryByDateRange(LocalDate start, LocalDate end) {
        return history.stream()
                .filter(r -> {
                    LocalDate recordDate = r.getTimestamp().toLocalDate();
                    return !recordDate.isBefore(start) && !recordDate.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    /**
     * 按状态查询
     */
    public List<ConversionRecord> queryByStatus(String status) {
        return history.stream()
                .filter(r -> status.equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 按DSL类型查询
     */
    public List<ConversionRecord> queryByType(String dslType) {
        return history.stream()
                .filter(r -> dslType.equals(r.getDslType()))
                .collect(Collectors.toList());
    }

    /**
     * 按LLM模型查询
     */
    public List<ConversionRecord> queryByModel(String llmModel) {
        return history.stream()
                .filter(r -> llmModel.equals(r.getLlmModel()))
                .collect(Collectors.toList());
    }

    /**
     * 获取最近N条记录
     */
    public List<ConversionRecord> getRecentRecords(int limit) {
        return history.stream()
                .sorted(Comparator.comparing(ConversionRecord::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有记录
     */
    public List<ConversionRecord> getAllRecords() {
        return new ArrayList<>(history);
    }

    /**
     * 获取统计信息
     */
    public ConversionStatistics getStatistics() {
        return calculateStatistics();
    }

    /**
     * 清空历史记录
     */
    public synchronized void clear() {
        history.clear();
        saveToFile();
        updateStatistics();
    }

    /**
     * 确保输出目录存在
     */
    private void ensureOutputDirectory() throws IOException {
        Path outputDir = Path.of("output");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
    }

    // 内部类：JSON包装器
    public static class HistoryWrapper {
        private List<ConversionRecord> history;
        private LocalDateTime lastUpdate;
        private int totalRecords;

        public List<ConversionRecord> getHistory() { return history; }
        public void setHistory(List<ConversionRecord> history) { this.history = history; }

        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }

        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
    }

    // 内部类：统计信息
    public static class ConversionStatistics {
        private int totalConversions;
        private int successCount;
        private int failedCount;
        private double successRate;
        private double averageAttempts;
        private Map<String, ModelStats> modelStats;
        private Map<String, TypeStats> typeStats;
        private LocalDateTime lastUpdate;

        public int getTotalConversions() { return totalConversions; }
        public void setTotalConversions(int totalConversions) { this.totalConversions = totalConversions; }

        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }

        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }

        public double getAverageAttempts() { return averageAttempts; }
        public void setAverageAttempts(double averageAttempts) { this.averageAttempts = averageAttempts; }

        public Map<String, ModelStats> getModelStats() { return modelStats; }
        public void setModelStats(Map<String, ModelStats> modelStats) { this.modelStats = modelStats; }

        public Map<String, TypeStats> getTypeStats() { return typeStats; }
        public void setTypeStats(Map<String, TypeStats> typeStats) { this.typeStats = typeStats; }

        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    }

    // 模型统计
    public static class ModelStats {
        private int success;
        private int failed;

        public void incrementSuccess() { success++; }
        public void incrementFailed() { failed++; }

        public int getSuccess() { return success; }
        public void setSuccess(int success) { this.success = success; }

        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }

        public double getSuccessRate() {
            int total = success + failed;
            return total == 0 ? 0 : (double) success / total;
        }
    }

    // 类型统计
    public static class TypeStats {
        private int success;
        private int failed;

        public void incrementSuccess() { success++; }
        public void incrementFailed() { failed++; }

        public int getSuccess() { return success; }
        public void setSuccess(int success) { this.success = success; }

        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }

        public double getSuccessRate() {
            int total = success + failed;
            return total == 0 ? 0 : (double) success / total;
        }
    }
}
