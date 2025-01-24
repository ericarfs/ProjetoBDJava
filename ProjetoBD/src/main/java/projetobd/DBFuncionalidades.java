package projetobd;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
/**
 *
 * @author junio
 */
public class DBFuncionalidades {
    private static CqlSession session;
    ResultSet res;
    JTextArea jtAreaDeStatus;
    int nColunas;
    long nTuplas;
    static List<String>  columns;
    static Map<String, String>  columnsType;        
    List<String> tables;
    static List<Integer> pkColumns;  
    static Map<String, String>  pColumns;    
    static Map<String, String>  cColumns;
    List<String> ckColumns;  
    Map<String, String> ckColumnValues; 
    List<String> fkColumns;
    Map<String, String> fkParentTables;   
    Map<String, String> fkParentColumns;
    ArrayList<HashMap<String, Object>> dadosDicionario;
    
    Object dados[][];
                    
    List<String> valores;
    
    JTable jt;
    ArrayTableModel model;
    
    public DBFuncionalidades(JTextArea jtaTextArea){
        jtAreaDeStatus = jtaTextArea;
    }
    
    public boolean conectar() {
        try {
            // Estabelecendo a conexão com o Cassandra local
            session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                    .withKeyspace(CqlIdentifier.fromCql("universidade")) 
                    .withLocalDatacenter("datacenter1") 
                    .build();
            
            System.out.println("Conectado ao Cassandra com sucesso!");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            jtAreaDeStatus.setText("Problema: verifique sua conexão!");
        }
        return false;
    }
    
    public void pegarNomesDeTabelas(JComboBox jc) throws IOException{
        tables = new ArrayList<>();
        String query = "";
        try {
            query = "SELECT table_name FROM system_schema.tables WHERE keyspace_name = 'universidade'";
            res = session.execute(query);
            for (Row row : res) {
                jc.addItem(row.getString("table_name"));
                tables.add(row.getString("table_name"));
            }
        } catch (Exception ex) {
            jtAreaDeStatus.setText("Erro na consulta: \"" + query + "\"");
        }        
    }
    
    
    public void preencheDadosDeTabela(String sTableName){
        String text = "";
        columns = new ArrayList<>();
        columnsType = new HashMap<>();       
        pColumns = new HashMap<>();
        cColumns = new HashMap<>();
        
        nColunas = 0;
        try {
            res = session.execute("""
                                   SELECT column_name, kind, type FROM system_schema.columns
                                   WHERE keyspace_name = 'universidade' AND table_name = '""" + sTableName + "'");
            for (Row row : res) {
                text += row.getString("column_name") + ". " 
                      + row.getString("kind") + "  "
                      + row.getString("type") + "\n";
                nColunas += 1;          
                columns.add(row.getString("column_name"));
                columnsType.put(row.getString("column_name"), row.getString("type"));

                if (row.getString("kind").equals("partition_key"))
                    pColumns.put(row.getString("column_name"), row.getString("type"));
                if (row.getString("kind").equals("clustering"))
                    cColumns.put(row.getString("column_name"), row.getString("type"));
            }
            jtAreaDeStatus.setText(text);
        }
        catch (Exception e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
    }
    
       
    static class CustomRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            com.setBackground(Color.YELLOW);

            // Verifica se é a coluna que queremos colorir
            if (pColumns.get(columns.get(column)) != null) {
                cell.setBackground(Color.YELLOW); 
                cell.setFont(new Font("Dialog", Font.BOLD, 14));
            } 
            return cell;
        }
    }
    
    public void selecionarDados(String query){
        int i = 0;
        try {
            res = session.execute(query);
            for (Row row : res) {
                HashMap<String, Object> dadosRow = new HashMap<>();
                for(int j=0; j < nColunas; j++){
                    Object obj = row.getObject(columns.get(j));
                    if (obj == null){
                        dados[i][j] = "";
                        dadosRow.put(columns.get(j), "");
                    }
                    else{
                        String value = (row.getObject(columns.get(j))).toString();
                        dados[i][j] = value;
                        dadosRow.put(columns.get(j), value);
                    }
                }
                dadosDicionario.add(dadosRow);
                i++;
            }
        } catch (Exception e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
    }
    
    
    public JTable exibeDados(String sTableName){
        try {
            res = session.execute("SELECT COUNT(*) AS CONT FROM universidade."+sTableName);
            for (Row row : res) {
                nTuplas = row.getLong(0);
            }
        }
        catch (NumberFormatException e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
        
        dados = new String[(int) nTuplas][nColunas];
        dadosDicionario = new ArrayList<>();
         
        String query = "SELECT * FROM universidade."+sTableName;
        
        selecionarDados(query);
        
        model = new ArrayTableModel(dados, columns.toArray(String[]::new));
        jt = new JTable(model);
        
        TableCellRenderer renderer = new CustomRenderer();
        for (String column : pColumns.keySet()) {
            jt.getColumnModel().getColumn(columns.indexOf(column)).setCellRenderer(renderer);
        }
        return jt;
    }
    
    
    public Map<String, List<String>> pkOptions(String parentTable, String pkColumns, JTextArea jtAreaDeStatus){
        Map<String, List<String>> mapa = new HashMap<>();
        Set<String> valoresRef;
        
        try {
            for (String column: pColumns.keySet()){
                res = session.execute("SELECT DISTINCT "+pkColumns+" FROM "+parentTable);
                valoresRef = new HashSet<>(); 
                for(Row row: res){
                    valoresRef.add((row.getObject(column)).toString());
                }
                List<String> valoresOp =  new ArrayList<>();
                valoresOp.addAll(valoresRef);
                mapa.put(column, valoresOp);
            }
            
        }
        catch (Exception e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
        return mapa;
    }
    
    
    public void inserePainelBusca(JPanel pPainel, String sTableName){       
        pPainel.removeAll();
         
        String selecao[];
        pPainel.setLayout(new BoxLayout(pPainel, BoxLayout.PAGE_AXIS));
        
        String partitionKs = String.join(",", pColumns.keySet());
        
        Map<String, List<String>> list = pkOptions(sTableName, partitionKs, jtAreaDeStatus);
        
        pPainel.add(Box.createVerticalStrut(20));
        JLabel titulo = new JLabel("CONSULTAR DADOS");
        pPainel.add(titulo);
        titulo.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        pPainel.add(Box.createVerticalStrut(20));
        
        for (String coluna : list.keySet()) {
            JLabel label = new JLabel(coluna);
            pPainel.add(label);
            label.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            pPainel.add(Box.createVerticalStrut(10));
           
            selecao = list.get(coluna).toArray (String[]::new);
           
            JComboBox campo = new JComboBox(selecao); 
            
            pPainel.add(campo);
    
            pPainel.add(Box.createVerticalStrut(10));
        }
         
    }
    
    
    public void consultarDados(JPanel painel, String sTableName){
        Component[] components = painel.getComponents();
        
        valores = new ArrayList<>();
        String valor;
        for (Component component : components) {
            if (component.getClass().equals(JComboBox.class)){
                valor = (String) (((JComboBox) component).getSelectedItem()); 
                valores.add(valor);
            } 
        }
        
        dados = new Object[(int) nTuplas][nColunas];
        dadosDicionario = new ArrayList<>();
        
        //Construir a query
        String query = "SELECT * FROM universidade."+sTableName + " WHERE ";
        
        int k = 0;
        for (String coluna: pColumns.keySet()){
            query += coluna + " = ";
            if (pColumns.get(coluna).equals("text"))    
                query += "'" + valores.get(k++) +"'";
            else    
                query += valores.get(k++);

            if (k < pColumns.size())    
                query += " AND ";
        }
        
        selecionarDados(query);
        
        model.setData(dados);
    }

    public void inserirPainel(JPanel pPainelDeInsecaoDeDados, String sTableName){        
        pPainelDeInsecaoDeDados.removeAll();
        
        pPainelDeInsecaoDeDados.setLayout(new GridLayout(nColunas, 2));
        
        for (int i = 0; i < columns.size(); i++) {
            String coluna = columns.get(i);
            JLabel label = new JLabel(coluna);
            pPainelDeInsecaoDeDados.add(label);
            
            if (columnsType.get(coluna).equals("uuid")) {
                JComboBox campo = new JComboBox();
                campo.addItem("uuid()");
                
                pPainelDeInsecaoDeDados.add(campo);
            }
            else{
                JTextField campo = new JTextField(); 
                pPainelDeInsecaoDeDados.add(campo);
            } 
            
        }
    }
        


    public void inserirDados(JPanel painel, String sTableName) {
        Component[] components = painel.getComponents();
        
        valores = new ArrayList<>();
        String valor;
        for (Component component : components) {
            if (component.getClass().equals(JTextField.class)) {
                valor = ((JTextField) component).getText();
                valores.add(valor);
            }
            else if (component.getClass().equals(JComboBox.class)){
                valor = (String) (((JComboBox) component).getSelectedItem()); 
                valores.add(valor);
            } 
        }
        
        // Construa o comando SQL INSERT
        StringBuilder sql = new StringBuilder("INSERT INTO " + sTableName + " (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i));
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(") VALUES (");

        for (int i = 0; i < valores.size(); i++) {
            if (columns.get(i).toUpperCase().contains("DATE")) {
                sql.append("TO_DATE(?, 'YYYY-MM-DD')");
            } else {
                if (columnsType.get(columns.get(i)).equals("text")){
                    sql.append("'").append(valores.get(i)).append("'");
                }
                else sql.append(valores.get(i));
            }
            if (i < valores.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");

        // Tente executar o comando
        try{
            session.execute(sql.toString());
            jtAreaDeStatus.setText("Dados inseridos com sucesso na tabela " + sTableName);
        } catch (Exception e) {
            jtAreaDeStatus.setText("Erro ao inserir dados: " + e.getMessage());
        }
    } 
    
    
    public void atualizarDados(String sTableName) {
        int rows[] = jt.getSelectedRows();
        System.out.println(Arrays.toString(rows));
        
        String updateQuery;
        for(int row: rows){
            updateQuery = "UPDATE " + sTableName + " SET ";
            for (int i = 0; i < columns.size(); i++){
                if (columnsType.get(jt.getColumnName(i)).equals("uuid") == false && 
                    pColumns.containsKey(jt.getColumnName(i)) == false &&
                    cColumns.containsKey(jt.getColumnName(i)) == false) {
                    updateQuery += jt.getColumnName(i) + " = ";
                    if (columnsType.get(jt.getColumnName(i)).equals("text") ||
                        columnsType.get(jt.getColumnName(i)).equals("timestamp"))    
                        updateQuery += "'" + jt.getValueAt(row, i) +"'";
                    else    
                        updateQuery += jt.getValueAt(row, i);
                    
                    if ((i < columns.size() - 1))    
                        updateQuery += ", ";
                }
            }
            updateQuery = updateQuery.replaceAll(", $", "");
            updateQuery += " WHERE ";
            for (int i = 0; i < columns.size(); i++){
                if(pColumns.containsKey(jt.getColumnName(i)) || cColumns.containsKey(jt.getColumnName(i))){
                    updateQuery += jt.getColumnName(i) + " = ";
                    if (columnsType.get(jt.getColumnName(i)).equals("text"))    
                        updateQuery += "'" + dadosDicionario.get(row).get(jt.getColumnName(i)) +"'";
                    else    
                        updateQuery += dadosDicionario.get(row).get(jt.getColumnName(i));
                    
                     if ((i < columns.size() - 1))    
                        updateQuery += " AND ";
                    
                }
    
            }  
            
            updateQuery = updateQuery.replaceAll(" AND $", "");
         
            try {
                res = session.execute(updateQuery);
                jtAreaDeStatus.setText("Dados atualizados com sucesso!");
            } catch (Exception e) {
                jtAreaDeStatus.setText(e.getMessage());
            }
            
        }
    }
    
    
    public void deletarDados(String sTableName) {
        int rows[] = jt.getSelectedRows();
        System.out.println(Arrays.toString(rows));
        
        String deleteQuery;
        for(int row: rows){
            deleteQuery = "DELETE FROM " + sTableName + " WHERE ";
            for (int i = 0; i < columns.size(); i++){
                if(pColumns.containsKey(jt.getColumnName(i)) || cColumns.containsKey(jt.getColumnName(i))){
                    deleteQuery += jt.getColumnName(i) + " = ";
                    if (columnsType.get(jt.getColumnName(i)).equals("text"))    
                        deleteQuery += "'" + jt.getValueAt(row, i) +"'";
                    else    
                        deleteQuery += jt.getValueAt(row, i);
                    
                     if ((i < columns.size() - 1))    
                        deleteQuery += " AND ";
                    
                }
    
            }  
            
            deleteQuery = deleteQuery.replaceAll(" AND $", "");
            

            try {
                res = session.execute(deleteQuery);
                jtAreaDeStatus.setText("Dados deletados com sucesso!");
            } catch (Exception e) {
                jtAreaDeStatus.setText(e.getMessage());
            }
            
            nTuplas = nTuplas - rows.length;
            Object nDados[][] = new Object[(int) nTuplas][nColunas];
            int j = 0;
            for (int i = 0; i < dados.length; i++){
                final int k = i;
                boolean contains = IntStream.of(rows).anyMatch(x -> x == k);
                if(contains == false){
                    System.arraycopy(dados[i], 0, nDados[j], 0, columns.size());
                    j++;
                }
                
            }
            
            dados = nDados;
            
            model.setData(dados);
        }
    }
    
    
    public void salvarDados(String sTableName) throws FileNotFoundException, IOException{
        String filename = sTableName + ".csv";
        FileOutputStream file = new FileOutputStream(filename);
        try (OutputStreamWriter writer = new OutputStreamWriter(file, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < columns.size(); i++) {
                String element = columns.get(i);
                sb.append(element);
                if (i < columns.size() - 1) sb.append(';');
                else sb.append('\n');
            }
            
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Object[] dado : dados) {
                for (int j = 0; j < columns.size(); j++) {
                    if (columnsType.get(columns.get(j)).equals("timestamp")){
                        // Formata a data
                        String data = dado[j].toString().substring(0,10);
                        LocalDate localDate = LocalDate.parse(data, formato);
                        sb.append(localDate);
                    }
                    else{
                        sb.append(dado[j]);
                    }
                   
                    if (j < columns.size() - 1) sb.append(';');
                    else sb.append('\n');
                }
            }

            writer.write("\uFEFF"); 
            writer.write(sb.toString());
            writer.close();
            
            jtAreaDeStatus.setText("Dados exportados com sucesso!");
        } catch (FileNotFoundException e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
    }
    
    
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
   
    
}
