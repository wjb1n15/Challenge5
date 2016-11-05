import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class Editor {
	
	private class MyDataModel extends AbstractTableModel {
		public int getColumnCount() { 
      	  if(varRecord.isEmpty())
      		  return 0;
      	  else
      		  return varRecord.get(varRecord.size() - 1).size();
        }
        public int getRowCount() { return varRecord.size() + 1;}
        public Object getValueAt(int row, int col) { 
      	  if(row == 0)
      		  return varRecord.get(varRecord.size() - 1).get(col).getName();
      	  else {
      		  if(varRecord.get(row - 1).size() <= col)
      			  return "0";
      		  else
      			  return String.valueOf(varRecord.get(row - 1).get(col).getValue());
      	  }
        }
        public void update() {
      	  fireTableStructureChanged();
      	  fireTableDataChanged();
        }
	}

	private JFrame frame;
	
	protected File sourceFile;
	protected Interpreter interpreter = new Interpreter();
	protected ArrayList<ArrayList<Variable>> varRecord;
	protected MyDataModel dataModel;
	private JTextPane textPane;
	private JTable tblVars;
	protected Font sizableFont;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Editor window = new Editor();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Editor() {
		initialize();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialize() {
		sizableFont = new Font(Font.SANS_SERIF, 3, 12);
		
		varRecord = new ArrayList<ArrayList<Variable>>();
		
		frame = new JFrame();
		frame.setBounds(100, 100, 1160, 696);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[grow 50][grow]", "[][grow]"));
		
		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newDoc();
			}
		});
		frame.getContentPane().add(btnNew, "flowx,cell 0 0");
		
		JLabel lblVariables = new JLabel("Variables");
		frame.getContentPane().add(lblVariables, "cell 1 0");
		
		textPane = new JTextPane();
		JScrollPane scrlTextPane = new JScrollPane(textPane);
		scrlTextPane.setVisible(true);
		frame.getContentPane().add(scrlTextPane, "flowx,cell 0 1,grow");
		
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) { 
				save();
			}
		});
		
		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		frame.getContentPane().add(btnOpen, "cell 0 0");
		frame.getContentPane().add(btnSave, "cell 0 0");
		
		JButton btnSaveAs = new JButton("Save As");
		btnSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		frame.getContentPane().add(btnSaveAs, "cell 0 0");
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				interpreter.setCode(textPane.getText());
				interpreter.run();
				varRecord = interpreter.getVarRecord();
				dataModel.update();
				showError();
			}
		});
		frame.getContentPane().add(btnRun, "cell 0 0");
		
		JButton btnStep = new JButton("Step");
		btnStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				interpreter.setCode(textPane.getText());
				interpreter.setRunning(true);
				highlightLine(interpreter.getPC(), Color.GREEN);
				interpreter.step();
				varRecord = interpreter.getVarRecord();
				dataModel.update();
				showError();
			}
		});
		frame.getContentPane().add(btnStep, "cell 0 0");
		
		dataModel = new MyDataModel();
		
		tblVars = new JTable(dataModel);
		tblVars.setRowSelectionAllowed(false);
		JScrollPane scrlTable = new JScrollPane(tblVars);
		scrlTable.setVisible(true);
		frame.getContentPane().add(scrlTable, "cell 1 1,growx,growy");
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				interpreter.cancel();
				dataModel.update();
				highlightLine(0, Color.WHITE);
			}
		});
		frame.getContentPane().add(btnCancel, "cell 0 0");
		
		JButton btnPlus = new JButton("+");
		btnPlus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sizableFont = new Font(null, Font.PLAIN, sizableFont.getSize() + 1);
				textPane.setFont(sizableFont);
				tblVars.setFont(sizableFont);
			}
		});
		frame.getContentPane().add(btnPlus, "cell 0 0");
		
		JButton btnMinus = new JButton("-");
		btnMinus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sizableFont = new Font(null, Font.PLAIN, sizableFont.getSize() - 1);
				textPane.setFont(sizableFont);
				tblVars.setFont(sizableFont);
			}
		});
		frame.getContentPane().add(btnMinus, "cell 0 0");
		
	}
	
	//clears the text editor and starts work on a new file
	public void newDoc()
	{
		textPane.setText("");
		sourceFile = null;
	}
	
	public void save()
	{
		if(sourceFile == null) {
			saveAs();
		} else {
			try {
				PrintWriter writer = new PrintWriter(sourceFile);
				writer.write(textPane.getText());
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void saveAs()
	{
		JFileChooser chooser = new JFileChooser();
		if(chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			sourceFile = chooser.getSelectedFile();
			save();
		}
	}
	
	public void open()
	{
		JFileChooser chooser = new JFileChooser();
		if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			sourceFile = chooser.getSelectedFile();
			try {
				BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
				String toAdd;
				do {
					toAdd = reader.readLine();
					if(toAdd != null) {
						if(!textPane.getText().equals(""))
							toAdd = "\n" + toAdd;
						textPane.setText(textPane.getText() + toAdd);
					}
				} while (toAdd != null);
				reader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void showError()
	{
		if(interpreter.getLastErr() != -1)
			highlightLine(interpreter.getLastErr(), Color.red);
	}
	
	protected void highlightLine(int line, Color colour)
	{
		int i = 0;
		int count = 0;
		int start = 0;
		int end = -2;
		int breaks = 0;
		
		while(count <= line && i < textPane.getText().length()) {
			if(textPane.getText().charAt(i) == ';') {
				count++;
				start = end + 1;
				end = i + 2;
			} else if(/*textPane.getText().charAt(i) == '\n' ||*/ textPane.getText().charAt(i) == '\r') {
				breaks++;
			}
			
			i++;
		}
		
		if(i == textPane.getText().length() && end < i - 1) {
			start = end + 1;
			end = i + 1;
		}
		
		SimpleAttributeSet highlightStyle = new SimpleAttributeSet();
		StyleConstants.setBackground(highlightStyle, colour);
		StyledDocument doc = textPane.getStyledDocument();
		doc.setCharacterAttributes(0, doc.getLength(), new SimpleAttributeSet(), true);
		doc.setCharacterAttributes(start - breaks - 1, end - start - 1, highlightStyle, false);
	}

}
