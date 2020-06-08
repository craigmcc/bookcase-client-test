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

import org.craigmcc.bookcase.exception.BadRequest;
import org.craigmcc.bookcase.exception.NotFound;
import org.craigmcc.bookcase.model.Anthology;
import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.model.Story;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

public class StoryClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final AnthologyClient anthologyClient = new AnthologyClient();
    private final BookClient bookClient = new BookClient();
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

        List<Story> stories = storyClient.findAll();
        assertThat(stories.size(), is(greaterThan(0)));

        for (Story story : stories) {

            // Delete and verify we can no longer retrieve it
            storyClient.delete(story.getId());
            assertThrows(NotFound.class,
                    () -> storyClient.find(story.getId()));

        }

        // We should have deleted all Storys
        assertThat(storyClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> storyClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {
        List<Story> stories = storyClient.findAll();
        for (Story story : stories) {
            Story found = storyClient.find(story.getId());
            assertThat(found.equals(story), is(true));
        }
    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> storyClient.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Story> stories = storyClient.findAll();
        assertThat(stories.size(), is(greaterThan(0)));

    }

    // findByAnthologyId() tests

    public void findByAnthologyIdHappy() throws Exception {

        List<Anthology> anthologies = anthologyClient.findAll();
        for (Anthology anthology : anthologies) {
            List<Story> stories = storyClient.findByAnthologyId(anthology.getId());
            if (stories.size() > 0) {
                Integer previousOrdinal = null;
                for (Story story : stories) {
                    if (previousOrdinal != null) {
                        assertThat(story.getOrdinal(), is(greaterThan(previousOrdinal)));
                    }
                    previousOrdinal = story.getOrdinal();
                }
            }

        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Story Story = newStory();
        Story inserted = storyClient.insert(Story);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        Story found = storyClient.find(inserted.getId());
        assertThat(found.equals(inserted), is(true));

    }

    @Test
    public void insertBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        // Completely empty instance
        final Story story0 = new Story();
        assertThrows(BadRequest.class,
                () -> storyClient.insert(story0));

        // Missing bookId field
        final Story story1 = newStory();
        story1.setBookId(null);
        assertThrows(BadRequest.class,
                () -> storyClient.insert(story1));

        // Invalid bookId field
        final Story story2 = newStory();
        story2.setBookId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> storyClient.insert(story2));

        // Missing anthologyId field
        final Story story3 = newStory();
        story3.setAnthologyId(null);
        assertThrows(BadRequest.class,
                () -> storyClient.insert(story3));

        // Invalid anthologyId field
        final Story story4 = newStory();
        story4.setAnthologyId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> storyClient.insert(story4));

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
        Story original = findFirstStory();

        // Update this entity
        Story Story = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        Story.setOrdinal(Story.getOrdinal() + 100);
        Story updated = storyClient.update(Story);

        // Validate this entity
        assertThat(updated.getId(), is(Story.getId()));
        assertThat(updated.getPublished(), is(Story.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getOrdinal(), is(original.getOrdinal() + 100));

    }

    @Test
    public void updateBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        // Get original entity
        Story original = findFirstStory();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Completely empty instance
        final Story story0 = new Story();
        assertThrows(NotFound.class,
                () -> storyClient.update(story0));

        // Missing bookId field
        final Story story1 = original.clone();
        story1.setBookId(null);
        assertThrows(BadRequest.class,
                () -> storyClient.update(story1));

        // Invalid bookId field
        final Story story2 = original.clone();
        story2.setBookId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> storyClient.update(story2));

        // Missing anthologyId field
        final Story story3 = original.clone();
        story3.setAnthologyId(null);
        assertThrows(BadRequest.class,
                () -> storyClient.update(story3));

        // Invalid anthologyId field
        final Story story4 = original.clone();
        story4.setAnthologyId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> storyClient.update(story4));

    }

    @Test
    public void updateNotUnique() throws Exception {
        if (disabled()) {
            return;
        }
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private Anthology findFirstAnthology() throws Exception {
        List<Anthology> anthologies = anthologyClient.findAll();
        return anthologies.get(0);
    }

    private Book findFirstBook() throws Exception {
        List<Book> books = bookClient.findAll();
        return books.get(0);
    }

    private Story findFirstStory() throws Exception {
        List<Story> Storys = storyClient.findAll();
        assertThat(Storys.size(), is(greaterThan(0)));
        return Storys.get(0);
    }

    private Story newStory() throws Exception {
        return new Story(findFirstAnthology().getId(), findFirstBook().getId(), 123);
    }

}
