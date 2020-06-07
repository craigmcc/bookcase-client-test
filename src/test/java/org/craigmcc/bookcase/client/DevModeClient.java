/*
 * Copyright 2020 craigmcc.
 *
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
 */
package org.craigmcc.bookcase.client;

import org.craigmcc.bookcase.exception.Forbidden;
import org.craigmcc.bookcase.exception.InternalServerError;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DevModeClient extends AbstractClient {

    // Instance Variables ----------------------------------------------------

    private final WebTarget depopulateTarget = getBaseTarget()
            .path("devmode")
            .path("depopulate");

    private final WebTarget populateTarget = getBaseTarget()
            .path("devmode")
            .path("populate");

    // Public Methods --------------------------------------------------------

    public void depopulate() throws Forbidden, InternalServerError {

        Response response = depopulateTarget
            .request(MediaType.TEXT_PLAIN)
            .post(Entity.text(""));
        if (response.getStatus() == RESPONSE_FORBIDDEN) {
            throw new Forbidden(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_NO_CONTENT) {
            return;
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    public void populate() throws Forbidden, InternalServerError {

        Response response = populateTarget
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.text(""));
        if (response.getStatus() == RESPONSE_FORBIDDEN) {
            throw new Forbidden(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_NO_CONTENT) {
            return;
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

}
