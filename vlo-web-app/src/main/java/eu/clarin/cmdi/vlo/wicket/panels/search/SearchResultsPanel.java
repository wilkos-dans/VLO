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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.config.FieldValueDescriptor;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermBehavior;
import eu.clarin.cmdi.vlo.wicket.PreferredExplicitOrdering;
import eu.clarin.cmdi.vlo.wicket.model.SearchContextModel;
import eu.clarin.cmdi.vlo.wicket.model.SearchResultExpansionStateModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.AbstractPageableView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that has a data view on the current search results
 *
 * @author twagoo
 */
public class SearchResultsPanel extends GenericPanel<QueryFacetsSelection> {

    public static final Logger log = LoggerFactory.getLogger(SearchResultsPanel.class);
    
    public static final String TRACKING_EVENT_TITLE = "Search page";
    
    public static final List<Long> ITEMS_PER_PAGE_OPTIONS = Arrays.asList(5L, 10L, 25L, 50L, 100L);

    private final DataView<SolrDocument> resultsView;
    private final IModel<Set<Object>> expansionsModel;

    @SpringBean
    private VloConfig vloConfig;

    public SearchResultsPanel(String id, final IModel<QueryFacetsSelection> selectionModel, IDataProvider<SolrDocument> solrDocumentProvider) {
        super(id, selectionModel);
        this.expansionsModel = new Model(new HashSet<Object>());

        add(new Label("title", new SearchResultsTitleModel(selectionModel)));
        
        //define the order for availability values
        final Ordering<String> availabilityOrdering = new PreferredExplicitOrdering(
                //extract the 'primary' availability values from the configuration
                FieldValueDescriptor.valuesList(vloConfig.getAvailabilityValues()));

        
        // data view for search results
        resultsView = new DataView<SolrDocument>("resultItem", solrDocumentProvider, 10) {

            @Override
            protected void populateItem(Item<SolrDocument> item) {
                final long index = (getCurrentPage() * getItemsPerPage()) + item.getIndex();
                final long size = internalGetDataProvider().size();
                final SearchContextModel contextModel = new SearchContextModel(index, size, selectionModel);
                // single result item
                item.add(new SearchResultItemPanel("resultItemDetails", item.getModel(), contextModel,
                        new SearchResultExpansionStateModel(expansionsModel, item.getModel()), availabilityOrdering
                ));
            }
        };
        add(resultsView);

        //For Ajax updating of search results
        setOutputMarkupId(true);

        add(new HighlightSearchTermBehavior() {

            @Override
            protected String getComponentSelector(String componentId) {
                return ".searchresultitem"; //"h3, .searchresultdescription"
            }

            @Override
            protected String getWordList(Component component) {
                return selectionModel.getObject().getQuery();
            }

        });
    }

    public void resetExpansion() {
        expansionsModel.getObject().clear();
    }

    private static class SearchResultsTitleModel extends AbstractReadOnlyModel<String> {

        private final IModel<QueryFacetsSelection> selectionModel;

        public SearchResultsTitleModel(IModel<QueryFacetsSelection> selectionModel) {
            this.selectionModel = selectionModel;
        }

        @Override
        public String getObject() {
            final QueryFacetsSelection selection = selectionModel.getObject();
            if ((selection.getQuery() == null || selection.getQuery().isEmpty())
                    && (selection.getSelection() == null || selection.getSelection().isEmpty())) {
                return "All records";
            } else {
                return "Search results";
            }
        }
    }

    public AbstractPageableView<SolrDocument> getResultsView() {
        return resultsView;
    }
    
    public long getPageCount() {
        return resultsView.getPageCount();
    }
    
    

}
