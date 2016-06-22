package org.nuxeo.ecm.platform.convert.tests;

import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.*;

@Features(CoreFeature.class)
@Deploy({"org.nuxeo.ecm.core.convert.api","org.nuxeo.ecm.core.convert.api",
        "org.nuxeo.ecm.platform.commandline.executor","org.nuxeo.ecm.platform.convert"})
@LocalDeploy("org.nuxeo.ecm.platform.convert:OSGI-INF/test-soffice-env-contrib.xml")
public class ConvertFeatures extends SimpleFeature {
}
