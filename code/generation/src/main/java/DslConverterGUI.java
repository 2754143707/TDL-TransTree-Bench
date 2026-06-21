import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * DSL 转换器图形界面主程序
 */
public class DslConverterGUI extends JFrame {
    private static final String APP_TITLE = "Link16 DSL 自然语言转换工具";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;

    // 组件
    private JList<File> fileList;
    private DefaultListModel<File> fileListModel;
    private JTextPane logArea;
    private JTextPane dslPreviewArea;
    private JTextPane errorArea;
    private JProgressBar progressBar;
    private JButton addFilesButton;
    private JButton removeFilesButton;
    private JButton clearFilesButton;
    private JButton startConversionButton;
    private JButton stopConversionButton;
    private JButton settingsButton;
    private JButton promptEditorButton;
    private JButton statisticsButton;
    private JComboBox<String> typeComboBox;

    // 业务逻辑
    private ToolchainService toolchainService;
    private volatile boolean isConverting = false;
    private Thread conversionThread;

    public DslConverterGUI() {
        super(APP_TITLE);
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        initializeServices();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        // 文件列表
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setCellRenderer(new FileListCellRenderer());

        // 日志区域
        logArea = createTextPane();
        dslPreviewArea = createTextPane();
        errorArea = createTextPane();

        // 进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("就绪");

        // 按钮
        addFilesButton = new JButton("📁 添加文件");
        removeFilesButton = new JButton("❌ 移除选中");
        clearFilesButton = new JButton("🗑️ 清空列表");
        startConversionButton = new JButton("▶️ 开始转换");
        stopConversionButton = new JButton("⏹️ 停止");
        stopConversionButton.setEnabled(false);
        settingsButton = new JButton("⚙️ LLM 配置");
        promptEditorButton = new JButton("📝 提示词配置");
        statisticsButton = new JButton("📊 统计信息");


        // 类型选择
        typeComboBox = new JComboBox<>(new String[]{BenchConstants.TYPE_TRANSLATION_TREE});
    }

    private JTextPane createTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);

        // 使用支持 Unicode 字符（包括 emoji）的字体
        Font font = null;

        // 优先使用 Microsoft YaHei UI（比 Microsoft YaHei 更好）
        if (isFontAvailable("Microsoft YaHei UI")) {
            font = new Font("Microsoft YaHei UI", Font.PLAIN, 12);
            System.out.println("使用字体: Microsoft YaHei UI");
        } else if (isFontAvailable("Microsoft YaHei")) {
            font = new Font("Microsoft YaHei", Font.PLAIN, 12);
            System.out.println("使用字体: Microsoft YaHei");
        } else if (isFontAvailable("Segoe UI Emoji")) {
            font = new Font("Segoe UI Emoji", Font.PLAIN, 12);
            System.out.println("使用字体: Segoe UI Emoji");
        } else if (isFontAvailable("SimSun")) {
            font = new Font("SimSun", Font.PLAIN, 12);
            System.out.println("使用字体: SimSun");
        } else {
            font = new Font("Dialog", Font.PLAIN, 12);
            System.out.println("使用字体: Dialog (默认)");
        }

        textPane.setFont(font);
        textPane.setBackground(Color.WHITE);

        return textPane;
    }

    /**
     * 检查系统是否支持指定字体
     */
    private boolean isFontAvailable(String fontName) {
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String availableFont : availableFonts) {
            if (availableFont.equalsIgnoreCase(fontName)) {
                return true;
            }
        }
        return false;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // 顶部工具栏
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(startConversionButton);
        buttonPanel.add(stopConversionButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(settingsButton);
        buttonPanel.add(promptEditorButton);
        buttonPanel.add(statisticsButton);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        typePanel.add(new JLabel("DSL 类型:"));
        typePanel.add(typeComboBox);

        topPanel.add(buttonPanel, BorderLayout.WEST);
        topPanel.add(typePanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 中间主要区域
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(300);

        // 左侧：文件列表
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder("待转换文件"));

        JPanel fileButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        fileButtonPanel.add(addFilesButton);
        fileButtonPanel.add(removeFilesButton);
        fileButtonPanel.add(clearFilesButton);

        leftPanel.add(fileButtonPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        // 右侧：标签页
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📋 转换日志", createScrollPane(logArea));
        tabbedPane.addTab("📄 DSL 预览", createScrollPane(dslPreviewArea));
        tabbedPane.addTab("⚠️ 错误信息", createScrollPane(errorArea));

        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(tabbedPane);

        add(mainSplitPane, BorderLayout.CENTER);

        // 底部：进度条
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JScrollPane createScrollPane(JTextPane textPane) {
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    private void setupEventHandlers() {
        // 添加文件
        addFilesButton.addActionListener(e -> addFiles());

        // 移除文件
        removeFilesButton.addActionListener(e -> removeSelectedFiles());

        // 清空列表
        clearFilesButton.addActionListener(e -> clearFileList());

        // 开始转换
        startConversionButton.addActionListener(e -> startConversion());

        // 停止转换
        stopConversionButton.addActionListener(e -> stopConversion());

        // 设置
        settingsButton.addActionListener(e -> openSettings());

        // 提示词编辑器
        promptEditorButton.addActionListener(e -> openPromptEditor());

        // 统计信息
        statisticsButton.addActionListener(e -> showStatistics());

        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isConverting) {
                    int result = JOptionPane.showConfirmDialog(
                            DslConverterGUI.this,
                            "转换正在进行中，确定要退出吗？",
                            "确认退出",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        stopConversion();
                    } else {
                        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        return;
                    }
                }
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

    private void initializeServices() {
        try {
            toolchainService = new ToolchainService();
            // 设置进度监听器
            toolchainService.setProgressListener(createProgressListener());
            appendLog("✅ 服务初始化成功");
            LlmConfig.getInstance().printConfig();
        } catch (Exception e) {
            appendLog("❌ 服务初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".txt") || name.endsWith(".png") || 
                       name.endsWith(".pdf") || name.endsWith(".md");
            }

            @Override
            public String getDescription() {
                return "支持的文件 (*.txt, *.png, *.pdf, *.md)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                if (!fileListModel.contains(file)) {
                    fileListModel.addElement(file);
                }
            }
        }
    }

    private void removeSelectedFiles() {
        List<File> selectedFiles = fileList.getSelectedValuesList();
        for (File file : selectedFiles) {
            fileListModel.removeElement(file);
        }
    }

    private void clearFileList() {
        int count = fileListModel.getSize();
        fileListModel.clear();
    }

    private void uploadImage() {
        String selectedType = (String) typeComboBox.getSelectedItem();
        if (!BenchConstants.isTranslationTree(selectedType)) {
            JOptionPane.showMessageDialog(this, 
                "图片上传功能仅适用于\"消息转发\"类型", 
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 创建并显示图片标注对话框
        ImageAnnotationDialog dialog = new ImageAnnotationDialog(this, toolchainService.getLlmClient());
        dialog.setVisible(true);

        // 检查是否完成了标注
        if (dialog.isAnnotationCompleted()) {
            // 创建一个虚拟文件来表示标注结果
            String content = "[标注图片数据]\n" + dialog.getAnnotatedImageBase64() + 
                           "\n\n[PDF原文]\n" + dialog.getPdfText();
            
            // 创建临时文件
            try {
                java.io.File tempFile = java.io.File.createTempFile("translation_tree_", ".txt");
                java.nio.file.Files.writeString(tempFile.toPath(), content, java.nio.charset.StandardCharsets.UTF_8);
                
                // 添加到文件列表
                if (!fileListModel.contains(tempFile)) {
                    fileListModel.addElement(tempFile);
                }

            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "创建临时文件失败: " + ex.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startConversion() {
        if (fileListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先添加要转换的文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        isConverting = true;
        startConversionButton.setEnabled(false);
        stopConversionButton.setEnabled(true);
        addFilesButton.setEnabled(false);
        removeFilesButton.setEnabled(false);
        clearFilesButton.setEnabled(false);

        logArea.setText("");
        dslPreviewArea.setText("");
        errorArea.setText("");

        conversionThread = new Thread(this::performConversion);
        conversionThread.start();
    }

    private void performConversion() {
        int totalFiles = fileListModel.getSize();
        String type = (String) typeComboBox.getSelectedItem();
        LlmConfig.resetInstance();
        if (toolchainService != null) {
            toolchainService.reloadConfig();
        }
        String activeLlm = LlmConfig.getInstance().getActiveLlm();

        appendLog("========================================");
        appendLog("🚀 开始批量转换");
        appendLog("========================================");
        appendLog("总文件数: " + totalFiles);
        appendLog("DSL 类型: " + type);
        appendLog("当前 LLM: " + activeLlm);
        appendLog("");

        // 检查是否为翻译树图片标注功能
        if (BenchConstants.isTranslationTree(type) && ("doubao-image".equals(activeLlm) || "gemini-image".equals(activeLlm))) {
            handleImageAnnotation();
        } else if (BenchConstants.isTranslationTree(type) && totalFiles > 1) {
            // 翻译树DSL生成：支持多文件合并处理
            handleTranslationTreeBatch();
        } else {
            // 常规的逐个文件处理
            handleRegularConversion();
        }
    }

    /**
     * 处理翻译树图片标注功能
     */
    private void handleImageAnnotation() {
        String activeLlm = LlmConfig.getInstance().getActiveLlm();
        String modelName = activeLlm.equals("doubao-image") ? 
            "豆包图片标注 (Doubao-Seedream-4.5)" : 
            "Gemini图片标注 (Gemini Vision)";
            
        appendLog("🎨 开始翻译树图片标注处理");
        appendLog("📋 使用模型: " + modelName);
        appendLog("");
        
        final int[] successCount = {0};
        final int totalFiles = fileListModel.getSize();
        final StringBuilder errorMessages = new StringBuilder();
        
        for (int i = 0; i < totalFiles && isConverting; i++) {
            File file = fileListModel.getElementAt(i);
            
            try {
                appendLog("📂 处理文件: " + file.getName());
                updateProgress((i * 100) / totalFiles, "标注图片: " + file.getName());
                
                // 读取文件内容
                String fileContent = FileContentExtractor.extractContent(file);
                String fileType = FileContentExtractor.getFileTypeDescription(file);
                appendLog("📖 已读取" + fileType + "，内容长度: " + fileContent.length() + " 字符");
                
                // 调用图片标注服务
                AnnotationResult result = toolchainService.annotateImage(fileContent, file.getName());
                
                if (result.success) {
                    appendLog("✅ 图片标注成功: " + file.getName());
                    appendLog("📁 标注结果保存至: " + result.imagePath);
                    
                    SwingUtilities.invokeLater(() -> {
                        String message = "图片标注成功！\n\n是否要打开标注后的文件？";
                        String[] options = new String[] { "打开", "取消" };
                        int choice = JOptionPane.showOptionDialog(
                                DslConverterGUI.this,
                                message,
                                "标注成功",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                options,
                                options[0]
                        );
                        if (choice == 0) {
                            try {
                                java.io.File f = new java.io.File(result.imagePath);
                                if (f.exists()) {
                                    java.awt.Desktop.getDesktop().open(f);
                                } else {
                                    JOptionPane.showMessageDialog(
                                        DslConverterGUI.this,
                                        "文件不存在：\n" + result.imagePath,
                                        "提示",
                                        JOptionPane.WARNING_MESSAGE
                                    );
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(
                                    DslConverterGUI.this,
                                    "无法打开文件：\n" + ex.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        }
                    });
                    appendLog("");
                    successCount[0]++;
                } else {
                    appendLog("❌ 图片标注失败: " + file.getName());
                    appendError("文件: " + file.getName() + "\n标注失败: " + result.imageData + "\n\n");
                    
                     SwingUtilities.invokeLater(() -> {
                        javax.swing.JTextArea area = new javax.swing.JTextArea(
                            "图片标注失败：\n\n文件: " + file.getName() + "\n\n错误信息:\n" + (result.imageData == null ? "" : result.imageData)
                        );
                        area.setEditable(false);
                        area.setLineWrap(true);
                        area.setWrapStyleWord(true);
                        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(area);
                        scroll.setPreferredSize(new java.awt.Dimension(600, 300));
                        JOptionPane.showMessageDialog(
                            DslConverterGUI.this,
                            scroll,
                            "图片标注失败",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                    errorMessages.append("文件: ").append(file.getName()).append("\n");
                    errorMessages.append("错误: ").append(result.imageData).append("\n\n");
                }
                
            } catch (IOException e) {
                appendLog("❌ 读取文件失败: " + e.getMessage());
                appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
                errorMessages.append("文件: ").append(file.getName()).append("\n");
                errorMessages.append("读取错误: ").append(e.getMessage()).append("\n\n");
            } catch (Exception e) {
                appendLog("❌ 图片标注失败: " + e.getMessage());
                appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
                errorMessages.append("文件: ").append(file.getName()).append("\n");
                errorMessages.append("处理错误: ").append(e.getMessage()).append("\n\n");
                e.printStackTrace();
            }
        }
        
        updateProgress(100, "图片标注完成");
        finishConversion();
        
        // 根据结果显示不同的提示框
        SwingUtilities.invokeLater(() -> {
            if (successCount[0] == totalFiles) {
                // 全部成功
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "图片标注已完成！\n\n" +
                    "成功标注: " + successCount[0] + "/" + totalFiles + " 个文件\n" +
                    "标注结果保存在 'annotated-images' 文件夹中。\n\n" +
                    "请查看标注结果，选择最佳的图片用于下一步DSL生成。\n\n" +
                    "是否现在打开标注结果文件夹？",
                    "标注完成",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        java.awt.Desktop.getDesktop().open(new File("annotated-images"));
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, 
                            "无法打开文件夹，请手动查看 'annotated-images' 目录", 
                            "提示", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } else if (successCount[0] > 0) {
                // 部分成功
                String formattedErrors = formatErrorMessage(errorMessages.toString());
                JOptionPane.showMessageDialog(
                    this,
                    "<html><body style='width: 400px;'>" +
                    "<h3>图片标注部分完成</h3>" +
                    "<p>成功标注: " + successCount[0] + "/" + totalFiles + " 个文件<br>" +
                    "失败文件: " + (totalFiles - successCount[0]) + " 个</p>" +
                    "<br><b>错误详情:</b><br>" +
                    formattedErrors +
                    "<br><p>成功的标注结果保存在 'annotated-images' 文件夹中。</p>" +
                    "</body></html>",
                    "标注部分完成",
                    JOptionPane.WARNING_MESSAGE
                );
            } else {
                // 全部失败
                String formattedErrors = formatErrorMessage(errorMessages.toString());
                JOptionPane.showMessageDialog(
                    this,
                    "<html><body style='width: 400px;'>" +
                    "<h3>图片标注失败</h3>" +
                    "<p>所有文件标注都失败了。</p>" +
                    "<br><b>错误详情:</b><br>" +
                    formattedErrors +
                    "</body></html>",
                    "标注失败",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * 格式化错误信息，添加HTML换行和限制长度
     */
    private String formatErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return "未知错误";
        }
        
        // 将长的错误信息分行显示
        StringBuilder formatted = new StringBuilder();
        String[] lines = errorMessage.split("\n");
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                formatted.append("<br>");
                continue;
            }
            
            // 如果行太长，进行换行处理
            if (line.length() > 80) {
                // 特殊处理JSON错误信息
                if (line.contains("{\"error\":")) {
                    formatted.append("<b>API错误:</b><br>");
                    String jsonPart = line.substring(line.indexOf("{"));
                    // 简化JSON显示
                    if (jsonPart.contains("InvalidParameter")) {
                        formatted.append("• 模型参数无效<br>");
                    }
                    if (jsonPart.contains("does not support this api")) {
                        formatted.append("• 模型不支持此API<br>");
                    }
                    if (jsonPart.contains("Request id:")) {
                        String requestId = jsonPart.substring(jsonPart.indexOf("Request id:"));
                        if (requestId.length() > 50) {
                            requestId = requestId.substring(0, 50) + "...";
                        }
                        formatted.append("• ").append(requestId).append("<br>");
                    }
                } else {
                    // 普通长行处理：每80字符换行
                    for (int i = 0; i < line.length(); i += 80) {
                        int end = Math.min(i + 80, line.length());
                        formatted.append(line.substring(i, end));
                        if (end < line.length()) {
                            formatted.append("<br>");
                        }
                    }
                }
            } else {
                formatted.append(line);
            }
            formatted.append("<br>");
        }
        
        return formatted.toString();
    }

    private void handleTranslationTreeBatch() {
        int totalFiles = fileListModel.getSize();
        appendLog("🔄 翻译树DSL批量处理模式");
        appendLog("将合并处理 " + totalFiles + " 个文件");
        appendLog("");

        StringBuilder combinedContent = new StringBuilder();
        combinedContent.append("=== 翻译树DSL批量处理 ===\n\n");

        // 收集所有文件内容
        for (int i = 0; i < totalFiles; i++) {
            File file = fileListModel.getElementAt(i);
            appendLog("📖 读取文件 [" + (i + 1) + "/" + totalFiles + "]: " + file.getName());

            try {
                String content = FileContentExtractor.extractContent(file);
                String fileType = FileContentExtractor.getFileTypeDescription(file);
                
                combinedContent.append("--- 文件 ").append(i + 1).append(": ").append(file.getName())
                              .append(" (").append(fileType).append(") ---\n");
                combinedContent.append(content).append("\n\n");
                
                appendLog("✅ 已读取" + fileType + "，内容长度: " + content.length() + " 字符");
            } catch (IOException e) {
                appendLog("❌ 读取文件失败: " + e.getMessage());
                combinedContent.append("--- 文件 ").append(i + 1).append(": ").append(file.getName())
                              .append(" (读取失败) ---\n");
                combinedContent.append("错误: ").append(e.getMessage()).append("\n\n");
            }
        }

        appendLog("");
        appendLog("🔄 开始合并处理翻译树DSL...");
        
        try {
            // 使用合并后的内容进行转换
            String combinedFileName = "translation_tree_batch_" + totalFiles + "_files";
            ToolchainResult result = toolchainService.generateAndValidate(
                combinedContent.toString(), BenchConstants.TYPE_TRANSLATION_TREE, combinedFileName);
            
            // 显示结果
            displayResult(result, combinedFileName);
            
        } catch (Exception e) {
            appendLog("❌ 批量转换失败: " + e.getMessage());
            appendError("批量处理错误: " + e.getMessage() + "\n\n");
            e.printStackTrace();
        }

        updateProgress(100, "批量处理完成");
        finishConversion();
    }

    private void handleRegularConversion() {
        int totalFiles = fileListModel.getSize();
        
        for (int i = 0; i < totalFiles && isConverting; i++) {
            File file = fileListModel.getElementAt(i);
            int fileIndex = i + 1;
            int progress = (int) ((i / (double) totalFiles) * 100);

            updateProgress(progress, "正在处理 " + fileIndex + "/" + totalFiles + ": " + file.getName());

            appendLog("╔════════════════════════════════════════════════════════════════╗");
            appendLog("║ 文件 [" + fileIndex + "/" + totalFiles + "]: " + file.getName());
            appendLog("╚════════════════════════════════════════════════════════════════╝");

            try {
                // 使用新的文件内容提取器
                String nlSpec = FileContentExtractor.extractContent(file);
                String fileType = FileContentExtractor.getFileTypeDescription(file);
                appendLog("📖 已读取" + fileType + "，内容长度: " + nlSpec.length() + " 字符");

                // 调用转换服务
                ToolchainResult result = toolchainService.generateAndValidate(nlSpec, (String) typeComboBox.getSelectedItem(), file.getName());

                // 显示结果
                displayResult(result, file.getName());

            } catch (IOException e) {
                appendLog("❌ 读取文件失败: " + e.getMessage());
                appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
            } catch (Exception e) {
                appendLog("❌ 转换失败: " + e.getMessage());
                appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
                e.printStackTrace();
            }

            appendLog("");
        }

        updateProgress(100, "转换完成");
        finishConversion();
    }

    private void finishConversion() {
        appendLog("========================================");
        appendLog("✅ 批量转换完成");
        appendLog("========================================");

        SwingUtilities.invokeLater(() -> {
            isConverting = false;
            startConversionButton.setEnabled(true);
            stopConversionButton.setEnabled(false);
            addFilesButton.setEnabled(true);
            removeFilesButton.setEnabled(true);
            clearFilesButton.setEnabled(true);
        });
    }

    private void displayResult(ToolchainResult result, String fileName) {
        // 详细信息已经通过进度监听器实时显示
        // 这里只做简单的汇总
        appendLog("");
        appendLog("════════════════════════════════════════════════════════════════");
        appendLog("文件处理完成: " + fileName);
        appendLog("结果: " + (result.success ? "✅ 成功" : "❌ 失败"));
        appendLog("总尝试次数: " + result.attempts.size());
        appendLog("════════════════════════════════════════════════════════════════");
        appendLog("");
    }

    private void stopConversion() {
        if (!isConverting) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要停止转换吗？",
                "确认停止",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            isConverting = false;
            appendLog("\n⚠️ 用户中止了转换过程");
        }
    }

    private void openSettings() {
        LlmConfigManagerDialog dialog = new LlmConfigManagerDialog(this);
        dialog.setVisible(true);

        // 重新加载配置以应用新配置
        if (dialog.isConfigChanged()) {
            // 重置 LlmConfig 单例，强制重新加载配置文件
            LlmConfig.resetInstance();

            // 重新加载 ToolchainService 的配置和 LLM 客户端
            if (toolchainService != null) {
                toolchainService.reloadConfig();
            }

            appendLog("🔄 已重新加载配置");
            LlmConfig.getInstance().printConfig();
        }
    }

    private void openPromptEditor() {
        PromptEditorDialog dialog = new PromptEditorDialog(this);
        dialog.setVisible(true);
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            appendToTextPane(logArea, message + "\n");
            System.out.println(message);
        });
    }

    private void appendDslPreview(String content) {
        SwingUtilities.invokeLater(() -> {
            appendToTextPane(dslPreviewArea, content);
        });
    }

    private void appendError(String message) {
        SwingUtilities.invokeLater(() -> {
            appendToTextPane(errorArea, message);
        });
    }

    /**
     * 向 JTextPane 追加文本的辅助方法
     */
    private void appendToTextPane(JTextPane textPane, String text) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            doc.insertString(doc.getLength(), text, null);
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(int value, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(message);
        });
    }


    /**
     * 创建进度监听器，处理转换过程中的各种事件
     */
    private ConversionProgressListener createProgressListener() {
        return new ConversionProgressListener() {
            private int currentAttempt = 0;

            @Override
            public void onLog(String message) {
                appendLog(message);
            }

            @Override
            public void onAttemptStart(int attemptNumber, int maxAttempts, boolean isRetry) {
                currentAttempt = attemptNumber;
                appendLog("");
                appendLog("╔════════════════════════════════════════════════════════════════╗");
                appendLog("║  第 " + attemptNumber + "/" + maxAttempts + " 次尝试" + (isRetry ? "（修改模式）" : "（生成模式）") + "                                      ║");
                appendLog("╚════════════════════════════════════════════════════════════════╝");
            }

            @Override
            public void onLlmCallStart(String llmName) {
                appendLog("🤖 正在调用 " + llmName + "...");
            }

            @Override
            public void onLlmCallComplete(String rawDsl, long durationMs) {
                appendLog("✅ LLM 调用完成，耗时: " + durationMs + " ms");
                appendLog("");
                appendLog("┌─────────────────────────────────────────────────────────────┐");
                appendLog("│  📝 LLM 原始输出（第 " + currentAttempt + " 次）");
                appendLog("└─────────────────────────────────────────────────────────────┘");

                // 显示原始输出（截取前500字符避免过长）
                String preview = rawDsl;
                if (preview.length() > 500) {
                    preview = preview.substring(0, 500) + "\n... (已截断，完整内容见 DSL 预览)";
                }
                appendLog(preview);
                appendLog("");
            }

            @Override
            public void onDslCleaned(String cleanedDsl) {
                appendLog("🧹 DSL 清理完成");
            }

            @Override
            public void onParseStart() {
                appendLog("🔍 开始语法解析...");
            }

            @Override
            public void onParseComplete(ParseResult result) {
                appendLog("");
                appendLog("┌─────────────────────────────────────────────────────────────┐");
                appendLog("│  🔍 解析结果（第 " + currentAttempt + " 次）");
                appendLog("└─────────────────────────────────────────────────────────────┘");

                if (result.success) {
                    appendLog("✅ 解析成功！");
                    appendLog("📊 解析日志: " + result.logPath);
                    appendLog("🖼️  可视化图: " + result.svgPath);
                } else {
                    appendLog("❌ 解析失败，发现 " + result.errorCount + " 个错误");
                    appendLog("");
                    appendLog("错误详情:");
                    appendLog("─────────────────────────────────────────────────────────────");

                    // 显示前5个错误
                    int errorCount = Math.min(5, result.errors.size());
                    for (int i = 0; i < errorCount; i++) {
                        appendLog((i + 1) + ". " + result.errors.get(i));
                    }
                    if (result.errors.size() > 5) {
                        appendLog("... 还有 " + (result.errors.size() - 5) + " 个错误（查看错误信息标签页）");
                    }
                    appendLog("─────────────────────────────────────────────────────────────");

                    // 在错误标签页显示完整错误
                    appendError("\n========== 第 " + currentAttempt + " 次尝试的错误 ==========\n");
                    for (String error : result.errors) {
                        appendError(error + "\n");
                    }
                    appendError("\n");
                }
                appendLog("");
            }

            @Override
            public void onDslGenerated(String dsl, int attemptNumber) {
                // 在 DSL 预览标签页显示生成的 DSL
                appendDslPreview("\n========== 第 " + attemptNumber + " 次尝试生成的 DSL ==========\n\n");
                appendDslPreview(dsl);
                appendDslPreview("\n\n");
                appendDslPreview("─────────────────────────────────────────────────────────────\n");
            }

            @Override
            public void onError(String error) {
                appendLog("❌ " + error);
                appendError(error + "\n");
            }

            @Override
            public void onSuccess(int attemptNumber, String dslPath, String svgPath) {
                appendLog("");
                appendLog("╔════════════════════════════════════════════════════════════════╗");
                appendLog("║  🎉 转换成功！                                                  ║");
                appendLog("╚════════════════════════════════════════════════════════════════╝");
                appendLog("📄 DSL 文件: " + dslPath);
                appendLog("🖼️  SVG 图形: " + svgPath);
            }

            @Override
            public void onFailure(int totalAttempts, String reason) {
                appendLog("");
                appendLog("╔════════════════════════════════════════════════════════════════╗");
                appendLog("║  ❌ 转换失败                                                    ║");
                appendLog("╚════════════════════════════════════════════════════════════════╝");
                appendLog("🔢 总尝试次数: " + totalAttempts);
                appendLog("📋 失败原因: " + reason);

                appendError("\n========== 最终失败原因 ==========\n");
                appendError(reason + "\n");
            }
        };
    }

    /**
     * 显示统计信息对话框
     */
    private void showStatistics() {
        try {
            ConversionHistory.ConversionStatistics stats = toolchainService.getConversionHistory().getStatistics();

            StringBuilder sb = new StringBuilder();
            sb.append("=== 转换统计信息 ===\n\n");
            sb.append("📊 总转换次数: ").append(stats.getTotalConversions()).append("\n");
            sb.append("✅ 成功次数: ").append(stats.getSuccessCount()).append("\n");
            sb.append("❌ 失败次数: ").append(stats.getFailedCount()).append("\n");
            sb.append("📈 成功率: ").append(String.format("%.1f%%", stats.getSuccessRate() * 100)).append("\n");
            sb.append("🔄 平均尝试次数: ").append(String.format("%.1f", stats.getAverageAttempts())).append("\n\n");

            // 按模型统计
            if (stats.getModelStats() != null && !stats.getModelStats().isEmpty()) {
                sb.append("=== 按模型统计 ===\n");
                stats.getModelStats().forEach((model, modelStats) -> {
                    sb.append("\n📱 ").append(model).append(":\n");
                    sb.append("   成功: ").append(modelStats.getSuccess()).append("\n");
                    sb.append("   失败: ").append(modelStats.getFailed()).append("\n");
                    sb.append("   成功率: ").append(String.format("%.1f%%", modelStats.getSuccessRate() * 100)).append("\n");
                });
                sb.append("\n");
            }

            // 按类型统计
            if (stats.getTypeStats() != null && !stats.getTypeStats().isEmpty()) {
                sb.append("=== 按DSL类型统计 ===\n");
                stats.getTypeStats().forEach((type, typeStats) -> {
                    sb.append("\n📋 ").append(type).append(":\n");
                    sb.append("   成功: ").append(typeStats.getSuccess()).append("\n");
                    sb.append("   失败: ").append(typeStats.getFailed()).append("\n");
                    sb.append("   成功率: ").append(String.format("%.1f%%", typeStats.getSuccessRate() * 100)).append("\n");
                });
            }

            sb.append("\n⏰ 最后更新: ").append(stats.getLastUpdate());

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
            textArea.setRows(20);
            textArea.setColumns(50);

            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "📊 转换统计信息",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "获取统计信息失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // 文件列表单元格渲染器
    private static class FileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                File file = (File) value;
                setText("📄 " + file.getName());
                setToolTipText(file.getAbsolutePath());
            }
            return this;
        }
    }

    public static void main(String[] args) {
        // 设置系统属性，确保正确处理中文
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            DslConverterGUI gui = new DslConverterGUI();
            gui.setVisible(true);
        });
    }
}

