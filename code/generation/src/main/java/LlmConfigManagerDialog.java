import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 新版LLM配置管理对话框
 * 支持动态添加、编辑、删除LLM配置
 */
public class LlmConfigManagerDialog extends JDialog {
    private LlmConfigManager configManager;
    private JTable configTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton setActiveButton;
    private JButton refreshButton;
    private JSpinner maxRetriesSpinner;
    private JCheckBox syntaxPreCheckBox;
    private boolean configChanged = false;

    public LlmConfigManagerDialog(Frame parent) {
        super(parent, "LLM配置管理", true);
        this.configManager = LlmConfigManager.getInstance();

        initializeComponents();
        layoutComponents();
        loadConfigs();
        setupEventHandlers();

        setSize(900, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // 表格
        String[] columnNames = {"配置ID", "显示名称", "模型", "类型", "激活"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        configTable = new JTable(tableModel);
        configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        configTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        configTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        configTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        configTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        // 按钮
        addButton = new JButton("➕ 添加新LLM");
        editButton = new JButton("✏️ 编辑");
        deleteButton = new JButton("🗑️ 删除");
        setActiveButton = new JButton("✅ 设为激活");
        refreshButton = new JButton("🔄 刷新");

        // 工具链配置
        maxRetriesSpinner = new JSpinner(new SpinnerNumberModel(
            configManager.getMaxRetries(), 1, 10, 1));
        syntaxPreCheckBox = new JCheckBox("启用语法预检查",
            configManager.isEnableSyntaxPreCheck());
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // 顶部工具栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        topPanel.add(addButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(setActiveButton);
        topPanel.add(new JSeparator(SwingConstants.VERTICAL));
        topPanel.add(refreshButton);

        // 中间表格
        JScrollPane scrollPane = new JScrollPane(configTable);
        scrollPane.setBorder(new TitledBorder("LLM配置列表"));

        // 底部工具链设置
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(new TitledBorder("工具链设置"));

        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        settingsPanel.add(new JLabel("最大重试次数:"));
        settingsPanel.add(maxRetriesSpinner);
        settingsPanel.add(Box.createHorizontalStrut(20));
        settingsPanel.add(syntaxPreCheckBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton saveButton = new JButton("💾 保存设置");
        JButton closeButton = new JButton("关闭");

        saveButton.addActionListener(e -> saveToolchainSettings());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        bottomPanel.add(settingsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 布局
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        addButton.addActionListener(e -> addLlmConfig());
        editButton.addActionListener(e -> editLlmConfig());
        deleteButton.addActionListener(e -> deleteLlmConfig());
        setActiveButton.addActionListener(e -> setActiveLlm());
        refreshButton.addActionListener(e -> refreshConfigs());

        // 双击编辑
        configTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editLlmConfig();
                }
            }
        });
    }

    private void loadConfigs() {
        tableModel.setRowCount(0);
        List<LlmConfigModel> configs = configManager.getAllConfigs();
        String activeId = configManager.getActiveLlmId();

        for (LlmConfigModel config : configs) {
            Object[] row = {
                config.getId(),
                config.getName(),
                config.getModel(),
                config.getType().equals("dsl-generation") ? "DSL生成" : "图片标注",
                config.getId().equals(activeId) ? "⭐ 激活" : ""
            };
            tableModel.addRow(row);
        }
    }

    private void addLlmConfig() {
        LlmConfigEditDialog dialog = new LlmConfigEditDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            LlmConfigModel newConfig = dialog.getConfig();
            try {
                configManager.saveConfig(newConfig);
                loadConfigs();
                configChanged = true;
                JOptionPane.showMessageDialog(this,
                    "LLM配置添加成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "保存配置失败: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editLlmConfig() {
        int selectedRow = configTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "请先选择要编辑的配置",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String configId = (String) tableModel.getValueAt(selectedRow, 0);
        LlmConfigModel config = configManager.getConfig(configId);

        LlmConfigEditDialog dialog = new LlmConfigEditDialog(this, config);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            LlmConfigModel updatedConfig = dialog.getConfig();
            try {
                configManager.saveConfig(updatedConfig);
                loadConfigs();
                configChanged = true;
                JOptionPane.showMessageDialog(this,
                    "LLM配置更新成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "保存配置失败: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteLlmConfig() {
        int selectedRow = configTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "请先选择要删除的配置",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String configId = (String) tableModel.getValueAt(selectedRow, 0);
        String configName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除配置 \"" + configName + "\" 吗？\n此操作不可恢复！",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                configManager.deleteConfig(configId);
                loadConfigs();
                configChanged = true;
                JOptionPane.showMessageDialog(this,
                    "配置删除成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "删除配置失败: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setActiveLlm() {
        int selectedRow = configTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "请先选择要激活的配置",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String configId = (String) tableModel.getValueAt(selectedRow, 0);
        String configName = (String) tableModel.getValueAt(selectedRow, 1);

        try {
            configManager.setActiveLlm(configId);
            configManager.saveConfig(configManager.getConfig(configId)); // 触发保存
            loadConfigs();
            configChanged = true;
            JOptionPane.showMessageDialog(this,
                "已将 \"" + configName + "\" 设为激活的LLM",
                "成功",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "设置激活LLM失败: " + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshConfigs() {
        configManager.reload();
        loadConfigs();
        JOptionPane.showMessageDialog(this,
            "配置已刷新",
            "提示",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveToolchainSettings() {
        try {
            configManager.setMaxRetries((Integer) maxRetriesSpinner.getValue());
            configManager.setEnableSyntaxPreCheck(syntaxPreCheckBox.isSelected());

            // 触发保存（通过保存当前激活的配置）
            LlmConfigModel activeConfig = configManager.getActiveConfig();
            if (activeConfig != null) {
                configManager.saveConfig(activeConfig);
            }

            configChanged = true;
            JOptionPane.showMessageDialog(this,
                "工具链设置已保存！",
                "成功",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "保存设置失败: " + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfigChanged() {
        return configChanged;
    }
}
