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
package org.protempa;

import org.protempa.proposition.DefaultUniqueIdFactory;

import org.protempa.proposition.PrimitiveParameter;
import org.protempa.proposition.Segment;
import org.protempa.proposition.Sequence;
import org.protempa.proposition.UniqueId;
import org.protempa.proposition.UniqueIdFactory;

/**
 * @author Andrew Post
 */
final class SegmentTestParameters {
    private static final UniqueIdFactory factory = 
            new DefaultUniqueIdFactory();

    private SegmentTestParameters() {
    }

    static Segment<PrimitiveParameter> getLength1PrimitiveParameterSegment() {
        PrimitiveParameter p1 = new PrimitiveParameter("TEST", uid());
        p1.setSourceSystem(DataSourceBackendSourceSystem.getInstance("TEST"));
        p1.setPosition(1L);
        Sequence<PrimitiveParameter> seq = new Sequence<>(
                "TEST", 1);
        seq.add(p1);
        return new SimpleSegment<>(seq);
    }

    static Segment<PrimitiveParameter> getLength2PrimitiveParameterSegment() {
        PrimitiveParameter p1 = new PrimitiveParameter("TEST", uid());
        p1.setSourceSystem(DataSourceBackendSourceSystem.getInstance("TEST"));
        p1.setPosition(1L);
        PrimitiveParameter p2 = new PrimitiveParameter("TEST", uid());
        p2.setSourceSystem(DataSourceBackendSourceSystem.getInstance("TEST"));
        p2.setPosition(2L);
        Sequence<PrimitiveParameter> seq = new Sequence<>(
                "TEST", 2);
        seq.add(p1);
        seq.add(p2);
        return new SimpleSegment<>(seq);
    }
    
    private static UniqueId uid() {
        return factory.getInstance();
    }
}
