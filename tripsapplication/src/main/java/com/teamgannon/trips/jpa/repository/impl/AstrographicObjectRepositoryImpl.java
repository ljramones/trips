package com.teamgannon.trips.jpa.repository.impl;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.repository.AstrographicObjectRepositoryCustom;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.stardata.StellarType;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by larrymitchell on 2017-04-19.
 */
@Slf4j
public class AstrographicObjectRepositoryImpl implements AstrographicObjectRepositoryCustom {

    /**
     * the entity manager for getting elements from table
     */
    private final EntityManager em;

    /**
     * the constructor needed for injecting the elasticsearch infrastructure
     */
    public AstrographicObjectRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    /**
     * find the list of AstrographicObject based on a search query
     *
     * @param astroSearchQuery the search query
     * @return the list of AstrographicObjects found
     */
    @Override
    public List<AstrographicObject> findBySearchQuery(AstroSearchQuery astroSearchQuery) {

        // create the criteria builder to start putting all this together
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // create the base criteria query
        CriteriaQuery<AstrographicObject> query = cb.createQuery(AstrographicObject.class);

        // create the root object
        Root<AstrographicObject> astrographicObject = query.from(AstrographicObject.class);

        // setup the list of predicates to apply
        List<Predicate> predicates = makeAstroQuery(astroSearchQuery, astrographicObject, cb);

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(astrographicObject.get("displayName")));

        TypedQuery<AstrographicObject> typedQuery = em.createQuery(query);

        List<AstrographicObject> astrographicObjects = typedQuery.getResultList();

        // spit out the the number of objects found
        log.info("number of objects found = {}", astrographicObjects.size());

        return astrographicObjects;
    }

    private List<Predicate> makeAstroQuery(AstroSearchQuery astroSearchQuery,
                                           Root<AstrographicObject> root,
                                           CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();
        log.info("created the predicates");

        predicates.add(cb.equal(root.get("dataSetName"), astroSearchQuery.getDescriptor().getDataSetName()));

        // create a query with a range limit
        predicates.add(cb.lessThanOrEqualTo(root.get("distance"), astroSearchQuery.getDistanceFromCenterStar()));

        // make a stellar spectral class query
        Set<StellarType> stellarSet = astroSearchQuery.getStellarTypes();

        if (!stellarSet.isEmpty()) {
            List<String> spectralClasses = stellarSet.stream().map(StellarType::getValue).collect(Collectors.toList());
            Expression<String> exp = root.get("orthoSpectralClass");
            Predicate predicate = exp.in(spectralClasses);
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

//        // setup a predicate based on military space types
//        Set<String> politySet = astroSearchQuery.getPolities();
//        if (!politySet.isEmpty()) {
//            List<String> polityList = new ArrayList<>(politySet);
//            Expression<String> exp = root.get("polity");
//            Predicate predicate = exp.in(polityList);
//            predicates.add(predicate);
//        }

//        // setup a predicate based on world types
//        Set<String> worldTypesSet = astroSearchQuery.getWorldTypes();
//        if (!worldTypesSet.isEmpty()) {
//            List<String> worldTypesList = new ArrayList<>(worldTypesSet);
//            Expression<String> exp = root.get("worldType");
//            Predicate predicate = exp.in(worldTypesList);
//            predicates.add(predicate);
//        }

//        // setup a predicate based on fuel types
//        Set<String> fuelTypesSet = astroSearchQuery.getFuelTypes();
//        if (!fuelTypesSet.isEmpty()) {
//            List<String> fuelTypesList = new ArrayList<>(fuelTypesSet);
//            Expression<String> exp = root.get("fuelType");
//            Predicate predicate = exp.in(fuelTypesList);
//            predicates.add(predicate);
//        }

//        // setup a predicate based on space port types
//        Set<String> portTypesSet = astroSearchQuery.getPortTypes();
//        if (!portTypesSet.isEmpty()) {
//            List<String> portTypesList = new ArrayList<>(portTypesSet);
//            Expression<String> exp = root.get("portType");
//            Predicate predicate = exp.in(portTypesList);
//            predicates.add(predicate);
//        }

//        // setup a predicate based on population types
//        Set<String> populationTypesSet = astroSearchQuery.getPopulationTypes();
//        if (!populationTypesSet.isEmpty()) {
//            List<String> populationTypesList = new ArrayList<>(populationTypesSet);
//            Expression<String> exp = root.get("populationType");
//            Predicate predicate = exp.in(populationTypesList);
//            predicates.add(predicate);
//        }

//        // setup a predicate based on tech types
//        Set<String> techTypesSet = astroSearchQuery.getTechTypes();
//        if (!techTypesSet.isEmpty()) {
//            List<String> techTypesList = new ArrayList<>(techTypesSet);
//            Expression<String> exp = root.get("techType");
//            Predicate predicate = exp.in(techTypesList);
//            predicates.add(predicate);
//        }

//        // setup a predicate based on product types
//        Set<String> productTypesSet = astroSearchQuery.getProductTypes();
//        if (!productTypesSet.isEmpty()) {
//            List<String> productTypesList = new ArrayList<>(productTypesSet);
//            Expression<String> exp = root.get("productType");
//            Predicate predicate = exp.in(productTypesList);
//            predicates.add(predicate);
//        }

//        // setup a predicate based on military space types
//        Set<String> milSpaceTypesSet = astroSearchQuery.getMilSpaceTypes();
//        if (!milSpaceTypesSet.isEmpty()) {
//            List<String> milSpaceTypesList = new ArrayList<>(milSpaceTypesSet);
//            Expression<String> exp = root.get("milSpaceType");
//            Predicate predicate = exp.in(milSpaceTypesList);
//            predicates.add(predicate);
//        }

//        // setup a predicate based on military planet types
//        Set<String> milPlanTypesSet = astroSearchQuery.getMilPlanTypes();
//        if (!milPlanTypesSet.isEmpty()) {
//            List<String> milPlanTypesList = new ArrayList<>(milPlanTypesSet);
//            Expression<String> exp = root.get("milPlanType");
//            Predicate predicate = exp.in(milPlanTypesList);
//            predicates.add(predicate);
//        }

        // return the  set of predicated to query on
        return predicates;
    }

}
