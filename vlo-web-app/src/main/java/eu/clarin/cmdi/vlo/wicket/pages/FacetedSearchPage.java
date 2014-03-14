package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.wicket.panels.FacetsPanel;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.panels.FacetPanel;
import eu.clarin.cmdi.vlo.wicket.components.SearchForm;
import eu.clarin.cmdi.vlo.wicket.panels.SearchResultsPanel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * The main search page showing a search form, facets, and search results
 *
 * @author twagoo
 */
public class FacetedSearchPage extends VloBasePage<QueryFacetsSelection> {

    private static final long serialVersionUID = 1L;

    @SpringBean
    private FacetFieldsService facetFieldsService;
    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    private final Panel searchResultsPanel;
    private final Panel facetsPanel;
    private final Panel collectionsPanel;
    private final BreadCrumbPanel breadCrumbPanel;
    
    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);

        final QueryFacetsSelection selection = paramsConverter.fromParameters(parameters);
        final IModel<QueryFacetsSelection> queryModel = new Model<QueryFacetsSelection>(selection);
        setModel(queryModel);
        
        breadCrumbPanel = createBreadCrumbPanel("breadcrumbs", queryModel);
        breadCrumbPanel.setOutputMarkupId(true);
        add(breadCrumbPanel);
        
        final SearchForm searchForm = new SearchForm("search", queryModel);
        add(searchForm);

        collectionsPanel = createCollectionsPanel("collectionsFacet");
        add(collectionsPanel);

        facetsPanel = createFacetsPanel("facets");
        add(facetsPanel);

        searchResultsPanel = new SearchResultsPanel("searchResults", queryModel);
        add(searchResultsPanel);
    }

    private BreadCrumbPanel createBreadCrumbPanel(String id, final IModel<QueryFacetsSelection> queryModel) {
        return new BreadCrumbPanel(id, queryModel) {
            
            @Override
            protected void onValuesUnselected(String facet, Collection<String> valuesRemoved, AjaxRequestTarget target) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    private Panel createCollectionsPanel(final String id) {
        final IModel<QueryFacetsSelection> queryModel = getModel();
        final FacetFieldModel collectionFacetFieldModel = new FacetFieldModel(facetFieldsService, vloConfig.getCollectionFacet(), queryModel);
        final FacetSelectionModel collectionSelectionModel = new FacetSelectionModel(collectionFacetFieldModel, queryModel);
        final FacetPanel panel = new FacetPanel(id, collectionSelectionModel, new Model<ExpansionState>(ExpansionState.COLLAPSED)) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }
        };
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel createFacetsPanel(final String id) {
        final IModel<QueryFacetsSelection> queryModel = getModel();
        final IModel<List<FacetField>> facetFieldsModel = new FacetFieldsModel(facetFieldsService, vloConfig.getFacetFields(), queryModel);
        final FacetsPanel panel = new FacetsPanel(id, facetFieldsModel, queryModel) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }
        };
        panel.setOutputMarkupId(true);
        return panel;
    }

    private void updateSelection(AjaxRequestTarget target) {
        // selection changed, update facets and search results
        target.add(breadCrumbPanel);
        target.add(searchResultsPanel);
        target.add(facetsPanel);
        target.add(collectionsPanel);
    }
}
