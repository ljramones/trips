package com.terranrepublic.assets;

/**
 * Shared identity and provenance contract for anything listed in a Terran Republic catalog.
 */
public interface Cataloged {

    String id();

    String name();

    String source();

    String faction();

    boolean concealed();

    String description();
}
