package clinica.view;

import clinica.exception.PacienteNaoEncontradoException;
import clinica.exception.AlertaUtil;
import clinica.model.Paciente;
import clinica.model.Registro;
import clinica.service.PacienteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import clinica.view.ValidadorUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PacienteView extends BorderPane {

    private final PacienteService service;
    private final ObservableList<Paciente> dados = FXCollections.observableArrayList();

    // Campos do formulário
    private TextField txtNome;
    private TextField txtCpf;
    private TextField txtTelefone;
    private DatePicker dpNascimento;
    private TextField txtConvenio;
    private TableView<Paciente> tabela;

    private Paciente pacienteSelecionado; // null = modo Novo

    public PacienteView(PacienteService service) {
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
        dpNascimento = new DatePicker();
        txtConvenio = new TextField();
        txtConvenio.setPromptText("Deixe vazio se particular");

        form.add(new Label("Nome:"), 0, 0);
        form.add(txtNome, 1, 0);
        form.add(new Label("CPF:"), 0, 1);
        form.add(txtCpf, 1, 1);
        form.add(new Label("Telefone:"), 0, 2);
        form.add(txtTelefone, 1, 2);
        form.add(new Label("Nascimento:"), 0, 3);
        form.add(dpNascimento, 1, 3);
        form.add(new Label("Convênio:"), 0, 4);
        form.add(txtConvenio, 1, 4);

        // Botões
        HBox botoes = new HBox(10);
        botoes.setAlignment(Pos.CENTER_LEFT);
        botoes.setPadding(new Insets(10, 0, 10, 0));

        Button btnNovo = new Button("Novo");
        Button btnSalvar = new Button("Salvar");
        Button btnEditar = new Button("Editar");
        Button btnExcluir = new Button("Excluir");
        Button btnBuscar = new Button("Buscar CPF");
        Button btnProntuario = new Button("Prontuário");
        Button btnLimpar = new Button("Limpar");

        btnNovo.setOnAction(e -> limparFormulario());
        btnSalvar.setOnAction(e -> salvar());
        btnEditar.setOnAction(e -> prepararEdicao());
        btnExcluir.setOnAction(e -> excluir());
        btnBuscar.setOnAction(e -> buscarPorCpf());
        btnProntuario.setOnAction(e -> abrirProntuario());
        btnLimpar.setOnAction(e -> limparFormulario());

        botoes.getChildren().addAll(btnNovo, btnSalvar, btnEditar, btnExcluir,
                btnBuscar, btnProntuario, btnLimpar);

        // Tabela
        tabela = new TableView<>();
        tabela.setItems(dados);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Paciente, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        TableColumn<Paciente, String> colCpf = new TableColumn<>("CPF");
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));

        TableColumn<Paciente, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        TableColumn<Paciente, String> colConvenio = new TableColumn<>("Convênio");
        colConvenio.setCellValueFactory(cell -> {
            Paciente p = cell.getValue();
            String valor = p.isParticular() ? "Particular" : p.getConvenio();
            return new javafx.beans.property.SimpleStringProperty(valor);
        });

        tabela.getColumns().addAll(colNome, colCpf, colTelefone, colConvenio);

        // Clique na tabela → preenche o formulário
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                preencherFormulario(novo);
                pacienteSelecionado = novo;
            }
        });

        // Layout
        VBox topo = new VBox(10, form, botoes);
        setTop(topo);
        setCenter(tabela);
    }

    // AÇÕES

    private void carregarTabela() {
        try {
            List<Paciente> lista = service.listar();
            dados.setAll(lista);
        } catch (Exception e) {
            mostrarErro("Erro ao carregar pacientes", e.getMessage());
        }
    }

    private void salvar() {
        try {
            String nome = txtNome.getText();
            String cpf = txtCpf.getText();
            String telefone = txtTelefone.getText();
            LocalDate nascimento = dpNascimento.getValue();
            String convenio = txtConvenio.getText();
            if (convenio != null && convenio.isBlank()) {
                convenio = null;
            }

            if (pacienteSelecionado == null) {
                // Novo
                Paciente novo = new Paciente(nome, cpf, telefone, nascimento, convenio);
                service.cadastrar(novo);
                mostrarInfo("Sucesso", "Paciente cadastrado com sucesso!");
            } else {
                // Edição
                pacienteSelecionado.setNome(nome);
                pacienteSelecionado.setTelefone(telefone);
                pacienteSelecionado.setDataNascimento(nascimento);
                pacienteSelecionado.setConvenio(convenio);
                // CPF normalmente não se altera, mas se o service permitir:
                service.atualizar(pacienteSelecionado);
                mostrarInfo("Sucesso", "Paciente atualizado com sucesso!");
            }

            limparFormulario();
            carregarTabela();

        } catch (IllegalArgumentException e) {
            mostrarErro("Dados inválidos", e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro ao salvar", e.getMessage());
        }
    }

    private void prepararEdicao() {
        Paciente selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAviso("Selecione um paciente na tabela para editar.");
            return;
        }
        preencherFormulario(selecionado);
        pacienteSelecionado = selecionado;
        txtCpf.setDisable(true); // CPF não deve ser alterado
    }

   private void excluir() {
    Paciente selecionado = tabela.getSelectionModel().getSelectedItem();
    if (selecionado == null) {
        AlertaUtil.aviso("Selecione um paciente para excluir.");
        return;
    }

    boolean confirmou = AlertaUtil.confirmar(
            "Confirmação",
            "Excluir Paciente",
            "Deseja realmente excluir " + selecionado.getNome() + "?"
    );

    if (confirmou) {
        try {
            service.excluir(selecionado.getCrm());
            AlertaUtil.info("Sucesso", "Paciente excluído com sucesso!");
            limparFormulario();
            carregarTabela();
        } catch (PacienteNaoEncontradoException e) {
            AlertaUtil.erro("Paciente não encontrado", e);
        } catch (Exception e) {
            AlertaUtil.erro("Erro ao excluir", e);
        }
    }
}
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText("Excluir paciente");
        confirm.setContentText("Deseja realmente excluir " + selecionado.getNome() + "?");
        Optional<ButtonType> resultado = confirm.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                service.excluir(selecionado.getCpf());
                mostrarInfo("Sucesso", "Paciente excluído.");
                limparFormulario();
                carregarTabela();
            } catch (PacienteNaoEncontradoException e) {
                mostrarErro("Erro", e.getMessage());
            } catch (Exception e) {
                mostrarErro("Erro ao excluir", e.getMessage());
            }
        }
    }

    private void buscarPorCpf() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Paciente");
        dialog.setHeaderText("Informe o CPF");
        dialog.setContentText("CPF:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isEmpty() || resultado.get().isBlank()) return;

        try {
            Paciente p = service.buscarPorCpf(resultado.get());
            preencherFormulario(p);
            pacienteSelecionado = p;
            // Destaca na tabela
            tabela.getSelectionModel().select(p);
            tabela.scrollTo(p);
        } catch (PacienteNaoEncontradoException e) {
            mostrarErro("Não encontrado", e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro na busca", e.getMessage());
        }
    }

    private void abrirProntuario() {
        Paciente selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAviso("Selecione um paciente para ver o prontuário.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Prontuário - " + selecionado.getNome());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox conteudo = new VBox(10);
        conteudo.setPadding(new Insets(15));

        // Lista de registros
        ListView<String> listaRegistros = new ListView<>();
        atualizarListaRegistros(listaRegistros, selecionado);

        // Campos para novo registro
        TextArea txtDescricao = new TextArea();
        txtDescricao.setPromptText("Descrição do registro");
        txtDescricao.setPrefRowCount(3);

        TextArea txtObservacoes = new TextArea();
        txtObservacoes.setPromptText("Observações (opcional)");
        txtObservacoes.setPrefRowCount(2);

        Button btnAdicionar = new Button("Adicionar Registro");
        btnAdicionar.setOnAction(e -> {
            String desc = txtDescricao.getText();
            if (desc == null || desc.isBlank()) {
                mostrarAviso("A descrição do registro é obrigatória.");
                return;
            }
            try {
                Registro reg = new Registro(LocalDate.now(), desc, txtObservacoes.getText());
                selecionado.getProntuario().adicionarRegistro(reg);
                // Se o service tiver método para persistir o prontuário, chame-o aqui
                service.atualizar(selecionado); // garante persistência
                atualizarListaRegistros(listaRegistros, selecionado);
                txtDescricao.clear();
                txtObservacoes.clear();
                mostrarInfo("Sucesso", "Registro adicionado ao prontuário.");
            } catch (Exception ex) {
                mostrarErro("Erro ao adicionar registro", ex.getMessage());
            }
        });

        conteudo.getChildren().addAll(
                new Label("Registros existentes:"),
                listaRegistros,
                new Separator(),
                new Label("Novo registro:"),
                txtDescricao,
                txtObservacoes,
                btnAdicionar
        );

        dialog.getDialogPane().setContent(conteudo);
        dialog.getDialogPane().setPrefSize(500, 450);
        dialog.showAndWait();
    }

    private void atualizarListaRegistros(ListView<String> lista, Paciente paciente) {
        lista.getItems().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Registro r : paciente.getProntuario().getRegistros()) {
            String obs = (r.getObservacoes() == null || r.getObservacoes().isBlank())
                    ? "" : " | Obs: " + r.getObservacoes();
            lista.getItems().add(r.getData().format(fmt) + " - " + r.getDescricao() + obs);
        }
    }

    // ==================== UTILITÁRIOS ====================

    private void preencherFormulario(Paciente p) {
        txtNome.setText(p.getNome());
        txtCpf.setText(p.getCpf());
        txtTelefone.setText(p.getTelefone());
        dpNascimento.setValue(p.getDataNascimento());
        txtConvenio.setText(p.isParticular() ? "" : p.getConvenio());
        txtCpf.setDisable(true);
    }

    private void limparFormulario() {
        txtNome.clear();
        txtCpf.clear();
        txtTelefone.clear();
        dpNascimento.setValue(null);
        txtConvenio.clear();
        txtCpf.setDisable(false);
        pacienteSelecionado = null;
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

    private boolean validarFormulario() {
    StringBuilder erros = new StringBuilder();

    boolean nomeInv = !ValidadorUtil.isTextoValido(txtNome.getText());
    ValidadorUtil.marcarInvalido(txtNome, nomeInv);
    if (nomeInv) erros.append("- Nome é obrigatório.\n");

    boolean cpfInv = !ValidadorUtil.isCpfValido(txtCpf.getText());
    ValidadorUtil.marcarInvalido(txtCpf, cpfInv);
    if (cpfInv) erros.append("- CPF inválido (informe 11 dígitos).\n");

    boolean telInv = !ValidadorUtil.isTextoValido(txtTelefone.getText());
    ValidadorUtil.marcarInvalido(txtTelefone, telInv);
    if (telInv) erros.append("- Telefone é obrigatório.\n");

    boolean nascInv = !ValidadorUtil.isDataNascimentoValida(dpNascimento.getValue());
    ValidadorUtil.marcarInvalido(dpNascimento, nascInv);
    if (nascInv) erros.append("- Data de nascimento obrigatória e não pode ser futura.\n");

    if (!erros.isEmpty()) {
        AlertaUtil.aviso("Corrija os campos destacados antes de salvar:\n\n" + erros);
        return false;
    }
    return true;
}
}
