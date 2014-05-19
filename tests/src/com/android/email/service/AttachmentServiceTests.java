/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.email.service;

import android.test.suitebuilder.annotation.SmallTest;

import com.android.emailcommon.provider.EmailContent;

import junit.framework.TestCase;

/**
 * Tests of the AttachmentService
 *
 * You can run this entire test case with:
 *   runtest -c com.android.email.service.AttachmentServiceTests email
 */
@SmallTest
public class AttachmentServiceTests extends TestCase {

    public void testDownloadRequestIsEquals() {
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        final AttachmentService.DownloadRequest dr2 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 2);
        assertTrue(dr.equals(dr));
        assertFalse(dr.equals(dr2));
    }

    public void testDownloadQueueEmptyQueue() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        assertEquals(0, dq.getSize());
        assertTrue(dq.isEmpty());
    }

    public void testDownloadQueueAddRequest() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        final boolean result = dq.addRequest(dr);
        assertTrue(result);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());
    }

    public void testDownloadQueueAddRequestNull() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final boolean result = dq.addRequest(null);
        assertFalse(result);
        assertEquals(0, dq.getSize());
        assertTrue(dq.isEmpty());
    }

    public void testDownloadQueueAddRequestExisting() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        boolean result = dq.addRequest(dr);
        assertTrue(result);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());

        // Now try to add the same one again. The queue should remain the same size.
        result = dq.addRequest(dr);
        assertTrue(result);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());
    }

    public void testDownloadQueueRemoveRequest() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        boolean result = dq.addRequest(dr);
        assertTrue(result);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());

        // Now remove the request and check the status of the queue
        result = dq.removeRequest(dr);
        assertTrue(result);

        // The queue should be empty.
        assertEquals(0, dq.getSize());
        assertTrue(dq.isEmpty());
    }

    public void testDownloadQueueRemoveRequestNull() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        boolean result = dq.addRequest(dr);
        assertTrue(result);
        assertEquals(dq.getSize(), 1);
        assertFalse(dq.isEmpty());

        // Now remove the request and check the status of the queue
        result = dq.removeRequest(null);
        assertTrue(result);

        // The queue should still have 1.
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());
    }

    public void testDownloadQueueRemoveRequestDoesNotExist() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        boolean result = dq.addRequest(dr);
        assertTrue(result);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());

        // Generate a new request and try to remove it.
        result = dq.removeRequest(new AttachmentService.DownloadRequest(
                AttachmentService.PRIORITY_FOREGROUND, 2));
        assertFalse(result);

        // The queue should still have 1.
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());
    }

    public void testDownloadQueueFindRequestById() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        final boolean result = dq.addRequest(dr);
        assertTrue(result);

        final AttachmentService.DownloadRequest drResult = dq.findRequestById(1);
        assertNotNull(drResult);

        // Now let's make sure that these objects are the same
        assertEquals(dr, drResult);
    }

    public void testDownloadQueueFindRequestByIdInvalidId() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        final boolean result = dq.addRequest(dr);
        assertTrue(result);

        final AttachmentService.DownloadRequest drResult = dq.findRequestById(-1);
        assertNull(drResult);
    }

    public void testDownloadQueueFindRequestByIdUnknownId() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        final boolean result = dq.addRequest(dr);
        assertTrue(result);

        final AttachmentService.DownloadRequest drResult = dq.findRequestById(5);
        assertNull(drResult);
    }

    /**
     * This is just to test the FIFOness of our queue.  We test priorities in a latter
     * test case.
     */
    public void testDownloadQueueGetNextRequest() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        boolean result = dq.addRequest(dr);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr2 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_SEND_MAIL, 2);
        result = dq.addRequest(dr2);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr3 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_BACKGROUND, 3);
        result = dq.addRequest(dr3);
        assertTrue(result);

        assertEquals(3, dq.getSize());
        assertFalse(dq.isEmpty());

        AttachmentService.DownloadRequest drResult = dq.getNextRequest();
        assertEquals(dr, drResult);
        assertEquals(2, dq.getSize());
        assertFalse(dq.isEmpty());

        drResult = dq.getNextRequest();
        assertEquals(dr2, drResult);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());

        drResult = dq.getNextRequest();
        assertEquals(dr3, drResult);
        assertEquals(0, dq.getSize());
        assertTrue(dq.isEmpty());
    }

    public void testDownloadQueueGetNextRequestEmptyQueue() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();
        AttachmentService.DownloadRequest drResult = dq.getNextRequest();
        assertNull(drResult);
    }

    public void testDownloadQueueSizeReporting() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();

        // Start adding some download request objects, note that the empty queue case has been
        // tested in above.
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);

        // Add the first DownloadRequest to the queue
        boolean result = dq.addRequest(dr);
        assertTrue(result);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());

        // Add the same one again, the size should be the same.
        result = dq.addRequest(dr);
        assertTrue(result);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());

        final AttachmentService.DownloadRequest dr2 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 2);

        // Add another DownloadRequest
        result = dq.addRequest(dr2);
        assertTrue(result);
        assertEquals(2, dq.getSize());
        assertFalse(dq.isEmpty());

        final AttachmentService.DownloadRequest dr3 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 3);

        result = dq.addRequest(dr3);
        assertTrue(result);
        assertEquals(3, dq.getSize());
        assertFalse(dq.isEmpty());

        // Remove a request and check new size.
        AttachmentService.DownloadRequest returnRequest = dq.getNextRequest();
        assertNotNull(returnRequest);
        assertEquals(2, dq.getSize());
        assertFalse(dq.isEmpty());

        final AttachmentService.DownloadRequest dr4 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 4);

        // Adding the last DownloadRequest
        result = dq.addRequest(dr4);
        assertTrue(result);
        assertEquals(3, dq.getSize());
        assertFalse(dq.isEmpty());

        // Start removing all the final requests and check sizes.
        returnRequest = dq.getNextRequest();
        assertNotNull(returnRequest);
        assertEquals(2, dq.getSize());
        assertFalse(dq.isEmpty());

        returnRequest = dq.getNextRequest();
        assertNotNull(returnRequest);
        assertEquals(1, dq.getSize());
        assertFalse(dq.isEmpty());

        returnRequest = dq.getNextRequest();
        assertNotNull(returnRequest);
        assertEquals(0, dq.getSize());
        assertTrue(dq.isEmpty());
    }

    /**
     * Insert DownloadRequest obje cts in a random priority sequence and make sure that
     * The highest priority items come out of the queue first.
     */
    public void testDownloadQueueTestPriority() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();

        // Start adding some download request objects, note that the empty queue case has been
        // tested in above.
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        boolean result = dq.addRequest(dr);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr2 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_BACKGROUND, 2);
        result = dq.addRequest(dr2);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr3 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_SEND_MAIL, 3);
        result = dq.addRequest(dr3);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr4 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_SEND_MAIL, 4);
        result = dq.addRequest(dr4);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr5 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 5);
        result = dq.addRequest(dr5);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr6 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_BACKGROUND, 6);
        result = dq.addRequest(dr6);
        assertTrue(result);

        // Set the priority to the highest possible value and everything show be
        // in descending order.
        int lastPriority = AttachmentService.PRIORITY_HIGHEST;
        for (int i = 0; i < dq.getSize(); i++){
            final AttachmentService.DownloadRequest returnRequest = dq.getNextRequest();
            assertNotNull(returnRequest);
            final int requestPriority = returnRequest.mPriority;
            // The values should be going up or staying the same...indicating a lower priority
            assertTrue(requestPriority >= lastPriority);
            lastPriority = requestPriority;
        }
    }

    /**
     * Insert DownloadRequest objects in a random time based sequence and make sure that
     * The oldest requests come out of the queue first.
     */
    public void testDownloadQueueTestDate() {
        final AttachmentService.DownloadQueue dq = new AttachmentService.DownloadQueue();

        // Start adding some unique attachments but with the same priority
        final AttachmentService.DownloadRequest dr =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 1);
        boolean result = dq.addRequest(dr);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr2 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 2);
        result = dq.addRequest(dr2);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr3 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 3);
        result = dq.addRequest(dr3);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr4 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 4);
        result = dq.addRequest(dr4);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr5 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 5);
        result = dq.addRequest(dr5);
        assertTrue(result);

        final AttachmentService.DownloadRequest dr6 =
                new AttachmentService.DownloadRequest(AttachmentService.PRIORITY_FOREGROUND, 6);
        result = dq.addRequest(dr6);
        assertTrue(result);

        // The output should return requests in increasing time.
        long lastTime = 0;
        for (int i = 0; i < dq.getSize(); i++){
            final AttachmentService.DownloadRequest returnRequest = dq.getNextRequest();
            assertNotNull(returnRequest);
            final long requestTime = returnRequest.mTime;
            // The time should be going up.
            assertTrue(requestTime >= lastTime);
            lastTime = requestTime;
        }
    }
}