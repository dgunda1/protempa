package org.protempa.proposition;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.protempa.DataSourceType;
import org.protempa.DataSourceBackendDataSourceType;
import org.protempa.DerivedDataSourceType;
import org.protempa.proposition.value.Value;

/**
 * Abstract class for implementing the various kinds of propositions.
 * 
 * @author Andrew Post
 */
public abstract class AbstractProposition implements Proposition {

    private static final int DEFAULT_REFERENCE_LIST_SIZE = 100;
    /**
     * An identification <code>String</code> for this proposition.
     */
    private String id;
    private static volatile int nextHashCode = 17;
    protected volatile int hashCode;
    private PropertyChangeSupport changes;
    private Map<String, Value> properties;
    private Map<String, List<UniqueId>> references;
    private UniqueId key;
    private DataSourceType dataSourceType;

    /**
     * Creates a proposition with an id.
     *
     * @param id
     *            an identification <code>String</code> for this proposition.
     */
    AbstractProposition(String id) {
        initializeAbstractProposition(id);
    }

    protected AbstractProposition() {
    }

    protected void initializeAbstractProposition(String id) {
        if (id == null) {
            this.id = "";
        } else {
            this.id = id.intern();
        }
    }

    protected void initializeProperties() {
        if (this.properties == null) {
            this.properties = new LinkedHashMap<String, Value>();
        }
    }

    protected void initializeReferences() {
        if (this.references == null) {
            this.references = new LinkedHashMap<String,
                    List<UniqueId>>();
        }
    }

    protected void initializePropertyChangeSupport() {
        this.changes = new PropertyChangeSupport(this);
    }

    @Override
    public String getId() {
        return this.id;
    }

    public final void setProperty(String name, Value value) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        initializeProperties();
        this.properties.put(name.intern(), value);
    }

    @Override
    public final Value getProperty(String name) {
        if (this.properties == null) {
            return null;
        } else {
            return this.properties.get(name);
        }
    }

    @Override
    public final String[] getPropertyNames() {
        if (this.properties == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        } else {
            Set<String> propNames = this.properties.keySet();
            return propNames.toArray(new String[propNames.size()]);
        }
    }

    public final void setUniqueIdentifier(UniqueId o) {
        this.key = o;
    }

    @Override
    public DataSourceType getDataSourceType() {
        return this.dataSourceType;
    }

    public void setDataSourceType(DataSourceType type) {
        this.dataSourceType = type;
    }

    @Override
    public final UniqueId getUniqueId() {
        return this.key;
    }

    public final void setReferences(String name, List<UniqueId> refs) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        initializeReferences();
        this.references.put(name.intern(), new ArrayList<UniqueId>(refs));
    }

    public final void addReference(String name, UniqueId ref) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (ref == null) {
            throw new IllegalArgumentException("ref cannot be null");
        }
        initializeReferences();
        List<UniqueId> refs = this.references.get(name);
        if (refs == null) {
            refs =
                    new ArrayList<UniqueId>(DEFAULT_REFERENCE_LIST_SIZE);
            refs.add(ref);
            this.references.put(name.intern(), refs);
        } else {
            refs.add(ref);
        }
    }

    @Override
    public final List<UniqueId> getReferences(String name) {
        if (this.references == null) {
            return Collections.emptyList();
        } else {
            List<UniqueId> result = this.references.get(name);
            if (result != null) {
                return Collections.unmodifiableList(result);
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public final String[] getReferenceNames() {
        if (this.references == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        } else {
            Set<String> refNames = this.references.keySet();
            return refNames.toArray(new String[refNames.size()]);
        }
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        initializePropertyChangeSupport();
        this.changes.addPropertyChangeListener(l);
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (this.changes != null) {
            this.changes.removePropertyChangeListener(l);
        }
    }

    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = nextHashCode;
            nextHashCode *= 37;
        }
        return this.hashCode;
    }

    @Override
    public boolean isEqual(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AbstractProposition)) {
            return false;
        }

        AbstractProposition p = (AbstractProposition) other;
        return (id == p.id || id.equals(p.id))
                && this.properties == p.properties ||
                (this.properties != null && this.properties.equals(p.properties));

    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append("properties", this.properties).append("references", this.references).append("uniqueIdentifier", this.key).append("dataSourceType", this.dataSourceType).toString();

    }
    // The following code implements hashCode() and equals() using unique 
    // identifiers, as well as the datasource backend identifiers.
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    /*
    @Override
    public int hashCode() {
    if (this.hashCode == 0) {
    if (this.key == null
    || this.datasourceBackendId == null) {
    this.hashCode = super.hashCode();
    } else {
    this.hashCode = 17;
    this.hashCode += 37 * this.key.hashCode();
    this.hashCode += 37 * this.datasourceBackendId.hashCode();
    }
    }
    return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
    if (this == obj) {
    return true;
    } else {
    if (!(obj instanceof Proposition)) {
    return false;
    }
    Proposition prop = (Proposition) obj;
    if (prop.getUniqueIdentifier() == null
    || this.key == null
    || prop.getDataSourceBackendId() == null
    || this.datasourceBackendId == null) {
    return false;
    } else {
    return prop.getUniqueIdentifier().equals(
    this.key)
    && prop.getDataSourceBackendId().equals(
    this.datasourceBackendId);
    }
    }
    }
     */

    protected void writeAbstractProposition(ObjectOutputStream s)
            throws IOException {
        s.writeObject(this.id);

        if (this.properties == null) {
            s.writeInt(0);
        } else {
            s.writeInt(this.properties.size());
            for (Map.Entry<String, Value> me : this.properties.entrySet()) {
                String propertyName = me.getKey();
                Value val = me.getValue();
                s.writeObject(propertyName);
                s.writeObject(val);
            }
        }

        if (this.references == null) {
            s.writeInt(0);
        } else {
            s.writeInt(this.references.size());
            for (Map.Entry<String, List<UniqueId>> me : this.references.entrySet()) {
                s.writeObject(me.getKey());
                List<UniqueId> val = me.getValue();
                if (val == null) {
                    s.writeInt(0);
                } else {
                    s.writeInt(val.size());
                    for (UniqueId uid : val) {
                        s.writeObject(uid);
                    }
                }
            }
        }

        s.writeObject(this.key);
        if (this.dataSourceType instanceof DerivedDataSourceType) {
            s.writeBoolean(true);
        } else {
            s.writeBoolean(false);
            s.writeObject(
                    ((DataSourceBackendDataSourceType) this.dataSourceType).getId());
        }
        s.writeObject(this.changes);
    }

    protected void readAbstractProposition(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        String tempId = (String) s.readObject();
        initializeAbstractProposition(tempId);

        int numProperties = s.readInt();
        if (numProperties < 0) {
            throw new InvalidObjectException(
                    "Negative properties count. Can't restore");
        }
        if (numProperties > 0) {
            for (int i = 0; i < numProperties; i++) {
                String propertyName = (String) s.readObject();
                Value val = (Value) s.readObject();
                if (val != null) {
                    val = val.replace();
                }
                setProperty(propertyName, val);
            }
        }
        
        int numRefs = s.readInt();
        if (numRefs < 0) {
            throw new InvalidObjectException(
                    "Negative reference count. Can't restore");
        }
        if (numRefs > 0) {
            for (int i = 0; i < numRefs; i++) {
                String refName = (String) s.readObject();
                int numUids = s.readInt();
                if (numUids < 0) {
                    throw new InvalidObjectException(
                            "Negative unique identifier count. Can't restore");
                }
                List<UniqueId> uids =
                        new ArrayList<UniqueId>(numUids);
                for (int j = 0; j < numUids; j++) {
                    uids.add((UniqueId) s.readObject());
                }
                setReferences(refName, uids);
            }
        }

        setUniqueIdentifier((UniqueId) s.readObject());
        if (s.readBoolean()) {
            setDataSourceType(DerivedDataSourceType.getInstance());
        } else {
            setDataSourceType(DataSourceBackendDataSourceType.getInstance(
                    (String) s.readObject()));
        }
        this.changes = (PropertyChangeSupport) s.readObject();
    }
}
