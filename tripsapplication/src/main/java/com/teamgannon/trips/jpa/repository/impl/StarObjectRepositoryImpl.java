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

import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
        List<StarObject> results = typedQuery.getResultList();
        log.info("number of records found={}", results.size());
        return results;
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
        // Use efficient count query instead of loading all results
        long totalRows = countBySearchQuery(astroSearchQuery);
        log.info("number of records found={}", totalRows);

        TypedQuery<StarObject> typedQuery = getStarObjectTypedQueryWithSort(astroSearchQuery, page.getSort());
        typedQuery.setFirstResult(page.getPageNumber() * page.getPageSize());
        typedQuery.setMaxResults(page.getPageSize());
        return new PageImpl<>(typedQuery.getResultList(), page, totalRows);
    }

    /**
     * count the number of stars matching a search query
     *
     * @param astroSearchQuery the astro query
     * @return the count of matching stars
     */
    @Override
    public long countBySearchQuery(AstroSearchQuery astroSearchQuery) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<StarObject> root = countQuery.from(StarObject.class);

        List<Predicate> predicates = makeAstroQuery(astroSearchQuery, root, cb);
        countQuery.select(cb.count(root));
        countQuery.where(predicates.toArray(new Predicate[0]));

        return em.createQuery(countQuery).getSingleResult();
    }


    /**
     * Get star objects that match query as a Java 8 stream.
     * <p>
     * Note: The stream must be consumed within the same transaction context.
     * Use try-with-resources to ensure proper cleanup.
     *
     * @param astroSearchQuery the astro query
     * @return the stream of objects
     */
    public Stream<StarObject> findBySearchQueryStream(@NotNull AstroSearchQuery astroSearchQuery) {
        // Use the dedicated count query instead of consuming the result list
        long totalRows = countBySearchQuery(astroSearchQuery);
        log.info("number of records found={}", totalRows);

        // Create a fresh query for streaming (the count query consumed the previous one conceptually)
        TypedQuery<StarObject> typedQuery = getStarObjectTypedQuery(astroSearchQuery);
        return typedQuery.getResultStream();
    }


    private TypedQuery<StarObject> getStarObjectTypedQuery(@NotNull AstroSearchQuery astroSearchQuery) {
        return getStarObjectTypedQueryWithSort(astroSearchQuery, Sort.by("displayName"));
    }

    private TypedQuery<StarObject> getStarObjectTypedQueryWithSort(@NotNull AstroSearchQuery astroSearchQuery, Sort sort) {
        // create the criteria builder to start putting all this together
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // create the base criteria query
        CriteriaQuery<StarObject> query = cb.createQuery(StarObject.class);

        // create the root object
        Root<StarObject> astrographicObject = query.from(StarObject.class);

        // setup the list of predicates to apply
        List<Predicate> predicates = makeAstroQuery(astroSearchQuery, astrographicObject, cb);

        query.where(predicates.toArray(new Predicate[0]));

        // Apply Sort from Pageable (instead of hardcoded displayName)
        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : sort) {
                try {
                    Path<?> path = astrographicObject.get(sortOrder.getProperty());
                    orders.add(sortOrder.isAscending() ? cb.asc(path) : cb.desc(path));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid sort property: {}", sortOrder.getProperty());
                }
            }
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            } else {
                // Default fallback
                query.orderBy(cb.asc(astrographicObject.get("displayName")));
            }
        } else {
            // Default sort
            query.orderBy(cb.asc(astrographicObject.get("displayName")));
        }

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
        if (astroSearchQuery.getUpperDistanceLimit() > 0) {
            Predicate nonZeroDistance = cb.greaterThan(root.get("distance"), 0.0);
            Predicate solByDisplayName = cb.equal(root.get("displayName"), "Sol");
            Predicate solByCommonName = cb.equal(root.get("commonName"), "Sol");
            predicates.add(cb.or(nonZeroDistance, solByDisplayName, solByCommonName));
        }

        // make a stellar spectral class query
        Set<StellarType> stellarSet = astroSearchQuery.getStellarTypes();

        if (!stellarSet.isEmpty()) {
            List<String> spectralClasses = stellarSet.stream().map(StellarType::getValue).collect(Collectors.toList());
            Expression<String> exp = root.get("orthoSpectralClass");
            Expression<String> subString = cb.substring(exp, 1, 1);
            Predicate predicate = subString.in(spectralClasses);
            predicates.add(predicate);
        }

        // Spectral component filtering (Chuck's special query)
        // Allows filtering by class letter, subtype, and luminosity class separately
        if (astroSearchQuery.hasSpectralComponentFilter()) {
            List<Predicate> spectralPredicates = new ArrayList<>();
            Expression<String> spectralClassExp = root.get("spectralClass");

            // Filter by spectral class letter (O, B, A, F, G, K, M, etc.)
            Set<String> classLetters = astroSearchQuery.getSpectralClassLetters();
            if (!classLetters.isEmpty()) {
                // Build OR predicate for class letters: spectralClass LIKE 'O%' OR spectralClass LIKE 'B%' ...
                List<Predicate> classPredicates = new ArrayList<>();
                for (String letter : classLetters) {
                    classPredicates.add(cb.like(spectralClassExp, letter + "%"));
                }
                if (!classPredicates.isEmpty()) {
                    spectralPredicates.add(cb.or(classPredicates.toArray(new Predicate[0])));
                }
            }

            // Filter by subtype (0-9)
            Set<String> subtypes = astroSearchQuery.getSpectralSubtypes();
            if (!subtypes.isEmpty()) {
                // Build regex-like pattern to match subtypes at position 2 (or after multi-char class)
                // We use LIKE patterns: spectralClass LIKE '_0%' OR spectralClass LIKE '_1%' ...
                // For multi-char classes, use additional patterns
                List<Predicate> subtypePredicates = new ArrayList<>();
                for (String subtype : subtypes) {
                    // Single char class (e.g., G2V): spectralClass LIKE '_X%' where X is subtype
                    subtypePredicates.add(cb.like(spectralClassExp, "_" + subtype + "%"));
                    // Two char class (e.g., DA2): spectralClass LIKE '__X%'
                    subtypePredicates.add(cb.like(spectralClassExp, "__" + subtype + "%"));
                }
                if (!subtypePredicates.isEmpty()) {
                    spectralPredicates.add(cb.or(subtypePredicates.toArray(new Predicate[0])));
                }
            }

            // Filter by luminosity class (I, II, III, IV, V, VI, VII)
            Set<String> lumClasses = astroSearchQuery.getLuminosityClasses();
            if (!lumClasses.isEmpty()) {
                // Build OR predicate for luminosity: spectralClass LIKE '%V' OR spectralClass LIKE '%III' ...
                // Note: Order matters - check longer patterns first (VII before VI before V)
                List<Predicate> lumPredicates = new ArrayList<>();
                // Sort by length descending to avoid partial matches (VII vs V)
                List<String> sortedLumClasses = lumClasses.stream()
                        .sorted((a, b) -> b.length() - a.length())
                        .collect(Collectors.toList());
                for (String lum : sortedLumClasses) {
                    lumPredicates.add(cb.like(spectralClassExp, "%" + lum));
                }
                if (!lumPredicates.isEmpty()) {
                    spectralPredicates.add(cb.or(lumPredicates.toArray(new Predicate[0])));
                }
            }

            // All spectral component predicates must match (AND)
            if (!spectralPredicates.isEmpty()) {
                predicates.add(cb.and(spectralPredicates.toArray(new Predicate[0])));
            }
        }

        // create a query with a real star type
        if (astroSearchQuery.isRealStars()) {
            predicates.add(cb.isTrue(root.get("realStar")));
        } else {
            predicates.add(cb.isFalse(root.get("realStar")));
        }

        // setup a predicate based on other is true (field now in worldBuilding embedded object)
        if (astroSearchQuery.isOtherSearch()) {
            predicates.add(cb.isTrue(root.get("worldBuilding").get("other")));
        } else {
            predicates.add(cb.isFalse(root.get("worldBuilding").get("other")));
        }

        // setup a predicate based on anomaly is true (field now in worldBuilding embedded object)
        if (astroSearchQuery.isAnomalySearch()) {
            predicates.add(cb.isTrue(root.get("worldBuilding").get("anomaly")));
        } else {
            predicates.add(cb.isFalse(root.get("worldBuilding").get("anomaly")));
        }


        // setup a predicate based on polities (field now in worldBuilding embedded object)
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
            Expression<String> exp = root.get("worldBuilding").get("polity");
            // setup predicates for actual matches
            Predicate setPredicate = null;
            if (polityList.size() > 0) {
                log.info("polities:" + polityList);
                setPredicate = exp.in(polityList);
                predicates.add(setPredicate);
            }
        }


        // setup a predicate based on world types (field now in worldBuilding embedded object)
        Set<String> worldTypesSet = astroSearchQuery.getWorldTypes();
        if (!worldTypesSet.isEmpty()) {
            List<String> worldTypesList = new ArrayList<>(worldTypesSet);
            Expression<String> exp = root.get("worldBuilding").get("worldType");
            Predicate predicate = exp.in(worldTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on fuel types (field now in worldBuilding embedded object)
        Set<String> fuelTypesSet = astroSearchQuery.getFuelTypes();
        if (!fuelTypesSet.isEmpty()) {
            List<String> fuelTypesList = new ArrayList<>(fuelTypesSet);
            Expression<String> exp = root.get("worldBuilding").get("fuelType");
            Predicate predicate = exp.in(fuelTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on space port types (field now in worldBuilding embedded object)
        Set<String> portTypesSet = astroSearchQuery.getPortTypes();
        if (!portTypesSet.isEmpty()) {
            List<String> portTypesList = new ArrayList<>(portTypesSet);
            Expression<String> exp = root.get("worldBuilding").get("portType");
            Predicate predicate = exp.in(portTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on population types (field now in worldBuilding embedded object)
        Set<String> populationTypesSet = astroSearchQuery.getPopulationTypes();
        if (!populationTypesSet.isEmpty()) {
            List<String> populationTypesList = new ArrayList<>(populationTypesSet);
            Expression<String> exp = root.get("worldBuilding").get("populationType");
            Predicate predicate = exp.in(populationTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on tech types (field now in worldBuilding embedded object)
        Set<String> techTypesSet = astroSearchQuery.getTechTypes();
        if (!techTypesSet.isEmpty()) {
            List<String> techTypesList = new ArrayList<>(techTypesSet);
            Expression<String> exp = root.get("worldBuilding").get("techType");
            Predicate predicate = exp.in(techTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on product types (field now in worldBuilding embedded object)
        Set<String> productTypesSet = astroSearchQuery.getProductTypes();
        if (!productTypesSet.isEmpty()) {
            List<String> productTypesList = new ArrayList<>(productTypesSet);
            Expression<String> exp = root.get("worldBuilding").get("productType");
            Predicate predicate = exp.in(productTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on military space types (field now in worldBuilding embedded object)
        Set<String> milSpaceTypesSet = astroSearchQuery.getMilSpaceTypes();
        if (!milSpaceTypesSet.isEmpty()) {
            List<String> milSpaceTypesList = new ArrayList<>(milSpaceTypesSet);
            Expression<String> exp = root.get("worldBuilding").get("milSpaceType");
            Predicate predicate = exp.in(milSpaceTypesList);
            predicates.add(predicate);
        }

        // setup a predicate based on military planet types (field now in worldBuilding embedded object)
        Set<String> milPlanTypesSet = astroSearchQuery.getMilPlanTypes();
        if (!milPlanTypesSet.isEmpty()) {
            List<String> milPlanTypesList = new ArrayList<>(milPlanTypesSet);
            Expression<String> exp = root.get("worldBuilding").get("milPlanType");
            Predicate predicate = exp.in(milPlanTypesList);
            predicates.add(predicate);
        }

        // return the  set of predicated to query on
        return predicates;
    }

}
