package cds.savot.model;

//Copyright 2002-2014 - UDS/CNRS
//The SAVOT library is distributed under the terms
//of the GNU General Public License version 3.
//
//This file is part of SAVOT.
//
//SAVOT is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, version 3 of the License.
//
//SAVOT is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//The GNU General Public License is available in COPYING file
//along with SAVOT.
//
//SAVOT - Simple Access to VOTable - Parser
//
//Author, Co-Author:  Andre Schaaff (CDS), Laurent Bourges (JMMC)
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Generic class for other set classes
 * </p>
 *
 * @param <E> element type
 * @author Andre Schaaff
 * @see SavotSet
 */
public class SavotSet<E> {

    /** storage of the set elements */
    public ArrayList<E> set = null;

    /**
     * Constructor
     */
    public SavotSet() {
    }

    /**
     * Add an item to the set
     * 
     * @param item
     */
    public final void addItem(final E item) {
        if (set == null) {
            set = new ArrayList<E>();
        }
        set.add(item);
    }

    /**
     * Get an item at a given position (index)
     * 
     * @param index
     * @return Object
     */
    public final E getItemAt(final int index) {
        if (set == null) {
            return null;
        }
        if (index >= 0 && index < set.size()) {
            return set.get(index);
        }
        return null;
    }

    /**
     * Remove an item at a given position (index)
     * 
     * @param index
     */
    public final void removeItemAt(final int index) {
        if (set != null && index >= 0 && index < set.size()) {
            set.remove(index);
        }
    }

    /**
     * Remove all items
     */
    public final void removeAllItems() {
        if (set != null && !set.isEmpty()) {
            set.clear();
        }
    }

    /**
     * Set the whole set to a given set
     * 
     * @param set
     */
    public final void setItems(final ArrayList<E> set) {
        this.set = set;
    }

    /**
     * Get the whole set
     * 
     * @return a ArrayList
     */
    public final List<E> getItems() {
        return set;
    }

    /**
     * Get the number of items
     * 
     * @return int
     */
    public final int getItemCount() {
        if (set != null) {
            return set.size();
        }
        return 0;
    }

    /**
     * Increases the capacity of this <tt>SavotSet</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    public final void ensureCapacity(final int minCapacity) {
        if (set == null) {
            set = new ArrayList<E>(minCapacity);
        } else {
            set.ensureCapacity(minCapacity);
        }
    }

    /**
     * Trims the capacity of this <tt>SavotSet</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>SavotSet</tt> instance.
     */
    public final void trim() {
        if (set != null) {
            set.trimToSize();
        }
    }
}
