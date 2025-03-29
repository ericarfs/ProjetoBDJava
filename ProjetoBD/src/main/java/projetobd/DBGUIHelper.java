package projetobd;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Érica Ribeiro
 */
public class DBGUIHelper {
    public int nColunas;
    public List<String>  columns;
    public Map<String, String>  columnsType;        
    
    public List<String> obterValoresConsulta(JPanel painel){
        Component[] components = painel.getComponents();
        
        List valores = new ArrayList<>();
        String valor;
        for (Component component : components) {
            if (component.getClass().equals(JComboBox.class)){
                valor = (String) (((JComboBox) component).getSelectedItem()); 
                valores.add(valor);
            } 
        }
        
        return valores;
    }
    
    public List<String> obterValoresInsercao(JPanel painel){
        Component[] components = painel.getComponents();
        
        List valores = new ArrayList<>();
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
        return valores;
    }
    
    public void construirPainelDeConsulta(JPanel painel, Map<String, List<String>> list){
        painel.removeAll();
         
        String selecao[];
        painel.setLayout(new BoxLayout(painel, BoxLayout.PAGE_AXIS));
        
        painel.add(Box.createVerticalStrut(20));
        JLabel titulo = new JLabel("CONSULTAR DADOS");
        painel.add(titulo);
        titulo.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        painel.add(Box.createVerticalStrut(20));
        
        for (String coluna : list.keySet()) {
            JLabel label = new JLabel(coluna);
            painel.add(label);
            label.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            painel.add(Box.createVerticalStrut(10));
           
            selecao = list.get(coluna).toArray (String[]::new);
           
            JComboBox campo = new JComboBox(selecao); 
            
            painel.add(campo);
    
            painel.add(Box.createVerticalStrut(10));
        }
    
    }
    
    public void construirPainelDeInsercao(JPanel painel){
        painel.removeAll();
        
        painel.setLayout(new GridLayout(nColunas, 2));
        
        for (int i = 0; i < columns.size(); i++) {
            String coluna = columns.get(i);
            JLabel label = new JLabel(coluna);
            painel.add(label);
            
            if (columnsType.get(coluna).equals("uuid")) {
                JComboBox campo = new JComboBox();
                campo.addItem("uuid()");
                
                painel.add(campo);
            }
            else{
                JTextField campo = new JTextField(); 
                painel.add(campo);
            } 
            
        }
    }
}
