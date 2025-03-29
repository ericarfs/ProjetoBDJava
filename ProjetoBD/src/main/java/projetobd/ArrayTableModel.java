package projetobd;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Érica Ribeiro
 */
// Modelo de tabela baseado em arrays
class ArrayTableModel extends AbstractTableModel {
    private Object[][] data;
    private final String[] columnNames;

    public ArrayTableModel(Object[][] data, String[] columnNames) {
        this.data = data;
        this.columnNames = columnNames;
    }

    // Atualiza os dados da tabela e notifica a JTable
    public void setData(Object[][] data) {
        this.data = data;
        fireTableDataChanged();  // Notifica a JTable que os dados mudaram
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;  // Permite editar as células diretamente
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        data[row][column] = value;
        fireTableCellUpdated(row, column);  // Notifica que uma célula foi atualizada
    }
}
