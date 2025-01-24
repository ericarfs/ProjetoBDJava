package projetobd;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;


/**
 *
 * @author junio
 */
public class JanelaPrincipal {
    JButton exportaBotao;
    JButton insereBotao;    
    JButton consultaBotao;
    JButton deleteBotao;
    JButton updateBotao;
    JFrame j;
    JPanel pPainelDeCima;
    JPanel pPainelDeBaixo;
    JComboBox jc;
    JTextArea jtAreaDeStatus;
    JTabbedPane tabbedPane;
    JPanel pPainelDeExibicaoDeDados;
    JTable jt;
    JScrollPane jsp;
    JPanel pPainelDeInsecaoDeDados;
    DBFuncionalidades bd;
    JPanel pPainelDoLado;
    JPanel pPainelDeConsultaDeDados;
    JPanel pPainelDeAcoes;

    public void ExibeJanelaPrincipal() throws IOException {
        /*Janela*/
        j = new JFrame("ICMC-USP - SCC0641 - Projeto - Apache Cassandra");
        j.setSize(960, 600);
        j.setLayout(new BorderLayout());
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*Painel da parte superior (north) - com combobox e outras informações*/
        pPainelDeCima = new JPanel();
        j.add(pPainelDeCima, BorderLayout.NORTH);
        jc = new JComboBox();
        pPainelDeCima.add(jc);

        
        /*Painel da parte inferior (south) - com área de status*/
        pPainelDeBaixo = new JPanel();
        j.add(pPainelDeBaixo, BorderLayout.SOUTH);
        jtAreaDeStatus = new JTextArea();
        jtAreaDeStatus.setText("Aqui é sua área de status");
        pPainelDeBaixo.add(jtAreaDeStatus);

        /*Painel do lado (east)*/
        pPainelDoLado = new JPanel();
        j.add(pPainelDoLado, BorderLayout.EAST);
        
        /*Painel tabulado na parte central (CENTER)*/
        tabbedPane = new JTabbedPane();
        j.add(tabbedPane, BorderLayout.CENTER);
        
        /*Tab de exibicao*/
        pPainelDeExibicaoDeDados = new JPanel();
        pPainelDeExibicaoDeDados.setLayout(new BoxLayout(pPainelDeExibicaoDeDados, BoxLayout.PAGE_AXIS));
        tabbedPane.add(pPainelDeExibicaoDeDados, "Exibição");


        /*Tab de inserção*/
        pPainelDeInsecaoDeDados = new JPanel();
        pPainelDeInsecaoDeDados.setLayout(new GridLayout(1, 1));
        tabbedPane.add(pPainelDeInsecaoDeDados, "Inserção");
        
        /*Painel de consulta*/
        pPainelDeConsultaDeDados = new JPanel();
        pPainelDoLado.add(pPainelDeConsultaDeDados);
        
        /*Painel de ações*/
        pPainelDeAcoes = new JPanel();
        pPainelDeAcoes.setLayout(new BoxLayout(pPainelDeAcoes, BoxLayout.LINE_AXIS));
                
        j.setVisible(true);
        
        /*Botão para exportação*/
        exportaBotao = new JButton("Exporta CSV MSExcel");
        exportaBotao.setVisible(true);
        pPainelDeCima.add(exportaBotao);
        
        /*Botão para inserção*/
        insereBotao = new JButton("Inserir Dados");
        insereBotao.setVisible(false);
        pPainelDeCima.add(insereBotao);
        
        /*Botão para consulta*/
        consultaBotao = new JButton("Buscar");
        consultaBotao.setVisible(true);
        pPainelDeAcoes.add(consultaBotao);
        pPainelDeAcoes.add(Box.createHorizontalStrut(20));
        
        /*Botão para atualização*/
        updateBotao = new JButton("Atualizar");
        updateBotao.setVisible(true);
        pPainelDeAcoes.add(updateBotao);
        pPainelDeAcoes.add(Box.createHorizontalStrut(20));

        /*Botão para deleção*/
        deleteBotao = new JButton("Deletar");
        deleteBotao.setVisible(true);
        pPainelDeAcoes.add(deleteBotao);
        pPainelDeAcoes.add(Box.createHorizontalStrut(20));    

               
        // Adicionando um ChangeListener ao JTabbedPane
        tabbedPane.addChangeListener((ChangeEvent e) -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            
            if (selectedIndex == 1) {
                exportaBotao.setVisible(false);
                consultaBotao.setVisible(false);
                insereBotao.setVisible(true);
                pPainelDeConsultaDeDados.setVisible(false);
            }
            if (selectedIndex == 0) {
                exportaBotao.setVisible(true);
                consultaBotao.setVisible(true);
                insereBotao.setVisible(false);
                pPainelDeConsultaDeDados.setVisible(true);

            }
        });
        
        bd = new DBFuncionalidades(jtAreaDeStatus);
        if (bd.conectar())
            bd.pegarNomesDeTabelas(jc);
        
        this.DefineEventos();        
    }

    private void DefineEventos() {
        jc.addActionListener((ActionEvent e) -> {
            
            JComboBox jcTemp = (JComboBox) e.getSource();
            bd.preencheDadosDeTabela((String) jcTemp.getSelectedItem());
            
            pPainelDeExibicaoDeDados.removeAll();
            jt = bd.exibeDados((String) jcTemp.getSelectedItem());
            
            jsp = new JScrollPane(jt);
            pPainelDeExibicaoDeDados.add(jsp);
            pPainelDeExibicaoDeDados.add(pPainelDeAcoes);

            pPainelDeConsultaDeDados.removeAll();
            bd.inserePainelBusca(pPainelDeConsultaDeDados, (String) jcTemp.getSelectedItem());
            consultaBotao.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            
            pPainelDeInsecaoDeDados.removeAll();

            bd.inserirPainel(pPainelDeInsecaoDeDados, (String) jcTemp.getSelectedItem());

            pPainelDeInsecaoDeDados.revalidate();
            pPainelDeInsecaoDeDados.repaint();
           
            for (ActionListener listener: exportaBotao.getActionListeners())
                exportaBotao.removeActionListener(listener);
            
            exportaBotao.addActionListener((ActionEvent event) -> {
                try {
                    bd.salvarDados((String) jcTemp.getSelectedItem());
                } catch (IOException ex) {
                    Logger.getLogger(JanelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
            
            for (ActionListener listener: insereBotao.getActionListeners())
                insereBotao.removeActionListener(listener);
            
            insereBotao.addActionListener((ActionEvent e1) -> {
                bd.inserirDados(pPainelDeInsecaoDeDados, (String) jcTemp.getSelectedItem());
            });
            
            for (ActionListener listener: consultaBotao.getActionListeners())
                consultaBotao.removeActionListener(listener);
            
            consultaBotao.addActionListener((ActionEvent e2) -> {
                bd.consultarDados(pPainelDeConsultaDeDados, (String) jcTemp.getSelectedItem());   
            });
            
            for (ActionListener listener: updateBotao.getActionListeners())
                updateBotao.removeActionListener(listener);
            
            updateBotao.addActionListener((ActionEvent e3) -> {
                bd.atualizarDados((String) jcTemp.getSelectedItem());   
            });
            
            for (ActionListener listener: deleteBotao.getActionListeners())
                deleteBotao.removeActionListener(listener);
            
            deleteBotao.addActionListener((ActionEvent e4) -> {
                bd.deletarDados((String) jcTemp.getSelectedItem());   
            });
            
            
        });
    }
}
