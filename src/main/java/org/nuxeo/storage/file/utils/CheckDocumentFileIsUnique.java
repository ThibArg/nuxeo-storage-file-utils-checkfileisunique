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
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.api.ConnectionHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Thibaud Arguillere
 *
 * @since 5.9.2
 * Note: Well, will also work with 5.8.
 *
 * WARNING: This plug-in should not be used if Unicity Service is enabled
 *
 */
public class CheckDocumentFileIsUnique {

    private static final Log _myLog = LogFactory.getLog(CheckDocumentFileIsUnique.class);

    // ==================================================
    // Using NXQL
    // ==================================================
    private static final String kSTATEMENT_NXQL = "SELECT * FROM Document "
                                                + "WHERE ecm:currentLifeCycleState != 'deleted' "
                                                + "  AND ecm:isCheckedInVersion = 0 "
                                                + "  AND file:content/data = '%s' "
                                                + "  AND ecm:uuid != '%s'";

    public static boolean isUniqueWithNXQL(CoreSession inSession, DocumentModel inDoc) throws ClientException {

        // What do we do with null?
        if(inSession == null) {
            throw new IllegalArgumentException("inSession should not be null");
        }
        if(inDoc == null) {
            throw new IllegalArgumentException("inDoc should not be null");
        }

        // Nothing if it's a proxy. It's ok, and not a duplicate
        if(inDoc.isImmutable() || inDoc.isProxy()) {
            return true;
        }

        // If we have no file, let's consider it's ok
        if(!inDoc.hasSchema("file")) {
            return true;
        }
        Blob theFile = (Blob) inDoc.getPropertyValue("file:content");
        if(theFile == null) {
            return true;
        }

        //_myLog.warn("CHECKCKING UNIQUENESS...");

        String statementStr = String.format(kSTATEMENT_NXQL,
                                            theFile.getDigest(),
                                            inDoc.getId());
        //_myLog.warn("... with statement:\n" + statementStr);

        DocumentModelList result = inSession.query(statementStr, 1);
        //_myLog.warn("CHECKCKING UNIQUENESS, is empty = " + result.isEmpty());

        return result.isEmpty();
    }

    // ==================================================
    // Using SQL
    // ==================================================
    // Using NXQL is recommended, it handles all the JOIN for you
    private static final String kSTATEMENT_SQL = "SELECT COUNT(*) FROM content c"
                                           + " LEFT JOIN hierarchy h ON h.id = c.id"
                                           + " LEFT JOIN misc m ON m.id = h.parentid" // In this context, we join on parent id, not id
                                           + " WHERE     c.data = '%s'" // Not c.digest. Or c.digest if "unicity" service is enabled?
                                           + "       AND h.parentid <> '%s'"
                                           + "       AND m.lifecyclestate <> 'deleted' "
                                           + "       AND h.isversion IS NULL";

    public static boolean isUniqueWithSQL(DocumentModel inDoc) throws SQLException, ClientException {

        // What do we do with null?
        if(inDoc == null) {
            throw new IllegalArgumentException("inDoc should not be null");
        }

        // Nothing if it's a proxy. It's ok, and not a duplicate
        if(inDoc.isImmutable() || inDoc.isProxy()) {
            return true;
        }

        // If we have no file, let's consider it's ok
        if(!inDoc.hasSchema("file")) {
            return true;
        }
        Blob theFile = (Blob) inDoc.getPropertyValue("file:content");
        if(theFile == null) {
            return true;
        }

        //_myLog.warn("CHECKCKING UNIQUENESS...");

        //OK, now we query
        Connection co = ConnectionHelper.getConnection(null);
        try {
            String statementStr = String.format(kSTATEMENT_SQL,
                                                    theFile.getDigest(),
                                                    inDoc.getId());
            //_myLog.warn("... with statement:\n" + statementStr);

            //Statement st = co.createStatement();
            Statement st = co.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery(statementStr);

            int count = -1;
            if(rs.first()) {
                count = rs.getInt(1);
            } else {
                throw new SQLException("Could not retrieve the COUNT(*) column");
            }

            //_myLog.warn("CHECKCKING UNIQUENESS, count = " + count + " (0 => unique)");

            return count == 0;

        } catch (SQLException e) {
            _myLog.warn("CHECKCKING UNIQUENESS, SQLException " + e);
            throw e;
        } finally {
            if(co != null) {
                co.close();
            }
        }
    }
}
