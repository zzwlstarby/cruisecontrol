/********************************************************************************
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
 ********************************************************************************/

package net.sourceforge.cruisecontrol.builders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnmappableCharacterException;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Text;

import net.sourceforge.cruisecontrol.Builder;
import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.Progress;
import net.sourceforge.cruisecontrol.gendoc.annotations.Default;
import net.sourceforge.cruisecontrol.gendoc.annotations.Description;
import net.sourceforge.cruisecontrol.gendoc.annotations.DescriptionFile;
import net.sourceforge.cruisecontrol.gendoc.annotations.ExamplesFile;
import net.sourceforge.cruisecontrol.gendoc.annotations.Optional;
import net.sourceforge.cruisecontrol.gendoc.annotations.Required;
import net.sourceforge.cruisecontrol.util.DateUtil;
import net.sourceforge.cruisecontrol.util.IO;
import net.sourceforge.cruisecontrol.util.StreamConsumer;
import net.sourceforge.cruisecontrol.util.Util;
import net.sourceforge.cruisecontrol.util.ValidationHelper;

/**
 * Builder allowing to write text messages into a file.
 * @author Tomas Ausberger, Daniel Tihelka
 */
@DescriptionFile
@ExamplesFile
public class WriterBuilder extends Builder {

    /** UTF8 encoding definition */
    public static final String UTF8 = "UTF-8";

    /** Value set through {@link #setFile(String)} */
    private java.io.File file = null;
    /** Value set through {@link #setWorkingDir(String)} */
    private java.io.File workingDir = null;
    /** Value set through {@link #setEncoding(String)} */
    private String encoding = UTF8;
    /** Value set through {@link #setGzip(boolean)} */
    private boolean gzip = false;
    /** Value set through {@link #setTrim(boolean)} */
    private boolean trim = false;
    /** Value set according to the action passed to {@link #setAction(String)} */
    private boolean overwrite = true;
    /** Value set according to the action passed to {@link #setAction(String)} */
    private boolean append = false;
    /** Value set through {@link #setReplaceChar(String)} */
    private String replaceChar = null;

    /** The list of sub-node-related actions */
    private final LinkedList<Content> messages = new LinkedList<Content>();

    /** Serialization UID */
    private static final long serialVersionUID = -8630377927448150601L;
    /** Logger output */
    private static final Logger LOG = Logger.getLogger(WriterBuilder.class);

    @Override
    public Element build(Map<String, String> properties, Progress progress)
            throws CruiseControlException {
        final long startTime = System.currentTimeMillis();
        final Element status = new Element("writer");
        OutputStream out = null;
        java.io.File f;

        // Resolve properties in the settings. Fail. if they cannot be resolved
        final String fname = Util.parsePropertiesInString(properties, this.file.getAbsolutePath(), true);
        f = new java.io.File(fname);

        try {
            // The output file must not exist
            if (!this.overwrite && f.exists()) {
                throw new IOException("File " + f + " exists but overwrite=false");
            }
            // gzip compression is set on
            if (this.gzip) {
                if (!f.getName().endsWith(".gzip")) {
                    f = new java.io.File(f.getAbsolutePath() + ".gzip");
                }
                out = new GZIPOutputStream(new FileOutputStream(f, this.append));
            // not-compressed file is required
            } else {
                out = new FileOutputStream(f, this.append);
            }

            final StreamConsumer consumer = getStreamConsumer(out, encoding);
            // Pass content to the consumer
            for (Content content : this.messages) {
                BufferedReader input = new BufferedReader(content.getContent(properties));
                String line;
                while ((line = input.readLine()) != null) {
                    consumer.consumeLine(line);
                }
                IO.close(input);
            }
            // Close the output
            consumer.consumeLine(null);

        } catch (Exception exc) {
            status.setAttribute("error", "build failed with exception: " + exc.getMessage());
        } finally {
            IO.close(out);
        }

        final long endTime = System.currentTimeMillis();
        status.setAttribute("time", DateUtil.getDurationAsString((endTime - startTime)));

        return status;
    }

    @Override
    public Element buildWithTarget(Map<String, String> properties,
            String target, Progress progress) throws CruiseControlException {

        // TODO: what to put here?
        return null;
    }

    @Override
    public void validate() throws CruiseControlException {
        // Must be set
        ValidationHelper.assertEncoding(this.encoding, getClass());
        ValidationHelper.assertIsSet(this.file, "file", getClass());
        // Invalid combination
        ValidationHelper.assertFalse(!this.overwrite && this.append, "action not set properly");

        // Set the file
        this.file = joinPath(this.file);
        ValidationHelper.assertIsNotDirectory(this.file, "file", getClass());
        // check, replacement char, if set. Empty string is valid replacement!
        if (this.replaceChar != null) {
            ValidationHelper.assertTrue(this.replaceChar.length() <= 1, "invalid replace char [" + replaceChar + "]");
        }

        if (!this.overwrite) {
            // When overwrite is disabled, the file must not exist
            try {
                ValidationHelper.assertNotExists(this.file, "file", getClass());
            } catch (CruiseControlException e) {
                ValidationHelper.fail("Trying to overwrite file without permission.");
            }
        }

        for (Content c : this.messages) {
            c.validate();
        }
    }

    /**
     * Creates new object to be filled from {@code <msg>...</msg>} element.
     * @return instance of {@link Msg}
     */
    public Object createMsg() {
        return addContent(new Msg());
    }

    /**
     * Creates new object to be filled from {@code <file/>} element.
     * @return instance of {@link File}
     */
    public Object createFile() {
        return addContent(new File());
    }

    @Description("The encoding of the output file. The string must be recognised by Java text "
            + "encoders")
    @Optional
    @Default(WriterBuilder.UTF8)
    @SuppressWarnings("javadoc")
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Description("When set to <tt>true</tt>, the output file will be gzipped")
    @Optional
    @Default("false")
    @SuppressWarnings("javadoc")
    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    @Description("The path to file to write the messages into. When the path is not absolute, "
            + "the path set by <tt>workingdir=''</tt> attribute is prepended, or actual working "
            + "directory is used when <tt>workingdir=''</tt> is not set.")
    @Required
    @SuppressWarnings("javadoc")
    public void setFile(String file) {
        this.file = new java.io.File(file);
    }

    @Description("When set, all white characters are stripped from the beginning and the end "
            + "of each line")
    @Optional
    @Default("false")
    @SuppressWarnings("javadoc")
    public void setTrim(boolean strip) {
        this.trim = strip;
    }

    @Description("When set to <i>overwrite</i>, the messages are written to the file even when it "
            + "does not exist, when set to <i>create</i>, new file is created but the build fails "
            + "when the file already exists, when set to <i>append</i>, the content is appended to "
            + "the existing file or new file is created if it does not exist")
    @Optional
    @Default("overwrite")
    @SuppressWarnings("javadoc")
    public void setAction(String action) {
        action = action.toLowerCase();
        if ("overwrite".equals(action)) {
            overwrite = true;
            append = false;
        } else if ("create".equals(action)) {
            overwrite = false;
            append = false;
        } else if ("append".equals(action)) {
            overwrite = true; // append = overwrite
            append = true;
        } else {
            // Invalid combination, cannot append to non-overwriteable file
            overwrite = false;
            append = true;
        }
    }

    /** @return <code>true</code> when {@link #setAction(String)} is set to <i>overwrite</i> */
    public boolean getOverwrite() {
        return overwrite;
    }

    /** @return <code>true</code> when {@link #setAction(String)} is set to <i>append</i> */
    public boolean getAppend() {
        return append;
    }

    @Description("When set, the path set is prepended to all files used in the builder")
    @Optional
    @SuppressWarnings("javadoc")
    public void setWorkingDir(String path) {
        workingDir = new java.io.File(path);
    }

    /** @return value set by {@link #setWorkingDir(String)} */
    public String getWorkingDir() {
        return workingDir != null ? workingDir.getAbsolutePath() : null;
    }

    /**
     * Implementation of {@link PipedScript#setTimeout(long)}. Does nothing, however.
     * @param val ignored
     */
    public void setTimeout(long val) {
        LOG.warn("timeout=" + val + " ignored");
    }

    @Description("Sets the character to be used when the text cannot be encoded to the required coding. "
           + "Use empty string to ignore the non-mappable characters.")
    @Optional("<i>Unless set, build fails on non-mappable characters.</i>")
    @SuppressWarnings("javadoc")
    public void setReplaceChar(String chr) {
        replaceChar = chr;
    }

    /**
     * Adds the instance of {@link Content} to the end of list of contents.
     * @param obj the instance to add
     * @return the instance
     */
    protected Content addContent(Content obj) {
        this.messages.add(obj);
        return obj;
    }

    /**
     * Creates instance of {@link StreamConsumer} passing data into the given {@link OutputStream}
     * instance. Note that <code>null</code> line must be passed to the consume to close the underlying
     * output!
     *
     * @param out stream to write messages into
     * @param encoding the encoding in which the output is written
     * @return {@link StreamConsumer} instance passing data to the given stream
     */
    protected StreamConsumer getStreamConsumer(final OutputStream out, final String encoding) {
        try {
            final BufferedWriter output = new BufferedWriter(new OutputStreamWriter(out,
                        createEncoder(encoding, replaceChar)));
            // Get consumer passing data to the output stream
            return new StreamConsumer() {
                @Override
                public void consumeLine(String line) {
                    try {
                        if (line == null) {
                            output.close();
                        } else {
                            output.write(line);
                            output.newLine();
                        }
                    } catch (UnmappableCharacterException exc) {
                        // THIS IS A BIT TRICKY: Must wrap it into RuntimeException, since the
                        // StreamConsumer#consumeLine() does not allow any exception to be thrown.
                        throw new RuntimeException("Unmappable characters found when encoding to " + encoding, exc);
                    } catch (IOException exc) {
                        // THIS IS A BIT TRICKY: dtto
                        throw new RuntimeException("Unable to write line \"" + line + "\"", exc);
                    }
                }
            };
        // Encoding was checked in validate(), so this exception should not occure
        } catch (IllegalCharsetNameException exc) {
            LOG.error("Unknown encoding: " + encoding, exc);
            return null;
        }
    }

    /**
     * Joins the given path with the path set through {@link #setWorkingDir(String)}, if it is not
     * absolute already; if no path was set through the method, returns the original path.
     *
     * @param path the path to append to
     * @return new absolute path (or the original path transformed to absolute path)
     */
    protected java.io.File joinPath(java.io.File path) {
        // If working dir was not set or the file is absolute already,
        if (this.workingDir == null || path.isAbsolute()) {
            return path;
        }
        // Join
        return new java.io.File(this.workingDir, path.getPath()).getAbsoluteFile();
    }

    /**
     * Creates action for the given replacement character
     */
    protected static CodingErrorAction createAction(String replaceChar) {
        if (replaceChar == null) {
            return CodingErrorAction.REPORT;
        }
        if ("".equals(replaceChar)) {
            return CodingErrorAction.IGNORE;
        } else {
             return CodingErrorAction.REPLACE;
        }
    }
    /**
     * Creates decoder for the given encoding
     */
    protected static CharsetDecoder createDecoder(String encoding, String replaceChar) {
        final CharsetDecoder decoder = Charset.forName(encoding).newDecoder();
        final CodingErrorAction act = createAction(replaceChar);
        // Configure it
        if (act == CodingErrorAction.REPLACE) {
            decoder.replaceWith(replaceChar); // replacement set and valid
        }
        decoder.onMalformedInput(act);
        decoder.onUnmappableCharacter(act);
        // Get it
        return decoder;
    }
    /**
     * Creates encoder for the given encoding
     */
    protected static CharsetEncoder createEncoder(String encoding, String replaceChar) {
        final CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
        final CodingErrorAction act = createAction(replaceChar);
        // Configure it
        if (act == CodingErrorAction.REPLACE) {
            encoder.replaceWith(replaceChar.getBytes()); // replacement set and valid
        }
        encoder.onMalformedInput(act);
        encoder.onUnmappableCharacter(act);
        // Get it
        return encoder;
    }


    /**
     * Interface allowing to work with {@code <msg></msg>} and {@code <file/>} sub-nodes in the same way.
     */
    public static interface Content {

        /** Returns the content of a message to write embedded in the {@link Reader}
         *  @param properties the additional build-time properties passed to {@link Builder#build(Map, Progress)}
         *  @return the text content as Reader */
        public Reader getContent(final Map<String, String> properties);
        /** Validation
         *  @throws CruiseControlException if not valid*/
        public void validate() throws CruiseControlException;
    }

    @Description("Element holding text to be written to the output file. The text is stored 'as-is' "
            + "stored withing the element, except when <tt>trim='true'</tt> is set in which case "
            + "the white spaces from the beginning and the end of each line are removed")
    @SuppressWarnings("javadoc")
    public final class Msg extends StringWriter implements Content {

        @Override
        public Reader getContent(final Map<String, String> properties) {
            String str = this.toString();
            // Resolve the properties
            try {
                str = Util.parsePropertiesInString(properties, str, false);
            } catch (CruiseControlException e) {
                LOG.warn("Unable to resolve property in " + str + "message", e);
            }
            if (str == null || str.length() == 0) {
                str = "\n";
            }
            return new StringReader(str);
        }

        @Override
        public void validate() throws CruiseControlException {
            // Nothing to validate in fact ...
        }

        /** Set the text content from a XML node.
         *  @param t inner text element of XML {@code <msg></msg>} element. */
        public void xmltext(final Text t) {
            /* if trim is required, split the text and trim each line */
            if (trim) {
                final String[] lines = t.getText().split(System.lineSeparator());
                for  (String s : lines) {
                    this.append(s.trim() + System.lineSeparator());
                }
            } else {
                this.append(t.getText());
            }
        }
    }

    @Description("Element used to configure file to be copied to the output file. The content of the "
            + "file is copied among messages into the position when the attribute is configured")
    @SuppressWarnings("javadoc")
    public final class File implements Content {

        /** The value set by {@link #setFile(String)} */
        private String file;
        /** The value set by {@link #setEncoding(String)} */
        private String encoding = UTF8;

        @Description("The path to file to be copied among the messages. If the path is not absolute, "
                + "it behaves exactly as the <tt>file=''</tt> attribute of the parent builder's node")
        @Required
        public void setFile(String file) {
            this.file = file;
        }

        @Description("The encoding of the file to be read. The string must be recognised by Java "
                + "text encoders")
        @Optional
        @Default(WriterBuilder.UTF8)
        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        @Override
        public Reader getContent(final Map<String, String> properties) {
            java.io.File f = null;
            try {
                // Join with working dir and resolve properties in the path
                f = joinPath(new java.io.File(this.file));
                f = new java.io.File(Util.parsePropertiesInString(properties, f.getAbsolutePath(), true));
                // Get the stream reader
                return new InputStreamReader(new FileInputStream(f), createDecoder(encoding, replaceChar));
            } catch (CruiseControlException e) {
                LOG.warn("Unable to resolve property in " + f.getAbsolutePath() + "message", e);
            } catch (Exception exc) {
                LOG.error("Unable to read data from " + file + " (in " + encoding + ")", exc);
            }
            return new StringReader("");
        }

        @Override
        public void validate() throws CruiseControlException {
            // Validate
            ValidationHelper.assertEncoding(this.encoding, getClass());
            ValidationHelper.assertIsSet(this.file, "file", getClass());
//
//            // Check if exists, ...
//            ValidationHelper.assertExists(this.file, "file", getClass());
//            ValidationHelper.assertIsReadable(this.file, "file", getClass());
//            ValidationHelper.assertIsNotDirectory(this.file, "file", getClass());
        }
    }
}
