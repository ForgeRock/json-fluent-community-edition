/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2010 ApexIdentity Inc. All rights reserved.
 * Portions Copyrighted 2011 ForgeRock AS.
 */

package org.forgerock.json.fluent;

/**
 * An exception that is thrown during path operations.
 *
 * @author Paul C. Bryan
 */
public class JsonPathException extends Exception {

    /** Serializable class a version number. */
    static final long serialVersionUID = 1L;

    /** A textual description of the path exception. */
    private final String description;

    /** A string representation of the path exhibiting the exception. */
    private final String path;

    /** The approximate index in the path string of the exception. */
    private final int index;

    /**
     * Constructs a new exception with the specified description, path and index.
     *
     * @param description a textual description of the path exception.
     * @param path a string representation of the path exhibiting the exception.
     * @param index the approximate index in the path string of the exception.
     */
    public JsonPathException(String description, String path, int index) {
        this.description = description;
        this.path = path;
        this.index = index;
    }

    /**
     * Returns the description of the path syntax exception.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the path that was the source of the exception.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the approximate index of the path syntax exception.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the detail message string of this exception.
     */
    @Override
    public String getMessage() {
        return description + " near index " + index + " in \"" + path + "\"";
    }
}
