package org.protempa.proposition.value;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Ordinal string values.
 * 
 * @author Andrew Post
 */
public final class OrdinalValue extends ValueImpl implements OrderedValue {

    private static final long serialVersionUID = -1605459658420554439L;
    private final String val;
    private final List<String> allowedValues;
    private transient volatile int hashCode;

    /**
     * Creates an ordinal value of a type with allowed values.
     *
     * @param value
     *            a {@link String}.
     * @param sortedAllowedValues
     *            the allowed values {@link List<String}.
     */
    OrdinalValue(String value, List<String> sortedAllowedValues) {
        super(ValueType.ORDINALVALUE);
        this.val = value;
        this.allowedValues = new ArrayList<String>(sortedAllowedValues);
    }
    
    @Override
    public OrdinalValue replace() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.protempa.proposition.value.Value#getFormatted()
     */
    @Override
    public String getFormatted() {
        return val;
    }

    /**
     * Returns the value.
     *
     * @return a {@link String}.
     */
    public String getValue() {
        return val;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.protempa.proposition.value.ValueImpl#compareOrdinalValue(org.protempa.proposition.value.OrdinalValue)
     */
    @Override
    protected ValueComparator compareOrdinalValue(OrdinalValue ordVal) {
        if (allowedValues == null
                || val == null
                || ordVal.allowedValues == null
                || ordVal.val == null
                || !(allowedValues == ordVal.allowedValues || allowedValues.equals(ordVal.allowedValues))) {
            return ValueComparator.UNKNOWN;
        }

        int c = allowedValues.indexOf(val) - allowedValues.indexOf(ordVal.val);
        if (c == 0) {
            return ValueComparator.EQUAL_TO;
        } else if (c > 0) {
            return ValueComparator.GREATER_THAN;
        } else {
            return ValueComparator.LESS_THAN;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            if (val != null) {
                result = result * 37 + val.hashCode();
            }
            if (allowedValues != null) {
                result = result * 37 + allowedValues.hashCode();
            }
            hashCode = result;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrdinalValue other = (OrdinalValue) obj;
        if (!this.val.equals(other.val)) {
            return false;
        }
        if (this.allowedValues != other.allowedValues &&
                !this.allowedValues.equals(other.allowedValues)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ValueVisitor valueVisitor) {
        if (valueVisitor == null) {
            throw new IllegalArgumentException("valueVisitor cannot be null");
        }
        valueVisitor.visit(this);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
