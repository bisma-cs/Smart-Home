import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

abstract class SmartDevice {
    private final String name;
    private boolean on;

    public SmartDevice(String name) {
        this.name = name;
        this.on = false;
    }

    public String getName() { return name; }
    public boolean isOn() { return on; }

    public void switchOn() { on = true; }
    public void switchOff() { on = false; }
    public void toggle() { on = !on; }

    public abstract String getStatus();
    public abstract String getIcon();

    // Optional device-specific settings dialog
    public void openSettings(JFrame parent) {
        JOptionPane.showMessageDialog(parent, "No extra settings for " + getName());
    }
}

class Light extends SmartDevice {
    public Light(String name) { super(name); }
    public String getStatus() { return isOn() ? "ON" : "OFF"; }
    public String getIcon() { return "\uD83D\uDCA1"; } // 💡
}

class Fan extends SmartDevice {
    private int speed = 1; // 1 to 3

    public Fan(String name) { super(name); }

    public int getSpeed() { return speed; }

    public void setSpeed(int speed) throws Exception {
        if(speed < 1 || speed > 3) throw new Exception("Speed must be between 1 and 3");
        this.speed = speed;
    }

    public String getStatus() {
        return isOn() ? "ON at speed " + speed : "OFF";
    }

    public String getIcon() { return "\uD83C\uDF2C"; } // 🌀

    @Override
    public void openSettings(JFrame parent) {
        String input = JOptionPane.showInputDialog(parent, "Set Fan Speed (1-3):", speed);
        try {
            if(input == null) return; // Cancel
            int sp = Integer.parseInt(input.trim());
            setSpeed(sp);
            switchOn();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(parent, "Invalid input: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class Thermostat extends SmartDevice {
    private int temperature = 22; // Celsius

    public Thermostat(String name) { super(name); }

    public int getTemperature() { return temperature; }

    public void setTemperature(int temp) throws Exception {
        if(temp < 10 || temp > 30) throw new Exception("Temperature must be between 10°C and 30°C");
        this.temperature = temp;
    }

    public String getStatus() {
        return isOn() ? "ON at " + temperature + "°C" : "OFF";
    }

    public String getIcon() { return "\uD83C\uDF21"; } // 🌡

    @Override
    public void openSettings(JFrame parent) {
        String input = JOptionPane.showInputDialog(parent, "Set Temperature (10-30°C):", temperature);
        try {
            if(input == null) return;
            int temp = Integer.parseInt(input.trim());
            setTemperature(temp);
            switchOn();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(parent, "Invalid input: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class DoorLock extends SmartDevice {
    public DoorLock(String name) { super(name); }

    public String getStatus() {
        return isOn() ? "Locked" : "Unlocked";
    }

    public String getIcon() { return "\uD83D\uDD12"; } // 🔒

    @Override
    public void toggle() {
        if(isOn()) switchOff();
        else switchOn();
    }

    @Override
    public void openSettings(JFrame parent) {
        int option = JOptionPane.showConfirmDialog(parent, "Toggle lock state?", getName(), JOptionPane.YES_NO_OPTION);
        if(option == JOptionPane.YES_OPTION) toggle();
    }
}

class CCTV extends SmartDevice {
    public CCTV(String name) { super(name); }
    public String getStatus() { return isOn() ? "Recording" : "Idle"; }
    public String getIcon() { return "\uD83D\uDCF7"; } // 📷
}

class SmartSpeaker extends SmartDevice {
    private boolean muted = false;

    public SmartSpeaker(String name) { super(name); }

    public boolean isMuted() { return muted; }

    public void mute() { muted = true; }
    public void unmute() { muted = false; }

    public String getStatus() {
        if(!isOn()) return "OFF";
        return muted ? "Muted" : "Playing";
    }

    public String getIcon() { return "\uD83D\uDD0A"; } // 🔊

    @Override
    public void openSettings(JFrame parent) {
        if(!isOn()) {
            JOptionPane.showMessageDialog(parent, "Speaker is OFF. Turning ON.");
            switchOn();
        }
        Object[] options = {"Mute", "Unmute", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, "Speaker Control", getName(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[2]);
        if(choice == 0) mute();
        else if(choice == 1) unmute();
    }
}

public class SmartHomeGUI extends JFrame {
    private final List<SmartDevice> devices = new ArrayList<>();
    private final JList<SmartDevice> deviceList;
    private final JTextPane statusArea;
    private final JButton toggleBtn, settingsBtn;
    private final JLabel titleLabel;

    public SmartHomeGUI() {
        super("🔥 Smart Home Controller");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 450));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout(10, 10));

        // Initialize devices
        devices.add(new Light("Living Room Light"));
        devices.add(new Light("Bedroom Light"));
        devices.add(new Light("Kitchen Light"));
        devices.add(new Light("Outdoor Light"));
        devices.add(new Fan("Bedroom Fan"));
        devices.add(new Thermostat("Main Thermostat"));
        devices.add(new DoorLock("Front Door Lock"));
        devices.add(new CCTV("Home CCTV"));
        devices.add(new SmartSpeaker("Living Room Speaker"));

        // Top panel with title
        titleLabel = new JLabel("🔥 Smart Home Controller", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(255, 140, 0));
        titleLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Left panel: Device list and controls
        JPanel leftPanel = new JPanel(new BorderLayout(10,10));
        leftPanel.setBackground(new Color(40, 40, 40));
        leftPanel.setBorder(new EmptyBorder(10, 15, 10, 10));
        add(leftPanel, BorderLayout.WEST);

        // Device list with custom renderer
        deviceList = new JList<>(devices.toArray(new SmartDevice[0]));
        deviceList.setCellRenderer(new DeviceListCellRenderer());
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceList.setBackground(new Color(60, 60, 60));
        deviceList.setForeground(Color.WHITE);
        deviceList.setFixedCellHeight(40);
        deviceList.setBorder(new LineBorder(new Color(120, 120, 120), 1));
        deviceList.setSelectedIndex(0);

        // Scroll pane for device list
        JScrollPane listScrollPane = new JScrollPane(deviceList);
        listScrollPane.setPreferredSize(new Dimension(300, 400));
        listScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150,150,150)), "Devices",
            0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(255, 140, 0)
        ));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Controls panel below the list
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(new Color(40, 40, 40));
        controlsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        leftPanel.add(controlsPanel, BorderLayout.SOUTH);

        toggleBtn = new JButton("Toggle Power");
        settingsBtn = new JButton("Settings");

        styleButton(toggleBtn, "\u23FB"); // Power symbol
        styleButton(settingsBtn, "\u2699"); // Gear symbol

        controlsPanel.add(toggleBtn);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlsPanel.add(settingsBtn);

        // Center panel: Status area with HTML support
        statusArea = new JTextPane();
        statusArea.setContentType("text/html");
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(20, 20, 20));
        statusArea.setForeground(new Color(0, 255, 0));
        statusArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        statusArea.setBorder(new CompoundBorder(new LineBorder(new Color(100, 100, 100), 2),
                new EmptyBorder(15, 15, 15, 15)));

        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150,150,150)), "Device Status",
            0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(255, 140, 0)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Add listeners
        deviceList.addListSelectionListener(e -> refreshStatus());
        toggleBtn.addActionListener(e -> toggleSelectedDevice());
        settingsBtn.addActionListener(e -> openSettingsForSelectedDevice());

        // Initial display
        refreshStatus();

        // Use javax.swing.Timer for thread safety to refresh status every 3 seconds
        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> refreshStatus());
        timer.start();

        // Keyboard shortcuts
        // Space toggles power on selected device
        deviceList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "togglePower");
        deviceList.getActionMap().put("togglePower", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSelectedDevice();
            }
        });

        // Enter opens settings
        deviceList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "openSettings");
        deviceList.getActionMap().put("openSettings", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSettingsForSelectedDevice();
            }
        });
    }

    private void styleButton(JButton btn, String iconUnicode) {
        btn.setFocusPainted(false);
        btn.setBackground(new Color(255, 140, 0));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 100, 0), 2),
                new EmptyBorder(8,15,8,15)
        ));
        btn.setIconTextGap(8);

        // Set button text with icon unicode in front
        btn.setText(iconUnicode + "  " + btn.getText());

        btn.setToolTipText(btn.getText());

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(255, 180, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(255, 140, 0));
            }
        });
    }

    private void toggleSelectedDevice() {
        int idx = deviceList.getSelectedIndex();
        if(idx < 0) return;
        SmartDevice device = devices.get(idx);
        if(device instanceof DoorLock){
            int opt = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to toggle the lock state of " + device.getName() + "?",
                    "Confirm Toggle",
                    JOptionPane.YES_NO_OPTION);
            if(opt != JOptionPane.YES_OPTION) return;
        }
        device.toggle();
        refreshStatus();
    }

    private void openSettingsForSelectedDevice() {
        int idx = deviceList.getSelectedIndex();
        if(idx < 0) return;
        SmartDevice device = devices.get(idx);
        try {
            device.openSettings(this);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
        }
        refreshStatus();
    }

    private void refreshStatus() {
        StringBuilder sb = new StringBuilder("<html><body style='font-family: Monospaced; font-weight: bold;'>");
        sb.append("<h2 style='color: #FF8C00;'>=== Smart Home Device Status ===</h2><br>");
        for(SmartDevice d : devices) {
            String statusColor = d.isOn() ? "#32CD32" : "#D9534F"; // Green for ON, Red for OFF
            String icon = d.getIcon();
            sb.append(String.format(
                "<p style='font-size: 14px; color: #FFFFFF;'>" +
                "<span style='font-size:18px;'>%s</span> " +
                "<span>%s :</span> " +
                "<span style='color: %s;'>%s</span></p>",
                icon, d.getName(), statusColor, d.getStatus()));
        }
        sb.append("</body></html>");
        statusArea.setText(sb.toString());
    }

    private static class DeviceListCellRenderer extends JLabel implements ListCellRenderer<SmartDevice> {
        private static final Color selectedBg = new Color(255, 140, 0);
        private static final Color selectedFg = Color.BLACK;

        public DeviceListCellRenderer() {
            setOpaque(true);
            setFont(new Font("SansSerif", Font.BOLD, 16));
            setBorder(new EmptyBorder(5,10,5,10));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends SmartDevice> list,
                                                      SmartDevice value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(value.getIcon() + "  " + value.getName());
            if (isSelected) {
                setBackground(selectedBg);
                setForeground(selectedFg);
            } else {
                setBackground(new Color(60, 60, 60));
                setForeground(Color.WHITE);
            }
            return this;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 14));
            UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 16));
            UIManager.put("List.font", new Font("SansSerif", Font.BOLD, 16));
            UIManager.put("ToolTip.font", new Font("SansSerif", Font.PLAIN, 14));
            UIManager.put("OptionPane.messageFont", new Font("SansSerif", Font.PLAIN, 14));
            UIManager.put("OptionPane.buttonFont", new Font("SansSerif", Font.BOLD, 14));
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SmartHomeGUI gui = new SmartHomeGUI();
            gui.setVisible(true);
        });
    }
}


