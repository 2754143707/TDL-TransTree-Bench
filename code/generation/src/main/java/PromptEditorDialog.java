import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 提示词编辑器对话框
 */
public class PromptEditorDialog extends JDialog {
    // 外部配置目录（优先级高，运行时可修改）
    private static final Path EXTERNAL_CONFIG_DIR = Path.of("config");
    private static final Path EXTERNAL_PROMPT_DIR = EXTERNAL_CONFIG_DIR.resolve("prompts");

    // 源码目录（开发环境）
    private static final Path SOURCE_PROMPT_DIR = Path.of("src", "main", "resources", "prompts");

    // Classpath 前缀（打包后）
    private static final String CLASSPATH_PROMPTS_PREFIX = "/prompts/";

    private static final String DEFAULT_PROMPT = BenchConstants.PROMPT_GENERATION;
    private static final String USER_PREFERENCES_FILE = "config/prompt-editor-preferences.properties";

    private JComboBox<String> promptTypeComboBox;
    private JTextArea promptTextArea;
    private JLabel charCountLabel;
    private String currentPromptFile;

    public PromptEditorDialog(Frame parent) {
        super(parent, "提示词配置编辑器", true);
        initializeComponents();
        layoutComponents();
        loadPromptList();
        setupEventHandlers();

        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        promptTypeComboBox = new JComboBox<>();
        promptTextArea = new JTextArea();
        promptTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        promptTextArea.setLineWrap(true);
        promptTextArea.setWrapStyleWord(true);
        promptTextArea.setTabSize(4);

        charCountLabel = new JLabel("字符数: 0");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // 顶部：选择提示词类型
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(15, 15, 5, 15));

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("选择提示词模板:"));
        selectPanel.add(promptTypeComboBox);

        JButton refreshButton = new JButton("🔄 刷新");
        refreshButton.addActionListener(e -> loadPromptList());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);

        topPanel.add(selectPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 中间：编辑区域
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(new TitledBorder("提示词内容"));

        JScrollPane scrollPane = new JScrollPane(promptTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(charCountLabel);
        editorPanel.add(infoPanel, BorderLayout.SOUTH);

        centerPanel.add(editorPanel, BorderLayout.CENTER);

        // 提示信息
        JPanel hintPanel = new JPanel(new BorderLayout());
        hintPanel.setBorder(new TitledBorder("使用说明"));
        JTextArea hintArea = new JTextArea(
                "💡 提示词配置说明:\n" +
                "1. 提示词用于指导 LLM 生成符合语法的 DSL 代码\n" +
                "2. 可以在提示词中添加示例、语法规则、约束条件等\n" +
                "3. 修改后请点击保存按钮，重新加载配置后生效\n" +
                "4. 建议保留原有的结构框架，只修改具体的指导内容\n" +
                "5. 支持使用占位符，如 %s 用于动态插入内容"
        );
        hintArea.setEditable(false);
        hintArea.setBackground(new Color(255, 255, 220));
        hintArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        hintArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        hintPanel.add(hintArea, BorderLayout.CENTER);

        centerPanel.add(hintPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // 底部：按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(new EmptyBorder(5, 15, 15, 15));

        JButton saveButton = new JButton("💾 保存");
        JButton revertButton = new JButton("↶ 恢复");
        JButton closeButton = new JButton("❌ 关闭");

        saveButton.addActionListener(e -> savePrompt());
        revertButton.addActionListener(e -> revertPrompt());
        closeButton.addActionListener(e -> dispose());

        bottomPanel.add(saveButton);
        bottomPanel.add(revertButton);
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        promptTypeComboBox.addActionListener(e -> {
            String selectedPrompt = (String) promptTypeComboBox.getSelectedItem();
            if (selectedPrompt != null) {
                // 保存用户的选择
                saveLastSelectedPrompt(selectedPrompt);
                // 加载选中的提示词
                loadSelectedPrompt();
            }
        });

        promptTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }
        });
    }

    private void loadPromptList() {
        promptTypeComboBox.removeAllItems();

        java.util.Set<String> promptFiles = new java.util.LinkedHashSet<>();

        // 1. 加载外部配置目录中的提示词
        try {
            if (Files.exists(EXTERNAL_PROMPT_DIR)) {
                try (java.util.stream.Stream<Path> stream = Files.list(EXTERNAL_PROMPT_DIR)) {
                    stream.filter(p -> p.toString().endsWith(".txt"))
                            .forEach(p -> {
                                promptFiles.add(p.getFileName().toString());
                            });
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️  加载外部配置提示词失败: " + e.getMessage());
        }

        // 2. 加载源码目录中的提示词
        try {
            if (Files.exists(SOURCE_PROMPT_DIR)) {
                try (java.util.stream.Stream<Path> stream = Files.list(SOURCE_PROMPT_DIR)) {
                    stream.filter(p -> p.toString().endsWith(".txt"))
                            .forEach(p -> promptFiles.add(p.getFileName().toString()));
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️  加载源码目录提示词失败: " + e.getMessage());
        }

        // 3. 加载 classpath 中的提示词（打包后）
        try {
            java.net.URL resourceUrl = getClass().getResource(CLASSPATH_PROMPTS_PREFIX);
            if (resourceUrl != null) {
                if (resourceUrl.getProtocol().equals("file")) {
                    // 文件系统
                    Path resourcePath = Path.of(resourceUrl.toURI());
                    if (Files.exists(resourcePath)) {
                        try (java.util.stream.Stream<Path> stream = Files.list(resourcePath)) {
                            stream.filter(p -> p.toString().endsWith(".txt"))
                                    .forEach(p -> promptFiles.add(p.getFileName().toString()));
                        }
                    }
                } else if (resourceUrl.getProtocol().equals("jar")) {
                    // JAR 包内
                    // 可以通过 JAR 文件系统访问，但这里简化处理
                    // 假设已知的提示词文件列表
                    promptFiles.add(BenchConstants.PROMPT_IMAGE_ANNOTATION);
                    promptFiles.add(BenchConstants.PROMPT_GENERATION);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️  加载 classpath 提示词失败: " + e.getMessage());
        }

        // 添加到下拉框
        promptFiles.forEach(promptTypeComboBox::addItem);

        if (promptTypeComboBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "未找到提示词文件\n已搜索路径:\n" +
                    "1. " + EXTERNAL_PROMPT_DIR.toAbsolutePath() + "\n" +
                    "2. " + SOURCE_PROMPT_DIR.toAbsolutePath() + "\n" +
                    "3. classpath:" + CLASSPATH_PROMPTS_PREFIX,
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 加载用户上次的选择，如果没有则默认选择功能模型提示词
            String lastSelected = loadLastSelectedPrompt();
            boolean found = false;
            
            if (lastSelected != null) {
                for (int i = 0; i < promptTypeComboBox.getItemCount(); i++) {
                    if (promptTypeComboBox.getItemAt(i).equals(lastSelected)) {
                        promptTypeComboBox.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
            }
            
            // 如果没找到上次的选择，则默认选择功能模型提示词
            if (!found) {
                for (int i = 0; i < promptTypeComboBox.getItemCount(); i++) {
                    if (promptTypeComboBox.getItemAt(i).equals(DEFAULT_PROMPT)) {
                        promptTypeComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void loadSelectedPrompt() {
        String selectedPrompt = (String) promptTypeComboBox.getSelectedItem();
        if (selectedPrompt == null) {
            return;
        }

        currentPromptFile = selectedPrompt;
        String content = null;

        // 1. 优先读取外部配置
        Path externalPath = EXTERNAL_PROMPT_DIR.resolve(selectedPrompt);
        if (Files.exists(externalPath)) {
            try {
                content = Files.readString(externalPath, StandardCharsets.UTF_8);
                System.out.println("✅ 从外部配置读取: " + externalPath);
            } catch (IOException e) {
                System.err.println("⚠️  读取外部配置失败: " + e.getMessage());
            }
        }

        // 2. 降级到源码目录
        if (content == null) {
            Path sourcePath = SOURCE_PROMPT_DIR.resolve(selectedPrompt);
            if (Files.exists(sourcePath)) {
                try {
                    content = Files.readString(sourcePath, StandardCharsets.UTF_8);
                    System.out.println("✅ 从源码目录读取: " + sourcePath);
                } catch (IOException e) {
                    System.err.println("⚠️  读取源码目录失败: " + e.getMessage());
                }
            }
        }

        // 3. 降级到 classpath
        if (content == null) {
            String resourcePath = CLASSPATH_PROMPTS_PREFIX + selectedPrompt;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is != null) {
                    content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("✅ 从 classpath 读取: " + resourcePath);
                }
            } catch (IOException e) {
                System.err.println("⚠️  从 classpath 读取失败: " + e.getMessage());
            }
        }

        if (content != null) {
            promptTextArea.setText(content);
            promptTextArea.setCaretPosition(0);
            updateCharCount();
        } else {
            promptTextArea.setText("");
            JOptionPane.showMessageDialog(this,
                    "提示词文件不存在: " + selectedPrompt + "\n已搜索:\n" +
                    "1. " + externalPath.toAbsolutePath() + "\n" +
                    "2. " + SOURCE_PROMPT_DIR.resolve(selectedPrompt).toAbsolutePath() + "\n" +
                    "3. classpath:" + CLASSPATH_PROMPTS_PREFIX + selectedPrompt,
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePrompt() {
        if (currentPromptFile == null) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要保存的提示词",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 始终保存到外部配置目录（运行时可修改）
        Path externalPath = EXTERNAL_PROMPT_DIR.resolve(currentPromptFile);

        try {
            // 确保目录存在
            Files.createDirectories(EXTERNAL_PROMPT_DIR);

            // 备份原文件
            if (Files.exists(externalPath)) {
                Path backupPath = EXTERNAL_PROMPT_DIR.resolve(currentPromptFile + ".backup");
                Files.copy(externalPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ 已备份: " + backupPath);
            } else {
                System.out.println("ℹ️  首次保存到外部配置目录");
            }

            // 保存新内容
            Files.writeString(externalPath, promptTextArea.getText(), StandardCharsets.UTF_8);

            JOptionPane.showMessageDialog(this,
                    "提示词已保存到外部配置目录！\n\n" +
                    "保存位置: " + externalPath.toAbsolutePath() + "\n" +
                    "字符数: " + promptTextArea.getText().length() + "\n\n" +
                    "✅ 修改立即生效，无需重新编译！",
                    "保存成功",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "保存提示词失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void revertPrompt() {
        int result = JOptionPane.showConfirmDialog(this,
                "确定要恢复到文件中保存的内容吗？\n当前未保存的修改将丢失。",
                "确认恢复",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            loadSelectedPrompt();
        }
    }

    private void updateCharCount() {
        int charCount = promptTextArea.getText().length();
        int lineCount = promptTextArea.getLineCount();
        charCountLabel.setText(String.format("字符数: %d  |  行数: %d", charCount, lineCount));
    }

    /**
     * 加载用户上次选择的提示词文件
     */
    private String loadLastSelectedPrompt() {
        try {
            Path preferencesFile = Path.of(USER_PREFERENCES_FILE);
            if (Files.exists(preferencesFile)) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.InputStream input = Files.newInputStream(preferencesFile)) {
                    props.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
                    return props.getProperty("last.selected.prompt");
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️  加载提示词编辑器偏好设置失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 保存用户当前选择的提示词文件
     */
    private void saveLastSelectedPrompt(String promptFile) {
        try {
            Path preferencesFile = Path.of(USER_PREFERENCES_FILE);
            // 确保config目录存在
            Files.createDirectories(preferencesFile.getParent());
            
            java.util.Properties props = new java.util.Properties();
            
            // 如果文件已存在，先加载现有配置
            if (Files.exists(preferencesFile)) {
                try (java.io.InputStream input = Files.newInputStream(preferencesFile)) {
                    props.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
                }
            }
            
            // 更新选择的提示词
            props.setProperty("last.selected.prompt", promptFile);
            
            // 保存配置
            try (java.io.OutputStream output = Files.newOutputStream(preferencesFile)) {
                props.store(new java.io.OutputStreamWriter(output, StandardCharsets.UTF_8), 
                           "Prompt Editor Preferences - Updated at " + new java.util.Date());
            }
            
        } catch (IOException e) {
            System.err.println("⚠️  保存提示词编辑器偏好设置失败: " + e.getMessage());
        }
    }
}

