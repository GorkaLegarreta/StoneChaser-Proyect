package main.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.JobAttributes.DefaultSelectionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import main.Game;
import main.GameDB;
import main.GameDBException;
import main.states.MenuState;

public class Viewer extends JFrame {

	private static final long serialVersionUID = -1092180823219722606L;
	private DefaultListModel<String> userNameListModel;
	private JList<String> userNameJList;
	private JLabel sessions, x, y;
	private JPanel down;
	//inicio
	private JTable table;
	private JScrollPane scrollPane;
	//fin
	private DefaultTableModel dtm;
	private String[] header;
	private Object[][] data;
	private int user_code;
	
	public Viewer() {
		// Settings
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
		setTitle("Game Session Viewer");
		setSize(700, 400);
		setLocationRelativeTo(null);
		
		// MenuBar
		configMenuBar();
		
		// Central Panel
		configFrameDivider();
		
		// Upload JList
		loadJList();
		
		//Closing events
		addWindowListener(closingEvents());
		
	}
	
	public void configMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu fichero = new JMenu("Fichero");
		JMenuItem salir = new JMenuItem("Salir");
		fichero.add(salir);
		menuBar.add(fichero);
		
		JMenu datos = new JMenu("Datos");
		JMenuItem exportar = new JMenuItem("Exportar");
		datos.add(exportar);
		menuBar.add(datos);
		
		JMenu tabla = new JMenu("Tabla");
		JMenuItem todos = new JMenuItem("Mostrar todo");
		tabla.add(todos);
		menuBar.add(tabla);
		
		salir.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				Game.getWindow().setVisibility();	
			}
		});
		exportar.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
							
			}
		});
		todos.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				createInventoryTable();
				loadUserInventoryTable();
			}
		});
	}
		
	/**
	 * JFrame will be divided into three panels: left, right up and right bottom panels.
	 */
	public void configFrameDivider() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		// Left
		Border border = BorderFactory.createTitledBorder("Current UserNames:");		
		JPanel left = new JPanel(new BorderLayout());
		left.setBorder(border);
		userNameListModel = new DefaultListModel<String>(); 
		userNameJList = new JList<String>(userNameListModel);
		userNameJList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		left.add(userNameJList, BorderLayout.CENTER);
		JLabel bottom = new JLabel("Usuarios inicializados: "+GameDB.getGameUsers()+"/4");
		left.add(bottom, BorderLayout.SOUTH);
		userNameJList.addListSelectionListener(new ListSelectionListener() {			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				user_code = userNameJList.getSelectedIndex()+1;
				try {
					if (GameDB.existsGamePlayer(user_code)) {
						loadUserInformation();
						createInventoryTable();
						loadUserInventoryTable();
					} 
				} catch (GameDBException ex) {
					ex.printStackTrace();
				}
			}			
		});
		
		// Right
		JPanel right = new JPanel(new GridBagLayout());
		
		// Right up
		border = BorderFactory.createTitledBorder("Information about User:");	
		JPanel up = new JPanel(new GridLayout(2,2));
		sessions = new JLabel("Sesiones: ");
		up.add(sessions);		
		up.add(new JPanel());
		x = new JLabel("Player_X: ");
		up.add(x);
		y = new JLabel("Player_Y: ");
		up.add(y);
		up.setBorder(border);
		
		// Right bottom
		border = BorderFactory.createTitledBorder("Inventory of the User:");
		down = new JPanel(new BorderLayout());
		down.setBorder(border);
		
		
				
		addComponentToLayout(panel,left,0,0);
		addComponentToLayout(panel,right,0,1);
		addComponentToRightLayout(right, up, 0, 0);
		addComponentToRightLayout(right, down, 1, 0);
		add(panel);
		
		
	}
	
	public void loadJList() {
		for (int i=0; i<4; i++) {
			userNameListModel.addElement(GameDB.getGameUserName(i+1));
		}		
	}
	
	private void createInventoryTable() {
		String [] header = {"Jugador","Espacio en Inventario","Item","Cantidad"};
		data = null;
		dtm = new DefaultTableModel(data, header);
		table = new JTable(dtm);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		down.add(scrollPane, BorderLayout.CENTER);
	}
	public void loadUserInventoryTable() {
		try {
			for(int i=0;i<6;i++) {
				if (GameDB.theObjectInPlayersInventory(user_code, i)) {
					Object [] rowData = {getUserName(user_code), i, getItemName(user_code, i), getItemQuantity(user_code, i)};
					dtm.addRow(rowData);
				}
			}				
		} catch (GameDBException e) {
			e.printStackTrace();
		}
	}
	public void loadTable() {
		try {
			ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
			for(int i=0;i<6;i++) {
				if (GameDB.theObjectInPlayersInventory(user_code, i)) {
					ArrayList<Object> arr = new ArrayList<Object>();
					arr.add(getUserName(user_code));
					arr.add(i);
					arr.add(getItemName(user_code, i));
					arr.add(getItemQuantity(user_code, i));
					data.add(arr);
				}
			}
			MyTableModel mod = new MyTableModel(data);
	        table.setModel((TableModel) mod);
	        JScrollBar sc = scrollPane.getVerticalScrollBar();
	        sc.setValue(0);
		} catch (GameDBException e) {
			e.printStackTrace();
		}
	}
	class MyTableModel implements TableModel {
    	ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
    	ArrayList<String> columnNames = new ArrayList<String>();

		public MyTableModel( ArrayList<ArrayList<Object>> d) {
			super();
	    	columnNames.add("Jugador");
	    	columnNames.add("Posici�n en Inventario");
	    	columnNames.add("Item");
	    	columnNames.add("Cantidad");
			for (ArrayList<Object> a: d) {
				data.add(a);
			}
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.size();
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames.get(columnIndex);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data.get(rowIndex).get(columnIndex);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

		@Override
		public void addTableModelListener(TableModelListener l) {}

		@Override
		public void removeTableModelListener(TableModelListener l) {}
	}
	
//	public void loadCompletelyInventoryTable() {	
//		try {
//			for (int user_code = 1; user_code<5; user_code++) {
//				
//					for(int i=0;i<6;i++) {
//						if (GameDB.theObjectInPlayersInventory(user_code, i)) {
//							Object [] rowData = {getName(user_code), i, getItem(user_code, i), getQuantity(user_code, i)};
//							dtm.addRow(rowData);
//						}
//					}					
//			} 
//			
//		} catch (GameDBException e) {
//			e.printStackTrace();
//		}
//	}
	
	
	private void addComponentToLayout(JPanel form, Component component, int row, int column) {   
		  GridBagConstraints constraints = new GridBagConstraints();   
		  constraints.fill = GridBagConstraints. BOTH ;    
		  constraints.gridx = column;   
		  constraints.gridy = row;   
		  constraints.gridwidth = 1;    
		  constraints.weightx = column == 0 ? 0.2 : 0.8;   
		  constraints.weighty = 1;    
		  form.add(component, constraints); 
	}
	
	private void addComponentToRightLayout(JPanel form, Component component, int row, int column) {   
		  GridBagConstraints constraints = new GridBagConstraints();   
		  constraints.fill = GridBagConstraints. BOTH ;    
		  constraints.gridx = column;   
		  constraints.gridy = row;   
		  constraints.gridwidth = 1;    
		  constraints.weightx = 1;   
		  constraints.weighty = row == 0 ? 0.2 : 0.8;    
		  form.add(component, constraints); 
	}	
	
	public String getUserName(int user_code) {
		return GameDB.getGameUserName(user_code);
	}
	
	public String getItemName(int user_code, int item_index) {
		try {
			return GameDB.getItemName(user_code, item_index);
		} catch (GameDBException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public int getItemQuantity(int user_code, int item_index) {
		try {
			return GameDB.getInventoryObjectQuantity(user_code, item_index);
		} catch (GameDBException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public WindowListener closingEvents() {
		
		WindowListener wl = new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}	
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				Game.getWindow().setVisibility();		
			}			
			
			@Override
			public void windowClosed(WindowEvent e) {
				dispose();
				Game.getWindow().setVisibility();
			}	
			
			@Override
			public void windowActivated(WindowEvent e) {}
		};
		return wl;
	 }
	/*
	 * FUNCIONA PERFECTO
	 */
	public void loadUserInformation() {
		try {
			if (GameDB.existsGamePlayer(user_code)) {
				sessions.setText("Sesiones: "+Integer.toString(GameDB.getNumberSessions(user_code)));
				x.setText("Player_X: "+Integer.toString(GameDB.getGamePlayerXPosition(user_code)));
				y.setText("Player_Y: "+Integer.toString(GameDB.getGamePlayerYPosition(user_code)));
			} else {
				sessions.setText("Sesiones:");
				x.setText("Player_X:");
				y.setText("Player_Y:");
			}
		} catch (GameDBException e) {
			e.printStackTrace();
		}		
	}
	public void clearUserInformation() {
		try {
			if (GameDB.existsGamePlayer(user_code)) {
				sessions.setText("Sesiones: "+Integer.toString(GameDB.getNumberSessions(user_code)));
				x.setText("Player_X: "+Integer.toString(GameDB.getGamePlayerXPosition(user_code)));
				y.setText("Player_Y: "+Integer.toString(GameDB.getGamePlayerYPosition(user_code)));
			} else {
				sessions.setText("Sesiones:");
				x.setText("Player_X:");
				y.setText("Player_Y:");
			}
		} catch (GameDBException e) {
			e.printStackTrace();
		}		
	}
}

//data = {
//		{getName(user_code), 0, getItem(user_code, 0), getQuantity(user_code, 0)},
//		{getName(user_code), 1, getItem(user_code, 1), getQuantity(user_code, 1)},
//		{getName(user_code), 2, getItem(user_code, 2), getQuantity(user_code, 2)},
//		{getName(user_code), 3, getItem(user_code, 3), getQuantity(user_code, 3)},
//		{getName(user_code), 4, getItem(user_code, 4), getQuantity(user_code, 4)},
//		{getName(user_code), 5, getItem(user_code, 5), getQuantity(user_code, 5)}
//};
//Object[][] data = {
//		{"Mary", "Campione", "Esquiar", 6},
//		{"Lhucas", "Huml", "Patinar", 54},
//		{"Kathya", "Walrath", "Escalar", 4},
//		{"Marcus", "Andrews", "Correr",6},
//		{"Angela", "Lalth", "Nadar", 7}
//		};
//try {
//	for (int item_index = 0; item_index<6; item_index++) {
//		if (GameDB.theObjectInPlayersInventory(user_code, item_index)) {
//			
//			String name = userNameJList.getSelectedValue();
//			String space = Integer.toString(item_index);
//			String item = GameDB.getItemName(user_code, item_index);
//			String quantity = Integer.toString(GameDB.getInventoryObjectQuantity(user_code, item_index));
//			
//			String [] row = {name,space,item,quantity};					
//		}
//	}
//} catch (GameDBException e) {
//		e.printStackTrace();				
//}