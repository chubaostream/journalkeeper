/**
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
 */
package io.journalkeeper.sql.client.support;

import io.journalkeeper.sql.client.SQLClient;
import io.journalkeeper.sql.client.SQLOperator;
import io.journalkeeper.sql.client.SQLTransactionOperator;
import io.journalkeeper.sql.client.helper.ParamHelper;
import io.journalkeeper.sql.exception.SQLException;

import java.util.List;
import java.util.Map;

/**
 * DefaultSQLOperator
 * author: gaohaoxiang
 * date: 2019/8/7
 */
public class DefaultSQLOperator implements SQLOperator {

    private SQLClient client;
    private TransactionIdGenerator transactionIdGenerator;

    public DefaultSQLOperator(SQLClient client) {
        this.client = client;
        this.transactionIdGenerator = new TransactionIdGenerator();
    }

    @Override
    public String insert(String sql, Object... params) {
        client.insert(sql, ParamHelper.toString(params));
        return null;
    }

    @Override
    public int update(String sql, Object... params) {
        client.update(sql, ParamHelper.toString(params));
        return 0;
    }

    @Override
    public int delete(String sql, Object... params) {
        client.delete(sql, ParamHelper.toString(params));
        return 0;
    }

    @Override
    public List<Map<String, String>> query(String sql, Object... params) {
        try {
            return client.query(sql, ParamHelper.toString(params)).get();
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new SQLException(e);
            }
        }
    }

    @Override
    public SQLTransactionOperator beginTransaction() {
        String id = transactionIdGenerator.generate();
        return new DefaultSQLTransactionOperator(id, client);
    }
}