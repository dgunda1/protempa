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

import org.protempa.backend.dsb.DataValidationEvent;

/**
 *
 * @author Andrew Post
 */
public final class DataSourceFailedDataValidationException extends DataSourceException {
    private static final long serialVersionUID = 4682398146182167907L;
    
    private static final DataValidationEvent[] EMPTY_VALIDATION_EVENTS_ARRAY =
            new DataValidationEvent[0];
    private final DataValidationEvent[] validationEvents;

    public DataSourceFailedDataValidationException(Throwable cause, DataValidationEvent[] validationEvents) {
        super(cause);
        if (validationEvents == null) {
            this.validationEvents = EMPTY_VALIDATION_EVENTS_ARRAY;
        } else {
            this.validationEvents = validationEvents.clone();
        }
    }

    public DataSourceFailedDataValidationException(String message, Throwable cause, DataValidationEvent[] validationEvents) {
        super(message, cause);
        if (validationEvents == null) {
            this.validationEvents = EMPTY_VALIDATION_EVENTS_ARRAY;
        } else {
            this.validationEvents = validationEvents.clone();
        }
    }

    public DataSourceFailedDataValidationException(String message, DataValidationEvent[] validationEvents) {
        super(message);
        if (validationEvents == null) {
            this.validationEvents = EMPTY_VALIDATION_EVENTS_ARRAY;
        } else {
            this.validationEvents = validationEvents.clone();
        }
    }

    public DataSourceFailedDataValidationException(DataValidationEvent[] validationEvents) {
        if (validationEvents == null) {
            this.validationEvents = EMPTY_VALIDATION_EVENTS_ARRAY;
        } else {
            this.validationEvents = validationEvents.clone();
        }
    }

    public DataValidationEvent[] getValidationEvents() {
        return this.validationEvents.clone();
    }

}
