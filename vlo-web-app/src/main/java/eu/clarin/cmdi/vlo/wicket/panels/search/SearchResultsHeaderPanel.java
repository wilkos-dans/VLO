/*
 * Copyright (C) 2016 CLARIN
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

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.AbstractPageableView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class SearchResultsHeaderPanel extends GenericPanel<QueryFacetsSelection> {

    private final IDataProvider<SolrDocument> solrDocumentProvider;
    private final AbstractPageableView<SolrDocument> resultsView;
    private final AjaxPagingNavigator navigatorTop;

    public SearchResultsHeaderPanel(String id, IModel<QueryFacetsSelection> model, AbstractPageableView<SolrDocument> resultsView, IDataProvider<SolrDocument> solrDocumentProvider) {
        super(id, model);

        this.solrDocumentProvider = solrDocumentProvider;
        this.resultsView = resultsView;

        // total result counter
        add(createResultCount("resultCount"));

        // page result indicater
        add(createResultPageIndicator("resultPageIndicator", resultsView));

        // navigator in header
        add(navigatorTop = new AjaxPagingNavigator("pagingTop", resultsView));
        
        //For Ajax updating of search results
        setOutputMarkupId(true);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        navigatorTop.setVisible(resultsView.getPageCount() > 1);

    }

    private Label createResultCount(String id) {
        final IModel<String> resultCountModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return String.format("%d results", solrDocumentProvider.size());
            }
        };
        return new Label(id, resultCountModel);
    }

    private Label createResultPageIndicator(String id, final IPageableItems resultsView) {
        IModel<String> indicatorModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                final long firstShown = 1 + resultsView.getCurrentPage() * resultsView.getItemsPerPage();
                final long lastShown = Math.min(resultsView.getItemCount(), firstShown + resultsView.getItemsPerPage() - 1);
                return String.format("Showing %d to %d", firstShown, lastShown);
            }
        };
        return new Label(id, indicatorModel) {

            @Override
            protected void onConfigure() {
                // hide if no results
                setVisible(resultsView.getItemCount() > 0);
            }

        };
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        solrDocumentProvider.detach();
    }

}
