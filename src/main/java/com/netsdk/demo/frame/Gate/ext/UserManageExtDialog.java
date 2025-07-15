package com.netsdk.demo.frame.Gate.ext;

import com.netsdk.common.LoginPanel;
import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKitEx;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import com.netsdk.demo.module.GateModule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

    /*
     * 注意
     * 1: 点击查询经常发生Invalid memory access，可能dll本身接口封装有问题
     * 2：人员必须要有门禁权限（关联通道和有效期），白版人员（只有信息没有下发门禁权限）的查不到
     * 3：某些字段信息，设备可能不支持存储（如卡号、部门名称等），查询接口返回对应字段值为空。需要在上层业务自己维护用户Id和对应信息的映射关系
     * 4：查询接口不支持分页，查询结果全部返回，需要在上层业务自己实现分页逻辑
     */
public class UserManageExtDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private LoginPanel loginPanel;
    private JButton btnQuery;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnQuickEdit;
    private JTextField tfUserId;
    private JTextField tfMaxCount;
    private NetSDKLib.LLong loginHandle = new NetSDKLib.LLong(0);
    private JLabel labelTotalCount;

    public UserManageExtDialog() {
        setTitle("扩展：门禁用户管理");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new BorderLayout());

        // SDK初始化
        LoginModule.init(null, null);

        // 登录面板
        loginPanel = new LoginPanel();

        // 查询条件面板
        JPanel queryPanel = new JPanel(new FlowLayout());
        JLabel lblUserId = new JLabel("用户ID(可选):");
        tfUserId = new JTextField(10);
        JLabel lblMaxCount = new JLabel("最大查询条数:");
        tfMaxCount = new JTextField("100", 6);
        btnQuery = new JButton("查询");
        btnQuery.setEnabled(false);
        tfUserId.setEnabled(false);
        queryPanel.add(lblUserId); queryPanel.add(tfUserId);
        queryPanel.add(lblMaxCount); queryPanel.add(tfMaxCount);
        queryPanel.add(btnQuery);

        // 新增操作按钮
        btnAdd = new JButton("快速添加");
        btnEdit = new JButton("修改");
        btnDelete = new JButton("删除");
        btnClear = new JButton("清空");
        btnQuickEdit = new JButton("快速编辑");
        btnQuickEdit.setEnabled(false);
        queryPanel.add(btnAdd);
        queryPanel.add(btnEdit);
        queryPanel.add(btnQuickEdit);
        queryPanel.add(btnDelete);
        queryPanel.add(btnClear);
        // 合并顶部面板，避免遮挡
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(loginPanel);
        topPanel.add(queryPanel);
        // 总记录数标签
        labelTotalCount = new JLabel("总记录数: -");
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countPanel.add(labelTotalCount);
        topPanel.add(countPanel);
        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columnNames = {"选择", "序号", "用户ID", "用户名", "卡号", "权限", "类型", "有效期起", "有效期止", "状态", "门数目", "门序号"};
        tableModel = new DefaultTableModel(null, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
            public boolean isCellEditable(int row, int column) { return column == 0; }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        // 设置表格列宽
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(40); // 选择
        table.getColumnModel().getColumn(1).setPreferredWidth(40); // 序号
        table.getColumnModel().getColumn(5).setPreferredWidth(40); // 权限
        table.getColumnModel().getColumn(6).setPreferredWidth(40); // 类型
        table.getColumnModel().getColumn(7).setPreferredWidth(140); // 有效期起
        table.getColumnModel().getColumn(8).setPreferredWidth(140); // 有效期止

        // 移除分页控件，仅保留底部空白或自定义内容
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);

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
                tfUserId.setEnabled(true);
                btnAdd.setEnabled(true);
                btnEdit.setEnabled(true);
                btnDelete.setEnabled(true);
                btnClear.setEnabled(true);
                btnQuickEdit.setEnabled(true);
            }
        });

        // 登出按钮事件
        loginPanel.addLogoutBtnActionListener(e -> {
            logoutAndReset();
            btnQuery.setEnabled(false);
            tfUserId.setEnabled(false);
            btnAdd.setEnabled(false);
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
            btnClear.setEnabled(false);
            btnQuickEdit.setEnabled(false);
            tableModel.setRowCount(0);
        });

        // 查询按钮事件
        btnQuery.addActionListener(e -> {
            tableModel.setRowCount(0);
            queryUserRecords();
        });

        // 添加按钮事件
        btnAdd.addActionListener(e -> {
            QuickAddUserDialog dialog = new QuickAddUserDialog(this);
            dialog.setVisible(true);
            if (dialog.isOk()) {
                String userId = dialog.getUserId();
                String authority = dialog.getAuthority();
                String userType = dialog.getUserType();
                String channelIds = dialog.getChannelIds();
                String validFrom = dialog.getValidFrom();
                String validTo = dialog.getValidTo();
                if (quickAddUser(userId, authority, userType, channelIds, validFrom, validTo)) {
                    JOptionPane.showMessageDialog(this, "添加成功");
                    btnQuery.doClick();
                } else {
                    JOptionPane.showMessageDialog(this, "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 修改按钮事件
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要修改的用户");
                return;
            }
            NetSDKLib.NET_ACCESS_USER_INFO oldUserInfo = getUserInfoFromTable(row);
            QuickEditUserDialog dialog = new QuickEditUserDialog(this, oldUserInfo);
            dialog.setVisible(true);
            if (dialog.isOk()) {
                String userId = dialog.getUserId();
                String authority = dialog.getAuthority();
                String userType = dialog.getUserType();
                String channelIds = dialog.getChannelIds();
                String validFrom = dialog.getValidFrom();
                String validTo = dialog.getValidTo();
                if (quickEditUser(userId, authority, userType, channelIds, validFrom, validTo)) {
                    JOptionPane.showMessageDialog(this, "修改成功");
                    btnQuery.doClick();
                } else {
                    JOptionPane.showMessageDialog(this, "修改失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 删除按钮事件
        btnDelete.addActionListener(e -> {
            List<String> userIds = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
                if (checked != null && checked) {
                    String userId = (String) tableModel.getValueAt(i, 2);
                    userIds.add(userId);
                }
            }
            if (userIds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先勾选要删除的用户", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            deleteUsers(userIds);
        });

        // 清空按钮事件
        btnClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要清空所有用户吗？", "确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (clearAllUsers()) {
                    JOptionPane.showMessageDialog(this, "清空成功");
                    btnQuery.doClick();
                }
            }
        });

        // 监听勾选变化，控制按钮可用性
        tableModel.addTableModelListener(e -> {
            int selectedCount = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
                if (checked != null && checked) selectedCount++;
            }
            btnDelete.setEnabled(selectedCount > 0);
            btnEdit.setEnabled(selectedCount == 1);
            btnAdd.setEnabled(selectedCount == 0);
            btnQuickEdit.setEnabled(selectedCount == 1);
        });

        // 快速编辑按钮事件
        btnQuickEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要快速编辑的用户");
                return;
            }
            NetSDKLib.NET_ACCESS_USER_INFO oldUserInfo = getUserInfoFromTable(row);
            QuickEditUserDialog dialog = new QuickEditUserDialog(this, oldUserInfo);
            dialog.setVisible(true);
            if (dialog.isOk()) {
                String userId = dialog.getUserId();
                String authority = dialog.getAuthority();
                String userType = dialog.getUserType();
                String channelIds = dialog.getChannelIds();
                String validFrom = dialog.getValidFrom();
                String validTo = dialog.getValidTo();
                if (quickEditUser(userId, authority, userType, channelIds, validFrom, validTo)) {
                    JOptionPane.showMessageDialog(this, "快速编辑成功");
                    btnQuery.doClick();
                } else {
                    JOptionPane.showMessageDialog(this, "快速编辑失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 关闭窗口时自动登出和清理
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                logoutAndReset();
                LoginModule.cleanup();
                dispose();
            }
        });
    }

    private boolean login(String ip, int port, String user, String pwd) {
        boolean result = LoginModule.login(ip, port, user, pwd);
        if (result) {
            loginHandle = LoginModule.m_hLoginHandle;
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "登录失败: " + ToolKits.getErrorCodeShow(), "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void logoutAndReset() {
        LoginModule.logout();
        loginPanel.setButtonEnable(false);
        tableModel.setRowCount(0);
    }

    // 查询数据，返回用户列表（无UI操作）
    private List<NetSDKLib.NET_ACCESS_USER_INFO> queryUserRecordsData(String userId, int maxCount) {
        return com.netsdk.demo.module.ext.GateExtModule.getUserRecords(loginHandle, userId, maxCount);
    }

    // 填充表格
    private void fillTableWithUserRecords(List<NetSDKLib.NET_ACCESS_USER_INFO> userList) {
        int index = 1;
        tableModel.setRowCount(0);
        for (NetSDKLib.NET_ACCESS_USER_INFO user : userList) {
            String uid = new String(user.szUserID).trim();
            String uname = new String(user.szName).trim();
            String card = ""; // 若有卡号字段可补充
            String authority = String.valueOf(user.emAuthority);
            String type = String.valueOf(user.emUserType);
            String validFrom = user.stuValidBeginTime != null ? user.stuValidBeginTime.toStringTimeEx() : "";
            String validTo = user.stuValidEndTime != null ? user.stuValidEndTime.toStringTimeEx() : "";
            int status = user.nUserStatus;
            String doorNum = String.valueOf(user.nDoorNum);
            StringBuilder doors = new StringBuilder();
            for (int d = 0; d < user.nDoorNum; d++) {
                if (d > 0) doors.append(",");
                doors.append(user.nDoors[d]);
            }
            String doorList = doors.toString();
            tableModel.addRow(new Object[]{false, index++, uid, uname, card, authority, type, validFrom, validTo, status, doorNum, doorList});
        }
    }

    // 查询总记录数接口
    private int getUserRecordsTotalCount(String userId) {
        return com.netsdk.demo.module.ext.GateExtModule.getUserRecordsCount(loginHandle, userId);
    }

    // 用户信息分页查询与表格填充
    private void queryUserRecords() {
        String userId = tfUserId.getText().trim();
        int maxCount = 100;
        try {
            maxCount = Integer.parseInt(tfMaxCount.getText().trim());
            if (maxCount <= 0) maxCount = 100;
        } catch (Exception e) {
            maxCount = 100;
        }
        // 先查总数并展示
        int totalCount = 0;
        try {
            totalCount = getUserRecordsTotalCount(userId);
        } catch (Exception e) {
            totalCount = 0;
        }
        labelTotalCount.setText("总记录数: " + totalCount);
        // 再查明细
        List<NetSDKLib.NET_ACCESS_USER_INFO> userList;
        try {
            userList = queryUserRecordsData(userId, maxCount);
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(this, ise.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        fillTableWithUserRecords(userList);
    }

    // 新增/修改用户
    private boolean addUser(NetSDKLib.NET_ACCESS_USER_INFO userInfo) {
        boolean ret = com.netsdk.demo.module.ext.GateExtModule.addOrUpdateUser(loginHandle, userInfo);
        if (!ret) {
            JOptionPane.showMessageDialog(this, "新增/修改用户失败: " + com.netsdk.lib.ToolKits.getErrorCodePrint().trim(), "错误", JOptionPane.ERROR_MESSAGE);
        }
        return ret;
    }

    // 删除用户
    private boolean deleteUser(NetSDKLib.NET_ACCESS_USER_INFO userInfo) {
        boolean ret = com.netsdk.demo.module.ext.GateExtModule.deleteUser(loginHandle, userInfo.szUserID);
        if (!ret) {
            JOptionPane.showMessageDialog(this, "删除用户失败: " + com.netsdk.lib.ToolKits.getErrorCodePrint().trim(), "错误", JOptionPane.ERROR_MESSAGE);
        }
        return ret;
    }

    // 从表格行获取用户信息
    private NetSDKLib.NET_ACCESS_USER_INFO getUserInfoFromTable(int row) {
        NetSDKLib.NET_ACCESS_USER_INFO user = new NetSDKLib.NET_ACCESS_USER_INFO();
        ToolKits.StringToByteArray(tableModel.getValueAt(row, 2).toString(), user.szUserID); // 用户ID
        ToolKits.StringToByteArray(tableModel.getValueAt(row, 3).toString(), user.szName);   // 用户名
        // 权限
        try { user.emAuthority = Integer.parseInt(tableModel.getValueAt(row, 5).toString()); } catch (Exception e) { user.emAuthority = 0; }
        // 类型
        try { user.emUserType = Integer.parseInt(tableModel.getValueAt(row, 6).toString()); } catch (Exception e) { user.emUserType = 0; }
        // 有效期起
        user.stuValidBeginTime = ToolKitEx.parseDateTime(tableModel.getValueAt(row, 7).toString());
        // 有效期止
        user.stuValidEndTime = ToolKitEx.parseDateTime(tableModel.getValueAt(row, 8).toString());
        // 状态
        try { user.nUserStatus = Integer.parseInt(tableModel.getValueAt(row, 9).toString()); } catch (Exception e) { user.nUserStatus = 0; }
        // 门数目和门序号
        try {
            user.nDoorNum = Integer.parseInt(tableModel.getValueAt(row, 10).toString());
            String doorsStr = tableModel.getValueAt(row, 11).toString();
            if (!doorsStr.isEmpty()) {
                String[] parts = doorsStr.split(",");
                for (int i = 0; i < parts.length && i < user.nDoorNum; i++) {
                    user.nDoors[i] = Integer.parseInt(parts[i].trim());
                }
            }
        } catch (Exception e) { user.nDoorNum = 0; }
        return user;
    }

    // 用户编辑对话框
    class UserEditDialog extends JDialog {
        private JTextField tfUserId, tfAuthority, tfUserType, tfCardNo, tfChannelIds, tfValidFrom, tfValidTo;
        private boolean ok = false;
        private NetSDKLib.NET_ACCESS_USER_INFO userInfo;
        public UserEditDialog(JDialog parent, NetSDKLib.NET_ACCESS_USER_INFO userInfo) {
            super(parent, "用户信息", true);
            setSize(500, 360);
            setLocationRelativeTo(parent);
            JPanel contentPanel = new JPanel(new GridLayout(8, 2, 10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            setContentPane(contentPanel);
            add(new JLabel("用户ID:"));
            tfUserId = new JTextField();
            add(tfUserId);
            add(new JLabel("用户权限:"));
            tfAuthority = new JTextField();
            add(tfAuthority);
            add(new JLabel("用户类型:"));
            tfUserType = new JTextField();
            add(tfUserType);
            add(new JLabel("卡号:"));
            tfCardNo = new JTextField();
            add(tfCardNo);
            add(new JLabel("门通道ID列表(逗号分隔):"));
            tfChannelIds = new JTextField();
            add(tfChannelIds);
            add(new JLabel("有效期开始时间(yyyy-MM-dd HH:mm:ss):"));
            tfValidFrom = new JTextField();
            add(tfValidFrom);
            add(new JLabel("有效期结束时间(yyyy-MM-dd HH:mm:ss):"));
            tfValidTo = new JTextField();
            add(tfValidTo);
            JButton btnOk = new JButton("确定");
            JButton btnCancel = new JButton("取消");
            add(btnOk); add(btnCancel);
            if (userInfo != null) {
                tfUserId.setText(new String(userInfo.szUserID).trim());
                tfAuthority.setText(String.valueOf(userInfo.emAuthority));
                tfValidFrom.setText(userInfo.stuValidBeginTime != null ? userInfo.stuValidBeginTime.toStringTimeEx() : "");
                tfValidTo.setText(userInfo.stuValidEndTime != null ? userInfo.stuValidEndTime.toStringTimeEx() : "");
            }
            btnOk.addActionListener(e -> {
                this.userInfo = new NetSDKLib.NET_ACCESS_USER_INFO();
                ToolKits.StringToByteArray(tfUserId.getText().trim(), this.userInfo.szUserID);
                this.userInfo.emAuthority = Integer.parseInt(tfAuthority.getText().trim());
                ok = true;
                dispose();
            });
            btnCancel.addActionListener(e -> {
                ok = false;
                dispose();
            });
        }
        public boolean isOk() { return ok; }
        public NetSDKLib.NET_ACCESS_USER_INFO getUserInfo() { return userInfo; }
    }

    private boolean clearAllUsers() {
        boolean ret = com.netsdk.demo.module.ext.GateExtModule.clearAllUsers(loginHandle);
        if (!ret) {
            JOptionPane.showMessageDialog(this, "清空失败: " + com.netsdk.lib.ToolKits.getErrorCodePrint().trim(), "错误", JOptionPane.ERROR_MESSAGE);
        }
        return ret;
    }

    // 新增：批量删除用户接口
    private void deleteUsers(List<String> userIds) {
        // 遍历userIds，逐个调用SDK删除接口，或一次性批量删除（如SDK支持）
        boolean allSuccess = true;
        for (String userId : userIds) {
            boolean success = deleteUser(getUserInfoFromTable(Integer.parseInt(userId) - 1));
            if (!success) allSuccess = false;
        }
        if (allSuccess) {
            JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "部分用户删除失败", "提示", JOptionPane.WARNING_MESSAGE);
        }
        // 刷新表格
        // ...重新查询/刷新...
    }
    
    /**
     * 快速增加用户
     * @param userId 用户ID
     * @param authority 用户权限
     * @param userType 用户类型
     * @param channelIds 门通道ID列表
     * @param validFrom 有效期开始时间
     * @param validTo 有效期结束时间
     * @return true表示成功，false表示失败
     */
    private boolean quickAddUser(String userId, String authority, String userType, String channelIds, String validFrom, String validTo) {
        // 组装用户信息结构体
        NetSDKLib.NET_ACCESS_USER_INFO userInfo = new NetSDKLib.NET_ACCESS_USER_INFO();
        ToolKits.StringToByteArray(userId, userInfo.szUserID);
        try { userInfo.emAuthority = Integer.parseInt(authority); } catch (Exception e) { userInfo.emAuthority = 0; }
        try { userInfo.emUserType = Integer.parseInt(userType); } catch (Exception e) { userInfo.emUserType = 0; }
        userInfo.stuValidBeginTime = ToolKitEx.parseDateTime(validFrom);
        userInfo.stuValidEndTime = ToolKitEx.parseDateTime(validTo);
        userInfo.nUserStatus = 0;
        // 解析门通道ID列表
        if (channelIds != null && !channelIds.isEmpty()) {
            String[] parts = channelIds.split(",");
            userInfo.nDoorNum = parts.length;
            userInfo.nTimeSectionNum = parts.length;
            for (int i = 0; i < parts.length; i++) {
                try {
                    userInfo.nDoors[i] = Integer.parseInt(parts[i].trim());
                    userInfo.nTimeSectionNo[i] = 255; // 全天有效
                } catch (Exception ex) {
                    userInfo.nDoors[i] = 0;
                }
            }
        } else {
            userInfo.nDoorNum = 0;
        }
        boolean userOk = addUser(userInfo);
        return userOk;
    }

    // 快速添加用户对话框
    class QuickAddUserDialog extends JDialog {
        private JTextField tfUserId, tfAuthority, tfUserType, tfChannelIds, tfValidFrom, tfValidTo;
        private boolean ok = false;
        public QuickAddUserDialog(JDialog parent) {
            super(parent, "快速添加用户", true);
            setSize(400, 300);
            setLocationRelativeTo(parent);
            JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            panel.add(new JLabel("用户ID:"));
            tfUserId = new JTextField();
            panel.add(tfUserId);
            panel.add(new JLabel("用户权限:"));
            tfAuthority = new JTextField();
            panel.add(tfAuthority);
            panel.add(new JLabel("用户类型:"));
            tfUserType = new JTextField();
            panel.add(tfUserType);
            panel.add(new JLabel("门通道ID列表(逗号分隔):"));
            tfChannelIds = new JTextField();
            panel.add(tfChannelIds);
            panel.add(new JLabel("有效期开始时间(yyyy-MM-dd HH:mm:ss):"));
            tfValidFrom = new JTextField();
            panel.add(tfValidFrom);
            panel.add(new JLabel("有效期结束时间(yyyy-MM-dd HH:mm:ss):"));
            tfValidTo = new JTextField();
            panel.add(tfValidTo);
            JButton btnOk = new JButton("确认");
            JButton btnCancel = new JButton("取消");
            panel.add(btnOk); panel.add(btnCancel);
            setContentPane(panel);
            btnOk.addActionListener(e -> {
                ok = true;
                dispose();
            });
            btnCancel.addActionListener(e -> {
                ok = false;
                dispose();
            });
        }
        public boolean isOk() { return ok; }
        public String getUserId() { return tfUserId.getText().trim(); }
        public String getAuthority() { return tfAuthority.getText().trim(); }
        public String getUserType() { return tfUserType.getText().trim(); }
        public String getChannelIds() { return tfChannelIds.getText().trim(); }
        public String getValidFrom() { return tfValidFrom.getText().trim(); }
        public String getValidTo() { return tfValidTo.getText().trim(); }
    }

    // 快速修改用户对话框
    class QuickEditUserDialog extends JDialog {
        private JTextField tfUserId, tfAuthority, tfUserType, tfChannelIds, tfValidFrom, tfValidTo;
        private boolean ok = false;
        public QuickEditUserDialog(JDialog parent, NetSDKLib.NET_ACCESS_USER_INFO userInfo) {
            super(parent, "快速修改用户", true);
            setSize(400, 300);
            setLocationRelativeTo(parent);
            JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            panel.add(new JLabel("用户ID:"));
            tfUserId = new JTextField();
            tfUserId.setEditable(false);
            panel.add(tfUserId);
            panel.add(new JLabel("用户权限:"));
            tfAuthority = new JTextField();
            panel.add(tfAuthority);
            panel.add(new JLabel("用户类型:"));
            tfUserType = new JTextField();
            panel.add(tfUserType);
            panel.add(new JLabel("门通道ID列表(逗号分隔):"));
            tfChannelIds = new JTextField();
            panel.add(tfChannelIds);
            panel.add(new JLabel("有效期开始时间(yyyy-MM-dd HH:mm:ss):"));
            tfValidFrom = new JTextField();
            panel.add(tfValidFrom);
            panel.add(new JLabel("有效期结束时间(yyyy-MM-dd HH:mm:ss):"));
            tfValidTo = new JTextField();
            panel.add(tfValidTo);
            JButton btnOk = new JButton("确认");
            JButton btnCancel = new JButton("取消");
            panel.add(btnOk); panel.add(btnCancel);
            setContentPane(panel);
            // 填充原有数据
            if (userInfo != null) {
                fillFieldsFromUserInfo(userInfo);
            }
            btnOk.addActionListener(e -> {
                ok = true;
                dispose();
            });
            btnCancel.addActionListener(e -> {
                ok = false;
                dispose();
            });
        }
        public boolean isOk() { return ok; }
        public String getUserId() { return tfUserId.getText().trim(); }
        public String getAuthority() { return tfAuthority.getText().trim(); }
        public String getUserType() { return tfUserType.getText().trim(); }
        public String getChannelIds() { return tfChannelIds.getText().trim(); }
        public String getValidFrom() { return tfValidFrom.getText().trim(); }
        public String getValidTo() { return tfValidTo.getText().trim(); }
        // 字段批量填充方法
        private void fillFieldsFromUserInfo(NetSDKLib.NET_ACCESS_USER_INFO userInfo) {
            tfUserId.setText(new String(userInfo.szUserID).trim());
            tfAuthority.setText(String.valueOf(userInfo.emAuthority));
            tfUserType.setText(String.valueOf(userInfo.emUserType));
            // 门通道ID
            StringBuilder channelIds = new StringBuilder();
            for (int i = 0; i < userInfo.nDoorNum; i++) {
                if (i > 0) channelIds.append(",");
                channelIds.append(userInfo.nDoors[i]);
            }
            tfChannelIds.setText(channelIds.toString());
            tfValidFrom.setText(userInfo.stuValidBeginTime != null ? userInfo.stuValidBeginTime.toStringTimeEx() : "");
            tfValidTo.setText(userInfo.stuValidEndTime != null ? userInfo.stuValidEndTime.toStringTimeEx() : "");
        }
    }

    // 快速修改用户接口
    private boolean quickEditUser(String userId, String authority, String userType, String channelIds, String validFrom, String validTo) {
        // 组装用户信息结构体
        NetSDKLib.NET_ACCESS_USER_INFO userInfo = new NetSDKLib.NET_ACCESS_USER_INFO();
        ToolKits.StringToByteArray(userId, userInfo.szUserID);
        try { userInfo.emAuthority = Integer.parseInt(authority); } catch (Exception e) { userInfo.emAuthority = 0; }
        try { userInfo.emUserType = Integer.parseInt(userType); } catch (Exception e) { userInfo.emUserType = 0; }
        userInfo.stuValidBeginTime = ToolKitEx.parseDateTime(validFrom);
        userInfo.stuValidEndTime = ToolKitEx.parseDateTime(validTo);
        userInfo.nUserStatus = 0;
        // 解析门通道ID列表
        if (channelIds != null && !channelIds.isEmpty()) {
            String[] parts = channelIds.split(",");
            userInfo.nDoorNum = parts.length;
            userInfo.nTimeSectionNum = parts.length;
            for (int i = 0; i < parts.length; i++) {
                try {
                    userInfo.nDoors[i] = Integer.parseInt(parts[i].trim());
                    userInfo.nTimeSectionNo[i] = 255; // 全天有效
                } catch (Exception ex) {
                    userInfo.nDoors[i] = 0;
                }
            }
        } else {
            userInfo.nDoorNum = 0;
        }
        boolean userOk = addUser(userInfo);
        return userOk;
    }
} 