/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bstefanescu
 *     vpasquier
 */
package org.nuxeo.ecm.automation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes an operation chain execution.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class OperationChain implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final String id;

    // (via REST for example)
    protected final List<OperationParameters> operations;

    protected final Map<String, Object> chainParameters;

    protected String description;

    /**
     * @since 7.1
     */
    protected String[] aliases;

    protected boolean isPublic; // whether this chain is visible to clients

    public OperationChain(String id) {
        this.id = id;
        operations = new ArrayList<OperationParameters>();
        chainParameters = new HashMap<String, Object>();
    }

    public OperationChain(String id, List<OperationParameters> operations) {
        this.id = id;
        this.operations = operations;
        chainParameters = new HashMap<String, Object>();
    }

    public OperationChain(String id, List<OperationParameters> operations, Map<String, Object> chainParameters) {
        this.id = id;
        this.operations = operations;
        this.chainParameters = chainParameters;
    }

    public String getId() {
        return id;
    }

    /**
     * @since 7.1
     */
    public String[] getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @since 7.1
     */
    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<OperationParameters> getOperations() {
        return operations;
    }

    public void add(OperationParameters op) {
        operations.add(op);
    }

    public OperationParameters add(String operationId) {
        OperationParameters op = new OperationParameters(operationId);
        operations.add(op);
        return op;
    }

    /**
     * @since 5.7.2 Adding chain parameters
     */
    public void addChainParameters(Map<String, Object> chainParameter) {
        chainParameters.putAll(chainParameter);
    }

    /**
     * @since 5.7.2 Getting chain parameters
     */
    public Map<String, Object> getChainParameters() {
        return chainParameters;
    }

}
