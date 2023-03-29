package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommunicationProtocolPostNormalizer extends AbstractPostNormalizer {

    private final static Logger LOG = LoggerFactory.getLogger(CommunicationProtocolPostNormalizer.class);

    /**
     * Filters/reformats invalid creator information
     * @param value unfiltered protocol information
     * @param cmdiData
     * @return filtered protocol information
     */
    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        System.out.println("[FAIR_A_1_1] normalize["+value+"]");
        if (value == null) {
            return Collections.singletonList(null);
        } else {
            value = value.trim();
            if(value.startsWith("https:")) {
                return Collections.singletonList("true");
            }
        }
        return Collections.singletonList("false");
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
