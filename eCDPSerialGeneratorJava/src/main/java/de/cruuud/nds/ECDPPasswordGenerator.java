package de.cruuud.nds;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;

public class ECDPPasswordGenerator {

    public static final int MAX_STORE_NUMBER_FOR_RANDOM_PLUS_1 = 1000000;

    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        ILogger logger = System.out::println;
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
        MaskFormatter passwordFormatter = null;
        try {
            macFormatter = new MaskFormatter("AA-AA-AA-AA-AA-AA");
            macFormatter.setPlaceholder("00-00-00-00-00-00");
            macFormatter.setValidCharacters("0123456789abcdefABCDEF");

            passwordFormatter = new MaskFormatter("AAAAAA");
            passwordFormatter.setPlaceholder("");
            passwordFormatter.setValidCharacters(ECDP.PASSWORD_ALPHABET + ECDP.PASSWORD_ALPHABET.toLowerCase());

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

        final JFormattedTextField txtPassword = new JFormattedTextField(passwordFormatter);
        final JLabel lblPassword = new JLabel("Your password: ", JLabel.RIGHT);
        lblPassword.setFont(fontLabel);
        lblPassword.setToolTipText("Your generated password/serial code");
        txtPassword.setToolTipText(lblPassword.getToolTipText());

        txtPassword.setFont(font);
        txtPassword.setBackground(Color.black);
        txtPassword.setForeground(Color.green);
        txtPassword.setCaretColor(Color.white);

        final JButton btnGenerate = new JButton("Generate password");
        btnGenerate.setMnemonic('G');
        btnGenerate.setToolTipText("Generate a password based on given MAC, store and store management number");

        final JButton btnReverse = new JButton("Reverse from password");
        btnReverse.setMnemonic('R');
        btnReverse.setToolTipText("Calculate the Store and Store Management Numbers based on the MAC address and given password (password must be entered!)");
        btnReverse.setEnabled(false);

        final JButton btnGenerateMultiple = new JButton("Bulk generate passwords");
        btnGenerateMultiple.setMnemonic('B');
        btnGenerateMultiple.setToolTipText("Generate multiple passwords");

        final JButton btnShowMaster = new JButton("Show masterpassword");
        btnShowMaster.setMnemonic('M');
        btnShowMaster.setToolTipText("Show the master password that works with any Store, Store Manager and MAC address combination");

        final JCheckBox chkRandomStore = new JCheckBox("Random store numbers");
        final JCheckBox chkRandomManagement = new JCheckBox("Random management numbers");
        final JSpinner spinCountPasswords = new JSpinner();
        final SpinnerNumberModel model = new SpinnerNumberModel(10, 1, 1000, 10);
        spinCountPasswords.setModel(model);
        final JLabel lblSpinner = new JLabel("Number of passwords: ");
        lblSpinner.setToolTipText("Set number of passwords to generate, or maximum number of passwords to reverse");
        spinCountPasswords.setToolTipText(lblSpinner.getToolTipText());
        final JPanel pnlSpinner = new JPanel();
        pnlSpinner.add(lblSpinner);
        pnlSpinner.add(spinCountPasswords);

        final JPanel pnlGenerator = new JPanel(new BorderLayout());
        pnlGenerator.setBorder(BorderFactory.createLoweredBevelBorder());
        final JPanel pnlCheckBox = new JPanel(new GridLayout(3, 1));
        pnlCheckBox.add(pnlSpinner);
        pnlCheckBox.add(chkRandomStore);
        pnlCheckBox.add(chkRandomManagement);
        pnlGenerator.add(btnGenerateMultiple, BorderLayout.CENTER);
        pnlGenerator.add(pnlCheckBox, BorderLayout.EAST);

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
        pnlButton.add(btnShowMaster);
        pnlButton.add(btnGenerate);
        pnlButton.add(btnReverse);

        final JCheckBox chkLimitReverse = new JCheckBox("Limit result");
        chkLimitReverse.setToolTipText("Limit the number of combinations to be found to the number given at the 'Number of passwords' selection");
        pnlButton.add(chkLimitReverse);

        final JPanel pnlButtons = new JPanel(new BorderLayout());

        pnlButtons.add(pnlButton, BorderLayout.EAST);
        pnlButtons.add(pnlGenerator, BorderLayout.SOUTH);
        pnlButtons.setBorder(BorderFactory.createRaisedSoftBevelBorder());

        pnlMain.add(pnlButtons, BorderLayout.CENTER);

        final JTextArea txtLogging = new JTextArea("", 16, 64);
        txtLogging.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
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
                String password = ECDP.generatePassword(logger, mac, store, storeManagement);
                txtPassword.setText(password);
                txtMacAddress.setText(txtMacAddress.getText().toUpperCase());
                btnReverse.setEnabled(true);
            } catch (IllegalArgumentException ie) {
                JOptionPane.showMessageDialog(frame, ie.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnReverse.addActionListener(e -> {
            txtLogging.setText("");
            final String mac = txtMacAddress.getText().replaceAll("-", "");
            final String password = txtPassword.getText().toUpperCase();
            txtPassword.setText(password);
            long numPasswords = ECDP.reverseFromPassword(logger, mac, password, (chkLimitReverse.isSelected() ? (Integer) spinCountPasswords.getValue() : 1000000000000L), 0);
            if (numPasswords == 0) {
                JOptionPane.showMessageDialog(frame, "No valid Store and Store Management number combination found for the given MAC and password combination!", "NO VALID COMBINATIONS FOUND", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Found " + numPasswords + " valid Store and Store Management number combinations for the given MAC address and password" + (chkLimitReverse.isSelected() ? "\n\nLimited results to " + (Integer) spinCountPasswords.getValue() + " combinations" : ""), numPasswords + " combinations found", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnShowMaster.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "The following password/code works with any MAC address,\nStore and Store Management number combination:\n\n" + ECDP.MASTER_PASSWORD, "The master password: " + ECDP.MASTER_PASSWORD, JOptionPane.INFORMATION_MESSAGE);
        });

        btnGenerateMultiple.addActionListener(e -> {
            txtLogging.setText("");
            for (int i = 0; i < (Integer) spinCountPasswords.getValue(); i++) {
                int storeNumber = i;
                int storeManagementNumber = i;
                if (chkRandomStore.isSelected()) {
                    storeNumber = (int) (Math.random() * MAX_STORE_NUMBER_FOR_RANDOM_PLUS_1);
                }
                if (chkRandomManagement.isSelected()) {
                    storeManagementNumber = (int) (Math.random() * MAX_STORE_NUMBER_FOR_RANDOM_PLUS_1);
                }
                storeNumber %= MAX_STORE_NUMBER_FOR_RANDOM_PLUS_1;
                storeManagementNumber %= MAX_STORE_NUMBER_FOR_RANDOM_PLUS_1;
                final String mac = txtMacAddress.getText().replaceAll("-", "");
                txtMacAddress.setText(txtMacAddress.getText().toUpperCase());

                final String txtStore = String.format("%06d", storeNumber);
                final String txtStoreManagement = String.format("%06d", storeManagementNumber);
                final String password = ECDP.generatePassword(ECDP.NULL_LOGGER, mac, txtStore, txtStoreManagement);
                txtLogging.append(String.format("[mac-address=%s][store=%s][store management=%s][password=%s]\n", mac, txtStore, txtStoreManagement, password));
            }
        });

        txtStoreNumber.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setGenerateButtonState(btnGenerate, txtStoreNumber, txtStoreManagerNumber);
            }
        });

        txtStoreManagerNumber.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setGenerateButtonState(btnGenerate, txtStoreNumber, txtStoreManagerNumber);
            }
        });
        txtStoreNumber.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setGenerateButtonState(btnGenerate, txtStoreNumber, txtStoreManagerNumber);
            }

            @Override
            public void focusGained(FocusEvent e) {
                setGenerateButtonState(btnGenerate, txtStoreNumber, txtStoreManagerNumber);
            }
        });
        txtStoreManagerNumber.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setGenerateButtonState(btnGenerate, txtStoreNumber, txtStoreManagerNumber);
            }

            @Override
            public void focusGained(FocusEvent e) {
                setGenerateButtonState(btnGenerate, txtStoreNumber, txtStoreManagerNumber);
            }
        });

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                btnReverse.setEnabled(txtPassword.getText().trim().length() == 6);
            }

            @Override
            public void focusLost(FocusEvent e) {
                btnReverse.setEnabled(txtPassword.getText().trim().length() == 6);
            }
        });
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                btnReverse.setEnabled(txtPassword.getText().trim().length() == 6);
            }
        });
    }

    private static void setGenerateButtonState(final JButton btnGenerate, final JTextField txtStoreNumber, final JTextField txtStoreManagerNumber) {
        final String store = txtStoreNumber.getText().trim();
        final String storeManagement = txtStoreManagerNumber.getText().trim();
        if (store.length() < 6 || storeManagement.length() < 6) {
            btnGenerate.setEnabled(false);
        } else {
            btnGenerate.setEnabled(true);
        }
    }
}

