import java.util.*;

/**
 * LLM配置模型
 * 表示单个LLM的完整配置
 */
public class LlmConfigModel {
    private String id;              // 配置ID（唯一标识）
    private String name;            // 显示名称
    private String apiBase;         // API地址
    private String apiKey;          // API密钥
    private String model;           // 模型名称
    private double temperature;     // 温度参数
    private int maxTokens;          // 最大Token数
    private String type;            // LLM类型（dsl-generation / image-annotation）
    private boolean enabled;        // 是否启用

    public LlmConfigModel() {
        // 默认值
        this.temperature = 0.2;
        this.maxTokens = 4096;
        this.type = "dsl-generation";
        this.enabled = true;
    }

    public LlmConfigModel(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiBase() {
        return apiBase;
    }

    public void setApiBase(String apiBase) {
        this.apiBase = apiBase;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 转换为Properties格式
     */
    public Map<String, String> toPropertiesMap() {
        Map<String, String> map = new LinkedHashMap<>();
        String prefix = "llm." + id + ".";

        map.put(prefix + "name", name != null ? name : "");
        map.put(prefix + "apiBase", apiBase != null ? apiBase : "");
        map.put(prefix + "apiKey", apiKey != null ? apiKey : "");
        map.put(prefix + "model", model != null ? model : "");
        map.put(prefix + "temperature", String.valueOf(temperature));
        map.put(prefix + "maxTokens", String.valueOf(maxTokens));
        map.put(prefix + "type", type != null ? type : "dsl-generation");
        map.put(prefix + "enabled", String.valueOf(enabled));

        return map;
    }

    /**
     * 从Properties加载
     */
    public static LlmConfigModel fromProperties(String id, Properties props) {
        LlmConfigModel config = new LlmConfigModel();
        config.setId(id);

        String prefix = "llm." + id + ".";
        config.setName(props.getProperty(prefix + "name", id));
        config.setApiBase(props.getProperty(prefix + "apiBase", ""));
        config.setApiKey(props.getProperty(prefix + "apiKey", ""));
        config.setModel(props.getProperty(prefix + "model", ""));

        try {
            config.setTemperature(Double.parseDouble(props.getProperty(prefix + "temperature", "0.2")));
        } catch (NumberFormatException e) {
            config.setTemperature(0.2);
        }

        try {
            config.setMaxTokens(Integer.parseInt(props.getProperty(prefix + "maxTokens", "4096")));
        } catch (NumberFormatException e) {
            config.setMaxTokens(4096);
        }

        config.setType(props.getProperty(prefix + "type", "dsl-generation"));
        config.setEnabled(Boolean.parseBoolean(props.getProperty(prefix + "enabled", "true")));

        return config;
    }

    /**
     * 验证配置是否完整
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (id == null || id.trim().isEmpty()) {
            errors.add("配置ID不能为空");
        } else if (!id.matches("[a-zA-Z0-9_-]+")) {
            errors.add("配置ID只能包含字母、数字、下划线和连字符");
        }

        if (name == null || name.trim().isEmpty()) {
            errors.add("显示名称不能为空");
        }

        if (apiBase == null || apiBase.trim().isEmpty()) {
            errors.add("API地址不能为空");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            errors.add("API密钥不能为空");
        }

        if (model == null || model.trim().isEmpty()) {
            errors.add("模型名称不能为空");
        }

        if (temperature < 0 || temperature > 2) {
            errors.add("温度参数必须在0-2之间");
        }

        if (maxTokens < 1 || maxTokens > 100000) {
            errors.add("最大Token数必须在1-100000之间");
        }

        return errors;
    }

    @Override
    public String toString() {
        return name != null ? name : id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LlmConfigModel that = (LlmConfigModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
