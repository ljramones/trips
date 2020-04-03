package com.teamgannon.trips.elasticsearch.repository;

import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.stardata.StellarType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by larrymitchell on 2017-04-19.
 */
@Slf4j
public class AstrographicObjectRepositoryImpl implements AstrographicObjectRepositoryCustom {

    private final static int MAX_RESULTS = 500;
    private final static int FIRST_PAGE = 0;

    /**
     * link to the elasticsearch engine
     */
    private ElasticsearchOperations operations;

    /**
     * the constructor needed for injecting the elasticsearch infrastructure
     *
     * @param elasticsearchOperations the engine
     */
    public AstrographicObjectRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.operations = elasticsearchOperations;
    }

    /**
     * find the list of AstrographicObject based on a search query
     *
     * @param astroSearchQuery the search query
     * @return the list of AstrographicObjects found
     */
    @Override
    public List<AstrographicObject> findBySearchQuery(AstroSearchQuery astroSearchQuery) {

        // build the master query from the AstroSearchQuery from
        CriteriaQuery query = makeAstroQuery(astroSearchQuery);

        // run the search
        List<AstrographicObject> astrographicObjects = operations.queryForList(query, AstrographicObject.class);

        // spit out the the number of objects found
        log.info("number of objects found = {}", astrographicObjects.size());

        // return it for use
        return astrographicObjects;
    }

    /////////////////////////

    /**
     * create a placeholder for the Criteria query
     *
     * @param criteria the initial query to add
     * @return the created Criteria based query
     */
    private CriteriaQuery createInitialQuery(Criteria criteria) {
        return new CriteriaQuery(criteria);
    }

    /**
     * make a master query for all the elements in astro search query
     *
     * @param astroSearchQuery the master query
     * @return a query builder that connects all the selected items
     */
    private CriteriaQuery makeAstroQuery(AstroSearchQuery astroSearchQuery) {

        CriteriaQuery criteriaQuery = null;

        criteriaQuery = addQuery(criteriaQuery, makeRangeQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeStellarQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeFictionalStarQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeRealStarQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeOtherQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeAnomalyQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makePolityQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeWorldQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeFuelQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makePortQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makePopulationQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeTechQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeProductQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeMilSpaceQuery(astroSearchQuery));

        criteriaQuery = addQuery(criteriaQuery, makeMilPlanQuery(astroSearchQuery));

        return criteriaQuery;
    }

    /**
     * This is a generic helper that connects a new query builder into the existing one but only if
     * it is needed
     *
     * @param criteriaQuery the criteria query if not null
     * @param criteria      the new criteria
     * @return the criteria query
     */
    private CriteriaQuery addQuery(CriteriaQuery criteriaQuery, Criteria criteria) {
        // if criteria is null then we just skip and pass through
        if (criteria != null) {
            if (criteriaQuery == null) {
                // if the criteriaQuery is null then create and fill
                criteriaQuery = new CriteriaQuery(criteria);
            } else {
                // add to an existing criteria query
                criteriaQuery.addCriteria(criteria);
            }
        }
        return criteriaQuery;
    }

    /**
     * create a query with a range limit
     *
     * @param astroSearchQuery the query structure
     * @return the generic query builder
     */
    private Criteria makeRangeQuery(AstroSearchQuery astroSearchQuery) {
        double distance = astroSearchQuery.getDistanceFromCenterStar();
        return new Criteria("distance").lessThanEqual(distance);
    }

    /**
     * make a stellar spectral class query
     *
     * @param astroSearchQuery the query structure
     * @return the generic query builder
     */
    private Criteria makeStellarQuery(AstroSearchQuery astroSearchQuery) {
        Set<StellarType> stellarSet = astroSearchQuery.getStellarTypes();
        if (stellarSet.isEmpty()) {
            // no queries for this
            return null;
        } else {
            List<String> spectralClasses = stellarSet.stream().map(StellarType::getValue).collect(Collectors.toList());
            return new Criteria("spectralClass").in(spectralClasses);
        }
    }

    private Criteria makeFictionalStarQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("starType").contains("fictional");
    }

    private Criteria makeRealStarQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("starType").contains("real");
    }

    private Criteria makeOtherQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("other").contains("true");
    }

    private Criteria makeAnomalyQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("anomaly").contains("true");
    }

    private Criteria makePolityQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("polity").in(astroSearchQuery.getPolities());
    }

    private Criteria makeWorldQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("worldType").in(astroSearchQuery.getWorldTypes());
    }

    private Criteria makeFuelQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("fuelType").in(astroSearchQuery.getFuelTypes());
    }

    private Criteria makePortQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("portType").in(astroSearchQuery.getPortTypes());
    }

    private Criteria makePopulationQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("populationType").in(astroSearchQuery.getPopulationTypes());
    }

    private Criteria makeTechQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("techType").in(astroSearchQuery.getTechTypes());
    }

    private Criteria makeProductQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("productType").in(astroSearchQuery.getProductTypes());
    }

    private Criteria makeMilSpaceQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("milSpaceType").in(astroSearchQuery.getMilSpaceTypes());
    }

    private Criteria makeMilPlanQuery(AstroSearchQuery astroSearchQuery) {
        return new Criteria("milPlanType").in(astroSearchQuery.getMilPlanTypes());
    }

}
