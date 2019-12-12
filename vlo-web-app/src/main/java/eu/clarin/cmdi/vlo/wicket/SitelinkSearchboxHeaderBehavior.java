/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Structured data to provide a search box in a search engine's search results
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see https://developers.google.com/search/docs/data-types/sitelinks-searchbox
 */
public class SitelinkSearchboxHeaderBehavior extends Behavior {

    private final static String QUERY_SUFFIX = "?" + VloWebAppParameters.QUERY + "={search_term_string}";
    private final static PageParameters EMPTY_PAGE_PARAMETERS = new PageParameters();

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        final UrlRenderer urlRenderer = component.getRequestCycle().getUrlRenderer();

        final String baseUrl
                = urlRenderer.renderFullUrl(Url.parse(component.urlFor(component.getApplication().getHomePage(), EMPTY_PAGE_PARAMETERS)));

        final String searchTargetUrl
                = urlRenderer.renderFullUrl(Url.parse(component.urlFor(FacetedSearchPage.class, EMPTY_PAGE_PARAMETERS))) + QUERY_SUFFIX;

        final String script = "<script type=\"application/ld+json\">\n"
                + "/*<![CDATA[*/\n"
                + "{\n"
                + "  \"@context\": \"https://schema.org\",\n"
                + "  \"@type\": \"WebSite\",\n"
                + "  \"url\": \"" + baseUrl + "\",\n"
                + "  \"potentialAction\": {\n"
                + "    \"@type\": \"SearchAction\",\n"
                + "    \"target\": \"" + searchTargetUrl + "\",\n"
                + "    \"query-input\": \"required name=search_term_string\"\n"
                + "  }\n"
                + "}\n"
                + "/*]]>*/\n"
                + "</script>\n";

        response.render(new StringHeaderItem(script));
    }

}