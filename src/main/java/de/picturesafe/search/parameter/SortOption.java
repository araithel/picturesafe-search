/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.picturesafe.search.parameter;

import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Representation of a search result sort option
 */
public class SortOption {

    public enum Direction {ASC, DESC}

    private String fieldName;
    private Direction sortDirection = Direction.ASC;
    private Expression filter;

    /**
     * Default constructor
     */
    public SortOption() {
    }

    /**
     * Constructor
     * @param fieldName Field name
     */
    public SortOption(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Constructor
     * @param fieldName Field name
     * @param sortDirection Sort direction
     */
    public SortOption(String fieldName, Direction sortDirection) {
        this.fieldName = fieldName;
        this.sortDirection = sortDirection;
    }

    /**
     * Gets the field name
     * @return Field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name
     * @param fieldName Field name
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the sort direction
     * @return Sort direction
     */
    public Direction getSortDirection() {
        return sortDirection;
    }

    /**
     * Sets the sort direction
     * @param sortDirection Sort direction
     */
    public void setSortDirection(Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * Gets an optional filter expression (for nested objects).
     * @return Filter expression
     */
    public Expression getFilter() {
        return filter;
    }

    /**
     * Sets an optional filter expression (for nested objects).
     * @param filter Filter expression
     */
    public void setFilter(Expression filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("fieldName", fieldName)
                .append("sortDirection", sortDirection)
                .append("filter", filter)
                .toString();
    }
}