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

package de.picturesafe.search.elasticsearch.config;

import de.picturesafe.search.elasticsearch.config.util.IndexPresetConfigurationDocumentBuilder;
import de.picturesafe.search.elasticsearch.model.IndexObject;

import java.util.List;
import java.util.Map;

/**
 * Presets for index creation
 */
public interface IndexPresetConfiguration extends IndexObject<IndexPresetConfiguration> {

    /**
     * Gets the alias name.
     * @return Alias name
     */
    String getIndexAlias();

    /**
     * Gets the number of shards.
     * @return Number of shards
     */
    int getNumberOfShards();

    /**
     * Gets the number of replicas.
     * @return Number of replicas
     */
    int getNumberOfReplicas();

    /**
     * Gets the maximum result window size (maximum number of hits in a search result).
     * @return Maximum result window size
     */
    int getMaxResultWindow();

    /**
     * Gets the maximum number of fields in the index.
     * @return Maximum number of fields in the index (null = use default)
     */
    Integer getFieldsLimit();

    /**
     * Checks if compression should be used.
     * @return TRUE if compression should be used
     */
    boolean isUseCompression();

    /**
     * Gets optional character mappings (e.g. for mapping umlauts to latin character sequences).
     * @return Optional character mappings
     */
    Map<String, String> getCharMappings();

    /**
     * Gets optional custom tokenizers.
     * @return Custom tokenizers
     */
    List<IndexSettingsObject> getCustomTokenizers();

    /**
     * Gets optional custom analyzers.
     * @return Custom analyzers
     */
    List<IndexSettingsObject> getCustomAnalyzers();

    /**
     * Creates a new index name based on the given alias name.
     * @return New index name
     */
    String createNewIndexName();

    @Override
    default Map<String, Object> toDocument() {
        return IndexPresetConfigurationDocumentBuilder.build(this);
    }

    @Override
    default IndexPresetConfiguration fromDocument(Map<String, Object> document) {
        final String className = IndexObject.classNameFromDocument(document);
        try {
            return ((IndexPresetConfiguration) Class.forName(className).newInstance()).internalFromDocument(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    IndexPresetConfiguration internalFromDocument(Map<String, Object> document);
}
