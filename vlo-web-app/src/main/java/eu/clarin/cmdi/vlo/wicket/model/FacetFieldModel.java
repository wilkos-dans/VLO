/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import java.util.Collections;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Decorator for {@link FacetFieldsModel} for a selection of a single facet.
 *
 * Notice that the actual retrieval is carried out by the provided
 * {@link FacetFieldsService}, which therefore should be configured to actually
 * retrieve the specified facet (through the constructor), otherwise it may not
 * be presented.
 *
 * TODO: Provide some kind of batch retrieval and look up to prevent a call for
 * each instance of this
 *
 * @author twagoo
 */
public class FacetFieldModel extends LoadableDetachableModel<FacetField> {

    //todo: can probably be made more efficient/elegant than wrapping fields model
    private final FacetFieldsModel fieldsModel;

    /**
     *
     * @param service service to use for facet field retrieval
     * @param facetField facet field to provide
     * @param selectionModel model that provides current query/selection
     */
    public FacetFieldModel(FacetFieldsService service, FacetField facetField, IModel<QueryFacetsSelection> selectionModel) {
        super(facetField);
        fieldsModel = new FacetFieldsModel(service, Collections.singletonList(facetField.getName()), selectionModel);
    }

    /**
     *
     * @param service service to use for facet field retrieval
     * @param facet facet to provide
     * @param selectionModel model that provides current query/selection
     */
    public FacetFieldModel(FacetFieldsService service, String facet, IModel<QueryFacetsSelection> selectionModel) {
        fieldsModel = new FacetFieldsModel(service, Collections.singletonList(facet), selectionModel);
    }

    @Override
    protected FacetField load() {
        final List<FacetField> fieldsList = fieldsModel.getObject();
        if (fieldsList == null || fieldsList.isEmpty()) {
            return null;
        } else {
            return fieldsList.get(0);
        }
    }

    @Override
    public void detach() {
        super.detach();
        fieldsModel.detach();
    }
}
