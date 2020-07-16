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

import org.craigmcc.bookcase.model.Author;
import org.craigmcc.bookcase.model.Book;
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

public class BookClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final AuthorClient authorClient = new AuthorClient();
    private final BookClient bookClient = new BookClient();
    private final MemberClient memberClient = new MemberClient();
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

        List<Book> books = bookClient.findAll();
        assertThat(books.size(), is(greaterThan(0)));

        for (Book book : books) {

/*          (Not true for actual test data)
            // Test data should not have any books with no members
            List<Member> members = memberClient.findByBookId(book.getId());
            assertThat(members.size(), greaterThan(0));
*/

/*          (Not true for actual test data)
            // Test data should not have any books with no stories
            List<Story> stories = storyClient.findByBookId(book.getId());
            assertThat(stories.size(), greaterThan(0));
*/

            // Delete and verify we can no longer retrieve it
            bookClient.delete(book.getId());
            assertThrows(NotFound.class,
                    () -> bookClient.find(book.getId()));

            // Delete should have cascaded to members and stories
/*          TODO - no endpoints or service methods for these two calls
            assertThat(memberClient.findByBookId(book.getId()).size(), is(0));
            assertThat(storyClient.findByBookId(book.getId()).size(), is(0));
*/

        }

        // We should have deleted all Books
        assertThat(bookClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> bookClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {
        List<Book> books = bookClient.findAll();
        for (Book book : books) {
            Book found = bookClient.find(book.getId());
            assertThat(found.equals(book), is(true));
        }
    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> bookClient.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Book> Books = bookClient.findAll();
        assertThat(Books, is(notNullValue()));
        assertThat(Books.size(), is(greaterThan(0)));

        String previousTitle = null;
        for (Book Book : Books) {
            if (previousTitle != null) {
                assertThat(Book.getTitle(), is(greaterThan(previousTitle)));
            }
            previousTitle = Book.getTitle();
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Book Book = newBook();
        Book inserted = bookClient.insert(Book);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        try {
            Book found = bookClient.find(inserted.getId());
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
        final Book book0 = new Book();
        assertThrows(BadRequest.class,
                () -> bookClient.insert(book0));

        // Missing authorId field
        final Book book1 = newBook();
        book1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> bookClient.insert(book1));

        // Invalid authorId field
        final Book book2 = newBook();
        book2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> bookClient.insert(book1));

        // Missing title field
        final Book book3 = newBook();
        book3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> bookClient.insert(book3));

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
        Book original = findFirstBookByTitle("by");

        // Update this entity
        Book book = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        book.setTitle(book.getTitle() + " Updated");
        Book updated = bookClient.update(book.getId(), book);

        // Validate this entity
        assertThat(updated.getId(), is(book.getId()));
        assertThat(updated.getPublished(), is(book.getPublished()));
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
        Book original = findFirstBookByTitle(" by ");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Missing authorId field
        final Book book1 = original.clone();
        book1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> bookClient.update(book1.getId(), book1));

        // Invalid authorId field
        final Book book2 = original.clone();
        book2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> bookClient.update(book2.getId(), book2));

        // Missing title field
        final Book book3 = original.clone();
        book3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> bookClient.update(book3.getId(), book3));

    }

    @Test
    public void updateNotUnique() throws Exception {
        if (disabled()) {
            return;
        }
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private Book findFirstBookByTitle(String title) throws Exception {
        List<Book> Books = bookClient.findAll();
        assertThat(Books.size(), is(greaterThan(0)));
        return Books.get(0);
    }

    private Book newBook() throws Exception {
        List<Author> authors = authorClient.findAll();
        assertThat(authors.size(), is(greaterThan(0)));
        return new Book(
                authors.get(0).getId(),
                Book.Location.OTHER,
                "Notes about New Book",
                true,
                "New Book");
    }

}
