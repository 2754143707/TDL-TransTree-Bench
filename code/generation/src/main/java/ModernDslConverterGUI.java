import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 现代化 DSL 转换器图形界面
 * 采用左侧导航栏 + 右侧内容区的布局设计
 */
public class ModernDslConverterGUI extends JFrame {
    private static final String APP_TITLE = "TDL-TransTree-Bench DSL Converter";
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;

    // 颜色主题
    private static final Color SIDEBAR_BG = new Color(250, 247, 242);
    private static final Color SIDEBAR_SELECTED = new Color(240, 235, 228);
    private static final Color CONTENT_BG = new Color(255, 253, 250);
    private static final Color CARD_BG = new Color(245, 240, 232);
    private static final Color CARD_BORDER = new Color(230, 225, 218);
    private static final Color PRIMARY_TEXT = new Color(60, 55, 50);
    private static final Color SECONDARY_TEXT = new Color(120, 115, 110);
    private static final Color ACCENT_COLOR = new Color(180, 160, 140);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color BUTTON_BG = new Color(255, 255, 255);
    private static final Color BUTTON_BORDER = new Color(200, 195, 188);

    // 字体
    private Font titleFont;
    private Font normalFont;
    private Font smallFont;
    private Font iconFont;

    // 导航项
    private String[] navItems = {"首页", "LLM配置", "提示词编辑", "文件管理", "结果预览", "DSL实例查看"};
    private String[] navIcons = {"", "", "", "", "", ""};
    private JPanel[] navButtons;
    private int selectedNavIndex = 0;

    // 内容面板
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // 各页面面板
    private JPanel homePanel;
    private JPanel dslTypePanel;
    private JPanel llmConfigPanel;
    private JPanel promptConfigPanel;
    private JPanel fileManagePanel;
    private JPanel resultPreviewPanel;
    private JPanel dslInstancesPanel;

    // 组件
    private JList<File> fileList;
    private DefaultListModel<File> fileListModel;
    private JTextPane logArea;
    private JTextPane dslPreviewArea;
    private JTextPane errorArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JComboBox<String> typeComboBox;
    private JComboBox<LlmConfigModel> llmComboBox;
    private JComboBox<String> promptComboBox;

    // 操作按钮
    private JButton retryLimitButton;
    private JButton startButton;
    private JButton stopButton;
    private JButton generateButton;
    private JButton repairButton;

    // 首页统计标签
    private JLabel fileCountLabel;
    private JLabel llmTypeLabel;
    private JLabel successCountLabel;
    private JLabel statusValueLabel;

    // 当前配置显示
    private JLabel currentLlmLabel;
    private JLabel currentPromptLabel;
    private JLabel currentDslTypeLabel;
    private JLabel outputDirLabel;
    private JLabel maxTokensLabel;
    private JLabel temperatureLabel;

    // 统计信息展示（底部模块）
    private JLabel totalConversionsLabel;
    private JLabel totalSuccessesLabel;
    private JLabel totalFailuresLabel;
    private JLabel successRateLabel;
    private JLabel avgAttemptsLabel;
    private JLabel lastUpdatedLabel;

    // 统计数值存储
    private int totalConversions = 0;
    private int totalSuccesses = 0;
    private int totalFailures = 0;
    private int totalAttemptsAccum = 0;

    // 业务逻辑
    private ToolchainService toolchainService;
    private volatile boolean isConverting = false;
    private Thread conversionThread;

    // 最近一次由“单次生成”或修复得到的 DSL（用于修复并重新生成）
    private String lastGeneratedDsl = "";
    private String lastGeneratedType = null;
    private String lastGeneratedSourceFileName = null;
    // 修复重生成的尝试次数计数（单次生成后的修复次数）
    private int repairAttemptCount = 0;

    // 抑制下拉框事件（当我们程序化更新下拉框时，不要触发保存行为）
    private boolean suppressLlmChangeEvents = false;

    // DSL实例查看页组件
    private JComboBox<String> dslInstancesSourceComboBox;
    private DefaultListModel<File> dslInstancesListModel;
    private JList<File> dslInstancesList;
    private JTextPane dslInstancePreviewArea;

    // DSL实例查看：当前目录与返回按钮
    private java.nio.file.Path dslInstancesCurrentDir;
    private JButton dslInstancesBackButton;

    private static final java.nio.file.Path OUTPUT_DIR = java.nio.file.Path.of("output");
    private static final java.nio.file.Path PRESET_DSL_DIR = java.nio.file.Path.of("preset-dsl");

    public ModernDslConverterGUI() {
        super(APP_TITLE);
        initializeFonts();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        initializeServices();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 700));
    }

    private void initializeFonts() {
        String fontName = "Microsoft YaHei UI";
        if (!isFontAvailable(fontName)) {
            fontName = "Microsoft YaHei";
            if (!isFontAvailable(fontName)) {
                fontName = "SimSun";
            }
        }
        titleFont = new Font(fontName, Font.BOLD, 24);
        normalFont = new Font(fontName, Font.PLAIN, 14);
        smallFont = new Font(fontName, Font.PLAIN, 12);
        iconFont = new Font("Segoe UI Emoji", Font.PLAIN, 16);
    }

    private boolean isFontAvailable(String fontName) {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String f : fonts) {
            if (f.equalsIgnoreCase(fontName)) return true;
        }
        return false;
    }

    private void initializeComponents() {
        // 文件列表
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setCellRenderer(new ModernFileListCellRenderer());
        fileList.setBackground(CONTENT_BG);
        fileList.setFont(normalFont);

        // 日志区域
        logArea = createTextPane();
        dslPreviewArea = createTextPane();
        errorArea = createTextPane();
        // 允许用户在错误区域编辑错误信息/补充修改要求
        errorArea.setEditable(true);

        // 进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("就绪");
        progressBar.setFont(smallFont);
        progressBar.setBackground(CARD_BG);
        progressBar.setForeground(ACCENT_COLOR);

        // 状态标签
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(smallFont);
        statusLabel.setForeground(SECONDARY_TEXT);

        // 下拉框
        typeComboBox = createStyledComboBox(new String[]{BenchConstants.TYPE_TRANSLATION_TREE});
        // 动态从配置管理器加载可用的 LLM 配置（使用模型对象显示友好名称，并保留 id）
        java.util.List<LlmConfigModel> llmModels = new java.util.ArrayList<>();
        try {
            LlmConfigManager mgr = LlmConfigManager.getInstance();
            llmModels.addAll(mgr.getEnabledConfigs());
        } catch (Exception ignored) {
        }
        if (llmModels.isEmpty()) {
            // 兜底几个常见的模型（显示名 = id）
            llmModels.add(new LlmConfigModel("gemini", "gemini"));
            llmModels.add(new LlmConfigModel("deepseek", "deepseek"));
            llmModels.add(new LlmConfigModel("qwen", "qwen"));
        }
        llmComboBox = createStyledComboBox(llmModels.toArray(new LlmConfigModel[0]));
        promptComboBox = createStyledComboBox(new String[]{
            BenchConstants.PROMPT_IMAGE_ANNOTATION,
            BenchConstants.PROMPT_GENERATION
        });
        // 当用户选择提示词模板时，同步首页显示的提示词版本
        promptComboBox.addActionListener(e -> {
            Object sel = promptComboBox.getSelectedItem();
            if (sel != null) currentPromptLabel.setText(sel.toString());
        });

        // 操作按钮
        // 修复次数上限设置按钮
        retryLimitButton = createActionButton("修复次数上限", new Color(156, 39, 176));
        // 更新为新文案：
        startButton = createActionButton("自动化生成与修复", new Color(76, 175, 80));
        // 停止按钮改为更醒目的红色并加粗字体
        // 改为深灰背景，白字，非加粗
        stopButton = createActionButton("停止生成", new Color(68, 68, 68));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFont(normalFont);
        generateButton = createActionButton("单次DSL实例生成", new Color(33, 150, 243));
        repairButton = createActionButton("修复并重新生成", new Color(255, 152, 0));
        stopButton.setEnabled(false);

        // 首页统计标签
        fileCountLabel = new JLabel("0");
        llmTypeLabel = new JLabel("gemini");
        successCountLabel = new JLabel("0");
        statusValueLabel = new JLabel("正常");

        // 当前配置标签
        currentLlmLabel = new JLabel("gemini");
        currentPromptLabel = new JLabel("");
        currentDslTypeLabel = new JLabel(BenchConstants.TYPE_TRANSLATION_TREE);
        outputDirLabel = new JLabel("/output/");
        maxTokensLabel = new JLabel("4096");
        temperatureLabel = new JLabel("0.7");

        // 统计信息展示默认值
        totalConversionsLabel = new JLabel("0");
        totalSuccessesLabel = new JLabel("0");
        totalFailuresLabel = new JLabel("0");
        successRateLabel = new JLabel("0%");
        avgAttemptsLabel = new JLabel("0.0");
        lastUpdatedLabel = new JLabel("-");

        // DSL实例查看页
        dslInstancesSourceComboBox = createStyledComboBox(new String[]{
            "输出的DSL实例（output）",
            "预制的DSL实例（preset-dsl）"
        });
        dslInstancesListModel = new DefaultListModel<>();
        dslInstancesList = new JList<>(dslInstancesListModel);
        dslInstancesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dslInstancesList.setFont(normalFont);
        dslInstancesList.setBackground(CONTENT_BG);

        dslInstancePreviewArea = createTextPane();
        dslInstancePreviewArea.setEditable(false);

        dslInstancesBackButton = new JButton("返回上一级");
        dslInstancesBackButton.setFont(normalFont);
        dslInstancesBackButton.setBackground(BUTTON_BG);
        dslInstancesBackButton.setForeground(PRIMARY_TEXT);
        dslInstancesBackButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        dslInstancesBackButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 默认目录：output 根
        dslInstancesCurrentDir = OUTPUT_DIR;
        refreshDslInstancesList();
    }


    private <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(normalFont);
        combo.setBackground(BUTTON_BG);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return combo;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(normalFont);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(160, 45));
        
        // 圆角效果
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        return button;
    }

    private JTextPane createTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(normalFont);
        textPane.setBackground(CONTENT_BG);
        textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return textPane;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(CONTENT_BG);

        // 左侧导航栏
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 右侧内容区
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(CONTENT_BG);

        // 创建各页面
        homePanel = createHomePanel();
        // dslTypePanel = createDslTypePanel(); // Removed
        llmConfigPanel = createLlmConfigPanel();
        promptConfigPanel = createPromptConfigPanel();
        fileManagePanel = createFileManagePanel();
        resultPreviewPanel = createResultPreviewPanel();
        dslInstancesPanel = createDslInstancesPanel();

        contentPanel.add(homePanel, "首页");
        // contentPanel.add(dslTypePanel, "DSL类型"); // Removed
        contentPanel.add(llmConfigPanel, "LLM配置");
        contentPanel.add(promptConfigPanel, "提示词编辑");
        contentPanel.add(fileManagePanel, "文件管理");
        contentPanel.add(resultPreviewPanel, "结果预览");
        contentPanel.add(dslInstancesPanel, "DSL实例查看");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        // 启动时在控制台打印一次导航项，便于排查“看不到 DSL实例查看”是否因为运行了旧 jar
        try {
            System.out.println("[ModernGui] navItems=" + java.util.Arrays.toString(navItems));
        } catch (Exception ignored) {
        }
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CARD_BORDER));

        // 顶部Logo区域
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel logoLabel = new JLabel("Link16 DSL 转换工具");
        logoLabel.setFont(new Font(normalFont.getFamily(), Font.BOLD, 14));
        logoLabel.setForeground(PRIMARY_TEXT);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoPanel.add(logoLabel);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        // 导航菜单
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(SIDEBAR_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        navButtons = new JPanel[navItems.length];
        for (int i = 0; i < navItems.length; i++) {
            navButtons[i] = createNavButton(navIcons[i], navItems[i], i);
            navPanel.add(navButtons[i]);
            navPanel.add(Box.createVerticalStrut(5));
        }

        sidebar.add(navPanel, BorderLayout.CENTER);

        // 底部操作按钮
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(SIDEBAR_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 20, 15));

        // 调整按钮大小
        Dimension buttonSize = new Dimension(170, 40);
        retryLimitButton.setMaximumSize(buttonSize);
        retryLimitButton.setPreferredSize(buttonSize);
        startButton.setMaximumSize(buttonSize);
        startButton.setPreferredSize(buttonSize);
        stopButton.setMaximumSize(buttonSize);
        stopButton.setPreferredSize(buttonSize);
        generateButton.setMaximumSize(buttonSize);
        generateButton.setPreferredSize(buttonSize);
        repairButton.setMaximumSize(buttonSize);
        repairButton.setPreferredSize(buttonSize);

        retryLimitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        generateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        repairButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(retryLimitButton);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(startButton);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(stopButton);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(generateButton);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(repairButton);

        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createNavButton(String icon, String text, int index) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(index == selectedNavIndex ? SIDEBAR_SELECTED : SIDEBAR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(normalFont);
        textLabel.setForeground(PRIMARY_TEXT);

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(textLabel);

        panel.add(contentPanel, BorderLayout.CENTER);

        // 选中指示器
        if (index == selectedNavIndex) {
            JPanel indicator = new JPanel();
            indicator.setBackground(ACCENT_COLOR);
            indicator.setPreferredSize(new Dimension(4, 0));
            panel.add(indicator, BorderLayout.WEST);
        }

        // 点击事件
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectNavItem(index);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (index != selectedNavIndex) {
                    panel.setBackground(SIDEBAR_SELECTED);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (index != selectedNavIndex) {
                    panel.setBackground(SIDEBAR_BG);
                }
            }
        });

        return panel;
    }

    private void selectNavItem(int index) {
        // 更新选中状态
        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].setBackground(i == index ? SIDEBAR_SELECTED : SIDEBAR_BG);
            navButtons[i].removeAll();
            
            JLabel iconLabel = new JLabel(navIcons[i]);
            iconLabel.setFont(iconFont);
            iconLabel.setForeground(PRIMARY_TEXT);

            JLabel textLabel = new JLabel("  " + navItems[i]);
            textLabel.setFont(normalFont);
            textLabel.setForeground(PRIMARY_TEXT);

            JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            contentPanel.setOpaque(false);
            contentPanel.add(iconLabel);
            contentPanel.add(textLabel);

            navButtons[i].add(contentPanel, BorderLayout.CENTER);

            if (i == index) {
                JPanel indicator = new JPanel();
                indicator.setBackground(ACCENT_COLOR);
                indicator.setPreferredSize(new Dimension(4, 0));
                navButtons[i].add(indicator, BorderLayout.WEST);
            }
            
            navButtons[i].revalidate();
            navButtons[i].repaint();
        }

        selectedNavIndex = index;
        cardLayout.show(this.contentPanel, navItems[index]);
    }


    // ==================== 首页面板 ====================
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 顶部标题和状态
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Link16 DSL 转换工具 首页");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PRIMARY_TEXT);

        JLabel subtitleLabel = new JLabel("欢迎使用，高效管理您的 DSL 转换流程");
        subtitleLabel.setFont(smallFont);
        subtitleLabel.setForeground(SECONDARY_TEXT);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // 状态栏
        JPanel statusBar = createStatusBar();

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statusBar, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // 中间内容
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        // 统计卡片行
        JPanel statsPanel = createStatsPanel();
        centerPanel.add(statsPanel);

        centerPanel.add(Box.createVerticalStrut(30));

        // 当前配置面板
        JPanel configPanel = createCurrentConfigPanel();
        centerPanel.add(configPanel);

        // 底部统计信息展示模块（样式类似于当前配置面板）
        JPanel statsModule = createStatisticsPanel();
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(statsModule);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        panel.setOpaque(false);

        // 仅显示当前 LLM 名称（从首页同步），隐藏服务/端口状态
        currentLlmLabel.setFont(smallFont);
        currentLlmLabel.setForeground(SECONDARY_TEXT);
        panel.add(currentLlmLabel);

         return panel;
     }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        panel.add(createStatCard("文件数", fileCountLabel, "待转换文件数量"));
        panel.add(createStatCard("LLM类型", llmTypeLabel, "当前使用的模型"));
        panel.add(createStatCard("转换成功", successCountLabel, "成功转换数量"));
        panel.add(createStatCard("配置状态", statusValueLabel, "系统配置状态"));

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String tooltip) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setToolTipText(tooltip);

        // 内容
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(smallFont);
        titleLabel.setForeground(SECONDARY_TEXT);

        valueLabel.setFont(new Font(normalFont.getFamily(), Font.BOLD, 20));
        valueLabel.setForeground(PRIMARY_TEXT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(valueLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createCurrentConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel titleLabel = new JLabel("当前配置");
        titleLabel.setFont(new Font(normalFont.getFamily(), Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_TEXT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        panel.add(titleLabel, BorderLayout.NORTH);

        // 配置项网格
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 30, 15));
        gridPanel.setOpaque(false);

        gridPanel.add(createConfigItem("LLM模型", currentLlmLabel));
        gridPanel.add(createConfigItem("提示词模板", currentPromptLabel));
        gridPanel.add(createConfigItem("DSL类型", currentDslTypeLabel));
        gridPanel.add(createConfigItem("输出目录", outputDirLabel));
        gridPanel.add(createConfigItem("最大Token数", maxTokensLabel));
        gridPanel.add(createConfigItem("温度", temperatureLabel));

        panel.add(gridPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createConfigItem(String label, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(smallFont);
        labelComp.setForeground(SECONDARY_TEXT);

        valueLabel.setFont(new Font(normalFont.getFamily(), Font.BOLD, 14));
        valueLabel.setForeground(PRIMARY_TEXT);

        panel.add(labelComp);
        panel.add(Box.createVerticalStrut(3));
        panel.add(valueLabel);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        // 再次增加高度以保证字段文本完整显示（避免换行/截断）
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        panel.setPreferredSize(new Dimension(0, 280));

        JLabel title = new JLabel("统计信息展示");
        title.setFont(new Font(normalFont.getFamily(), Font.BOLD, 16));
        title.setForeground(PRIMARY_TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 8));
        grid.setOpaque(false);

        grid.add(createConfigItem("总转换次数", totalConversionsLabel));
        grid.add(createConfigItem("成功次数", totalSuccessesLabel));
        grid.add(createConfigItem("失败次数", totalFailuresLabel));
        grid.add(createConfigItem("成功率", successRateLabel));
        grid.add(createConfigItem("平均尝试次数", avgAttemptsLabel));
        grid.add(createConfigItem("最后更新", lastUpdatedLabel));

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private void refreshStatisticsLabels() {
        SwingUtilities.invokeLater(() -> {
            totalConversionsLabel.setText(String.valueOf(totalConversions));
            totalSuccessesLabel.setText(String.valueOf(totalSuccesses));
            totalFailuresLabel.setText(String.valueOf(totalFailures));
            double rate = totalConversions == 0 ? 0.0 : (double) totalSuccesses / totalConversions * 100.0;
            successRateLabel.setText(String.format("%.1f%%", rate));
            double avg = totalConversions == 0 ? 0.0 : (double) totalAttemptsAccum / totalConversions;
            avgAttemptsLabel.setText(String.format("%.2f", avg));
            // 使用更友好的本地时间格式显示最后更新时间
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            lastUpdatedLabel.setText(now);
        });
    }

    /**
     * 从 ConversionHistory（本地 JSON 持久化）加载统计，填充到首页统计展示。
     */
    private void loadAndDisplayPersistedStatistics() {
        try {
            if (toolchainService == null || toolchainService.getConversionHistory() == null) return;
            ConversionHistory.ConversionStatistics stats = toolchainService.getConversionHistory().getStatistics();
            if (stats == null) return;

            totalConversions = stats.getTotalConversions();
            totalSuccesses = stats.getSuccessCount();
            totalFailures = stats.getFailedCount();

            // Modern GUI 需要总尝试次数来算平均尝试数；历史统计里已提供 averageAttempts，所以反推一个累计值用于展示即可
            double avg = stats.getAverageAttempts();
            if (avg < 0) avg = 0;
            totalAttemptsAccum = (int) Math.round(avg * Math.max(0, totalConversions));

            refreshStatisticsLabels();

            // 单独设置 lastUpdatedLabel，确保展示的就是历史统计更新时间
            SwingUtilities.invokeLater(() -> {
                try {
                    if (stats.getLastUpdate() != null) {
                        lastUpdatedLabel.setText(stats.getLastUpdate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
            // 统计展示不应影响主流程
        }
    }

    /**
     * 将给定 LLM 模型的关键参数同步到首页显示（最大Token、温度、输出目录、显示名等）。
     */
    private void updateHomeLlmDetails(LlmConfigModel model) {
        if (model == null) return;
        SwingUtilities.invokeLater(() -> {
            try { currentLlmLabel.setText(model.getName()); } catch (Exception ignored) {}
            try { llmTypeLabel.setText(model.getId().toLowerCase()); } catch (Exception ignored) {}
            // 使用配置管理器读取原始字符串，保留用户输入的位数/格式
            try {
                LlmConfigManager mgr = LlmConfigManager.getInstance();
                String tempKey = "llm." + model.getId() + ".temperature";
                String tokensKey = "llm." + model.getId() + ".maxTokens";
                String tempRaw = mgr.getRawProperty(tempKey, String.valueOf(model.getTemperature()));
                String tokensRaw = mgr.getRawProperty(tokensKey, String.valueOf(model.getMaxTokens()));
                temperatureLabel.setText(formatTemperature(tempRaw, model.getTemperature()));
                maxTokensLabel.setText(tokensRaw);
            } catch (Exception ignored) {}
            // 图片标注模型使用 annotated-images 输出目录
            try {
                if ("doubao-image".equals(model.getId()) || "gemini-image".equals(model.getId())) {
                    outputDirLabel.setText("output\\annotated-images");
                } else {
                    outputDirLabel.setText("output");
                }
            } catch (Exception ignored) {}
        });
    }


    // ==================== DSL类型面板 ====================
    private JPanel createDslTypePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 标题
        JLabel titleLabel = new JLabel("DSL类型配置");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PRIMARY_TEXT);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 内容
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        // DSL类型选择卡片
        JPanel typeCard = new JPanel(new BorderLayout());
        typeCard.setBackground(CARD_BG);
        typeCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        typeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel typeTitle = new JLabel("选择DSL类型");
        typeTitle.setFont(new Font(normalFont.getFamily(), Font.BOLD, 16));
        typeTitle.setForeground(PRIMARY_TEXT);

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        selectPanel.setOpaque(false);
        
        typeComboBox.setPreferredSize(new Dimension(300, 40));
        selectPanel.add(typeComboBox);

        JPanel typeContentPanel = new JPanel(new BorderLayout());
        typeContentPanel.setOpaque(false);
        typeContentPanel.add(typeTitle, BorderLayout.NORTH);
        typeContentPanel.add(selectPanel, BorderLayout.CENTER);

        typeCard.add(typeContentPanel, BorderLayout.CENTER);

        // 类型说明
        JPanel descCard = createDescriptionCard("DSL类型说明", 
            "• 功能模型：用于描述系统功能结构的DSL\n" +
            "• 消息规则：用于定义消息处理规则和业务触发条件的DSL\n" +
            "• 消息转发：翻译树DSL，支持图片标注功能");
        // 调整说明框高度：卡片底端略低于“消息转发”那一行，且文本不会被遮挡
        descCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        descCard.setPreferredSize(new Dimension(0, 110));
        // 给内容文本底部留少量内边距，防止文字被裁切
        Component centerComp = descCard.getComponent(1);
        if (centerComp instanceof JTextPane) {
            ((JTextPane) centerComp).setBorder(BorderFactory.createEmptyBorder(0, 8, 6, 8));
        } else if (centerComp instanceof JPanel) {
            for (Component c : ((JPanel) centerComp).getComponents()) {
                if (c instanceof JTextPane) {
                    ((JTextPane) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 6, 8));
                    break;
                }
            }
        }

        contentPanel.add(typeCard);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(descCard);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==================== LLM配置面板 ====================
    private JPanel createLlmConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 标题
        JLabel titleLabel = new JLabel("LLM配置");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PRIMARY_TEXT);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 内容
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        // LLM选择卡片
        JPanel llmCard = new JPanel(new BorderLayout());
        llmCard.setBackground(CARD_BG);
        llmCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        llmCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel llmTitle = new JLabel("选择LLM模型");
        llmTitle.setFont(new Font(normalFont.getFamily(), Font.BOLD, 16));
        llmTitle.setForeground(PRIMARY_TEXT);

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        selectPanel.setOpaque(false);
        
        llmComboBox.setPreferredSize(new Dimension(380, 40));
        selectPanel.add(llmComboBox);

        JButton configButton = new JButton("详细配置");
        configButton.setFont(normalFont);
        configButton.setBackground(BUTTON_BG);
        configButton.setForeground(PRIMARY_TEXT);
        configButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        configButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        configButton.addActionListener(e -> openSettings());
        selectPanel.add(configButton);

        JPanel llmContentPanel = new JPanel(new BorderLayout());
        llmContentPanel.setOpaque(false);
        llmContentPanel.add(llmTitle, BorderLayout.NORTH);
        llmContentPanel.add(selectPanel, BorderLayout.CENTER);

        llmCard.add(llmContentPanel, BorderLayout.CENTER);

        // LLM说明
        JPanel descCard = createDescriptionCard("支持的LLM模型", 
            "• Gemini Pro：Google 出品，适用于 DSL 生成；版本: Gemini Claud Sonnet 4.5（适合复杂文本生成）\n" +
            "• DeepSeek V3：DeepSeek 平台，适用于高性价比的 DSL 生成与对话；版本: deepseek-v3\n" +
            "• Doubao-Seed-1.6-flash：字节跳动（Doubao）DSL 生成模型，适用于高精度 DSL 生成；版本: Seed-1.6-flash\n" +
            "• Qwen Plus：阿里通义（Qwen）增强版，适合中文理解和生成；版本: qwen-plus\n" +
            "• Gemini 3 Pro Image Preview：Google Gemini 图像标注模型，专用于翻译树图片标注；版本: Gemini 3 Pro Image Preview\n" +
            "• Doubao-Seedream-4.5：字节跳动 Doubao 提供的图像标注模型，擅长视觉理解与像素级/区域标注，适用于翻译树的图片标注场景；版本: Seedream-4.5；用途: 翻译树图像标注。\n\n" +
             "说明：以上模型显示名称来自配置文件（config/llm-config.properties），\n" +
             "类型说明：dsl-generation = 用于文本DSL生成；image-annotation = 用于翻译树的图片标注。\n" +
             "如需添加/编辑模型请点击“详细配置”进行管理（支持添加/编辑/删除/设为激活/刷新）");

        // 文本已经简化，缩小说明框高度以保持界面紧凑
        // 文本底部被略微遮挡，稍微增大说明框底部空间
        descCard.setPreferredSize(new Dimension(0, 180));
        descCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

         contentPanel.add(llmCard);
         contentPanel.add(Box.createVerticalStrut(20));
         contentPanel.add(descCard);

         panel.add(contentPanel, BorderLayout.CENTER);

         return panel;
     }

    // ==================== 提示词配置面板 ====================
    private JPanel createPromptConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 标题
        JLabel titleLabel = new JLabel("提示词编辑");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PRIMARY_TEXT);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 内容
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        // 提示词选择卡片
        JPanel promptCard = new JPanel(new BorderLayout());
        promptCard.setBackground(CARD_BG);
        promptCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        promptCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel promptTitle = new JLabel("选择提示词模板");
        promptTitle.setFont(new Font(normalFont.getFamily(), Font.BOLD, 16));
        promptTitle.setForeground(PRIMARY_TEXT);

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        selectPanel.setOpaque(false);
        
        promptComboBox.setPreferredSize(new Dimension(300, 40));
        selectPanel.add(promptComboBox);

        JButton editButton = new JButton("编辑提示词");
        editButton.setFont(normalFont);
        editButton.setBackground(BUTTON_BG);
        editButton.setForeground(PRIMARY_TEXT);
        editButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> openPromptEditor());
        selectPanel.add(editButton);

        JPanel promptContentPanel = new JPanel(new BorderLayout());
        promptContentPanel.setOpaque(false);
        promptContentPanel.add(promptTitle, BorderLayout.NORTH);
        promptContentPanel.add(selectPanel, BorderLayout.CENTER);

        promptCard.add(promptContentPanel, BorderLayout.CENTER);

        // 提示词说明
        JPanel descCard = createDescriptionCard("提示词说明", 
            "提示词用于指导LLM生成符合语法的DSL代码。\n\n" +
            "• 可以在提示词中添加示例、语法规则、约束条件等\n" +
            "• 修改后请点击保存按钮，重新加载配置后生效\n" +
            "• 提示词文件位于 config/prompts/ 目录下");

        contentPanel.add(promptCard);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(descCard);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDescriptionCard(String title, String content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(normalFont.getFamily(), Font.BOLD, 14));
        titleLabel.setForeground(PRIMARY_TEXT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // 使用JTextPane支持行间距设置
        JTextPane contentPane = new JTextPane();
        contentPane.setFont(new Font(normalFont.getFamily(), Font.PLAIN, 13));
        contentPane.setForeground(SECONDARY_TEXT);
        contentPane.setBackground(CARD_BG);
        contentPane.setEditable(false);
        contentPane.setText(content);
        
        // 设置行间距
        StyledDocument doc = contentPane.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(attrs, 0.4f);
        doc.setParagraphAttributes(0, doc.getLength(), attrs, false);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentPane, BorderLayout.CENTER);

        return card;
    }


    // ==================== 文件管理面板 ====================
    private JPanel createFileManagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 标题
        JLabel titleLabel = new JLabel("文件管理");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PRIMARY_TEXT);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 内容
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        // 上传区域（支持拖拽）
        JPanel uploadPanel = createDragDropPanel();
        contentPanel.add(uploadPanel, BorderLayout.NORTH);

        // 文件列表
        JPanel fileListPanel = createFileListPanel();
        contentPanel.add(fileListPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDragDropPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(ACCENT_COLOR, 2, 5, 5, true),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        panel.setPreferredSize(new Dimension(0, 190));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel mainLabel = new JLabel("拖拽文件到此处上传");
        mainLabel.setFont(new Font(normalFont.getFamily(), Font.PLAIN, 15));
        mainLabel.setForeground(PRIMARY_TEXT);
        mainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("支持 .txt, .png, .pdf, .md 格式");
        subLabel.setFont(smallFont);
        subLabel.setForeground(SECONDARY_TEXT);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton selectFileButton = new JButton("选择文件");
        selectFileButton.setFont(normalFont);
        selectFileButton.setBackground(BUTTON_BG);
        selectFileButton.setForeground(PRIMARY_TEXT);
        selectFileButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        selectFileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectFileButton.addActionListener(e -> addFiles());

        JButton selectFolderButton = new JButton("选择文件夹");
        selectFolderButton.setFont(normalFont);
        selectFolderButton.setBackground(BUTTON_BG);
        selectFolderButton.setForeground(PRIMARY_TEXT);
        selectFolderButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        selectFolderButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectFolderButton.addActionListener(e -> addFolder());

        buttonPanel.add(selectFileButton);
        buttonPanel.add(selectFolderButton);

        centerPanel.add(mainLabel);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(subLabel);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(buttonPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // 设置拖拽支持
        setupDragAndDrop(panel);

        return panel;
    }

    private void setupDragAndDrop(JPanel panel) {
        new DropTarget(panel, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                panel.setBackground(new Color(235, 230, 222));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createDashedBorder(new Color(150, 130, 110), 3, 5, 5, true),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)
                ));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                panel.setBackground(CARD_BG);
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createDashedBorder(ACCENT_COLOR, 2, 5, 5, true),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)
                ));
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    
                    @SuppressWarnings("unchecked")
                    List<File> droppedFiles = (List<File>) dtde.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);
                    
                    int addedCount = 0;
                    for (File file : droppedFiles) {
                        if (file.isDirectory()) {
                            addedCount += addFilesFromFolder(file);
                        } else if (isValidFile(file)) {
                            if (!fileListModel.contains(file)) {
                                fileListModel.addElement(file);
                                addedCount++;
                            }
                        }
                    }
                    
                    updateFileCount();
                    updateStatus("已添加 " + addedCount + " 个文件");
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    panel.setBackground(CARD_BG);
                    panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createDashedBorder(ACCENT_COLOR, 2, 5, 5, true),
                        BorderFactory.createEmptyBorder(30, 30, 30, 30)
                    ));
                }
            }
        });
    }

    private boolean isValidFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".txt") || name.endsWith(".png") || 
               name.endsWith(".pdf") || name.endsWith(".md") ||
               name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    private int addFilesFromFolder(File folder) {
        int count = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += addFilesFromFolder(file);
                } else if (isValidFile(file)) {
                    if (!fileListModel.contains(file)) {
                        fileListModel.addElement(file);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private JPanel createFileListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // 标题栏
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel listTitle = new JLabel("待转换文件列表");
        listTitle.setFont(new Font(normalFont.getFamily(), Font.BOLD, 14));
        listTitle.setForeground(PRIMARY_TEXT);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton removeButton = new JButton("移除选中");
        removeButton.setFont(smallFont);
        removeButton.setBackground(BUTTON_BG);
        removeButton.setForeground(PRIMARY_TEXT);
        removeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeButton.addActionListener(e -> removeSelectedFiles());

        JButton clearButton = new JButton("清空列表");
        clearButton.setFont(smallFont);
        clearButton.setBackground(BUTTON_BG);
        clearButton.setForeground(PRIMARY_TEXT);
        clearButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearFileList());

        Dimension actionButtonSize = new Dimension(100, 28);
        removeButton.setPreferredSize(actionButtonSize);
        clearButton.setPreferredSize(actionButtonSize);

        actionPanel.add(removeButton);
        actionPanel.add(clearButton);

        headerPanel.add(listTitle, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // 文件列表
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(CONTENT_BG);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }


    // ==================== 结果预览面板 ====================
    private JPanel createResultPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 标题
        JLabel titleLabel = new JLabel("结果预览");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PRIMARY_TEXT);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 内容
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // 标签页面板
        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.setBackground(CARD_BG);
        tabPanel.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));

        // 自定义标签页头部
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setBackground(CARD_BG);
        tabHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        String[] tabNames = {"转换日志", "DSL预览", "错误信息"};
        JTextPane[] tabContents = {logArea, dslPreviewArea, errorArea};
        JPanel[] tabButtons = new JPanel[3];
        JPanel tabContentPanel = new JPanel(new CardLayout());
        tabContentPanel.setBackground(CONTENT_BG);

        for (int i = 0; i < tabNames.length; i++) {
            final int index = i;
            JPanel tabButton = new JPanel(new BorderLayout());
            tabButton.setBackground(i == 0 ? CONTENT_BG : CARD_BG);
            tabButton.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
            tabButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel tabLabel = new JLabel(tabNames[i]);
            tabLabel.setFont(normalFont);
            tabLabel.setForeground(i == 0 ? PRIMARY_TEXT : SECONDARY_TEXT);
            tabButton.add(tabLabel, BorderLayout.CENTER);

            tabButtons[i] = tabButton;

            // 添加到 tabHeader，恢复可见的标签栏
            tabHeader.add(tabButton);

            tabButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (int j = 0; j < tabButtons.length; j++) {
                        tabButtons[j].setBackground(j == index ? CONTENT_BG : CARD_BG);
                        ((JLabel)tabButtons[j].getComponent(0)).setForeground(j == index ? PRIMARY_TEXT : SECONDARY_TEXT);
                    }
                    ((CardLayout)tabContentPanel.getLayout()).show(tabContentPanel, tabNames[index]);
                }
            });

            // 添加内容
            JScrollPane scrollPane = new JScrollPane(tabContents[i]);
            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(CONTENT_BG);
            tabContentPanel.add(scrollPane, tabNames[i]);
        }

        tabPanel.add(tabHeader, BorderLayout.NORTH);
        tabPanel.add(tabContentPanel, BorderLayout.CENTER);

        contentPanel.add(tabPanel, BorderLayout.CENTER);

        // 底部状态栏
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        progressBar.setPreferredSize(new Dimension(0, 25));
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==================== DSL实例查看页面 ====================
    private JPanel createDslInstancesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("DSL实例查看");
        title.setFont(titleFont);
        title.setForeground(PRIMARY_TEXT);
        header.add(title, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);
        dslInstancesSourceComboBox.setPreferredSize(new Dimension(260, 36));
        rightActions.add(new JLabel("数据源:"));
        rightActions.add(dslInstancesSourceComboBox);
        rightActions.add(dslInstancesBackButton);

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setFont(normalFont);
        refreshBtn.setBackground(BUTTON_BG);
        refreshBtn.setForeground(PRIMARY_TEXT);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshDslInstancesList());
        rightActions.add(refreshBtn);

        header.add(rightActions, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(CARD_BG);
        left.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        left.setPreferredSize(new Dimension(420, 0));

        JLabel listTitle = new JLabel("文件列表（文件夹可进入）");
        listTitle.setFont(new Font(normalFont.getFamily(), Font.BOLD, 13));
        listTitle.setForeground(PRIMARY_TEXT);
        listTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        left.add(listTitle, BorderLayout.NORTH);

        dslInstancesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    File f = (File) value;
                    setText((f.isDirectory() ? "[文件夹] " : "") + f.getName());
                }
                setFont(normalFont);
                setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                if (isSelected) {
                    setBackground(SIDEBAR_SELECTED);
                    setForeground(PRIMARY_TEXT);
                } else {
                    setBackground(CONTENT_BG);
                    setForeground(PRIMARY_TEXT);
                }
                return this;
            }
        });

        JScrollPane listScroll = new JScrollPane(dslInstancesList);
        listScroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        listScroll.getViewport().setBackground(CONTENT_BG);
        left.add(listScroll, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(CARD_BG);
        right.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel previewTitle = new JLabel("内容预览");
        previewTitle.setFont(new Font(normalFont.getFamily(), Font.BOLD, 13));
        previewTitle.setForeground(PRIMARY_TEXT);
        previewTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        right.add(previewTitle, BorderLayout.NORTH);

        JScrollPane previewScroll = new JScrollPane(dslInstancePreviewArea);
        previewScroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        previewScroll.getViewport().setBackground(CONTENT_BG);
        right.add(previewScroll, BorderLayout.CENTER);

        body.add(left, BorderLayout.WEST);
        body.add(right, BorderLayout.CENTER);

        panel.add(body, BorderLayout.CENTER);

        if (dslInstancePreviewArea.getText() == null || dslInstancePreviewArea.getText().isBlank()) {
            dslInstancePreviewArea.setText("请选择左侧文件预览，或点击文件夹进入。\n\n数据源可切换：output / preset-dsl");
        }

        return panel;
    }

    private void refreshDslInstancesList() {
        SwingUtilities.invokeLater(() -> {
            dslInstancesListModel.clear();

            java.nio.file.Path root = getSelectedDslInstancesRootDir();
            if (dslInstancesCurrentDir == null) {
                dslInstancesCurrentDir = root;
            }

            // 切换数据源时，强制回到该数据源根目录
            try {
                if (!dslInstancesCurrentDir.toAbsolutePath().normalize().startsWith(root.toAbsolutePath().normalize())) {
                    dslInstancesCurrentDir = root;
                }
            } catch (Exception ignored) {
                dslInstancesCurrentDir = root;
            }

            File baseDir = dslInstancesCurrentDir.toFile();
            if (!baseDir.exists() || !baseDir.isDirectory()) {
                dslInstancePreviewArea.setText("目录不存在或不可用：\n" + baseDir.getAbsolutePath());
                return;
            }

            // 返回按钮：仅在 preset-dsl 且不在根目录时启用
            boolean enableBack = false;
            try {
                enableBack = root.equals(PRESET_DSL_DIR) && !dslInstancesCurrentDir.equals(root);
            } catch (Exception ignored) {
            }
            dslInstancesBackButton.setEnabled(enableBack);

            java.util.List<File> dirs = new java.util.ArrayList<>();
            java.util.List<File> files = new java.util.ArrayList<>();
            File[] listed = baseDir.listFiles();
            if (listed != null) {
                for (File f : listed) {
                    if (f.isDirectory()) dirs.add(f);
                    else if (f.isFile()) files.add(f);
                }
            }
            dirs.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            files.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

            for (File d : dirs) dslInstancesListModel.addElement(d);
            for (File f : files) dslInstancesListModel.addElement(f);

            if (dslInstancesListModel.isEmpty()) {
                dslInstancePreviewArea.setText("该目录下暂无内容：\n" + baseDir.getAbsolutePath());
            } else {
                dslInstancePreviewArea.setText("当前目录：\n" + baseDir.getAbsolutePath() + "\n\n" +
                    "提示：\n- 点击文件夹进入\n- 点击文件预览\n" +
                    (enableBack ? "- 可点击“返回上一级”回到分类根目录\n" : ""));
            }
        });
    }

    private java.nio.file.Path getSelectedDslInstancesRootDir() {
        Object sel = dslInstancesSourceComboBox.getSelectedItem();
        if (sel != null && sel.toString().contains("preset-dsl")) {
            return PRESET_DSL_DIR;
        }
        return OUTPUT_DIR;
    }

    // 兼容旧方法名
    private java.nio.file.Path getSelectedDslInstancesDir() {
        return getSelectedDslInstancesRootDir();
    }

    /**
     * DSL实例查看：预览选中文件内容
     */
    private void showDslInstanceFile(File file) {
        SwingUtilities.invokeLater(() -> {
            if (file == null) {
                dslInstancePreviewArea.setText("");
                return;
            }
            if (!file.exists() || !file.isFile()) {
                dslInstancePreviewArea.setText("不是可预览的文件：\n" + file.getAbsolutePath());
                return;
            }
            try {
                String content = java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
                dslInstancePreviewArea.setText("文件：" + file.getAbsolutePath() + "\n\n" + (content == null ? "" : content));
                dslInstancePreviewArea.setCaretPosition(0);
            } catch (Exception ex) {
                dslInstancePreviewArea.setText("读取失败：" + ex.getMessage() + "\n\n" + file.getAbsolutePath());
            }
        });
    }

    private void setupEventHandlers() {
        // 修复次数上限设置
        retryLimitButton.addActionListener(e -> openRetryLimitDialog());

        // 开始转换
        startButton.addActionListener(e -> startConversion());

        // 停止转换
        stopButton.addActionListener(e -> stopConversion());

        // 生成按钮（单次DSL实例生成）
        generateButton.addActionListener(e -> {
            // 单次生成：使用所选文件或弹窗输入自然语言需求
            if (toolchainService == null) {
                JOptionPane.showMessageDialog(this, "服务未初始化", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 点击后切换到结果预览页面（与“自动化生成与修复”一致）
            selectNavItem(5);

            // 新的单次生成开始，重置修复次数计数
            repairAttemptCount = 0;

            String type = (String) typeComboBox.getSelectedItem();
            String nlSpec = null;
            String sourceFileName = null;

            // 若有选中文件则读取第一个文件的内容作为输入
            if (!fileListModel.isEmpty()) {
                File file = fileListModel.getElementAt(0);
                try {
                    nlSpec = FileContentExtractor.extractContent(file);
                    sourceFileName = file.getName();
                } catch (Exception ex) {
                    appendLog("读取文件失败: " + ex.getMessage());
                    nlSpec = null;
                }
            }

            if (nlSpec == null || nlSpec.isBlank()) {
                // 询问用户输入自然语言需求
                nlSpec = JOptionPane.showInputDialog(this, "请输入自然语言需求用于生成DSL:", "输入需求", JOptionPane.PLAIN_MESSAGE);
                if (nlSpec == null) return; // 用户取消
            }

            // 禁用按钮防止重复点击
            generateButton.setEnabled(false);
            repairButton.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            // 使用 determinate 进度风格，与自动化生成保持一致
            updateProgress(0, "单次生成中...");
            statusLabel.setText("单次生成中...");

            final String finalNl = nlSpec;
            final String finalType = type;
            final String finalSourceFile = sourceFileName;

            new Thread(() -> {
                try {
                    ToolchainResult res = toolchainService.generateSingleDslInstance(finalNl, finalType, finalSourceFile);
                    SwingUtilities.invokeLater(() -> {
                        dslPreviewArea.setText(res.dsl == null ? "" : res.dsl);
                        // show errors or success message
                        if (res.success) {
                            errorArea.setText("解析通过，无错误。\n解析日志: " + res.logPath);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            if (res.errors != null && !res.errors.isEmpty()) {
                                for (String err : res.errors) sb.append(err).append("\n");
                            } else if (res.failureReason != null) {
                                sb.append(res.failureReason);
                            }
                            // 把解析日志路径也显示，便于用户定位
                            sb.append("\n\n解析日志: ").append(res.logPath == null ? "(无)" : res.logPath);
                            errorArea.setText(sb.toString());
                        }
                        // 保存 lastGeneratedDsl
                        lastGeneratedDsl = res.dsl == null ? "" : res.dsl;
                        lastGeneratedType = finalType;
                        lastGeneratedSourceFileName = finalSourceFile;

                        // 统计：单次生成也计入总次数（成功/失败分别计数），尝试次数用 attempts.size()
                        int attempts = (res.attempts == null || res.attempts.isEmpty()) ? 1 : res.attempts.size();
                        totalConversions += 1;
                        if (res.success) {
                            totalSuccesses += 1;
                        } else {
                            totalFailures += 1;
                        }
                        totalAttemptsAccum += attempts;
                        refreshStatisticsLabels();

                        appendLog("单次生成完成: success=" + res.success + ", errors=" + (res.errors == null ? 0 : res.errors.size()));
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "单次生成失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        appendLog("单次生成异常: " + ex.getMessage());

                        // 统计：异常也算一次失败
                        totalConversions += 1;
                        totalFailures += 1;
                        totalAttemptsAccum += 1;
                        refreshStatisticsLabels();
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        generateButton.setEnabled(true);
                        repairButton.setEnabled(true);
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                        updateProgress(100, "单次生成完成");
                        statusLabel.setText("就绪");
                    });
                }
            }).start();
         });

        // 修复按钮（修复并重新生成）
        repairButton.addActionListener(e -> {
            if (toolchainService == null) {
                JOptionPane.showMessageDialog(this, "服务未初始化", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (lastGeneratedDsl == null || lastGeneratedDsl.isBlank()) {
                JOptionPane.showMessageDialog(this, "请先使用“单次DSL实例生成”获取初始 DSL，然后再点击修复并重新生成。", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 点击后切换到结果预览页面（与“自动化生成与修复”一致）
            selectNavItem(5);

            // 注意：修复次数上限只约束“自动化生成与修复”（ToolchainService 内部重试）。
            // “修复并重新生成”作为用户手动操作，不受修复次数上限约束，可无限次点击。

            String userEditedFeedback = errorArea.getText();
            String type = lastGeneratedType == null ? (String) typeComboBox.getSelectedItem() : lastGeneratedType;
            String sourceFile = lastGeneratedSourceFileName;

            // 防止重复点击
            repairButton.setEnabled(false);
            generateButton.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            // 使用 determinate 进度风格，与自动化生成保持一致
            updateProgress(0, "修复并重新生成中...");
            statusLabel.setText("修复并重新生成中...");

            new Thread(() -> {
                try {
                    ToolchainResult res = toolchainService.repairAndRegenerate(lastGeneratedDsl, userEditedFeedback, type, sourceFile);
                    SwingUtilities.invokeLater(() -> {
                        dslPreviewArea.setText(res.dsl == null ? "" : res.dsl);
                        if (res.success) {
                            errorArea.setText("解析通过，无错误。\n解析日志: " + res.logPath);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            if (res.errors != null && !res.errors.isEmpty()) {
                                for (String err : res.errors) sb.append(err).append("\n");
                            } else if (res.failureReason != null) {
                                sb.append(res.failureReason);
                            }
                            sb.append("\n\n解析日志: ").append(res.logPath == null ? "(无)" : res.logPath);
                            errorArea.setText(sb.toString());
                        }
                        // 更新 lastGeneratedDsl 为本次修复得到的DSL
                        lastGeneratedDsl = res.dsl == null ? lastGeneratedDsl : res.dsl;

                        // 统计：手动修复也计入总次数（成功/失败分别计数），尝试次数用 attempts.size()
                        int attempts = (res.attempts == null || res.attempts.isEmpty()) ? 1 : res.attempts.size();
                        totalConversions += 1;
                        if (res.success) {
                            totalSuccesses += 1;
                        } else {
                            totalFailures += 1;
                        }
                        totalAttemptsAccum += attempts;
                        refreshStatisticsLabels();

                        appendLog("修复并重新生成完成: success=" + res.success + ", errors=" + (res.errors == null ? 0 : res.errors.size()));
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "修复并重新生成失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        appendLog("修复并重新生成异常: " + ex.getMessage());

                        // 统计：异常也算一次失败
                        totalConversions += 1;
                        totalFailures += 1;
                        totalAttemptsAccum += 1;
                        refreshStatisticsLabels();
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        repairButton.setEnabled(true);
                        generateButton.setEnabled(true);
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                        updateProgress(100, "修复完成");
                        statusLabel.setText("就绪");
                    });
                }
            }).start();
        });

        // DSL类型选择变化
        typeComboBox.addActionListener(e -> {
            String selected = (String) typeComboBox.getSelectedItem();
            currentDslTypeLabel.setText(selected);
            // 同步提示词模板为默认模板（基于DSL类型和当前LLM类型）
            LlmConfigModel cur = (LlmConfigModel) llmComboBox.getSelectedItem();
            String promptTemplate = getDefaultPromptTemplateForType(cur == null ? null : cur.getType(), selected);
            currentPromptLabel.setText(promptTemplate);
            // 在 promptComboBox 中选中（如果存在）
            for (int pi = 0; pi < promptComboBox.getItemCount(); pi++) {
                Object it = promptComboBox.getItemAt(pi);
                if (it != null && promptTemplate.equals(it.toString())) {
                    promptComboBox.setSelectedIndex(pi);
                    break;
                }
            }
        });

        // LLM选择变化
        llmComboBox.addActionListener(e -> {
            if (suppressLlmChangeEvents) return;
            LlmConfigModel selectedModel = (LlmConfigModel) llmComboBox.getSelectedItem();
            if (selectedModel == null) return;

            String selectedId = selectedModel.getId();

            // 更新首页显示（显��友好名称）
            currentLlmLabel.setText(selectedModel.getName());
             // 同步首页的最大Token与温度显示
            try {
                LlmConfigManager mgr = LlmConfigManager.getInstance();
                String tokensRaw = mgr.getRawProperty("llm." + selectedModel.getId() + ".maxTokens", String.valueOf(selectedModel.getMaxTokens()));
                String tempRaw = mgr.getRawProperty("llm." + selectedModel.getId() + ".temperature", String.valueOf(selectedModel.getTemperature()));
                maxTokensLabel.setText(tokensRaw);
                temperatureLabel.setText(formatTemperature(tempRaw, selectedModel.getTemperature()));
            } catch (Exception ignored) {}
            try { llmTypeLabel.setText(selectedId.toLowerCase()); } catch (Exception ignored) {}
            // 同步提示词模板（考虑 DSL 类型和模型类型）
            String currentDsl = (String) typeComboBox.getSelectedItem();
            String promptTemplate = getDefaultPromptTemplateForType(selectedModel.getType(), currentDsl);
            currentPromptLabel.setText(promptTemplate);
            for (int pi = 0; pi < promptComboBox.getItemCount(); pi++) {
                Object it = promptComboBox.getItemAt(pi);
                if (it != null && promptTemplate.equals(it.toString())) {
                    promptComboBox.setSelectedIndex(pi);
                    break;
                }
            }

            // 同步首页输出目录：图片模型使用 annotated-images
            if ("doubao-image".equals(selectedId) || "gemini-image".equals(selectedId)) {
                outputDirLabel.setText("output\\annotated-images");
            } else {
                outputDirLabel.setText("output");
            }

            // 同步到配置并持久化，使运行时和详细配置保持一致
            try {
                LlmConfigManager mgr = LlmConfigManager.getInstance();
                mgr.setActiveLlm(selectedId);
                mgr.saveToFile();

                // 重新加载运行时配置
                LlmConfig.resetInstance();
                if (toolchainService != null) {
                    toolchainService.reloadConfig();
                }
                appendLog("已将主界面选择的LLM同步到配置：" + selectedId);
            } catch (Exception ex) {
                appendLog("同步LLM选择失败: " + ex.getMessage());
            }
        });

        // DSL实例查看页：数据源选择
        dslInstancesSourceComboBox.addActionListener(e -> {
            dslInstancesCurrentDir = getSelectedDslInstancesRootDir();
            refreshDslInstancesList();
        });

        // DSL实例查看页：返回上一级（仅 preset-dsl 生效）
        dslInstancesBackButton.addActionListener(e -> {
            java.nio.file.Path root = getSelectedDslInstancesRootDir();
            if (dslInstancesCurrentDir == null) {
                dslInstancesCurrentDir = root;
            } else if (!dslInstancesCurrentDir.equals(root)) {
                java.nio.file.Path parent = dslInstancesCurrentDir.getParent();
                dslInstancesCurrentDir = (parent == null) ? root : parent;
                // 防止越界
                try {
                    if (!dslInstancesCurrentDir.toAbsolutePath().normalize().startsWith(root.toAbsolutePath().normalize())) {
                        dslInstancesCurrentDir = root;
                    }
                } catch (Exception ignored) {
                    dslInstancesCurrentDir = root;
                }
            }
            refreshDslInstancesList();
        });

        // DSL实例查看：点击目录进入；点击文件预览
        dslInstancesList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            File f = dslInstancesList.getSelectedValue();
            if (f == null) return;

            if (f.isDirectory()) {
                dslInstancesCurrentDir = f.toPath();
                refreshDslInstancesList();
            } else {
                showDslInstanceFile(f);
            }
        });

        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isConverting) {
                    int result = JOptionPane.showConfirmDialog(
                            ModernDslConverterGUI.this,
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
            toolchainService.setProgressListener(createProgressListener());
            appendLog("服务初始化成功");

            // 启动时加载并展示历史统计（从 output/statistics.json + output/conversion-history.json 计算得来）
            loadAndDisplayPersistedStatistics();

            // 更新首页配置显示（显示友好名称）
            LlmConfig config = LlmConfig.getInstance();
            currentLlmLabel.setText(config.getActiveLlmName());
            llmTypeLabel.setText(config.getActiveLlm().toLowerCase());

            // 设置首页 maxTokens / temperature 根据激活 LLM
            LlmConfigModel activeModel = LlmConfigManager.getInstance().getConfig(config.getActiveLlm());
            if (activeModel != null) {
                try {
                    LlmConfigManager mgr = LlmConfigManager.getInstance();
                    String tokensRaw = mgr.getRawProperty("llm." + activeModel.getId() + ".maxTokens", String.valueOf(activeModel.getMaxTokens()));
                    String tempRaw = mgr.getRawProperty("llm." + activeModel.getId() + ".temperature", String.valueOf(activeModel.getTemperature()));
                    maxTokensLabel.setText(tokensRaw);
                    temperatureLabel.setText(formatTemperature(tempRaw, activeModel.getTemperature()));
                } catch (Exception ignored) {}
                // 同步首页提示词模板（基于当前 DSL 类型与 LLM 类型）
                try {
                    String dslType = (String) typeComboBox.getSelectedItem();
                    String promptTemplate = getDefaultPromptTemplateForType(activeModel.getType(), dslType);
                    currentPromptLabel.setText(promptTemplate);
                    // 在 promptComboBox 中选中该模板（如果存在）
                    for (int pi = 0; pi < promptComboBox.getItemCount(); pi++) {
                        Object it = promptComboBox.getItemAt(pi);
                        if (it != null && promptTemplate.equals(it.toString())) {
                            promptComboBox.setSelectedIndex(pi);
                            break;
                        }
                    }
                } catch (Exception ignored) {}
             }

            // 当激活模型是图片标注模型时，首页应显示 annotated-images 输出目录
            String activeId = config.getActiveLlm();
            if ("doubao-image".equals(activeId) || "gemini-image".equals(activeId)) {
                outputDirLabel.setText("output\\annotated-images");
            } else {
                outputDirLabel.setText("output");
            }

             // 设置下拉框默认值（按 id 匹配）
             suppressLlmChangeEvents = true;
             for (int i = 0; i < llmComboBox.getItemCount(); i++) {
                 LlmConfigModel m = llmComboBox.getItemAt(i);
                 if (m != null && m.getId().equals(config.getActiveLlm())) {
                     llmComboBox.setSelectedItem(m);
                     break;
                 }
             }
             suppressLlmChangeEvents = false;

        } catch (Exception e) {
            appendLog("服务初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || isValidFile(f);
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
            updateFileCount();
            updateStatus("已添加 " + files.length + " 个文件");
        }
    }

    private void addFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("选择文件夹");

        int result = folderChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File folder = folderChooser.getSelectedFile();
            int count = addFilesFromFolder(folder);
            updateFileCount();
            updateStatus("已从文件夹添加 " + count + " 个文件");
        }
    }

    private void removeSelectedFiles() {
        List<File> selectedFiles = fileList.getSelectedValuesList();
        for (File file : selectedFiles) {
            fileListModel.removeElement(file);
        }
        updateFileCount();
        updateStatus("已移除 " + selectedFiles.size() + " 个文件");
    }

    private void clearFileList() {
        int count = fileListModel.getSize();
        fileListModel.clear();
        updateFileCount();
        updateStatus("已清空 " + count + " 个文件");
    }

    private void updateFileCount() {
        fileCountLabel.setText(String.valueOf(fileListModel.getSize()));
    }

    private void openSettings() {
        // 打开 LlmConfigManagerDialog（支持添加/编辑/删除/设为激活/刷新）
        LlmConfigManagerDialog dialog = new LlmConfigManagerDialog(this);
        dialog.setVisible(true);

        if (dialog.isConfigChanged()) {
            // 重新加载配置并刷新下拉框与首页显示
            LlmConfigManager.resetInstance(); // ensure reload
            LlmConfigManager mgr = LlmConfigManager.getInstance();

            java.util.List<LlmConfigModel> configs = mgr.getEnabledConfigs();
            if (configs.isEmpty()) {
                configs.add(new LlmConfigModel("deepseek", "deepseek"));
            }

            suppressLlmChangeEvents = true;
            llmComboBox.removeAllItems();
            for (LlmConfigModel c : configs) {
                llmComboBox.addItem(c);
            }

            // 根据新的激活项选中
            String activeId = mgr.getActiveLlmId();
            for (int i = 0; i < llmComboBox.getItemCount(); i++) {
                LlmConfigModel m = llmComboBox.getItemAt(i);
                if (m != null && m.getId().equals(activeId)) {
                    llmComboBox.setSelectedItem(m);
                    currentLlmLabel.setText(m.getName());
                    llmTypeLabel.setText(m.getId().toLowerCase());
                    // 同步首页其他详情
                    updateHomeLlmDetails(m);
                    // 同步提示词模板：根据 DSL 类型选择默认模板
                    String promptTemplate = getDefaultPromptTemplateForType(m.getType(), (String) typeComboBox.getSelectedItem());
                    currentPromptLabel.setText(promptTemplate);
                    // 尝试在 promptComboBox 中选中该模板（如果存在）
                    for (int pi = 0; pi < promptComboBox.getItemCount(); pi++) {
                        Object it = promptComboBox.getItemAt(pi);
                        if (it != null && promptTemplate.equals(it.toString())) {
                            promptComboBox.setSelectedIndex(pi);
                            break;
                        }
                    }
                    break;
                }
            }
            suppressLlmChangeEvents = false;

            // 刷新 Toolchain 配置
            LlmConfig.resetInstance();
            if (toolchainService != null) toolchainService.reloadConfig();

            appendLog("已重新加载配置并刷新下拉框");
        }
    }

    private void openPromptEditor() {
        PromptEditorDialog dialog = new PromptEditorDialog(this);
        dialog.setVisible(true);
    }

    /**
     * 根据 LLM 类型和 DSL 类型返回默认提示词模板文件名
     */
    private String getDefaultPromptTemplateForType(String llmType, String dslType) {
        if (llmType != null && llmType.equals("image-annotation")) {
            return BenchConstants.PROMPT_IMAGE_ANNOTATION;
        }
        return BenchConstants.PROMPT_GENERATION;
    }

    // ==================== 转换逻辑 ====================
    private void startConversion() {
        if (fileListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先添加要转换的文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 确保自动化生成与修复使用最新的 toolchain.maxRetries（与“修复次数上限”设置保持一致）
        try {
            LlmConfig.resetInstance();
            if (toolchainService != null) {
                toolchainService.reloadConfig();
            }
        } catch (Exception ignored) {
        }

        isConverting = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        generateButton.setEnabled(false);
        repairButton.setEnabled(false);

        logArea.setText("");
        dslPreviewArea.setText("");
        errorArea.setText("");

        // 切换到结果预览页面
        selectNavItem(5);

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
        appendLog("开始批量转换");
        appendLog("========================================");
        appendLog("总文件数: " + totalFiles);
        appendLog("DSL 类型: " + type);
        appendLog("当前 LLM: " + activeLlm);
        appendLog("");

        if (BenchConstants.isTranslationTree(type) && ("doubao-image".equals(activeLlm) || "gemini-image".equals(activeLlm))) {
            handleImageAnnotation();
        } else if (BenchConstants.isTranslationTree(type) && totalFiles > 1) {
            handleTranslationTreeBatch();
        } else {
            handleRegularConversion();
        }
    }

    private void handleImageAnnotation() {
        String activeLlm = LlmConfig.getInstance().getActiveLlm();
        String modelName = activeLlm.equals("doubao-image") ? 
            "豆包图片标注 (Doubao-Seedream-4.5)" : 
            "Gemini图片标注 (Gemini Vision)";
            
        appendLog("开始翻译树图片标注处理");
        appendLog("使用模型: " + modelName);
        appendLog("");
        
        final int[] successCount = {0};
        final int totalFiles = fileListModel.getSize();
        
        for (int i = 0; i < totalFiles && isConverting; i++) {
            File file = fileListModel.getElementAt(i);
            
            try {
                appendLog("处理文件: " + file.getName());
                updateProgress((i * 100) / totalFiles, "标注图片: " + file.getName());
                
                String fileContent = FileContentExtractor.extractContent(file);
                String fileType = FileContentExtractor.getFileTypeDescription(file);
                appendLog("已读取" + fileType + "，内容长度: " + fileContent.length() + " 字符");
                
                AnnotationResult result = toolchainService.annotateImage(fileContent, file.getName());
                
                if (result.success) {
                    appendLog("图片标注成功: " + file.getName());
                    appendLog("标注结果保存至: " + result.imagePath);
                    
                    SwingUtilities.invokeLater(() -> {
                        String message = "图片标注成功！\n\n是否要打开标注后的文件？";
                        String[] options = new String[] { "打开", "取消" };
                        int choice = JOptionPane.showOptionDialog(
                                ModernDslConverterGUI.this,
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
                                        ModernDslConverterGUI.this,
                                        "文件不存在：\n" + result.imagePath,
                                        "提示",
                                        JOptionPane.WARNING_MESSAGE
                                    );
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(
                                    ModernDslConverterGUI.this,
                                    "无法打开文件：\n" + ex.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        }
                    });
                    successCount[0]++;
                } else {
                    appendLog("图片标注失败: " + file.getName());
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
                            ModernDslConverterGUI.this,
                            scroll,
                            "图片标注失败",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                }
                
            } catch (Exception e) {
                appendLog("处理失败: " + e.getMessage());
                appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
            }
        }
        
        updateProgress(100, "图片标注完成");
        finishConversion();
        successCountLabel.setText(String.valueOf(successCount[0]));
    }

    private void handleTranslationTreeBatch() {
        int totalFiles = fileListModel.getSize();
        appendLog("翻译树DSL批量处理模式");
        appendLog("将合并处理 " + totalFiles + " 个文件");
        appendLog("");

        StringBuilder combinedContent = new StringBuilder();
        combinedContent.append("=== 翻译树DSL批量处理 ===\n\n");

        for (int i = 0; i < totalFiles; i++) {
            File file = fileListModel.getElementAt(i);
            appendLog("读取文件 [" + (i + 1) + "/" + totalFiles + "]: " + file.getName());

            try {
                String content = FileContentExtractor.extractContent(file);
                String fileType = FileContentExtractor.getFileTypeDescription(file);
                
                combinedContent.append("--- 文件 ").append(i + 1).append(": ").append(file.getName())
                              .append(" (").append(fileType).append(") ---\n");
                combinedContent.append(content).append("\n\n");
                
                appendLog("已读取" + fileType + "，内容长度: " + content.length() + " 字符");
            } catch (IOException e) {
                appendLog("读取文件失败: " + e.getMessage());
            }
        }

        appendLog("");
        appendLog("开始合并处理翻译树DSL...");
        
        try {
            String combinedFileName = "translation_tree_batch_" + totalFiles + "_files";
            ToolchainResult result = toolchainService.generateAndValidate(
                combinedContent.toString(), BenchConstants.TYPE_TRANSLATION_TREE, combinedFileName);
            
            displayResult(result, combinedFileName);
            if (result.success) {
                int current = Integer.parseInt(successCountLabel.getText());
                successCountLabel.setText(String.valueOf(current + 1));
            }
            
        } catch (Exception e) {
            appendLog("批量转换失败: " + e.getMessage());
            appendError("批量处理错误: " + e.getMessage() + "\n\n");
        }

        updateProgress(100, "批量处理完成");
        finishConversion();
    }

    private void handleRegularConversion() {
        int totalFiles = fileListModel.getSize();
        int successCount = 0;
        
        for (int i = 0; i < totalFiles && isConverting; i++) {
            File file = fileListModel.getElementAt(i);
            int fileIndex = i + 1;
            int progress = (int) ((i / (double) totalFiles) * 100);

            updateProgress(progress, "正在处理 " + fileIndex + "/" + totalFiles + ": " + file.getName());

            appendLog("╔════════════════════════════════════════════════════════════════════════╗");
            appendLog("║ 文件 [" + fileIndex + "/" + totalFiles + "]: " + file.getName());
            appendLog("╚════════════════════════════════════════════════════════════════╝");

            try {
                String nlSpec = FileContentExtractor.extractContent(file);
                String fileType = FileContentExtractor.getFileTypeDescription(file);
                appendLog("已读取" + fileType + "，内容长度: " + nlSpec.length() + " 字符");

                ToolchainResult result = toolchainService.generateAndValidate(
                    nlSpec, (String) typeComboBox.getSelectedItem(), file.getName());

                displayResult(result, file.getName());
                if (result.success) successCount++;

            } catch (IOException e) {
                appendLog("读取文件失败: " + e.getMessage());
                appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
            } catch (Exception e) {
                appendLog("转换失败: " + e.getMessage());
                appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
            }

            appendLog("");
        }

        updateProgress(100, "转换完成");
        finishConversion();
        successCountLabel.setText(String.valueOf(successCount));
    }

    private void finishConversion() {
        appendLog("========================================");
        appendLog("批量转换完成");
        appendLog("========================================");

        SwingUtilities.invokeLater(() -> {
            isConverting = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            generateButton.setEnabled(true);
            repairButton.setEnabled(true);
        });
    }

    private void displayResult(ToolchainResult result, String fileName) {
        appendLog("");
        appendLog("════════════════════════════════════════════════════════════════");
        appendLog("文件处理完成: " + fileName);
        appendLog("结果: " + (result.success ? "成功" : "失败"));
        appendLog("总尝试次数: " + result.attempts.size());
        appendLog("════════════════════════════════════════════════════════════════");
        appendLog("");
    }

    private void stopConversion() {
        if (!isConverting) return;

        int result = JOptionPane.showConfirmDialog(
                this, "确定要停止转换吗？", "确认停止", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            isConverting = false;
            appendLog("\n用户中止了转换过程");
            updateStatus("已停止");
        }
    }

    // 单次 DSL 实例生成
    private void runSingleGeneration() {
        if (fileListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先添加要转换的文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 重置修复次数计数
        repairAttemptCount = 0;

        isConverting = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        generateButton.setEnabled(false);
        repairButton.setEnabled(false);

        logArea.setText("");
        dslPreviewArea.setText("");
        errorArea.setText("");

        selectNavItem(5);

        conversionThread = new Thread(this::performSingleGeneration);
        conversionThread.start();
    }

    private void performSingleGeneration() {
        if (fileListModel.isEmpty()) {
            appendLog("没有要处理的文件");
            finishConversion();
            return;
        }

        // 重置修复次数计数
        repairAttemptCount = 0;

        File file = fileListModel.getElementAt(0);
        appendLog("╔════════════════════════════════════════════════════════════════════════╗");
        appendLog("║  单次 DSL 实例生成");
        appendLog("║  文件: " + file.getName());
        appendLog("╚════════════════════════════════════════════════════════════════╝");

        try {
            String nlSpec = FileContentExtractor.extractContent(file);
            String fileType = FileContentExtractor.getFileTypeDescription(file);
            appendLog("已读取" + fileType + "，内容长度: " + nlSpec.length() + " 字符");

            // 保存为单次生成
            String fileName = file.getName();
            lastGeneratedSourceFileName = fileName;
            lastGeneratedType = (String) typeComboBox.getSelectedItem();

            // 调用 ToolchainService 进行单次生成（仅生成一次，不重试）
            ToolchainResult result = toolchainService.generateAndValidate(nlSpec, lastGeneratedType, fileName);

            // 保存最后生成的 DSL
            if (!result.attempts.isEmpty()) {
                lastGeneratedDsl = result.attempts.get(result.attempts.size() - 1).dsl;
                appendDslPreview("\n========== 单次生成的 DSL ==========" + "\n\n");
                appendDslPreview(lastGeneratedDsl);
                appendDslPreview("\n\n");
            }

            displayResult(result, file.getName());

        } catch (IOException e) {
            appendLog("读取文件失败: " + e.getMessage());
            appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
        } catch (Exception e) {
            appendLog("单次生成失败: " + e.getMessage());
            appendError("文件: " + file.getName() + "\n错误: " + e.getMessage() + "\n\n");
        }

        updateProgress(100, "单次生成完成");
        finishConversion();
    }

    // 修复并重新生成
    private void runRepairGeneration() {
        if (lastGeneratedDsl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先点击'单次DSL实例生成'", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 从错误信息面板读取用户编辑的错误/修改要求
        String errorInfo = errorArea.getText();
        if (errorInfo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请在错误信息中编辑修改要求", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        isConverting = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        generateButton.setEnabled(false);
        repairButton.setEnabled(false);

        logArea.setText("");
        dslPreviewArea.setText("");
        errorArea.setText("");

        selectNavItem(5);

        conversionThread = new Thread(this::performRepairGeneration);
        conversionThread.start();
    }

    private void performRepairGeneration() {
        appendLog("╔════════════════════════════════════════════════════════════════════════╗");
        appendLog("║  修复并重新生成");
        appendLog("║  基于上一版 DSL 与用户修改要求");
        appendLog("╚════════════════════════════════════════════════════════════════╝");

        try {
            // 用用户编辑的错误信息作为修改提示
            String userFeedback = errorArea.getText();

            // 组合为新的提示：上一版 DSL + 错误信息 + 修改要求
            String repairSpec = "上一版 DSL:\n\n" + lastGeneratedDsl + "\n\n用户修改要求:\n\n" + userFeedback;

            ToolchainResult result = toolchainService.generateAndValidate(repairSpec, lastGeneratedType, lastGeneratedSourceFileName);

            // 保存最后生成的 DSL
            if (!result.attempts.isEmpty()) {
                lastGeneratedDsl = result.attempts.get(result.attempts.size() - 1).dsl;
                appendDslPreview("\n========== 修复后的 DSL ==========" + "\n\n");
                appendDslPreview(lastGeneratedDsl);
                appendDslPreview("\n\n");
            }

            displayResult(result, lastGeneratedSourceFileName);

        } catch (Exception e) {
            appendLog("修复重生成失败: " + e.getMessage());
            appendError("错误: " + e.getMessage() + "\n\n");
        }

        updateProgress(100, "修复重生成完成");
        finishConversion();
    }

    // 打开修复次数上限设置对话框
    private void openRetryLimitDialog() {
        // 每次打开对话框，数字初值固定为 3（不继承上一次用户设置值）
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        spinner.setFont(normalFont);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("设置修复/重试次数上限 (默认3):"), BorderLayout.NORTH);
        panel.add(spinner, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "修复次数上限设置", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int value = (Integer) spinner.getValue();
            try {
                LlmConfigManager mgr = LlmConfigManager.getInstance();
                mgr.setMaxRetries(value);
                // 触发保存（复用当前激活配置保存逻辑）
                LlmConfigModel active = mgr.getActiveConfig();
                if (active != null) {
                    mgr.saveConfig(active);
                }

                // 立即让运行时生效：重置并重新加载 Toolchain 配置
                try {
                    LlmConfig.resetInstance();
                    if (toolchainService != null) {
                        toolchainService.reloadConfig();
                    }
                } catch (Exception ignored) {
                }

                JOptionPane.showMessageDialog(this, "修复次数上限已更新为 " + value + "（将用于自动化生成与修复）", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // ==================== 辅助方法 ====================
    /**
     * 将原始配置字符串或 double 值格式化为简短的、去尾零的十进制字符串。
     * 优先使用用户在配置文件中输入的原始字符串（如果是合法数字），否则使用 fallback double。
     */
    private String formatTemperature(String raw, double fallback) {
        if (raw != null) {
            String trimmed = raw.trim();
            if (!trimmed.isEmpty()) {
                try {
                    BigDecimal bdRaw = new BigDecimal(trimmed);
                    BigDecimal bdFallback = BigDecimal.valueOf(fallback);

                    // 统一将两边都四舍五入到 6 位小数用于比较（将像 0.19999999999999998 归为 0.2）
                    BigDecimal bdRawRounded = bdRaw.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();
                    BigDecimal bdFallbackRounded = bdFallback.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();

                    if (bdRawRounded.compareTo(bdFallbackRounded) == 0) {
                        return bdFallbackRounded.toPlainString();
                    }

                    // 如果原始字符串非常长或有过多小数位，展示四舍五入后的简短结果
                    if (bdRaw.scale() > 6 || trimmed.length() > 8) {
                        return bdRawRounded.toPlainString();
                    }

                    BigDecimal bd = bdRaw.stripTrailingZeros();
                    return bd.toPlainString();
                } catch (NumberFormatException ignored) {
                    // fallthrough to use fallback
                }
            }
        }
        BigDecimal bd = BigDecimal.valueOf(fallback).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd.toPlainString();
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

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }

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
                appendLog("║  第 " + attemptNumber + "/" + maxAttempts + " 次尝试" + (isRetry ? "（修改模式）" : "（生成模式）"));
                appendLog("╚════════════════════════════════════════════════════════════════╝");
            }

            @Override
            public void onLlmCallStart(String llmName) {
                appendLog("正在调用 " + llmName + "...");
                updateStatus("正在进行LLM调用 (" + llmName + ")...");
            }

            @Override
            public void onLlmCallComplete(String rawDsl, long durationMs) {
                appendLog("LLM 调用完成，耗时: " + durationMs + " ms");
                appendLog("");
                appendLog("┌─────────────────────────────────────────────────────────────┐");
                appendLog("│  LLM 原始输出（第 " + currentAttempt + " 次）");
                appendLog("└─────────────────────────────────────────────────────────────┘");

                String preview = rawDsl;
                if (preview != null && preview.length() > 500) {
                    preview = preview.substring(0, 500) + "\n... (已截断，完整内容见 DSL 预览)";
                }
                if (preview == null) preview = "";
                appendLog(preview);
                appendLog("");
            }

            @Override
            public void onDslCleaned(String cleanedDsl) {
                appendLog("DSL 清理完成");
            }

            @Override
            public void onParseStart() {
                appendLog("开始语法解析...");
                updateStatus("正在进行语法解析...");
            }

            @Override
            public void onParseComplete(ParseResult result) {
                appendLog("");
                appendLog("┌─────────────────────────────────────────────────────────────┐");
                appendLog("│  解析结果（第 " + currentAttempt + " 次）");
                appendLog("└─────────────────────────────────────────────────────────────┘");

                if (result.success) {
                    appendLog("解析成功！");
                    appendLog("解析日志: " + result.logPath);
                    appendLog("可视化图: " + result.svgPath);
                } else {
                    appendLog("解析失败，发现 " + result.errorCount + " 个错误");
                    appendLog("");
                    appendLog("错误详情:");
                    appendLog("─────────────────────────────────────────────────────────────");

                    int errorCount = Math.min(5, result.errors.size());
                    for (int i = 0; i < errorCount; i++) {
                        appendLog((i + 1) + ". " + result.errors.get(i));
                    }
                    if (result.errors.size() > 5) {
                        appendLog("... 还有 " + (result.errors.size() - 5) + " 个错误（查看错误信息标签页）");
                    }
                    appendLog("─────────────────────────────────────────────────────────────");

                    appendError("\n========== 第 " + currentAttempt + " 次尝试的错误 ==========" + "\n");
                    for (String error : result.errors) {
                        appendError(error + "\n");
                    }
                    appendError("\n");
                }
                appendLog("");
            }

            @Override
            public void onDslGenerated(String dsl, int attemptNumber) {
                appendDslPreview("\n========== 第 " + attemptNumber + " 次尝试生成的 DSL ==========" + "\n\n");
                appendDslPreview(dsl);
                appendDslPreview("\n\n");
                appendDslPreview("─────────────────────────────────────────────────────────────\n");
            }

            @Override
            public void onError(String error) {
                appendLog(error);
                appendError(error + "\n");
            }

            @Override
            public void onSuccess(int attemptNumber, String dslPath, String svgPath) {
                appendLog("");
                appendLog("╔════════════════════════════════════════════════════════════════╗");
                appendLog("║  转换成功！                                                   ║");
                appendLog("╚════════════════════════════════════════════════════════════════╝");
                appendLog("成功尝试次数: " + attemptNumber);
                appendLog("DSL 文件: " + dslPath);
                appendLog("SVG 图形: " + svgPath);
                updateStatus("转换成功！");

                // 更新统计信息（成功）
                totalConversions += 1;
                totalSuccesses += 1;
                totalAttemptsAccum += attemptNumber;
                refreshStatisticsLabels();
            }

            @Override
            public void onFailure(int totalAttempts, String reason) {
                appendLog("");
                appendLog("╔════════════════════════════════════════════════════════════════╗");
                appendLog("║  转换失败                                                     ║");
                appendLog("╚════════════════════════════════════════════════════════════════╝");
                appendLog("总尝试次数: " + totalAttempts);
                appendLog("失败原因: " + reason);
                updateStatus("转换失败");

                appendError("\n========== 最终失败原因 ==========" + "\n");
                appendError(reason + "\n");

                // 更新统计信息（失败）
                totalConversions += 1;
                totalFailures += 1;
                totalAttemptsAccum += totalAttempts;
                refreshStatisticsLabels();
            }
        };
    }

    // 文件列表单元格渲染器（简洁版）
    private class ModernFileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                File file = (File) value;
                setText(file.getName());
                setToolTipText(file.getAbsolutePath());
                setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                setFont(normalFont);
                if (isSelected) {
                    setBackground(SIDEBAR_SELECTED);
                    setForeground(PRIMARY_TEXT);
                } else {
                    setBackground(CONTENT_BG);
                    setForeground(PRIMARY_TEXT);
                }
            }
            return this;
        }
    }

    // Main 方法入口
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        javax.swing.SwingUtilities.invokeLater(() -> {
            ModernDslConverterGUI gui = new ModernDslConverterGUI();
            gui.setVisible(true);
        });
    }

}

