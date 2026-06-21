import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * LLM配置编��对话框
 * 用于添加或编辑单个LLM配置
 */
public class LlmConfigEditDialog extends JDialog {
    private LlmConfigModel config;
    private final boolean isNewConfig;
    private boolean saved = false;

    private JTextField idField;
    private JTextField nameField;
    private JTextField apiBaseField;
    private JPasswordField apiKeyField;
    private JToggleButton showApiKeyButton;
    private JTextField modelField;
    private JSpinner temperatureSpinner;
    private JSpinner maxTokensSpinner;
    private JComboBox<String> typeComboBox;
    private JCheckBox enabledCheckBox;

    public LlmConfigEditDialog(Dialog parent, LlmConfigModel config) {
        super(parent, config == null ? "添加新LLM配置" : "编辑LLM配置", true);

        this.isNewConfig = (config == null);
        this.config = isNewConfig ? new LlmConfigModel() : config;

        initializeComponents();
        layoutComponents();
        loadConfigData();
        setupEventHandlers();

        setSize(600, 500);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        idField = new JTextField(20);
        idField.setEnabled(isNewConfig); // ID只能在新建时设置

        nameField = new JTextField(20);
        apiBaseField = new JTextField(30);

        // 使用密码字段隐藏API密钥
        apiKeyField = new JPasswordField(30);
        apiKeyField.setEchoChar('*');

        // 显示/隐藏密钥按钮
        showApiKeyButton = new JToggleButton("👁");
        showApiKeyButton.setToolTipText("显示/隐藏API密钥");
        showApiKeyButton.setPreferredSize(new Dimension(40, 25));
        showApiKeyButton.addActionListener(e -> {
            if (showApiKeyButton.isSelected()) {
                apiKeyField.setEchoChar((char) 0);
                showApiKeyButton.setText("🔒");
            } else {
                apiKeyField.setEchoChar('*');
                showApiKeyButton.setText("👁");
            }
        });

        modelField = new JTextField(30);

        temperatureSpinner = new JSpinner(new SpinnerNumberModel(0.2, 0.0, 2.0, 0.1));
        maxTokensSpinner = new JSpinner(new SpinnerNumberModel(4096, 1, 100000, 100));

        typeComboBox = new JComboBox<>(new String[]{
            "dsl-generation (DSL生成)",
            "image-annotation (图片标注)"
        });

        enabledCheckBox = new JCheckBox("启用此配置", true);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 基本信息面板
        JPanel basicPanel = new JPanel(new GridBagLayout());
        basicPanel.setBorder(new TitledBorder("基本信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 配置ID
        gbc.gridx = 0; gbc.gridy = 0;
        basicPanel.add(new JLabel("配置ID *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        basicPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel idHintLabel = new JLabel("(只能包含字母、数字、下划线和连字符)");
        idHintLabel.setFont(idHintLabel.getFont().deriveFont(Font.ITALIC, 10f));
        idHintLabel.setForeground(Color.GRAY);
        gbc.gridx = 1;
        basicPanel.add(idHintLabel, gbc);

        // 显示名称
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        basicPanel.add(new JLabel("显示名称 *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        basicPanel.add(nameField, gbc);

        // API配置面板
        JPanel apiPanel = new JPanel(new GridBagLayout());
        apiPanel.setBorder(new TitledBorder("API配置"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // API地址
        gbc.gridx = 0; gbc.gridy = 0;
        apiPanel.add(new JLabel("API地址 *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        apiPanel.add(apiBaseField, gbc);

        // API密钥（带显示/隐藏按钮）
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        apiPanel.add(new JLabel("API密钥 *:"), gbc);

        JPanel apiKeyPanel = new JPanel(new BorderLayout(5, 0));
        apiKeyPanel.add(apiKeyField, BorderLayout.CENTER);
        apiKeyPanel.add(showApiKeyButton, BorderLayout.EAST);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        apiPanel.add(apiKeyPanel, gbc);

        // 模型名称
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        apiPanel.add(new JLabel("模型名称 *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        apiPanel.add(modelField, gbc);


        // 参数配置面板
        JPanel paramsPanel = new JPanel(new GridBagLayout());
        paramsPanel.setBorder(new TitledBorder("参数配置"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 温度参数
        gbc.gridx = 0; gbc.gridy = 0;
        paramsPanel.add(new JLabel("温度参数:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        paramsPanel.add(temperatureSpinner, gbc);

        // 最大Token数
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        paramsPanel.add(new JLabel("最大Token数:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        paramsPanel.add(maxTokensSpinner, gbc);

        // 配置类型
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        paramsPanel.add(new JLabel("配置类型:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        paramsPanel.add(typeComboBox, gbc);

        // 启用状态
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        paramsPanel.add(enabledCheckBox, gbc);

        // 添加所有面板
        mainPanel.add(basicPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(apiPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(paramsPanel);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = new JButton("💾 保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> saveConfig());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // 滚动面板
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {

        // 回车键保存
        getRootPane().setDefaultButton(null);
    }

    private void loadConfigData() {
        if (!isNewConfig) {
            idField.setText(config.getId());
            nameField.setText(config.getName());
            apiBaseField.setText(config.getApiBase());
            apiKeyField.setText(config.getApiKey());
            modelField.setText(config.getModel());
            temperatureSpinner.setValue(config.getTemperature());
            maxTokensSpinner.setValue(config.getMaxTokens());

            if ("image-annotation".equals(config.getType())) {
                typeComboBox.setSelectedIndex(1);
            } else {
                typeComboBox.setSelectedIndex(0);
            }

            enabledCheckBox.setSelected(config.isEnabled());
        }
    }

    private void saveConfig() {
        // 收集表单数据
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String apiBase = apiBaseField.getText().trim();
        String apiKey = new String(apiKeyField.getPassword()).trim();
        String model = modelField.getText().trim();
        double temperature = (Double) temperatureSpinner.getValue();
        int maxTokens = (Integer) maxTokensSpinner.getValue();
        String type = typeComboBox.getSelectedIndex() == 0 ? "dsl-generation" : "image-annotation";
        boolean enabled = enabledCheckBox.isSelected();

        // 创建新配置对象
        LlmConfigModel newConfig = new LlmConfigModel();
        newConfig.setId(id);
        newConfig.setName(name);
        newConfig.setApiBase(apiBase);
        newConfig.setApiKey(apiKey);
        newConfig.setModel(model);
        newConfig.setTemperature(temperature);
        newConfig.setMaxTokens(maxTokens);
        newConfig.setType(type);
        newConfig.setEnabled(enabled);

        // 验证配置
        List<String> errors = newConfig.validate();
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "配置验证失败:\n" + String.join("\n", errors),
                "验证错误",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 保存成功
        this.config = newConfig;
        this.saved = true;
        dispose();
    }


    public boolean isSaved() {
        return saved;
    }

    public LlmConfigModel getConfig() {
        return config;
    }
}
