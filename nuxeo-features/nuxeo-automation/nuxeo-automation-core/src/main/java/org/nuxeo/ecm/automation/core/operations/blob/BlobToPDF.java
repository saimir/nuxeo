/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ecm.automation.core.operations.blob;

import java.io.Serializable;
import java.util.HashMap;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;

/**
 * Save the input document
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * @author tiry
 */
@Operation(id = BlobToPDF.ID, category = Constants.CAT_CONVERSION, label = "Convert To PDF", description = "Convert the input file to a PDF and return the new file.")
public class BlobToPDF {

    public static final String ID = "Blob.ToPDF";

    @Context
    protected ConversionService service;

    @OperationMethod
    public Blob run(DocumentModel doc) {
        BlobHolder bh = doc.getAdapter(BlobHolder.class);
        if (bh == null) {
            return null;
        }
        if ("application/pdf".equals(bh.getBlob().getMimeType())) {
            return bh.getBlob();
        }
        BlobHolder pdfBh = service.convertToMimeType("application/pdf", bh, new HashMap<String, Serializable>());
        Blob result = pdfBh.getBlob();

        String fname = result.getFilename();
        String filename = bh.getBlob().getFilename();
        if (filename != null && !filename.isEmpty()) {
            // add pdf extension
            int pos = filename.lastIndexOf('.');
            if (pos > 0) {
                filename = filename.substring(0, pos);
            }
            filename += ".pdf";
            result.setFilename(filename);
        } else if (fname != null && !fname.isEmpty()) {
            result.setFilename(fname);
        } else {
            result.setFilename("file");
        }

        result.setMimeType("application/pdf");
        return result;
    }

    @OperationMethod
    public Blob run(Blob blob) {
        if ("application/pdf".equals(blob.getMimeType())) {
            return blob;
        }
        BlobHolder bh = new SimpleBlobHolder(blob);
        bh = service.convertToMimeType("application/pdf", bh, new HashMap<String, Serializable>());
        Blob result = bh.getBlob();
        adjustBlobName(blob, result);
        return result;
    }

    @OperationMethod
    public BlobList run(BlobList blobs) {
        BlobList bl = new BlobList();
        for (Blob blob : blobs) {
            bl.add(this.run(blob));
        }
        return bl;
    }

    protected void adjustBlobName(Blob in, Blob out) {
        String fname = in.getFilename();
        if (fname == null) {
            fname = "Unknown_" + System.identityHashCode(in);
        }
        out.setFilename(fname + ".pdf");
        out.setMimeType("application/pdf");
    }

}
