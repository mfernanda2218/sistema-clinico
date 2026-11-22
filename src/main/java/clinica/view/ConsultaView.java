package clinica.view;

import clinica.model.Consulta;
import clinica.model.Medico;
import clinica.model.Paciente;
import clinica.interfaces.Cobravel;
import clinica.exception.HorarioIndisponivelException;
import clinica.service.ConsultaService;
import clinica.service.MedicoService;
import clinica.service.PacienteService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

public class ConsultaView extends BorderPane {
    private final ConsultaService consultas;
    private final PacienteService pacientes;
    private final MedicoService medicos;
    private final ComboBox<Paciente> paciente = new ComboBox<>();
    private final ComboBox<Medico> medico = new ComboBox<>();
    private final DatePicker data = new DatePicker(LocalDate.now());
    private final TextField hora = new TextField();
    private final TextField tipo = new TextField();
    private final TableView<Consulta> tabela = new TableView<>();
    private final Label cobranca = new Label("Selecione uma consulta para ver a cobrança.");
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    public ConsultaView(ConsultaService consultas, PacienteService pacientes, MedicoService medicos) {
        this.consultas = consultas;
        this.pacientes = pacientes;
        this.medicos = medicos;
        setPadding(new Insets(15));
        paciente.setConverter(conversor(p -> p.getNome() + " — " + p.getCpf()));
        medico.setConverter(conversor(m -> m.getNome() + " — CRM " + m.getCrm()));
        paciente.setMaxWidth(Double.MAX_VALUE);
        medico.setMaxWidth(Double.MAX_VALUE);
        hora.setPromptText("HH:mm");
        tipo.setPromptText("Ex.: Rotina");
        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.addRow(0, new Label("Paciente"), paciente, new Label("Médico"), medico);
        formulario.addRow(1, new Label("Data"), data, new Label("Hora"), hora);
        formulario.addRow(2, new Label("Tipo"), tipo);
        Button agendar = new Button("Agendar");
        Button remarcar = new Button("Remarcar selecionada");
        Button cancelar = new Button("Cancelar selecionada");
        Button atualizar = new Button("Atualizar");
        agendar.setOnAction(e -> agendar());
        remarcar.setOnAction(e -> remarcar());
        cancelar.setOnAction(e -> cancelar());
        atualizar.setOnAction(e -> atualizar());
        remarcar.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        cancelar.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        FlowPane botoes = new FlowPane(10, 10, agendar, remarcar, cancelar, atualizar);
        VBox topo = new VBox(12, new Label("Agendamento e cobrança"), formulario, botoes,
                new Label("Para remarcar, selecione a consulta e informe a nova data e hora."));
        topo.setPadding(new Insets(0, 0, 15, 0));
        setTop(topo);
        coluna("Data e hora", c -> c.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 145);
        coluna("Paciente", c -> c.getPaciente().getNome(), 140);
        coluna("Médico", c -> c.getMedico().getNome(), 140);
        coluna("Tipo", Consulta::getTipo, 100);
        coluna("Cobrança", Consulta::getFormaCobranca, 100);
        coluna("Valor", this::valor, 100);
        tabela.setPlaceholder(new Label("Nenhuma consulta agendada."));
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antiga, selecionada) -> {
            if (selecionada != null) {
                paciente.setValue(selecionada.getPaciente());
                medico.setValue(selecionada.getMedico());
                data.setValue(selecionada.getDataHora().toLocalDate());
                hora.setText(selecionada.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm")));
                tipo.setText(selecionada.getTipo());
                cobranca.setText(selecionada.getFormaCobranca() + ": " + valor(selecionada));
            } else {
                cobranca.setText("Selecione uma consulta para ver a cobrança.");
            }
        });
        setCenter(tabela);
        cobranca.setPadding(new Insets(12, 0, 0, 0));
        setBottom(cobranca);
        atualizar();
    }

    private <T> StringConverter<T> conversor(Function<T, String> texto) {
        return new StringConverter<>() {
            public String toString(T item) { return item == null ? "" : texto.apply(item); }
            public T fromString(String valor) { return null; }
        };
    }

    private void coluna(String titulo, Function<Consulta, String> texto, double largura) {
        TableColumn<Consulta, String> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(c -> new ReadOnlyStringWrapper(texto.apply(c.getValue())));
        coluna.setPrefWidth(largura);
        tabela.getColumns().add(coluna);
    }

    private String valor(Consulta consulta) {
        Cobravel cobravel = consulta;
        return moeda.format(cobravel.calcularValor());
    }

    private LocalDateTime horario() {
        data.commitValue();
        if (data.getValue() == null || !hora.getText().trim().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            throw new IllegalArgumentException("Informe uma data e uma hora válida no formato HH:mm.");
        }
        return LocalDateTime.of(data.getValue(), LocalTime.parse(hora.getText().trim()));
    }

    public void atualizar() {
        try {
            paciente.getItems().setAll(pacientes.listar());
            medico.getItems().setAll(medicos.listar());
            var lista = consultas.listar();
            lista.sort(Comparator.comparing(Consulta::getDataHora));
            tabela.getItems().setAll(lista);
        } catch (Exception e) {
            erro(e);
        }
    }

    private void agendar() {
        try {
            if (paciente.getValue() == null || medico.getValue() == null) {
                throw new IllegalArgumentException("Selecione o paciente e o médico.");
            }
            Consulta nova = consultas.agendar(paciente.getValue().getCpf(), medico.getValue().getCrm(), horario(), tipo.getText());
            atualizar();
            tabela.getSelectionModel().select(nova);
        } catch (Exception e) { erro(e); }
    }

    private void remarcar() {
        Consulta selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) { return; }
        try {
            consultas.remarcar(selecionada.getId(), horario());
            atualizar();
            tabela.getSelectionModel().select(consultas.buscarPorId(selecionada.getId()));
        } catch (Exception e) { erro(e); }
    }

    private void cancelar() {
        Consulta selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) { return; }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancelar a consulta de " + selecionada.getPaciente().getNome() + "?", ButtonType.YES, ButtonType.NO);
        if (confirmacao.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) { return; }
        try {
            consultas.cancelar(selecionada.getId());
            atualizar();
        } catch (Exception e) { erro(e); }
    }

    private void erro(Exception e) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setHeaderText(e instanceof HorarioIndisponivelException ? "Horário indisponível" : "Não foi possível concluir");
        alerta.setContentText(e.getMessage() == null ? "Verifique os dados informados." : e.getMessage());
        alerta.showAndWait();
    }
}
