package de.cruuud.nds;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;

public class ECDPPasswordGenerator {

    public static void main(String[] args) {
        final JFrame frame = new JFrame();

        try {
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(ECDPPasswordGenerator.class.getResource("/icon.png")));
        } catch (Exception ex) {

        }

        frame.getContentPane().setLayout(new BorderLayout());

        addComponents(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("eCDP Password Generator - By Cruuud");
        frame.pack();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screen.width / 2 - frame.getWidth() / 2, screen.height / 2 - frame.getHeight() / 2);
        frame.setResizable(true);
        frame.setVisible(true);
    }

    public static void addComponents(final JFrame frame) {

        final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 24);
        final Font fontLabel = new Font(Font.MONOSPACED, Font.BOLD, 16);
        MaskFormatter macFormatter = null;
        MaskFormatter codeFormatter = null;
        try {
            macFormatter = new MaskFormatter("AA-AA-AA-AA-AA-AA");
            macFormatter.setPlaceholder("00-00-00-00-00-00");
            macFormatter.setValidCharacters("0123456789abcdefABCDEF");

            codeFormatter = new MaskFormatter("######");
            codeFormatter.setPlaceholder("000000");
        } catch (ParseException pe) {

        }
        final JFormattedTextField txtMacAddress = new JFormattedTextField(macFormatter);
        final JLabel lblMac = new JLabel("MAC address: ", JLabel.RIGHT);
        lblMac.setFont(fontLabel);
        lblMac.setToolTipText("Enter MAC Address of your Nintent DS(i) WiFi Adapter (eg. 002A3DFB9936)");
        txtMacAddress.setToolTipText(lblMac.getToolTipText());
        txtMacAddress.setFont(font);
        txtMacAddress.setBackground(Color.black);
        txtMacAddress.setForeground(Color.cyan);
        txtMacAddress.setCaretColor(Color.white);

        final JFormattedTextField txtStoreNumber = new JFormattedTextField(codeFormatter);
        final JLabel lblStore = new JLabel("Store Number: ", JLabel.RIGHT);
        lblStore.setToolTipText("Enter McDonalds Store Number (make up any 6 digit number)");
        lblStore.setFont(fontLabel);
        txtStoreNumber.setToolTipText(lblStore.getToolTipText());
        txtStoreNumber.setFont(font);
        txtStoreNumber.setBackground(Color.black);
        txtStoreNumber.setForeground(Color.red);
        txtStoreNumber.setCaretColor(Color.white);

        final JFormattedTextField txtStoreManagerNumber = new JFormattedTextField(codeFormatter);
        final JLabel lblStoreManagement = new JLabel("Store Management Number: ", JLabel.RIGHT);
        lblStoreManagement.setFont(fontLabel);
        lblStoreManagement.setToolTipText("Enter McDonalds Store Management Number of DS Card (make up any 6 digit number)");
        txtStoreManagerNumber.setToolTipText(lblStoreManagement.getToolTipText());
        txtStoreManagerNumber.setFont(font);
        txtStoreManagerNumber.setBackground(Color.black);
        txtStoreManagerNumber.setForeground(Color.yellow);
        txtStoreManagerNumber.setCaretColor(Color.white);

        final JFormattedTextField txtPassword = new JFormattedTextField();
        final JLabel lblPassword = new JLabel("Your password: ", JLabel.RIGHT);
        lblPassword.setFont(fontLabel);

        txtPassword.setEditable(false);
        txtPassword.setFont(font);
        txtPassword.setBackground(Color.black);
        txtPassword.setForeground(Color.green);
        txtPassword.setCaretColor(Color.white);

        final JButton btnGenerate = new JButton("Generate password");
        btnGenerate.setMnemonic('G');
        btnGenerate.setToolTipText("Password would be invalid when both Store and Store Management Number are 000000");

        final JPanel pnlFields = new JPanel(new GridLayout(4, 2, 0, 0));
        pnlFields.add(lblMac);
        pnlFields.add(txtMacAddress);
        pnlFields.add(lblStore);
        pnlFields.add(txtStoreNumber);
        pnlFields.add(lblStoreManagement);
        pnlFields.add(txtStoreManagerNumber);
        pnlFields.add(lblPassword);
        pnlFields.add(txtPassword);

        final JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createRaisedBevelBorder());

        pnlMain.add(pnlFields, BorderLayout.NORTH);

        final JPanel pnlButton = new JPanel();
        pnlButton.add(btnGenerate);
        pnlMain.add(pnlButton, BorderLayout.EAST);

        final JTextArea txtLogging = new JTextArea("", 16, 64);
        txtLogging.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 8));
        txtLogging.setBackground(Color.black);
        txtLogging.setCaretColor(Color.YELLOW);
        txtLogging.setForeground(Color.LIGHT_GRAY);
        txtLogging.setEditable(false);

        final JScrollPane pnlLogging = new JScrollPane(txtLogging, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pnlLogging.setBorder(BorderFactory.createLoweredBevelBorder());
        final ILogger logger = new JTextAreaLogger(txtLogging);

        final JPanel pnlForLayout = new JPanel();
        pnlForLayout.add(pnlMain);
        frame.getContentPane().add(pnlForLayout, BorderLayout.NORTH);
        frame.getContentPane().add(pnlLogging, BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> {
            try {
                txtLogging.setText("");
                final String mac = txtMacAddress.getText().replaceAll("-", "");
                final String store = txtStoreNumber.getText();
                final String storeManagement = txtStoreManagerNumber.getText();
                if (store.equals("000000") && storeManagement.equals("000000")) {
                    txtPassword.setText("");
                } else {
                    String password = ECDP.generatePassword(logger, mac, store, storeManagement);
                    txtPassword.setText(password);
                }
            } catch (IllegalArgumentException ie) {
                JOptionPane.showMessageDialog(frame, ie.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

