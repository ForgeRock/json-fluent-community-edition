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
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 */

package org.forgerock.json.fluent;

/**
 * Interface for transforming a JSON node from one value to another. Applied during the
 * construction of a JSON node, and is inherited by child nodes.
 *
 * @author Paul C. Bryan
 */
public interface JsonTransformer {

    /**
     * Transforms the value of the specified JSON node. If a transformation is not applicable,
     * then this method return the node's value untransformed.
     *
     * @param source the JSON node containing the value to be transformed.
     * @return the transformed value (or untransformed if not applicable).
     * @throws JsonNodeException if an exception occured applying the transformation.
     */
    Object transform(JsonNode source) throws JsonNodeException;
}
