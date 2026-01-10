package com.teamgannon.trips.jpa.repository.impl;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.StarObjectRepositoryCustom;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.stellarmodelling.StellarType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by larrymitchell on 2017-04-19.
 */
@Slf4j
public class StarObjectRepositoryImpl implements StarObjectRepositoryCustom {

    /**
     * the entity manager for getting elements from table
     */
    private final EntityManager em;

    /**
     * the constructor needed for injecting the elasticsearch infrastructure
     */
    public StarObjectRepositoryImpl(EntityManager em) {
        this.em = em;
    }


    /**
     * find the list of AstrographicObject based on a search query
     *
     * @param astroSearchQuery the search query
     * @return the list of AstrographicObjects found
     */
    @Override
    public List<StarObject> findBySearchQuery(@NotNull AstroSearchQuery astroSearchQuery) {

        TypedQuery<StarObject> typedQuery = getStarObjectTypedQuery(astroSearchQuery);
        int totalRows = typedQuery.getResultList().size();
        log.info("number of records found={}", totalRows);

        return typedQuery.getResultList();
    }

    /**
     * get the star objects via a query as a series of pages
     *
     * @param astroSearchQuery the astro query
     * @param page             the page we want
     * @return the page of objects
     */
    @Override
    public Page<StarObject> findBySearchQueryPaged(AstroSearchQuery astroSearchQuery, Pageable page) {
        TypedQuery<StarObject> typedQuery = getStarObjectTypedQuery(astroSearchQuery);
        int totalRows = typedQuery.getResultList().size();
        log.info("number of records found={}", totalRows);

        typedQuery.setFirstResult(page.getPageNumber() * page.getPageSize());
        typedQuery.setMaxResults(page.getPageSize());
        return new PageImpl<>(typedQuery.getResultList(), page, totalRows);
    }


    /**
     * get star objects that match query as a Java 8 stream
     *
     * @param astroSearchQuery the astro query the query
     * @return the stream of objects
     * @TODO: 2021-02-24  Currently this fails on first read. No idea why. Don't use until I figure it out
     */
    public Stream<StarObject> findBySearchQueryStream(@NotNull AstroSearchQuery astroSearchQuery) {
        TypedQuery<StarObject> typedQuery = getStarObjectTypedQuery(astroSearchQuery);
        int totalRows = typedQuery.getResultList().size();
        log.info("number of records found={}", totalRows);

        return typedQuery.getResultStream();
    }


    private TypedQuery<StarObject> getStarObjectTypedQuery(@NotNull AstroSearchQuery astroSearchQuery) {
        // create the criteria builder to start putting all this together
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // create the base criteria query
        CriteriaQuery<StarObject> query = cb.createQuery(StarObject.class);

        // create the root object
        Root<StarObject> astrographicObject = query.from(StarObject.class);

        // setup the list of predicates to apply
        List<Predicate> predicates = makeAstroQuery(astroSearchQuery, astrographicObject, cb);

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(astrographicObject.get("displayName")));

        return em.createQuery(query);
    }


    private @NotNull List<Predicate> makeAstroQuery(@NotNull AstroSearchQuery astroSearchQuery,
                                                    @NotNull Root<StarObject> root,
                                                    @NotNull CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();
        log.info("created the predicates");

        predicates.add(cb.equal(root.get("dataSetName"), astroSearchQuery.getDataSetContext().getDescriptor().getDataSetName()));

        // create a query with a range limit
        predicates.add(cb.lessThanOrEqualTo(root.get("distance"), astroSearchQuery.getUpperDistanceLimit()));

        predicates.add(cb.greaterThanOrEqualTo(root.get("distance"), astroSearchQuery.getLowerDistanceLimit()));

        // make a stellar spectral class query
        Set<StellarType> stellarSet = astroSearchQuery.getStellarTypes();

        if (!stellarSet.isEmpty()) {
            List<String> spectralClasses = stellarSet.stream().map(StellarType::getValue).collect(Collectors.toList());
            Expression<String> exp = root.get("orthoSpectralClass");
            Expression<String> subString = cb.substring(exp, 1, 1);
            Predicate predicate = subString.in(spectralClasses);
            predicates.add(predicate);
        }

        // create a query with a real star type
        if (astroSearchQuery.isRealStars()) {
            predicates.add(cb.isTrue(root.get("realStar")));
        } else {
            predicates.add(cb.isFalse(root.get("realStar")));
        }

        // setup a predicate based on other is true
        if (astroSearchQuery.isOtherSearch()) {
            predicates.add(cb.isTrue(root.get("other")));
        } else {
            predicates.add(cb.isFalse(root.get("other")));
        }

        // setup a predicate based on anomaly is true
        if (astroSearchQuery.isAnomalySearch()) {
            predicates.add(cb.isTrue(root.get("anomaly")));
        } else {
            predicates.add(cb.isFalse(root.get("anomaly")));
        }


        // setup a predicate based on military space types
        Set<String> politySet = astroSearchQuery.getPolities();
        if (!politySet.isEmpty()) {
            boolean noneSelected = politySet.stream().anyMatch(polity -> polity.equalsIgnoreCase("none"));
            List<String> polityList = politySet.stream()
                    .filter(polity -> !polity.equalsIgnoreCase("none"))
                    .collect(Collectors.toList());
            if (noneSelected) {
                log.info("None selected");
                polityList.add("NA");
            }
            Expression<String> exp = root.get("polity");
            // setup predicates for actual matches
            Predicate setPredicate = null;
            if (polityList.size() > 0) {
                log.info("polities:" + polityList);
                setPredicate = exp.in(polityList);
                predicates.add(setPredicate);
            }
            // set predicate for empty polity accepted if it was selected
//            if (noneSelected) {
//                log.info("None selected");
//                Predicate nonePredicate = cb.equal(root.get("polity"), "Terran");
//                predicates.add(nonePredicate);
////                if (setPredicate != null) {
////                    Predicate jointPredicate = cb.or(setPredicate, nonePredicate);
////                }
//            }
        }


        // setup a predicate based on world types
        Set<String> worldTypesSet = astroSearchQuery.getWorldTypes();
        if (!worldTypesSet.isEmpty()) {
            List<String> worldTypesList = new ArrayList<>(worldTypesSet);
            Expression<String> exp = root.get("worldType");
            Predicate predicate = exp.in(worldTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on fuel types
        Set<String> fuelTypesSet = astroSearchQuery.getFuelTypes();
        if (!fuelTypesSet.isEmpty()) {
            List<String> fuelTypesList = new ArrayList<>(fuelTypesSet);
            Expression<String> exp = root.get("fuelType");
            Predicate predicate = exp.in(fuelTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on space port types
        Set<String> portTypesSet = astroSearchQuery.getPortTypes();
        if (!portTypesSet.isEmpty()) {
            List<String> portTypesList = new ArrayList<>(portTypesSet);
            Expression<String> exp = root.get("portType");
            Predicate predicate = exp.in(portTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on population types
        Set<String> populationTypesSet = astroSearchQuery.getPopulationTypes();
        if (!populationTypesSet.isEmpty()) {
            List<String> populationTypesList = new ArrayList<>(populationTypesSet);
            Expression<String> exp = root.get("populationType");
            Predicate predicate = exp.in(populationTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on tech types
        Set<String> techTypesSet = astroSearchQuery.getTechTypes();
        if (!techTypesSet.isEmpty()) {
            List<String> techTypesList = new ArrayList<>(techTypesSet);
            Expression<String> exp = root.get("techType");
            Predicate predicate = exp.in(techTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on product types
        Set<String> productTypesSet = astroSearchQuery.getProductTypes();
        if (!productTypesSet.isEmpty()) {
            List<String> productTypesList = new ArrayList<>(productTypesSet);
            Expression<String> exp = root.get("productType");
            Predicate predicate = exp.in(productTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on military space types
        Set<String> milSpaceTypesSet = astroSearchQuery.getMilSpaceTypes();
        if (!milSpaceTypesSet.isEmpty()) {
            List<String> milSpaceTypesList = new ArrayList<>(milSpaceTypesSet);
            Expression<String> exp = root.get("milSpaceType");
            Predicate predicate = exp.in(milSpaceTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on military planet types
        Set<String> milPlanTypesSet = astroSearchQuery.getMilPlanTypes();
        if (!milPlanTypesSet.isEmpty()) {
            List<String> milPlanTypesList = new ArrayList<>(milPlanTypesSet);
            Expression<String> exp = root.get("milPlanType");
            Predicate predicate = exp.in(milPlanTypesList);
            predicates.add(predicate);
        }

        // return the  set of predicated to query on
        return predicates;
    }

}
