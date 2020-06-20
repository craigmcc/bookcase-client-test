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
import org.craigmcc.bookcase.model.Series;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class AuthorClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final AnthologyClient anthologyClient = new AnthologyClient();
    private final AuthorClient authorClient = new AuthorClient();
    private final BookClient bookClient = new BookClient();
    private final SeriesClient seriesClient = new SeriesClient();

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

        List<Author> authors = authorClient.findAll();
        assertThat(authors.size(), is(greaterThan(0)));

        for (Author author : authors) {

/*          (Not true for actual test data)
            // Test data should not have any authors with no anthologies
            List<Anthology> anthologies = findAnthologiesByAuthorId(author.getId());
            assertThat(anthologies.size(), greaterThan(0));
*/

/*          (Not true for actual test data)
            // Test data should not have any authors with no books
            List<Book> books = findBooksByAuthorId(author.getId());
            assertThat(books.size(), greaterThan(0));
*/

/*          (Not true for actual test data)
            // Test data should not have any authors with no series
            List<Series> series = findSeriesByAuthorId(author.getId());
            assertThat(series.size(), greaterThan(0));
*/

            // Delete and verify we can no longer retrieve it
            authorClient.delete(author.getId());
            assertThrows(NotFound.class,
                    () -> authorClient.find(author.getId()));

            // Delete should have cascaded to anthologies/books/series
            assertThat(findAnthologiesByAuthorId(author.getId()).size(), is(0));
            assertThat(findBooksByAuthorId(author.getId()).size(), is(0));
            assertThat(findSeriesByAuthorId(author.getId()).size(), is(0));

        }

        // We should have deleted all authors
        assertThat(authorClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> authorClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {
        List<Author> authors = authorClient.findAll();
        for (Author author : authors) {
            Author found = authorClient.find(author.getId());
            assertThat(found.equals(author), is(true));
        }
    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> authorClient.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Author> authors = authorClient.findAll();
        assertThat(authors, is(notNullValue()));
        assertThat(authors.size(), is(greaterThan(0)));

        String previousName = null;
        for (Author author : authors) {
            String thisName = author.getLastName() + "|" + author.getFirstName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Author author = newAuthor();
        Author inserted = authorClient.insert(author);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        try {
            Author found = authorClient.find(inserted.getId());
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
        final Author author0 = new Author();
        assertThrows(BadRequest.class,
                () -> authorClient.insert(author0));

        // Missing firstName field
        final Author author1 = newAuthor();
        author1.setFirstName(null);
        assertThrows(BadRequest.class,
                () -> authorClient.insert(author1));

        // Missing lastName field
        final Author author2 = newAuthor();
        author2.setLastName(null);
        assertThrows(BadRequest.class,
                () -> authorClient.insert(author2));

    }

    @Test
    public void insertNotUnique() throws Exception {

        if (disabled()) {
            return;
        }

        Author author = new Author("Barney", "Rubble", "New Notes about Barney");
        author.setId(null);
        assertThrows(NotUnique.class,
                () -> authorClient.insert(author));

    }

    // update() tests --------------------------------------------------------

    @Test
    public void updateHappy() throws Exception {

        if (disabled()) {
            return;
        }

        // Get original entity
        Author original = findFirstAuthorByName("Pebbles");

        // Update this entity
        Author Author = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        Author.setFirstName(Author.getFirstName() + " Updated");
        Author updated = authorClient.update(Author);

        // Validate this entity
        assertThat(updated.getId(), is(Author.getId()));
        assertThat(updated.getPublished(), is(Author.getPublished()));
        assertThat(updated.getUpdated(), is(greaterThan(original.getUpdated())));
        assertThat(updated.getVersion(), is(greaterThan(original.getVersion())));
        assertThat(updated.getFirstName(), is(original.getFirstName() + " Updated"));
        assertThat(updated.getLastName(), is(original.getLastName()));

    }

    @Test
    public void updateBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        // Get original entity
        Author original = findFirstAuthorByName("Rubble");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Completely empty instance
        final Author author0 = new Author();
        assertThrows(NotFound.class,
                () -> authorClient.update(author0));

        // Missing firstName field
        final Author author1 = original.clone();
        author1.setFirstName(null);
        assertThrows(BadRequest.class,
                () -> authorClient.update(author1));

        // Missing lastName field
        final Author author2 = original.clone();
        author1.setLastName(null);
        assertThrows(BadRequest.class,
                () -> authorClient.update(author1));

    }

    @Test
    public void updateNotUnique() throws Exception {

        if (disabled()) {
            return;
        }

        Author author = findFirstAuthorByName("Flintstone");
        author.setFirstName("Barney");
        author.setLastName("Rubble");
        assertThrows(NotUnique.class,
                () -> authorClient.update(author));

    }

    // Private Methods -------------------------------------------------------

    private List<Anthology> findAnthologiesByAuthorId(Long authorId) throws Exception {
        List<Anthology> in = anthologyClient.findAll();
        List<Anthology> out = new ArrayList<>();
        for (Anthology check : in) {
            if (authorId.equals(check.getAuthorId())) {
                out.add(check);
            }
        }
        return out;
    }

    private Author findFirstAuthorByName(String name) throws Exception {
        List<Author> authors = authorClient.findByName(name);
        assertThat(authors.size(), is(greaterThan(0)));
        return authors.get(0);
    }

    private List<Book> findBooksByAuthorId(Long authorId) throws Exception {
        List<Book> in = bookClient.findAll();
        List<Book> out = new ArrayList<>();
        for (Book check : in) {
            if (authorId.equals(check.getAuthorId())) {
                out.add(check);
            }
        }
        return out;
    }

    private List<Series> findSeriesByAuthorId(Long authorId) throws Exception {
        List<Series> in = seriesClient.findAll();
        List<Series> out = new ArrayList<>();
        for (Series check : in) {
            if (authorId.equals(check.getAuthorId())) {
                out.add(check);
            }
        }
        return out;
    }

    private Author newAuthor() throws Exception {
        List<Author> authors = authorClient.findAll();
        assertThat(authors.size(), is(greaterThan(0)));
        return new Author(
                "Another",
                "Rubble",
                "Notes about Another Rubble");
    }

}
