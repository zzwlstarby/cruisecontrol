/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2007, ThoughtWorks, Inc.
 * 200 E. Randolph, 25th Floor
 * Chicago, IL 60601 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package net.sourceforge.cruisecontrol.dashboard;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.DataUtils;

public class BuildSummaryStatisticsTest extends TestCase {
    private List list = new ArrayList();

    protected void setUp() throws Exception {
        createBuildSummaryAndPutInMap("project1", CurrentStatus.WAITING, PreviousResult.PASSED);
        createBuildSummaryAndPutInMap("project2", CurrentStatus.WAITING, PreviousResult.FAILED);
        createBuildSummaryAndPutInMap("project3", CurrentStatus.BUILDING, PreviousResult.FAILED);
        createBuildSummaryAndPutInMap("project4", CurrentStatus.WAITING, PreviousResult.FAILED);
        createBuildSummaryAndPutInMap("project5", CurrentStatus.WAITING, PreviousResult.UNKNOWN);
        createBuildSummaryAndPutInMap("project6", CurrentStatus.WAITING, PreviousResult.UNKNOWN);
        createBuildSummaryAndPutInMap("project7", CurrentStatus.DISCONTINUED, PreviousResult.FAILED);
    }

    public void testShouldReturnBeAbleToReturnHashContainsSummaryInfomation() {
        BuildSummaryStatistics statistics = new BuildSummaryStatistics(list);
        assertEquals(new Integer(2), statistics.failed());
        assertEquals(new Integer(1), statistics.building());
        assertEquals(new Integer(1), statistics.passed());
        assertEquals(new Integer(2), statistics.inactive());
        assertEquals(new Integer(1), statistics.discontinued());
        assertEquals(new Integer(4), statistics.total());
        assertEquals("25%", statistics.rate());
    }

    public void testShouldReturnBeAbleToReturnZeroAsTheDetaulValue() {
        BuildSummaryStatistics statistics = new BuildSummaryStatistics(new ArrayList());
        assertEquals(new Integer(0), statistics.failed());
        assertEquals(new Integer(0), statistics.building());
        assertEquals(new Integer(0), statistics.passed());
        assertEquals(new Integer(0), statistics.inactive());
        assertEquals(new Integer(0), statistics.total());
        assertEquals("0%", statistics.rate());
    }

    private void createBuildSummaryAndPutInMap(String name, CurrentStatus status, PreviousResult result) {
        String filename = DataUtils.PASSING_BUILD_LBUILD_0_XML;
        BuildSummary build = new BuildSummary(name, result, filename);
        build.updateStatus(status.getCruiseStatus());
        list.add(build);
    }

}
