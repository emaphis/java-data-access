package browser;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.*;

public class DatabaseBrowser extends JFrame {

	private static final long serialVersionUID = -4239697698079982662L;
	
	private Connection connection;
	private JComboBox<Object> catalogBox;
	private JComboBox<Object> schemaBox;
	private JComboBox<Object> tableBox;
	private JTable table;

	public static void main(String[] args) {
		try {
			//new postgesql.driver.PostgresDriver
			//DatabaseBrowser browser = new DatabaseBrowser();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			//browser.
		}
	}

	public DatabaseBrowser() throws Exception {
		super("Database Browser");
		ConnectionDialog dialog = new ConnectionDialog(this);
		connection = dialog.getConnection();
		buildFrameLayout();
		setSize(600, 450);
		setVisible(true);
	}

	private void buildFrameLayout() {
		Container pane = getContentPane();
		pane.add(getSelectionPanel(), BorderLayout.NORTH);
		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		refreshTable();
		pane.add(new JScrollPane(table), BorderLayout.CENTER);
		pane.add(getFrameButtonPanel(), BorderLayout.SOUTH);
	}

	private JPanel getSelectionPanel() {
		JLabel lable;
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = 0;
		constraints.insets = new Insets(5, 10, 5, 10);
		lable = new JLabel("Catalog", JLabel.RIGHT);
		panel.add(lable, constraints);
		lable = new JLabel("Schema", JLabel.RIGHT);
		panel.add(lable, constraints);
		lable = new JLabel("Table", JLabel.RIGHT);
		panel.add(lable, constraints);

		constraints.gridy = 1;
		catalogBox = new JComboBox<Object>();
		populateCatalogBox();
		panel.add(catalogBox, constraints);
		schemaBox = new JComboBox<Object>();
		populateSchemaBox();
		panel.add(schemaBox, constraints);
		tableBox = new JComboBox<Object>();
		populateTableBox();
		panel.add(tableBox, constraints);

		catalogBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				String newCatalog = (String) (catalogBox.getSelectedItem());
				try {
					connection.setCatalog(newCatalog);
				} catch (Exception e) {
				}
				;
				populateSchemaBox();
				populateTableBox();
				refreshTable();
			}
		});

		schemaBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				populateTableBox();
				refreshTable();
			}
		});

		tableBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				refreshTable();
			}
		});
		return panel;
	}

	private void populateCatalogBox() {
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet resultSet = metaData.getCatalogs();
			ArrayList<String> values = new ArrayList<String>();
			while (resultSet.next()) {
				values.add(resultSet.getString(1));
			}
			resultSet.close();
			catalogBox.setModel(new DefaultComboBoxModel<Object>(values.toArray()));
			catalogBox.setSelectedItem(connection.getCatalog());
			catalogBox.setEnabled(values.size() > 0);
		} catch (Exception e) {
			catalogBox.setEnabled(false);
		} finally {
		}
	}

	private void populateSchemaBox() {
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet resultSet = metaData.getSchemas();
			ArrayList<String> values = new ArrayList<String>();
			while (resultSet.next()) {
				values.add(resultSet.getString(1));
			}
			resultSet.close();
			schemaBox.setModel(new DefaultComboBoxModel<Object>(values.toArray()));
			schemaBox.setEnabled(values.size() > 0);
		} catch (Exception e) {
			schemaBox.setEnabled(false);
		}
	}

	private void populateTableBox() {
		try {
			String[] types = { "TABLE" };
			String catalog = connection.getCatalog();
			String schema = (String) (schemaBox.getSelectedItem());
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet resultSet = metaData.getTables(catalog, schema, null, types);
			ArrayList<String> values = new ArrayList<String>();
			while (resultSet.next()) {
				values.add(resultSet.getString(3));
			}
			resultSet.close();
			tableBox.setModel(new DefaultComboBoxModel<Object>(values.toArray()));
			tableBox.setEnabled(values.size() > 0);
		} catch (Exception e) {
			tableBox.setEnabled(false);
		} finally {
		}
	}

	private JPanel getFrameButtonPanel() {
		JPanel panel = new JPanel();
		JButton button = new JButton("Exit");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		panel.add(button);
		return panel;
	}

	private void refreshTable() {
		String schema = (schemaBox.isEnabled() ? schemaBox.getSelectedItem().toString() : null);
		String tableName = (String) tableBox.getSelectedItem();
		if (tableName == null) {
			table.setModel(new DefaultTableModel());
			return;
		}
		String selectTable = (schema == null ? "" : schema + ".") + tableName;
		if (selectTable.indexOf(' ') > 0) {
			selectTable = "\"" + selectTable + "\"";
		}
		try {
			Statement stmt = connection.createStatement();
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + selectTable);
			table.setModel((TableModel) new ResultSetTableModel(resultSet));
		} catch (Exception e) {
		}
		;
	}

	class ConnectionDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3082771732275132912L;
		private JTextField useridField;
		private JTextField passwordField;
		private JTextField urlField;

		//private boolean canceled;
		private Connection connect;

		public ConnectionDialog(JFrame f) {
			super(f, "Connect To Database", true);
			buildDialogLayout();
			setSize(300, 200);
		}

		public Connection getConnection() {
			setVisible(true);
			return connect;
		}

		private void buildDialogLayout() {
			JLabel label;

			Container pane = getContentPane();
			pane.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.WEST;
			constraints.insets = new Insets(5, 10, 5, 10);

			constraints.gridx = 0;
			constraints.gridy = 0;
			label = new JLabel("Userid:", JLabel.LEFT);
			pane.add(label, constraints);

			constraints.gridy++;
			label = new JLabel("Password:", JLabel.LEFT);
			pane.add(label, constraints);

			constraints.gridy++;
			label = new JLabel("URL:", JLabel.LEFT);
			pane.add(label, constraints);
			constraints.gridx = 1;
			constraints.gridy = 0;

			useridField = new JTextField(10);
			pane.add(useridField, constraints);

			constraints.gridy++;
			passwordField = new JTextField(10);
			pane.add(passwordField, constraints);

			constraints.gridy++;
			urlField = new JTextField(15);
			pane.add(urlField, constraints);

			constraints.gridx = 0;
			constraints.gridy = 3;
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			constraints.anchor = GridBagConstraints.CENTER;
			pane.add(getButtonPanel(), constraints);
		}

		private JPanel getButtonPanel() {
			JPanel panel = new JPanel();
			JButton btn = new JButton("Ok");
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					onDialogOk();
				}
			});
			panel.add(btn);
			btn = new JButton("Cancel");
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					onDialogCancel();
				}
			});
			panel.add(btn);
			return panel;
		}

		private void onDialogOk() {
			if (attemptConnection()) {
				setVisible(false);
			}
		}

		private void onDialogCancel() {
			System.exit(0);
		}

		private boolean attemptConnection() {
			try {
				connect = DriverManager.getConnection(urlField.getText(), useridField.getText(),
						passwordField.getText());
				return true;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error connecting to " + "database: " + e.getMessage());
			}
			return false;
		}

	}

	class ResultSetTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4000994414116367598L;
		private ArrayList<String> columnHeaders;
		private ArrayList<ArrayList<Object>> tableData;

		public ResultSetTableModel(ResultSet resultSet) throws SQLException {
			ArrayList<Object> rowData;
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int count = rsmd.getColumnCount();
			columnHeaders = new ArrayList<String>(count);
			tableData = new ArrayList<ArrayList<Object>>();
			for (int i = 1; i <= count; i++) {
				columnHeaders.add(rsmd.getColumnName(i));
			}
			while (resultSet.next()) {
				rowData = new ArrayList<Object>(count);
				for (int i = 1; i <= count; i++) {
					rowData.add(resultSet.getObject(i));
				}
				tableData.add(rowData);
			}
		}

		public int getColumnCount() {
			return columnHeaders.size();
		}

		public int getRowCount() {
			return tableData.size();
		}

		public Object getValueAt(int row, int column) {
			ArrayList<?> rowData = (tableData.get(row));
			return ((ArrayList<?>) rowData).get(column);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

		public String getColumnName(int column) {
			return (columnHeaders.get(column));
		}

	}
}

