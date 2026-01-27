package com.teamgannon.trips.utility;

import generated.Resolver;
import generated.Sesame;
import generated.Target;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SesameResolver {
    /**
     * pre-create the list of aliases
     */
    List<String> aliasList = new ArrayList<>();

    private final WebClient client = WebClient.create();


    /**
     * the URL format string
     */
    String urlTemplate = "http://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/-oxI/~SNVA?%s";

    public List<String> findAliases(String starToFind) {
        aliasList.clear();

        try {

            // create a HTTP request to ask for it
            String urlString = urlTemplate.formatted(starToFind.trim());

            // clreate the webclient request to access it
            WebClient.ResponseSpec responseSpec = client.get()
                    .uri(urlString)
                    .retrieve();

            // pull the result
            Mono<String> responseMono = responseSpec.bodyToMono(String.class);
            String response = responseMono.block();

            if (response != null) {
                log.info("response is: {}", response);

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
            } else {
                log.warn("Failed to find the star named: " + starToFind);

            }
        } catch (Exception e) {
            log.error("Failed to parse the incoming xml response:" + e.getMessage());
        }

        return aliasList;
    }

}
