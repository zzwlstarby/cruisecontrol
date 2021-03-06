/****************************************************************************
* CruiseControl, a Continuous Integration Toolkit
* Copyright (c) 2001, ThoughtWorks, Inc.
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
****************************************************************************/

package net.sourceforge.cruisecontrol.distributed;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sourceforge.cruisecontrol.distributed.core.PropertiesHelper;

import org.apache.log4j.Logger;

class SearchablePropertyEntries {

    private static final Logger LOG = Logger.getLogger(SearchablePropertyEntries.class);

    private static final String OS_NAME = "os.name";
    /** As of jdk 1.6.0_04+, this shows the hotspot vm verson, like 10.0-b19. */
    private static final String JAVA_VM_VERSION = "java.vm.version";
    private static final String JAVA_VERSION = "java.version";
    public static final String HOSTNAME = "hostname";
    // @todo Use enumeration when min JRE version allows it...
    public static final String[] SYSTEM_ENTRY_KEYS = new String[] {
            OS_NAME, JAVA_VM_VERSION, JAVA_VERSION, HOSTNAME
    };

    private final Properties entryProperties = new Properties();

    public Properties getProperties() {
        return entryProperties;
    }

    public SearchablePropertyEntries(final String userDefinedPropertiesFilename) {
        entryProperties.putAll(getSystemEntryProps());

        final Map tempProperties = PropertiesHelper.loadOptionalProperties(userDefinedPropertiesFilename);
        for (Iterator iter = tempProperties.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String value = (String) tempProperties.get(key);
            entryProperties.put(key, value);
            LOG.debug("Set user-defined search entry " + key + " to: " + value);
        }
    }

    static Properties getSystemEntryProps() {
        final Properties systemEntryProps = new Properties();

        final String osName = System.getProperty(OS_NAME);
        systemEntryProps.put(OS_NAME, osName);
        LOG.debug("Set search entry " + OS_NAME + " to: " + osName);

        final String javaVmVersion = System.getProperty(JAVA_VM_VERSION);
        systemEntryProps.put(JAVA_VM_VERSION, javaVmVersion);
        LOG.debug("Set search entry " + JAVA_VM_VERSION + " to: " + javaVmVersion);

        final String javaVersion = System.getProperty(JAVA_VERSION);
        systemEntryProps.put(JAVA_VERSION, javaVersion);
        LOG.debug("Set search entry " + JAVA_VERSION + " to: " + javaVersion);

        final String hostname;
        try {
             hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            final String message = "Failed to set hostname";
            LOG.error(message, e);
            System.err.println(message + " - " + e.getMessage());
            throw new RuntimeException(message, e);
        }
        systemEntryProps.put(HOSTNAME, hostname);
        LOG.debug("Set search entry " + HOSTNAME + " to: " + hostname);

        return systemEntryProps;
    }

    public static PropertyEntry[] getPropertiesAsEntryArray(final Properties properties) {
        final List<PropertyEntry> entries = new ArrayList<PropertyEntry>();
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            entries.add(new PropertyEntry((String) entry.getKey(), (String) entry.getValue()));
        }
        return entries.toArray(new PropertyEntry[entries.size()]);
    }

}
