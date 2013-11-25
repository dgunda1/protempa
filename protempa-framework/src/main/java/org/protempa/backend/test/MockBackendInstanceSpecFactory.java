/*
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 - 2013 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.protempa.backend.test;

import java.util.ArrayList;
import java.util.List;

import org.protempa.backend.Backend;
import org.protempa.backend.BackendInstanceSpec;
import org.protempa.backend.BackendPropertySpec;
import org.protempa.backend.BackendSpec;

public final class MockBackendInstanceSpecFactory<B extends Backend<?>> {
    private BackendInstanceSpec<B> backendInstSpec;

    public BackendInstanceSpec<B> getInstance() {
        return backendInstSpec;
    }

    public MockBackendInstanceSpecFactory(List<BackendPropertySpec> propSpecs) {
        BackendSpec<B> backendSpec = new BackendSpec<>(
                new MockBackendProvider(), "mockSpec", "Mock Spec",
                new ArrayList<BackendPropertySpec>());
                
        backendInstSpec = backendSpec.newBackendInstanceSpec();
    }
}
