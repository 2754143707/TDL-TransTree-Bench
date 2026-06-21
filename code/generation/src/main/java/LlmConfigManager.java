import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LLM配置管理器
 * 管理所有LLM配置，支持动态添加、编辑、删除
 */
public class LlmConfigManager {
    private static final String CONFIG_FILE = "llm-config.properties";
    private static final Path EXTERNAL_CONFIG_DIR = Path.of("config");
    private static final Path EXTERNAL_CONFIG_FILE = EXTERNAL_CONFIG_DIR.resolve(CONFIG_FILE);
    private static final Path SOURCE_CONFIG_FILE = Path.of("src", "main", "resources", CONFIG_FILE);

    private static LlmConfigManager instance;

    private Properties properties;
    private Map<String, LlmConfigModel> llmConfigs;
    private String activeLlmId;

    private LlmConfigManager() {
        this.llmConfigs = new LinkedHashMap<>();
        loadConfiguration();
    }

    public static synchronized LlmConfigManager getInstance() {
        if (instance == null) {
            instance = new LlmConfigManager();
        }
        return instance;
    }

    /**
     * 重置并重新加载配置
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * 加载配置文件
     */
    private void loadConfiguration() {
        properties = new Properties();
        boolean loaded = false;

        // 1. 优先读取外部配置
        if (Files.exists(EXTERNAL_CONFIG_FILE)) {
            try (InputStream input = Files.newInputStream(EXTERNAL_CONFIG_FILE)) {
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
                System.out.println("✅ 从外部配置加载: " + EXTERNAL_CONFIG_FILE.toAbsolutePath());
                loaded = true;
            } catch (IOException e) {
                System.err.println("⚠️  读取外部配置失败: " + e.getMessage());
            }
        }

        // 2. 降级到源码目录
        if (!loaded && Files.exists(SOURCE_CONFIG_FILE)) {
            try (InputStream input = Files.newInputStream(SOURCE_CONFIG_FILE)) {
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
                System.out.println("✅ 从源码目录加载: " + SOURCE_CONFIG_FILE.toAbsolutePath());
                loaded = true;
            } catch (IOException e) {
                System.err.println("⚠️  读取源码目录配置失败: " + e.getMessage());
            }
        }

        // 3. 降级到 classpath
        if (!loaded) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (input != null) {
                    properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
                    System.out.println("✅ 从 classpath 加载: " + CONFIG_FILE);
                    loaded = true;
                }
            } catch (IOException e) {
                System.err.println("⚠️  从 classpath 加载失败: " + e.getMessage());
            }
        }

        if (!loaded) {
            System.err.println("❌ 无法加载配置文件，将使用空配置");
            properties = new Properties();
        }

        // 解析所有LLM配置
        parseAllLlmConfigs();

        // 获取当前激活的LLM
        activeLlmId = properties.getProperty("llm.active", "");
        if (activeLlmId.isEmpty() && !llmConfigs.isEmpty()) {
            activeLlmId = llmConfigs.keySet().iterator().next();
        }

        System.out.println("✅ 已加载 " + llmConfigs.size() + " 个LLM配置，当前激活: " + activeLlmId);
    }

    /**
     * 解析所有LLM配置
     */
    private void parseAllLlmConfigs() {
        llmConfigs.clear();

        // 找出所有 llm.*.name 的配置项
        Set<String> llmIds = new HashSet<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("llm.") && key.endsWith(".name")) {
                String id = key.substring(4, key.length() - 5); // 去掉 "llm." 和 ".name"
                llmIds.add(id);
            }
        }

        // 为每个ID创建配置对象
        for (String id : llmIds) {
            LlmConfigModel config = LlmConfigModel.fromProperties(id, properties);
            llmConfigs.put(id, config);
        }
    }

    /**
     * 获取所有LLM配置
     */
    public List<LlmConfigModel> getAllConfigs() {
        return new ArrayList<>(llmConfigs.values());
    }

    /**
     * 获取启用的LLM配置
     */
    public List<LlmConfigModel> getEnabledConfigs() {
        return llmConfigs.values().stream()
                .filter(LlmConfigModel::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定ID的配置
     */
    public LlmConfigModel getConfig(String id) {
        return llmConfigs.get(id);
    }

    /**
     * 获取当前激活的配置
     */
    public LlmConfigModel getActiveConfig() {
        return llmConfigs.get(activeLlmId);
    }

    /**
     * 获取当前激活的LLM ID
     */
    public String getActiveLlmId() {
        return activeLlmId;
    }

    /**
     * 设置激活的LLM
     */
    public void setActiveLlm(String id) {
        if (llmConfigs.containsKey(id)) {
            this.activeLlmId = id;
            properties.setProperty("llm.active", id);
        }
    }

    /**
     * 添加或更新LLM配置
     */
    public void saveConfig(LlmConfigModel config) throws Exception {
        // 验证配置
        List<String> errors = config.validate();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("配置验证失败:\n" + String.join("\n", errors));
        }

        // 保存到内存
        llmConfigs.put(config.getId(), config);

        // 更新Properties
        Map<String, String> propsMap = config.toPropertiesMap();
        for (Map.Entry<String, String> entry : propsMap.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }

        // 持久化到文件
        saveToFile();
    }

    /**
     * 删除LLM配置
     */
    public void deleteConfig(String id) throws Exception {
        if (id.equals(activeLlmId)) {
            throw new IllegalArgumentException("不能删除当前激活的LLM配置");
        }

        // 从内存删除
        llmConfigs.remove(id);

        // 从Properties删除
        String prefix = "llm." + id + ".";
        List<String> keysToRemove = properties.stringPropertyNames().stream()
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toList());

        for (String key : keysToRemove) {
            properties.remove(key);
        }

        // 持久化到文件
        saveToFile();
    }

    /**
     * 保存配置到文件
     */
    public void saveToFile() throws IOException {
        // 确保配置目录存在
        Files.createDirectories(EXTERNAL_CONFIG_DIR);

        // 使用自定义格式保存（添加注释和分组）
        try (BufferedWriter writer = Files.newBufferedWriter(EXTERNAL_CONFIG_FILE, StandardCharsets.UTF_8)) {
            // 写入头部注释
            writer.write("#LLM Configuration - Updated by GUI at " + new Date());
            writer.newLine();
            writer.write("#" + new Date());
            writer.newLine();
            writer.newLine();

            // 写入激活的LLM
            writer.write("# Active LLM");
            writer.newLine();
            writer.write("llm.active=" + escapePropertyValue(activeLlmId));
            writer.newLine();
            writer.newLine();

            // 写入工具链配置
            writer.write("# Toolchain Settings");
            writer.newLine();
            if (properties.containsKey("toolchain.maxRetries")) {
                writer.write("toolchain.maxRetries=" + properties.getProperty("toolchain.maxRetries"));
                writer.newLine();
            }
            if (properties.containsKey("toolchain.enableSyntaxPreCheck")) {
                writer.write("toolchain.enableSyntaxPreCheck=" + properties.getProperty("toolchain.enableSyntaxPreCheck"));
                writer.newLine();
            }
            writer.newLine();

            // 按LLM ID分组写入配置
            List<String> sortedIds = new ArrayList<>(llmConfigs.keySet());
            Collections.sort(sortedIds);

            for (String id : sortedIds) {
                LlmConfigModel config = llmConfigs.get(id);
                writer.write("# " + config.getName());
                writer.newLine();

                Map<String, String> propsMap = config.toPropertiesMap();
                // 按固定顺序写入
                String[] keys = {"name", "apiBase", "apiKey", "model", "temperature", "maxTokens", "type", "enabled"};
                for (String key : keys) {
                    String fullKey = "llm." + id + "." + key;
                    if (propsMap.containsKey(fullKey)) {
                        writer.write(fullKey + "=" + escapePropertyValue(propsMap.get(fullKey)));
                        writer.newLine();
                    }
                }
                writer.newLine();
            }
        }

        System.out.println("✅ 配置已保存到: " + EXTERNAL_CONFIG_FILE.toAbsolutePath());
    }

    /**
     * 转义属性值中的特殊字符
     */
    private String escapePropertyValue(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace(":", "\\:")
                    .replace("=", "\\=");
    }

    /**
     * 获取工具链配置
     */
    public int getMaxRetries() {
        return Integer.parseInt(properties.getProperty("toolchain.maxRetries", "3"));
    }

    public boolean isEnableSyntaxPreCheck() {
        return Boolean.parseBoolean(properties.getProperty("toolchain.enableSyntaxPreCheck", "true"));
    }

    public void setMaxRetries(int maxRetries) {
        properties.setProperty("toolchain.maxRetries", String.valueOf(maxRetries));
    }

    public void setEnableSyntaxPreCheck(boolean enable) {
        properties.setProperty("toolchain.enableSyntaxPreCheck", String.valueOf(enable));
    }

    /**
     * 重新加载配置
     */
    public void reload() {
        loadConfiguration();
    }

    /**
     * 获取任意原始属性值（不做额外解析），用于保留用户在配置中填写的格式
     */
    public String getRawProperty(String fullKey, String defaultValue) {
        if (properties == null) return defaultValue == null ? "" : defaultValue;
        return properties.getProperty(fullKey, defaultValue == null ? "" : defaultValue).trim();
    }
}
