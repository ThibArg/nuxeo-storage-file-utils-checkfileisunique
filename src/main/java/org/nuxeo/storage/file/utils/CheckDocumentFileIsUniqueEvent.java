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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.RecoverableClientException;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;


/**
 * @author Thibaud Arguillere (Nuxeo)
 *
 * @since 5.9.2
 * Note: Well, will also work with 5.8.
 */
public class CheckDocumentFileIsUniqueEvent implements EventListener {

    private static final Log _myLog = LogFactory.getLog(CheckDocumentFileIsUniqueEvent.class);

    @Override
    public void handleEvent(Event event) throws ClientException {

/*
 * OK, so this works under 2 conditions:
 *      -> The same file drag-dropped => not unique detected, but the exception does not bubble
 *         so the UI just displays "unknown server error" while the log contains the correct
 *         "Duplicate file detected" error.
 *
 *      -> the "unicity" service must not be enabled, it fout la merde totale with the query. or
 *         maybe changing the digest kind from sha256 to md5 will solve the problem.
 *
 */
        if ( DocumentEventTypes.DOCUMENT_CREATED.equals(event.getName())
                || DocumentEventTypes.DOCUMENT_UPDATED.equals(event.getName()) ){

            //_myLog.warn("Handling event \"" + event.getName() + "\"");

            EventContext ctx = event.getContext();

            if (ctx instanceof DocumentEventContext) {
                DocumentEventContext docCtx = (DocumentEventContext) ctx;
                DocumentModel doc = docCtx.getSourceDocument();

                if (!doc.isProxy() && doc.hasSchema("file")) {
                    try {
                        //_myLog.warn("Checking uniqueness of file...");

                       // Using the NXQL utility
                       if(!CheckDocumentFileIsUnique.isUniqueWithNXQL(docCtx.getCoreSession(), doc)) {
                           event.markBubbleException();
                           //event.markRollBack();
                           throw new RecoverableClientException("Duplicate file detected", "Duplicate file detected", null);
                       }
                       //...or this SQL => uncomment the catch SQLException part below
                       /*
                        if(!CheckDocumentFileIsUnique.isUniqueWithSQL(doc)) {
                            _myLog.warn("...file is *not* unique => throwing RecoverableClientException");
                            event.markBubbleException();
                            throw new RecoverableClientException("Duplicate file detected", "Duplicate file detected", null);
                        } else {

                            _myLog.warn("...file is unique.");
                        }
                        */

                    } catch (ClientException e) {
                        e.printStackTrace();
                        event.markBubbleException();
                        throw new RecoverableClientException(e.getMessage(), e.getLocalizedMessage(), null);
                    } /*catch (SQLException e) {
                        e.printStackTrace();
                        event.markBubbleException();
                        throw new RecoverableClientException(e.getMessage(), e.getLocalizedMessage(), null);
                    } */ catch (Exception e) {
                        e.printStackTrace();
                        event.markBubbleException();
                        throw new RecoverableClientException(e.getMessage(), e.getLocalizedMessage(), null);
                    }
                }
            }
        }
    }
}
