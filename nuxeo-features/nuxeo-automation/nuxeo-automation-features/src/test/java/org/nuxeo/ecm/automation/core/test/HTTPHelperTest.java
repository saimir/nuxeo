/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Ricardo Dias
 */
package org.nuxeo.ecm.automation.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.BinaryBody;
import org.mockserver.model.Body;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.JsonBody;
import org.mockserver.model.StringBody;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.context.ContextHelper;
import org.nuxeo.ecm.automation.context.ContextService;
import org.nuxeo.ecm.automation.core.scripting.Scripting;
import org.nuxeo.ecm.automation.features.HTTPHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * @since 8.3
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.automation.features" })
public class HTTPHelperTest {

    protected static final Log log = LogFactory.getLog(HTTPHelperTest.class);

    @Inject
    CoreSession session;

    @Inject
    ContextService ctxService;

    OperationContext ctx;

    protected ClientAndProxy proxy;
    protected ClientAndServer mockServer;
    protected String SERVER_HOST = "localhost";
    protected String SERVER_PATH = "/ws/path";
    protected int SERVER_PORT = 1080;
    protected String SERVER_URL = "http://" + SERVER_HOST + ":" + String.valueOf(SERVER_PORT) + SERVER_PATH;

    @Before
    public void setUp() throws Exception {
        mockServer = startClientAndServer(1080);
        proxy = startClientAndProxy(1090);

        ctx = new OperationContext(session);
    }

    @After
    public void tearDown() throws Exception {
        proxy.stop();
        mockServer.stop();
    }

    @Test
    public void testGetHTTPHelper() {
        // set up and create the mock server
        Body answer = new JsonBody("{\"message\": \"Testing the GET request.\"}");
        Map<String, String> responseHeaders = new HashMap<>(2);
        responseHeaders.put("Content-Type", "application/json; charset=utf-8");
        responseHeaders.put("Cache-Control", "public, max-age=86400");
        createMockServer(responseHeaders, answer);

        // call the server using the helper
        HTTPHelper helper = getHTTPHelper();
        try {
            Map<String, String> headers = new HashMap<>(2);
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");

            // call the helper from the context helpers to perform the GET request
            Blob httpResult = helper.call(null, null, "GET", SERVER_URL, headers);
            // parse the json result
            ObjectMapper mapper = new ObjectMapper();
            Map<String,String> jsonResult = mapper.readValue(httpResult.getString(), Map.class);

            String message = jsonResult.get("message");
            assertEquals("Testing the GET request.", message);

            // call the helper from the scripting
            String expr = String.format("HTTP.call(null,null,\"GET\",\"%s\",headers)", SERVER_URL);
            ctx.put("headers", headers);
            Blob resultBlob = (Blob) Scripting.newExpression(expr).eval(ctx);
            String result = IOUtils.toString(resultBlob.getStream(), "utf-8");
            jsonResult = mapper.readValue(result, Map.class);

            message = jsonResult.get("message");
            assertEquals("Testing the GET request.", message);
        } catch (IOException exception){
            log.error("Problem parsing the result. " + exception);

        }
    }

    @Test
    public void testDownloadFile() {
        // set up and create the mock server
        try {
            File file = FileUtils.getResourceFileFromContext("test-data/sample.jpeg");
            byte[] answer = FileUtils.readBytes(file);

            Body responseBody = new BinaryBody(answer);
            Map<String, String> responseHeaders = new HashMap<>(3);
            responseHeaders.put("Content-Type", "image/jpeg");
            responseHeaders.put("Cache-Control", "public, max-age=86400");
            responseHeaders.put("Content-Disposition", "attachment; filename=\"sample.jpeg\"");
            createMockServer(responseHeaders, responseBody);
        } catch(IOException e) {
            log.error("Error reading the image file." + e.getMessage());
        }

        HTTPHelper helper = getHTTPHelper();
        try {
            // call the helper to download a file
            Map<String, String> headers = new HashMap<>(2);
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "image/jpeg");

            // call the helper from the context helpers to perform the GET request
            Blob httpResult = helper.call(null, null, "GET", SERVER_URL, headers);
            // assert that the file was received
            assertTrue(httpResult.getLength() > 0);
        } catch (IOException e) {
            log.error("Error reading the image file." + e.getMessage());
        }
    }

    /**
     * Gets the {@link HTTPHelper} using the context service.
     * @return
     */
    public HTTPHelper getHTTPHelper() {
        Map<String, ContextHelper> contextHelperList = ctxService.getHelperFunctions();
        HTTPHelper httpHelper = (HTTPHelper) contextHelperList.get("HTTP");
        assertNotNull(httpHelper);
        assertTrue(httpHelper instanceof HTTPHelper);
        return httpHelper;
    }

    /**
     * Create a mock server that answers http requests with a certain set of headers and a body answer.
     * @param headers the headers for the http response
     * @param answer the body of the http response
     */
    public void createMockServer(Map<String, String> headers, Body answer) {
        // create the headers
        List<Header> headersList = new ArrayList<>(headers.size());
        for (Map.Entry<String, String> header: headers.entrySet()) {
            headersList.add(new Header(header.getKey(), header.getValue()));
        }

        new MockServerClient(SERVER_HOST, SERVER_PORT)
            .when(
                request()
                        .withMethod("GET")
                        .withPath(SERVER_PATH),
                Times.exactly(2))
            .respond(
                response()
                        .withStatusCode(200)
                        .withHeaders(headersList)
                        .withBody(answer)
                        .withDelay(new Delay(TimeUnit.SECONDS, 1)));
    }
}
