package com.netsdk.demo.frame.Gate.ext;

import com.netsdk.demo.module.LoginModule;
import com.netsdk.demo.module.ext.GateExtModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.common.Res;
import com.netsdk.common.LoginPanel;
import com.netsdk.lib.ToolKits;
import com.netsdk.lib.enumeration.EM_CFG_CAP_CMD;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;

    /*
     * 参考：设备网络SDK_C/Demo/MfcCategoryDemo/26.AccessControl2s和AccessControl2s.exe
     * 注意：
     * 1、设备侧可能不支持存储通道名称，查询接口返回通道名称字段可能为空
     */
public class AccessChannelInfoExtDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private LoginPanel loginPanel;

    // 断线重连回调
    private static final NetSDKLib.fDisConnect disConnect = new DisConnect();
    private static final NetSDKLib.fHaveReConnect haveReConnect = new HaveReConnect();

    public AccessChannelInfoExtDialog() {
        setTitle("扩展：门禁通道信息");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new BorderLayout());

        // SDK初始化
        System.out.println("[AccessChannelInfoExDialog] 初始化SDK...");
        LoginModule.init(disConnect, haveReConnect);

        // 复用LoginPanel
        loginPanel = new LoginPanel();
        add(loginPanel, BorderLayout.NORTH);

        String[] columnNames = {"通道号", "名称", "门禁状态"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 门禁控制按钮面板
        JPanel controlPanel = new JPanel();
        JButton btnOpen = new JButton("开门");
        JButton btnClose = new JButton("关门");
        JButton btnAlwaysOpen = new JButton("常开");
        JButton btnAlwaysClose = new JButton("常闭");
        JButton btnQuery = new JButton("查询");
        controlPanel.add(btnOpen);
        controlPanel.add(btnClose);
        controlPanel.add(btnAlwaysOpen);
        controlPanel.add(btnAlwaysClose);
        controlPanel.add(btnQuery);
        add(controlPanel, BorderLayout.SOUTH);

        // 登录按钮监听
        loginPanel.addLoginBtnActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!loginPanel.checkLoginText()) return;
                String ip = loginPanel.ipTextArea.getText().trim();
                int port = Integer.parseInt(loginPanel.portTextArea.getText().trim());
                String user = loginPanel.nameTextArea.getText().trim();
                String pwd = new String(loginPanel.passwordTextArea.getPassword());
                System.out.printf("[AccessChannelInfoExDialog] 尝试登录设备: ip=%s, port=%d, user=%s\n", ip, port, user);
                if (login(ip, port, user, pwd)) {
                    loginPanel.setButtonEnable(true); // 登录后禁用登录按钮，启用登出
                    tableModel.setRowCount(0);
                    loadChannelInfo();
                }
            }
        });

        // 登出按钮监听
        loginPanel.addLogoutBtnActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("[AccessChannelInfoExDialog] 用户点击登出");
                logoutAndReset();
            }
        });

        // 关闭窗口时自动登出和清理
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("[AccessChannelInfoExDialog] 窗口关闭，执行登出和SDK清理");
                logoutAndReset();
                LoginModule.cleanup();
                dispose();
            }
        });

        // 按钮事件
        btnOpen.addActionListener(e -> controlDoorSelectedChannel(1)); // 1: 开门
        btnClose.addActionListener(e -> controlDoorSelectedChannel(2)); // 2: 关门
        btnAlwaysOpen.addActionListener(e -> controlDoorSelectedChannel(3)); // 3: 常开
        btnAlwaysClose.addActionListener(e -> controlDoorSelectedChannel(4)); // 4: 常闭
        btnQuery.addActionListener(e -> loadChannelInfo());
    }

    // 断线回调
    private static class DisConnect implements NetSDKLib.fDisConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("[AccessChannelInfoExDialog] Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, Res.string().getDisConnect(), Res.string().getErrorMessage(), JOptionPane.WARNING_MESSAGE)
            );
        }
    }

    // 重连回调
    private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("[AccessChannelInfoExDialog] ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, Res.string().getReconnectSucceed(), Res.string().getPromptMessage(), JOptionPane.INFORMATION_MESSAGE)
            );
        }
    }

    // 参考Gate.java的login实现
    private boolean login(String ip, int port, String user, String pwd) {
        System.out.printf("[AccessChannelInfoExDialog] 调用LoginModule.login(ip=%s, port=%d, user=%s)\n", ip, port, user);
        boolean result = LoginModule.login(ip, port, user, pwd);
        System.out.printf("[AccessChannelInfoExDialog] LoginModule.login 返回: %s\n", result);
        if (result) {
            // 可根据LoginModule.m_stDeviceInfo.byChanNum等做后续处理
            // 这里只做通道信息表格刷新
            return true;
        } else {
            System.err.printf("[AccessChannelInfoExDialog] 登录失败, 错误: %s\n", ToolKits.getErrorCodeShow());
            JOptionPane.showMessageDialog(this, Res.string().getLoginFailed() + ", " + ToolKits.getErrorCodeShow(), Res.string().getErrorMessage(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void logoutAndReset() {
        System.out.println("[AccessChannelInfoExDialog] 调用LoginModule.logout()");
        LoginModule.logout();
        loginPanel.setButtonEnable(false); // 启用登录按钮，禁用登出
        tableModel.setRowCount(0);
    }

    /**
     * 优化：先通过能力集获取门禁通道数量，再逐个获取门禁通道信息
     */
    private void loadChannelInfo() {
        System.out.println("[AccessChannelInfoExDialog] 查询门禁通道数量...");
        int channelCount = GateExtModule.getAccessChannelCount(LoginModule.m_hLoginHandle);
        if (channelCount <= 0) {
            System.err.println("[AccessChannelInfoExDialog] 通道数量为0，无法查询门禁信息");
            return;
        }

        tableModel.setRowCount(0);
        List<NetSDKLib.CFG_ACCESS_EVENT_INFO> channelInfoList = com.netsdk.demo.module.ext.GateExtModule.getAllAccessChannelInfo(LoginModule.m_hLoginHandle, channelCount);
        for (int i = 0; i < channelInfoList.size(); i++) {
            NetSDKLib.CFG_ACCESS_EVENT_INFO info = channelInfoList.get(i);
            String name = new String(info.szChannelName).trim();
            tableModel.addRow(new Object[]{i, name, info.emState});
            System.out.printf("[AccessChannelInfoExDialog] 门%d: name=%s, 门禁状态=%d\n", i, name, info.emState);
        }
    }

    /**
     * 门禁控制操作
     * @param action 1-开门 2-关门 3-常开 4-常闭
     * @return 操作是否成功
     */
    private boolean controlDoorSelectedChannel(int action) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要操作的门禁通道", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        int channelNo = (int) tableModel.getValueAt(row, 0);
        switch (action) {
            case 1:
                return openDoor(channelNo);
            case 2:
                return closeDoor(channelNo);
            case 3:
                return alwaysOpenDoor(channelNo);
            case 4:
                return alwaysCloseDoor(channelNo);
            default:
                return false;
        }
    }

    // 开门
    private boolean openDoor(int channelNo) {
        boolean ret = com.netsdk.demo.module.ext.GateExtModule.openDoor(LoginModule.m_hLoginHandle, channelNo);
        if (ret) {
            JOptionPane.showMessageDialog(this, "开门成功: 通道号=" + channelNo);
        } else {
            JOptionPane.showMessageDialog(this, "开门失败: 通道号=" + channelNo + " 错误: " + com.netsdk.lib.ToolKits.getErrorCodeShow());
        }
        return ret;
    }

    // 关门
    private boolean closeDoor(int channelNo) {
        boolean ret = com.netsdk.demo.module.ext.GateExtModule.closeDoor(LoginModule.m_hLoginHandle, channelNo);
        if (ret) {
            JOptionPane.showMessageDialog(this, "关门成功: 通道号=" + channelNo);
        } else {
            JOptionPane.showMessageDialog(this, "关门失败: 通道号=" + channelNo + " 错误: " + com.netsdk.lib.ToolKits.getErrorCodeShow());
        }
        return ret;
    }

    // 常开
    private boolean alwaysOpenDoor(int channelNo) {
        boolean ret = com.netsdk.demo.module.ext.GateExtModule.alwaysOpenDoor(LoginModule.m_hLoginHandle, channelNo);
        if (ret) {
            JOptionPane.showMessageDialog(this, "常开设置成功: 通道号=" + channelNo);
        } else {
            JOptionPane.showMessageDialog(this, "常开设置失败: 通道号=" + channelNo + " 错误: " + com.netsdk.lib.ToolKits.getErrorCodeShow());
        }
        return ret;
    }

    // 常闭
    private boolean alwaysCloseDoor(int channelNo) {
        boolean ret = com.netsdk.demo.module.ext.GateExtModule.alwaysCloseDoor(LoginModule.m_hLoginHandle, channelNo);
        if (ret) {
            JOptionPane.showMessageDialog(this, "常闭设置成功: 通道号=" + channelNo);
        } else {
            JOptionPane.showMessageDialog(this, "常闭设置失败: 通道号=" + channelNo + " 错误: " + com.netsdk.lib.ToolKits.getErrorCodeShow());
        }
        return ret;
    }
} 