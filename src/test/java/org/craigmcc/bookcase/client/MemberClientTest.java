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

import org.craigmcc.bookcase.model.Book;
import org.craigmcc.bookcase.model.Member;
import org.craigmcc.bookcase.model.Series;
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

public class MemberClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final BookClient bookClient = new BookClient();
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

        List<Member> members = memberClient.findAll();
        assertThat(members.size(), is(greaterThan(0)));

        for (Member member : members) {

            // Delete and verify we can no longer retrieve it
            memberClient.delete(member.getId());
            assertThrows(NotFound.class,
                    () -> memberClient.find(member.getId()));

        }

        // We should have deleted all members
        assertThat(memberClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> memberClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {
        List<Member> members = memberClient.findAll();
        for (Member member : members) {
            Member found = memberClient.find(member.getId());
            assertThat(found.equals(member), is(true));
        }
    }

    @Test
    public void findNotFound() throws Exception {
        assertThrows(NotFound.class,
                () -> memberClient.find(Long.MAX_VALUE));
    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Member> members = memberClient.findAll();
        assertThat(members.size(), is(greaterThan(0)));

    }

    // findBySeriesId() tests

    public void findBySeriesIdHappy() throws Exception {

        List<Series> serieses = seriesClient.findAll();
        for (Series series : serieses) {
            List<Member> members = memberClient.findBySeriesId(series.getId());
            if (members.size() > 0) {
                Integer previousOrdinal = null;
                for (Member member : members) {
                    if (previousOrdinal != null) {
                        assertThat(member.getOrdinal(), is(greaterThan(previousOrdinal)));
                    }
                    previousOrdinal = member.getOrdinal();
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

        Member Member = newMember();
        Member inserted = memberClient.insert(Member);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        Member found = memberClient.find(inserted.getId());
        assertThat(found.equals(inserted), is(true));

    }

    @Test
    public void insertBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        // Completely empty instance
        final Member member0 = new Member();
        assertThrows(BadRequest.class,
                () -> memberClient.insert(member0));

        // Missing bookId field
        final Member member1 = newMember();
        member1.setBookId(null);
        assertThrows(BadRequest.class,
                () -> memberClient.insert(member1));

        // Invalid bookId field
        final Member member2 = newMember();
        member2.setBookId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> memberClient.insert(member2));

        // Missing seriesId field
        final Member member3 = newMember();
        member3.setSeriesId(null);
        assertThrows(BadRequest.class,
                () -> memberClient.insert(member3));

        // Invalid seriesId field
        final Member member4 = newMember();
        member4.setSeriesId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> memberClient.insert(member4));

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
        Member original = findFirstMember();

        // Update this entity
        Member member = original.clone();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }
        member.setOrdinal(member.getOrdinal() + 100);
        Member updated = memberClient.update(member.getId(), member);

        // Validate this entity
        assertThat(updated.getId(), is(member.getId()));
        assertThat(updated.getPublished(), is(member.getPublished()));
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
        Member original = findFirstMember();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            /* Ignore */;
        }

        // Missing bookId field
        final Member member1 = original.clone();
        member1.setBookId(null);
        assertThrows(BadRequest.class,
                () -> memberClient.update(member1.getId(), member1));

        // Invalid bookId field
        final Member member2 = original.clone();
        member2.setBookId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> memberClient.update(member2.getId(), member2));

        // Missing seriesId field
        final Member member3 = original.clone();
        member3.setSeriesId(null);
        assertThrows(BadRequest.class,
                () -> memberClient.update(member3.getId(), member3));

        // Invalid seriesId field
        final Member member4 = original.clone();
        member4.setSeriesId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> memberClient.update(member4.getId(), member4));

    }

    @Test
    public void updateNotUnique() throws Exception {
        if (disabled()) {
            return;
        }
        // No uniqueness constraints to test
    }

    // Private Methods -------------------------------------------------------

    private Book findFirstBook() throws Exception {
        List<Book> books = bookClient.findAll();
        return books.get(0);
    }

    private Member findFirstMember() throws Exception {
        List<Member> members = memberClient.findAll();
        assertThat(members.size(), is(greaterThan(0)));
        return members.get(0);
    }

    private Series findFirstSeries() throws Exception {
        List<Series> serieses = seriesClient.findAll();
        return serieses.get(0);
    }

    private Member newMember() throws Exception {
        return new Member(findFirstBook().getId(), 123, findFirstSeries().getId());
    }

}
