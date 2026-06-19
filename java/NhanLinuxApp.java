import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class NhanLinuxApp {

    static final Color BG        = new Color(245, 245, 245);
    static final Color WHITE     = Color.WHITE;
    static final Color ACCENT    = new Color(37, 99, 235);
    static final Color SUCCESS   = new Color(22, 163, 74);
    static final Color DANGER    = new Color(220, 38, 38);
    static final Color TEXT      = new Color(30, 30, 30);
    static final Color TEXT_GRAY = new Color(120, 120, 120);
    static final Color BORDER    = new Color(220, 220, 220);

    static Font FONT_TITLE  = new Font("SansSerif", Font.BOLD, 22);
    static Font FONT_LABEL  = new Font("SansSerif", Font.BOLD, 13);
    static Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    static Font FONT_MONO   = new Font("Monospaced", Font.PLAIN, 12);

    static JTextArea outputArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Lập trình nhân Linux – Đề tài 23");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(860, 640);
            frame.setMinimumSize(new Dimension(760, 520));
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(BG);
            frame.setLayout(new BorderLayout(0, 0));

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(ACCENT);
            header.setBorder(new EmptyBorder(16, 24, 16, 24));
            JLabel title = new JLabel("Lập trình nhân Linux");
            title.setFont(FONT_TITLE);
            title.setForeground(WHITE);
            JLabel subtitle = new JLabel("Đề tài 23  ·  Shell  ·  C Program  ·  Kernel Module");
            subtitle.setFont(FONT_NORMAL);
            subtitle.setForeground(new Color(200, 220, 255));
            JPanel headerText = new JPanel();
            headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
            headerText.setOpaque(false);
            headerText.add(title);
            headerText.add(Box.createVerticalStrut(4));
            headerText.add(subtitle);
            header.add(headerText, BorderLayout.CENTER);
            frame.add(header, BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(FONT_LABEL);
            tabs.setBackground(BG);
            tabs.addTab("🖥  Shell", buildShellTab());
            tabs.addTab("⚙  C Program", buildCTab());
            tabs.addTab("🔧  Kernel Module", buildModuleTab());
            tabs.setBorder(new EmptyBorder(8, 8, 0, 8));

            JPanel outputPanel = new JPanel(new BorderLayout());
            outputPanel.setBorder(new CompoundBorder(
                new EmptyBorder(0, 8, 8, 8),
                new LineBorder(BORDER, 1, true)
            ));
            outputPanel.setBackground(WHITE);

            JPanel outputHeader = new JPanel(new BorderLayout());
            outputHeader.setBackground(new Color(235, 235, 235));
            outputHeader.setBorder(new EmptyBorder(6, 12, 6, 12));
            JLabel outputLabel = new JLabel("Output");
            outputLabel.setFont(FONT_LABEL);
            outputLabel.setForeground(TEXT_GRAY);
            JButton clearBtn = new JButton("Xóa");
            clearBtn.setFont(FONT_NORMAL);
            clearBtn.setForeground(TEXT_GRAY);
            clearBtn.setBorderPainted(false);
            clearBtn.setContentAreaFilled(false);
            clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            clearBtn.addActionListener(e -> outputArea.setText(""));
            outputHeader.add(outputLabel, BorderLayout.WEST);
            outputHeader.add(clearBtn, BorderLayout.EAST);

            outputArea = new JTextArea(8, 0);
            outputArea.setFont(FONT_MONO);
            outputArea.setEditable(false);
            outputArea.setBackground(WHITE);
            outputArea.setForeground(TEXT);
            outputArea.setBorder(new EmptyBorder(8, 12, 8, 12));
            JScrollPane outputScroll = new JScrollPane(outputArea);
            outputScroll.setBorder(null);

            outputPanel.add(outputHeader, BorderLayout.NORTH);
            outputPanel.add(outputScroll, BorderLayout.CENTER);

            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, outputPanel);
            split.setDividerLocation(340);
            split.setDividerSize(4);
            split.setBorder(null);
            frame.add(split, BorderLayout.CENTER);

            frame.setVisible(true);
            appendOutput("✅ Ứng dụng đã khởi động. Chọn tab và nhấn nút để chạy lệnh.\n");
        });
    }

    // ════════════════════════════════════════════════
    //  TAB 1 – SHELL
    // ════════════════════════════════════════════════
    static JPanel buildShellTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel desc = new JLabel("Chạy các script Shell quản lý hệ thống Ubuntu");
        desc.setFont(FONT_NORMAL);
        desc.setForeground(TEXT_GRAY);
        panel.add(desc, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);

        grid.add(makeCard("📁  Quản lý File",
            "Tạo, đọc, sửa, đổi tên,\nsao chép, nén, xóa file",
            ACCENT, () -> runInTerminal("~/NhanLinux/shell/file.sh")));

        grid.add(makeCard("⏰  Lập lịch Crontab",
            "Tạo, xóa, liệt kê\ncác tác vụ cron job",
            ACCENT, () -> runInTerminal("~/NhanLinux/shell/crontab.sh")));

        grid.add(makeCard("🕐  Thời gian hệ thống",
            "Xem và thiết lập thời gian\nthủ công hoặc tự động",
            ACCENT, () -> runSudoInTerminal("~/NhanLinux/shell/datetime.sh")));

        grid.add(makeCard("📦  Quản lý chương trình",
            "Cài đặt, gỡ bỏ, liệt kê\ncác gói phần mềm (apt)",
            ACCENT, () -> runInTerminal("~/NhanLinux/shell/program.sh")));

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════
    //  TAB 2 – C PROGRAM
    // ════════════════════════════════════════════════
    static JPanel buildCTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel desc = new JLabel("Biên dịch và chạy các chương trình C quản lý hệ thống");
        desc.setFont(FONT_NORMAL);
        desc.setForeground(TEXT_GRAY);
        panel.add(desc, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);

        grid.add(makeCard("📄  Quản lý File (C)",
            "Tạo file và ghi nội dung,\nđọc file, xóa file",
            new Color(79, 70, 229), () -> {
                runCommand("cd ~/NhanLinux/c && gcc file.c -o file");
                runInTerminal("~/NhanLinux/c/file");
            }));

        grid.add(makeCard("🔄  Quản lý tiến trình",
            "Xem danh sách tiến trình\nvà kill tiến trình theo PID",
            new Color(79, 70, 229), () -> {
                runCommand("cd ~/NhanLinux/c && gcc process.c -o process");
                runInTerminal("~/NhanLinux/c/process");
            }));

        grid.add(makeCard("🌐  Network Interface",
            "Liệt kê các interface mạng\nvà địa chỉ IP hiện tại",
            new Color(79, 70, 229), () -> {
                runCommand("cd ~/NhanLinux/c && gcc network.c -o network");
                runCommandAndShow("~/NhanLinux/c/network");
            }));

        // ── FIX: Mở cả server + 2 client terminal ──
        grid.add(makeCard("💬  Socket Chat",
            "Khởi động server + 2 client\nchat TCP qua port 8888",
            new Color(79, 70, 229), () -> {
                // Bước 1: Biên dịch cả server và client
                appendOutput("▶ Đang biên dịch server và client...\n");
                new Thread(() -> {
                    try {
                        Process compile = Runtime.getRuntime().exec(new String[]{
                            "bash", "-c",
                            "cd ~/NhanLinux/c/socket && " +
                            "gcc server.c -o server -lpthread && " +
                            "gcc client.c -o client -lpthread && " +
                            "echo COMPILE_OK"
                        });
                        BufferedReader br = new BufferedReader(
                            new InputStreamReader(compile.getInputStream()));
                        BufferedReader berr = new BufferedReader(
                            new InputStreamReader(compile.getErrorStream()));
                        StringBuilder errBuf = new StringBuilder();
                        String line;
                        boolean ok = false;
                        while ((line = br.readLine()) != null) {
                            if (line.equals("COMPILE_OK")) ok = true;
                        }
                        while ((line = berr.readLine()) != null) {
                            errBuf.append(line).append("\n");
                        }
                        compile.waitFor();

                        if (!ok) {
                            appendOutput("❌ Biên dịch thất bại:\n" + errBuf + "\n");
                            return;
                        }
                        appendOutput("✅ Biên dịch thành công!\n");

                        // Bước 2: Mở terminal Server
                        appendOutput("▶ Mở terminal Server (port 8888)...\n");
                        Runtime.getRuntime().exec(new String[]{
                            "bash", "-c",
                            "x-terminal-emulator -T 'Socket Server' -e bash -c " +
                            "'echo === SERVER === && ~/NhanLinux/c/socket/server 8888; " +
                            "echo; read -p \"Nhấn Enter để đóng...\" ' &"
                        });

                        // Đợi 1.5 giây để server khởi động trước
                        Thread.sleep(1500);

                        // Bước 3: Mở terminal Client 1
                        appendOutput("▶ Mở terminal Client 1...\n");
                        Runtime.getRuntime().exec(new String[]{
                            "bash", "-c",
                            "x-terminal-emulator -T 'Socket Client 1' -e bash -c " +
                            "'echo === CLIENT 1 === && ~/NhanLinux/c/socket/client 127.0.0.1 8888; " +
                            "echo; read -p \"Nhấn Enter để đóng...\" ' &"
                        });

                        // Đợi thêm 0.5 giây
                        Thread.sleep(500);

                        // Bước 4: Mở terminal Client 2
                        appendOutput("▶ Mở terminal Client 2...\n");
                        Runtime.getRuntime().exec(new String[]{
                            "bash", "-c",
                            "x-terminal-emulator -T 'Socket Client 2' -e bash -c " +
                            "'echo === CLIENT 2 === && ~/NhanLinux/c/socket/client 127.0.0.1 8888; " +
                            "echo; read -p \"Nhấn Enter để đóng...\" ' &"
                        });

                        appendOutput("✅ Đã mở 3 terminal: 1 Server + 2 Client.\n");
                        appendOutput("💡 Gõ tin nhắn ở Client 1 hoặc Client 2, Server sẽ nhận và relay.\n");
                        appendOutput("─────────────────────────────────\n");

                    } catch (Exception ex) {
                        appendOutput("❌ Lỗi: " + ex.getMessage() + "\n");
                    }
                }).start();
            }));

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════
    //  TAB 3 – KERNEL MODULE
    // ════════════════════════════════════════════════
    static JPanel buildModuleTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel desc = new JLabel("Build và nạp module nhân Linux sắp xếp mảng");
        desc.setFont(FONT_NORMAL);
        desc.setForeground(TEXT_GRAY);
        panel.add(desc, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        JLabel lbValues = new JLabel("Mảng cần sắp xếp:");
        lbValues.setFont(FONT_LABEL);
        form.add(lbValues, gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        JTextField tfValues = new JTextField("5,3,1,4,2", 20);
        tfValues.setFont(FONT_MONO);
        form.add(tfValues, gc);
        gc.fill = GridBagConstraints.NONE; gc.weightx = 0;

        gc.gridx = 0; gc.gridy = 1;
        JLabel lbAlgo = new JLabel("Thuật toán:");
        lbAlgo.setFont(FONT_LABEL);
        form.add(lbAlgo, gc);
        gc.gridx = 1;
        String[] algos = {
            "1 – Bubble Sort",
            "2 – Selection Sort",
            "3 – Insertion Sort",
            "4 – Merge Sort",
            "5 – Quick Sort"
        };
        JComboBox<String> cbAlgo = new JComboBox<>(algos);
        cbAlgo.setFont(FONT_NORMAL);
        form.add(cbAlgo, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);

        JButton btnBuild  = makeBtn("Build module", ACCENT);
        JButton btnLoad   = makeBtn("Nạp module (insmod)", SUCCESS);
        JButton btnLog    = makeBtn("Xem kết quả (dmesg)", new Color(107, 114, 128));
        JButton btnUnload = makeBtn("Gỡ module (rmmod)", DANGER);
        JButton btnClean  = makeBtn("make clean", new Color(156, 163, 175));

        btnBuild.addActionListener(e ->
            runCommandAndShow("cd ~/NhanLinux/c/module && make build 2>&1"));

        btnLoad.addActionListener(e -> {
            int select = cbAlgo.getSelectedIndex() + 1;
            String values = tfValues.getText().trim();
            if (values.isEmpty()) {
                appendOutput("❌ Vui lòng nhập mảng cần sắp xếp!\n");
                return;
            }
            runCommandAndShow(
                "cd ~/NhanLinux/c/module && sudo insmod sort.ko select=" + select +
                " values=" + values + " 2>&1");
        });

        btnLog.addActionListener(e ->
            runCommandAndShow("sudo dmesg | tail -20"));

        btnUnload.addActionListener(e ->
            runCommandAndShow("sudo rmmod sort 2>&1 && echo '✅ Đã gỡ module sort'"));

        btnClean.addActionListener(e ->
            runCommandAndShow("cd ~/NhanLinux/c/module && make clean 2>&1"));

        btnRow.add(btnBuild);
        btnRow.add(btnLoad);
        btnRow.add(btnLog);
        btnRow.add(btnUnload);
        btnRow.add(btnClean);
        form.add(btnRow, gc);

        panel.add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel("💡 Thứ tự: Build → Nạp module → Xem kết quả → Gỡ module");
        hint.setFont(FONT_NORMAL);
        hint.setForeground(TEXT_GRAY);
        hint.setBorder(new EmptyBorder(8, 0, 0, 0));
        panel.add(hint, BorderLayout.SOUTH);

        return panel;
    }

    // ════════════════════════════════════════════════
    //  HELPER
    // ════════════════════════════════════════════════
    static JPanel makeCard(String title, String desc, Color color, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(FONT_LABEL);
        lbTitle.setForeground(TEXT);

        JLabel lbDesc = new JLabel("<html>" + desc.replace("\n", "<br>") + "</html>");
        lbDesc.setFont(FONT_NORMAL);
        lbDesc.setForeground(TEXT_GRAY);

        JButton btn = makeBtn("Chạy", color);
        btn.addActionListener(e -> action.run());

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(lbTitle, BorderLayout.NORTH);
        top.add(lbDesc, BorderLayout.CENTER);

        card.add(top, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    static JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_LABEL);
        btn.setBackground(bg);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    static void runInTerminal(String cmd) {
        appendOutput("▶ Mở terminal: " + cmd + "\n");
        runShell("x-terminal-emulator -e bash -c '" + cmd + "; echo; read -p \"Nhấn Enter để đóng...\" ' &");
    }

    static void runSudoInTerminal(String cmd) {
        appendOutput("▶ Mở terminal (sudo): " + cmd + "\n");
        runShell("x-terminal-emulator -e bash -c 'sudo " + cmd + "; echo; read -p \"Nhấn Enter để đóng...\" ' &");
    }

    static void runCommandAndShow(String cmd) {
        appendOutput("▶ " + cmd + "\n");
        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd});
                BufferedReader br   = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader berr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while ((line = br.readLine())   != null) appendOutput(line + "\n");
                while ((line = berr.readLine()) != null) appendOutput("[err] " + line + "\n");
                p.waitFor();
                appendOutput("─────────────────────────────────\n");
            } catch (Exception ex) {
                appendOutput("❌ Lỗi: " + ex.getMessage() + "\n");
            }
        }).start();
    }

    static void runCommand(String cmd) {
        appendOutput("▶ " + cmd + "\n");
        runShell(cmd + " 2>&1");
    }

    static void runShell(String cmd) {
        try {
            Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd});
        } catch (Exception ex) {
            appendOutput("❌ " + ex.getMessage() + "\n");
        }
    }

    static void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            if (outputArea != null) {
                outputArea.append(text);
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            }
        });
    }
}
