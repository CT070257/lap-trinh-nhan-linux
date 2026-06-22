import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.Map;

public class NhanLinuxApp {

    // ── Màu sắc & font ──────────────────────────────────────────
    static final Color BG          = new Color(245, 245, 245);
    static final Color WHITE       = Color.WHITE;
    static final Color ACCENT      = new Color(37, 99, 235);
    static final Color C_COLOR     = new Color(79, 70, 229);
    static final Color MOD_COLOR   = new Color(217, 119, 6);
    static final Color SUCCESS     = new Color(22, 163, 74);
    static final Color DANGER      = new Color(220, 38, 38);
    static final Color TEXT        = new Color(30, 30, 30);
    static final Color TEXT_GRAY   = new Color(120, 120, 120);
    static final Color BORDER      = new Color(220, 220, 220);
    static final Color ROW_ALT     = new Color(248, 249, 252);
    static final Color TERM_BG     = new Color(22, 22, 30);
    static final Color TERM_FG     = new Color(220, 220, 220);

    static Font FONT_TITLE  = new Font("SansSerif", Font.BOLD, 22);
    static Font FONT_LABEL  = new Font("SansSerif", Font.BOLD, 13);
    static Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    static Font FONT_MONO   = new Font("Monospaced", Font.PLAIN, 12);

    // ── Card layout ──────────────────────────────────────────────
    static CardLayout cardLayout;
    static JPanel     cardPanel;

    static final String CARD_HOME           = "home";
    static final String CARD_SHELL          = "shell";
    static final String CARD_C              = "c";
    static final String CARD_MODULE         = "module";
    static final String CARD_SHELL_FILE     = "shell_file";
    static final String CARD_SHELL_CRON     = "shell_cron";
    static final String CARD_SHELL_DATE     = "shell_date";
    static final String CARD_SHELL_PROG     = "shell_prog";
    static final String CARD_C_FILE         = "c_file";
    static final String CARD_C_NET          = "c_net";
    static final String CARD_C_PROC         = "c_proc";
    static final String CARD_C_SOCKET       = "c_socket";

    // ── Socket chat state ────────────────────────────────────────
    // Mỗi entry: socket kết nối đến server (phía client)
    // Server quản lý riêng qua ChatServer inner-class
    static JTextArea      serverLog;
    static JTextArea[]    clientChats  = new JTextArea[2];
    static JTextField[]   clientInputs = new JTextField[2];
    static ChatServer     activeChatServer = null;
    static ChatClient[]   activeChatClients = new ChatClient[2];

    // ── Inner class: Chat Server ─────────────────────────────────
    static class ChatServer {
        final int port;
        ServerSocket ss;
        // Map socket → writer, được bảo vệ bằng synchronized(peers)
        final Map<Socket, PrintWriter> peers = new LinkedHashMap<>();
        volatile boolean running = false;

        ChatServer(int port) { this.port = port; }

        void start(JTextArea log, JButton bStart, JButton bStop) throws IOException {
            ss = new ServerSocket(port);
            running = true;
            SwingUtilities.invokeLater(() -> {
                log.append("✅ Server khởi động, lắng nghe port " + port + "...\n");
                bStart.setEnabled(false); bStop.setEnabled(true);
            });
            new Thread(() -> {
                while (running) {
                    try {
                        Socket client = ss.accept();
                        PrintWriter pw = new PrintWriter(new BufferedOutputStream(client.getOutputStream()), true);
                        synchronized (peers) { peers.put(client, pw); }
                        String ip = client.getInetAddress().getHostAddress();
                        SwingUtilities.invokeLater(() -> log.append("🔗 Client kết nối từ " + ip + "\n"));
                        // Thread đọc cho từng client
                        new Thread(() -> readLoop(client, log)).start();
                    } catch (Exception e) { break; }
                }
            }, "server-accept").start();
        }

        void readLoop(Socket src, JTextArea log) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(src.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    final String msg = line;
                    SwingUtilities.invokeLater(() -> log.append("[relay] " + msg + "\n"));
                    // Relay tới mọi client KHÁC (dùng writer đã lưu sẵn)
                    synchronized (peers) {
                        for (Map.Entry<Socket, PrintWriter> e : peers.entrySet()) {
                            if (!e.getKey().equals(src)) {
                                e.getValue().println(msg);
                                e.getValue().flush();
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            // Client ngắt kết nối
            synchronized (peers) { peers.remove(src); }
            try { src.close(); } catch (Exception ignored) {}
            SwingUtilities.invokeLater(() -> log.append("⚠ Một client đã ngắt kết nối.\n"));
        }

        void stop(JTextArea log, JButton bStart, JButton bStop) {
            running = false;
            synchronized (peers) {
                for (Socket s : peers.keySet()) try { s.close(); } catch (Exception ignored) {}
                peers.clear();
            }
            try { if (ss != null) ss.close(); } catch (Exception ignored) {}
            SwingUtilities.invokeLater(() -> {
                log.append("⏹ Server đã dừng.\n");
                bStart.setEnabled(true); bStop.setEnabled(false);
            });
        }
    }

    // ── Inner class: Chat Client ─────────────────────────────────
    static class ChatClient {
        Socket sock;
        PrintWriter pw;
        volatile boolean connected = false;

        void connect(int port, String name, int idx,
                     JButton bConn, JButton bDisc, JButton bSend) throws IOException {
            sock = new Socket("127.0.0.1", port);
            pw   = new PrintWriter(new BufferedOutputStream(sock.getOutputStream()), true);
            connected = true;
            SwingUtilities.invokeLater(() -> {
                clientChats[idx].append("✅ Kết nối thành công! Xin chào " + name + "\n");
                bConn.setEnabled(false); bDisc.setEnabled(true);
                bSend.setEnabled(true); clientInputs[idx].setEnabled(true);
            });
            // Thread đọc nhận tin từ server (relay từ client kia)
            new Thread(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        final String msg = line;
                        SwingUtilities.invokeLater(() -> {
                            clientChats[idx].append("📨 " + msg + "\n");
                            clientChats[idx].setCaretPosition(clientChats[idx].getDocument().getLength());
                        });
                    }
                } catch (Exception ignored) {}
                SwingUtilities.invokeLater(() -> {
                    if (connected) clientChats[idx].append("⚠ Mất kết nối với server.\n");
                });
            }, "client-read-" + idx).start();
        }

        void send(String msg) {
            if (pw != null && connected) { pw.println(msg); pw.flush(); }
        }

        void disconnect(int idx, JButton bConn, JButton bDisc, JButton bSend) {
            connected = false;
            try { if (sock != null) sock.close(); } catch (Exception ignored) {}
            SwingUtilities.invokeLater(() -> {
                clientChats[idx].append("⚠ Đã ngắt kết nối.\n");
                bConn.setEnabled(true); bDisc.setEnabled(false);
                bSend.setEnabled(false); clientInputs[idx].setEnabled(false);
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Lập trình nhân Linux – Đề tài 23");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(960, 700);
            frame.setMinimumSize(new Dimension(800, 560));
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(ACCENT);
            header.setBorder(new EmptyBorder(14, 24, 14, 24));
            JLabel t = new JLabel("Lập trình nhân Linux");
            t.setFont(FONT_TITLE); t.setForeground(WHITE);
            JLabel s = new JLabel("Đề tài 23  ·  Shell  ·  C Program  ·  Kernel Module");
            s.setFont(FONT_NORMAL); s.setForeground(new Color(200, 220, 255));
            JPanel ht = new JPanel(); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
            ht.setOpaque(false); ht.add(t); ht.add(Box.createVerticalStrut(3)); ht.add(s);
            header.add(ht, BorderLayout.CENTER);
            frame.add(header, BorderLayout.NORTH);

            // Cards
            cardLayout = new CardLayout();
            cardPanel  = new JPanel(cardLayout);
            cardPanel.setBackground(BG);
            cardPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

            cardPanel.add(buildHome(),   CARD_HOME);
            cardPanel.add(buildShellMenu(), CARD_SHELL);
            cardPanel.add(buildCMenu(),     CARD_C);
            cardPanel.add(wrapBack("🔧  Kernel Module", CARD_HOME, buildModulePanel()), CARD_MODULE);

            cardPanel.add(wrapBack("🖥  Shell  ›  📁 Quản lý File",        CARD_SHELL, buildShellFile()),    CARD_SHELL_FILE);
            cardPanel.add(wrapBack("🖥  Shell  ›  ⏰ Crontab",              CARD_SHELL, buildShellCron()),    CARD_SHELL_CRON);
            cardPanel.add(wrapBack("🖥  Shell  ›  🕐 Thời gian hệ thống",  CARD_SHELL, buildShellDate()),    CARD_SHELL_DATE);
            cardPanel.add(wrapBack("🖥  Shell  ›  📦 Quản lý chương trình",CARD_SHELL, buildShellProg()),    CARD_SHELL_PROG);

            cardPanel.add(wrapBack("⚙  C  ›  📄 Quản lý File",            CARD_C, buildCFile()),   CARD_C_FILE);
            cardPanel.add(wrapBack("⚙  C  ›  🌐 Network Interface",        CARD_C, buildCNet()),    CARD_C_NET);
            cardPanel.add(wrapBack("⚙  C  ›  🔄 Quản lý tiến trình",      CARD_C, buildCProc()),   CARD_C_PROC);
            cardPanel.add(wrapBack("⚙  C  ›  💬 Socket Chat",              CARD_C, buildCSocket()), CARD_C_SOCKET);

            frame.add(cardPanel, BorderLayout.CENTER);
            frame.setVisible(true);
            cardLayout.show(cardPanel, CARD_HOME);
        });
    }

    // ════════════════════════════════════════════════════════════
    //  TRANG CHỦ
    // ════════════════════════════════════════════════════════════
    static JPanel buildHome() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG); p.setBorder(new EmptyBorder(16,16,16,16));
        JLabel d = new JLabel("Chọn một mô-đun bên dưới để mở giao diện");
        d.setFont(FONT_NORMAL); d.setForeground(TEXT_GRAY);
        p.add(d, BorderLayout.NORTH);
        JPanel list = new JPanel(new GridLayout(3,1,12,12)); list.setOpaque(false);
        list.add(navCard("🖥  Shell Script",    "Quản lý file, crontab, thời gian, phần mềm",  ACCENT,    () -> go(CARD_SHELL)));
        list.add(navCard("⚙  C Program",       "Quản lý file, network, tiến trình, socket",   C_COLOR,   () -> go(CARD_C)));
        list.add(navCard("🔧  Kernel Module",   "Build và nạp module nhân sắp xếp mảng",       MOD_COLOR, () -> go(CARD_MODULE)));
        p.add(list, BorderLayout.CENTER);
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  MENU SHELL  (4 bài con)
    // ════════════════════════════════════════════════════════════
    static JPanel buildShellMenu() {
        return menuScreen(CARD_HOME, "🖥  Shell Script",
            "Chọn bài Shell bên dưới", ACCENT,
            new String[]{"📁  Quản lý File",         "Tạo, đọc, sửa, đổi tên, sao chép, nén, xóa file"},
            new String[]{"⏰  Lập lịch Crontab",      "Tạo, xóa, liệt kê các cron job"},
            new String[]{"🕐  Thời gian hệ thống",   "Xem, thiết lập thời gian thủ công hoặc tự động"},
            new String[]{"📦  Quản lý chương trình",  "Cài đặt, gỡ bỏ, liệt kê gói apt"},
            new Runnable[]{() -> go(CARD_SHELL_FILE), () -> go(CARD_SHELL_CRON),
                           () -> go(CARD_SHELL_DATE), () -> go(CARD_SHELL_PROG)}
        );
    }

    // ════════════════════════════════════════════════════════════
    //  MENU C  (4 bài con)
    // ════════════════════════════════════════════════════════════
    static JPanel buildCMenu() {
        return menuScreen(CARD_HOME, "⚙  C Program",
            "Chọn bài C bên dưới", C_COLOR,
            new String[]{"📄  Quản lý File (C)",     "Tạo, đọc, xóa file bằng stdio.h"},
            new String[]{"🌐  Network Interface",     "Liệt kê interface mạng và địa chỉ IP"},
            new String[]{"🔄  Quản lý tiến trình",   "Xem top và kill tiến trình theo PID"},
            new String[]{"💬  Socket Chat",           "TCP chat: server + 2 client tích hợp"},
            new Runnable[]{() -> go(CARD_C_FILE), () -> go(CARD_C_NET),
                           () -> go(CARD_C_PROC), () -> go(CARD_C_SOCKET)}
        );
    }

    // Tạo màn hình menu 4 nút điều hướng (dùng cho Shell và C)
    static JPanel menuScreen(String backCard, String title, String subtitle, Color color,
                             String[] t1, String[] t2, String[] t3, String[] t4,
                             Runnable[] actions) {
        JPanel p = new JPanel(new BorderLayout(0,0)); p.setOpaque(false);
        JPanel bar = backBar("←  Trang chủ", backCard);
        p.add(bar, BorderLayout.NORTH);

        JPanel inner = new JPanel(new BorderLayout(0,12));
        inner.setBackground(BG); inner.setBorder(new EmptyBorder(12,16,16,16));
        JLabel d = new JLabel(subtitle); d.setFont(FONT_NORMAL); d.setForeground(TEXT_GRAY);
        inner.add(d, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2,2,12,12)); grid.setOpaque(false);
        String[][] items = {t1,t2,t3,t4};
        for (int i=0;i<4;i++) {
            final int idx=i;
            grid.add(navCard(items[i][0], items[i][1], color, actions[idx]));
        }
        inner.add(grid, BorderLayout.CENTER);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  SHELL – Quản lý File
    // ════════════════════════════════════════════════════════════
    static JPanel buildShellFile() {
        JTextArea out = terminal();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE); form.setBorder(pad(12));
        GridBagConstraints g = gbc();

        JTextField tfName    = field(""); JTextField tfNew = field("");
        JTextField tfSrc     = field(""); JTextField tfDst = field("");
        JTextField tfArchive = field(""); JTextArea  taContent = new JTextArea(4,20);
        taContent.setFont(FONT_MONO); taContent.setBorder(new LineBorder(BORDER));

        // Hàng nút chức năng
        JButton[] btns = {
            btn("📄 Tạo file",     ACCENT),
            btn("👁 Đọc file",     ACCENT),
            btn("✏ Sửa file",      ACCENT),
            btn("🔄 Đổi tên",      ACCENT),
            btn("📋 Sao chép",     ACCENT),
            btn("🗜 Nén .tar.gz",  ACCENT),
            btn("🗑 Xóa file",     DANGER),
        };

        // Layout form
        row(form,g,0, lbl("Tên file / file nguồn:"), tfName);
        row(form,g,1, lbl("Tên file đích / mới:"),   tfNew);
        row(form,g,2, lbl("File nén (.tar.gz):"),     tfArchive);
        row(form,g,3, lbl("Nội dung (tạo/sửa):"),    new JScrollPane(taContent));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        btnRow.setOpaque(false);
        for (JButton b : btns) btnRow.add(b);
        addFull(form,g,4,btnRow);

        // Actions
        btns[0].addActionListener(e -> {
            String n=tfName.getText().trim(), c=taContent.getText();
            if(n.isEmpty()){out.append("❌ Nhập tên file\n");return;}
            runCmd("bash -c \"printf '%s' "+shellEsc(c)+" > "+shellEsc(n)+"\"", out);
        });
        btns[1].addActionListener(e -> {
            String n=tfName.getText().trim();
            if(n.isEmpty()){out.append("❌ Nhập tên file\n");return;}
            runCmd("cat "+shellEsc(n), out);
        });
        btns[2].addActionListener(e -> {
            String n=tfName.getText().trim(), c=taContent.getText();
            if(n.isEmpty()){out.append("❌ Nhập tên file\n");return;}
            runCmd("bash -c \"printf '%s' "+shellEsc(c)+" > "+shellEsc(n)+"\"", out);
            out.append("✅ Đã ghi đè nội dung vào "+n+"\n");
        });
        btns[3].addActionListener(e -> {
            String o=tfName.getText().trim(), n=tfNew.getText().trim();
            if(o.isEmpty()||n.isEmpty()){out.append("❌ Nhập cả tên cũ và tên mới\n");return;}
            runCmd("mv "+shellEsc(o)+" "+shellEsc(n), out);
            out.append("✅ Đổi tên "+o+" → "+n+"\n");
        });
        btns[4].addActionListener(e -> {
            String s=tfName.getText().trim(), d2=tfNew.getText().trim();
            if(s.isEmpty()||d2.isEmpty()){out.append("❌ Nhập file nguồn và file đích\n");return;}
            runCmd("cp "+shellEsc(s)+" "+shellEsc(d2), out);
            out.append("✅ Sao chép "+s+" → "+d2+"\n");
        });
        btns[5].addActionListener(e -> {
            String f=tfName.getText().trim(), a=tfArchive.getText().trim();
            if(f.isEmpty()||a.isEmpty()){out.append("❌ Nhập tên file và tên file nén\n");return;}
            runCmd("tar -czvf "+shellEsc(a)+" "+shellEsc(f), out);
        });
        btns[6].addActionListener(e -> {
            String n=tfName.getText().trim();
            if(n.isEmpty()){out.append("❌ Nhập tên file\n");return;}
            int ok=JOptionPane.showConfirmDialog(null,"Xóa file '"+n+"'?","Xác nhận",JOptionPane.YES_NO_OPTION);
            if(ok==0) runCmd("rm -f "+shellEsc(n), out);
        });

        return splitPanel(form, out);
    }

    // ════════════════════════════════════════════════════════════
    //  SHELL – Crontab
    // ════════════════════════════════════════════════════════════
    static JPanel buildShellCron() {
        JTextArea out = terminal();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE); form.setBorder(pad(12));
        GridBagConstraints g = gbc();

        JTextField tfPath = field(""); JTextField tfExpr = field("* * * * *");
        row(form,g,0, lbl("Đường dẫn file shell:"), tfPath);
        row(form,g,1, lbl("Cron expression:"),       tfExpr);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        btnRow.setOpaque(false);
        JButton bAdd  = btn("➕ Tạo tác vụ",    ACCENT);
        JButton bList = btn("📋 Xem danh sách", new Color(107,114,128));
        JButton bDel  = btn("🗑 Xóa tác vụ",    DANGER);
        btnRow.add(bAdd); btnRow.add(bList); btnRow.add(bDel);
        addFull(form,g,2,btnRow);

        bAdd.addActionListener(e -> {
            String path=tfPath.getText().trim(), expr=tfExpr.getText().trim();
            if(path.isEmpty()||expr.isEmpty()){out.append("❌ Nhập đầy đủ đường dẫn và expression\n");return;}
            runCmd("bash -c \"chmod +x "+shellEsc(path)+" && (crontab -l 2>/dev/null; echo '"+expr+" "+path+"') | crontab -\"", out);
            out.append("✅ Đã thêm cron job: "+expr+" "+path+"\n");
        });
        bList.addActionListener(e -> runCmd("crontab -l", out));
        bDel.addActionListener(e -> {
            String path=tfPath.getText().trim();
            if(path.isEmpty()){out.append("❌ Nhập đường dẫn file shell cần xóa\n");return;}
            runCmd("bash -c \"crontab -l | grep -v "+shellEsc(path)+" | crontab -\"", out);
            out.append("✅ Đã xóa cron job chứa: "+path+"\n");
        });

        return splitPanel(form, out);
    }

    // ════════════════════════════════════════════════════════════
    //  SHELL – Thời gian hệ thống
    // ════════════════════════════════════════════════════════════
    static JPanel buildShellDate() {
        JTextArea out = terminal();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE); form.setBorder(pad(12));
        GridBagConstraints g = gbc();

        JTextField tfDate = field("2024-03-16 09:18:01");
        row(form,g,0, lbl("Thời gian mới (YYYY-MM-DD HH:mm:ss):"), tfDate);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        btnRow.setOpaque(false);
        JButton bShow  = btn("🕐 Xem giờ hiện tại",    ACCENT);
        JButton bManual= btn("✏ Thiết lập thủ công",   new Color(107,114,128));
        JButton bAuto  = btn("🌐 Thiết lập tự động",   SUCCESS);
        btnRow.add(bShow); btnRow.add(bManual); btnRow.add(bAuto);
        addFull(form,g,1,btnRow);

        bShow.addActionListener(e   -> runCmd("date '+%Y-%m-%d %H:%M:%S'", out));
        bManual.addActionListener(e -> {
            String dt=tfDate.getText().trim();
            if(dt.isEmpty()){out.append("❌ Nhập thời gian\n");return;}
            runCmd("bash -c \"sudo timedatectl set-ntp false && sudo timedatectl set-time '"+dt+"' && date\"", out);
        });
        bAuto.addActionListener(e   -> runCmd(
		"bash -c \"sudo timedatectl set-ntp true && sleep 2 && date\"", out));

        return splitPanel(form, out);
    }

    // ════════════════════════════════════════════════════════════
    //  SHELL – Quản lý chương trình
    // ════════════════════════════════════════════════════════════
    static JPanel buildShellProg() {
        JTextArea out = terminal();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE); form.setBorder(pad(12));
        GridBagConstraints g = gbc();

        JTextField tfPkg = field("");
        row(form,g,0, lbl("Tên gói (apt):"), tfPkg);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        btnRow.setOpaque(false);
        JButton bInst  = btn("⬇ Cài đặt",           SUCCESS);
        JButton bRemove= btn("🗑 Gỡ bỏ",             DANGER);
        JButton bList  = btn("📋 Đã cài",            new Color(107,114,128));
        JButton bAvail = btn("🔍 Tìm kiếm",          ACCENT);
        btnRow.add(bInst); btnRow.add(bRemove); btnRow.add(bList); btnRow.add(bAvail);
        addFull(form,g,1,btnRow);

        bInst.addActionListener(e -> {
            String p=tfPkg.getText().trim();
            if(p.isEmpty()){out.append("❌ Nhập tên gói\n");return;}
            runCmd("sudo apt-get install -y "+p, out);
        });
        bRemove.addActionListener(e -> {
            String p=tfPkg.getText().trim();
            if(p.isEmpty()){out.append("❌ Nhập tên gói\n");return;}
            runCmd("sudo apt-get remove -y "+p, out);
        });
        bList.addActionListener(e  -> runCmd("dpkg --get-selections | grep -v deinstall | head -50", out));
        bAvail.addActionListener(e -> {
            String p=tfPkg.getText().trim();
            if(p.isEmpty()){out.append("❌ Nhập tên gói cần tìm\n");return;}
            runCmd("apt-cache search "+p, out);
        });

        return splitPanel(form, out);
    }

    // ════════════════════════════════════════════════════════════
    //  C – Quản lý File
    // ════════════════════════════════════════════════════════════
    static JPanel buildCFile() {
        JTextArea out = terminal();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE); form.setBorder(pad(12));
        GridBagConstraints g = gbc();

        JTextField tfName = field(""); JTextArea taContent = new JTextArea(4,20);
        taContent.setFont(FONT_MONO); taContent.setBorder(new LineBorder(BORDER));
        row(form,g,0, lbl("Tên file:"),            tfName);
        row(form,g,1, lbl("Nội dung (tạo file):"), new JScrollPane(taContent));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        btnRow.setOpaque(false);
        JButton bCreate = btn("📄 Tạo file",   C_COLOR);
        JButton bRead   = btn("👁 Đọc file",   C_COLOR);
        JButton bDelete = btn("🗑 Xóa file",   DANGER);
        btnRow.add(bCreate); btnRow.add(bRead); btnRow.add(bDelete);
        addFull(form,g,2,btnRow);

        // Dùng Java trực tiếp (không gọi gcc) – tái hiện đúng logic file.c
        bCreate.addActionListener(e -> {
            String n=tfName.getText().trim(), c=taContent.getText();
            if(n.isEmpty()){out.append("❌ Nhập tên file\n");return;}
            try(PrintWriter pw=new PrintWriter(new FileWriter(n))){
                pw.print(c);
                out.append("✅ Tạo và ghi nội dung vào file '"+n+"' thành công.\n");
            } catch(Exception ex){out.append("❌ Lỗi: "+ex.getMessage()+"\n");}
        });
        bRead.addActionListener(e -> {
            String n=tfName.getText().trim();
            if(n.isEmpty()){out.append("❌ Nhập tên file\n");return;}
            File f=new File(n);
            if(!f.exists()){out.append("❌ File '"+n+"' không tồn tại\n");return;}
            try(BufferedReader br=new BufferedReader(new FileReader(f))){
                out.append("--------------------------------------------------\n");
                String line; while((line=br.readLine())!=null) out.append(line+"\n");
                out.append("--------------------------------------------------\n");
            } catch(Exception ex){out.append("❌ Lỗi: "+ex.getMessage()+"\n");}
        });
        bDelete.addActionListener(e -> {
            String n=tfName.getText().trim();
            if(n.isEmpty()){out.append("❌ Nhập tên file\n");return;}
            File f=new File(n);
            if(!f.exists()){out.append("❌ File '"+n+"' không tồn tại\n");return;}
            int ok=JOptionPane.showConfirmDialog(null,"Xóa file '"+n+"'?","Xác nhận",JOptionPane.YES_NO_OPTION);
            if(ok==0){ f.delete(); out.append("✅ Xóa file '"+n+"' thành công\n"); }
        });

        return splitPanel(form, out);
    }

    // ════════════════════════════════════════════════════════════
    //  C – Network Interface
    // ════════════════════════════════════════════════════════════
    static JPanel buildCNet() {
        JTextArea out = terminal();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE); form.setBorder(pad(12));
        GridBagConstraints g = gbc();

        JLabel info = new JLabel("<html>Liệt kê tất cả interface mạng và địa chỉ IP<br>"
            + "bằng cách gọi <b>ip addr show</b> (tái hiện logic getifaddrs)</html>");
        info.setFont(FONT_NORMAL); info.setForeground(TEXT_GRAY);
        addFull(form,g,0,info);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        btnRow.setOpaque(false);
        JButton bRun  = btn("▶ Liệt kê interface", C_COLOR);
        JButton bPing = btn("📶 Ping gateway",      new Color(107,114,128));
        JTextField tfPingHost = field("8.8.8.8");
        btnRow.add(bRun); btnRow.add(bPing); btnRow.add(lbl("Host:")); btnRow.add(tfPingHost);
        addFull(form,g,1,btnRow);

        bRun.addActionListener(e  -> runCmd("ip -o addr show | awk '{print $2, $3, $4}'", out));
        bPing.addActionListener(e -> runCmd("ping -c 4 "+tfPingHost.getText().trim(), out));

        return splitPanel(form, out);
    }

    // ════════════════════════════════════════════════════════════
    //  C – Quản lý tiến trình
    // ════════════════════════════════════════════════════════════
    static JPanel buildCProc() {
        JTextArea out = terminal();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE); form.setBorder(pad(12));
        GridBagConstraints g = gbc();

        JTextField tfPid    = field("");
        JTextField tfFilter = field("");
        row(form,g,0, lbl("PID cần kill:"),    tfPid);
        row(form,g,1, lbl("Lọc theo tên:"),    tfFilter);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        btnRow.setOpaque(false);
        JButton bList   = btn("📋 Danh sách tiến trình", C_COLOR);
        JButton bFilter = btn("🔍 Lọc tiến trình",       new Color(107,114,128));
        JButton bKill   = btn("💀 Kill PID",              DANGER);
        btnRow.add(bList); btnRow.add(bFilter); btnRow.add(bKill);
        addFull(form,g,2,btnRow);

        bList.addActionListener(e -> runCmd("ps aux --sort=-%cpu | head -25", out));
        bFilter.addActionListener(e -> {
            String f=tfFilter.getText().trim();
            if(f.isEmpty()){out.append("❌ Nhập tên tiến trình cần lọc\n");return;}
            runCmd("ps aux | grep "+f+" | grep -v grep", out);
        });
        bKill.addActionListener(e -> {
            String pid=tfPid.getText().trim();
            if(pid.isEmpty()){out.append("❌ Nhập PID\n");return;}
            int ok=JOptionPane.showConfirmDialog(null,"Kill tiến trình PID "+pid+"?","Xác nhận",JOptionPane.YES_NO_OPTION);
            if(ok==0) runCmd("kill -9 "+pid, out);
        });

        return splitPanel(form, out);
    }

    // ════════════════════════════════════════════════════════════
    //  C – Socket Chat  (server + 2 client trong 1 giao diện)
    // ════════════════════════════════════════════════════════════
    static JPanel buildCSocket() {
        // ── Server panel ─────────────────────────────────────────
        serverLog = terminal(); serverLog.setRows(5);
        JPanel serverCtrl = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        serverCtrl.setBackground(new Color(235,235,235));
        serverCtrl.setBorder(new EmptyBorder(6,12,6,12));
        JTextField tfPort = field("9999"); tfPort.setColumns(6);
        JButton bStart = btn("▶ Khởi động Server", SUCCESS);
        JButton bStop  = btn("⏹ Dừng Server",      DANGER);
        bStop.setEnabled(false);
        serverCtrl.add(lbl("Port:")); serverCtrl.add(tfPort);
        serverCtrl.add(bStart); serverCtrl.add(bStop);

        JPanel serverPanel = new JPanel(new BorderLayout());
        serverPanel.setBackground(WHITE);
        serverPanel.setBorder(titledBorder("🖥  Server", SUCCESS));
        serverPanel.add(serverCtrl, BorderLayout.NORTH);
        serverPanel.add(new JScrollPane(serverLog), BorderLayout.CENTER);

        // ── Hai client panel ──────────────────────────────────────
        JPanel[] clientPanels = new JPanel[2];
        for (int i = 0; i < 2; i++) {
            final int idx = i;
            clientChats[i]  = terminal();
            clientInputs[i] = new JTextField();
            clientInputs[i].setFont(FONT_MONO);
            clientInputs[i].setEnabled(false);

            JTextField tfUser = field("Client" + (i+1));
            JButton bConn = btn("🔌 Kết nối", C_COLOR);
            JButton bDisc = btn("✖ Ngắt",     DANGER);
            JButton bSend = btn("➤ Gửi",      C_COLOR);
            bDisc.setEnabled(false);
            bSend.setEnabled(false);

            JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
            ctrl.setBackground(new Color(235,235,235));
            ctrl.setBorder(new EmptyBorder(6,12,6,12));
            ctrl.add(lbl("Tên:")); ctrl.add(tfUser); ctrl.add(bConn); ctrl.add(bDisc);

            JPanel inputRow = new JPanel(new BorderLayout(6,0));
            inputRow.setBackground(WHITE); inputRow.setBorder(new EmptyBorder(6,8,6,8));
            inputRow.add(clientInputs[i], BorderLayout.CENTER);
            inputRow.add(bSend, BorderLayout.EAST);

            JPanel cp = new JPanel(new BorderLayout());
            cp.setBackground(WHITE);
            cp.setBorder(titledBorder("💬 Client " + (i+1), C_COLOR));
            cp.add(ctrl, BorderLayout.NORTH);
            cp.add(new JScrollPane(clientChats[i]), BorderLayout.CENTER);
            cp.add(inputRow, BorderLayout.SOUTH);
            clientPanels[i] = cp;

            // Kết nối
            bConn.addActionListener(e -> {
                if (activeChatServer == null || !activeChatServer.running) {
                    serverLog.append("❌ Server chưa chạy! Hãy khởi động server trước.\n"); return;
                }
                int port;
                try { port = Integer.parseInt(tfPort.getText().trim()); }
                catch (Exception ex) { clientChats[idx].append("❌ Port không hợp lệ\n"); return; }
                String uname = tfUser.getText().trim();
                ChatClient cc = new ChatClient();
                activeChatClients[idx] = cc;
                new Thread(() -> {
                    try {
                        cc.connect(port, uname, idx, bConn, bDisc, bSend);
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() ->
                            clientChats[idx].append("❌ Lỗi kết nối: " + ex.getMessage() + "\n"));
                    }
                }, "client-connect-" + idx).start();
            });

            // Ngắt kết nối
            bDisc.addActionListener(e -> {
                if (activeChatClients[idx] != null) {
                    activeChatClients[idx].disconnect(idx, bConn, bDisc, bSend);
                    activeChatClients[idx] = null;
                }
            });

            // Gửi tin nhắn
            ActionListener sendAction = e2 -> {
                String msg = clientInputs[idx].getText().trim();
                if (msg.isEmpty()) return;
                String name = tfUser.getText().trim();
                String full = name + ": " + msg;
                if (activeChatClients[idx] != null && activeChatClients[idx].connected) {
                    activeChatClients[idx].send(full);
                    clientChats[idx].append("📤 " + full + "\n");
                    clientChats[idx].setCaretPosition(clientChats[idx].getDocument().getLength());
                }
                clientInputs[idx].setText("");
            };
            bSend.addActionListener(sendAction);
            clientInputs[idx].addActionListener(sendAction);
        }

        // Khởi động server
        bStart.addActionListener(e -> {
            if (activeChatServer != null && activeChatServer.running) {
                serverLog.append("⚠ Server đã đang chạy\n"); return;
            }
            int port;
            try { port = Integer.parseInt(tfPort.getText().trim()); }
            catch (Exception ex) { serverLog.append("❌ Port không hợp lệ\n"); return; }
            ChatServer cs = new ChatServer(port);
            activeChatServer = cs;
            new Thread(() -> {
                try { cs.start(serverLog, bStart, bStop); }
                catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> serverLog.append("❌ Lỗi: " + ex.getMessage() + "\n"));
                }
            }, "server-main").start();
        });

        // Dừng server
        bStop.addActionListener(e -> {
            // Ngắt tất cả client trước
            for (int i = 0; i < 2; i++) {
                if (activeChatClients[i] != null) {
                    activeChatClients[i].connected = false;
                    try { if (activeChatClients[i].sock != null) activeChatClients[i].sock.close(); } catch (Exception ignored) {}
                    activeChatClients[i] = null;
                }
            }
            if (activeChatServer != null) {
                activeChatServer.stop(serverLog, bStart, bStop);
                activeChatServer = null;
            }
            // Reset UI client
            for (int i = 0; i < 2; i++) {
                clientChats[i].append("⚠ Server đã dừng.\n");
                clientInputs[i].setEnabled(false);
            }
        });

        // ── Layout tổng ──────────────────────────────────────────
        JPanel clients = new JPanel(new GridLayout(1,2,8,0));
        clients.setOpaque(false);
        clients.add(clientPanels[0]); clients.add(clientPanels[1]);

        JPanel main = new JPanel(new BorderLayout(0,8));
        main.setBackground(BG); main.setBorder(new EmptyBorder(8,0,0,0));
        main.add(serverPanel, BorderLayout.NORTH);
        main.add(clients, BorderLayout.CENTER);

        JLabel hint = new JLabel("💡 Thứ tự: Khởi động Server → Kết nối Client 1 → Kết nối Client 2 → Nhắn tin qua ô bên dưới");
        hint.setFont(FONT_NORMAL); hint.setForeground(TEXT_GRAY);
        hint.setBorder(new EmptyBorder(4,0,0,0));
        main.add(hint, BorderLayout.SOUTH);
        return main;
    }

    // ════════════════════════════════════════════════════════════
    //  KERNEL MODULE
    // ════════════════════════════════════════════════════════════
    static JPanel buildModulePanel() {
        JTextArea out = terminal();
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE); form.setBorder(pad(16));
        GridBagConstraints g = gbc();

        JTextField tfValues = field("5,3,1,4,2");
        String[] algos = {"1 – Bubble Sort","2 – Selection Sort","3 – Insertion Sort","4 – Merge Sort","5 – Quick Sort"};
        JComboBox<String> cbAlgo = new JComboBox<>(algos); cbAlgo.setFont(FONT_NORMAL);

        row(form,g,0, lbl("Mảng cần sắp xếp:"), tfValues);
        row(form,g,1, lbl("Thuật toán:"),         cbAlgo);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        btnRow.setOpaque(false);
        JButton bBuild   = btn("⚙ Build",           ACCENT);
        JButton bLoad    = btn("⬆ insmod",           SUCCESS);
        JButton bDmesg   = btn("📄 dmesg",           new Color(107,114,128));
        JButton bUnload  = btn("⬇ rmmod",            DANGER);
        JButton bClean   = btn("🧹 make clean",      new Color(156,163,175));
        btnRow.add(bBuild); btnRow.add(bLoad); btnRow.add(bDmesg); btnRow.add(bUnload); btnRow.add(bClean);
        addFull(form,g,2,btnRow);

        bBuild.addActionListener(e -> runCmd("cd ~/NhanLinux/c/module && make build 2>&1", out));
        bLoad.addActionListener(e -> {
            String vals=tfValues.getText().trim();
            if(vals.isEmpty()){out.append("❌ Nhập mảng\n");return;}
            int sel=cbAlgo.getSelectedIndex()+1;
            runCmd("cd ~/NhanLinux/c/module && sudo insmod sort.ko select="+sel+" values="+vals, out);
        });
        bDmesg.addActionListener(e   -> runCmd("sudo dmesg | tail -20", out));
        bUnload.addActionListener(e  -> runCmd("sudo rmmod sort 2>&1", out));
        bClean.addActionListener(e   -> runCmd("cd ~/NhanLinux/c/module && make clean 2>&1", out));

        JLabel hint = new JLabel("💡 Thứ tự: Build → insmod → dmesg xem kết quả → rmmod");
        hint.setFont(FONT_NORMAL); hint.setForeground(TEXT_GRAY);
        hint.setBorder(new EmptyBorder(6,0,0,0));

        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setBackground(BG);
        p.add(form, BorderLayout.NORTH);
        p.add(new JScrollPane(out), BorderLayout.CENTER);
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  UTILITY – layout helpers
    // ════════════════════════════════════════════════════════════
    static JPanel splitPanel(JPanel form, JTextArea out) {
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            form, new JScrollPane(out));
        sp.setDividerLocation(180); sp.setDividerSize(4); sp.setBorder(null);
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG); p.add(sp);
        return p;
    }

    static JPanel wrapBack(String title, String backCard, JPanel content) {
        JPanel p = new JPanel(new BorderLayout(0,8)); p.setOpaque(false);
        p.add(backBar("← Quay lại", backCard), BorderLayout.NORTH);
        JPanel inner = new JPanel(new BorderLayout()); inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(0,16,16,16));
        inner.add(content, BorderLayout.CENTER);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    static JPanel backBar(String label, String dest) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(235,235,235));
        bar.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true),new EmptyBorder(8,12,8,12)));
        JButton b = new JButton(label); b.setFont(FONT_LABEL); b.setForeground(ACCENT);
        b.setBorderPainted(false); b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> go(dest));
        bar.add(b, BorderLayout.WEST); return bar;
    }

    static JPanel navCard(String title, String desc, Color color, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(16,0));
        card.setBackground(WHITE); card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true),new EmptyBorder(18,20,18,20)));
        JPanel stripe = new JPanel(); stripe.setBackground(color);
        stripe.setPreferredSize(new Dimension(5,0)); card.add(stripe, BorderLayout.WEST);
        JLabel lt = new JLabel(title); lt.setFont(new Font("SansSerif",Font.BOLD,15));
        lt.setForeground(TEXT); lt.setBorder(new EmptyBorder(0,16,0,0));
        JLabel ld = new JLabel("<html>"+desc+"</html>"); ld.setFont(FONT_NORMAL);
        ld.setForeground(TEXT_GRAY); ld.setBorder(new EmptyBorder(0,16,0,0));
        JPanel tc = new JPanel(); tc.setLayout(new BoxLayout(tc,BoxLayout.Y_AXIS));
        tc.setOpaque(false); tc.add(lt); tc.add(Box.createVerticalStrut(6)); tc.add(ld);
        JButton b = btn("Mở  →", color); b.addActionListener(e->action.run());
        JPanel bw = new JPanel(new GridBagLayout()); bw.setOpaque(false); bw.add(b);
        card.add(tc, BorderLayout.CENTER); card.add(bw, BorderLayout.EAST);
        card.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){action.run();}});
        return card;
    }

    static GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,6,5,6); g.anchor = GridBagConstraints.WEST; return g;
    }
    static void row(JPanel p, GridBagConstraints g, int row, JComponent lbl, JComponent field) {
        g.gridx=0; g.gridy=row; g.weightx=0; g.fill=GridBagConstraints.NONE; p.add(lbl,g);
        g.gridx=1; g.weightx=1; g.fill=GridBagConstraints.HORIZONTAL; p.add(field,g);
        g.fill=GridBagConstraints.NONE; g.weightx=0;
    }
    static void addFull(JPanel p, GridBagConstraints g, int row, JComponent c) {
        g.gridx=0; g.gridy=row; g.gridwidth=2; g.weightx=1;
        g.fill=GridBagConstraints.HORIZONTAL; p.add(c,g);
        g.gridwidth=1; g.fill=GridBagConstraints.NONE; g.weightx=0;
    }
    static JLabel      lbl(String t)   { JLabel l=new JLabel(t); l.setFont(FONT_LABEL); return l; }
    static JTextField  field(String t) { JTextField f=new JTextField(t,18); f.setFont(FONT_MONO); return f; }
    static EmptyBorder pad(int v)      { return new EmptyBorder(v,v,v,v); }
    static JButton btn(String t, Color c) {
        JButton b=new JButton(t); b.setFont(FONT_LABEL); b.setBackground(c); b.setForeground(WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7,14,7,14)); return b;
    }
    static JTextArea terminal() {
        JTextArea ta = new JTextArea(8,0); ta.setFont(FONT_MONO);
        ta.setBackground(TERM_BG); ta.setForeground(TERM_FG); ta.setEditable(false);
        ta.setCaretColor(TERM_FG); ta.setBorder(new EmptyBorder(8,10,8,10)); return ta;
    }
    static TitledBorder titledBorder(String t, Color c) {
        TitledBorder tb = BorderFactory.createTitledBorder(new LineBorder(c,1,true),t);
        tb.setTitleFont(FONT_LABEL); tb.setTitleColor(c); return tb;
    }
    static String shellEsc(String s){ return "'"+s.replace("'","'\\''")+"'"; }

    static void go(String card){ cardLayout.show(cardPanel, card); }

    static void runCmd(String cmd, JTextArea out) {
        out.append("$ "+cmd+"\n");
        new Thread(()->{
            try{
                Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",cmd});
                BufferedReader br  =new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader bre =new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while((line=br.readLine())!=null){final String l=line; SwingUtilities.invokeLater(()->{out.append(l+"\n");scrollDown(out);});}
                while((line=bre.readLine())!=null){final String l=line; SwingUtilities.invokeLater(()->{out.append("[err] "+l+"\n");scrollDown(out);});}
                p.waitFor();
                SwingUtilities.invokeLater(()->{ out.append("─────────────────────────\n"); scrollDown(out); });
            } catch(Exception ex){ SwingUtilities.invokeLater(()->out.append("❌ "+ex.getMessage()+"\n")); }
        }).start();
    }
    static void scrollDown(JTextArea ta){ ta.setCaretPosition(ta.getDocument().getLength()); }
}
