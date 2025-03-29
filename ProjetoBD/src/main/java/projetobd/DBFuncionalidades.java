package projetobd;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author junio
 */
public class DBFuncionalidades {

    private static CqlSession session;
    private ResultSet res;
    private final JTextArea jtAreaDeStatus;
    private int nColunas;
    private long nTuplas;
    private static List<String> columns;
    private static Map<String, String> columnsType;
    private List<String> tables;
    private static List<Integer> pkColumns;
    private static Map<String, String> pColumns;
    private static Map<String, String> cColumns;
    private List<String> ckColumns;
    private Map<String, String> ckColumnValues;
    private List<String> fkColumns;
    private Map<String, String> fkParentTables;
    private Map<String, String> fkParentColumns;
    private ArrayList<HashMap<String, Object>> dadosDicionario;
    private String query;
    private Object dados[][];

    private List<String> valores;

    private JTable jt;
    private ArrayTableModel model;

    public final DBGUIHelper view;

    public DBFuncionalidades(JTextArea jtaTextArea) {
        jtAreaDeStatus = jtaTextArea;
        view = new DBGUIHelper();
    }

    public boolean conectar() {
        try {
            session = CassandraConnection.getSession();
            System.out.println("Conectado ao Cassandra com sucesso!");
            return true;
        } catch (Exception ex) {
            jtAreaDeStatus.setText("Problema: verifique sua conexão!");
        }
        return false;
    }

    public void pegarNomesDeTabelas(JComboBox jc) throws IOException {
        tables = new ArrayList<>();
        query = "SELECT table_name FROM system_schema.tables WHERE keyspace_name = 'universidade'";
        try {
            res = session.execute(query);
            for (Row row : res) {
                String tableName = row.getString("table_name");
                jc.addItem(tableName);
                tables.add(tableName);
            }
        } catch (Exception e) {
            jtAreaDeStatus.setText("Erro na consulta: \"" + query + "\" \n" + e.getMessage());
        }
    }

    public void preencheDadosDeTabela(String sTableName) {
        String text = "";
        columns = new ArrayList<>();
        columnsType = new HashMap<>();
        pColumns = new HashMap<>();
        cColumns = new HashMap<>();

        query = "SELECT column_name, kind, type FROM system_schema.columns \n" +
                "WHERE keyspace_name = 'universidade' AND table_name = '" + sTableName + "'";

        nColunas = 0;
        try {
            res = session.execute(query);
            for (Row row : res) {
                String columnName = row.getString("column_name");
                String kind = row.getString("kind");
                String type = row.getString("type");

                text += columnName.toUpperCase() + ".  " + kind.toUpperCase() + "  " + type.toUpperCase() + "\n";

                columns.add(columnName);
                columnsType.put(columnName, type);

                if ("partition_key".equals(kind)) {
                    pColumns.put(columnName, type);
                }
                if ("clustering".equals(kind)) {
                    cColumns.put(columnName, type);
                }

                nColunas += 1;
            }
            jtAreaDeStatus.setText(text);
            view.nColunas = nColunas;
            view.columns = columns;
            view.columnsType = columnsType;
        } catch (Exception e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
    }

    public void selecionarDados(String query) {
        int i = 0;
        try {
            res = session.execute(query);
            for (Row row : res) {
                HashMap<String, Object> dadosRow = new HashMap<>();
                for (int j = 0; j < nColunas; j++) {
                    String columnName = columns.get(j);
                    Object obj = row.getObject(columnName);

                    String columnValue = obj != null ? obj.toString() : "";

                    dados[i][j] = columnValue;
                    dadosRow.put(columnName, columnValue);

                }
                dadosDicionario.add(dadosRow);
                i++;
            }
        } catch (Exception e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
    }

    public void contarDados(String sTableName) {
        query = "SELECT COUNT(*) AS CONT FROM universidade." + sTableName;
        try {
            res = session.execute(query);
            nTuplas = res.one().getLong(0);
        } catch (NumberFormatException e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
    }

    public JTable exibeDados(String sTableName) {
        contarDados(sTableName);

        dados = new String[(int) nTuplas][nColunas];
        dadosDicionario = new ArrayList<>();

        query = "SELECT * FROM universidade." + sTableName;

        selecionarDados(query);

        model = new ArrayTableModel(dados, columns.toArray(String[]::new));
        jt = new JTable(model);

        TableCellRenderer renderer = new CustomRenderer();
        for (String column : pColumns.keySet()) {
            jt.getColumnModel().getColumn(columns.indexOf(column)).setCellRenderer(renderer);
        }
        return jt;
    }

    public Map<String, List<String>> pkOptions(String parentTable, String pkColumns, JTextArea jtAreaDeStatus) {
        Map<String, List<String>> mapa = new HashMap<>();
        Set<String> valoresRef;

        query = "SELECT DISTINCT " + pkColumns + " FROM " + parentTable;
        try {
            for (String column : pColumns.keySet()) {
                res = session.execute(query);
                valoresRef = new HashSet<>();
                for (Row row : res) {
                    valoresRef.add((row.getObject(column)).toString());
                }
                List<String> valoresOp = new ArrayList<>();
                valoresOp.addAll(valoresRef);
                mapa.put(column, valoresOp);
            }

        } catch (Exception e) {
            jtAreaDeStatus.setText(e.getMessage());
        }
        return mapa;
    }

    public void inserirPainelBusca(JPanel pPainel, String sTableName) {
        String partitionKs = String.join(",", pColumns.keySet());

        Map<String, List<String>> list = pkOptions(sTableName, partitionKs, jtAreaDeStatus);

        view.construirPainelDeConsulta(pPainel, list);
    }

    public void consultarDados(JPanel painel, String sTableName) {
        valores = view.obterValoresConsulta(painel);

        dados = new Object[(int) nTuplas][nColunas];
        dadosDicionario = new ArrayList<>();

        //Construir a query
        StringBuilder sql = new StringBuilder("SELECT * FROM universidade." + sTableName + " WHERE ");

        int k = 0;
        for (String coluna : pColumns.keySet()) {
            sql.append(coluna).append(" = ");

            String valorColuna = valores.get(k++);

            if (pColumns.get(coluna).equals("text")) {
                valorColuna = "'" + valorColuna + "'";
            }

            sql.append(valorColuna);

            if (k < pColumns.size()) {
                sql.append(" AND ");
            }
        }

        query = sql.toString();

        selecionarDados(query);

        model.setData(dados);
    }

    public void inserirDados(JPanel painel, String sTableName) {
        valores = view.obterValoresInsercao(painel);

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
                if (columnsType.get(columns.get(i)).equals("text")) {
                    sql.append("'").append(valores.get(i)).append("'");
                } else {
                    sql.append(valores.get(i));
                }
            }
            if (i < valores.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");

        // Tente executar o comando
        try {
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
        for (int row : rows) {
            updateQuery = "UPDATE " + sTableName + " SET ";
            for (int i = 0; i < columns.size(); i++) {
                if (columnsType.get(jt.getColumnName(i)).equals("uuid") == false
                        && pColumns.containsKey(jt.getColumnName(i)) == false
                        && cColumns.containsKey(jt.getColumnName(i)) == false) {
                    updateQuery += jt.getColumnName(i) + " = ";
                    if (columnsType.get(jt.getColumnName(i)).equals("text")
                            || columnsType.get(jt.getColumnName(i)).equals("timestamp")) {
                        updateQuery += "'" + jt.getValueAt(row, i) + "'";
                    } else {
                        updateQuery += jt.getValueAt(row, i);
                    }

                    if ((i < columns.size() - 1)) {
                        updateQuery += ", ";
                    }
                }
            }
            updateQuery = updateQuery.replaceAll(", $", "");
            updateQuery += " WHERE ";
            for (int i = 0; i < columns.size(); i++) {
                if (pColumns.containsKey(jt.getColumnName(i)) || cColumns.containsKey(jt.getColumnName(i))) {
                    updateQuery += jt.getColumnName(i) + " = ";
                    if (columnsType.get(jt.getColumnName(i)).equals("text")) {
                        updateQuery += "'" + dadosDicionario.get(row).get(jt.getColumnName(i)) + "'";
                    } else {
                        updateQuery += dadosDicionario.get(row).get(jt.getColumnName(i));
                    }

                    if ((i < columns.size() - 1)) {
                        updateQuery += " AND ";
                    }

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
        for (int row : rows) {
            deleteQuery = "DELETE FROM " + sTableName + " WHERE ";
            for (int i = 0; i < columns.size(); i++) {
                if (pColumns.containsKey(jt.getColumnName(i)) || cColumns.containsKey(jt.getColumnName(i))) {
                    deleteQuery += jt.getColumnName(i) + " = ";

                    Object valorColuna = jt.getValueAt(row, i);

                    if (columnsType.get(jt.getColumnName(i)).equals("text")) {
                        valorColuna = "'" + valorColuna + "'";
                    }

                    deleteQuery += valorColuna;

                    if ((i < columns.size() - 1)) {
                        deleteQuery += " AND ";
                    }

                }

            }

            deleteQuery = deleteQuery.replaceAll(" AND $", "");

            try {
                res = session.execute(deleteQuery);
                jtAreaDeStatus.setText("Dados deletados com sucesso!");
            } catch (Exception e) {
                jtAreaDeStatus.setText(e.getMessage());
            }
        }

        nTuplas = nTuplas - rows.length;
        Object nDados[][] = new Object[(int) nTuplas][nColunas];
        int j = 0;
        for (int i = 0; i < dados.length; i++) {
            final int k = i;
            boolean contains = IntStream.of(rows).anyMatch(x -> x == k);
            if (contains == false) {
                System.arraycopy(dados[i], 0, nDados[j], 0, columns.size());
                j++;
            }
        }

        dados = nDados;

        model.setData(dados);
    }

    public void salvarDados(String sTableName) throws FileNotFoundException, IOException {
        String filename = sTableName + ".csv";
        FileOutputStream file = new FileOutputStream(filename);

        try (OutputStreamWriter writer = new OutputStreamWriter(file, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < columns.size(); i++) {
                String element = columns.get(i);
                sb.append(element);
                
                if (i < columns.size() - 1) {
                    sb.append(';');
                } else {
                    sb.append('\n');
                }
            }

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Object[] dado : dados) {
                for (int j = 0; j < columns.size(); j++) {
                    if (columnsType.get(columns.get(j)).equals("timestamp")) {
                        // Formata a data
                        String data = dado[j].toString().substring(0, 10);
                        LocalDate localDate = LocalDate.parse(data, formato);
                        sb.append(localDate);
                    } else {
                        sb.append(dado[j]);
                    }

                    if (j < columns.size() - 1) {
                        sb.append(';');
                    } else {
                        sb.append('\n');
                    }
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
}
