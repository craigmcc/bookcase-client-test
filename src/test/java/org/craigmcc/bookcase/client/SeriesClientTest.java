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
import org.craigmcc.bookcase.model.Author;
import org.craigmcc.bookcase.model.Series;
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

public class SeriesClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final AuthorClient authorClient = new AuthorClient();
    private final MemberClient memberClient = new MemberClient();
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

        List<Series> serieses = seriesClient.findAll();
        assertThat(serieses.size(), is(greaterThan(0)));

        for (Series series : serieses) {

/*          (Not true for the actual test data)
            // Test data should not have any serieses with no members
            List<Member> members = memberClient.findBySeriesId(series.getId());
            assertThat(members.size(), greaterThan(0));
*/

            // Delete and verify we can no longer retrieve it
            seriesClient.delete(series.getId());
            assertThrows(NotFound.class,
                    () -> seriesClient.find(series.getId()));

            // Delete should have cascaded to members
            assertThat(memberClient.findBySeriesId(series.getId()).size(), is(0));

        }

        // We should have deleted all serieses
        assertThat(seriesClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> seriesClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {
        List<Series> serieses = seriesClient.findAll();
        for (Series series : serieses) {
            Series found = seriesClient.find(series.getId());
            assertThat(found.equals(series), is(true));
        }
    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> seriesClient.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Series> serieses = seriesClient.findAll();
        assertThat(serieses, is(notNullValue()));
        assertThat(serieses.size(), is(greaterThan(0)));

        String previousTitle = null;
        for (Series series : serieses) {
            if (previousTitle != null) {
                assertThat(series.getTitle(), is(greaterThan(previousTitle)));
            }
            previousTitle = series.getTitle();
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Series series = newSeries();
        Series inserted = seriesClient.insert(series);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        try {
            Series found = seriesClient.find(inserted.getId());
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
        final Series series0 = new Series();
        assertThrows(BadRequest.class,
                () -> seriesClient.insert(series0));

        // Missing authorId field
        final Series series1 = newSeries();
        series1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> seriesClient.insert(series1));

        // Invalid authorId field
        final Series series2 = newSeries();
        series2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> seriesClient.insert(series1));

        // Missing title field
        final Series series3 = newSeries();
        series3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> seriesClient.insert(series3));

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
        Series original = findFirstSeriesByTitle("by");

        // Update this entity
        Series series = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        series.setTitle(series.getTitle() + " Updated");
        Series updated = seriesClient.update(series);

        // Validate this entity
        assertThat(updated.getId(), is(series.getId()));
        assertThat(updated.getPublished(), is(series.getPublished()));
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
        Series original = findFirstSeriesByTitle(" by ");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Completely empty instance
        final Series series0 = new Series();
        assertThrows(NotFound.class,
                () -> seriesClient.update(series0));

        // Missing authorId field
        final Series series1 = original.clone();
        series1.setAuthorId(null);
        assertThrows(BadRequest.class,
                () -> seriesClient.update(series1));

        // Invalid authorId field
        final Series series2 = original.clone();
        series2.setAuthorId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> seriesClient.update(series1));

        // Missing title field
        final Series series3 = original.clone();
        series3.setTitle(null);
        assertThrows(BadRequest.class,
                () -> seriesClient.update(series3));

    }

    @Test
    public void updateNotUnique() throws Exception {
        if (disabled()) {
            return;
        }
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private Series findFirstSeriesByTitle(String title) throws Exception {
        List<Series> serieses = seriesClient.findAll();
        assertThat(serieses.size(), is(greaterThan(0)));
        return serieses.get(0);
    }

    private Series newSeries() throws Exception {
        List<Author> authors = authorClient.findAll();
        assertThat(authors.size(), is(greaterThan(0)));
        return new Series(
                authors.get(0).getId(),
                "Notes about New Series",
                "New Series");
    }

}
