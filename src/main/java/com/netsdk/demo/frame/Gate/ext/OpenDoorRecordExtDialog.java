package com.netsdk.demo.frame.Gate.ext;

import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import com.netsdk.common.LoginPanel;
import com.netsdk.demo.module.LoginModule;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/*
 * 需关注问题
 * 1：查询接口不支持条件查询（如时间范围和卡号）,只提供全量分页查询
 * 2: 返回开门记录，时间字段为UTC时间，需要转换为UTC+8
 */
public class OpenDoorRecordExtDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private LoginPanel loginPanel;
    private JButton btnQuery;
    private JTextField tfStart, tfEnd, tfCard;
    private NetSDKLib.LLong loginHandle = new NetSDKLib.LLong(0);
    // 新增分页控件
    private int pageNum = 1;
    private int pageSize = 10;
    private int totalPage = 1;
    private JLabel pageLabel;
    private JButton btnPrev;
    private JButton btnNext;
    private JTextField tfPageSize;
    private JTextField tfJumpPage;
    private JButton btnJump;
    // 新增：总记录数标签
    private JLabel lblTotalCount = new JLabel("总记录数: 0");

    public OpenDoorRecordExtDialog() {
        setTitle("扩展：开门记录查询");
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
        JLabel lblStart = new JLabel("开始时间(yyyy-MM-dd HH:mm:ss):");
        tfStart = new JTextField(16);
        JLabel lblEnd = new JLabel("结束时间(yyyy-MM-dd HH:mm:ss):");
        tfEnd = new JTextField(16);
        JLabel lblCard = new JLabel("卡号(可选):");
        tfCard = new JTextField(10);
        btnQuery = new JButton("查询");
        btnQuery.setEnabled(false);
        tfStart.setEnabled(false);
        tfEnd.setEnabled(false);
        tfCard.setEnabled(false);
        // 设置默认时间为今天
        String[] today = getTodayTimeRange();
        tfStart.setText(today[0]);
        tfEnd.setText(today[1]);
        queryPanel.add(lblStart); queryPanel.add(tfStart);
        queryPanel.add(lblEnd); queryPanel.add(tfEnd);
        queryPanel.add(lblCard); queryPanel.add(tfCard);
        queryPanel.add(btnQuery);
        // 合并顶部面板
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(loginPanel);
        topPanel.add(queryPanel);
        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columnNames = {"序号", "卡号", "用户ID", "事件时间", "通道号", "开门方式", "结果", "错误码"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        // 设置表格列宽
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // "序号"列宽（3位数字）
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        // "事件时间"列宽（完整时间）
        table.getColumnModel().getColumn(3).setPreferredWidth(180);

        // 在窗体底部增加分页控件
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel pagePanel = new JPanel();
        btnPrev = new JButton("上一页");
        btnNext = new JButton("下一页");
        pageLabel = new JLabel();
        tfPageSize = new JTextField(String.valueOf(pageSize), 4);
        tfJumpPage = new JTextField(4);
        btnJump = new JButton("跳转");
        pagePanel.add(btnPrev);
        pagePanel.add(btnNext);
        pagePanel.add(new JLabel("每页数量:"));
        pagePanel.add(tfPageSize);
        pagePanel.add(pageLabel);
        pagePanel.add(new JLabel("跳转到第"));
        pagePanel.add(tfJumpPage);
        pagePanel.add(new JLabel("页"));
        pagePanel.add(btnJump);
        bottomPanel.add(pagePanel, BorderLayout.CENTER);
        bottomPanel.add(lblTotalCount, BorderLayout.EAST);
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
                tfStart.setEnabled(true);
                tfEnd.setEnabled(true);
                tfCard.setEnabled(true);
            }
        });

        // 登出按钮事件
        loginPanel.addLogoutBtnActionListener(e -> {
            logoutAndReset();
            btnQuery.setEnabled(false);
            tfStart.setEnabled(false);
            tfEnd.setEnabled(false);
            tfCard.setEnabled(false);
            tableModel.setRowCount(0);
        });

        // 查询按钮事件
        btnQuery.addActionListener(e -> {
            tableModel.setRowCount(0);
            String start = tfStart.getText().trim();
            String end = tfEnd.getText().trim();
            String cardNo = tfCard.getText().trim();
            if (start.isEmpty() || end.isEmpty()) {
                JOptionPane.showMessageDialog(OpenDoorRecordExtDialog.this, "请输入起止时间", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            queryOpenDoorRecordsWithPage();
        });

        // 关闭窗口时自动登出和清理
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                logoutAndReset();
                LoginModule.cleanup();
                dispose();
            }
        });

        btnPrev.addActionListener(e -> {
            if (pageNum > 1) {
                pageNum--;
                queryOpenDoorRecordsWithPage();
            }
        });
        btnNext.addActionListener(e -> {
            if (pageNum < totalPage) {
                pageNum++;
                queryOpenDoorRecordsWithPage();
            }
        });

        // 每页数量输入监听
        tfPageSize.addActionListener(e -> {
            try {
                int newSize = Integer.parseInt(tfPageSize.getText().trim());
                if (newSize > 0) {
                    pageSize = newSize;
                    tfPageSize.setText(String.valueOf(pageSize)); // 强制刷新文本内容
                    pageNum = 1;
                    queryOpenDoorRecordsWithPage();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的每页数量", "提示", JOptionPane.WARNING_MESSAGE);
                tfPageSize.setText(String.valueOf(pageSize)); // 恢复为当前有效值
            }
        });
        // 新增：失去焦点时自动生效
        tfPageSize.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                try {
                    int newSize = Integer.parseInt(tfPageSize.getText().trim());
                    if (newSize > 0 && newSize != pageSize) {
                        pageSize = newSize;
                        tfPageSize.setText(String.valueOf(pageSize));
                        pageNum = 1;
                        queryOpenDoorRecordsWithPage();
                    }
                } catch (NumberFormatException ex) {
                    tfPageSize.setText(String.valueOf(pageSize));
                }
            }
        });
        // 跳转页号监听
        btnJump.addActionListener(e -> {
            try {
                int jumpTo = Integer.parseInt(tfJumpPage.getText().trim());
                if (jumpTo > 0 && jumpTo <= totalPage) {
                    pageNum = jumpTo;
                    queryOpenDoorRecordsWithPage();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的页号", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    // 登录逻辑
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

    // 新增：表格填充方法
    private void fillTableWithRecords(List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> records, int pageNum, int pageSize) {
        if (records == null || records.isEmpty()) {
            tableModel.setRowCount(0);
            table.repaint();
            return;
        }
        int startIndex = (pageNum - 1) * pageSize;
        int index = startIndex + 1;
        tableModel.setRowCount(0);
        for (NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC rec : records) {
            String card = new String(rec.szCardNo).trim();
            String userId = new String(rec.szUserID).trim();
            String eventTime = rec.stuTime != null ? rec.stuTime.toStringTimeEx() : "";
            int channel = rec.nDoor;
            int openMethod = rec.emMethod;
            int result = rec.bStatus;
            int errCode = rec.nErrorCode;
            tableModel.addRow(new Object[]{index++, card, userId, eventTime, channel, openMethod, result, errCode});
        }
        table.repaint();
        // 更新分页信息
        pageLabel.setText("第" + pageNum + "/" + totalPage + "页  每页:" + pageSize);
    }

    // 查询开门记录总数
    private int getTotalRecordCount(String start, String end, String cardNo) {
        return com.netsdk.demo.module.ext.GateExtModule.getOpenDoorRecordCount(loginHandle, start, end, cardNo);
    }

    // 查询开门记录，带分页
    private List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> queryOpenDoorRecords(String start, String end, String cardNo, int pageNum, int pageSize) {
        return com.netsdk.demo.module.ext.GateExtModule.getOpenDoorRecords(loginHandle, start, end, cardNo, pageNum, pageSize);
    }

    // 分页查询入口
    private void queryOpenDoorRecordsWithPage() {
        try {
            int inputPageSize = Integer.parseInt(tfPageSize.getText().trim());
            if (inputPageSize > 0) {
                pageSize = inputPageSize;
            }
        } catch (NumberFormatException ex) {
            // 保持原pageSize
        }
        String start = tfStart.getText().trim();
        String end = tfEnd.getText().trim();
        String cardNo = tfCard.getText().trim();
        // 1. 获取总记录数
        int totalCount = getTotalRecordCount(start, end, cardNo);
        totalPage = (totalCount + pageSize - 1) / pageSize;
        if (totalPage < 1) totalPage = 1;
        if (pageNum > totalPage) pageNum = totalPage;
        if (pageNum < 1) pageNum = 1;
        // 2. 查询当前页数据
        List<NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARDREC> records = queryOpenDoorRecords(start, end, cardNo, pageNum, pageSize);
        fillTableWithRecords(records, pageNum, pageSize);
        // 3. 更新总记录数标签
        lblTotalCount.setText("总记录数: " + totalCount);
    }

    // 获取当天00:00:00和23:59:59字符串
    private String[] getTodayTimeRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        return new String[] { today + " 00:00:00", today + " 23:59:59" };
    }
} 