package chatserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import javax.swing.*;

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);

    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> userList = new JList<>(listModel);
    JCheckBox broadcastBox = new JCheckBox("Broadcast");

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.
     */
    public ChatClient() {

        textField.setEditable(false);
        messageArea.setEditable(false);

        userList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(new JScrollPane(userList), "East");
        frame.getContentPane().add(broadcastBox, "South");

        frame.pack();

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String msg = textField.getText();
                List<String> selected = userList.getSelectedValuesList();

                if (!broadcastBox.isSelected() && !selected.isEmpty()) {
                    String targets = String.join(",", selected);
                    out.println(targets + ">>" + msg);
                } else {
                    out.println(msg);
                }
                textField.setText("");
            }
        });
    }

    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);
    }

    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {

        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);

        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            String line = in.readLine();

            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());

            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);

            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");

            } else if (line.startsWith("USERLIST")) {
                listModel.clear();
                String[] users = line.substring(9).split(",");
                for (String user : users) {
                    listModel.addElement(user);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
