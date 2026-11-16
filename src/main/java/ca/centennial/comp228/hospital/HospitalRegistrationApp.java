package ca.centennial.comp228.hospital;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class HospitalRegistrationApp extends Application {

  private static final String DB_URL =
      "jdbc:mysql://localhost:3306/hospital_db"
          + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Toronto";
  private static final String DB_USER = "hospital_app";
  private static final String DB_PASSWORD = "HospitalApp#2026"; // Change this

  private final TextField idField = new TextField();
  private final TextField nameField = new TextField();
  private final TextField ageField = new TextField();
  private final ComboBox<String> genderBox = new ComboBox<>();
  private final TextField phoneField = new TextField();
  private final TextField diagnosisField = new TextField();
  private final DatePicker admissionPicker = new DatePicker();

  private final TextField searchField = new TextField();
  private final TableView<Patient> patientTable = new TableView<>();
  private final ObservableList<Patient> patients = FXCollections.observableArrayList();
  private final Label statusLabel = new Label("Enter patient details.");

  @Override
  public void start(Stage stage) {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(20));

    Label heading = new Label("Hospital Management System");
    heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
    Label subtitle = new Label("Patient Registration • JavaFX • JDBC • MySQL");
    VBox header = new VBox(5, heading, subtitle);
    header.setPadding(new Insets(0, 0, 15, 0));
    root.setTop(header);

    VBox form = buildFormPane();
    VBox records = buildRecordsPane();
    HBox.setHgrow(records, Priority.ALWAYS);

    HBox center = new HBox(16, form, records);
    root.setCenter(center);

    HBox statusBar = new HBox(statusLabel);
    statusBar.setPadding(new Insets(10, 0, 0, 0));
    root.setBottom(statusBar);

    stage.setTitle("Hospital Management System");
    stage.setScene(new Scene(root, 1150, 680));
    stage.show();

    loadPatients();
  }

  // ---------------------------------------------------------------- layout

  private VBox buildFormPane() {
    Label section = new Label("Patient Details");
    section.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    idField.setEditable(false);
    idField.setPromptText("Auto-generated");

    nameField.setPromptText("Full name");
    ageField.setPromptText("0 - 125");
    phoneField.setPromptText("416-555-0142");
    diagnosisField.setPromptText("Reason for visit");

    genderBox.getItems().addAll("Female", "Male", "Non-binary", "Prefer not to say");
    genderBox.setPromptText("Select gender");

    admissionPicker.setPromptText("yyyy-mm-dd");

    GridPane form = new GridPane();
    form.setHgap(12);
    form.setVgap(12);
    form.setPadding(new Insets(15));

    addRow(form, 0, "Patient ID:", idField);
    addRow(form, 1, "Full name:", nameField);
    addRow(form, 2, "Age:", ageField);
    addRow(form, 3, "Gender:", genderBox);
    addRow(form, 4, "Phone:", phoneField);
    addRow(form, 5, "Diagnosis:", diagnosisField);
    addRow(form, 6, "Admission date:", admissionPicker);

    ColumnConstraints labels = new ColumnConstraints(120);
    ColumnConstraints fields = new ColumnConstraints();
    fields.setHgrow(Priority.ALWAYS);
    form.getColumnConstraints().addAll(labels, fields);

    Button addButton = new Button("Add Patient");
    addButton.setDefaultButton(true);
    addButton.setOnAction(e -> addPatient());

    Button updateButton = new Button("Update");
    updateButton.setOnAction(e -> updatePatient());

    Button deleteButton = new Button("Delete");
    deleteButton.setOnAction(e -> deletePatient());

    Button clearButton = new Button("Clear");
    clearButton.setOnAction(e -> clearForm());

    HBox row1 = new HBox(10, addButton, updateButton);
    HBox row2 = new HBox(10, deleteButton, clearButton);

    VBox pane = new VBox(12, section, form, row1, row2);
    pane.setPrefWidth(430);
    pane.setMinWidth(400);
    return pane;
  }

  private VBox buildRecordsPane() {
    Label section = new Label("Patient Records");
    section.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    searchField.setPromptText("Search by name or phone");
    HBox.setHgrow(searchField, Priority.ALWAYS);
    searchField.setOnAction(e -> searchPatients());

    Button searchButton = new Button("Search");
    searchButton.setOnAction(e -> searchPatients());

    Button showAllButton = new Button("Show All");
    showAllButton.setOnAction(e -> showAllPatients());

    HBox searchBar = new HBox(10, searchField, searchButton, showAllButton);
    searchBar.setAlignment(Pos.CENTER_LEFT);

    buildTable();

    VBox pane = new VBox(12, section, searchBar, patientTable);
    VBox.setVgrow(patientTable, Priority.ALWAYS);
    return pane;
  }

  private void buildTable() {
    TableColumn<Patient, Number> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getPatientId()));

    TableColumn<Patient, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getName()));

    TableColumn<Patient, Number> ageColumn = new TableColumn<>("Age");
    ageColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getAge()));

    TableColumn<Patient, String> genderColumn = new TableColumn<>("Gender");
    genderColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getGender()));

    TableColumn<Patient, String> phoneColumn = new TableColumn<>("Phone");
    phoneColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getPhone()));

    TableColumn<Patient, String> diagnosisColumn = new TableColumn<>("Diagnosis");
    diagnosisColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getDiagnosis()));

    TableColumn<Patient, LocalDate> dateColumn = new TableColumn<>("Admission");
    dateColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getAdmissionDate()));

    patientTable.getColumns().addAll(idColumn, nameColumn, ageColumn, genderColumn,
        phoneColumn, diagnosisColumn, dateColumn);
    patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    patientTable.setItems(patients);
    patientTable.setPlaceholder(new Label("No patients to display"));
    patientTable.setPrefHeight(450);

    patientTable.getSelectionModel().selectedItemProperty().addListener(
        (obs, old, selected) -> {
          if (selected != null) {
            populateForm(selected);
          }
        });
  }

  private void addRow(GridPane grid, int row, String text, Control control) {
    Label label = new Label(text);
    label.setStyle("-fx-font-weight: bold;");
    control.setMaxWidth(Double.MAX_VALUE);
    grid.add(label, 0, row);
    grid.add(control, 1, row);
  }

  // ------------------------------------------------------------ operations

  private void addPatient() {
    if (!validateForm()) {
      return;
    }

    String sql = "INSERT INTO patients "
        + "(name, age, gender, phone, diagnosis, admission_date) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, nameField.getText().trim());
      statement.setInt(2, Integer.parseInt(ageField.getText().trim()));
      statement.setString(3, genderBox.getValue());
      statement.setString(4, phoneField.getText().trim());
      statement.setString(5, diagnosisField.getText().trim());
      statement.setObject(6, admissionPicker.getValue());

      int count = statement.executeUpdate();
      if (count == 1) {
        setStatus("Patient added successfully!", false);
        clearFields();
        loadPatients();
      } else {
        setStatus("No record was inserted.", true);
      }
    } catch (SQLException ex) {
      setStatus("Database error: " + ex.getMessage(), true);
      ex.printStackTrace();
    }
  }

  private void loadPatients() {
    String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date "
        + "FROM patients ORDER BY patient_id";

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {

      patients.clear();
      while (resultSet.next()) {
        patients.add(new Patient(
            resultSet.getInt("patient_id"),
            resultSet.getString("name"),
            resultSet.getInt("age"),
            resultSet.getString("gender"),
            resultSet.getString("phone"),
            resultSet.getString("diagnosis"),
            resultSet.getDate("admission_date").toLocalDate()));
      }
    } catch (SQLException ex) {
      setStatus("Database error: " + ex.getMessage(), true);
      ex.printStackTrace();
    }
  }

  private void updatePatient() {
    Patient selected = patientTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      setStatus("Select a patient in the table before updating.", true);
      return;
    }
    if (!validateForm()) {
      return;
    }

    String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ?, "
        + "diagnosis = ?, admission_date = ? WHERE patient_id = ?";

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, nameField.getText().trim());
      statement.setInt(2, Integer.parseInt(ageField.getText().trim()));
      statement.setString(3, genderBox.getValue());
      statement.setString(4, phoneField.getText().trim());
      statement.setString(5, diagnosisField.getText().trim());
      statement.setObject(6, admissionPicker.getValue());
      statement.setInt(7, selected.getPatientId());

      int count = statement.executeUpdate();
      if (count == 1) {
        setStatus("Patient updated successfully!", false);
        loadPatients();
      } else {
        setStatus("No record was updated.", true);
      }
    } catch (SQLException ex) {
      setStatus("Database error: " + ex.getMessage(), true);
      ex.printStackTrace();
    }
  }

  private void deletePatient() {
    Patient selected = patientTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      setStatus("Select a patient in the table before deleting.", true);
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "Delete patient #" + selected.getPatientId() + " (" + selected.getName() + ")?",
        ButtonType.CANCEL, ButtonType.OK);
    alert.setHeaderText("Confirm delete");
    if (alert.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
      return;
    }

    String sql = "DELETE FROM patients WHERE patient_id = ?";

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, selected.getPatientId());

      int count = statement.executeUpdate();
      if (count == 1) {
        setStatus("Patient deleted successfully!", false);
        clearFields();
        loadPatients();
      } else {
        setStatus("No record was deleted.", true);
      }
    } catch (SQLException ex) {
      setStatus("Database error: " + ex.getMessage(), true);
      ex.printStackTrace();
    }
  }

  private void searchPatients() {
    String query = searchField.getText().trim();
    if (query.isEmpty()) {
      loadPatients();
      setStatus("Showing all patients.", false);
      return;
    }

    String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date "
        + "FROM patients WHERE name LIKE ? OR phone LIKE ? ORDER BY patient_id";

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, "%" + query + "%");
      statement.setString(2, "%" + query + "%");

      try (ResultSet resultSet = statement.executeQuery()) {
        patients.clear();
        while (resultSet.next()) {
          patients.add(new Patient(
              resultSet.getInt("patient_id"),
              resultSet.getString("name"),
              resultSet.getInt("age"),
              resultSet.getString("gender"),
              resultSet.getString("phone"),
              resultSet.getString("diagnosis"),
              resultSet.getDate("admission_date").toLocalDate()));
        }
      }
      setStatus(patients.size() + " record(s) matched \"" + query + "\".", false);
    } catch (SQLException ex) {
      setStatus("Database error: " + ex.getMessage(), true);
      ex.printStackTrace();
    }
  }

  private void showAllPatients() {
    searchField.clear();
    loadPatients();
    setStatus("Showing all patients.", false);
  }

  // -------------------------------------------------------- form handling

  private boolean validateForm() {
    String name = nameField.getText().trim();
    String ageValue = ageField.getText().trim();
    String phone = phoneField.getText().trim();
    String diagnosis = diagnosisField.getText().trim();

    if (name.isEmpty() || ageValue.isEmpty() || genderBox.getValue() == null
        || phone.isEmpty() || diagnosis.isEmpty() || admissionPicker.getValue() == null) {
      setStatus("Please complete all fields.", true);
      return false;
    }

    int age;
    try {
      age = Integer.parseInt(ageValue);
    } catch (NumberFormatException ex) {
      setStatus("Age must be a whole number.", true);
      return false;
    }

    if (age < 0 || age > 125) {
      setStatus("Age must be between 0 and 125.", true);
      return false;
    }
    if (!phone.matches("^[0-9+()\\-\\s]{7,20}$")) {
      setStatus("Enter a valid phone number.", true);
      return false;
    }
    if (admissionPicker.getValue().isAfter(LocalDate.now())) {
      setStatus("Admission date cannot be in the future.", true);
      return false;
    }
    return true;
  }

  private void populateForm(Patient patient) {
    idField.setText(String.valueOf(patient.getPatientId()));
    nameField.setText(patient.getName());
    ageField.setText(String.valueOf(patient.getAge()));
    genderBox.setValue(patient.getGender());
    phoneField.setText(patient.getPhone());
    diagnosisField.setText(patient.getDiagnosis());
    admissionPicker.setValue(patient.getAdmissionDate());
    setStatus("Selected patient #" + patient.getPatientId() + " - " + patient.getName(), false);
  }

  private void clearForm() {
    clearFields();
    patientTable.getSelectionModel().clearSelection();
    setStatus("Form cleared.", false);
  }

  private void clearFields() {
    idField.clear();
    nameField.clear();
    ageField.clear();
    genderBox.getSelectionModel().clearSelection();
    phoneField.clear();
    diagnosisField.clear();
    admissionPicker.setValue(null);
  }

  private void setStatus(String message, boolean error) {
    statusLabel.setText(message);
    statusLabel.setStyle(error ? "-fx-text-fill: #b00020;"
                  : "-fx-text-fill: #167545;");
  }

  public static void main(String[] args) {
    launch(args);
  }
}
