package eu.clarin.cmdi.vlo.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.Resources;
import eu.clarin.cmdi.vlo.dao.AutoCompleteDao;
import fiftyfive.wicket.basic.TruncatedLabel;

public class FacetedSearchPage extends BasePage {
    private static final long serialVersionUID = 1L;

    private final SearchPageQuery query;
    private final static AutoCompleteDao autoCompleteDao = new AutoCompleteDao();
    
    /**
     * @param parameters Page parameters
     * @throws SolrServerException
     */
    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);
        query = new SearchPageQuery(parameters);
        addSearchBox();
        addFacetColumns();
        addSearchResults();
    }

    @SuppressWarnings("serial")
    private class SearchBoxForm extends Form<SearchPageQuery> {
        private final AutoCompleteTextField<String> searchBox;

		public SearchBoxForm(String id, SearchPageQuery query) {
			super(id, new CompoundPropertyModel<SearchPageQuery>(query));
			add(new ExternalLink("vloHomeLink", Configuration.getInstance().getVloHomeLink()));
			searchBox = new AutoCompleteTextField<String>("searchQuery") {
				@Override
				protected Iterator<String> getChoices(String input) {
					return autoCompleteDao.getChoices(input).iterator();
				}
			};
            add(searchBox);
            Button submit = new Button("searchSubmit");
            add(submit);
        }

        @Override
        protected void onSubmit() {
            SearchPageQuery query = getModelObject();
            PageParameters pageParameters = query.getPageParameters();
            setResponsePage(FacetedSearchPage.class, pageParameters);
        }
    }

    private void addSearchBox() {
        add(new SearchBoxForm("searchForm", query));
    }

    @SuppressWarnings("serial")
    private void addFacetColumns() {
        GridView<FacetField> facetColumns = new GridView<FacetField>("facetColumns", new SolrFacetDataProvider(query.getSolrQuery()
                .getCopy())) {
            @Override
            protected void populateItem(Item<FacetField> item) {
                item.add(new FacetBoxPanel("facetBox", item.getModel()).create(query));
            }

            @Override
            protected void populateEmptyItem(Item<FacetField> item) {
                item.add(new Label("facetBox", ""));
            }
        };
        facetColumns.setColumns(2);
        add(facetColumns);
    }

    @SuppressWarnings("serial")
    private void addSearchResults() {
        List<IColumn<SolrDocument>> columns = new ArrayList<IColumn<SolrDocument>>();
        columns.add(new AbstractColumn<SolrDocument>(new ResourceModel(Resources.NAME)) {
            
            @Override
            public void populateItem(Item<ICellPopulator<SolrDocument>> cellItem, String componentId, IModel<SolrDocument> rowModel) {
                cellItem.add(new DocumentLinkPanel(componentId, rowModel, query));
            }
        });
        columns.add(new AbstractColumn<SolrDocument>(new ResourceModel(Resources.DESCRIPTION)) {

            @Override
            public void populateItem(Item<ICellPopulator<SolrDocument>> cellItem, String componentId, IModel<SolrDocument> rowModel) {
        	String descriptionText = (String) rowModel.getObject().getFirstValue(FacetConstants.FIELD_DESCRIPTION);
                cellItem.add(new TruncatedLabel(componentId, 100, descriptionText));
                
            }
        });
        AjaxFallbackDefaultDataTable<SolrDocument> searchResultList = new AjaxFallbackDefaultDataTable<SolrDocument>("searchResults", columns,
                new SolrDocumentDataProvider(query.getSolrQuery().getCopy()), 10);

        add(searchResultList);
    }
    
}
