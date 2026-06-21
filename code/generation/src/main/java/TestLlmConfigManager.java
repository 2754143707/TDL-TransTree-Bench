import javax.swing.*;

/**
 * LLM配置管理测试程序
 * 用于测试LLM配置的添加、编辑、删除功能
 */
public class TestLlmConfigManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 创建主窗口
            JFrame frame = new JFrame("LLM配置管理测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);

            // 创建面板
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel("LLM配置管理系统");
            titleLabel.setFont(titleLabel.getFont().deriveFont(24f));
            titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

            JButton openManagerButton = new JButton("🔧 打开配置管理器");
            openManagerButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
            openManagerButton.setFont(openManagerButton.getFont().deriveFont(16f));

            openManagerButton.addActionListener(e -> {
                LlmConfigManagerDialog dialog = new LlmConfigManagerDialog(frame);
                dialog.setVisible(true);
            });

            panel.add(titleLabel);
            panel.add(Box.createVerticalStrut(30));
            panel.add(openManagerButton);

            JTextArea infoArea = new JTextArea();
            infoArea.setEditable(false);
            infoArea.setText(
                "功能说明：\n\n" +
                "1. ➕ 添加新LLM：创建新的LLM配置\n" +
                "2. ✏️ 编辑：修改现有配置\n" +
                "3. 🗑️ 删除：删除不需要的配置\n" +
                "4. ✅ 设为激活：设置当前使用的LLM\n" +
                "5. 🔄 刷新：重新加载配置文件\n\n" +
                "配置文件位置：\n" +
                "config/llm-config.properties"
            );
            infoArea.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
            infoArea.setLineWrap(true);
            infoArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(infoArea);
            scrollPane.setAlignmentX(JScrollPane.CENTER_ALIGNMENT);

            panel.add(Box.createVerticalStrut(20));
            panel.add(scrollPane);

            frame.add(panel);
            frame.setVisible(true);

            // 显示当前配置信息
            LlmConfigManager manager = LlmConfigManager.getInstance();
            System.out.println("=== LLM配置管理器启动 ===");
            System.out.println("已加载配置数量: " + manager.getAllConfigs().size());
            System.out.println("当前激活LLM: " + manager.getActiveLlmId());
            System.out.println("配置列表:");
            for (LlmConfigModel config : manager.getAllConfigs()) {
                System.out.println("  - " + config.getId() + ": " + config.getName() +
                    " [" + (config.isEnabled() ? "启用" : "禁用") + "]");
            }
        });
    }
}
