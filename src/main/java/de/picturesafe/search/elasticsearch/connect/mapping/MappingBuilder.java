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

package de.picturesafe.search.elasticsearch.connect.mapping;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.LanguageSortConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.logging.XcontentToString;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.KEYWORD;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.TEXT;
import static de.picturesafe.search.elasticsearch.connect.mapping.MappingConstants.KEYWORD_FIELD;
import static de.picturesafe.search.elasticsearch.connect.mapping.MappingConstants.MULTILINGUAL_KEYWORD_FIELD;
import static de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils.isTextField;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class MappingBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingBuilder.class);

    private final List<LanguageSortConfiguration> languageSortConfigurations;

    public MappingBuilder(List<LanguageSortConfiguration> languageSortConfigurations) {
        this.languageSortConfigurations = languageSortConfigurations;
    }

    public XContentBuilder build(MappingConfiguration mappingConfiguration) {
        return build(mappingConfiguration.getFieldConfigurations(), !containsFulltextFieldConfiguration(mappingConfiguration));
    }

    private boolean containsFulltextFieldConfiguration(MappingConfiguration mappingConfiguration) {
        return mappingConfiguration.getFieldConfiguration(FieldConfiguration.FIELD_NAME_FULLTEXT) != null;
    }

    public XContentBuilder buildUpdate(List<? extends FieldConfiguration> fieldConfigurations) {
        return build(fieldConfigurations, false);
    }

    private XContentBuilder build(List<? extends FieldConfiguration> fieldConfigurations, boolean addFulltextField) {
        XContentBuilder mapping;
        try {
            mapping = jsonBuilder();
            mapping = mapping.startObject().startObject("properties");

            if (addFulltextField) {
                mapping.startObject(FieldConfiguration.FIELD_NAME_FULLTEXT);
                mapping.field("type", TEXT.getElasticType());
                mapping.endObject();
            }

            addFields(mapping, fieldConfigurations);

            mapping.endObject();
            mapping.endObject();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.debug("Mapping configuration: \n{}", new XcontentToString(mapping));
        return mapping;
    }

    private void addFields(XContentBuilder mapping, List<? extends FieldConfiguration> fieldConfigurations) throws IOException {
        for (final FieldConfiguration fieldConfig : fieldConfigurations) {
            mapping.startObject(fieldConfig.getName());
            if (isTextField(fieldConfig)) {
                if (fieldConfig.isMultilingual()) {
                    addMultilingualTextField(mapping, fieldConfig);
                } else {
                    addTextField(mapping, fieldConfig);
                }
            } else {
                if (fieldConfig.hasInnerFields()) {
                    addObjectField(mapping, fieldConfig);
                } else {
                    mapping.field("type", fieldConfig.getElasticsearchType());
                    addCopyTo(mapping, fieldConfig);
                    addAdditionalParameters(mapping, fieldConfig);
                }
            }

            if (fieldConfig.isWithoutIndexing()) {
                mapping.field("enabled", false);
            }
            if (StringUtils.isNoneBlank(fieldConfig.getAnalyzer())) {
                mapping.field("analyzer", fieldConfig.getAnalyzer());
            }
            mapping.endObject();
        }
    }

    private void addMultilingualTextField(XContentBuilder mapping, FieldConfiguration fieldConfiguration) throws IOException {
        mapping.startObject("properties");

        for (LanguageSortConfiguration languageSortConfiguration : languageSortConfigurations) {
            final String localeSubName = languageSortConfiguration.getLanguage();
            mapping.startObject(localeSubName);
            mapping.field("type", TEXT.getElasticType());
            addCopyTo(mapping, fieldConfiguration);

            if (fieldConfiguration.isSortable() || fieldConfiguration.isAggregatable()) {
                mapping.startObject("fields");
                if (fieldConfiguration.isSortable()) {
                    mapping.startObject(MULTILINGUAL_KEYWORD_FIELD);
                    mapping.field("type", "icu_collation_keyword");
                    mapping.field("language", languageSortConfiguration.getLanguage());
                    mapping.field("country", languageSortConfiguration.getCountry());
                    if (StringUtils.isNotBlank(languageSortConfiguration.getVariant())) {
                        mapping.field("variant", languageSortConfiguration.getVariant());
                    }
                    mapping.endObject();
                }
                if (fieldConfiguration.isAggregatable()) {
                    mapping.startObject(KEYWORD_FIELD);
                    mapping.field("type", KEYWORD.getElasticType());
                    mapping.endObject();
                }
                mapping.endObject();
            }

            addAdditionalParameters(mapping, fieldConfiguration);
            mapping.endObject();
        }

        mapping.endObject();
    }

    private void addTextField(XContentBuilder mapping, FieldConfiguration fieldConfiguration) throws IOException {
        mapping.field("type", fieldConfiguration.getElasticsearchType());
        addCopyTo(mapping, fieldConfiguration);

        if (fieldConfiguration.isAggregatable() || fieldConfiguration.isSortable()) {
            mapping.startObject("fields");
            mapping.startObject(KEYWORD_FIELD);
            mapping.field("type", KEYWORD.getElasticType());
            mapping.endObject();
            mapping.endObject();
        }
        addAdditionalParameters(mapping, fieldConfiguration);
    }

    private void addObjectField(XContentBuilder mapping, FieldConfiguration fieldConfiguration) throws IOException {
        mapping.field("type", fieldConfiguration.getElasticsearchType());
        mapping.startObject("properties");
        addFields(mapping, fieldConfiguration.getInnerFields());
        mapping.endObject();
    }

    private void addCopyTo(XContentBuilder mapping, FieldConfiguration fieldConfiguration) throws IOException {
        if (CollectionUtils.isNotEmpty(fieldConfiguration.getCopyToFields())) {
            mapping.field("copy_to", fieldConfiguration.getCopyToFields());
        }
    }

    private void addAdditionalParameters(XContentBuilder mapping, FieldConfiguration fieldConfiguration) throws IOException {
        if (MapUtils.isNotEmpty(fieldConfiguration.getAdditionalParameters())) {
            for (final Map.Entry<String, Object> entry : fieldConfiguration.getAdditionalParameters().entrySet()) {
                mapping.field(entry.getKey(), entry.getValue());
            }
        }
    }
}
