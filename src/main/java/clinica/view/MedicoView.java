package clinica.view;

import clinica.exception.MedicoNaoEncontradoException;
import clinica.model.Medico;
import clinica.service.MedicoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class MedicoView extends BorderPane {

    private final MedicoService service;
    private final ObservableList<Medico> dados = FXCollections.observableArrayList();

    // Campos do formulário
    private TextField txtNome;
    private TextField txtCpf;
    private TextField txtTelefone;
    private TextField txtCrm;
    private ComboBox<String> cbEspecialidade;
    private TableView<Medico> tabela;

    private Medico medicoSelecionado;

    public MedicoView(MedicoService service) {
        this.service = service;
        montarInterface();
        carregarTabela();
    }

    private void montarInterface() {
        setPadding(new Insets(15));

        // Formulário
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        txtNome = new TextField();
        txtCpf = new TextField();
        txtTelefone = new TextField();
        txtCrm = new TextField();
        cbEspecialidade = new ComboBox<>();
        cbEspecialidade.getItems().addAll("Cardiologista", "Pediatra", "Clínico Geral");
        cbEspecialidade.setPromptText("Selecione uma especialidade");

        form.add(new Label("Nome:"), 0, 0);
        form.add(txtNome, 1, 0);
        form.add(new Label("CPF:"), 0, 1);
        form.add(txtCpf, 1, 1);
        form.add(new Label("Telefone:"), 0, 2);
        form.add(txtTelefone, 1, 2);
        form.add(new Label("CRM:"), 0, 3);
        form.add(txtCrm, 1, 3);
        form.add(new Label("Especialidade:"), 0, 4);
        form.add(cbEspecialidade, 1, 4);

        // Botões
        HBox botoes = new HBox(10);
        botoes.setAlignment(Pos.CENTER_LEFT);
        botoes.setPadding(new Insets(10, 0, 10, 0));

        Button btnNovo = new Button("Novo");
        Button btnSalvar = new Button("Salvar");
        Button btnEditar = new Button("Editar");
        Button btnExcluir = new Button("Excluir");
        Button btnBuscar = new Button("Filtrar especialidade");
        Button btnLimpar = new Button("Limpar");

        btnNovo.setOnAction(e -> limparFormulario());
        btnSalvar.setOnAction(e -> salvar());
        btnEditar.setOnAction(e -> prepararEdicao());
        btnExcluir.setOnAction(e -> excluir());
        btnBuscar.setOnAction(e -> buscarPorEspecialidade());
        btnLimpar.setOnAction(e -> {
            limparFormulario();
            carregarTabela();
        });

        botoes.getChildren().addAll(btnNovo, btnSalvar, btnEditar, btnExcluir, btnBuscar, btnLimpar);

        VBox topo = new VBox(5, form, botoes);
        setTop(topo);

        // Configuração da TableView
        tabela = new TableView<>(dados);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Medico, String> colCrm = new TableColumn<>("CRM");
        colCrm.setCellValueFactory(new PropertyValueFactory<>("crm"));

        TableColumn<Medico, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Medico, String> colCpf = new TableColumn<>("CPF");
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));

        TableColumn<Medico, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        TableColumn<Medico, String> colEspecialidade = new TableColumn<>("Especialidade");
        colEspecialidade.setCellValueFactory(new PropertyValueFactory<>("especialidade"));

        tabela.getColumns().addAll(colCrm, colNome, colCpf, colTelefone, colEspecialidade);
        setCenter(tabela);
    }

    private void prepararEdicao() {
        Medico selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAviso("Selecione um médico na tabela para editar.");
            return;
        }
        preencherFormulario(selecionado);
        medicoSelecionado = selecionado;
        txtCrm.setDisable(true);
        txtCpf.setDisable(true);
    }

    private void excluir() {
        Medico selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAviso("Selecione um médico para excluir.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText("Excluir Médico");
        confirm.setContentText("Deseja realmente excluir " + selecionado.getNome() + "?");
        Optional<ButtonType> resultado = confirm.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                service.excluir(selecionado.getCrm());
                mostrarInfo("Sucesso", "Médico excluído com sucesso!");
                limparFormulario();
                carregarTabela();
            } catch (MedicoNaoEncontradoException e) {
                mostrarErro("Erro, médico não encontrado.", e.getMessage());
            } catch (Exception e) {
                mostrarErro("Erro ao excluir", e.getMessage());
            }
        }
    }

    private void carregarTabela() {
        try {
            List<Medico> lista = service.listar();
            dados.setAll(lista);
        } catch (Exception e) {
            mostrarErro("Erro ao carregar médicos", e.getMessage());
        }
    }

    private void salvar() {
        try {
            String nome = txtNome.getText();
            String cpf = txtCpf.getText();
            String telefone = txtTelefone.getText();
            String crm = txtCrm.getText();
            String especialidade = cbEspecialidade.getValue();

            if (medicoSelecionado == null) {
                service.cadastrarPorEspecialidade(especialidade, nome, cpf, telefone, crm);
                mostrarInfo("Sucesso", "Médico cadastrado com sucesso!");
            } else {
                medicoSelecionado.setNome(nome);
                medicoSelecionado.setTelefone(telefone);
                service.atualizar(medicoSelecionado);
                mostrarInfo("Sucesso", "Médico atualizado com sucesso!");
            }

            limparFormulario();
            carregarTabela();
        } catch (IllegalArgumentException | MedicoNaoEncontradoException e) {
            mostrarErro("Erro de validação", e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro ao salvar", e.getMessage());
        }
    }

    private void buscarPorEspecialidade() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Filtrar Médicos");
        dialog.setHeaderText("Informe a especialidade para filtrar");
        dialog.setContentText("Especialidade:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isEmpty() || resultado.get().isBlank()) return;

        try {
            List<Medico> filtrados = service.buscarPorEspecialidade(resultado.get());
            if (filtrados.isEmpty()) {
                mostrarAviso("Nenhum médico encontrado para a especialidade informada.");
            } else {
                dados.setAll(filtrados);
            }
        } catch (IllegalArgumentException e) {
            mostrarErro("Filtro inválido", e.getMessage());
        }
    }

    // ==================== UTILITÁRIOS ====================

    private void preencherFormulario(Medico m) {
        txtNome.setText(m.getNome());
        txtCpf.setText(m.getCpf());
        txtTelefone.setText(m.getTelefone());
        txtCrm.setText(m.getCrm());
        cbEspecialidade.setValue(m.getEspecialidade());
        txtCpf.setDisable(true);
        txtCrm.setDisable(true);
    }

    private void limparFormulario() {
        txtNome.clear();
        txtCpf.clear();
        txtTelefone.clear();
        txtCrm.clear();
        cbEspecialidade.setValue(null);
        txtCpf.setDisable(false);
        txtCrm.setDisable(false);
        medicoSelecionado = null;
        tabela.getSelectionModel().clearSelection();
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarAviso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}