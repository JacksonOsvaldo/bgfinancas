/*
Copyright 2015 Jose Robson Mariano Alves

This file is part of bgfinancas.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This package is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

*/

package badernageral.bgfinancas.modulo.despesa;

import badernageral.bgfinancas.biblioteca.contrato.Categoria;
import badernageral.bgfinancas.biblioteca.contrato.ControladorFormulario;
import badernageral.bgfinancas.biblioteca.utilitario.Animacao;
import badernageral.bgfinancas.biblioteca.sistema.Botao;
import badernageral.bgfinancas.biblioteca.utilitario.Erro;
import badernageral.bgfinancas.biblioteca.utilitario.Validar;
import badernageral.bgfinancas.biblioteca.contrato.Item;
import badernageral.bgfinancas.biblioteca.sistema.Janela;
import badernageral.bgfinancas.biblioteca.sistema.Kernel;
import badernageral.bgfinancas.biblioteca.tipo.Acao;
import badernageral.bgfinancas.biblioteca.tipo.Duracao;
import badernageral.bgfinancas.biblioteca.tipo.Operacao;
import badernageral.bgfinancas.biblioteca.tipo.Status;
import badernageral.bgfinancas.biblioteca.utilitario.Datas;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import badernageral.bgfinancas.template.botao.BotaoFormulario;
import badernageral.bgfinancas.template.botao.BotaoListaCategoria;
import badernageral.bgfinancas.modelo.Conta;
import badernageral.bgfinancas.modelo.Despesa;
import badernageral.bgfinancas.modelo.DespesaItem;
import badernageral.bgfinancas.modulo.conta.ContaFormularioControlador;
import badernageral.bgfinancas.modulo.despesa.item.DespesaItemFormularioControlador;
import badernageral.bgfinancas.template.botao.BotaoListaItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public final class DespesaFormularioControlador implements Initializable, ControladorFormulario {
       
    @FXML private TitledPane formulario;
    @FXML private GridPane tabela;
    @FXML private Label labelItem;
    @FXML private Label labelConta;
    @FXML private Label labelData;
    @FXML private Label labelQuantidade;
    @FXML private Label labelValor;
    @FXML private BotaoListaItem itemController;
    @FXML private BotaoListaCategoria contaController;
    @FXML private DatePicker data;
    @FXML private TextField quantidade;
    @FXML private TextField valor;
    @FXML private BotaoFormulario botaoController;
    
    private final CheckBox checkAgendar = new CheckBox();
    private final TextField qtdMeses = new TextField();
    private Boolean pagar = false;
    
    private Despesa modelo;
    
    private Acao acao;
    private DespesasAgendadasControlador controlador = null;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        formulario.setText(idioma.getMensagem("despesa"));
        Botao.prepararBotaoModal(this, botaoController, itemController, contaController);
        labelItem.setText(idioma.getMensagem("item")+":");
        labelConta.setText(idioma.getMensagem("conta")+":");
        labelData.setText(idioma.getMensagem("data")+":");
        labelQuantidade.setText(idioma.getMensagem("quantidade")+":");
        labelValor.setText(idioma.getMensagem("valor")+":");
        new DespesaItem().montarSelectItem(itemController.getComboItem());
        new Conta().montarSelectCategoria(contaController.getComboCategoria());
    }
    
    @Override
    public void acaoCancelar() {
        Animacao.fadeInOutClose(formulario);
    }
    
    @Override
    public void acaoCadastrar(int botao) {
        if(botao==1){
            Animacao.fadeOutInvisivel(itemController.getComboItem(), formulario);
            DespesaItemFormularioControlador controladorItem = Janela.abrir(DespesaItem.FXML_FORMULARIO, idioma.getMensagem("despesa"));
            controladorItem.cadastrar(this, null, itemController.getComboItem().getEditor().getText());
        }else{
            Animacao.fadeOutInvisivel(contaController.getComboCategoria(), formulario);
            ContaFormularioControlador controller = Janela.abrir(Conta.FXML_FORMULARIO, idioma.getMensagem("despesa"));
            controller.cadastrar(this);
        }
    }

    @Override
    public void selecionarComboItem(int combo, Item item) {
        if(item!=null){
            new DespesaItem().montarSelectItem(itemController.getComboItem());
            itemController.setItemSelecionado(item);
        }
        Animacao.fadeInInvisivel(itemController.getComboItem(), formulario);
    }
    
    @Override
    public void selecionarComboCategoria(int combo, Categoria categoria) {
        if(categoria!=null){
            new Conta().montarSelectCategoria(contaController.getComboCategoria());
            contaController.setCategoriaSelecionada(categoria);
        }
        Animacao.fadeInInvisivel(contaController.getComboCategoria(), formulario);
    }
    
    public void cadastrar(DespesasAgendadasControlador controlador){
        acao = Acao.CADASTRAR;
        this.controlador = controlador;
        if(controlador!=null){
            data.setValue(LocalDate.now().withMonth(controlador.getData().getMonthValue()));
        }else{
            data.setValue(LocalDate.now());
        }
        quantidade.setText("1");
        botaoController.setTextBotaoFinalizar(idioma.getMensagem("cadastrar"));
        tabela.getChildren().remove(botaoController.getStackPane());
        tabela.add(botaoController.getStackPane(), 1, 6);
        Label labelAgendada = new Label(idioma.getMensagem("agendar")+":");
        tabela.add(labelAgendada, 0, 5);
        qtdMeses.setPromptText(idioma.getMensagem("numero_meses"));
        qtdMeses.setOnAction(e -> acaoFinalizar());
        HBox grupo = new HBox();
        grupo.getChildren().addAll(checkAgendar,qtdMeses);
        tabela.add(grupo, 1, 5);
        if(controlador!=null){
            checkAgendar.setSelected(true);
            checkAgendar.setDisable(true);
        }else{
            qtdMeses.setVisible(false);
        }
        checkAgendar.setOnAction(e -> {
            if(checkAgendar.isSelected()){
                qtdMeses.setVisible(true);
            }else{
                qtdMeses.setVisible(false);
            }
        });
    }
    
    public void alterar(Despesa modelo){
        this.modelo = modelo;
        botaoController.setTextBotaoFinalizar(idioma.getMensagem("alterar"));
        DespesaItem item = new DespesaItem().setIdItem(modelo.getIdItem()).consultar();
        if(item != null){
            itemController.setItemSelecionado(item);
        }
        Conta conta = new Conta().setIdCategoria(modelo.getIdConta()).consultar();
        if(conta != null){
            contaController.setCategoriaSelecionada(conta);
        }
        itemController.getComboItem().setDisable(true);
        itemController.getBotaoCadastrar().setDisable(true);
        data.setValue(modelo.getDataLocal());
        quantidade.setText(modelo.getQuantidade());
        valor.setText(modelo.getValor());
        if(modelo.getAgendada().equals("1")){
            Button bPagar = new Button(idioma.getMensagem("pagar"));
            botaoController.getGrupoBotao().getChildren().add(0, bPagar);
            bPagar.setOnAction(e -> {
                pagar = true;
                acaoFinalizar();
            });
        }
    }
    
    @Override
    public void acaoFinalizar(){
        if(validarFormulario()){
            if(acao == Acao.CADASTRAR){
                if(checkAgendar.isSelected()){
                    LocalDate dataCadastro = data.getValue();
                    int j = Integer.parseInt(qtdMeses.getText());
                    for(int i=1;i<=j;i++){
                        Despesa item = new Despesa(null, contaController.getIdCategoria(), itemController.getIdItem(), quantidade.getText(), valor.getText(), dataCadastro, Datas.getHoraAtual(), "1");
                        if(j>1){ item.setParcela(i+"/"+j); }
                        item.cadastrar();
                        dataCadastro = dataCadastro.plusMonths(1);
                    }
                    Kernel.principal.acaoDespesasAgendadas(data.getValue().getMonthValue(), data.getValue().getYear());
                }else{
                    Despesa item = new Despesa(null, contaController.getIdCategoria(), itemController.getIdItem(), quantidade.getText(), valor.getText(), data.getValue(), Datas.getHoraAtual(), "0");
                    item.cadastrar();
                    new Conta().alterarSaldo(Operacao.DECREMENTAR, contaController.getIdCategoria(), valor.getText());
                    Kernel.controlador.acaoFiltrar(true);
                }
                Janela.showTooltip(Status.SUCESSO, idioma.getMensagem("operacao_sucesso"), Duracao.CURTA);
                Animacao.fadeInOutClose(formulario);
            }else{
                if(!modelo.getAgendada().equals("1")){
                    Boolean contaMudou = !(modelo.getIdConta().equals(contaController.getIdCategoria()));
                    if(contaMudou){
                        new Conta().alterarSaldo(Operacao.INCREMENTAR, modelo.getIdConta(), modelo.getValor());
                        new Conta().alterarSaldo(Operacao.DECREMENTAR, contaController.getIdCategoria(), modelo.getValor());
                    }
                    modelo.setIdConta(contaController.getComboCategoria().getValue());
                    Boolean valorMudou = !(modelo.getValor().equals(valor.getText()));
                    if(valorMudou){
                        BigDecimal valorDiferenca = new BigDecimal(modelo.getValor());
                        valorDiferenca = valorDiferenca.subtract(new BigDecimal(valor.getText()));
                        new Conta().alterarSaldo(Operacao.INCREMENTAR, modelo.getIdConta(), valorDiferenca.toString());
                    }
                }else{
                    if(pagar){
                        modelo.setAgendada("0");
                        new Conta().alterarSaldo(Operacao.DECREMENTAR, contaController.getIdCategoria(), valor.getText());
                    }
                }
                modelo.setValor(valor.getText());
                modelo.setQuantidade(quantidade.getText());
                modelo.setData(data.getValue());
                modelo.alterar();
                Kernel.controlador.acaoFiltrar(true);
                Janela.showTooltip(Status.SUCESSO, idioma.getMensagem("operacao_sucesso"), Duracao.CURTA);
                Animacao.fadeInOutClose(formulario);
            }
        }
    }
    
    private boolean validarFormulario(){
        try {
            Validar.autoFiltro(itemController.getAutoFiltro(), itemController.getComboItem());
            Validar.comboBox(contaController.getComboCategoria());
            Validar.datePicker(data);
            Validar.textFieldDecimal(quantidade);
            Validar.textFieldDecimal(valor);
            if(checkAgendar.isSelected()){
                Validar.textFieldInteiro(qtdMeses);
            }
            return true;
        } catch (Erro ex) {
            return false;
        }
    }
    
    public void lancamentoGuiado(Item item){
        itemController.setItemSelecionado(item);
        itemController.getComboItem().setDisable(true);
        itemController.getBotaoCadastrar().setDisable(true);
        itemController.getBotaoCadastrar().setVisible(false);
    }

}
