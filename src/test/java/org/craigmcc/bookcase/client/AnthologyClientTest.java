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

import org.craigmcc.bookcase.model.Anthology;
import org.craigmcc.bookcase.model.Author;
import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.model.Story;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class AnthologyClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final AnthologyClient anthologyClient = new AnthologyClient();
    private final AuthorClient authorClient = new AuthorClient();
    private final StoryClient storyClient = new StoryClient();

    // Lifecycle Methods -----------------------------------------------------

    @Before
    public void before() {
        if ((depopulateEnabled == null) || (TRUE == depopulateEnabled)) {
            depopulate();
        }
        if ((populateEnabled == null) || (TRUE == populateEnabled)) {
            populate();
        }
    }

    // Test Methods ----------------------------------------------------------

    // delete() tests

    @Test
    public void deleteHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Anthology> anthologies = anthologyClient.findAll();
        assertThat(anthologies.size(), is(greaterThan(0)));

        for (Anthology anthology : anthologies) {

            // Test data should not have any anthologies with no stories
            List<Story> stories = storyClient.findByAnthologyId(anthology.getId());
            assertThat(stories.size(), greaterThan(0));

            // Delete and verify we can no longer retrieve it
            anthologyClient.delete(anthology.getId());
            assertThrows(NotFound.class,
                    () -> anthologyClient.find(anthology.getId()));

            // Delete should have cascaded to stories
            assertThat(storyClient.findByAnthologyId(anthology.getId()).size(), is(0));

        }

        // We should have deleted all anthologies
        assertThat(anthologyClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> anthologyClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {
        List<Anthology> anthologies = anthologyClient.findAll();
        for (Anthology anthology : anthologies) {
            Anthology found = anthologyClient.find(anthology.getId());
            assertThat(found.equals(anthology), is(true));
        }
    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> anthologyClient.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Anthology> anthologies = anthologyClient.findAll();
        assertThat(anthologies, is(notNullValue()));
        assertThat(anthologies.size(), is(greaterThan(0)));

        String previousTitle = null;
        for (Anthology anthology : anthologies) {
            if (previousTitle != null) {
                assertThat(anthology.getTitle(), is(greaterThan(previousTitle)));
            }
            previousTitle = anthology.getTitle();
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Anthology anthology = newAnthology();
        Anthology inserted = anthologyClient.insert(anthology);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        try {
            Anthology found = anthologyClient.find(inserted.getId());
            assertThat(found.equals(inserted), is(true));
        } catch (Exception e) {
            fail("Should not have thrown an exception: " + e.getMessage());
        }

    }

    @Test
    public void insertBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        // Completely empty instance
        final Anthology anthology0 = new Anthology();
        assertThrows(BadRequest.class,
                () -> anthologyClient.insert(anthology0));

        // Missing authorId field
        final Anthology anthology1 = newAnthology();
        anthology1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> anthologyClient.insert(anthology1));

        // Invalid authorId field
        final Anthology anthology2 = newAnthology();
        anthology2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> anthologyClient.insert(anthology1));

        // Missing title field
        final Anthology anthology3 = newAnthology();
        anthology3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> anthologyClient.insert(anthology3));

    }

    @Test
    public void insertNotUnique() throws Exception {
        if (disabled()) {
            return;
        }
        // No uniqueness constraints to test
    }

    // update() tests --------------------------------------------------------

    @Test
    public void updateHappy() throws Exception {

        if (disabled()) {
            return;
        }

        // Get original entity
        Anthology original = findFirstAnthologyByTitle("by");

        // Update this entity
        Anthology anthology = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        anthology.setTitle(anthology.getTitle() + " Updated");
        Anthology updated = anthologyClient.update(anthology.getId(), anthology);

        // Validate this entity
        assertThat(updated.getId(), is(anthology.getId()));
        assertThat(updated.getPublished(), is(anthology.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getTitle(), is(original.getTitle() + " Updated"));

    }

    @Test
    public void updateBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        // Get original entity
        Anthology original = findFirstAnthologyByTitle(" by ");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Missing authorId field
        final Anthology anthology1 = original.clone();
        anthology1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> anthologyClient.update(anthology1.getId(), anthology1));

        // Invalid authorId field
        final Anthology anthology2 = original.clone();
        anthology2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> anthologyClient.update(anthology2.getId(), anthology2));

        // Missing title field
        final Anthology anthology3 = original.clone();
        anthology3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> anthologyClient.update(anthology3.getId(), anthology3));

    }

    @Test
    public void updateNotUnique() throws Exception {
        if (disabled()) {
            return;
        }
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private Anthology findFirstAnthologyByTitle(String title) throws Exception {
        List<Anthology> anthologies = anthologyClient.findAll();
        assertThat(anthologies.size(), is(greaterThan(0)));
        return anthologies.get(0);
    }

    private Anthology newAnthology() throws Exception {
        List<Author> authors = authorClient.findAll();
        assertThat(authors.size(), is(greaterThan(0)));
        return new Anthology(
                authors.get(0).getId(),
                Book.Location.OTHER,
                "Notes about New Anthology",
                true,
                "New Anthology");
    }

}
