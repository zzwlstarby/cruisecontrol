/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 500
 * Chicago, IL 60661 USA
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
package net.sourceforge.cruisecontrol.labelincrementers;

import net.sourceforge.cruisecontrol.LabelIncrementer;
import org.jdom.Element;
import org.apache.log4j.Category;

/**
 * This class provides a label incrementation compatible with CVS tagging.
 * This class expects the label format to be "x-y",
 * where x is any String and y is an integer.
 *
 * @author Jeff Brekke (Jeff.Brekke@qg.com)
 * @author alden almagro (alden@thoughtworks.com), Paul Julius (pdjulius@thoughtworks.com), ThoughtWorks, Inc. 2001
 */
public class CVSLabelIncrementer implements LabelIncrementer {

    /** enable logging for this class */
    private static Category log = Category.getInstance(CVSLabelIncrementer.class.getName());

    /**
     * Increments the label when a successful build occurs.
     * Assumes that the label will be in
     * the format of "x-y", where x can be anything, and y is an integer.
     * The y value will be incremented by one, the rest will remain the same.
     *
     * @param oldLabel Label from previous successful build.
     * @return Label to use for most recent successful build.
     */
    public String incrementLabel(String oldLabel, Element buildLog) {

        String prefix = oldLabel.substring(0, oldLabel.lastIndexOf("-") + 1);
        String suffix = oldLabel.substring(oldLabel.lastIndexOf("-") + 1, oldLabel.length());
        int i = Integer.parseInt(suffix);
        String newLabel = prefix + ++i;
        log.debug("Incrementing label: " + oldLabel + " -> " + newLabel);
        return newLabel;
    }

    /**
     *  This label incrementer should be called after the build.
     */
    public boolean isPreBuildIncrementer() {
        return false;
    }

    /**
     *  Verify that the label specified is a valid label.  In this case a valid label contains
     *  at least one '-' character, and an integer after the last occurrence of the '-' character.
     */
    public boolean isValidLabel(String label) {

        if(label.indexOf("-") < 0) {
            return false;
        }

        try {
            String suffix = label.substring(label.lastIndexOf("-") + 1, label.length());
            int i = Integer.parseInt(suffix);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
