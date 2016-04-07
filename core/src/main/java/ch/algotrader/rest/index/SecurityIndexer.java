/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/

package ch.algotrader.rest.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.stereotype.Service;

import ch.algotrader.UnrecoverableCoreException;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityVO;

/**
 * @author <a href="mailto:vgolding@algotrader.ch">Vince Golding</a>
 */
@Service
public class SecurityIndexer {

    private final static String[] FIELDS = {"isin", "bbgid", "description", "symbol", "lmaxid", "ric", "id", "securityFamilyId", "underlyingId", "securityId"};

    private final Directory index;
    private final ConcurrentMap<Long, SecurityVO> securityCache;

    public SecurityIndexer() {
        this.index = new RAMDirectory();
        this.securityCache = new ConcurrentHashMap<>();
    }

    public void init(final Collection<Security> securities) {
        this.securityCache.clear();
        buildIndex(securities);
        this.securityCache.putAll(securities.stream()
                .map(Security::convertToVO)
                .collect(Collectors.toMap(SecurityVO::getId, Function.identity())));
    }

    private void buildIndex(Collection<Security> securities) {
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter iwriter = new IndexWriter(index, config)) {
            Collection<Collection<IndexableField>> securityDocuments = securities
                    .stream()
                    .map(Security::convertToVO)
                    .map(this::createDocument)
                    .collect(Collectors.toList());

            iwriter.addDocuments(securityDocuments);
        } catch (IOException ex) {
            throw new UnrecoverableCoreException("Unexpected I/O error building security index", ex);
        }
    }

    public List<SecurityVO> search(String queryStr) throws ParseException {
        try (IndexReader reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser queryParser = new MultiFieldQueryParser(FIELDS, new StandardAnalyzer());
            queryParser.setAllowLeadingWildcard(true);
            Query query = queryParser.parse(queryStr);

            TopDocs results = searcher.search(query, 10);
            return Arrays.asList(results.scoreDocs)
                    .stream()
                    .map(sd -> searchDocument(searcher, sd))
                    .mapToLong(d -> d.getField("id").numericValue().longValue())
                    .mapToObj(securityCache::get)
                    .collect(Collectors.toList());

        } catch (IOException ioe) {
            throw new UnrecoverableCoreException("Unexpected I/O error accessing security index", ioe);
        }
    }

    private Document searchDocument(IndexSearcher searcher, ScoreDoc scoreDoc) {
        try {
            return searcher.doc(scoreDoc.doc);
        } catch (IOException ioe) {
            throw new UnrecoverableCoreException("Unexpected I/O error accessing security index", ioe);
        }
    }

    private Collection<IndexableField> createDocument(SecurityVO security) {
        return Arrays.asList(
                new Field(FIELDS[0], optionalString(security.getIsin()), TextField.TYPE_STORED),
                new Field(FIELDS[1], optionalString(security.getBbgid()), TextField.TYPE_STORED),
                new Field(FIELDS[2], optionalString(security.getDescription()), TextField.TYPE_STORED),
                new Field(FIELDS[3], optionalString(security.getSymbol()), TextField.TYPE_STORED),
                new Field(FIELDS[4], optionalString(security.getLmaxid()), TextField.TYPE_STORED),
                new Field(FIELDS[5], optionalString(security.getRic()), TextField.TYPE_STORED),
                new LongField(FIELDS[6], security.getId(), LongField.TYPE_STORED),
                new Field(FIELDS[7], String.valueOf(security.getSecurityFamilyId()), TextField.TYPE_STORED),
                new Field(FIELDS[8], String.valueOf(security.getUnderlyingId()), TextField.TYPE_STORED),
                new Field(FIELDS[9], String.valueOf(security.getId()), TextField.TYPE_STORED));
    }

    private String optionalString(String str) {
        return str == null ? "" : str;
    }

    public static class FieldResult {
        private final String name;
        private final String value;

        public FieldResult(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
