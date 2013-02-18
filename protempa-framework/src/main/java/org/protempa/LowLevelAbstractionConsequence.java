/*
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 Emory University
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.drools.WorkingMemory;
import org.drools.rule.Declaration;
import org.drools.spi.Consequence;
import org.drools.spi.KnowledgeHelper;
import org.protempa.proposition.Context;
import org.protempa.proposition.PrimitiveParameter;
import org.protempa.proposition.Sequence;
import org.protempa.proposition.interval.Relation;

/**
 * @author Andrew Post
 */
final class LowLevelAbstractionConsequence implements Consequence {

    private static final long serialVersionUID = 2455607587534331595L;
    private static final Relation REL = new Relation(null, null, 0, null, null,
            null, null, null, null, null, null, null, 0, null, null, null);
    private final LowLevelAbstractionDefinition def;
    private final Algorithm algorithm;
    private final DerivationsBuilder derivationsBuilder;
    private transient MyObjectAsserter objAsserter;

    private void doProcess(WorkingMemory arg1, Sequence<PrimitiveParameter> subSeq) throws AlgorithmProcessingException, AlgorithmInitializationException {
        objAsserter.workingMemory = arg1;
        LowLevelAbstractionFinder.process(subSeq, this.def,
                this.algorithm,
                objAsserter, this.derivationsBuilder);
        objAsserter.workingMemory = null;
    }

    private static class MyObjectAsserter implements ObjectAsserter {

        private WorkingMemory workingMemory;

        @Override
        public void assertObject(Object obj) {
            workingMemory.insert(obj);
            System.err.println("asserting " + obj);
            ProtempaUtil.logger().log(Level.FINER,
                    "Asserted derived proposition {0}", obj);
        }
    }

    LowLevelAbstractionConsequence(
            LowLevelAbstractionDefinition simpleAbstractionDef,
            Algorithm algorithm, DerivationsBuilder derivationsBuilder) {
        this.def = simpleAbstractionDef;
        this.algorithm = algorithm;
        this.derivationsBuilder = derivationsBuilder;
        this.objAsserter = new MyObjectAsserter();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void evaluate(KnowledgeHelper arg0, WorkingMemory arg1)
            throws Exception {
        List<PrimitiveParameter> pl =
                (List<PrimitiveParameter>) arg0.get(
                arg0.getDeclaration("result"));
        Declaration declaration = arg0.getDeclaration("result2");
        Sequence<PrimitiveParameter> seq =
                new Sequence<PrimitiveParameter>(
                this.def.getAbstractedFrom(), pl);
        if (declaration != null) {
            List<Context> contexts =
                    (List<Context>) arg0.get(arg0.getDeclaration("result2"));

            Sequence<Context> contextSeq =
                    new Sequence<Context>(this.def.getContextId(), contexts);
            LinkedList<PrimitiveParameter> ll =
                    new LinkedList<PrimitiveParameter>(seq);

            boolean in = false;
            Iterator<PrimitiveParameter> itr = ll.iterator();
            PrimitiveParameter tp = itr.next();
            for (Context context : contextSeq) {
                Sequence<PrimitiveParameter> subSeq =
                        new Sequence<PrimitiveParameter>(seq.getPropositionIds());
                while (true) {
                    if (REL.hasRelation(tp.getInterval(), context.getInterval())) {
                        subSeq.add(tp);
                        in = true;
                    } else if (in) {
                        in = false;
                        break;
                    }
                    itr.remove();
                    if (itr.hasNext()) {
                        tp = itr.next();
                    } else {
                        break;
                    }

                }
                if (!subSeq.isEmpty()) {
                    doProcess(arg1, subSeq);
                    subSeq.clear();
                }
            }
        } else {
            doProcess(arg1, seq);
        }

    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.objAsserter = new MyObjectAsserter();
    }
}
