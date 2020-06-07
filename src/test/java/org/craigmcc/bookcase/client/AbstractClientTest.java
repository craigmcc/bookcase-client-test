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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    protected Boolean depopulateEnabled = null;
    protected Boolean populateEnabled = null;

    protected DevModeClient devModeClient = new DevModeClient();

    // Protected Methods -----------------------------------------------------

    protected boolean depopulate() {
        if (FALSE.equals(depopulateEnabled)) {
            return false;
        }
        try {
            devModeClient.depopulate();
            depopulateEnabled = TRUE;
        } catch (Forbidden e) {
            depopulateEnabled = FALSE;
        } catch (InternalServerError e) {
            depopulateEnabled = FALSE;
        }
        return depopulateEnabled;
    }

    protected boolean disabled() {
        return FALSE.equals(depopulateEnabled) || FALSE.equals(populateEnabled);
    }

    protected boolean populate() {
        if (FALSE.equals(populateEnabled)) {
            return false;
        }
        try {
            devModeClient.populate();
            populateEnabled = TRUE;
        } catch (Forbidden e) {
            populateEnabled = FALSE;
        } catch (InternalServerError e) {
            populateEnabled = FALSE;
        }
        return populateEnabled;
    }

}
