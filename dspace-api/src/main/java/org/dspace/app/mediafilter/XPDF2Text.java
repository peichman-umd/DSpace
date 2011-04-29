package org.dspace.app.mediafilter;

/*
 * $HeadURL: $
 *
 * Version: $Revision: $
 *
 * Date: $Date: $
 *
  * Copyright (c) 2002-2009, The DSpace Foundation.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the DSpace Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Utils;

/**
 * Text MediaFilter for PDF sources
 *
 * This filter produces extracted text suitable for building an index,
 * but not for display to end users.
 * It forks a process running the "pdftotext" program from the
 * XPdf suite -- see http://www.foolabs.com/xpdf/
 * This is a suite of open-source PDF tools that has been widely ported
 * to Unix platforms and the ones we use (pdftoppm, pdftotext) even
 * run on Win32.
 *
 * This was written for the FACADE project but it is not directly connected
 * to any of the other FACADE-specific software.  The FACADE UI expects
 * to find thumbnail images for 3D PDFs generated by this filter.
 *
 * Requires DSpace config properties keys:
 *
 *  xpdf.path.pdftotext -- path to "pdftotext" executable (required!)
 *
 * @author Larry Stone
 * @see org.dspace.app.mediafilter.MediaFilter
 */
public class XPDF2Text extends MediaFilter
{
    private static Logger log = Logger.getLogger(XPDF2Text.class);

    // Command to get text from pdf; @infile@, @COMMAND@ are placeholders
    private static final String XPDF_PDFTOTEXT_COMMAND[] =
    {
        "@COMMAND@", "-q", "-enc", "UTF-8", "@infile@", "-"
    };


    // executable path that comes from DSpace config at runtime.
    private String pdftotextPath = null;

    public String getFilteredName(String oldFilename)
    {
        return oldFilename + ".txt";
    }

    public String getBundleName()
    {
        return "TEXT";
    }

    public String getFormatString()
    {
        return "Text";
    }

    public String getDescription()
    {
        return "Extracted Text";
    }

    public InputStream getDestinationStream(InputStream sourceStream)
            throws Exception
    {
        // get configured value for path to XPDF command:
        if (pdftotextPath == null)
        {
            pdftotextPath = ConfigurationManager.getProperty("xpdf.path.pdftotext");
            if (pdftotextPath == null)
                throw new IllegalStateException("No value for key \"xpdf.path.pdftotext\" in DSpace configuration!  Should be path to XPDF pdftotext executable.");
        }

        File sourceTmp = File.createTempFile("DSfilt",".pdf");
        sourceTmp.deleteOnExit();  // extra insurance, we'll delete it here.
        int status = -1;
        try
        {
            // make local temp copy of source PDF since PDF tools
            // require a file for random access.
            // XXX fixme could optimize if we ever get an interface to grab asset *files*
            OutputStream sto = new FileOutputStream(sourceTmp);
            Utils.copy(sourceStream, sto);
            sto.close();
            sourceStream.close();

            String pdfCmd[] = XPDF_PDFTOTEXT_COMMAND.clone();
            pdfCmd[0] = pdftotextPath;
            pdfCmd[4] = sourceTmp.toString();

            log.debug("Running command: "+Arrays.deepToString(pdfCmd));
            Process pdfProc = Runtime.getRuntime().exec(pdfCmd);
            InputStream stdout = pdfProc.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Utils.copy(new BufferedInputStream(stdout), baos);
            stdout.close();
            baos.close();

            status = pdfProc.waitFor();
            String msg = null;
            if (status == 1)
                msg = "pdftotext failed opening input: file="+sourceTmp.toString();
            else if (status == 3)
                msg = "pdftotext permission failure (perhaps copying of text from this document is not allowed - check PDF file's internal permissions): file="+sourceTmp.toString();
            else if (status != 0)
                msg = "pdftotext failed, maybe corrupt PDF? status="+String.valueOf(status);

            if (msg != null)
            {
                log.error(msg);
                throw new IOException(msg);
            }

            return new ByteArrayInputStream(baos.toByteArray());
        }
        catch (InterruptedException e)
        {
            log.error("Failed in pdftotext subprocess: ",e);
            throw e;
        }
        finally
        {
            sourceTmp.delete();
            if (status != 0)
                log.error("PDF conversion proc failed, returns="+status+", file="+sourceTmp);
        }
    }
}

 	  	 
