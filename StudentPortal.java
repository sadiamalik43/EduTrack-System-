package com.example.haris1;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.*;

public class StudentPortal extends Application {
    private static final String DATA_FILE = "students.dat";
    private static final int WIDTH = 650, HEIGHT = 650;
    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();
    private Scene mainScene;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        initializeData();
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        Label title = new Label("Welcome to the Student Portal");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
        ComboBox<String> roles = new ComboBox<>();
        roles.getItems().addAll("Vice Chancellor", "Head Master", "Teacher", "Student");
        roles.setValue("Student");
        Button loginBtn = new Button("Login");
        loginBtn.setOnAction(e -> showLogin(stage, roles.getValue().toLowerCase()));
        root.getChildren().addAll(title, roles, loginBtn);
        mainScene = new Scene(root, WIDTH, 220);
        stage.setScene(mainScene);
        stage.setTitle("Student Portal");
        stage.show();
    }

    private void showLogin(Stage stage, String role) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20)); grid.setHgap(10); grid.setVgap(10);
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        Button login = new Button("Login"), back = new Button("Back");
        int row = 0;
        row = addLabeledField(grid, "Username:", userField, row);
        row = addLabeledField(grid, "Password:", passField, row);
        grid.add(login, 1, row++); grid.add(back, 1, row);
        login.setOnAction(e -> {
            User u = users.get(userField.getText().trim());
            if (u != null && passField.getText().equals(u.getPassword()) &&
                    ((role.equals("student") && u instanceof Student) ||
                            (role.equals("teacher") && u instanceof Teacher) ||
                            ((role.equals("vice chancellor") || role.equals("head master")) && u instanceof Faculty && !(u instanceof Teacher)))) {
                if (u instanceof Student s) showStudentView(stage, s);
                else showAdminView(stage);
            } else showAlert("Login Failed", "Invalid credentials or role.");
        });
        back.setOnAction(e -> applyFade(stage, mainScene));
        applyFade(stage, new Scene(grid, WIDTH, 250));
        stage.setTitle("Login - " + role);
    }

    private void showStudentView(Stage stage, Student s) {
        GridPane grid = createInfoGrid(s);
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        Button back = new Button("Back");
        grid.add(back, 0, grid.getRowCount());
        back.setOnAction(e -> start(stage));
        applyFade(stage, new Scene(scroll, WIDTH, HEIGHT));
        stage.setTitle("Student View - " + s.getName());
    }

    private void showAdminView(Stage stage) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        Label title = new Label("Admin Panel - Manage Students");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
        ListView<String> list = new ListView<>();
        list.getItems().addAll(students.keySet());
        HBox btns = new HBox(10);
        Button add = new Button("Add"), view = new Button("View"), edit = new Button("Edit"), del = new Button("Delete"), logout = new Button("Logout");
        btns.getChildren().addAll(add, view, edit, del, logout);
        root.getChildren().addAll(title, list, btns);
        add.setOnAction(e -> showStudentForm(stage, null));
        view.setOnAction(e -> {
            Student s = students.get(list.getSelectionModel().getSelectedItem());
            if (s != null) showStudentView(stage, s);
            else showAlert("Error", "No student selected.");
        });
        edit.setOnAction(e -> showStudentForm(stage, students.get(list.getSelectionModel().getSelectedItem())));
        del.setOnAction(e -> {
            String sel = list.getSelectionModel().getSelectedItem();
            if (sel != null) {
                students.remove(sel); users.remove(sel); saveData(); list.getItems().remove(sel);
                showAlert("Success", "Student deleted.");
            }
        });
        logout.setOnAction(e -> start(stage));
        applyFade(stage, new Scene(root, WIDTH, HEIGHT));
    }

    private void showStudentForm(Stage stage, Student student) {
        GridPane form = new GridPane(); form.setPadding(new Insets(15)); form.setHgap(10); form.setVgap(10);
        ScrollPane scroll = new ScrollPane(form); scroll.setFitToWidth(true);
        TextField name = new TextField(), pass = new PasswordField(), father = new TextField(), reg = new TextField(), sem = new TextField(),
                blood = new TextField(), dept = new TextField(), cgpa = new TextField(), contact = new TextField(), email = new TextField();
        TextArea addr = new TextArea(); VBox subjectsBox = new VBox(5);
        int row = 0;
        row = addLabeledField(form, "Name:", name, row);
        row = addLabeledField(form, "Password:", pass, row);
        row = addLabeledField(form, "Father's Name:", father, row);
        row = addLabeledField(form, "Registration:", reg, row);
        row = addLabeledField(form, "Semester:", sem, row);
        row = addLabeledField(form, "Blood Group:", blood, row);
        row = addLabeledField(form, "Department:", dept, row);
        row = addLabeledField(form, "CGPA:", cgpa, row);
        row = addLabeledField(form, "Contact:", contact, row);
        row = addLabeledField(form, "Email:", email, row);
        form.add(new Label("Address:"), 0, row); form.add(addr, 1, row++);
        form.add(new Label("Subjects (Name & Grade):"), 0, row++, 2, 1); form.add(subjectsBox, 0, row++, 2, 1);
        Button addSub = new Button("Add Subject");
        form.add(addSub, 0, row++, 2, 1);
        addSub.setOnAction(e -> {
            HBox rowBox = new HBox(10);
            TextField sub = new TextField(), grade = new TextField();
            Button rem = new Button("Remove");
            rowBox.getChildren().addAll(sub, grade, rem);
            rem.setOnAction(ev -> subjectsBox.getChildren().remove(rowBox));
            subjectsBox.getChildren().add(rowBox);
        });
        if (student != null) {
            name.setText(student.getName()); pass.setText(student.getPassword()); father.setText(student.getFatherName());
            reg.setText(student.getRegistrationName()); sem.setText(student.getSemester()); blood.setText(student.getBloodGroup());
            dept.setText(student.getDepartment()); cgpa.setText(String.valueOf(student.getCgpa())); contact.setText(student.getContact());
            email.setText(student.getEmail()); addr.setText(student.getAddress());
            for (Subject s : student.getSubjects()) {
                HBox box = new HBox(10);
                TextField sub = new TextField(s.getName()), grade = new TextField(s.getGrade());
                Button rem = new Button("Remove"); rem.setOnAction(e -> subjectsBox.getChildren().remove(box));
                box.getChildren().addAll(sub, grade, rem);
                subjectsBox.getChildren().add(box);
            }
        }
        Button save = new Button("Save"), cancel = new Button("Cancel");
        HBox btns = new HBox(10, save, cancel);
        form.add(btns, 0, row++, 2, 1);
        save.setOnAction(e -> {
            try {
                double gpa = Double.parseDouble(cgpa.getText());
                List<Subject> subs = new ArrayList<>();
                for (var node : subjectsBox.getChildren()) {
                    if (node instanceof HBox h) {
                        TextField sn = (TextField) h.getChildren().get(0);
                        TextField gr = (TextField) h.getChildren().get(1);
                        if (!sn.getText().isEmpty() && !gr.getText().isEmpty()) subs.add(new Subject(sn.getText(), gr.getText()));
                    }
                }
                Student s = new Student(name.getText(), pass.getText(), father.getText(), reg.getText(), sem.getText(), blood.getText(), dept.getText(), gpa, contact.getText(), email.getText(), addr.getText(), subs);
                students.put(s.getName(), s); users.put(s.getName(), s); saveData(); showAlert("Saved", "Student saved."); showAdminView(stage);
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid CGPA");
            }
        });
        cancel.setOnAction(e -> showAdminView(stage));
        applyFade(stage, new Scene(scroll, WIDTH, HEIGHT));
        stage.setTitle((student == null ? "Add" : "Edit") + " Student");
    }

    private void initializeData() {
        users.put("vicechancellor", new ViceChancellor("vicechancellor", "123"));
        users.put("headmaster", new HeadMaster("headmaster", "123"));
        users.put("teacher1", new Teacher("teacher1", "123"));
        loadData();
        if (students.isEmpty()) {
            List<Subject> subs = List.of(new Subject("Math", "A"), new Subject("Physics", "B+"));
            Student s = new Student("alice", "pass123", "Bob", "RegAlice", "5", "O+", "CS", 3.75, "1234567890", "alice@example.com", "123 Wonderland", subs);
            students.put(s.getName(), s); users.put(s.getName(), s); saveData();
        }
    }

    private GridPane createInfoGrid(Student s) {
        GridPane g = new GridPane(); g.setPadding(new Insets(15)); g.setHgap(10); g.setVgap(10);
        int row = 0;
        row = addLabeledField(g, "Name:", new Label(s.getName()), row);
        row = addLabeledField(g, "Father's Name:", new Label(s.getFatherName()), row);
        row = addLabeledField(g, "Registration:", new Label(s.getRegistrationName()), row);
        row = addLabeledField(g, "Semester:", new Label(s.getSemester()), row);
        row = addLabeledField(g, "Blood Group:", new Label(s.getBloodGroup()), row);
        row = addLabeledField(g, "Department:", new Label(s.getDepartment()), row);
        row = addLabeledField(g, "CGPA:", new Label(String.format("%.2f", s.getCgpa())), row);
        row = addLabeledField(g, "Contact:", new Label(s.getContact()), row);
        row = addLabeledField(g, "Email:", new Label(s.getEmail()), row);
        row = addLabeledField(g, "Address:", new Label(s.getAddress()), row);
        g.add(new Label("Subjects & Grades:"), 0, row++, 2, 1);
        for (Subject sub : s.getSubjects()) row = addLabeledField(g, sub.getName(), new Label(sub.getGrade()), row);
        return g;
    }

    private int addLabeledField(GridPane pane, String label, Control field, int row) {
        pane.add(new Label(label), 0, row); pane.add(field, 1, row); return row + 1;
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(students);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Object o = in.readObject();
            if (o instanceof HashMap) ((HashMap<?, ?>) o).forEach((k, v) -> {
                if (k instanceof String key && v instanceof Student s) {
                    students.put(key, s); users.put(key, s);
                }
            });
        } catch (Exception ignored) {}
    }

    private void applyFade(Stage stage, Scene scene) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), scene.getRoot());
        ft.setFromValue(0); ft.setToValue(1); stage.setScene(scene); ft.play();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }

    private static abstract class User implements Serializable {
        private final String username, password;
        protected User(String u, String p) { this.username = u; this.password = p; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    private static abstract class Faculty extends User { Faculty(String u, String p) { super(u, p); } }
    private static class ViceChancellor extends Faculty { ViceChancellor(String u, String p) { super(u, p); } }
    private static class HeadMaster extends Faculty { HeadMaster(String u, String p) { super(u, p); } }
    private static class Teacher extends Faculty { Teacher(String u, String p) { super(u, p); } }
    private static class Student extends User {
        private final String name, fatherName, registrationName, semester, bloodGroup, department, contact, email, address;
        private final double cgpa; private final List<Subject> subjects;
        Student(String n, String p, String f, String r, String s, String b, String d, double c, String con, String e, String a, List<Subject> sub) {
            super(n, p); name = n; fatherName = f; registrationName = r; semester = s; bloodGroup = b; department = d;
            cgpa = c; contact = con; email = e; address = a; subjects = sub;
        }
        public String getName() { return name; }
        public String getFatherName() { return fatherName; }
        public String getRegistrationName() { return registrationName; }
        public String getSemester() { return semester; }
        public String getBloodGroup() { return bloodGroup; }
        public String getDepartment() { return department; }
        public double getCgpa() { return cgpa; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
        public List<Subject> getSubjects() { return subjects; }
    }
    private static class Subject implements Serializable {
        private final String name, grade;
        Subject(String n, String g) { name = n; grade = g; }
        public String getName() { return name; }
        public String getGrade() { return grade; }
    }
}