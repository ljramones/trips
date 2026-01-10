package com.teamgannon.trips.dialogs.sesame;

import com.teamgannon.trips.utility.DialogUtils;
import generated.Resolver;
import generated.Sesame;
import generated.Target;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class SesameNameResolverDialog extends Dialog<List<String>> {

    /**
     * pre-create the list of aliases
     */
    List<String> aliasList = new ArrayList<>();

    private final WebClient client = WebClient.create();

    public final Button runQueryButton = new Button("Find Name");

    public final Button dismissButton = new Button("Dismiss");

    public final TextArea aliasBox = new TextArea();

    /**
     * the URL format string
     */
    String urlTemplate = "http://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/-oxI/~SNVA?%s";

    /**
     * to enter the star to find
     */
    public TextField nameToFindField = new TextField();

    /**
     * ctor
     */
    public SesameNameResolverDialog() {

        this.setTitle("Lookup a star name from the SESAME Name Resolver");

        VBox vBox = new VBox();

        nameToFindField.setMinWidth(200);
        HBox hBox = new HBox(10);
        hBox.setPadding(new Insets(10));
        vBox.getChildren().add(hBox);

        GridPane mainPane = new GridPane();
        mainPane.setMinWidth(600);
        mainPane.add(new Label("Enter name to search:  "), 0, 0);
        mainPane.add(nameToFindField, 1, 0);
        hBox.getChildren().add(mainPane);

        mainPane.add(new Separator(), 0,1, 2,1);

        mainPane.add(new Label("Alias List: "), 0,2,1,1);
        aliasBox.setPrefHeight(100);
        mainPane.add(aliasBox, 1, 2, 1, 1);

        HBox hBox2 = new HBox();
        hBox2.setSpacing(10);
        hBox2.setPadding(new Insets(10));
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        runQueryButton.setOnAction(this::runQueryClicked);
        hBox2.getChildren().add(runQueryButton);

        dismissButton.setOnAction(this::close);
        hBox2.getChildren().add(dismissButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);
    }

    private void close(WindowEvent windowEvent) {
        setResult(aliasList);
    }

    private void close(ActionEvent actionEvent) {
        setResult(aliasList);
    }

    private void runQueryClicked(ActionEvent actionEvent) {

        // wrap the text box
        aliasBox.setWrapText(true);

        // get our star to look up in SESAME
        String starToFind = nameToFindField.getText();

        // create a HTTP request to ask for it
        String urlString = String.format(urlTemplate, starToFind.trim());

        // clreate the webclient request to access it
        WebClient.ResponseSpec responseSpec = client.get()
                .uri(urlString)
                .retrieve();

        // pull the result
        Mono<String> responseMono = responseSpec.bodyToMono(String.class);
        String response = responseMono.block();

        if (response != null) {
            log.info("response is: {}", response);

            try {

                // clear the list in case this is not our first rodeo
                aliasList.clear();

                // setup JAXB parsing
                JAXBContext jaxbContext = JAXBContext.newInstance(Sesame.class);
                InputStream targetStream = new ByteArrayInputStream(response.getBytes());

                // unmarshall the response into the Sesmae class
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Sesame sesame = (Sesame) jaxbUnmarshaller.unmarshal(targetStream);
                log.info(sesame.toString());

                // parse the subclasses looking for aliases and add to the list
                List<Target> targetList = sesame.getTarget();
                for (Target target : targetList) {
                    List<Resolver> resolverList = target.getResolver();
                    for (Resolver resolver : resolverList) {
                        log.info(resolver.getName());
                        List<JAXBElement<?>> jaxbElements = resolver.getINFOOrERROROrOid();
                        for (JAXBElement<?> element : jaxbElements) {
                            String name = element.getName().toString();
                            if (name.equals("alias")) {
                                String alias = (String) element.getValue();
                                aliasList.add(alias);
                                log.info("alias is:" + alias);
                            }
                        }
                    }
                }

                // sort list
                Collections.sort(aliasList);
                log.info("done scanning, alias list=" + aliasList);
                aliasBox.setText(String.join(",\n", aliasList));

            } catch (JAXBException e) {
                showErrorAlert("Sesame lookup", "Failed to parse the incoming xml response");
                log.error("Failed to parse the incoming xml response:" + e.getMessage());
            }
        } else {
            log.warn("Failed to find the star named: " + nameToFindField.getText());
            showErrorAlert("Sesame lookup", "Wasn't able to find that star name:" + nameToFindField.getText());
        }

    }

}
