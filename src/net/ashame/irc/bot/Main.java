package net.ashame.irc.bot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Nathan on 1/4/14.
 * http://www.powerbot.org/community/user/523484-nathan-l/
 * http://www.excobot.org/forum/user/906-nathan/
 */

@SuppressWarnings("SuspiciousMethodCalls")
public class Main {

    private static JFrame frame = new JFrame();
    private static JTextArea textAreas[];
    private static final Font font = new Font("Segoe UI", Font.PLAIN, 11);
    public static Map<String, String> summonerCache = new LinkedHashMap<>();
    public static int apiQueries = 0;

    private static String twitch_oauth = "";
    private static String nickserv_pw = "";

    private static Bot bots[] = new Bot[]{
            new Bot(0),
            new Bot(1),
            new Bot(2)
    };

    public static void main(String[] args) {
        try {
            loadProperties();
            initComponents();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There was an error while starting up!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        Properties props2 = new Properties();
        try {
            FileInputStream in = new FileInputStream("summoner_cache.txt");
            props2.load(in);
            in.close();
        } catch (Exception e) {
            System.out.println("Error loading summoner cache: " + e.getMessage());
        }
        for (Object o : props2.keySet()) {
            if (!summonerCache.containsKey(o)) {
                summonerCache.put((String) o, (String) props2.get(o));
            }
        }
    }

    public static void initComponents() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        frame.setTitle("ashmbot");
        frame.setLayout(new BorderLayout());

        final JTabbedPane tabbedPane = new JTabbedPane();
        final JScrollPane scrollPane = new JScrollPane();

        final JButton buttons[] = new JButton[]{
                new JButton("Join Server!"),
                new JButton("Join Server!"),
                new JButton("Join Server!")
        };

        final JTextField textFields[] = new JTextField[]{
                new JTextField(60),
                new JTextField(60),
                new JTextField(60)
        };

        textAreas = new JTextArea[]{
                new JTextArea(),
                new JTextArea(),
                new JTextArea()
        };

        final JPanel panels[] = new JPanel[]{
                new JPanel(new FlowLayout()),
                new JPanel(new FlowLayout()),
                new JPanel(new FlowLayout())
        };

        for (JTextArea textArea : textAreas) {
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setFont(font);
        }

        for (int i = 0; i < panels.length; i++) {
            panels[i].add(textFields[i]);
            panels[i].add(Box.createHorizontalGlue());
            panels[i].add(buttons[i]);
        }

        scrollPane.setViewportView(textAreas[0]);
        scrollPane.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight() + 300));

        tabbedPane.addTab("Twitch", panels[0]);
        tabbedPane.addTab("Rizon", panels[1]);
        tabbedPane.addTab("AnimeBytes", panels[2]);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int i = tabbedPane.getSelectedIndex();
                scrollPane.setViewportView(textAreas[i]);
                textAreas[i].scrollRectToVisible(new Rectangle(0, textAreas[i].getHeight(), 0, 0));
            }
        });

        for (JButton joinButton : buttons) {
            joinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        int i = tabbedPane.getSelectedIndex();
                        if (!bots[i].isConnected()) {
                            connect(i);
                        } else {
                            JOptionPane.showMessageDialog(frame, "You are already connected to the server!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        for (final JTextField textField : textFields) {
            textField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        int i = tabbedPane.getSelectedIndex();
                        if (bots[i].isConnected()) {
                            bots[i].sendRawLine(textField.getText());
                            textField.setText("");
                        } else {
                            JOptionPane.showMessageDialog(frame, "You aren't connected to the server! Hit join before entering commands!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
        }

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(tabbedPane, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public static void log(String s, int id) {
        textAreas[id].append(s + System.getProperty("line.separator"));
        textAreas[id].scrollRectToVisible(new Rectangle(0, textAreas[id].getHeight(), 0, 0));
        System.out.println(s);
    }

    public static void connect(int id) throws Exception {
        bots[id].setVerbose(true);
        switch (id) {
            case 0:
                bots[id].connect("irc.twitch.tv", 6667, twitch_oauth);
                break;
            case 1:
                bots[id].connect("irc.rizon.net", 6667);
                bots[id].sendMessage("nickserv", "identify " + nickserv_pw);
                break;
            case 2:
                bots[id].connect("irc.animebytes.tv", 6667);
                bots[id].sendMessage("nickserv", "identify " + nickserv_pw);
                break;
            default:
                break;
        }
    }

    public static void loadProperties() {
        Properties properties = new Properties();
        try {
            FileInputStream in = new FileInputStream("settings.properties");
            properties.load(in);
            twitch_oauth = properties.getProperty("TWITCH_OAUTH");
            System.out.println(twitch_oauth);
            nickserv_pw= properties.getProperty("NICKSERV_PW");
            System.out.println(nickserv_pw);
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("Settings file not found; creating new default file.");
            try {
                Properties prop = new Properties();
                OutputStream out = new FileOutputStream(new File("settings.properties"));
                prop.put("API_KEY", "replace_with_api_key");
                prop.put("TWITCH_OAUTH", "oauth:");
                prop.put("NICKSERV_PW", "password");
                prop.store(out, "Authentication Info");
                out.flush();
                out.close();
            } catch (Exception ex) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
