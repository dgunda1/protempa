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
package org.protempa.proposition.value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Represents lists of values.
 * 
 * @author Andrew Post
 */
public class ValueList<V extends Value> extends ArrayList<V> implements Value {

    private static final long serialVersionUID = -6595541689042779012L;
    
    /**
     * Creates a list of the provided elements in the same order.
     * 
     * @param value zero or more values.
     * @return a list of the provided values.
     */
    public static <V extends Value> ValueList<V> getInstance(V... value) {
        ValueList<V> result = new ValueList<>(value.length);
        for (V val : value) {
            result.add(val);
        }
        return result;
    }
    
    public ValueList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Creates an empty list of values.
     */
    public ValueList() {
        super();
    }

    /**
     * Creates a list of values from the provided {@link List<V>} of values.
     * 
     * @param values a {@link List<V>} of values.
     */
    public ValueList(List<V> values) {
        super(values);
    }
    
    @Override
    public ValueList<V> replace() {
        return this;
    }

    /**
     * Compares a value list for membership in another value list. This method
     * does <b>NOT</b> compare two lists for equality. To do this, use 
     * {@link #equals(java.lang.Object) }.
     * 
     * @param o a {@link Value}.
     * @return if the provided value is a value list, returns 
     * {@link ValueComparator#IN} if the list is in the provided value list, or
     * {@link ValueComparator#NOT_IN} if not. If the provided value is
     * not a list, returns {@Link ValueComparator#UNKNOWN}.
     */
    @Override
    public ValueComparator compare(Value val) {
        if (val == null || val.getType() != ValueType.VALUELIST) {
            return ValueComparator.NOT_EQUAL_TO;
        }
        ValueList<?> vl = (ValueList<?>) val;
        return vl.contains(this) ? ValueComparator.IN : ValueComparator.NOT_IN;
    }

    @Override
    public String getFormatted() {
        List<String> l = new ArrayList<>(size());
        for (Value val : this) {
            if (val instanceof NominalValue) {
                l.add("'" + val.getFormatted() + "'");
            } else {
                l.add(val.getFormatted());
            }
        }
        return '[' + StringUtils.join(l, ", ") + ']';
    }
    
    @Override
    public String format(Format format) {
        if (format == null) {
            return getFormatted();
        } else {
            return format.format(this);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public ValueType getType() {
        return ValueType.VALUELIST;
    }

    @Override
    public void accept(ValueVisitor valueVisitor) {
        if (valueVisitor == null) {
            throw new IllegalArgumentException("valueVisitor cannot be null");
        }
        valueVisitor.visit(this);
    }
    
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.writeInt(size());
        for (Object val : this) {
            s.writeObject(val);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        int size = s.readInt();
        for (int i = 0; i < size; i++) {
            add((V) s.readObject());
        }
    }

    @Override
    public ValueBuilder asBuilder() {
        return new ValueListBuilder(this);
    }
}
