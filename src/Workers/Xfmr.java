// 
// Decompiled by Procyon v0.5.30
// 

package Workers;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import javax.xml.transform.Source;
import java.io.InputStream;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import javax.xml.transform.TransformerFactory;

public class Xfmr
{
    String xslFileName;
    String inFileName;
    String outFileName;
    
    public Xfmr(final String inFilename, final String outFilename, final String xslFilename) {
        this.xslFileName = "";
        this.inFileName = "";
        this.outFileName = "";
        try {
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            final Transformer xformer = template.newTransformer();
            final Source source = new StreamSource(new FileInputStream(inFilename));
            final Result result = new StreamResult(new FileOutputStream(outFilename));
            xformer.transform(source, result);
        }
        catch (FileNotFoundException e2) {}
        catch (TransformerConfigurationException e3) {}
        catch (TransformerException e) {
            final SourceLocator locator = e.getLocator();
            final int col = locator.getColumnNumber();
            final int line = locator.getLineNumber();
            final String publicId = locator.getPublicId();
            final String systemId = locator.getSystemId();
        }
    }
}
