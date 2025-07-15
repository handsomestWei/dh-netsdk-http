package com.netsdk.demo.frame.Gate.ext;

import com.netsdk.common.LoginPanel;
import com.netsdk.demo.module.GateModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.netsdk.common.BorderEx;
import com.netsdk.common.Res;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.demo.module.ext.GateExtModule;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

    /*
     * 注意：
     * 1. 查询不支持分页
     */
public class CardManageExtDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private LoginPanel loginPanel;
    private JTextField tfCardNo = new JTextField(10);
    private JTextField tfUserId = new JTextField(10);
    private JButton btnQuery, btnAdd, btnModify, btnDelete, btnClear, btnQuickAdd;
    private NetSDKLib.LLong loginHandle = new NetSDKLib.LLong(0);

    // 断线重连回调
    private static final NetSDKLib.fDisConnect disConnect = new DisConnect();
    private static final NetSDKLib.fHaveReConnect haveReConnect = new HaveReConnect();

    // 查询条件面板相关字段
    private JTextField tfMaxCount;

    public CardManageExtDialog() {
        // SDK初始化
        LoginModule.init(disConnect, haveReConnect);

        setTitle("扩展：门禁卡管理");
        setSize(950, 500);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new BorderLayout());

        // 登录面板
        loginPanel = new LoginPanel();
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(loginPanel);
        add(topPanel, BorderLayout.NORTH);

        // 查询条件和操作按钮
        JPanel queryPanel = new JPanel(new FlowLayout());
        JLabel lblCardNo = new JLabel("卡号:");
        queryPanel.add(lblCardNo);
        queryPanel.add(tfCardNo);
        queryPanel.add(new JLabel("用户ID:"));
        queryPanel.add(tfUserId);
        JLabel lblMaxCount = new JLabel("最大查询条数:");
        tfMaxCount = new JTextField("100", 6);
        queryPanel.add(lblMaxCount);
        queryPanel.add(tfMaxCount);
        btnQuery = new JButton("查询");
        btnAdd = new JButton("添加");
        btnModify = new JButton("修改");
        btnDelete = new JButton("删除");
        btnClear = new JButton("清空");
        btnQuickAdd = new JButton("快速添加");
        queryPanel.add(btnQuery);
        queryPanel.add(btnAdd);
        queryPanel.add(btnModify);
        queryPanel.add(btnDelete);
        queryPanel.add(btnClear);
        queryPanel.add(btnQuickAdd);
        topPanel.add(queryPanel);

        // 表格
        String[] columnNames = Res.string().getCardTable();
        String[] newColumnNames = new String[columnNames.length + 2];
        System.arraycopy(columnNames, 0, newColumnNames, 0, columnNames.length);
        newColumnNames[columnNames.length] = "门数目";
        newColumnNames[columnNames.length + 1] = "门序号";
        columnNames = newColumnNames;
        tableModel = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < columnNames.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(100);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);
        table.getColumnModel().getColumn(9).setPreferredWidth(100);
        table.getColumnModel().getColumn(10).setPreferredWidth(100);
        table.getColumnModel().getColumn(11).setPreferredWidth(150);
        table.getColumnModel().getColumn(12).setPreferredWidth(150);
        DefaultTableCellRenderer dCellRenderer = new DefaultTableCellRenderer();
        dCellRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, dCellRenderer);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 事件绑定
        btnQuery.addActionListener(e -> doQuery());
        btnAdd.addActionListener(e -> doAdd());
        btnModify.addActionListener(e -> doModify());
        btnDelete.addActionListener(e -> doDelete());
        btnClear.addActionListener(e -> doClear());
        btnQuickAdd.addActionListener(e -> {
            QuickAddCardDialog dialog = new QuickAddCardDialog(this);
            dialog.setVisible(true);
        });

        // 登录按钮事件
        loginPanel.addLoginBtnActionListener(e -> {
            if (!loginPanel.checkLoginText()) return;
            String ip = loginPanel.ipTextArea.getText().trim();
            int port = Integer.parseInt(loginPanel.portTextArea.getText().trim());
            String user = loginPanel.nameTextArea.getText().trim();
            String pwd = new String(loginPanel.passwordTextArea.getPassword());
            if (login(ip, port, user, pwd)) {
                loginPanel.setButtonEnable(true);
                btnQuery.setEnabled(true);
                btnAdd.setEnabled(true);
                btnModify.setEnabled(true);
                btnDelete.setEnabled(true);
                btnClear.setEnabled(true);
                btnQuickAdd.setEnabled(true);
                doQuery(); // 登录成功后自动查询
            }
        });
        // 登出按钮事件
        loginPanel.addLogoutBtnActionListener(e -> {
            logoutAndReset();
            btnQuery.setEnabled(false);
            btnAdd.setEnabled(false);
            btnModify.setEnabled(false);
            btnDelete.setEnabled(false);
            btnClear.setEnabled(false);
            btnQuickAdd.setEnabled(false);
            tableModel.setRowCount(0);
        });

        // 默认禁用操作按钮
        btnQuery.setEnabled(false);
        btnAdd.setEnabled(false);
        btnModify.setEnabled(false);
        btnDelete.setEnabled(false);
        btnClear.setEnabled(false);
        btnQuickAdd.setEnabled(false);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                logoutAndReset();
                com.netsdk.demo.module.LoginModule.cleanup();
                dispose();
            }
        });
    }

    private void doQuery() {
        tableModel.setRowCount(0);
        String cardNo = tfCardNo.getText().trim();
        String userId = tfUserId.getText().trim();
        int maxCount = 100;
        try {
            maxCount = Integer.parseInt(tfMaxCount.getText().trim());
            if (maxCount <= 0) maxCount = 100;
        } catch (Exception e) {
            maxCount = 100;
        }
        final int finalMaxCount = maxCount;
        SwingUtilities.invokeLater(() -> {
            java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD> cardList = GateExtModule.findCardList(loginHandle, cardNo, userId, finalMaxCount);
            if (cardList == null || cardList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "未查询到卡片数据", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            fillTableWithCardList(cardList);
        });
    }

    // 新增：表格填充方法
    private void fillTableWithCardList(java.util.List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD> cardList) {
        int index = 0;
        for (NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD card : cardList) {
            try {
                Vector<String> vector = new Vector<>();
                vector.add(String.valueOf(index + 1));
                vector.add(new String(card.szCardNo).trim());
                vector.add(new String(card.szCardName, "GBK").trim());
                vector.add(String.valueOf(card.nRecNo));
                vector.add(new String(card.szUserID).trim());
                vector.add(new String(card.szPsw).trim());
                vector.add(Res.string().getCardStatus(card.emStatus));
                vector.add(Res.string().getCardType(card.emType));
                vector.add(String.valueOf(card.nUserTime));
                vector.add(card.bFirstEnter == 1 ? Res.string().getFirstEnter() : Res.string().getNoFirstEnter());
                vector.add(card.bIsValid == 1 ? Res.string().getValid() : Res.string().getInValid());
                vector.add(card.stuValidStartTime.toStringTimeEx());
                vector.add(card.stuValidEndTime.toStringTimeEx());
                vector.add(String.valueOf(card.nDoorNum));
                StringBuilder doors = new StringBuilder();
                for (int d = 0; d < card.nDoorNum; d++) {
                    if (d > 0) doors.append(",");
                    doors.append(card.sznDoors[d]);
                }
                vector.add(doors.toString());
                tableModel.insertRow(index, vector);
                index++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // 移除多余空行
        if (tableModel.getRowCount() > cardList.size()) {
            for (int i = tableModel.getRowCount() - 1; i >= cardList.size(); i--) {
                tableModel.removeRow(i);
            }
        }
    }

    private void doAdd() {
        AddCardExtDialog dialog = new AddCardExtDialog();
        dialog.setVisible(true);
    }

    private void doModify() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的卡", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (tableModel.getValueAt(row, 3) == null || String.valueOf(tableModel.getValueAt(row, 3)).trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择有效卡", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        @SuppressWarnings("unchecked")
        Vector<String> vector = (Vector<String>) tableModel.getDataVector().get(row);
        ModifyCardExtDialog dialog = new ModifyCardExtDialog(vector);
        dialog.setVisible(true);
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的卡", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (tableModel.getValueAt(row, 3) == null || String.valueOf(tableModel.getValueAt(row, 3)).trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择有效卡", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        @SuppressWarnings("unchecked")
        Vector<String> v = (Vector<String>) tableModel.getDataVector().get(row);
        String recordNo = v.get(3).toString();
        String userId = v.get(4).toString();
        if (!GateModule.deleteFaceInfo(userId) || !GateModule.deleteCard(Integer.parseInt(recordNo))) {
            JOptionPane.showMessageDialog(this, ToolKits.getErrorCodeShow(), "错误", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
            tableModel.removeRow(row);
            table.updateUI();
        }
    }

    private void doClear() {
        int result = JOptionPane.showConfirmDialog(this, "确定要清空所有卡信息吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            if (!GateModule.clearFaceInfo() || !GateModule.clearCard()) {
                JOptionPane.showMessageDialog(this, ToolKits.getErrorCodeShow(), "错误", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "清空成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                tableModel.setRowCount(0);
                tableModel.setRowCount(18);
            }
        }
    }

    private boolean login(String ip, int port, String user, String pwd) {
        boolean result = com.netsdk.demo.module.LoginModule.login(ip, port, user, pwd);
        if (result) {
            loginHandle = com.netsdk.demo.module.LoginModule.m_hLoginHandle;
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "登录失败: " + ToolKits.getErrorCodeShow(), "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void logoutAndReset() {
        com.netsdk.demo.module.LoginModule.logout();
        loginPanel.setButtonEnable(false);
        tableModel.setRowCount(0);
    }

    // 断线回调
    private static class DisConnect implements NetSDKLib.fDisConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, com.sun.jna.Pointer dwUser) {
            System.out.printf("[CardManageExDialog] Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, Res.string().getDisConnect(), Res.string().getErrorMessage(), JOptionPane.WARNING_MESSAGE)
            );
        }
    }

    // 重连回调
    private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, com.sun.jna.Pointer dwUser) {
            System.out.printf("[CardManageExDialog] ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, Res.string().getReconnectSucceed(), Res.string().getPromptMessage(), JOptionPane.INFORMATION_MESSAGE)
            );
        }
    }

    // 快速添加卡片对话框
    class QuickAddCardDialog extends JDialog {
        private JTextField tfUserId, tfCardNo, tfCardType;
        private boolean ok = false;
        public QuickAddCardDialog(JDialog parent) {
            super(parent, "快速添加卡片", true);
            setSize(400, 220);
            setLocationRelativeTo(parent);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

            tfUserId = new JTextField(18);
            tfCardNo = new JTextField(18);
            tfCardType = new JTextField(18);

            mainPanel.add(createFieldPanel("用户ID:", tfUserId));
            mainPanel.add(createFieldPanel("卡号:", tfCardNo));
            mainPanel.add(createFieldPanel("卡类型:", tfCardType));

            // 按钮区
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            JButton btnOk = new JButton("确认");
            JButton btnCancel = new JButton("取消");
            btnPanel.add(btnOk);
            btnPanel.add(btnCancel);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(btnPanel);

            setContentPane(mainPanel);

            btnOk.addActionListener(e -> {
                String cardNo = tfCardNo.getText().trim();
                String userId = tfUserId.getText().trim();
                String cardTypeStr = tfCardType.getText().trim();
                int cardType = 0;
                try {
                    cardType = Integer.parseInt(cardTypeStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "卡类型应为数字", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean ret = GateExtModule.insertCardByService(loginHandle, cardNo, userId, cardType);
                if (ret) {
                    JOptionPane.showMessageDialog(this, "添加成功");
                    ok = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, ToolKits.getErrorCodeShow(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            btnCancel.addActionListener(e -> {
                ok = false;
                dispose();
            });
        }
        public boolean isOk() { return ok; }

        private JPanel createFieldPanel(String label, JTextField field) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel jLabel = new JLabel(label);
            jLabel.setPreferredSize(new Dimension(160, 25)); // 统一标签宽度
            panel.add(jLabel);
            panel.add(field);
            return panel;
        }
    }
} 