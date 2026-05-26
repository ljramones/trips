package com.terranrepublic.infrastructure;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.terranrepublic.assets.Cataloged;

import java.time.Instant;

/**
 * Catalog contract for non-asset transport infrastructure.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "kind"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TransportNode.class, name = "TRANSPORT_NODE"),
        @JsonSubTypes.Type(value = Conduit.class, name = "CONDUIT")
})
public sealed interface SpaceInfrastructure extends Cataloged permits TransportNode, Conduit {

    InfrastructureKind kind();

    Instant createdAt();

    Instant modifiedAt();
}
