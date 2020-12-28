/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.integration.cases.assertion;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.cases.assertion.dcl.DCLIntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.assertion.ddl.DDLIntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.assertion.dml.DMLIntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.assertion.dql.DQLIntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.assertion.root.IntegrateTestCase;
import org.apache.shardingsphere.test.integration.cases.assertion.root.IntegrateTestCases;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Integrate test cases loader.
 */
@Slf4j
public final class IntegrateTestCasesLoader {
    
    private static final IntegrateTestCasesLoader INSTANCE = new IntegrateTestCasesLoader();
    
    private final Map<IntegrateTestCaseType, List<? extends IntegrateTestCase>> integrateTestCases = new LinkedHashMap<>();
    
    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IntegrateTestCasesLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get integrate test cases.
     * 
     * @param caseType integration test case type
     * @return integrate test cases
     */
    public List<? extends IntegrateTestCase> getTestCases(final IntegrateTestCaseType caseType) {
        integrateTestCases.putIfAbsent(caseType, loadIntegrateTestCases(caseType));
        return integrateTestCases.get(caseType);
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class, JAXBException.class})
    private List<? extends IntegrateTestCase> loadIntegrateTestCases(final IntegrateTestCaseType caseType) {
        URL url = IntegrateTestCasesLoader.class.getClassLoader().getResource("integrate/cases/");
        Preconditions.checkNotNull(url, "Cannot found integrate test cases.");
        return loadIntegrateTestCases(url, caseType);
    }
    
    private List<? extends IntegrateTestCase> loadIntegrateTestCases(final URL url, final IntegrateTestCaseType caseType) throws IOException, URISyntaxException, JAXBException {
        List<String> files = getFiles(url, caseType);
        Preconditions.checkNotNull(files, "Cannot found integrate test cases.");
        List<? extends IntegrateTestCase> result = new LinkedList<>();
        for (String each : files) {
            result = unmarshal(each, caseType).getIntegrateTestCases();
            result.forEach(testCase -> testCase.setPath(each));
        }
        return result;
    }
    
    private static List<String> getFiles(final URL url, final IntegrateTestCaseType caseType) throws IOException, URISyntaxException {
        List<String> result = new LinkedList<>();
        Files.walkFileTree(Paths.get(url.toURI()), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes basicFileAttributes) {
                if (file.getFileName().toString().startsWith(caseType.getFilePrefix()) && file.getFileName().toString().endsWith(".xml")) {
                    result.add(file.toFile().getPath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    
    private static IntegrateTestCases unmarshal(final String integrateCasesFile, final IntegrateTestCaseType caseType) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(integrateCasesFile)) {
            switch (caseType) {
                case DQL:
                    return (DQLIntegrateTestCases) JAXBContext.newInstance(DQLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
                case DML:
                    return (DMLIntegrateTestCases) JAXBContext.newInstance(DMLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
                case DDL:
                    return (DDLIntegrateTestCases) JAXBContext.newInstance(DDLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
                case DCL:
                    return (DCLIntegrateTestCases) JAXBContext.newInstance(DCLIntegrateTestCases.class).createUnmarshaller().unmarshal(reader);
                default:
                    throw new UnsupportedOperationException(caseType.getFilePrefix());
            }
        }
    }
}
