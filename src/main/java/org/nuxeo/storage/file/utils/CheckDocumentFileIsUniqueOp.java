/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thibaud Arguillere (Nuxeo)
 */

package org.nuxeo.storage.file.utils;

import java.sql.SQLException;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;


//====


/**
 * @author Thibaud Arguillere (Nuxeo)
 *
 * @since 5.9.2
 */
@Operation(id=CheckDocumentFileIsUniqueOp.ID, category=Constants.CAT_DOCUMENT, label="Check document's file uniqueness", description="Check if the file (from file:content) of this document is unique or not, and set the varName context variable accordingly. If current document does not have the file schema, of does not have a file, the varName is set to true. The document is returned unchanged")
public class CheckDocumentFileIsUniqueOp {

    public static final String ID = "CheckDocumentFileUniqueness";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Param(name = "varName", required = true)
    protected String varName;

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) throws PropertyException, ClientException, SQLException {

        boolean isUnique = CheckDocumentFileIsUnique.isUniqueWithNXQL(session, input);
        ctx.put( varName, isUnique );
        return input;

        /*
        boolean isUnique = CheckDocumentFileUniqueness.isUniqueWithSQL(input);
        ctx.put( varName, isUnique );
        return input;
        */
    }

}
