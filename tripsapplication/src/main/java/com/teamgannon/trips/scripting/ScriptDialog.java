package com.teamgannon.trips.scripting;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.ScriptContext;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.scripting.engine.GroovyScriptingEngine;
import com.teamgannon.trips.scripting.engine.PythonScriptEngine;
import com.teamgannon.trips.scripting.model.ScriptEngineEnum;
import com.teamgannon.trips.scripting.model.ScriptFile;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class ScriptDialog extends Dialog<Boolean> {

    private final GroovyScriptingEngine groovyScriptEngine;
    private final PythonScriptEngine pythonScriptEngine;
    private final Menu scriptingMenu;
    private final TripsContext tripsContext;
    private final Localization localization;
    private final DatabaseManagementService databaseManagementService;

    private final TextArea programTextArea = new TextArea();

    private final TextArea outputTextArea = new TextArea();

    private final Stage stage;
    private final ApplicationEventPublisher eventPublisher;

    private ScriptFile activeScriptfile = ScriptFile.builder().build();

    private ToggleGroup engineToggleGroup = new ToggleGroup();


    public ScriptDialog(ApplicationEventPublisher eventPublisher,
                        GroovyScriptingEngine groovyScriptEngine,
                        PythonScriptEngine pythonScriptEngine,
                        Menu scriptingMenu,
                        TripsContext tripsContext,
                        Localization localization,
                        DatabaseManagementService databaseManagementService) {
        this.eventPublisher = eventPublisher;
        this.activeScriptfile.setEngineType(ScriptEngineEnum.NONE);
        this.groovyScriptEngine = groovyScriptEngine;
        this.pythonScriptEngine = pythonScriptEngine;
        this.scriptingMenu = scriptingMenu;
        this.tripsContext = tripsContext;
        this.localization = localization;
        this.databaseManagementService = databaseManagementService;

        this.setTitle("Script Editor");
        this.setWidth(500);
        this.setHeight(700);
        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        HBox topBox = new HBox();
        topBox.setAlignment(Pos.TOP_RIGHT);
        Button outputButton = new Button("Clear output");
        topBox.getChildren().add(outputButton);
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        outputButton.setFont(font);
        outputButton.setOnAction(this::clearOutput);
        vBox.getChildren().add(topBox);

        Pane scriptObj = createScriptPane();
        vBox.getChildren().add(scriptObj);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(5, 5, 5, 5));
        buttonBox.setSpacing(10);
        vBox.getChildren().add(buttonBox);

        HBox toggleBox = new HBox();
        toggleBox.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        toggleBox.setPadding(new Insets(5, 5, 5, 5));
        toggleBox.setSpacing(5);
        buttonBox.getChildren().add(toggleBox);
        RadioButton groovyRadioButton = new RadioButton("Groovy");
        groovyRadioButton.setSelected(true);
        toggleBox.getChildren().add(groovyRadioButton);
        RadioButton pythonRadioButton = new RadioButton("Python");
        toggleBox.getChildren().add(pythonRadioButton);
        engineToggleGroup.getToggles().add(groovyRadioButton);
        engineToggleGroup.getToggles().add(pythonRadioButton);
        engineToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> changed,
                                Toggle oldVal, Toggle newVal) {
                // Display the selection.
                String type = getConversionType();
                if (type.equals("Groovy")) {
                    log.info("Groovy type selected");
                    if (activeScriptfile.getName().equals(ScriptEngineEnum.NONE) && activeScriptfile.getContents().isEmpty()) {
                        activeScriptfile.setEngineType(ScriptEngineEnum.GROOVY);
                    } else {
                        showErrorAlert("Script File", "Changing the script engine on different content may not work");
                    }
                } else {
                    log.info("Python type selected");
                    if (activeScriptfile.getName().equals(ScriptEngineEnum.NONE) && activeScriptfile.getContents().isEmpty()) {
                        activeScriptfile.setEngineType(ScriptEngineEnum.PYTHON);
                    } else {
                        showErrorAlert("Script File", "Changing the script engine on different content may not work");
                    }
                }
            }
        });

        Button loadButton = new Button("Load Script");
        loadButton.setFont(font);
        loadButton.setOnAction(this::loadScript);
        buttonBox.getChildren().add(loadButton);

        Button runButton = new Button("Run Script");
        runButton.setFont(font);
        runButton.setOnAction(this::runScript);
        buttonBox.getChildren().add(runButton);

        Button saveButton = new Button("Install Script");
        saveButton.setFont(font);
        saveButton.setOnAction(this::installScript);
        buttonBox.getChildren().add(saveButton);

        Button cancelButton = new Button("Dismiss");
        cancelButton.setFont(font);
        cancelButton.setOnAction(this::close);
        buttonBox.getChildren().add(cancelButton);

        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private String getConversionType() {
        RadioButton selectedRadioButton = (RadioButton) engineToggleGroup.getSelectedToggle();
        return selectedRadioButton.getText();
    }

    private void clearOutput(ActionEvent actionEvent) {
        outputTextArea.clear();
    }

    private void loadScript(ActionEvent actionEvent) {
        String scriptType = getConversionType();
        if ("Python".equals(scriptType)) {
            log.debug("Import a Python file");
            File file = selectFile(ScriptEngineEnum.PYTHON);
            loadFile(file, ScriptEngineEnum.PYTHON);
        } else if ("Groovy".equals(scriptType)) {
            log.info("Import a Groovy file");
            File file = selectFile(ScriptEngineEnum.GROOVY);
            loadFile(file, ScriptEngineEnum.GROOVY);
        }
    }

    private void loadFile(File file, ScriptEngineEnum scriptEngineEnum) {
        if (file != null) {
            String script = loadScriptFile(file);
            if (script != null) {
                activeScriptfile = ScriptFile
                        .builder()
                        .selected(true)
                        .engineType(scriptEngineEnum)
                        .name(file.getName())
                        .path(file.getAbsolutePath())
                        .contents(script)
                        .build();
                programTextArea.setText(script);
            }
        } else {
            log.warn("file selection cancelled");
        }
    }

    private File selectFile(ScriptEngineEnum scriptEngineEnum) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(String.format("Select a %s file to load", scriptEngineEnum));
        File filesFolder = new File(localization.getScriptDirectory());
        if (!filesFolder.exists()) {
            boolean created = filesFolder.mkdirs();
            if (!created) {
                log.error("Script files folder did not exist, but attempt to create directories failed");
                showErrorAlert(
                        "Load Script ",
                        "script files folder did not exist, but attempt to create directories failed");
            }
        }
        fileChooser.setInitialDirectory(filesFolder);
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                scriptEngineEnum.equals(ScriptEngineEnum.PYTHON) ? "Python files (*.py)" : "Groovy files (*.groovy)",
                scriptEngineEnum.equals(ScriptEngineEnum.PYTHON) ? "*.py" : "*.groovy");
        fileChooser.getExtensionFilters().add(filter);
        return fileChooser.showOpenDialog(stage);
    }

    private String loadScriptFile(File file) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                return sb.toString();
            } catch (IOException e) {
                log.error("Failed to load file:" + e.getMessage());
            } finally {
                br.close();
            }
        } catch (IOException e) {
            log.error("failed to load the file: " + e.getMessage());
        }
        return null;
    }

    private void installScript(ActionEvent actionEvent) {
        ScriptContext scriptContext = tripsContext.getScriptContext();
        scriptContext.getScriptContextList().add(activeScriptfile);
        MenuItem menuItem = new MenuItem();
        menuItem.setText(activeScriptfile.getName());
        menuItem.setOnAction(event -> {
            if (activeScriptfile.getEngineType().equals(ScriptEngineEnum.PYTHON)) {
                String output = pythonScriptEngine.runAScript(activeScriptfile.getName(), activeScriptfile.getContents());
                log.info(output);
            } else if (activeScriptfile.getEngineType().equals(ScriptEngineEnum.GROOVY)) {
                String output = groovyScriptEngine.runAScript(activeScriptfile.getName(), activeScriptfile.getContents(), new ArrayList<>());
                log.info(output);
            } else {
                log.error("No Engine define for: " + activeScriptfile.getName());
            }
        });
        scriptingMenu.getItems().add(menuItem);
        showInfoMessage("Install script", activeScriptfile.getName() + " installed in Scripting menu");
    }

    private String runAScript(String scriptName, String theScript) {
        return pythonScriptEngine.runAScript(scriptName, theScript);
    }

    private void runScript(ActionEvent actionEvent) {
        if (activeScriptfile != null) {
            if (activeScriptfile.getEngineType().equals(ScriptEngineEnum.PYTHON)) {
                log.info("About to call Python");
                String output = pythonScriptEngine.runAScript(activeScriptfile.getName(), activeScriptfile.getContents());
                outputTextArea.setText(output);
                log.info(output);
            } else {
                if (activeScriptfile.getEngineType().equals(ScriptEngineEnum.GROOVY)) {
                    log.info("About to call Groovy");
                    String output = groovyScriptEngine.runAScript(activeScriptfile.getName(), activeScriptfile.getContents(), new ArrayList<>());
                    outputTextArea.setText(output);
                    log.info(output);
                } else {
                    showErrorAlert("Script Dialog", "No script engine to run");
                }
            }
        }
    }

    private Pane createScriptPane() {
        GridPane scriptBox = new GridPane();
        programTextArea.setWrapText(true);
        scriptBox.add(new Label("Program code"), 0, 0);
        scriptBox.add(programTextArea, 0, 1);
        outputTextArea.setWrapText(true);
        outputTextArea.setDisable(true);
        scriptBox.add(new Label("Log Output"), 1, 0);

        scriptBox.add(outputTextArea, 1, 1);
        return scriptBox;
    }

    private void close(WindowEvent windowEvent) {
        setResult(false);
    }

    private void close(ActionEvent actionEvent) {
        setResult(false);
    }

}
