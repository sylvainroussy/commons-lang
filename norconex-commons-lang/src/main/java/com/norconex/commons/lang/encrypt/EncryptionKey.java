/* Copyright 2015-2016 Norconex Inc.
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
 */
package com.norconex.commons.lang.encrypt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Pointer to the an encryption key, or the encryption key itself. An 
 * encryption key can be seen as equivalent to a secret key, 
 * passphrase or password.
 * @author Pascal Essiembre
 * @since 1.9.0
 * @see EncryptionUtil
 */
public class EncryptionKey {

    public enum Source { 
        /** Value is the actual key. */
        KEY, 
        /** Value is the path to a file containing the key. */
        FILE, 
        /** Value is the name of an environment variable containing the key. */
        ENVIRONMENT, 
        /** Value is the name of a JVM system property containing the key. */
        PROPERTY
    };

    private final String value;
    private final Source source;
    
    /**
     * Creates a new reference to an encryption key. The reference can either 
     * be the key itself, or a pointer to a file or environment variable
     * containing the key (as defined by the supplied value type).
     * @param value the encryption key
     * @param source the type of value
     */
    public EncryptionKey(String value, Source source) {
        super();
        this.value = value;
        this.source = source;
    }
    /**
     * Creates a new encryption key where the value is the actual key.
     * @param value the encryption key
     */
    public EncryptionKey(String value) {
        this(value, Source.KEY);
    }
    public String getValue() {
        return value;
    }
    public Source getSource() {
        return source;
    }

    /**
     * Locate the key according to its value type and return it.  This 
     * method will always resolve the value each type it is invoked and 
     * never caches the key, unless the key value specified at construction
     * time is the actual key.
     * @return encryption key or <code>null</code> if the key does not exist 
     * for the specified type
     */
    public String resolve() {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        if (source == null) {
            return value;
        }
        switch (source) {
        case KEY:
            return value;
        case FILE:
            return fromFile();
        case ENVIRONMENT:
            return fromEnv();
        case PROPERTY:
            return fromProperty();
        default:
            return null;
        }
    }
    
    private String fromEnv() {
        //TODO allow a flag to optionally throw an exception when null?
        return System.getenv(value);
    }
    
    private String fromProperty() {
        //TODO allow a flag to optionally throw an exception when null?
        return System.getProperty(value);
    }

    private String fromFile() {
        File file = new File(value);
        if (!file.isFile()) {
            throw new EncryptionException(
                    "Key file is not a file or does not exists: "
                    + file.getAbsolutePath());
        }
        try {
            //TODO allow a flag to optionally throw an exception when null?
            return new String(Files.readAllBytes(
                    Paths.get(value)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EncryptionException(
                    "Could not read key file.", e);
        }
    }
    
    //TODO do not use Apache Commons Lang below to avoid the dependency.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EncryptionKey)) {
            return false;
        }
        EncryptionKey other = (EncryptionKey) obj;
        if (source != other.source) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return "EncryptionKey [value=" + value + ", source=" + source + "]";
    }    
}
