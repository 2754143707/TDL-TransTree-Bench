import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * LLM 配置管理器
 * 从配置文件读取LLM相关配置，支持多个LLM提供商
 * 支持运行时动态加载（外部配置优先）
 */
public class LlmConfig {
    private static final String CONFIG_FILE = "llm-config.properties";

    // 外部配置目录（优先级高，运行时可修改）
    private static final Path EXTERNAL_CONFIG_DIR = Path.of("config");
    private static final Path EXTERNAL_CONFIG_FILE = EXTERNAL_CONFIG_DIR.resolve(CONFIG_FILE);

    // 源码目录（开发环境）
    private static final Path SOURCE_CONFIG_FILE = Path.of("src", "main", "resources", CONFIG_FILE);

    private static LlmConfig instance;

    private final Properties properties;
    // private final String activeLlm; // Config is now dynamic

    private LlmConfig() {
        properties = new Properties();
        boolean loaded = false;

        // 1. 优先读取外部配置（运行时可修改）
        if (Files.exists(EXTERNAL_CONFIG_FILE)) {
            try (InputStream input = Files.newInputStream(EXTERNAL_CONFIG_FILE)) {
                properties.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
                System.out.println("✅ 从外部配置加载 LLM 配置: " + EXTERNAL_CONFIG_FILE.toAbsolutePath());
                loaded = true;
            } catch (IOException e) {
                System.err.println("⚠️  读取外部配置失败，尝试其他路径: " + e.getMessage());
            }
        }

        // 2. 降级到源码目录（开发环境）
        if (!loaded && Files.exists(SOURCE_CONFIG_FILE)) {
            try (InputStream input = Files.newInputStream(SOURCE_CONFIG_FILE)) {
                properties.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
                System.out.println("✅ 从源码目录加载 LLM 配置: " + SOURCE_CONFIG_FILE.toAbsolutePath());
                loaded = true;
            } catch (IOException e) {
                System.err.println("⚠️  读取源码目录配置失败，尝试 classpath: " + e.getMessage());
            }
        }

        // 3. 降级到 classpath（打包后）
        if (!loaded) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (input == null) {
                    throw new IllegalStateException("无法找到配置文件: " + CONFIG_FILE +
                            "\n已尝试路径:\n" +
                            "  1. " + EXTERNAL_CONFIG_FILE.toAbsolutePath() + "\n" +
                            "  2. " + SOURCE_CONFIG_FILE.toAbsolutePath() + "\n" +
                            "  3. classpath:/" + CONFIG_FILE);
                }
                properties.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
                System.out.println("✅ 从 classpath 加载 LLM 配置: " + CONFIG_FILE);
                loaded = true;
            } catch (IOException e) {
                throw new IllegalStateException("加载配置文件失败: " + CONFIG_FILE, e);
            }
        }

        String active = properties.getProperty("llm.active", "deepseek");
        System.out.println("✅ LLM配置加载成功，当前使用: " + active);
    }

    public static synchronized LlmConfig getInstance() {
        if (instance == null) {
            instance = new LlmConfig();
        }
        return instance;
    }

    /**
     * 重置单例实例，强制重新加载配置
     * 用于配置文件更新后重新加载
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * 重新加载配置
     */
    public void reload() {
        resetInstance();
        getInstance();
    }

    /**
     * 获取当前激活的LLM名称
     */
    public String getActiveLlm() {
        return properties.getProperty("llm.active", "deepseek");
    }

    /**
     * 获取当前激活的LLM显示名称
     */
    public String getActiveLlmName() {
        return getProperty("name", "Unknown");
    }

    /**
     * 获取API Base URL
     */
    public String getApiBase() {
        return getProperty("apiBase");
    }

    /**
     * 获取API Key
     */
    public String getApiKey() {
        return getProperty("apiKey");
    }

    /**
     * 获取模型名称
     */
    public String getModel() {
        return getProperty("model");
    }

    /**
     * 获取温度参数
     */
    public double getTemperature() {
        String temp = getProperty("temperature", "0.2");
        return Double.parseDouble(temp);
    }

    /**
     * 获取最大Token数
     */
    public int getMaxTokens() {
        String maxTokens = getProperty("maxTokens", "4096");
        return Integer.parseInt(maxTokens);
    }

    /**
     * 获取最大重试次数
     */
    public int getMaxRetries() {
        String retries = properties.getProperty("toolchain.maxRetries", "3");
        return Integer.parseInt(retries);
    }

    /**
     * 是否启用语法预检查
     */
    public boolean isEnableSyntaxPreCheck() {
        String enable = properties.getProperty("toolchain.enableSyntaxPreCheck", "true");
        return Boolean.parseBoolean(enable);
    }

    /**
     * 获取指定LLM的属性
     */
    private String getProperty(String key) {
        String fullKey = "llm." + getActiveLlm() + "." + key;
        String value = properties.getProperty(fullKey);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("配置项缺失或为空: " + fullKey);
        }
        return value.trim();
    }

    /**
     * 获取指定LLM的属性，带默认值
     */
    private String getProperty(String key, String defaultValue) {
        String fullKey = "llm." + getActiveLlm() + "." + key;
        String value = properties.getProperty(fullKey, defaultValue);
        return value == null ? defaultValue : value.trim();
    }

    /**
     * 获取任意配置属性（公共方法）
     */
    public String getConfigProperty(String fullKey, String defaultValue) {
        String value = properties.getProperty(fullKey, defaultValue);
        return value == null ? defaultValue : value.trim();
    }

    /**
     * 运行时覆盖配置属性（用于实验脚本）
     */
    public void overrideProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * 打印当前配置
     */
    public void printConfig() {
        System.out.println("========================================");
        System.out.println("📋 当前LLM配置");
        System.out.println("========================================");
        System.out.println("激活的LLM: " + getActiveLlm());
        System.out.println("显示名称: " + getActiveLlmName());
        System.out.println("API地址: " + getApiBase());
        System.out.println("API密钥: " + maskApiKey(getApiKey()));
        System.out.println("模型: " + getModel());
        System.out.println("温度: " + getTemperature());
        System.out.println("最大Tokens: " + getMaxTokens());
        System.out.println("最大重试次数: " + getMaxRetries());
        System.out.println("语法预检查: " + (isEnableSyntaxPreCheck() ? "启用" : "禁用"));
        System.out.println("========================================");
        System.out.println();
    }

    /**
     * 隐藏API Key的敏感部分
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
