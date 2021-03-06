/*
 * #%L
 * Protempa Protege Knowledge Source Backend
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
package org.protempa.backend.ksb.protege;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protempa.AbstractAbstractionDefinition;
import org.protempa.AbstractPropositionDefinition;
import org.protempa.DefaultSourceId;
import org.protempa.HighLevelAbstractionDefinition;
import org.protempa.proposition.interval.Interval.Side;
import org.protempa.KnowledgeSourceReadException;
import org.protempa.TemporalPatternOffset;
import org.protempa.SequentialTemporalPatternDefinition;
import org.protempa.PropertyConstraint;
import org.protempa.PropertyDefinition;
import org.protempa.ReferenceDefinition;
import org.protempa.SimpleGapFunction;
import org.protempa.TemporalExtendedParameterDefinition;
import org.protempa.TemporalExtendedPropositionDefinition;
import org.protempa.valueset.ValueSet;
import org.protempa.valueset.ValueSetElement;
import org.protempa.backend.ksb.KnowledgeSourceBackend;
import org.protempa.proposition.interval.Relation;
import org.protempa.proposition.value.AbsoluteTimeUnit;
import org.protempa.proposition.value.NominalValue;
import org.protempa.proposition.value.RelativeHourUnit;
import org.protempa.proposition.value.Unit;
import org.protempa.proposition.value.Value;
import org.protempa.proposition.value.ValueType;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Utility class for classes in
 * <code>edu.virginia.pbhs.protempa.protege</code>.
 *
 * @author Andrew Post
 */
class Util {

    /*
     * Allowed time constraint units in the Protege knowledge base.
     */
    /**
     * Minutes (60 * 1000 milliseconds).
     */
    private static final String MINUTE = "Minute";
    /**
     * Hours (60 * 60 * 1000 milliseconds).
     */
    private static final String HOUR = "Hour";
    /**
     * Days (24 * 60 * 60 * 1000 milliseconds).
     */
    private static final String DAY = "Day";
    static final Map<String, AbsoluteTimeUnit> ABSOLUTE_DURATION_MULTIPLIER = new HashMap<>();

    static {
        ABSOLUTE_DURATION_MULTIPLIER.put(MINUTE, AbsoluteTimeUnit.MINUTE);
        ABSOLUTE_DURATION_MULTIPLIER.put(HOUR, AbsoluteTimeUnit.HOUR);
        ABSOLUTE_DURATION_MULTIPLIER.put(DAY, AbsoluteTimeUnit.DAY);
    }
    static final Map<String, RelativeHourUnit> RELATIVE_HOURS_DURATION_MULTIPLIER = new HashMap<>();

    static {
        RELATIVE_HOURS_DURATION_MULTIPLIER.put(HOUR, RelativeHourUnit.HOUR);
    }
    static final Map<String, ValueType> VALUE_CLASS_NAME_TO_VALUE_TYPE = new HashMap<>();

    static {
        VALUE_CLASS_NAME_TO_VALUE_TYPE.put("Value", ValueType.VALUE);
        VALUE_CLASS_NAME_TO_VALUE_TYPE.put("NominalValue",
                ValueType.NOMINALVALUE);
        VALUE_CLASS_NAME_TO_VALUE_TYPE.put("OrdinalValue",
                ValueType.ORDINALVALUE);
        VALUE_CLASS_NAME_TO_VALUE_TYPE.put("NumericalValue",
                ValueType.NUMERICALVALUE);
        VALUE_CLASS_NAME_TO_VALUE_TYPE
                .put("DoubleValue", ValueType.NUMBERVALUE);
        VALUE_CLASS_NAME_TO_VALUE_TYPE.put("InequalityDoubleValue",
                ValueType.INEQUALITYNUMBERVALUE);
        VALUE_CLASS_NAME_TO_VALUE_TYPE.put("DateValue", ValueType.DATEVALUE);
    }

    static ValueSet parseValueSet(Cls valueTypeCls, ValueType valueType,
            ConnectionManager cm, KnowledgeSourceBackend backend)
            throws KnowledgeSourceReadException {
        Slot valueSetSlot = cm.getSlot("valueSet");
        Collection objs = valueTypeCls
                .getDirectTemplateSlotValues(valueSetSlot);
        ValueSet valueSet = null;
        Slot displayNameSlot = cm.getSlot("displayName");
        Slot abbrevDisplayNameSlot = cm.getSlot("abbrevDisplayName");
        Slot valueSlot = cm.getSlot("value");
        if (!objs.isEmpty()) {
            valueSet = parseEnumeratedValueSet(valueTypeCls, objs, cm,
                    displayNameSlot, abbrevDisplayNameSlot, valueSlot,
                    valueType, backend);
        }
        return valueSet;
    }

    private static ValueSet parseEnumeratedValueSet(Cls valueSetCls,
            Collection objs, ConnectionManager cm, Slot displayNameSlot,
            Slot abbrevDisplayNameSlot, Slot valueSlot, ValueType valueType,
            KnowledgeSourceBackend backend) throws KnowledgeSourceReadException {
        ValueSetElement[] vses = new ValueSetElement[objs.size()];
        int i = 0;
        for (Object obj : objs) {
            Instance valueSetEltInst = (Instance) obj;
            String displayName = (String) cm.getOwnSlotValue(valueSetEltInst,
                    displayNameSlot);
            String abbrevDisplayName = (String) cm.getOwnSlotValue(
                    valueSetEltInst, abbrevDisplayNameSlot);
            String value = (String) cm.getOwnSlotValue(valueSetEltInst,
                    valueSlot);
            Value val = valueType.parse(value);
            vses[i] = new ValueSetElement(val, displayName, abbrevDisplayName);
            i++;
        }
        return new ValueSet(valueSetCls.getName(), valueSetCls.getName(), vses,
                DefaultSourceId.getInstance(backend.getDisplayName()));
    }

    static ValueType parseValueType(Cls valueTypeCls) {
        ValueType result = VALUE_CLASS_NAME_TO_VALUE_TYPE.get(valueTypeCls
                .getName());
        if (result == null) {
            result = parseValueSet(valueTypeCls);
        }
        return result;
    }

    static ValueType parseValueSet(Cls valueTypeCls) {
        Collection superClasses = valueTypeCls.getSuperclasses();
        ValueType valueType = null;
        for (Object superCls : superClasses) {
            Cls superClsCls = (Cls) superCls;
            String superClsName = superClsCls.getName();
            valueType = VALUE_CLASS_NAME_TO_VALUE_TYPE.get(superClsName);
            if (valueType != null) {
                break;
            }
        }
        if (valueType == null) {
            throw new AssertionError("valueTypeCls " + valueTypeCls.getName()
                    + " has no corresponding value type");
        }
        return valueType;
    }

    private Util() {
    }

    private static class LazyLoggerHolder {

        private static Logger instance = Logger.getLogger(Util.class
                .getPackage().getName());
    }

    static Logger logger() {
        return LazyLoggerHolder.instance;
    }

    /**
     * Calculates a time constraint in milliseconds from a pair of time
     * constraint and units values in a Protege instance.
     *
     * @param instance a Protege <code>Instance</code> object.
     * @param constraint a time constraint slot name. The named slot is expected
     * to have an Integer value.
     * @param constraintUnits a time constraint units slot name. May have the
     * values "Minute", "Hour", or "Day".
     * @return a <code>Weight</code> object representing a time in milliseconds.
     */
    static Integer parseTimeConstraint(Instance instance, String constraint,
            ConnectionManager cm) throws KnowledgeSourceReadException {
        Integer constraintValue = null;
        if (instance != null && constraint != null) {
            constraintValue = (Integer) cm.getOwnSlotValue(instance,
                    cm.getSlot(constraint));
        }

        return constraintValue;
    }

    static Unit parseUnitsConstraint(Instance instance, String constraintUnits,
            ProtegeKnowledgeSourceBackend backend, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        String constraintUnitsValue = null;
        if (instance != null && constraintUnits != null) {
            constraintUnitsValue = (String) cm.getOwnSlotValue(instance,
                    cm.getSlot(constraintUnits));
        }

        if (constraintUnitsValue == null) {
            return null;
        } else {
            return backend.parseUnit(constraintUnitsValue);
        }
    }

    /**
     * @param instance
     * @param d
     */
    static void setGap(Instance instance, AbstractAbstractionDefinition d,
            ProtegeKnowledgeSourceBackend backend, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        Integer maxGap = (Integer) cm.getOwnSlotValue(instance,
                cm.getSlot("maxGap"));
        Unit maxGapUnits = Util.parseUnitsConstraint(instance, "maxGapUnits",
                backend, cm);
        d.setGapFunction(new SimpleGapFunction(maxGap, maxGapUnits));
    }

    static void setNames(Instance complexAbstractionInstance,
            AbstractPropositionDefinition cad, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        cad.setDisplayName((String) cm.getOwnSlotValue(
                complexAbstractionInstance, cm.getSlot("displayName")));
        cad.setAbbreviatedDisplayName((String) cm.getOwnSlotValue(
                complexAbstractionInstance, cm.getSlot("abbrevDisplayName")));
    }

    static void setSolid(Instance protegeProposition,
            HighLevelAbstractionDefinition result, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        boolean bool = parseSolid(protegeProposition, cm);
        result.setSolid(bool);
    }

    static void setSolid(Instance protegeProposition, SequentialTemporalPatternDefinition result,
            ConnectionManager cm) throws KnowledgeSourceReadException {
        boolean bool = parseSolid(protegeProposition, cm);
        result.setSolid(bool);
    }

    static void setConcatenable(Instance protegeProposition,
            SequentialTemporalPatternDefinition result, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        boolean bool = parseConcatenable(protegeProposition, cm);
        result.setConcatenable(bool);
    }

    static void setConcatenable(Instance protegeProposition,
            HighLevelAbstractionDefinition result, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        boolean bool = parseConcatenable(protegeProposition, cm);
        result.setConcatenable(bool);
    }

    private static boolean parseConcatenable(Instance protegeProposition,
            ConnectionManager cm) throws KnowledgeSourceReadException {
        Boolean bool = (Boolean) protegeProposition.getDirectOwnSlotValue(cm
                .getSlot("concatenable"));
        if (bool == null) {
            Util.logger()
                    .log(Level.WARNING,
                    "{0} has no value for the 'concatenable' property: setting FALSE",
                    protegeProposition.getName());
            return false;
        } else {
            return bool.booleanValue();
        }
    }

    private static boolean parseSolid(Instance protegeProposition,
            ConnectionManager cm) throws KnowledgeSourceReadException {
        Boolean bool = (Boolean) protegeProposition.getDirectOwnSlotValue(cm
                .getSlot("solid"));
        if (bool == null) {
            Util.logger().log(Level.WARNING,
                    "{0} has no value for the 'solid' property: setting FALSE",
                    protegeProposition.getName());
            return false;
        } else {
            return bool.booleanValue();
        }
    }

    static void setInDataSource(Instance protegeProposition,
            AbstractPropositionDefinition result, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        Boolean bool = (Boolean) protegeProposition.getDirectOwnSlotValue(cm
                .getSlot("inDataSource"));
        if (bool == null) {
            bool = Boolean.FALSE;
            Util.logger()
                    .log(Level.WARNING,
                    "{0} has no value for the 'inDataSource' property: setting FALSE",
                    protegeProposition.getName());
        }
        result.setInDataSource(bool.booleanValue());
    }

    /**
     * Sets inverseIsA on the given proposition definition. Automatically
     * resolves duplicate entries but logs a warning.
     *
     * @param propInstance a Protege proposition definition {@link Instance}.
     * @param propDef the corresponding {@link AbstractPropositionDefinition}.
     * @param cm a {@link ConnectionManager}.
     * @throws KnowledgeSourceReadException if there is an error accessing the
     * Protege ontology.
     */
    static void setInverseIsAs(Instance propInstance,
            AbstractPropositionDefinition propDef, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        Collection<?> isas = propInstance.getDirectOwnSlotValues(cm
                .getSlot("inverseIsA"));
        Logger logger = Util.logger();
        if (isas != null && !isas.isEmpty()) {
            Set<String> inverseIsANames = resolveAndLogDuplicates(isas, logger,
                    propInstance, "inverseIsA");
            String[] inverseIsAsArr = inverseIsANames
                    .toArray(new String[inverseIsANames.size()]);
            propDef.setInverseIsA(inverseIsAsArr);
        }
    }

    static void setProperties(Instance propInstance,
            AbstractPropositionDefinition d, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        Slot propertySlot = cm.getSlot("property");
        Slot valueTypeSlot = cm.getSlot("valueType");
        Deque<Instance> stack = new ArrayDeque<>();
        createInheritanceStack(propInstance, cm, stack);
        Map<String, PropertyInstance> inheritedProperties = 
                assembleInheritedProperties(stack, cm, propertySlot);
        PropertyDefinition[] propDefs = new PropertyDefinition[inheritedProperties
                .size()];
        int i = 0;
        for (Map.Entry<String, PropertyInstance> propertyInstance : inheritedProperties.entrySet()) {
            String key = propertyInstance.getKey();
            PropertyInstance value = propertyInstance.getValue();
            Cls valueTypeCls = (Cls) cm.getOwnSlotValue(value.getPropertyInstance(), valueTypeSlot);
            if (valueTypeCls == null) {
                throw new AssertionError("property " + key
                        + " cannot have a null value type");
            }
            ValueType valueType = parseValueType(valueTypeCls);
            PropertyDefinition propDef = new PropertyDefinition(propInstance.getName(),
                    key, null, valueType, valueTypeCls.getName(), value.getPropositionId());
            propDefs[i] = propDef;
            i++;
        }
        d.setPropertyDefinitions(propDefs);
    }
    
    private static class PropertyInstance {
        Instance propertyInstance;
        String propositionId;

        public PropertyInstance(Instance propertyInstance, String propositionId) {
            this.propertyInstance = propertyInstance;
            this.propositionId = propositionId;
        }

        public Instance getPropertyInstance() {
            return propertyInstance;
        }

        public String getPropositionId() {
            return propositionId;
        }
        
    }

    private static Map<String, PropertyInstance> assembleInheritedProperties(Deque<Instance> stack, ConnectionManager cm, Slot propertySlot) throws KnowledgeSourceReadException {
        Slot valueTypeSlot = cm.getSlot("valueType");
        Map<String, PropertyInstance> inheritedProperties = new HashMap<>();
        Instance currentInst = stack.pollFirst();
        while (currentInst != null) {
            Collection<?> properties = cm.getOwnSlotValues(currentInst,
                    propertySlot);
            for (Object propertyInstance : properties) {
                Instance inst = (Instance) propertyInstance;
                PropertyInstance superPropertyInst = inheritedProperties.get(inst.getName());
                if (superPropertyInst != null && !superPropertyInst.getPropositionId().equals(currentInst.getName())) {
                    Cls valueTypeCls = (Cls) cm.getOwnSlotValue(inst, valueTypeSlot);
                    Cls superValueTypeCls = (Cls) cm.getOwnSlotValue(superPropertyInst.getPropertyInstance(), valueTypeSlot);
                    if (!valueTypeCls.hasSuperclass(superValueTypeCls)) {
                        throw new KnowledgeSourceReadException("Conflicting subclass: proposition " + currentInst.getName() + " with property " + inst.getName() + " has a superclass " + superPropertyInst.getPropositionId() + " with the same property with a conflicting type");
                    }
                }
                inheritedProperties.put(inst.getName(), new PropertyInstance(inst, currentInst.getName()));
            }
            currentInst = stack.pollFirst();
        }
        return inheritedProperties;
    }

    private static void createInheritanceStack(Instance propInstance, ConnectionManager cm, Deque<Instance> stack) throws KnowledgeSourceReadException {
        Slot isASlot = cm.getSlot("isA");
        Queue<Instance> q = new LinkedList<>();
        q.add(propInstance);
        while (!q.isEmpty()) {
            Instance instance = q.poll();
            stack.push(instance);
            for (Object parent : cm.getOwnSlotValues(instance, isASlot)) {
                q.add((Instance) parent);
            }
        }
    }
    
    static void setReferences(Instance propInstance,
            AbstractPropositionDefinition d, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        Slot referenceSlot = cm.getSlot("reference");
        if (referenceSlot == null) {
            logger().warning(
                    "The ontology doesn't know about the 'reference' slot");
            return;
        }
        Slot referenceToSlot = cm.getSlot("referenceTo");
        if (referenceToSlot == null) {
            logger().warning(
                    "The ontology doesn't know about the 'referenceTo' slot");
            return;
        }
        Collection<?> references = cm.getOwnSlotValues(propInstance,
                referenceSlot);
        ReferenceDefinition[] refDefs = new ReferenceDefinition[references
                .size()];
        int i = 0;
        for (Object refInstance : references) {
            Instance inst = (Instance) refInstance;
            Collection referenceTos = cm
                    .getOwnSlotValues(inst, referenceToSlot);
            if (referenceTos == null || referenceTos.isEmpty()) {
                throw new AssertionError("reference " + inst.getName()
                        + " from proposition " + propInstance.getName()
                        + " cannot have no referred-to proposition types!");
            }
            List<String> propIds = new ArrayList<>(referenceTos.size());
            for (Object refToInst : referenceTos) {
                Instance refToInstInst = (Instance) refToInst;
                String propId = refToInstInst.getName();
                propIds.add(propId);
            }
            ReferenceDefinition refDef = new ReferenceDefinition(
                    inst.getName(), inst.getName(), propIds.toArray(new String[propIds.size()]));
            refDefs[i] = refDef;
            i++;
        }
        d.setReferenceDefinitions(refDefs);
    }

    static void setTerms(Instance propInstance,
            AbstractPropositionDefinition d, ConnectionManager cm)
            throws KnowledgeSourceReadException {
        Slot termSlot = cm.getSlot("term");
        Collection<?> terms = cm.getOwnSlotValues(propInstance, termSlot);
        String[] termIds = new String[terms.size()];
        int i = 0;
        for (Object termInstance : terms) {
            Instance inst = (Instance) termInstance;
            // String termId =
            // (String) cm.getOwnSlotValue(inst, cm.getSlot("termId"));
            // termIds[i] = termId;
            String name = inst.getName();
            termIds[i] = name;
        }
        d.setTermIds(termIds);
        // d.setTermIds(null);
    }

    static Relation instanceToRelation(Instance relationInstance,
            ConnectionManager cm, ProtegeKnowledgeSourceBackend backend)
            throws KnowledgeSourceReadException {
        Integer mins1s2 = Util.parseTimeConstraint(relationInstance, "mins1s2",
                cm);
        Unit mins1s2Units = Util.parseUnitsConstraint(relationInstance,
                "mins1s2Units", backend, cm);
        Integer maxs1s2 = Util.parseTimeConstraint(relationInstance, "maxs1s2",
                cm);
        Unit maxs1s2Units = Util.parseUnitsConstraint(relationInstance,
                "maxs1s2Units", backend, cm);
        Integer mins1f2 = Util.parseTimeConstraint(relationInstance, "mins1f2",
                cm);
        Unit mins1f2Units = Util.parseUnitsConstraint(relationInstance,
                "mins1f2Units", backend, cm);
        Integer maxs1f2 = Util.parseTimeConstraint(relationInstance, "maxs1f2",
                cm);
        Unit maxs1f2Units = Util.parseUnitsConstraint(relationInstance,
                "maxs1f2Units", backend, cm);
        Integer minf1s2 = Util.parseTimeConstraint(relationInstance, "minf1s2",
                cm);
        Unit minf1s2Units = Util.parseUnitsConstraint(relationInstance,
                "minf1s2Units", backend, cm);
        Integer maxf1s2 = Util.parseTimeConstraint(relationInstance, "maxf1s2",
                cm);
        Unit maxf1s2Units = Util.parseUnitsConstraint(relationInstance,
                "maxf1s2Units", backend, cm);
        Integer minf1f2 = Util.parseTimeConstraint(relationInstance, "minf1f2",
                cm);
        Unit minf1f2Units = Util.parseUnitsConstraint(relationInstance,
                "minf1f2Units", backend, cm);
        Integer maxf1f2 = Util.parseTimeConstraint(relationInstance, "maxf1f2",
                cm);
        Unit maxf1f2Units = Util.parseUnitsConstraint(relationInstance,
                "maxf1f2Units", backend, cm);
        Relation relation = new Relation(mins1s2, mins1s2Units, maxs1s2,
                maxs1s2Units, mins1f2, mins1f2Units, maxs1f2, maxs1f2Units,
                minf1s2, minf1s2Units, maxf1s2, maxf1s2Units, minf1f2,
                minf1f2Units, maxf1f2, maxf1f2Units);
        return relation;
    }

    static TemporalExtendedPropositionDefinition instanceToTemporalExtendedPropositionDefinition(
            Instance extendedProposition, ProtegeKnowledgeSourceBackend backend)
            throws KnowledgeSourceReadException {
        ConnectionManager cm = backend.getConnectionManager();
        String ad = propositionId(extendedProposition);

        String displayName = (String) cm.getOwnSlotValue(extendedProposition,
                cm.getSlot("displayName"));
        String abbrevDisplayName = (String) cm.getOwnSlotValue(
                extendedProposition, cm.getSlot("abbrevDisplayName"));

        TemporalExtendedPropositionDefinition result;
        if (isParameter(extendedProposition, cm)) {
            TemporalExtendedParameterDefinition r = new TemporalExtendedParameterDefinition(
                    ad);
            r.setValue(extendedParameterValue(extendedProposition, cm));
            result = r;
        } else {
            result = new TemporalExtendedPropositionDefinition(ad);
        }

        result.setDisplayName(displayName);
        result.setAbbreviatedDisplayName(abbrevDisplayName);
        result.setMinLength(Util.parseTimeConstraint(extendedProposition,
                "minDuration", cm));
        result.setMinLengthUnit(Util.parseUnitsConstraint(extendedProposition,
                "minDurationUnits", backend, cm));
        result.setMaxLength(Util.parseTimeConstraint(extendedProposition,
                "maxDuration", cm));
        result.setMaxLengthUnit(Util.parseUnitsConstraint(extendedProposition,
                "maxDurationUnits", backend, cm));
        Slot slot = cm.getSlot("propertyConstraints");
        if (slot == null) {
            logger().log(Level.WARNING,
                    "Ontology does not have a propertyConstraints slot, skipping for extended proposition definition {0}",
                    result.getDisplayName());
        } else {
            result.setPropertyConstraints(
                    instanceToPropertyConstraints(
                    cm.getOwnSlotValues(extendedProposition,
                    cm.getSlot("propertyConstraints")), backend));
        }
        return result;
    }

    private static PropertyConstraint[] instanceToPropertyConstraints(
            @SuppressWarnings("rawtypes") Collection propertyConstraints,
            ProtegeKnowledgeSourceBackend backend)
            throws KnowledgeSourceReadException {
        PropertyConstraint[] result = new PropertyConstraint[propertyConstraints.size()];
        int i = 0;
        ConnectionManager cm = backend.getConnectionManager();
        for (Object o : propertyConstraints) {
            Instance pci = (Instance) o;
            PropertyConstraint pc = new PropertyConstraint();
            Instance propertyInst = (Instance) cm.getOwnSlotValue(pci,
                    cm.getSlot("property"));
            pc.setPropertyName(propertyInst.getName());
            pc.setValueComp(HighLevelAbstractionConverter.STRING_TO_VAL_COMP_MAP.get((String) cm
                    .getOwnSlotValue(pci, cm.getSlot("valComp"))));
            pc.setValue(ValueType.VALUE.parse((String) cm.getOwnSlotValue(pci,
                    cm.getSlot("value"))));
            result[i++] = pc;
        }

        return result;
    }

    static Value extendedParameterValue(Instance extendedParamInstance,
            ConnectionManager cm) throws KnowledgeSourceReadException {
        Value result = null;
        String resultStr = null;
        Instance paramConstraint = (Instance) cm.getOwnSlotValue(
                extendedParamInstance, cm.getSlot("parameterValue"));
        if (paramConstraint != null) {
            resultStr = (String) cm.getOwnSlotValue(paramConstraint,
                    cm.getSlot("displayName"));
            if (resultStr != null) {
                result = NominalValue.getInstance(resultStr);
            }
        }
        return result;
    }

    static TemporalPatternOffset temporalOffsets(
            Instance abstractionInstance,
            ProtegeKnowledgeSourceBackend backend,
            Map<Instance, TemporalExtendedPropositionDefinition> extendedParameterCache)
            throws KnowledgeSourceReadException {
        ConnectionManager cm = backend.getConnectionManager();
        Instance temporalOffsetInstance = (Instance) cm.getOwnSlotValue(
                abstractionInstance, cm.getSlot("temporalOffsets"));
        if (temporalOffsetInstance != null) {
            TemporalPatternOffset temporalOffsets = new TemporalPatternOffset();
            Instance startExtendedParamInstance = (Instance) cm
                    .getOwnSlotValue(temporalOffsetInstance,
                    cm.getSlot("startExtendedProposition"));
            Instance finishExtendedParamInstance = (Instance) cm
                    .getOwnSlotValue(temporalOffsetInstance,
                    cm.getSlot("finishExtendedProposition"));
            if (startExtendedParamInstance != null) {
                temporalOffsets
                        .setStartTemporalExtendedPropositionDefinition(extendedParameterCache
                        .get(startExtendedParamInstance));
                temporalOffsets
                        .setStartAbstractParamValue(Util
                        .extendedParameterValue(
                        startExtendedParamInstance, cm));
            }

            if (finishExtendedParamInstance != null) {
                temporalOffsets
                        .setFinishTemporalExtendedPropositionDefinition(extendedParameterCache
                        .get(finishExtendedParamInstance));
                temporalOffsets
                        .setFinishAbstractParamValue(Util
                        .extendedParameterValue(
                        finishExtendedParamInstance, cm));
            }

            String startSideStr = (String) cm.getOwnSlotValue(
                    temporalOffsetInstance, cm.getSlot("startSide"));
            if (startSideStr != null) {
                temporalOffsets.setStartIntervalSide(Side
                        .valueOf(startSideStr.toUpperCase()));
            }
            String finishSideStr = (String) cm.getOwnSlotValue(
                    temporalOffsetInstance, cm.getSlot("finishSide"));
            if (finishSideStr != null) {
                temporalOffsets.setFinishIntervalSide(Side
                        .valueOf(finishSideStr.toUpperCase()));
            }
            Integer startOffset = Util.parseTimeConstraint(
                    temporalOffsetInstance, "startOffset", cm);
            if (startOffset != null) {
                temporalOffsets.setStartOffset(startOffset);
            }

            temporalOffsets.setStartOffsetUnits(Util.parseUnitsConstraint(
                    temporalOffsetInstance, "startOffsetUnits", backend, cm));
            Integer finishOffset = Util.parseTimeConstraint(
                    temporalOffsetInstance, "finishOffset", cm);
            if (finishOffset != null) {
                temporalOffsets.setFinishOffset(finishOffset);
            }
            temporalOffsets.setFinishOffsetUnits(Util.parseUnitsConstraint(
                    temporalOffsetInstance, "finishOffsetUnits", backend, cm));
            return temporalOffsets;
        } else {
            return null;
        }
    }

    /**
     * Returns whether a Protege instance is a Parameter.
     *
     * @param extendedParameter a Protege instance that is assumed to be a
     * Proposition.
     * @return <code>true</code> if the provided Protege instance is a
     * Parameter, <code>false</code> otherwise.
     */
    private static boolean isParameter(Instance extendedParameter,
            ConnectionManager cm) throws KnowledgeSourceReadException {
        Instance proposition = (Instance) cm.getOwnSlotValue(extendedParameter,
                cm.getSlot("proposition"));
        if (proposition.hasType(cm.getCls("Parameter"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the proposition id for an extended proposition definition.
     *
     * @param extendedProposition an ExtendedProposition.
     * @return a proposition id {@link String}.
     */
    private static String propositionId(Instance extendedProposition) {
        Instance proposition = (Instance) extendedProposition
                .getOwnSlotValue(extendedProposition.getKnowledgeBase()
                .getSlot("proposition"));
        if (proposition.hasType(proposition.getKnowledgeBase().getCls(
                "ConstantParameter"))) {
            throw new IllegalStateException(
                    "Constant parameters are not yet supported as "
                    + "components of a high level abstraction definition.");
        } else {
            return proposition.getName();
        }
    }

    private static Set<String> resolveAndLogDuplicates(Collection<?> isas,
            Logger logger, Instance propInstance, String slotName) {
        Set<String> inverseIsAs = new HashSet<>();
        for (Object isAInstance : isas) {
            String name = ((Instance) isAInstance).getName();
            if (!inverseIsAs.add(name) && logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Duplicate {0} in {1}: {2}",
                        new Object[]{slotName, propInstance.getName(), name});
            }
        }
        return inverseIsAs;
    }
}
