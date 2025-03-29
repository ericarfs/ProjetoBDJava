package projetobd;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
import javax.swing.JButton;


/**
 *
 * @author Érica Ribeiro
 */
public class JanelaPrincipal {
    private JFrame janela;
    private JTabbedPane tabbedPane;
    
    private JPanel painelSuperior;
    private JPanel painelInferior;
    private JPanel painelLateral;
    private JPanel painelAcoes;
    
    private JComboBox comboBoxTabelas;
    private JTextArea areaStatus;
    
    private JPanel painelExibicao;
    private JPanel painelInsercao; 
    private JPanel painelConsulta;
    
    private JButton botaoExportacao;
    private JButton botaoInsercao;    
    private JButton botaoConsulta;
    private JButton botaoDelecao;
    private JButton botaoAtualizacao;

    private JTable tabela;
    private JScrollPane scrollPane;
    
    private DBFuncionalidades bd;
    
    public void ExibeJanelaPrincipal() throws IOException {
        inicializarComponentes();
        configurarJanela();
        definirEventos();   
        
        // Chamar banco de dados
        bd = new DBFuncionalidades(areaStatus);
        if (bd.conectar())
            bd.pegarNomesDeTabelas(comboBoxTabelas);
        
    }
        
    private void inicializarComponentes() {
        janela = new JFrame("ICMC-USP - SCC0641 - Projeto - Apache Cassandra");
        janela.setSize(1080, 640);
        janela.setLayout(new BorderLayout());
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        painelSuperior = new JPanel();
        painelInferior = new JPanel();
        painelLateral = new JPanel();
        painelAcoes = new JPanel();
        painelAcoes.setLayout(new BoxLayout(painelAcoes, BoxLayout.LINE_AXIS));
        
        comboBoxTabelas = new JComboBox();
        painelSuperior.add(comboBoxTabelas);
        
        areaStatus = new JTextArea("Aqui é sua área de status");
        painelInferior.add(areaStatus);
        
        botaoExportacao = new JButton("Exportar CSV MSExcel");
        botaoInsercao = new JButton("Inserir Dados");
        botaoConsulta = new JButton("Buscar");
        botaoAtualizacao = new JButton("Atualizar");
        botaoDelecao = new JButton("Deletar");
        
        painelSuperior.add(botaoExportacao);
        painelSuperior.add(botaoInsercao);
        
        painelAcoes.add(botaoConsulta);
        painelAcoes.add(Box.createHorizontalStrut(20));
        painelAcoes.add(botaoAtualizacao);
        painelAcoes.add(Box.createHorizontalStrut(20));
        painelAcoes.add(botaoDelecao);
        painelAcoes.add(Box.createHorizontalStrut(20));
        
        tabbedPane = new JTabbedPane();
        painelExibicao = new JPanel();
        painelExibicao.setLayout(new BoxLayout(painelExibicao, BoxLayout.PAGE_AXIS));
        painelInsercao = new JPanel(new GridLayout(1, 1));
        painelConsulta = new JPanel();
        
        painelLateral.add(painelConsulta);
        
        tabbedPane.addTab("Exibição", painelExibicao);
        tabbedPane.addTab("Inserção", painelInsercao);
    }

    private void configurarJanela() {
        janela.add(painelSuperior, BorderLayout.NORTH);
        janela.add(painelInferior, BorderLayout.SOUTH);
        janela.add(painelLateral, BorderLayout.EAST);
        janela.add(tabbedPane, BorderLayout.CENTER);
        
        janela.setVisible(true);
    }   

    private void definirEventos() {
        tabbedPane.addChangeListener(e -> visibilidadeBotoes(tabbedPane.getSelectedIndex()));
        
        comboBoxTabelas.addActionListener((ActionEvent e) -> { 
            atualizarExibicaoDeDados((JComboBox) e.getSource());
        });
    }
    
    private void visibilidadeBotoes(int index){
        boolean exibicao = index == 0;
        boolean insercao = index == 1;
        
        botaoExportacao.setVisible(exibicao);
        botaoConsulta.setVisible(exibicao);
        botaoInsercao.setVisible(insercao);
        painelConsulta.setVisible(exibicao); 
    }
    
    private void atualizarExibicaoDeDados(JComboBox comboBoxTabelasTemp) {
        String tabelaSelecionada = (String) comboBoxTabelas.getSelectedItem();
        String tabelaTemporaria = (String) comboBoxTabelasTemp.getSelectedItem();
           
        bd.preencheDadosDeTabela(tabelaTemporaria);
        
        tabela = bd.exibeDados(tabelaSelecionada);
        scrollPane = new JScrollPane(tabela);
        
        painelExibicao.removeAll();
        painelExibicao.add(scrollPane);
        painelExibicao.add(painelAcoes);
        
        painelConsulta.removeAll();
        bd.inserirPainelBusca(painelConsulta, tabelaSelecionada);
        
        painelInsercao.removeAll();
        bd.view.construirPainelDeInsercao(painelInsercao);
        
        painelInsercao.revalidate();
        painelInsercao.repaint();
        
        configurarAcoesBotoes(tabelaSelecionada);
    }

    private void configurarAcoesBotoes(String tabelaSelecionada) {
        configurarBotao(botaoExportacao, e -> exportarDados(tabelaSelecionada));
        configurarBotao(botaoInsercao, e -> bd.inserirDados(painelInsercao, tabelaSelecionada));
        configurarBotao(botaoConsulta, e -> bd.consultarDados(painelConsulta, tabelaSelecionada));
        configurarBotao(botaoAtualizacao, e -> bd.atualizarDados(tabelaSelecionada));
        configurarBotao(botaoDelecao, e -> bd.deletarDados(tabelaSelecionada));
    }

    private void configurarBotao(JButton botao, ActionListener acao) {
        for (ActionListener listener : botao.getActionListeners()) {
            botao.removeActionListener(listener);
        }
        botao.addActionListener(acao);
    }

    private void exportarDados(String tabelaSelecionada) {
        try {
            bd.salvarDados(tabelaSelecionada);
        } catch (IOException ex) {
            Logger.getLogger(JanelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}