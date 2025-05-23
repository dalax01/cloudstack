// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.upgrade.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseAccessObjectTest {

    @Mock
    private PreparedStatement preparedStatementMock;

    @Mock
    private Connection connectionMock;

    @Mock
    private Logger loggerMock;

    @Mock
    private ResultSet resultSetMock;

    private final DatabaseAccessObject dao = new DatabaseAccessObject();

    @Before
    public void setup() {
        dao.logger = loggerMock;
    }

    @Test
    public void testDropKey() throws Exception {
        when(connectionMock.prepareStatement(contains("DROP KEY"))).thenReturn(preparedStatementMock);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String key = "key";
        boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("successfully"));
    }

    @Test(expected = NullPointerException.class)
    public void testDropKeyWhenConnectionIsNull() throws Exception {
        Connection conn = null;
        String tableName = "tableName";
        String key = "key";
        boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);
    }

    @Test
    public void generateIndexNameTest() {
        String indexName = dao.generateIndexName("mytable","mycolumn1", "mycolumn2");
        Assert.assertEquals( "i_mytable__mycolumn1__mycolumn2", indexName);
    }

    @Test
    public void indexExistsFalseTest() throws Exception {
        when(resultSetMock.next()).thenReturn(false);
        when(connectionMock.prepareStatement(startsWith("SHOW INDEXES FROM"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);

        Connection conn = connectionMock;
        String tableName = "mytable";
        String indexName = "myindex";

        Assert.assertFalse(dao.indexExists(conn, tableName, indexName));
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
    }

    @Test
    public void indexExistsTrueTest() throws Exception {
        when(resultSetMock.next()).thenReturn(true);
        when(connectionMock.prepareStatement(startsWith("SHOW INDEXES FROM"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);

        Connection conn = connectionMock;
        String tableName = "mytable";
        String indexName = "myindex";

        Assert.assertTrue(dao.indexExists(conn, tableName, indexName));
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
    }

    @Test
    public void createIndexTest() throws Exception {
        when(connectionMock.prepareStatement(startsWith("CREATE INDEX"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.execute()).thenReturn(true);

        Connection conn = connectionMock;
        String tableName = "mytable";
        String columnName1 = "mycolumn1";
        String columnName2 = "mycolumn2";
        String indexName = "myindex";

        dao.createIndex(conn, tableName, indexName, columnName1, columnName2);
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).execute();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug("Created index myindex");
    }

    @Test
    public void testDropKeyWhenTableNameIsNull() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("null DROP KEY"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = null;
        String key = "key";
        boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropKeyWhenKeyIsNull() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP KEY null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String key = null;
        boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropKeyWhenKeysAreForeignKeys() throws Exception {
        when(connectionMock.prepareStatement(contains("DROP FOREIGN KEY"))).thenReturn(preparedStatementMock);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String key = "key";
        boolean isForeignKey = true;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("successfully"));
    }

    @Test
    public void testDropKeyWhenPrepareStatementResultsInException() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(any(String.class))).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String key = "key";
        boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(0)).executeUpdate();
        verify(preparedStatementMock, times(0)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropKeyWhenExecuteUpdateResultsInException() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP KEY"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String key = "key";
        boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @SuppressWarnings("static-access")
    @Test
    public void testClosePreparedStatementWhenPreparedStatementIsNull() throws Exception {
        PreparedStatement preparedStatement = null;
        String errorMessage = "some message";

        dao.closePreparedStatement(preparedStatement, errorMessage);

        verify(loggerMock, times(0)).warn(anyString(), any(Throwable.class));
    }

    @SuppressWarnings("static-access")
    @Test
    public void testClosePreparedStatementWhenPreparedStatementIsNotNullAndThereIsNoException() throws Exception {
        PreparedStatement preparedStatement = preparedStatementMock;
        String errorMessage = "some message";

        dao.closePreparedStatement(preparedStatement, errorMessage);

        verify(preparedStatement, times(1)).close();
        verify(loggerMock, times(0)).warn(anyString(), any(Throwable.class));
    }

    @SuppressWarnings("static-access")
    @Test
    public void testClosePreparedStatementWhenPreparedStatementIsNotNullAndThereIsException() throws Exception {
        SQLException sqlException = new SQLException();
        doThrow(sqlException).when(preparedStatementMock).close();

        PreparedStatement preparedStatement = preparedStatementMock;
        String errorMessage = "some message";

        dao.closePreparedStatement(preparedStatement, errorMessage);

        verify(preparedStatement, times(1)).close();
        verify(loggerMock, times(1)).warn(errorMessage, sqlException);
    }

    @Test
    public void testDropPrimaryKey() throws Exception {
        when(connectionMock.prepareStatement(contains("DROP PRIMARY KEY"))).thenReturn(preparedStatementMock);

        Connection conn = connectionMock;
        String tableName = "tableName";

        dao.dropPrimaryKey(conn, tableName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("successfully"));
    }

    @Test(expected = NullPointerException.class)
    public void testDropPrimaryKeyWhenConnectionIsNull() throws Exception {
        Connection conn = null;
        String tableName = "tableName";

        dao.dropPrimaryKey(conn, tableName);
    }

    @Test
    public void testDropPrimaryKeyWhenTableNameIsNull() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("null DROP PRIMARY KEY"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = null;

        dao.dropPrimaryKey(conn, tableName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropPrimaryKeyWhenPrepareStatementResultsInException() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP PRIMARY KEY"))).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = null;

        dao.dropPrimaryKey(conn, tableName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(0)).executeUpdate();
        verify(preparedStatementMock, times(0)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropPrimaryKeyWhenExecuteUpdateResultsInException() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP PRIMARY KEY"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = null;

        dao.dropPrimaryKey(conn, tableName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testColumnExists() throws Exception {
        when(connectionMock.prepareStatement(contains("SELECT"))).thenReturn(preparedStatementMock);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = "columnName";

        dao.columnExists(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString(), any(Throwable.class));
    }

    @Test(expected = NullPointerException.class)
    public void testColumnExistsWhenConnectionIsNull() throws Exception {
        Connection conn = null;
        String tableName = "tableName";
        String columnName = "columnName";

        dao.columnExists(conn, tableName, columnName);
    }

    @Test
    public void testColumnExistsWhenTableNameIsNull() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("FROM null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = null;
        String columnName = "columnName";

        dao.columnExists(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(anyString());
    }

    @Test
    public void testColumnExistsWhenColumnNameIsNull() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("SELECT null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = null;

        dao.columnExists(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(anyString());
    }

    @Test
    public void testDropColumn() throws Exception {
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(0)).executeQuery();
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(anyString());
        verify(loggerMock, times(0)).warn(anyString(), any(Throwable.class));
    }

    @Test(expected = NullPointerException.class)
    public void testDropColumnWhenConnectionIsNull() throws Exception {

        Connection conn = null;
        String tableName = "tableName";
        String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);
    }

    @Test
    public void testDropColumnWhenTableNameIsNull() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("ALTER TABLE null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = null;
        String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString());
        verify(loggerMock, times(1)).warn(anyString(), eq(sqlException));
    }

    @Test
    public void testDropColumnWhenColumnNameIsNull() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP COLUMN null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = null;

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString());
        verify(loggerMock, times(1)).warn(anyString(), eq(sqlException));
    }

    @Test
    public void testDropColumnWhenPrepareStatementResultsInException() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(anyString())).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(0)).executeUpdate();
        verify(preparedStatementMock, times(0)).close();
        verify(loggerMock, times(0)).debug(anyString());
        verify(loggerMock, times(1)).warn(anyString(), eq(sqlException));
    }

    @Test
    public void testDropColumnWhenexecuteUpdateResultsInException() throws Exception {
        SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString());
        verify(loggerMock, times(1)).warn(anyString(), eq(sqlException));
    }

    @Test
    public void testGetColumnType() throws Exception {
        when(connectionMock.prepareStatement(contains("DESCRIBE"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getString("Type")).thenReturn("type");

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = "columnName";

        Assert.assertEquals("type", dao.getColumnType(conn, tableName, columnName));

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString());
    }

    @Test
    public void testAddColumn() throws Exception {
        when(connectionMock.prepareStatement(contains("ADD COLUMN"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenReturn(1);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = "columnName";
        String columnType = "columnType";

        dao.addColumn(conn, tableName, columnName, columnType);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
    }

    @Test
    public void testChangeColumn() throws Exception {
        when(connectionMock.prepareStatement(contains("CHANGE COLUMN"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenReturn(1);

        Connection conn = connectionMock;
        String tableName = "tableName";
        String columnName = "columnName";
        String newColumnName = "columnName2";
        String columnDefinition = "columnDefinition";

        dao.changeColumn(conn, tableName, columnName, newColumnName, columnDefinition);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
    }
}
