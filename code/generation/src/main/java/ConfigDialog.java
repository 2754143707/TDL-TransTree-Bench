import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * LLM 配置对话框
 */
public class ConfigDialog extends JDialog {
    private static final String CONFIG_FILE = "llm-config.properties";

    private JTextField apiBaseField;
    private JTextField apiKeyField;
    private JTextField modelField;
    private JTextField temperatureField;
    private JTextField maxTokensField;
    private JSpinner maxRetriesSpinner;
    private JCheckBox syntaxPreCheckBox;
    private JComboBox<String> activeLlmComboBox;

    private Properties properties;
    private boolean configChanged = false;

    public ConfigDialog(Frame parent) {
        super(parent, "LLM 配置", true);
        loadProperties();
        initializeComponents();
        layoutComponents();
        loadCurrentConfig();
        setupEventHandlers();

        setSize(600, 500);
        setLocationRelativeTo(parent);
    }

    // 重载构造：从主界面传入初始选中的 LLM id，打开对话框时显示该 LLM 的详细配置
    public ConfigDialog(Frame parent, String initialLlm) {
        this(parent);
        if (initialLlm != null && !initialLlm.isEmpty()) {
            try {
                activeLlmComboBox.setSelectedItem(initialLlm);
                loadConfigForLlm(initialLlm);
            } catch (Exception ignored) {
            }
        }
    }

    private void loadProperties() {
        properties = new Properties();
        boolean loaded = false;

        // 1. 优先读取外部配置（运行时可修改）
        Path externalConfigFile = Path.of("config", CONFIG_FILE);
        if (Files.exists(externalConfigFile)) {
            try (InputStream input = Files.newInputStream(externalConfigFile)) {
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
                System.out.println("✅ ConfigDialog从外部配置加载: " + externalConfigFile.toAbsolutePath());
                loaded = true;
            } catch (IOException e) {
                System.err.println("⚠️  读取外部配置失败，尝试其他路径: " + e.getMessage());
            }
        }

        // 2. 降级到源码目录（开发环境）
        if (!loaded) {
            Path sourceConfigFile = Path.of("src", "main", "resources", CONFIG_FILE);
            if (Files.exists(sourceConfigFile)) {
                try (InputStream input = Files.newInputStream(sourceConfigFile)) {
                    properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
                    System.out.println("✅ ConfigDialog从源码目录加载: " + sourceConfigFile.toAbsolutePath());
                    loaded = true;
                } catch (IOException e) {
                    System.err.println("⚠️  读取源码目录配置失败，尝试 classpath: " + e.getMessage());
                }
            }
        }

        // 3. 降级到 classpath（打包后）
        if (!loaded) {
            try {
                InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
                if (input != null) {
                    properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
                    input.close();
                    System.out.println("✅ ConfigDialog从 classpath 加载: " + CONFIG_FILE);
                    loaded = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "加载配置文件失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (!loaded) {
            JOptionPane.showMessageDialog(this, "无法找到配置文件: " + CONFIG_FILE,
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeComponents() {
        // 从已加载的 properties 中提取可用的 llm id 列表（形如 llm.<id>.name）并构建下拉项
        java.util.Set<String> propNames = properties.stringPropertyNames();
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (String key : propNames) {
            if (key.startsWith("llm.") && key.endsWith(".name")) {
                String id = key.substring(4, key.length() - 5); // 去掉 "llm." 和 ".name"
                ids.add(id);
            }
        }
        java.util.Collections.sort(ids);
        if (ids.isEmpty()) {
            // 兜底项，确保下拉不为空
            ids.add("gemini");
            ids.add("deepseek");
            ids.add("qwen");
        }
        activeLlmComboBox = new JComboBox<>(ids.toArray(new String[0]));
        apiBaseField = new JTextField(30);
        apiKeyField = new JTextField(30);
        modelField = new JTextField(30);
        temperatureField = new JTextField(10);
        maxTokensField = new JTextField(10);
        maxRetriesSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        syntaxPreCheckBox = new JCheckBox("启用");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LLM 选择
        JPanel llmSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        llmSelectPanel.add(new JLabel("选择 LLM 提供商:"));
        llmSelectPanel.add(activeLlmComboBox);
        mainPanel.add(llmSelectPanel, BorderLayout.NORTH);

        // LLM 配置
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(new TitledBorder("LLM 配置"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;
        addConfigRow(configPanel, gbc, row++, "API Base URL:", apiBaseField);
        addConfigRow(configPanel, gbc, row++, "API Key:", apiKeyField);
        addConfigRow(configPanel, gbc, row++, "模型名称:", modelField);
        addConfigRow(configPanel, gbc, row++, "温度 (0-1):", temperatureField);
        addConfigRow(configPanel, gbc, row++, "最大 Tokens:", maxTokensField);

        // 工具链配置
        JPanel toolchainPanel = new JPanel(new GridBagLayout());
        toolchainPanel.setBorder(new TitledBorder("工具链配置"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = new Insets(5, 5, 5, 5);

        row = 0;
        addConfigRow(toolchainPanel, gbc2, row++, "最大重试次数:", maxRetriesSpinner);
        addConfigRow(toolchainPanel, gbc2, row++, "语法预检查:", syntaxPreCheckBox);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(configPanel, BorderLayout.NORTH);
        centerPanel.add(toolchainPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("💾 保存");
        JButton cancelButton = new JButton("❌ 取消");
        JButton testButton = new JButton("🧪 测试连接");

        saveButton.addActionListener(e -> saveConfig());
        cancelButton.addActionListener(e -> dispose());
        testButton.addActionListener(e -> testConnection());

        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void addConfigRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(component, gbc);
    }

    private void setupEventHandlers() {
        activeLlmComboBox.addActionListener(e -> {
            String selectedLlm = (String) activeLlmComboBox.getSelectedItem();
            loadConfigForLlm(selectedLlm);
        });
    }

    private void loadCurrentConfig() {
        String activeLlm = properties.getProperty("llm.active", "deepseek");
        activeLlmComboBox.setSelectedItem(activeLlm);
        loadConfigForLlm(activeLlm);

        // 加载工具链配置
        maxRetriesSpinner.setValue(Integer.parseInt(properties.getProperty("toolchain.maxRetries", "3")));
        syntaxPreCheckBox.setSelected(Boolean.parseBoolean(properties.getProperty("toolchain.enableSyntaxPreCheck", "true")));
    }

    private void loadConfigForLlm(String llm) {
        String prefix = "llm." + llm + ".";
        apiBaseField.setText(properties.getProperty(prefix + "apiBase", ""));
        apiKeyField.setText(properties.getProperty(prefix + "apiKey", ""));
        modelField.setText(properties.getProperty(prefix + "model", ""));
        temperatureField.setText(properties.getProperty(prefix + "temperature", "0.2"));
        maxTokensField.setText(properties.getProperty(prefix + "maxTokens", "4096"));
    }

    private void saveConfig() {
        try {
            // 验证输入
            if (!validateInputs()) {
                return;
            }

            String activeLlm = (String) activeLlmComboBox.getSelectedItem();
            String prefix = "llm." + activeLlm + ".";

            // 更新属性
            properties.setProperty("llm.active", activeLlm);
            properties.setProperty(prefix + "apiBase", apiBaseField.getText().trim());
            properties.setProperty(prefix + "apiKey", apiKeyField.getText().trim());
            properties.setProperty(prefix + "model", modelField.getText().trim());
            properties.setProperty(prefix + "temperature", temperatureField.getText().trim());
            properties.setProperty(prefix + "maxTokens", maxTokensField.getText().trim());

            properties.setProperty("toolchain.maxRetries", maxRetriesSpinner.getValue().toString());
            properties.setProperty("toolchain.enableSyntaxPreCheck", String.valueOf(syntaxPreCheckBox.isSelected()));

            // 保存到运行时配置目录（config/）
            File configFile = new File("config", CONFIG_FILE);

            // 确保配置目录存在
            configFile.getParentFile().mkdirs();

            // 保存配置
            try (FileOutputStream output = new FileOutputStream(configFile)) {
                properties.store(new OutputStreamWriter(output, StandardCharsets.UTF_8),
                        "LLM Configuration - Updated by GUI at " + new java.util.Date());
            }

            // 如果在开发环境，也同步更新到源文件和编译后的文件
            try {
                Path sourceConfigPath = Path.of("src", "main", "resources", CONFIG_FILE);
                if (Files.exists(sourceConfigPath.getParent())) {
                    try (FileOutputStream output = new FileOutputStream(sourceConfigPath.toFile())) {
                        properties.store(new OutputStreamWriter(output, StandardCharsets.UTF_8),
                                "LLM Configuration - Updated by GUI");
                    }
                }

                Path targetConfigPath = Path.of("target", "classes", CONFIG_FILE);
                if (Files.exists(targetConfigPath.getParent())) {
                    try (FileOutputStream output = new FileOutputStream(targetConfigPath.toFile())) {
                        properties.store(new OutputStreamWriter(output, StandardCharsets.UTF_8),
                                "LLM Configuration - Updated by GUI");
                    }
                }
            } catch (Exception ignored) {
                // 在发布包中这些路径不存在，忽略错误
            }

            configChanged = true;
            JOptionPane.showMessageDialog(this,
                    "配置已保存到: " + configFile.getAbsolutePath(),
                    "保存成功",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "保存配置失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInputs() {
        if (apiBaseField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "API Base URL 不能为空", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (apiKeyField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "API Key 不能为空", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (modelField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "模型名称不能为空", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            double temp = Double.parseDouble(temperatureField.getText().trim());
            if (temp < 0 || temp > 1) {
                JOptionPane.showMessageDialog(this, "温度值必须在 0-1 之间", "验证错误", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "温度值必须是数字", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            int maxTokens = Integer.parseInt(maxTokensField.getText().trim());
            if (maxTokens <= 0) {
                JOptionPane.showMessageDialog(this, "最大 Tokens 必须大于 0", "验证错误", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "最大 Tokens 必须是整数", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void testConnection() {
        try {
            String apiBase = apiBaseField.getText().trim();
            String apiKey = apiKeyField.getText().trim();
            String model = modelField.getText().trim();
            double temperature = Double.parseDouble(temperatureField.getText().trim());

            JOptionPane.showMessageDialog(this,
                    "测试连接功能需要实际调用 API\n" +
                    "请确保配置正确后保存即可\n\n" +
                    "API Base: " + apiBase + "\n" +
                    "Model: " + model,
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "配置格式错误，请检查输入",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfigChanged() {
        return configChanged;
    }
}

