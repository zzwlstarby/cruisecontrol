package net.sourceforge.cruisecontrol.launch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.testutil.TestCase;
import net.sourceforge.cruisecontrol.testutil.TestUtil.FilesToDelete;
import net.sourceforge.cruisecontrol.testutil.TestUtil.PropertiesRestorer;


public class ConfigurationTest extends TestCase {
    
    private FilesToDelete filesToDelete = new FilesToDelete();
    private PropertiesRestorer propRestorer = new PropertiesRestorer();

    @Override
    protected void setUp() throws IOException {
        propRestorer.record();
    }

    @Override
    protected void tearDown() throws Exception {
        filesToDelete.delete();
        propRestorer.restore();
//        // Must also release all properties added here
//        for (String name : System.getProperties().stringPropertyNames()) {
//            if (name.startsWith("cc.")) {
//                System.clearProperty(name);
//            }
//        }
//        final Properties props = System.getProperties(); 
//        for (String name : props.stringPropertyNames()) {
//            if (name.startsWith("cc.")) {
//                props.remove(name);
//            }
//        }
    }

    /** Tests the default values of the options, when not overridden by anything else 
     * @throws LaunchException */
    public void testDefaultVals() throws CruiseControlException, LaunchException {
        final Configuration config = Configuration.getInstance(new String[0]);

        assertEquals("artifacts", config.getOptionRaw(Configuration.KEY_ARTIFACTS));
        assertEquals("lib", config.getOptionRaw(Configuration.KEY_LIBRARY_DIR));
        assertEquals("logs", config.getOptionRaw(Configuration.KEY_LOG_DIR));
        assertEquals("projects", config.getOptionRaw(Configuration.KEY_PROJECTS));
        assertEquals("cruisecontrol.xml", config.getOptionRaw(Configuration.KEY_CONFIG_FILE));
        assertEquals("log4j.properties", config.getOptionRaw(Configuration.KEY_LOG4J_CONFIG));
        assertEquals(false, config.getOptionBool(Configuration.KEY_NO_USER_LIB));
        // libs
        // distDir
        // homeDir
        
        // None was set
        assertFalse(config.wasOptionSet(Configuration.KEY_ARTIFACTS));
        assertFalse(config.wasOptionSet(Configuration.KEY_LIBRARY_DIR));
        assertFalse(config.wasOptionSet(Configuration.KEY_LOG_DIR));
        assertFalse(config.wasOptionSet(Configuration.KEY_PROJECTS));
        assertFalse(config.wasOptionSet(Configuration.KEY_CONFIG_FILE));
        assertFalse(config.wasOptionSet(Configuration.KEY_LOG4J_CONFIG));
        assertFalse(config.wasOptionSet(Configuration.KEY_NO_USER_LIB));

//        public static final String keyNoUserLib = "nouserlib";
//        public static final String keyUserLibDirs = "lib";
//        public static final String keyDistDir = "dist";
//        public static final String keyHomeDir = "home";
//        public static final String keyPrintHelp1 = "help";
//        public static final String keyPrintHelp2 = "?";
//        public static final String keyDebug = "debug";
//        public static final String keyRMIPort = "rmiport";
//        public static final String keyPort = "port"; // deprecated, use keyJMXPort
//        public static final String keyJMXPort = "jmxport";
//        public static final String keyWebPort = "webport";
//        public static final String keyWebAppPath = "webapppath";
//        public static final String keyDashboard = "dashboard";
//        public static final String keyDashboardUrl = "dashboardurl";
//        public static final String keyPostInterval = "postinterval";
//        public static final String keyPostEnabled = "postenabled";
//        public static final String keyXLSPath = "xslpath";
//        public static final String keyJettyXml = "jettyxml";
//        public static final String keyPassword = "password";
//        public static final String keyUser = "user";
//        public static final String keyCCname = "ccname";
//        public static final String keyJmxAgentUtil = "agentutil";
    
    }

    /** Tests the case where there is no -XXX option on the command line
     */
    public void testNoOptName() {
        String args[] = {"opt1", "val1", //  options may be invalid since they are not recognised anyway
                "param", "a value",
                "lib", "path/1/with/subpath",
                "path", "path/3/"};
        
        try {
            Configuration.getInstance(args);
            fail("Exception has been expected");
        } catch (LaunchException e) {
            assertEquals("Unknown option opt1", e.getMessage());
        }
    }

    /** Tests the overwrite of default values through command line arguments */
    public void testArgumentVals() throws LaunchException, CruiseControlException {
        final String[] args = new String[] {
                "-"+Configuration.KEY_ARTIFACTS,  "/tmp/artifacts",
                "-"+Configuration.KEY_LIBRARY_DIR, "/usr/share/cruisecontrol/lib",
                "-"+Configuration.KEY_LOG4J_CONFIG,"/var/spool/cruisecontrol/log4j.config",
                };
        final Configuration config = Configuration.getInstance(args);

        // test changed
        assertEquals("/tmp/artifacts", config.getOptionRaw(Configuration.KEY_ARTIFACTS));
        assertEquals("/usr/share/cruisecontrol/lib", config.getOptionRaw(Configuration.KEY_LIBRARY_DIR));
        assertEquals("/var/spool/cruisecontrol/log4j.config", config.getOptionRaw(Configuration.KEY_LOG4J_CONFIG));
        // Those has been set
        assertTrue(config.wasOptionSet(Configuration.KEY_ARTIFACTS));
        assertTrue(config.wasOptionSet(Configuration.KEY_LIBRARY_DIR));
        assertTrue(config.wasOptionSet(Configuration.KEY_LOG4J_CONFIG));
        // Others must remain
        assertEquals("logs", config.getOptionRaw(Configuration.KEY_LOG_DIR));
        assertEquals("projects", config.getOptionRaw(Configuration.KEY_PROJECTS));
        assertEquals("cruisecontrol.xml", config.getOptionRaw(Configuration.KEY_CONFIG_FILE));
        // So they have not been set
        assertFalse(config.wasOptionSet(Configuration.KEY_LOG_DIR));
        assertFalse(config.wasOptionSet(Configuration.KEY_PROJECTS));
        assertFalse(config.wasOptionSet(Configuration.KEY_CONFIG_FILE));
    }

    /** Tests the overwrite of default values through properties */
    public void testPropertiesVals() throws LaunchException, CruiseControlException {
        System.setProperty("cc."+Configuration.KEY_ARTIFACTS,  "/tmp/cruise/artifacts");
        System.setProperty("cc."+Configuration.KEY_LIBRARY_DIR, "/usr/share/cruise/lib");
        System.setProperty("cc."+Configuration.KEY_LOG4J_CONFIG,"/var/spool/cruise/log4j.conf");
        
        final Configuration config = Configuration.getInstance(new String[0]);

        // test changed
        assertEquals("/tmp/cruise/artifacts", config.getOptionRaw(Configuration.KEY_ARTIFACTS));
        assertEquals("/usr/share/cruise/lib", config.getOptionRaw(Configuration.KEY_LIBRARY_DIR));
        assertEquals("/var/spool/cruise/log4j.conf", config.getOptionRaw(Configuration.KEY_LOG4J_CONFIG));
        // Those has been set
        assertTrue(config.wasOptionSet(Configuration.KEY_ARTIFACTS));
        assertTrue(config.wasOptionSet(Configuration.KEY_LIBRARY_DIR));
        assertTrue(config.wasOptionSet(Configuration.KEY_LOG4J_CONFIG));
        // Others must remain
        assertEquals("logs", config.getOptionRaw(Configuration.KEY_LOG_DIR));
        assertEquals("projects", config.getOptionRaw(Configuration.KEY_PROJECTS));
        assertEquals("cruisecontrol.xml", config.getOptionRaw(Configuration.KEY_CONFIG_FILE));
        // So they have not been set
        assertFalse(config.wasOptionSet(Configuration.KEY_LOG_DIR));
        assertFalse(config.wasOptionSet(Configuration.KEY_PROJECTS));
        assertFalse(config.wasOptionSet(Configuration.KEY_CONFIG_FILE));
    }
    
    public void testConfigVals() throws LaunchException, CruiseControlException, IOException {
        final Map<String, String> opts = new HashMap<String, String>();
        opts.put(Configuration.KEY_ARTIFACTS,  "/home/CC/artifacts");
        opts.put(Configuration.KEY_LIBRARY_DIR, "/usr/share/CC/lib");
        opts.put(Configuration.KEY_LOG4J_CONFIG,"/var/spool/CC/log4j.conf");

        final Element data = makeLauchXML(opts);
        final File xml = storeXML(data, filesToDelete.add("config.xml"));
        final Configuration config = Configuration.getInstance(
                new String[] {"-"+Configuration.KEY_CONFIG_FILE, xml.getAbsolutePath()});

        // test changed
        assertEquals("/home/CC/artifacts", config.getOptionRaw(Configuration.KEY_ARTIFACTS));
        assertEquals("/usr/share/CC/lib", config.getOptionRaw(Configuration.KEY_LIBRARY_DIR));
        assertEquals("/var/spool/CC/log4j.conf", config.getOptionRaw(Configuration.KEY_LOG4J_CONFIG));
        // Those has been set
        assertTrue(config.wasOptionSet(Configuration.KEY_ARTIFACTS));
        assertTrue(config.wasOptionSet(Configuration.KEY_LIBRARY_DIR));
        assertTrue(config.wasOptionSet(Configuration.KEY_LOG4J_CONFIG));
        // Others must remain
        assertEquals("logs", config.getOptionRaw(Configuration.KEY_LOG_DIR));
        assertEquals("projects", config.getOptionRaw(Configuration.KEY_PROJECTS));
        assertEquals("cruisecontrol.xml", config.getOptionRaw(Configuration.KEY_CONFIG_FILE)); // must be default since no other has been specified
        // So they have not been set
        assertFalse(config.wasOptionSet(Configuration.KEY_LOG_DIR));
        assertFalse(config.wasOptionSet(Configuration.KEY_PROJECTS));
        assertFalse(config.wasOptionSet(Configuration.KEY_CONFIG_FILE));
    }
    
    /** Tests various levels of data overriding */
    public void testConfigOverride() throws LaunchException, CruiseControlException, IOException {
        // Configuration file, the lowest priority
        final Map<String, String> opts = new HashMap<String, String>();
        opts.put(Configuration.KEY_ARTIFACTS,  "/home/CC/artifacts");
        opts.put(Configuration.KEY_LIBRARY_DIR, "/usr/share/CC/lib");
        final Element data = makeLauchXML(opts);
        final File xml = storeXML(data, filesToDelete.add("config.xml"));
        // Properties - the highest priority, overrides config file
        System.setProperty("cc."+Configuration.KEY_ARTIFACTS,  "/tmp/cruise/artifacts");
        System.setProperty("cc."+Configuration.KEY_LIBRARY_DIR, "/usr/share/cruisecontrol/lib");
        // command line options - overrides options from config and from properties 
        final String[] args = new String[] {
                "-"+Configuration.KEY_ARTIFACTS,  "/tmp/artifacts",
                "-"+Configuration.KEY_CONFIG_FILE, xml.getAbsolutePath()
                };
        
        // Create the object
        final Configuration config = Configuration.getInstance(args);

        // test changed
        assertEquals("/tmp/artifacts", config.getOptionRaw(Configuration.KEY_ARTIFACTS));
        assertEquals("/usr/share/cruisecontrol/lib", config.getOptionRaw(Configuration.KEY_LIBRARY_DIR));
    }
    
    
    /** Tests if correct path to main config file is returned when the <launcher>...</launcher>
     *  configuration stands on its own and points to an "external" main
     *  <cruisecontrol>...</cruisecontrol> configuration.
     *  
     *  @throws Exception 
     */
    public void testLaunchSeparate() throws Exception {
        // Configuration file, referenced to an external file
        final Map<String, String> opts = new HashMap<String, String>();
        opts.put(Configuration.KEY_CONFIG_FILE,  "/home/CC/mainconfig.xml");
        final Element launch = makeLauchXML(opts);
        final File xml = storeXML(launch, filesToDelete.add("launch.xml"));
        // command line options - overrides options from config 
        final String[] args = new String[] {
                "-"+Configuration.KEY_CONFIG_FILE, xml.getAbsolutePath()
                };
        // Create the object
        final Configuration config = Configuration.getInstance(args);

        // Must return path to the main configuration file! 
        assertEquals("/home/CC/mainconfig.xml", config.getOptionRaw(Configuration.KEY_CONFIG_FILE));
    }
    /** Tests if correct path to main config file is returned when the <launcher>...</launcher> configuration
     *  is embedded in the main <cruisecontrol>...</cruisecontrol> configuration.
     *  
     *  @throws Exception 
     */
    public void testLaunchEmbedded() throws Exception {
        // Configuration file, referenced to an external file
        final Map<String, String> opts = new HashMap<String, String>();
        opts.put(Configuration.KEY_CONFIG_FILE,  "/home/CC/mainconfig.xml");  // should be ignored, even if presented!
        final Element launch = makeLauchXML(opts);
        final Element main = makeConfigXML(launch); // embeds <launcher> to the main config
        final File xml = storeXML(main, filesToDelete.add("cruisecontrol.xml"));
        // command line options - overrides options from config 
        final String[] args = new String[] {
                "-"+Configuration.KEY_CONFIG_FILE, xml.getAbsolutePath()
                };
        // Create the object
        final Configuration config = Configuration.getInstance(args);

        // Must return path to the main configuration file! 
        assertEquals(xml.getAbsolutePath(), config.getOptionRaw(Configuration.KEY_CONFIG_FILE));
    }

    /** Tests if default path to main config file is returned when the <launcher>...</launcher>
     *  configuration stands on its own and DOES NOT point to an "external" main
     *  <cruisecontrol>...</cruisecontrol> configuration.
     *  
     *  @throws Exception 
     */
    public void testConfigNotSet() throws Exception {
        // Configuration file, referenced to an external file
        final Element launch = makeLauchXML(new HashMap<String, String>());
        final File xml = storeXML(launch, filesToDelete.add("launch.xml"));
        // command line options - overrides options from config 
        final String[] args = new String[] {
                "-"+Configuration.KEY_CONFIG_FILE, xml.getAbsolutePath()
                };
        // Create the object
        final Configuration config = Configuration.getInstance(args);

        // Must return default path to the main configuration file! 
        assertEquals("cruisecontrol.xml", config.getOptionRaw(Configuration.KEY_CONFIG_FILE));
    }
    
    public void testBoolArgs() throws Exception {
        Configuration config;
        
        config = Configuration.getInstance(new String[] {"-"+Configuration.KEY_DEBUG, "true"});
        assertTrue(config.getOptionBool(Configuration.KEY_DEBUG));

        config = Configuration.getInstance(new String[] {"-"+Configuration.KEY_DEBUG, "false"});
        assertFalse(config.getOptionBool(Configuration.KEY_DEBUG));

        // No true/false value set, must be true
        config = Configuration.getInstance(new String[] {"-"+Configuration.KEY_DEBUG});
        assertTrue(config.getOptionBool(Configuration.KEY_DEBUG));

        // Default is false to make the previous test meaningful
        config = Configuration.getInstance(new String[] {});
        assertFalse(config.getOptionBool(Configuration.KEY_DEBUG));
    }

    /** Tests the case where an option can be set multiple times in the <launcher>...</launcher>
     *  XML element
     */
    public void testMultiOpts() throws Exception {
        Configuration config;
        List<Map.Entry<String,String>> opts = new ArrayList<Map.Entry<String,String>>(); 

        // Fill with values. Paths does not have to exist since Configuration.getOptionRaw() method
        // is used to ge the value
        opts.add(new AbstractMap.SimpleEntry<String, String>("lib", "path/1"));
        opts.add(new AbstractMap.SimpleEntry<String, String>("lib", "path/1/with/subpath/"));
        opts.add(new AbstractMap.SimpleEntry<String, String>("lib", "path/2"));
        opts.add(new AbstractMap.SimpleEntry<String, String>("lib", "path/3/with/even/more"));
        opts.add(new AbstractMap.SimpleEntry<String, String>("lib", "path_4_with_nonsence"));
        // Make XML config
        File confFile = storeXML(makeLauchXML(opts), filesToDelete.add("launch", ".conf"));
        
        config = Configuration.getInstance(new String[] {"-"+Configuration.KEY_CONFIG_FILE, confFile.getAbsolutePath()});
        assertEquals("path/1" + Configuration.ITEM_SEPARATOR + "path/1/with/subpath/"  + Configuration.ITEM_SEPARATOR +
                     "path/2" + Configuration.ITEM_SEPARATOR + "path/3/with/even/more" + Configuration.ITEM_SEPARATOR +
                     "path_4_with_nonsence",
                     config.getOptionRaw(Configuration.KEY_USER_LIB_DIRS));
    }
    
    /** Tests the case where an option can be set multiple times on the command line, i.e.
     *  <code>-lib path/to/lib/1 -lib path/to/lib/3 -lib path/to/lib/2 ...</code>
     */
    public void testMultiArgs() throws Exception {
        String args[] = {          // Paths does not have to exist since Configuration.getOptionRaw()
                "-lib", "path/1/", //  method is used to get the value
                "-lib", "path/1/with/subpath",
                "-lib", "path/2/",
                "-lib", "path/3/"};
        Configuration config;
        
        config = Configuration.getInstance(args);
        assertEquals("path/1/" + Configuration.ITEM_SEPARATOR + "path/1/with/subpath" + Configuration.ITEM_SEPARATOR +
                     "path/2/" + Configuration.ITEM_SEPARATOR + "path/3/", 
                     config.getOptionRaw(Configuration.KEY_USER_LIB_DIRS));
    }

    /** Tests the case where an option is set multiple times, but an option contains forbidden separator
     */
    public void testMultiArgsInvalid() throws Exception {
        String args[] = {
                "-lib", "path/1/",
                "-lib", "path/1/with/subpath",
                "-lib", "path/2/" + Configuration.ITEM_SEPARATOR + "and/one/more",
                "-lib", "path/3/"};
        
        try {
            Configuration.getInstance(args);
            fail("Exception was expected!");
        } catch(IllegalArgumentException exc) {
            // OK, here
        }
    }
    
    public void testFindFile() throws Exception {
        // Various files
        final File inAbsolutePath = filesToDelete.add("file1.xml");
        final File inWorkingDir = filesToDelete.add(new File("file2.txt"));
        final File inHomeDir = filesToDelete.add(new File(new File(System.getProperty("user.home")).getAbsoluteFile(), "file3.txt"));
        Configuration config;
        String file;
        
        // absolute
        file = inAbsolutePath.getAbsolutePath();
        makeLaunchConfig(inAbsolutePath, file);
        
        config = Configuration.getInstance(new String[] {"-"+Configuration.KEY_CONFIG_FILE, file});
        
        assertTrue(new File(file).isAbsolute());
        assertEquals(inAbsolutePath, config.getOptionFile(Configuration.KEY_CONFIG_FILE));
        
        // in working dir
        file = inWorkingDir.getName();
        makeLaunchConfig(inWorkingDir, file);
        
        config = Configuration.getInstance(new String[] {"-"+Configuration.KEY_CONFIG_FILE, file});
        
        assertFalse(new File(file).isAbsolute());
        assertEquals(inWorkingDir, config.getOptionFile(Configuration.KEY_CONFIG_FILE));
        
        // in home dir
        file = inHomeDir.getName();
        makeLaunchConfig(inHomeDir, file);
        
        config = Configuration.getInstance(new String[] {"-"+Configuration.KEY_CONFIG_FILE, file});
        
        assertFalse(new File(file).isAbsolute());
        assertEquals(inHomeDir, config.getOptionFile(Configuration.KEY_CONFIG_FILE));
    }
    
    
    /** From the set of entries creates string with <launch> ... </launch> XML fragment with values
     *  filled according to the items in the set */
    public static Element makeLauchXML(final Map<String,String> opts) {
        return makeLauchXML(opts.entrySet());
    }
    
    /** From the map, where keys are names of options, creates string with <launch> ... </launch> XML
     *  fragment with values filled according to the map */
    public static Element makeLauchXML(final Collection<Map.Entry<String,String>> opts) {
       Element root = new Element("launcher");
       
       for (Map.Entry<String, String> item : opts) {
           Element conf = new Element(item.getKey());
           conf.setText(item.getValue());
           
           root.addContent(conf);
       }
       return root;
    }

    /** The given <launch> ... </launch> XML element embedds into the <cruisecontrol>...</cruisecontrol>
     *  element */
    public static Element makeConfigXML(final Element launchConf) {
       Element root = new Element("cruisecontrol");
       root.addContent((Element) launchConf.clone());
       
       return root;
    }
    
    /** Stores the given element to the given file */ 
    public static File storeXML(final Element xml, final File file) throws IOException { 
       final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
       
       out.output(new Document(xml), new FileOutputStream(file));
       return file;
    }

    /**
     * Creates the XML file with the following format:
     * <pre>
     *      <launcher>
     *          <configfile>cruiseconfigFname</configfile>
     *      <launcher>
     * </pre>
     * and stores it to launchConfigFname
     * 
     * @param launchConfigFname the file to be created
     * @param cruiseConfigFname the content of <configfile>...</configfile> element
     * @throws IOException if the file cannot be created 
     */
    static
    private void makeLaunchConfig(final File launchConfigFname, final String cruiseConfigFname) throws IOException  {
        Map<String, String> opts = new HashMap<String, String>();
        Element xml;
        
        opts.put(Configuration.KEY_CONFIG_FILE, cruiseConfigFname);
        xml = makeLauchXML(opts);
        
        storeXML(xml, launchConfigFname);
    }
    
}
