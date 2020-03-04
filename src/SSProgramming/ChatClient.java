package SSProgramming;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

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
	    // TODO: Add a list box
	    DefaultListModel<String> ListBox = new DefaultListModel<>();
	    JList<String> Nlist = new JList<>(ListBox);	
	    
	    //Added a Checkbox
	    JCheckBox checkbox = new JCheckBox("Check to BROADCAST");
	    boolean check = true;

	    /**
	     * Constructs the client by laying out the GUI and registering a
	     * listener with the textfield so that pressing Return in the
	     * listener sends the textfield contents to the server.  Note
	     * however that the textfield is initially NOT editable, and
	     * only becomes editable AFTER the client receives the NAMEACCEPTED
	     * message from the server.
	     */
	    public ChatClient() {

	        // Layout GUI
	        textField.setEditable(false);
	        messageArea.setEditable(false);
	        frame.getContentPane().add(textField, "North");
	        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
	        frame.getContentPane().add(Nlist,"South");
	        Nlist.setPreferredSize(new Dimension(250,80));
	        Nlist.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	        Nlist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	        frame.getContentPane().add(checkbox, "East");
	        checkbox.setSelected(false);
	        frame.pack();
	        
	        
	        //Initiliazing the checkbox
	        checkbox.addActionListener(new ActionListener() {	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(checkbox.isSelected()) {
						check = true;
					}
					else {
						check = false;
					}
				}
			});


	        // TODO: You may have to edit this event handler to handle point to point messaging,
	        // where one client can send a message to a specific client. You can add some header to 
	        // the message to identify the recipient. You can get the receipient name from the listbox.
	        textField.addActionListener(new ActionListener() {
	            /**
	             * Responds to pressing the enter key in the textfield by sending
	             * the contents of the text field to the server.    Then clear
	             * the text area in preparation for the next message.
	             */
	            public void actionPerformed(ActionEvent e) {
	            	if(check || Nlist.isSelectionEmpty()){
	            			out.println(textField.getText());
	            			textField.setText("");
	            	}
	            	else {
	            		HashSet<String> selectedNames = new HashSet<String>();//Creating a hashset to add the selected names from the JList
	            		int[] indices = Nlist.getSelectedIndices();//Adding the selected names indices to a variable
	            		for(int i=0;i<indices.length;i++) {
	            			selectedNames.add(Nlist.getModel().getElementAt(indices[i]));//adding the selected names to the hashset
	            			System.out.println(Nlist.getModel().getElementAt(indices[i]));
	            		}
	            		out.println("MULTI"+textField.getText()+ selectedNames); /*sending out the message to the server to be sent to the selected names 
	            																	identified by adding the header "MULTI"*/
	            		System.out.println("MULTI "+textField.getText()+ selectedNames);
	            	}
	                
	            }
	        });
	        
	        
	    }

	    /**
	     * Prompt for and return the address of the server.
	     */
	    private String getServerAddress() {
	        return JOptionPane.showInputDialog(
	            frame,
	            "Enter IP Address of the Server:",
	            "Welcome to the Chatter",
	            JOptionPane.QUESTION_MESSAGE);
	    }

	    /**
	     * Prompt for and return the desired screen name.
	     */
	    private String getName() {
	        return JOptionPane.showInputDialog(
	            frame,
	            "Choose a screen name:",
	            "Screen name selection",
	            JOptionPane.PLAIN_MESSAGE);
	    }

	    /**
	     * Connects to the server then enters the processing loop.
	     */
	    private void run() throws IOException {

	        // Make connection and initialize streams
	        String serverAddress = getServerAddress();
	        Socket socket = new Socket(serverAddress, 9001);
	        in = new BufferedReader(new InputStreamReader(
	            socket.getInputStream()));
	        out = new PrintWriter(socket.getOutputStream(), true);

	        // Process all messages from server, according to the protocol.
	        
	        // TODO: You may have to extend this protocol to achieve task 9 in the lab sheet
	        while (true) {
	            String line = in.readLine();
	            if (line.startsWith("SUBMITNAME")) {
	                out.println(getName());
	            } else if (line.startsWith("NAMEACCEPTED")) {
	                textField.setEditable(true);
	               
	                
	                
	            } else if (line.startsWith("MESSAGE")) {
	                messageArea.append(line.substring(8) + "\n");
	             
	            }else if (line.startsWith("NEWNAME")) {
	            	ListBox.addElement(line.substring(7));
	            }else if (line.startsWith("MULTIC")) {
	            	messageArea.append(line.substring(7) + "\n");
	            }else if(line.startsWith("EXIT")) {
	            	ListBox.remove(ListBox.indexOf(line.substring(4)));
	            }
	        }
	    }

	    /**
	     * Runs the client as an application with a closeable frame.
	     */
	    public static void main(String[] args) throws Exception {
	        ChatClient client = new ChatClient();
	        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        client.frame.setVisible(true);
	        client.run();
	    }
}			
