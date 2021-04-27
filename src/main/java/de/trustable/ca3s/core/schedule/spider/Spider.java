package de.trustable.ca3s.core.schedule.spider;

import com.google.common.collect.ImmutableList;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.schedule.ImportInfo;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Spider {

    Logger LOGGER = LoggerFactory.getLogger(Spider.class);

    private static final int MAX_PAGES_TO_SEARCH = 100;


    /**
     * Our main launching point for the Spider's functionality. Internally it creates spider legs
     * that make an HTTP request and parse the response (the web page).
     *
     * @param url        - The starting point of the spider
     * @param regEx - The word or string that you are searching for
     */
    public Set<String> search(String url, String regEx) {

        Set<String> pagesVisited = new HashSet<String>();
        Set<String> certificateSet = new HashSet<String>();
        List<String> pagesToVisit = new LinkedList<String>();

        Pattern searchPattern = Pattern.compile(regEx);

        while (pagesVisited.size() < MAX_PAGES_TO_SEARCH) {
            String currentUrl;
            SpiderLeg leg = new SpiderLeg();
            if (pagesToVisit.isEmpty()) {
                currentUrl = url;
                pagesVisited.add(url);
            } else {

                String nextUrl;
                do {
                    nextUrl = pagesToVisit.remove(0);
                } while (pagesVisited.contains(nextUrl));
                pagesVisited.add(nextUrl);
                currentUrl = nextUrl;
            }

            leg.crawl(currentUrl, searchPattern, certificateSet); // Lots of stuff happening here. Look at the crawl method in

            pagesToVisit.addAll(leg.getLinks());
        }
        LOGGER.debug("Visited " + pagesVisited.size() + " web page(s), found #"+certificateSet.size()+" different certificates");

        return certificateSet;
    }

}
