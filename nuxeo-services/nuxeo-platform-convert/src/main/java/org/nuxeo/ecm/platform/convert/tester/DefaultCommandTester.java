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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.ecm.platform.convert.tester;

import java.io.IOException;

import org.nuxeo.common.utils.ArrayUtils;
import org.nuxeo.ecm.platform.commandline.executor.service.CommandLineDescriptor;
import org.nuxeo.ecm.platform.commandline.executor.service.cmdtesters.CommandTestResult;
import org.nuxeo.ecm.platform.commandline.executor.service.cmdtesters.CommandTester;

/**
 * Simple {@link CommandTester} that accepts a command to test the existance of the command line.
 * @author rdias
 */
public class DefaultCommandTester implements CommandTester {

    @Override
    public CommandTestResult test(CommandLineDescriptor cmdDescriptor) {

        String cmd = cmdDescriptor.getTesterParameters().get("command");
        try {

            Runtime.getRuntime().exec(cmd);

        } catch (IOException e) {
            return new CommandTestResult(
                    "command " + cmdDescriptor.getCommand() + " not found in system path (descriptor " + cmdDescriptor + ")");
        }

        return new CommandTestResult();
    }

}
