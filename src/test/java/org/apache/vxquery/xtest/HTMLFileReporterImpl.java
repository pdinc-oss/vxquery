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
package org.apache.vxquery.xtest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.vxquery.xtest.TestCaseResult.State;

public class HTMLFileReporterImpl implements ResultReporter {
    private List<TestCaseResult> results;

    private int count;

    private int userErrors;

    private int internalErrors;

    private Map<Class<?>, Integer> exDistribution;

    private Map<TestCaseResult.State, Integer> stDistribution;

    private long startTime;

    private long endTime;

    private PrintWriter out;
    
    private File resultsDir;

    public HTMLFileReporterImpl(File file) throws IOException {
        results = new ArrayList<TestCaseResult>();
        count = 0;
        userErrors = 0;
        internalErrors = 0;
        startTime = -1;
        exDistribution = new HashMap<Class<?>, Integer>();
        stDistribution = new HashMap<TestCaseResult.State, Integer>();
        if (file != null) {
            out = new PrintWriter(file);
            String fileName = file.getName();
            int dot = file.getName().lastIndexOf('.');
            String dirName = (dot < 0 ? fileName : fileName.substring(0, dot)) + "_results";
            resultsDir = file.getParent() != null 
                ? new File(file.getParent() + File.separator + dirName)
                : new File(dirName);
            ensureDir(resultsDir);
        }
    }

    @Override
    public void reportResult(TestCaseResult result) {
        results.add(result);
        endTime = System.currentTimeMillis();
        if (startTime < 0) {
            startTime = endTime;
        }
        if (result.error()) {
            if (result.userError()) {
                ++userErrors;
            } else {
                ++internalErrors;
                Integer count = exDistribution.get(result.error.getClass());
                if (count == null) {
                    count = 0;
                }
                count++;
                exDistribution.put(result.error.getClass(), count);
            }
        }
        Integer stCount = stDistribution.get(result.state);
        if (stCount == null) {
            stCount = 0;
        }
        stCount++;
        stDistribution.put(result.state, stCount);
        ++count;
    }

    @Override
    public void close() {
        if (out != null) {
            writeHTML(out);
            out.flush();
        }
    }
    
    public void writeHTML(PrintWriter out) {
        out.println("<html><body>");
        writeSummary(out, count, userErrors, internalErrors, startTime, endTime);
        out.println("<hl>");
        writeStateDistribution(out, stDistribution);
        out.println("<hl>");
        writeExceptionDistribution(out, exDistribution);
        out.println("<hl>");
        writeResults(out, results, resultsDir);
        out.println("</body></html>");
    }

    private static void writeSummary(PrintWriter out, int count, int userErrors, 
            int internalErrors, long startTime, long endTime) {
        out.println("<table>");
        out.println("<tr><td>Test Count</td><td>");
        out.println(count);
        out.println("</td></tr>");
        out.println("<tr><td>Test Errors</td><td>");
        out.println(userErrors);
        out.println("</td></tr>");
        out.println("<tr><td>Test Failures</td><td>");
        out.println(internalErrors);
        out.println("</td></tr>");
        out.println("<tr><td>Total time</td><td>");
        out.println(endTime - startTime);
        out.println("</td></tr>");
        out.println("</table>");
    } 

    private static void writeExceptionDistribution(PrintWriter out, Map<Class<?>, Integer> exDistribution) {
        out.println("<table>");
        List<Entry<Class<?>, Integer>> entryList = new ArrayList<Entry<Class<?>, Integer>>(exDistribution.entrySet());
        Comparator<Entry<Class<?>, Integer>> comp = new Comparator<Entry<Class<?>, Integer>>() {
            public int compare(Entry<Class<?>, Integer> o1, Entry<Class<?>, Integer> o2) {
                return o1.getKey().getName().compareTo(o2.getKey().getName());
            }
        };
        Collections.sort(entryList, comp);
        for (Map.Entry<Class<?>, Integer> e : entryList) {
            out.println("<tr><td>");
            out.println(e.getKey().getName());
            out.println("</td><td>");
            out.println(e.getValue());
            out.println("</td></tr>");
        }
        out.println("</table>");
    }

    private static void writeStateDistribution(PrintWriter out, Map<TestCaseResult.State, Integer> stDistribution) {
        out.println("<table>");
        List<Map.Entry<TestCaseResult.State, Integer>> entryList 
            = new ArrayList<Map.Entry<TestCaseResult.State, Integer>>(stDistribution.entrySet());
        Comparator<Map.Entry<TestCaseResult.State, Integer>> comp 
            = new Comparator<Map.Entry<TestCaseResult.State, Integer>>() {
                public int compare(Map.Entry<TestCaseResult.State, Integer> o1, Map.Entry<TestCaseResult.State, Integer> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            };
        Collections.sort(entryList, comp);
        for (Map.Entry<TestCaseResult.State, Integer> e : entryList) {
            State key = e.getKey();
            out.print("<tr style=\"background: ");
            out.println(key.getColor());
            out.println(";\"><td>");
            out.println(key);
            out.println("</td><td>");
            out.println(e.getValue());
            out.println("</td></tr>");
        }
        out.println("</table>");
    }

    private static void writeResults(PrintWriter out, List<TestCaseResult> results, File resultsDir) {
        out.println("<table>");
        int len = results.size();
        for (int i = 0; i < len; ++i) {
            TestCaseResult res = results.get(i);
            out.print("<tr style=\"background: ");
            out.println(res.state.getColor());
            out.println(";\"><td>");
            out.print(i + 1);
            out.print("</td><td><a href=\"");
            out.print(res.testCase.getXQueryFile().toURI());
            out.print("\">");
            String queryDisplayName = res.testCase.getXQueryDisplayName();
            out.print(queryDisplayName);
            out.print("</a></td><td>");
            out.print(res.time);
            out.print("</td><td>");
            File resultFile = createResultFile(resultsDir, res, queryDisplayName);
            if (resultFile != null) {
                out.print("<a href=\"");
                out.print(resultFile.toURI());
                out.print("\">");
                out.print(res.report);
                out.print("</a>");
            } else {
                out.print(res.report);
            }
            out.println("</td></tr>");
        }
        out.println("</table>");
    }

    private static File createResultFile(File resultsDir, TestCaseResult res, String queryDisplayName) {
        if (resultsDir == null) {
            return null;
        }
        try {
            File resultFile = new File(resultsDir + File.separator + queryDisplayName + ".res");
            ensureDir(resultFile.getParentFile());
            FileWriter fw = new FileWriter(resultFile);
            if (res.result != null) {
                fw.write(res.result);
            } else {
                PrintWriter pw = new PrintWriter(fw);
                res.error.printStackTrace(pw);
            }
            fw.close();
            return resultFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static void ensureDir(File dir) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("could not create dir " + dir);
        }
    }
}