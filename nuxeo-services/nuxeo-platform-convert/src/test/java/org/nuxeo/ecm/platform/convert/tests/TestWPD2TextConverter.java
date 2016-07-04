/*
 * (C) Copyright 2010-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.platform.convert.tests;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.convert.api.ConverterCheckResult;
import org.nuxeo.runtime.api.Framework;

import static org.junit.Assert.*;

public class TestWPD2TextConverter extends BaseConverterTest {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.core.commandline.executor");
    }

    @Test
    public void testWordPerfectToTextConverter() throws Exception {

        ConversionService cs = Framework.getLocalService(ConversionService.class);
        assertNotNull(cs);
        ConverterCheckResult check = cs.isConverterAvailable("wpd2text");
        assertNotNull(check);
        Assume.assumeTrue(
                String.format("Skipping Wordperfect conversion test since libpwd-tool is not installed:\n"
                        + "- installation message: %s\n" + "- error message: %s", check.getInstallationMessage(),
                        check.getErrorMessage()), check.isAvailable());

        String converterName = cs.getConverterName("application/wordperfect", "text/plain");
        assertEquals("wpd2text", converterName);

        BlobHolder hg = getBlobFromPath("test-docs/test.wpd", "application/wordperfect");

        BlobHolder result = cs.convert(converterName, hg, null);
        assertNotNull(result);

        String txt = result.getBlob().getString();
        assertTrue(txt.contains("Zoonotic"));
        assertTrue(txt.contains("Committee"));
    }

}
